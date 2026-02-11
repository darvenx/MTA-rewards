import { ChangeDetectionStrategy, Component, OnDestroy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { BehaviorSubject, Subject } from 'rxjs';
import { finalize, takeUntil } from 'rxjs/operators';
import { mapLoginSuccessToSessionUser } from '../../core/api/api-mappers';

@Component({
  selector: 'app-signup',
  standalone: false,
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent implements OnDestroy {
  signupForm: FormGroup;
  hidePassword = true;
  // Reactive loading indicator used with async pipe in template
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
    if (this.signupForm.valid) {
      this.isLoading$.next(true);
      // Map form values to DTO expected by backend (UserSignUpDto)
      // Form: firstName, lastName, username, email, phoneNumber, password
      // DTO: phoneNumber, email, username, password, firstName, lastName
      // Explicitly map form controls to the DTO expected by backend
      const signupData = {
        phoneNumber: this.signupForm.get('phoneNumber')?.value,
        email: this.signupForm.get('email')?.value,
        username: this.signupForm.get('username')?.value,
        password: this.signupForm.get('password')?.value,
        firstName: this.signupForm.get('firstName')?.value,
        lastName: this.signupForm.get('lastName')?.value
      };

      this.authService.signup(signupData)
        .pipe(
          finalize(() => this.isLoading$.next(false)),
          takeUntil(this.destroyed$)
        )
        .subscribe({
          next: (res) => {
            // Defensive check for response structure
            if (!res || !res.token) {
              console.error('Invalid signup response:', res);
              this.snackBar.open('Signup succeeded but received invalid response. Please try logging in.', 'Close', {
                duration: 4000,
                panelClass: ['error-snackbar']
              });
              this.router.navigate(['/login']);
              return;
            }

            // Handle successful signup - normalise to SessionUser and persist
            const session = mapLoginSuccessToSessionUser(res, signupData.username);
            this.authService.setSession(session);

            this.snackBar.open('Account created successfully! Logging in...', 'Close', {
              duration: 3000,
              panelClass: ['success-snackbar']
            });
            // Redirect to dashboard
            this.router.navigate(['/dashboard']);
          },
          error: (err) => {
            console.error('Signup API error:', err);
            // Defensive handling for non-JSON or unexpected error shapes
            try {
              // If backend sends a plain string message (e.g. "User Already Exists"), handle it
              if (err && err.status === 400 && err.error && typeof err.error === 'string') {
                const msg = err.error as string;
                // common case: username/email already exists
                if (msg.toLowerCase().includes('already')) {
                  // set server error on username to indicate duplication
                  const control = this.signupForm.get('username');
                  if (control) control.setErrors({ serverError: msg });
                } else {
                  this.snackBar.open('Signup Failed: ' + msg, 'Close', { duration: 3000, panelClass: ['error-snackbar'] });
                }
                return;
              }

              if (err && err.status === 400 && err.error && typeof err.error === 'object') {
                const payload = err.error;
                // Server may return structured field errors
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
                  Object.keys(payload.errors).forEach(field => {
                    const control = this.signupForm.get(field);
                    if (control) control.setErrors({ serverError: payload.errors[field] });
                  });
                  return;
                }

                // If payload has a message, show it
                if (payload.message) {
                  this.snackBar.open('Signup Failed: ' + payload.message, 'Close', {
                    duration: 3000,
                    panelClass: ['error-snackbar']
                  });
                  return;
                }
              }
            } catch (e) {
              // swallow parsing errors and fall through to generic message
            }

            // Fallback generic message
            const generic = (err && err.error && err.error.message) ? err.error.message : (err && err.message) ? err.message : 'Please try again';
            this.snackBar.open('Signup Failed: ' + generic, 'Close', {
              duration: 3000,
              panelClass: ['error-snackbar']
            });
          }
        });
    }
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
    this.isLoading$.complete();
  }
}
