import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { Account } from '../../core/models/account.model';
import { Observable } from 'rxjs';
import { Transaction, TransactionType, TransactionStatus } from '../../core/models/transaction.model';
import { User } from '../../core/models/user-data.model';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  account$: Observable<Account> | undefined;
  // balance stream is converted to an animated UI value to keep
  // business data intact while providing a richer visual.
  animatedBalance = 0;
  isBalanceLoaded = false;
  showBalance = true;
  lastLogin = localStorage.getItem('lastLogin') || 'Jan 31, 10:42 PM';
  // Totals computed from transactions
  totalSent = 0;
  totalReceived = 0;
  thisMonth = 0; // net (received - sent) for current month

  // UI-facing animated stats
  displayTotalSent = 0;
  displayTotalReceived = 0;
  displayThisMonth = 0;

  // Simple UI loading flags (no additional API calls)
  isActionsLoading = false;
  isStatsLoading = true;
  hasTransactions = false;

  // Mocked unread count for notification bell – logic unchanged,
  // just surfaced as a property for animation/styling.
  unreadCount = 3;

  constructor(
    private accountService: AccountService,
    private authService: AuthService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const accountId = this.authService.getAccountId();
    let user:User;
    if (accountId) {
      // user = this.accountService.getAccount(accountId);
      // Fetch balance once and animate the UI value
      // this.accountService.getBalance(accountId).subscribe({
      //   next: (value) => {
      //     this.animateValue((v) => (this.animatedBalance = v), 0, value);
      //     this.isBalanceLoaded = true;
      //   },
      //   error: () => {
      //     this.isBalanceLoaded = true;
      //   }
      // });

      // Fetch transactions and compute totals for the quick-stats
      this.accountService.getTransactions(accountId).subscribe({
        next: (txns) => {
          this.hasTransactions = !!txns && txns.length > 0;
          this.computeTotals(txns);
          this.isStatsLoading = false;
        },
        error: () => {
          // keep totals at zero on error
          this.isStatsLoading = false;
        }
      });

      // quick actions load instantly for now but the flag lets us
      // show skeletons without fake delays if needed.
      this.isActionsLoading = false;
    }
  }

  private computeTotals(transactions: Transaction[]): void {
    const now = new Date();
    const currentMonth = now.getMonth();
    const currentYear = now.getFullYear();

    let sent = 0;
    let received = 0;
    let monthNet = 0;

    transactions.forEach(t => {
      // Only count successful transactions
      if (t.transactionStatus !== TransactionStatus.SUCCESS) return;

      if (t.type === TransactionType.DEBIT) {
        sent += t.amount;
      } else if (t.type === TransactionType.CREDIT) {
        received += t.amount;
      }

      const d = new Date(t.date);
      if (d.getFullYear() === currentYear && d.getMonth() === currentMonth) {
        // for month net, treat credits as positive and debits as negative
        monthNet += (t.type === TransactionType.CREDIT ? t.amount : -t.amount);
      }
    });

    this.totalSent = sent;
    this.totalReceived = received;
    this.thisMonth = monthNet;

    // Animate UI-facing numbers while keeping the raw totals
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

  // Simple count-up animation utility used for balances and stats.
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
      return;
    }

    const step = (now: number) => {
      const elapsed = now - startTime;
      const progress = Math.min(elapsed / duration, 1);
      const eased = 1 - Math.pow(1 - progress, 3); // ease-out
      const current = start + range * eased;
      setter(Number(current.toFixed(2)));

      if (progress < 1) {
        requestAnimationFrame(step);
      }
    };

    requestAnimationFrame(step);
  }

  copyAccountNumber(acc?: string) {
    if (!acc) return;
    try { navigator.clipboard.writeText(acc); } catch (e) { /* ignore */ }
    // show a small toast
    try { const snack = (window as any).ng?.getInjector?.() } catch {}
  }

  logout(): void {
    this.authService.logout();
  }

  navigateTo(path: string): void {
    this.router.navigate([path]);
  }
}
