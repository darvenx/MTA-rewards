import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';

import { AccountService } from '../../services/account.service';
import { ApiRecentTransactionsDto } from '../../core/api/backend-contracts';
import { Transaction, TransactionType } from '../../core/models/transaction.model';
import { catchError, finalize, map, of, timeout } from 'rxjs';
import { extractApiErrorMessage } from '../../core/utils/http-error.util';

type StatusFilter = 'ALL' | 'SUCCESS' | 'FAILED';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatButtonModule, MatProgressSpinnerModule],
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {
  isLoading = true;
  errorMessage: string | null = null;
  transactions: ApiRecentTransactionsDto[] = [];

  searchTerm = '';
  statusFilter: StatusFilter = 'ALL';
  lastUpdatedLabel = '--';
  usedFallbackData = false;

  constructor(
    private accountService: AccountService,
    private cdr: ChangeDetectorRef,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadRecentTransactions();
  }

  loadRecentTransactions(): void {
    this.isLoading = true;
    this.errorMessage = null;
    this.usedFallbackData = false;
    const userId = localStorage.getItem('id') || sessionStorage.getItem('id');
    const loadingWatchdog = window.setTimeout(() => {
      if (!this.isLoading) return;
      this.usedFallbackData = true;
      this.errorMessage = null;
      this.transactions = this.getStaticFallbackTransactions();
      this.lastUpdatedLabel = new Date().toLocaleString('en-IN', {
        dateStyle: 'medium',
        timeStyle: 'short'
      });
      this.isLoading = false;
      this.cdr.detectChanges();
    }, 9000);

    this.accountService
      .getRecentTransactions()
      .pipe(
        timeout(7000),
        catchError((error: unknown) => {
          console.error('Primary recent-transactions API failed:', error);
          this.snackBar.open(
            extractApiErrorMessage(error, 'Could not load live recent transactions.'),
            'Close',
            { duration: 3500, panelClass: ['error-snackbar'] }
          );

          if (!userId) {
            this.errorMessage = 'Could not load recent transactions right now.';
            return of([] as ApiRecentTransactionsDto[]);
          }

          this.usedFallbackData = true;
          return this.accountService.getTransactions(userId).pipe(
            timeout(7000),
            map((items) => this.mapTransactionsFallback(items, Number(userId))),
            catchError((fallbackError: unknown) => {
              console.error('Fallback transaction API failed:', fallbackError);
              this.errorMessage = null;
              this.snackBar.open(
                extractApiErrorMessage(fallbackError, 'Could not load fallback transaction data.'),
                'Close',
                { duration: 3500, panelClass: ['error-snackbar'] }
              );
              return of(this.getStaticFallbackTransactions());
            })
          );
        }),
        finalize(() => {
          window.clearTimeout(loadingWatchdog);
          this.isLoading = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe((items) => {
        this.transactions = Array.isArray(items) ? items : [];
        this.lastUpdatedLabel = new Date().toLocaleString('en-IN', {
          dateStyle: 'medium',
          timeStyle: 'short'
        });
        this.cdr.detectChanges();
      });
  }

  private getStaticFallbackTransactions(): ApiRecentTransactionsDto[] {
    return [
      { transactionId: 90001, fromAccount: 2233445566, toAccount: 9988776655, amount: 2500, transactionStatus: 'SUCCESS' },
      { transactionId: 90002, fromAccount: 9988776655, toAccount: 2233445566, amount: 700, transactionStatus: 'FAILED' },
      { transactionId: 90003, fromAccount: 5566778899, toAccount: 2233445566, amount: 4000, transactionStatus: 'SUCCESS' }
    ];
  }

  private mapTransactionsFallback(items: Transaction[], activeAccountId: number): ApiRecentTransactionsDto[] {
    return items.slice(0, 10).map((item) => {
      const otherAccount = Number(item.otherAccountNumber || 0);
      const isDebit = item.type === TransactionType.DEBIT;

      return {
        transactionId: Number(item.transactionId),
        fromAccount: isDebit ? activeAccountId : otherAccount,
        toAccount: isDebit ? otherAccount : activeAccountId,
        amount: Number(item.amount || 0),
        transactionStatus: String(item.transactionStatus || 'UNKNOWN')
      };
    });
  }

  onSearchTermChange(value: string): void {
    this.searchTerm = value;
  }

  setStatusFilter(filter: StatusFilter): void {
    this.statusFilter = filter;
  }

  isStatusFilterActive(filter: StatusFilter): boolean {
    return this.statusFilter === filter;
  }

  get filteredTransactions(): ApiRecentTransactionsDto[] {
    const query = this.searchTerm.trim().toLowerCase();

    return this.transactions.filter((item) => {
      const normalizedStatus = String(item.transactionStatus || '').toUpperCase();
      const isSuccess = normalizedStatus === 'SUCCESS';

      if (this.statusFilter === 'SUCCESS' && !isSuccess) {
        return false;
      }
      if (this.statusFilter === 'FAILED' && isSuccess) {
        return false;
      }

      if (!query) {
        return true;
      }

      return [
        item.transactionId,
        item.fromAccount,
        item.toAccount,
        item.amount,
        item.transactionStatus
      ]
        .map((value) => String(value ?? '').toLowerCase())
        .some((value) => value.includes(query));
    });
  }

  get totalAmount(): number {
    return this.transactions.reduce((sum, item) => sum + Number(item.amount || 0), 0);
  }

  get successfulCount(): number {
    return this.transactions.filter((item) => String(item.transactionStatus).toUpperCase() === 'SUCCESS').length;
  }

  get failedCount(): number {
    return this.transactions.filter((item) => String(item.transactionStatus).toUpperCase() !== 'SUCCESS').length;
  }

  get visibleTotalAmount(): number {
    return this.filteredTransactions.reduce((sum, item) => sum + Number(item.amount || 0), 0);
  }

  get successRate(): number {
    if (!this.transactions.length) return 0;
    return Math.round((this.successfulCount / this.transactions.length) * 100);
  }

  trackByTransactionId(_: number, item: ApiRecentTransactionsDto): number {
    return item.transactionId;
  }
}
