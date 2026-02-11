import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Observable, take, of } from 'rxjs';
import { delay } from 'rxjs/operators';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { Account } from '../../core/models/account.model';
import { User } from '../../core/models/user-data.model';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatDividerModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {
  account$!: Observable<User>;
  balance$!: Observable<number>;

  profileForm!: FormGroup;
  passwordForm!: FormGroup;

  avatarPreview: string | null = null;
  loadingProfile = false;
  savingProfile = false;
  lastLogin = localStorage.getItem('lastLogin') || '';
  username = localStorage.getItem('holderName') || '';

  constructor(
    private fb: FormBuilder,
    private accountService: AccountService,
    private auth: AuthService,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    const accountId = localStorage.getItem("id");
    this.initForms();
    console.log("accountId" + accountId);

    if (accountId) {
      this.loadingProfile = true;
      this.account$ = this.accountService.getAccount(accountId);
      // this.balance$ = this.accountService.getBalance(accountId);

      // populate when available
      this.account$.pipe(take(1)).subscribe(acc => {
        this.profileForm.patchValue({
          fullName: acc.fullName,
          email: (acc as any).email || '',
          phone: (acc as any).phoneNumber || '',
          address: (acc as any).address || ''
        });
        this.loadingProfile = false;
      }, () => this.loadingProfile = false);
    }
  }

  private initForms() {
    this.profileForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.maxLength(100)]],
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.minLength(6)]],
      address: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', [Validators.required]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordsMatch });
  }

  private passwordsMatch(group: FormGroup) {
    const a = group.get('newPassword')?.value;
    const b = group.get('confirmPassword')?.value;
    return a === b ? null : { mismatch: true };
  }

  onAvatarSelected(evt: Event) {
    const input = evt.target as HTMLInputElement;
    if (!input.files || !input.files[0]) return;
    const file = input.files[0];
    const reader = new FileReader();
    reader.onload = () => this.avatarPreview = reader.result as string;
    reader.readAsDataURL(file);
    // In production you'd upload the file to the server here
  }

  getInitials(name?: string | null): string {
    if (!name) return 'U';
    return name
      .split(' ')
      .map(n => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  copyAccountNumber(accountNumber: string | null | undefined) {
    if (!accountNumber) return;
    try { navigator.clipboard.writeText(accountNumber); } catch (e) { /* ignore */ }
  }

  saveProfile() {
    if (!this.profileForm.valid || !this.profileForm.dirty) return;
    this.savingProfile = true;
    const payload = this.profileForm.value;
    // call API (mocked) and notify
    const accountId = this.auth.getAccountId();
    if (!accountId) return;
    // AccountService does not implement update in this demo; simulate a request
    of(null).pipe(delay(700)).pipe(take(1)).subscribe(() => {
      this.savingProfile = false;
      this.profileForm.markAsPristine();
      this.snack.open('Profile updated', 'Close', { duration: 3000 });
    }, () => {
      this.savingProfile = false;
      this.snack.open('Unable to update profile', 'Close', { duration: 3000 });
    });
  }

  cancelChanges() {
    this.profileForm.reset();
    // repopulate from account
    this.account$.pipe(take(1)).subscribe(acc => {
      this.profileForm.patchValue({ fullName: acc.fullName, email: (acc as any).email || '', phone: (acc as any).phone || '', address: (acc as any).address || '' });
    });
  }

  updatePassword() {
    if (!this.passwordForm.valid) return;
    // call change password API (mock)
    const accountId = this.auth.getAccountId();
    if (!accountId) return;
    // Simulate password change API call
    of(null).pipe(delay(700)).pipe(take(1)).subscribe(() => {
      this.snack.open('Password updated', 'Close', { duration: 3000 });
      this.passwordForm.reset();
      this.passwordForm.markAsPristine();
    }, () => this.snack.open('Unable to update password', 'Close', { duration: 3000 }));
  }

  logout() { this.auth.logout(); }
}
