import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { mapLoginSuccessToSessionUser } from '../../core/api/api-mappers';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loginForm: FormGroup;
  hidePassword = true;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.loginForm = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
  }

  openForgotPassword(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/forgot-password']);
  }

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.isLoading = true;
      const { username, password } = this.loginForm.value;
      console.log(username,password);
      this.authService.login(username, password).subscribe({
        next: (res) => {
          console.log(res);
          this.isLoading = false;
          sessionStorage.setItem("id",String(res.id));
          console.log(sessionStorage);
          // Defensive check for response structure
          if (!res || !res.token) {
            console.error('Invalid login response:', res);
            this.snackBar.open('Login succeeded but received invalid response. Please try again.', 'Close', {
              duration: 4000,
              panelClass: ['error-snackbar']
            });
            return;
          }

          // Map transport DTO -> UI session model and persist via AuthService
          const session = mapLoginSuccessToSessionUser(res, username);
          this.authService.setSession(session);

          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          this.isLoading = false;
          console.error('Login error:', err);

          // Defensive error handling to prevent silent crashes
          try {
            // Handle different error status codes
            if (err?.status === 400) {
              // Bad request - validation error
              const message = err.error?.message || 'Invalid login data. Please check your credentials.';
              this.snackBar.open('Login Failed: ' + message, 'Close', {
                duration: 4000,
                panelClass: ['error-snackbar']
              });
            } else if (err?.status === 401) {
              // Unauthorized - wrong credentials
              this.snackBar.open('Invalid username or password', 'Close', {
                duration: 4000,
                panelClass: ['error-snackbar']
              });
            } else if (err?.status === 0) {
              // Network error - server unreachable
              this.snackBar.open('Cannot connect to server. Please check your connection.', 'Close', {
                duration: 5000,
                panelClass: ['error-snackbar']
              });
            } else if (err?.status >= 500) {
              // Server error
              this.snackBar.open('Server error. Please try again later.', 'Close', {
                duration: 4000,
                panelClass: ['error-snackbar']
              });
            } else {
              // Generic fallback
              const message = err?.error?.message || err?.message || 'Something went wrong. Please try again.';
              this.snackBar.open('Login Failed: ' + message, 'Close', {
                duration: 4000,
                panelClass: ['error-snackbar']
              });
            }
          } catch (e) {
            // Final safety net - if error parsing fails
            console.error('Error handling failed:', e);
            this.snackBar.open('An unexpected error occurred. Please try again.', 'Close', {
              duration: 4000,
              panelClass: ['error-snackbar']
            });
          }
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }
}
