import { Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BehaviorSubject, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { AuthService } from '../../services/auth.service';
import { ApiUserSuccessLoginOrSignUpDto } from '../../core/api/backend-contracts';
import { SessionUser } from '../../core/models/session-user.model';

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
        error: (err) => {
          console.error('Signup API error:', err);
          try {
            if (err && err.status === 400 && err.error && typeof err.error === 'string') {
              const msg = err.error as string;
              if (msg.toLowerCase().includes('already')) {
                const control = this.signupForm.get('username');
                if (control) control.setErrors({ serverError: msg });
              } else {
                this.snackBar.open('Signup Failed: ' + msg, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
              }
              return;
            }

            if (err && err.status === 400 && err.error && typeof err.error === 'object') {
              const payload = err.error;

              if (payload.fieldErrors && Array.isArray(payload.fieldErrors)) {
                payload.fieldErrors.forEach((fe: any) => {
                  const control = this.signupForm.get(fe.field);
                  if (control) {
                    control.setErrors({ serverError: fe.message });
                  }
                });
                return;
              }

              if (payload.errors && typeof payload.errors === 'object') {
                Object.keys(payload.errors).forEach((field) => {
                  const control = this.signupForm.get(field);
                  if (control) control.setErrors({ serverError: payload.errors[field] });
                });
                return;
              }

              if (payload.message) {
                this.snackBar.open('Signup Failed: ' + payload.message, 'Close', {
                  duration: 3000,
                  panelClass: ['error-snackbar']
                });
                return;
              }
            }
          } catch {
            // fall through to generic message
          }

          const generic =
            err && err.error && err.error.message
              ? err.error.message
              : err && err.message
                ? err.message
                : 'Please try again';
          this.snackBar.open('Signup Failed: ' + generic, 'Close', {
            duration: 3000,
            panelClass: ['error-snackbar']
          });
        }
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
