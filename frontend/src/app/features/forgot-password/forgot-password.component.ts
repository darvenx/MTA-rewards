import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { ApiUserUpdatePasswordRequest } from '../../core/api/backend-contracts';
import { extractApiErrorMessage } from '../../core/utils/http-error.util';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, MatFormFieldModule, MatInputModule, MatButtonModule, MatIconModule, MatToolbarModule, MatCardModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  form: FormGroup;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private snackBar: MatSnackBar,
    private router: Router
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/^[0-9]{10}$/)]],
      // oldPassword: ['', [Validators.required, Validators.minLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]]
    });
  }

  close() {
    // As a full page, closing takes the user back to login.
    this.router.navigate(['/login']);
  }

  submit() {
    if (this.form.invalid) return;

    this.isLoading = true;
    const payload: ApiUserUpdatePasswordRequest = {
      username: this.form.value.username,
      phoneNumber: this.form.value.phoneNumber,
      // oldPassword: this.form.value.oldPassword,
      newPassword: this.form.value.newPassword
    };

    this.authService.forgotPassword(payload).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('Password reset successfully!', 'Close', {
          duration: 3000,
          panelClass: ['success-snackbar']
        });
        this.close();
      },
      error: (err: unknown) => {
        this.isLoading = false;
        console.error('Password reset error:', err);
        const message = extractApiErrorMessage(err, 'Unable to reset password. Please try again.');
        this.snackBar.open(`Reset Failed: ${message}`, 'Close', {
          duration: 4000,
          panelClass: ['error-snackbar']
        });
      }
    });
  }
}
