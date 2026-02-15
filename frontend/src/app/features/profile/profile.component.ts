import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router } from '@angular/router';
import { Observable, Subject, filter, take, takeUntil } from 'rxjs';
import { finalize, timeout } from 'rxjs/operators';

import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';

import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { Account } from '../../core/models/account.model';
import { accountsData } from '../../core/models/accounts-data.model';

type AccountFilterType = 'SAVINGS' | 'CURRENT';

interface ProfileAccountCard {
  accountId: number;
  balance: number;
  accountType: string;
  accountStatus: string;
}

interface ReadOnlyProfileData {
  fullName: string;
  email: string;
  phone: string;
  address: string;
}

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatFormFieldModule,
    MatDividerModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    MatSelectModule
  ],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit, OnDestroy {
  account$?: Observable<Account>;
  profileData: ReadOnlyProfileData = {
    fullName: 'User',
    email: 'Not Available',
    phone: 'Not Available',
    address: 'Not Available'
  };

  avatarPreview: string | null = null;
  private avatarObjectUrl: string | null = null;
  loadingProfile = false;
  loadingAccounts = false;
  lastLogin = '13 Feb 2026, 10:30 AM';
  username = localStorage.getItem('holderName') || '';

  selectedAccountType: AccountFilterType = 'SAVINGS';
  selectedAccountId: number | null = null;
  selectedAccount: ProfileAccountCard | null = null;
  accounts: ProfileAccountCard[] = [];
  filteredAccounts: ProfileAccountCard[] = [];
  accountNumberOptions: ProfileAccountCard[] = [];
  private readonly togglingAccountIds = new Set<number>();
  private readonly destroyed$ = new Subject<void>();

  constructor(
    private accountService: AccountService,
    private auth: AuthService,
    private router: Router,
    private snack: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.reloadProfileData();
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd => event instanceof NavigationEnd),
        takeUntil(this.destroyed$)
      )
      .subscribe((event) => {
        if (event.urlAfterRedirects.startsWith('/profile')) {
          this.reloadProfileData();
        }
      });
  }

  private resolveUserId(): string | null {
    return localStorage.getItem('id') || sessionStorage.getItem('id') || this.auth.getAccountId();
  }

  private reloadProfileData(): void {
    const userId = this.resolveUserId();
    if (!userId) {
      this.loadingProfile = false;
      this.loadingAccounts = false;
      return;
    }

    this.loadingProfile = true;
    this.account$ = this.accountService.getAccount(userId);
    this.loadAccounts(userId);

    this.account$.pipe(take(1)).subscribe({
      next: (account) => {
        this.username = account.username || this.username;
        this.profileData = {
          fullName: account.holderName || this.username || 'User',
          email: account.email || 'Not Available',
          phone: account.phone || 'Not Available',
          address: account.address || 'Not Available'
        };
        this.loadingProfile = false;
      },
      error: (err) => {
        this.loadingProfile = false;
        this.snack.open(this.extractApiErrorMessage(err, 'Could not load profile details.'), 'Close', {
          duration: 100
        });
      }
    });
  }

  private loadAccounts(userId: string, preferredAccountId?: number): void {
    this.loadingAccounts = true;
    this.accountService.getBalance(userId).pipe(take(1)).subscribe({
      next: (data) => {
        this.accounts = this.mapAccountsData(data);
        const preferredAccount = this.accounts.find((account) => account.accountId === preferredAccountId);

        if (preferredAccount) {
          this.selectedAccountType = preferredAccount.accountType === 'CURRENT' ? 'CURRENT' : 'SAVINGS';
          this.selectedAccountId = preferredAccount.accountId;
        } else if (this.accounts.length > 0 && !this.accounts.some((a) => a.accountType === this.selectedAccountType)) {
          this.selectedAccountType = this.accounts[0].accountType === 'CURRENT' ? 'CURRENT' : 'SAVINGS';
        }
        this.applyAccountTypeFilter();
        this.loadingAccounts = false;
      },
      error: (err) => {
        this.accounts = [];
        this.filteredAccounts = [];
        this.loadingAccounts = false;
        this.snack.open(this.extractApiErrorMessage(err, 'Could not load account list.'), 'Close', {
          duration: 3200
        });
      }
    });
  }

  private mapAccountsData(data: accountsData | null | undefined): ProfileAccountCard[] {
    if (!data) {
      return [];
    }

    if (Array.isArray(data.accountIds) && data.accountIds.length > 0) {
      return data.accountIds.map((accountId, index) => {
        const balance = Array.isArray(data.balances) ? Number(data.balances[index] ?? 0) : 0;
        const type = Array.isArray(data.accountType) ? String(data.accountType[index] ?? 'SAVINGS') : 'SAVINGS';
        const status = Array.isArray(data.accountStatus) ? String(data.accountStatus[index] ?? 'ACTIVE') : 'ACTIVE';

        return {
          accountId,
          balance: Number.isFinite(balance) ? balance : 0,
          accountType: type.toUpperCase(),
          accountStatus: status.toUpperCase()
        };
      });
    }

    // Fallback for legacy/single-account response shapes.
    const legacy = data as unknown as {
      id?: number | string;
      accountNumber?: number | string;
      balance?: number;
      type?: string;
      status?: string;
    };
    const accountId = Number(legacy.accountNumber ?? legacy.id ?? 0);
    if (!accountId) {
      return [];
    }

    return [
      {
        accountId,
        balance: Number.isFinite(legacy.balance ?? 0) ? Number(legacy.balance ?? 0) : 0,
        accountType: String(legacy.type ?? 'SAVINGS').toUpperCase(),
        accountStatus: String(legacy.status ?? 'ACTIVE').toUpperCase()
      }
    ];
  }

  onAccountTypeChange(value: AccountFilterType): void {
    this.selectedAccountType = value;
    this.applyAccountTypeFilter();
  }

  onAccountNumberChange(value: number | string): void {
    const parsedId = Number(value);
    if (!Number.isFinite(parsedId)) {
      return;
    }
    this.selectedAccountId = parsedId;
    this.updateSelectedAccount();
  }

  private applyAccountTypeFilter(): void {
    this.filteredAccounts = this.accounts.filter((item) => item.accountType === this.selectedAccountType);
    this.accountNumberOptions = [...this.filteredAccounts];

    if (this.accountNumberOptions.length === 0) {
      this.selectedAccountId = null;
      this.selectedAccount = null;
      return;
    }

    if (!this.selectedAccountId || !this.accountNumberOptions.some((account) => account.accountId === this.selectedAccountId)) {
      this.selectedAccountId = this.accountNumberOptions[0].accountId;
    }

    this.updateSelectedAccount();
  }

  private updateSelectedAccount(): void {
    this.selectedAccount =
      this.filteredAccounts.find((account) => account.accountId === this.selectedAccountId) || null;
  }

  isTogglingAccountStatus(accountId: number): boolean {
    return this.togglingAccountIds.has(accountId);
  }

  isAccountActive(accountStatus: string): boolean {
    return String(accountStatus).toUpperCase() === 'ACTIVE';
  }

  getAccountToggleLabel(accountStatus: string): string {
    return this.isAccountActive(accountStatus) ? 'Deactivate Account' : 'Reactivate Account';
  }

  toggleAccountStatus(account: ProfileAccountCard): void {
    if (this.isTogglingAccountStatus(account.accountId)) {
      return;
    }

    this.togglingAccountIds.add(account.accountId);
    const isActive = this.isAccountActive(account.accountStatus);
    const actionLabel = isActive ? 'deactivated' : 'reactivated';
    const updatedStatus = isActive ? 'LOCKED' : 'ACTIVE';

    this.accountService
      .toggleAccountStatus(account.accountId)
      .pipe(
        take(1),
        timeout(10000),
        finalize(() => this.togglingAccountIds.delete(account.accountId))
      )
      .subscribe({
        next: (isSuccess) => {
          if (!isSuccess) {
            this.snack.open('Could not update account status. Please try again.', 'Close', { duration: 3200 });
            return;
          }

          this.accounts = this.accounts.map((item) =>
            item.accountId === account.accountId ? { ...item, accountStatus: updatedStatus } : item
          );
          this.applyAccountTypeFilter();
          this.updateSelectedAccount();

          this.snack.open(`Account ${account.accountId} ${actionLabel}.`, 'Close', { duration: 3200 });
        },
        error: (err) => {
          this.snack.open(this.extractApiErrorMessage(err, 'Account status update failed.'), 'Close', {
            duration: 3200
          });
        }
      });
  }

  private extractApiErrorMessage(err: unknown, fallback: string): string {
    if (!err) {
      return fallback;
    }

    const httpErr = err as {
      error?: unknown;
      message?: unknown;
    };

    if (typeof httpErr.error === 'string' && httpErr.error.trim()) {
      return httpErr.error;
    }

    if (httpErr.error && typeof httpErr.error === 'object') {
      const payload = httpErr.error as {
        message?: unknown;
        error?: unknown;
        fieldErrors?: Array<{ field?: string; message?: string }>;
      };

      if (typeof payload.message === 'string' && payload.message.trim()) {
        return payload.message;
      }

      if (typeof payload.error === 'string' && payload.error.trim()) {
        return payload.error;
      }

      if (Array.isArray(payload.fieldErrors) && payload.fieldErrors.length > 0) {
        const firstFieldError = payload.fieldErrors.find((item) => item?.message)?.message;
        if (firstFieldError) {
          return firstFieldError;
        }
      }
    }

    if (typeof httpErr.message === 'string' && httpErr.message.trim()) {
      return httpErr.message;
    }

    return fallback;
  }

  onAvatarSelected(evt: Event): void {
    const input = evt.target as HTMLInputElement;
    if (!input.files || !input.files[0]) return;
    const file = input.files[0];

    // Object URLs are faster than base64 conversion for immediate preview.
    if (this.avatarObjectUrl) {
      URL.revokeObjectURL(this.avatarObjectUrl);
      this.avatarObjectUrl = null;
    }

    this.avatarObjectUrl = URL.createObjectURL(file);
    this.avatarPreview = this.avatarObjectUrl;
  }

  getInitials(name?: string | null): string {
    if (!name) return 'U';
    return name
      .split(' ')
      .map((n) => n[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  }

  logout(): void {
    this.auth.logout();
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
    if (this.avatarObjectUrl) {
      URL.revokeObjectURL(this.avatarObjectUrl);
      this.avatarObjectUrl = null;
    }
  }
}
