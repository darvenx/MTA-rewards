import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ApiUserSuccessLoginOrSignUpDto } from '../../core/api/backend-contracts';
import { SessionUser } from '../../core/models/session-user.model';
import { extractApiErrorMessage, extractApiFieldErrors } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-signup',
  standalone: false,
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnDestroy {
  signupForm: FormGroup;
  hidePassword = true;
  isLoading$ = new BehaviorSubject<boolean>(false);

  private destroyed$ = new Subject<void>();

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {
    this.signupForm = this.fb.group({
      firstName: ['', Validators.required],
      lastName: ['', Validators.required],
      username: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      password: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  onSubmit(): void {
    if (!this.signupForm.valid) {
      return;
    }

    this.clearServerErrors();
    this.isLoading$.next(true);

    const signupData = {
      phoneNumber: this.signupForm.get('phoneNumber')?.value,
      email: this.signupForm.get('email')?.value,
      username: this.signupForm.get('username')?.value,
      password: this.signupForm.get('password')?.value,
      firstName: this.signupForm.get('firstName')?.value,
      lastName: this.signupForm.get('lastName')?.value
    };

    this.authService
      .signup(signupData)
      .pipe(finalize(() => this.isLoading$.next(false)), takeUntil(this.destroyed$))
      .subscribe({
        next: (res) => {
          if (!res || !res.token || !res.id) {
            this.snackBar.open('Signup succeeded but received invalid response. Please try logging in.', 'Close', {
              duration: 4000,
              panelClass: ['error-snackbar']
            });
            this.router.navigate(['/login']);
            return;
          }

          this.finishSignup(res, signupData.username, this.getPrimaryAccountNumber(res));
        },
        error: (err: unknown) => {
          console.error('Signup API error:', err);
          const fieldErrors = extractApiFieldErrors(err);
          if (fieldErrors.length > 0) {
            fieldErrors.forEach((item) => {
              if (!item.field) {
                return;
              }
              const control = this.signupForm.get(item.field);
              if (!control) {
                return;
              }
              control.setErrors({
                ...(control.errors ?? {}),
                serverError: item.message
              });
              control.markAsTouched();
            });

            const firstFieldMessage = fieldErrors[0]?.message;
            if (firstFieldMessage) {
              this.snackBar.open(`Signup Failed: ${firstFieldMessage}`, 'Close', {
                duration: 3000,
                panelClass: ['error-snackbar']
              });
            }
            return;
          }

          const generic = extractApiErrorMessage(err, 'Please try again');
          if (generic.toLowerCase().includes('already')) {
            const usernameControl = this.signupForm.get('username');
            if (usernameControl) {
              usernameControl.setErrors({
                ...(usernameControl.errors ?? {}),
                serverError: generic
              });
              usernameControl.markAsTouched();
            }
          }

          this.snackBar.open('Signup Failed: ' + generic, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  private clearServerErrors(): void {
    Object.keys(this.signupForm.controls).forEach((field) => {
      const control = this.signupForm.get(field);
      if (!control?.errors || !control.errors['serverError']) {
        return;
      }

      const nextErrors = { ...control.errors };
      delete nextErrors['serverError'];
      control.setErrors(Object.keys(nextErrors).length > 0 ? nextErrors : null);
    });
  }

  private finishSignup(res: ApiUserSuccessLoginOrSignUpDto, holderName: string, accountNumber?: number): void {
    const session: SessionUser = {
      token: res.token,
      holderName,
      accountId: String(accountNumber ?? res.id)
    };

    this.authService.setSession(session);
    localStorage.setItem('id', String(res.id));
    sessionStorage.setItem('id', String(res.id));

    this.snackBar.open('Account created successfully! Logging in...', 'Close', {
      duration: 3000,
      panelClass: ['success-snackbar']
    });

    this.router.navigate(['/dashboard']);
  }

  private getPrimaryAccountNumber(res: ApiUserSuccessLoginOrSignUpDto): number | undefined {
    const accountNumbers = (res as any).accountNumbers;
    if (Array.isArray(accountNumbers) && accountNumbers.length > 0 && typeof accountNumbers[0] === 'number') {
      return accountNumbers[0];
    }

    // Backend compatibility: some implementations return `accounts` instead.
    const accounts = (res as any).accounts;
    if (Array.isArray(accounts) && accounts.length > 0 && typeof accounts[0] === 'number') {
      return accounts[0];
    }

    return undefined;
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
    this.isLoading$.complete();
  }
}
