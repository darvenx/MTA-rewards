import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NavigationEnd, Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { mapLoginSuccessToSessionUser } from '../../core/api/api-mappers';
import { Subject, filter, takeUntil } from 'rxjs';
import { getRoleFromLoginResponse } from '../../core/utils/auth-role.util';
import { extractApiErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy {
  loginForm: FormGroup;
  hidePassword = true;
  isLoading = false;
  isAdminLoginMode = false;
  private readonly destroyed$ = new Subject<void>();

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

  ngOnInit(): void {
    this.updateAdminMode(this.router.url);
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntil(this.destroyed$)
      )
      .subscribe((event) => this.updateAdminMode(event.urlAfterRedirects));
  }

  private updateAdminMode(url: string): void {
    this.isAdminLoginMode = url.startsWith('/admin-log') || url.startsWith('/admin-login');
  }

  openForgotPassword(event: Event): void {
    event.preventDefault();
    this.router.navigate(['/forgot-password']);
  }

  goToAdminLogin(): void {
    this.router.navigate(['/admin-log']);
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
          // Defensive check for response structure
          if (!res || !res.token) {
            console.error('Invalid login response:', res);
            this.snackBar.open('Login succeeded but received invalid response. Please try again.', 'Close', {
              duration: 4000,
              panelClass: ['error-snackbar']
            });
            return;
          }

          const loggedInRole = getRoleFromLoginResponse(res);
          if (this.isAdminLoginMode && loggedInRole !== 'ADMIN') {
            this.snackBar.open('Only admin users can log in from this page.', 'Close', {
              duration: 4200,
              panelClass: ['error-snackbar']
            });
            return;
          }

          // Map transport DTO -> UI session model and persist via AuthService
          const session = mapLoginSuccessToSessionUser(res, username);
          this.authService.setSession(session);
          sessionStorage.setItem("id", String(res.id));
          localStorage.setItem("id", String(res.id));
          console.log(sessionStorage);

          this.router.navigate([this.isAdminLoginMode ? '/admin' : '/dashboard']);
        },
        error: (err: unknown) => {
          this.isLoading = false;
          console.error('Login error:', err);
          const message = extractApiErrorMessage(err, 'Unable to log in. Please try again.');
          this.snackBar.open(`Login Failed: ${message}`, 'Close', {
            duration: 4000,
            panelClass: ['error-snackbar']
          });
        },
        complete: () => {
          this.isLoading = false;
        }
      });
    }
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }
}
