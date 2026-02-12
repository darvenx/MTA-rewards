import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { Transaction, TransactionType } from '../../core/models/transaction.model';
import { MatTabChangeEvent } from '@angular/material/tabs';

@Component({
    selector: 'app-history',
    standalone: false,
    templateUrl: './history.component.html',
    styleUrls: ['./history.component.css']
})
export class HistoryComponent implements OnInit {
    allTransactions: Transaction[] = [];
    filteredTransactions: Transaction[] = [];
    isLoading = true;

    constructor(
        private accountService: AccountService,
        private authService: AuthService,
        private router: Router,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        const accountId = localStorage.getItem("id");
        console.log("on init");
        console.log(accountId);
        if (accountId) {
            this.accountService.getTransactions(accountId).subscribe({
                next: (data) => {
                    console.log(data);
                    // Sort by date newest first
                    this.allTransactions = data.sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
                    this.filteredTransactions = this.allTransactions;
                    this.isLoading = false;
                    this.cdr.markForCheck();
                },
                error: () => {
                    this.isLoading = false;
                    this.cdr.markForCheck();
                }
            });
        }
        this.isLoading = false;
        this.cdr.markForCheck();
    }

    onTabChange(event: MatTabChangeEvent): void {
        console.log(event.index);
        switch (event.index) {
            case 0: // All
                this.filteredTransactions = this.allTransactions;
                break;
            case 1: // Sent / DEBIT
                this.filteredTransactions = this.allTransactions.filter(t => t.type === TransactionType.DEBIT);
                break;
            case 2: // Received / CREDIT
                this.filteredTransactions = this.allTransactions.filter(t => t.type === TransactionType.CREDIT);
                break;
        }
        this.cdr.markForCheck();
    }

    goBack(): void {
        this.router.navigate(['/dashboard']);
    }
}
