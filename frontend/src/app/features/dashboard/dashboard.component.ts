import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { Account } from '../../core/models/account.model';
import { Observable, Subject } from 'rxjs';
import { takeUntil, tap } from 'rxjs/operators';
import { ApiAccountSuccessCreation } from '../../core/api/backend-contracts';
import { Transaction, TransactionType, TransactionStatus } from '../../core/models/transaction.model';

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

  unreadCount = 3;
  currentUserId: number | null = null;
  currentHolderName = 'User';
  private destroyed$ = new Subject<void>();

  constructor(
    private accountService: AccountService,
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
      this.account$ = this.accountService.getAccount(userId).pipe(
        tap((account) => {
          if (account?.holderName) {
            this.currentHolderName = account.holderName;
          }
        })
      );
      this.loadDashboardData(userId);
    }
  }

  private loadDashboardData(userId: string): void {
    this.isStatsLoading = true;
    this.isBalanceLoaded = false;

    const balance$ = this.accountService.getBalance(userId);
    const transactions$ = this.accountService.getTransactions(userId);

    import('rxjs').then(({ forkJoin }) => {
      forkJoin({
        balance: balance$,
        transactions: transactions$
      })
        .pipe(takeUntil(this.destroyed$))
        .subscribe({
          next: ({ balance, transactions }) => {
            if (balance && balance.balances && balance.balances.length > 0) {
              const firstAccountBalance = balance.balances[0];

              // animate balance
              this.animateValue(
                (v) => (this.animatedBalance = v),
                0,
                firstAccountBalance
              );
            }

            this.isBalanceLoaded = true;

            this.hasTransactions = !!transactions && transactions.length > 0;
            this.computeTotals(transactions);
            this.isStatsLoading = false;
          },
          error: (err) => {
            console.error('Error fetching dashboard data:', err);
            this.isBalanceLoaded = true;
            this.isStatsLoading = false;
          }
        });
    });
  }

  private computeTotals(transactions: Transaction[]): void {
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

    this.animateValue((v) => (this.displayTotalSent = v), 0, this.totalSent);
    this.animateValue((v) => (this.displayTotalReceived = v), 0, this.totalReceived);
    this.animateValue((v) => (this.displayThisMonth = v), 0, this.thisMonth);
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
      this.cdr.detectChanges();  // ensure UI updates
      return;
    }

    const step = (now: number) => {
      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3);
      const current = start + range * eased;

      setter(Number(current.toFixed(2)));
      this.cdr.detectChanges();   // ✅ critical fix

      if (progress < 1) {
        requestAnimationFrame(step);
      }
    };

    requestAnimationFrame(step);
  }

  copyAccountNumber(acc?: string) {
    if (!acc) return;
    try { navigator.clipboard.writeText(acc); } catch (e) {}
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

          const createdAccountNumber = this.getCreatedAccountNumber(res);
          const successMessage = createdAccountNumber
            ? `Account created successfully: ${createdAccountNumber}`
            : 'Account created successfully';

          this.snackBar.open(successMessage, 'Close', {
            duration: 3500,
            panelClass: ['success-snackbar']
          });
        },
        error: (err) => {
          this.isCreatingAccount = false;
          const message = err?.error?.message || err?.message || 'Unable to create account';
          this.snackBar.open('Account creation failed: ' + message, 'Close', {
            duration: 3500,
            panelClass: ['error-snackbar']
          });
        }
      });
  }

  private getCreatedAccountNumber(res: ApiAccountSuccessCreation): number | null {
    if (res && Array.isArray(res.accountNumbers) && res.accountNumbers.length > 0) {
      return res.accountNumbers[0];
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
