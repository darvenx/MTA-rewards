import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { Account } from '../../core/models/account.model';
import { Observable, Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';
import { ApiAccountSuccessCreation } from '../../core/api/backend-contracts';
import { Transaction, TransactionType, TransactionStatus } from '../../core/models/transaction.model';
import { accountsData } from '../../core/models/accounts-data.model';
import { extractApiErrorMessage } from '../../core/utils/http-error.util';

interface DashboardAccountOption {
  accountId: number;
  balance: number;
  accountType: string;
  accountStatus: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  account$: Observable<Account> | undefined;
  addAccountForm: FormGroup;
  accountTypeOptions: string[] = ['SAVINGS', 'CURRENT'];
  showAddAccountForm = false;
  isCreatingAccount = false;

  animatedBalance = 0;
  isBalanceLoaded = false;
  showBalance = true;
  lastLogin = localStorage.getItem('lastLogin') || 'Jan 31, 10:42 PM';

  totalSent = 0;
  totalReceived = 0;
  thisMonth = 0;

  displayTotalSent = 0;
  displayTotalReceived = 0;
  displayThisMonth = 0;

  isActionsLoading = false;
  isStatsLoading = true;
  hasTransactions = false;

  availableAccounts: DashboardAccountOption[] = [];
  accountSelectOptions: DashboardAccountOption[] = [];
  selectedAccountId: number | null = null;
  selectedAccountType = 'SAVINGS';
  selectedAccountStatus = 'ACTIVE';

  currentUserId: number | null = null;
  currentHolderName = 'User';
  private statsRequestId = 0;
  private destroyed$ = new Subject<void>();

  constructor(
    private accountService: AccountService,
    private authService: AuthService,
    private router: Router,
    private fb: FormBuilder,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef   // ✅ Added
  ) {
    this.addAccountForm = this.fb.group({
      accountType: ['SAVINGS', Validators.required]
    });
  }

  ngOnInit(): void {
    const userId = localStorage.getItem("id");
    if (userId) {
      this.currentUserId = Number(userId);
      const persistedAccountId = Number(this.authService.getAccountId());
      this.selectedAccountId = Number.isFinite(persistedAccountId) && persistedAccountId > 0
        ? persistedAccountId
        : Number(userId);
      this.syncAccountSelectOptions();
      this.account$ = this.accountService.getAccount(userId).pipe(
        tap((account) => {
          if (account?.holderName) {
            this.currentHolderName = account.holderName;
          }
          const accountNumber = Number(account?.accountNumber);
          if (Number.isFinite(accountNumber) && accountNumber > 0) {
            this.selectedAccountId = accountNumber;
            this.syncAccountSelectOptions();
          }
        })
      );
      this.loadDashboardData(userId);
    }
  }

  private loadDashboardData(userId: string): void {
    this.isStatsLoading = true;
    this.isBalanceLoaded = false;
    this.accountService.getUserAccounts(userId)
      .pipe(takeUntil(this.destroyed$))
      .subscribe({
        next: (accounts) => {
          const mappedAccounts = this.mapAccountsData(accounts);
          this.availableAccounts = this.mergeAccounts(this.availableAccounts, mappedAccounts);
          this.syncAccountSelectOptions();
          const initialAccount =
            this.availableAccounts.find((account) => account.accountId === this.selectedAccountId) ||
            this.availableAccounts.find((account) => account.accountStatus === 'ACTIVE');

          if (initialAccount) {
            this.setSelectedAccount(initialAccount.accountId, false);
          } else {
            this.selectedAccountId = null;
            this.animatedBalance = 0;
            this.isBalanceLoaded = true;
            this.hasTransactions = false;
            this.computeTotals([], false);
            this.isStatsLoading = false;
          }
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error fetching dashboard data:', err);
          this.snackBar.open(
            extractApiErrorMessage(err, 'Could not load account details right now.'),
            'Close',
            { duration: 3500, panelClass: ['error-snackbar'] }
          );
          setTimeout(() => {
            this.syncAccountSelectOptions();
            this.isBalanceLoaded = true;
            this.hasTransactions = false;
            this.computeTotals([], false);
            this.isStatsLoading = false;
            this.cdr.markForCheck();
          }, 0);
        }
      });
  }

  private mapAccountsData(data: accountsData | null | undefined): DashboardAccountOption[] {
    if (!data) return [];

    const raw = data as unknown as Record<string, unknown>;
    const ids = this.toNumberArray(
      raw['accountIds'] ?? raw['accounts'] ?? raw['accountNumbers'] ?? raw['accountId']
    );
    const balances = this.toNumberArray(raw['balances'] ?? raw['accountBalance']);
    const types = this.toStringArray(raw['accountType'] ?? raw['accountTypes'] ?? raw['types']);
    const statuses = this.toStringArray(raw['accountStatus'] ?? raw['accountStatuses'] ?? raw['statuses']);

    const mapped = ids.map((accountId, idx) => ({
      accountId,
      balance: Number.isFinite(balances[idx]) ? balances[idx] : 0,
      accountType: (types[idx] || 'SAVINGS').toUpperCase(),
      accountStatus: (statuses[idx] || 'ACTIVE').toUpperCase()
    }));

    if (mapped.length > 0) {
      return mapped;
    }

    // Single-account payload fallback.
    const singleAccountId = Number(raw['accountNumber'] ?? raw['id'] ?? raw['accountId'] ?? 0);
    if (!Number.isFinite(singleAccountId) || singleAccountId <= 0) {
      return [];
    }
    const singleBalance = Number(raw['balance'] ?? raw['accountBalance'] ?? 0);

    return [
      {
        accountId: singleAccountId,
        balance: Number.isFinite(singleBalance) ? singleBalance : 0,
        accountType: String(raw['type'] ?? raw['accountType'] ?? 'SAVINGS').toUpperCase(),
        accountStatus: String(raw['status'] ?? raw['accountStatus'] ?? 'ACTIVE').toUpperCase()
      }
    ];
  }

  private mapAccountCreationData(data: ApiAccountSuccessCreation | null | undefined): DashboardAccountOption[] {
    if (!data || !Array.isArray(data.accountNumbers) || data.accountNumbers.length === 0) {
      return [];
    }

    return data.accountNumbers
      .map((rawId, idx) => {
        const accountId = Number(rawId);
        if (!Number.isFinite(accountId) || accountId <= 0) {
          return null;
        }

        const balance = Number(Array.isArray(data.accountBalance) ? data.accountBalance[idx] : 0);
        const accountType = String(Array.isArray(data.accountType) ? data.accountType[idx] ?? 'SAVINGS' : 'SAVINGS').toUpperCase();
        const accountStatus = String(Array.isArray(data.accountStatus) ? data.accountStatus[idx] ?? 'ACTIVE' : 'ACTIVE').toUpperCase();

        return {
          accountId,
          balance: Number.isFinite(balance) ? balance : 0,
          accountType,
          accountStatus
        } as DashboardAccountOption;
      })
      .filter((account): account is DashboardAccountOption => account !== null);
  }

  private mergeAccounts(
    existing: DashboardAccountOption[],
    incoming: DashboardAccountOption[]
  ): DashboardAccountOption[] {
    const merged = new Map<number, DashboardAccountOption>();

    existing.forEach((account) => {
      merged.set(account.accountId, account);
    });

    incoming.forEach((account) => {
      merged.set(account.accountId, account);
    });

    return Array.from(merged.values()).sort((a, b) => a.accountId - b.accountId);
  }

  private toNumberArray(input: unknown): number[] {
    if (!Array.isArray(input)) return [];
    return input
      .map((value) => Number(value))
      .filter((value) => Number.isFinite(value));
  }

  private toStringArray(input: unknown): string[] {
    if (!Array.isArray(input)) return [];
    return input.map((value) => String(value));
  }

  onAccountSelectionChange(selectedId: number | string): void {
    const parsedId = Number(selectedId);
    if (!Number.isFinite(parsedId)) return;
    this.setSelectedAccount(parsedId, true);
  }

  private setSelectedAccount(accountId: number, animateFromCurrent: boolean): void {
    const selected = this.availableAccounts.find((account) => account.accountId === accountId);

    if (!selected) {
      this.isBalanceLoaded = true;
      this.isStatsLoading = false;
      this.cdr.markForCheck();
      return;
    }

    this.selectedAccountId = selected.accountId;
    this.selectedAccountType = selected.accountType;
    this.selectedAccountStatus = selected.accountStatus;
    this.authService.setActiveAccountId(String(selected.accountId));

    this.animateValue(
      (value) => (this.animatedBalance = value),
      animateFromCurrent ? this.animatedBalance : 0,
      selected.balance
    );
    this.isBalanceLoaded = true;
    this.syncAccountSelectOptions();
    this.loadStatsForAccount(selected.accountId);
  }

  private loadStatsForAccount(accountId: number): void {
    this.isStatsLoading = true;
    const requestId = ++this.statsRequestId;

    this.accountService.getTransactions(String(accountId))
      .pipe(takeUntil(this.destroyed$))
      .subscribe({
        next: (transactions) => {
          if (requestId !== this.statsRequestId) return;

          this.hasTransactions = Array.isArray(transactions) && transactions.length > 0;
          this.computeTotals(transactions);
          this.isStatsLoading = false;
          this.cdr.markForCheck();
        },
        error: (err) => {
          if (requestId !== this.statsRequestId) return;

          console.error(`Error fetching transactions for account ${accountId}:`, err);
          this.snackBar.open(
            extractApiErrorMessage(err, 'Could not load transactions for the selected account.'),
            'Close',
            { duration: 3500, panelClass: ['error-snackbar'] }
          );
          this.hasTransactions = false;
          this.computeTotals([], true);
          this.isStatsLoading = false;
          this.cdr.markForCheck();
        }
      });
  }

  private syncAccountSelectOptions(): void {
    if (this.availableAccounts.length > 0) {
      this.accountSelectOptions = this.availableAccounts;
      return;
    }

    const fallbackAccountId = this.selectedAccountId ?? this.currentUserId;
    if (!fallbackAccountId) {
      this.accountSelectOptions = [];
      return;
    }

    this.accountSelectOptions = [
      {
        accountId: fallbackAccountId,
        balance: this.animatedBalance,
        accountType: this.selectedAccountType || 'SAVINGS',
        accountStatus: this.selectedAccountStatus || 'ACTIVE'
      }
    ];
  }

  private computeTotals(transactions: Transaction[], animateFromCurrent = true): void {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    let sent = 0;
    let received = 0;
    let monthNet = 0;

    transactions.forEach(t => {
      if (t.transactionStatus !== TransactionStatus.SUCCESS) return;

      if (t.type === TransactionType.DEBIT) {
        sent += t.amount;
      } else if (t.type === TransactionType.CREDIT) {
        received += t.amount;
      }

      const d = new Date(t.date);
      if (d.getFullYear() === currentYear && d.getMonth() === currentMonth) {
        monthNet += (t.type === TransactionType.CREDIT ? t.amount : -t.amount);
      }
    });

    this.totalSent = sent;
    this.totalReceived = received;
    this.thisMonth = monthNet;

    this.animateValue(
      (v) => (this.displayTotalSent = v),
      animateFromCurrent ? this.displayTotalSent : 0,
      this.totalSent
    );
    this.animateValue(
      (v) => (this.displayTotalReceived = v),
      animateFromCurrent ? this.displayTotalReceived : 0,
      this.totalReceived
    );
    this.animateValue(
      (v) => (this.displayThisMonth = v),
      animateFromCurrent ? this.displayThisMonth : 0,
      this.thisMonth
    );
  }

  getGreeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Good Morning';
    if (h < 18) return 'Good Afternoon';
    return 'Good Evening';
  }

  toggleBalanceVisibility(): void {
    this.showBalance = !this.showBalance;
  }

  // ✅ FIXED animation method
  private animateValue(
    setter: (value: number) => void,
    start: number,
    end: number,
    duration: number = 600
  ): void {
    const startTime = performance.now();
    const range = end - start;

    if (duration <= 0 || range === 0) {
      setter(end);
      this.cdr.markForCheck();
      return;
    }

    const step = (now: number) => {
      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const current = start + range * eased;

      setter(Number(current.toFixed(2)));
      this.cdr.markForCheck();

      if (progress < 1) {
        requestAnimationFrame(step);
      }
    };

    requestAnimationFrame(step);
  }

  copyAccountNumber(acc?: string) {
    if (!acc) return;
    try { navigator.clipboard.writeText(acc); } catch (e) { }
  }

  toggleAddAccountForm(): void {
    this.showAddAccountForm = !this.showAddAccountForm;
    if (!this.showAddAccountForm) {
      this.addAccountForm.reset({ accountType: 'SAVINGS' });
    }
  }

  createAccount(): void {
    if (this.addAccountForm.invalid) {
      this.addAccountForm.markAllAsTouched();
      return;
    }

    if (!this.currentUserId) {
      this.snackBar.open('User session not found. Please log in again.', 'Close', {
        duration: 3000,
        panelClass: ['error-snackbar']
      });
      return;
    }

    this.isCreatingAccount = true;

    const payload = {
      accountHolderName: this.currentHolderName || 'User',
      accountType: this.addAccountForm.value.accountType,
      userId: this.currentUserId
    };

    this.accountService
      .createAccount(payload)
      .pipe(takeUntil(this.destroyed$))
      .subscribe({
        next: (res) => {
          this.isCreatingAccount = false;
          this.showAddAccountForm = false;
          this.addAccountForm.reset({ accountType: 'SAVINGS' });

          const createdAccounts = this.mapAccountCreationData(res);
          if (createdAccounts.length > 0) {
            this.availableAccounts = this.mergeAccounts(this.availableAccounts, createdAccounts);
            this.syncAccountSelectOptions();
          }

          const createdAccountNumber = this.getCreatedAccountNumber(res);
          const successMessage = createdAccountNumber
            ? `Account created successfully: ${createdAccountNumber}`
            : 'Account created successfully';

          this.snackBar.open(successMessage, 'Close', {
            duration: 3500,
            panelClass: ['success-snackbar']
          });

          if (createdAccountNumber) {
            this.setSelectedAccount(createdAccountNumber, true);
          }

          this.loadDashboardData(String(this.currentUserId));
        },
        error: (err) => {
          this.isCreatingAccount = false;
          const message = extractApiErrorMessage(err, 'Unable to create account');
          this.snackBar.open('Account creation failed: ' + message, 'Close', {
            duration: 3500,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  private getCreatedAccountNumber(res: ApiAccountSuccessCreation): number | null {
    if (res && Array.isArray(res.accountNumbers) && res.accountNumbers.length > 0) {
      const parsedNumbers = res.accountNumbers
        .map((value) => Number(value))
        .filter((value) => Number.isFinite(value) && value > 0);

      if (parsedNumbers.length > 0) {
        return Math.max(...parsedNumbers);
      }
    }
    return null;
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
  }
}
