import { Component, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { MatSort } from '@angular/material/sort';
import { Transaction, TransactionType } from '../../../core/models/transaction.model';

@Component({
  selector: 'app-transaction-table',
  standalone: false,
  template: `
    <table mat-table [dataSource]="dataSource" matSort class="full-width">
      
      <!-- Date Column -->
      <ng-container matColumnDef="date">
        <th mat-header-cell *matHeaderCellDef mat-sort-header> Date </th>
        <td mat-cell *matCellDef="let transaction"> {{transaction.date | date:'medium'}} </td>
      </ng-container>

      <!-- Type Column -->
      <ng-container matColumnDef="type">
        <th mat-header-cell *matHeaderCellDef mat-sort-header> Type </th>
        <td mat-cell *matCellDef="let transaction">
          <span [class.credit]="transaction.type === 'CREDIT'" [class.debit]="transaction.type === 'DEBIT'">
            {{transaction.type}}
          </span>
        </td>
      </ng-container>

      <!-- Party Column -->
      <ng-container matColumnDef="party">
        <th mat-header-cell *matHeaderCellDef mat-sort-header> Party </th>
        <td mat-cell *matCellDef="let transaction">
          <div *ngIf="transaction.otherPartyName; else fallback">
            <div class="party-name">{{ transaction.otherPartyName }}</div>
            <div class="party-account small">{{ transaction.otherAccountNumber }}</div>
          </div>
          <ng-template #fallback>
            {{ transaction.description }}
          </ng-template>
        </td>
      </ng-container>

      <!-- Amount Column -->
      <ng-container matColumnDef="amount">
        <th mat-header-cell *matHeaderCellDef mat-sort-header> Amount </th>
        <td mat-cell *matCellDef="let transaction" [class.credit]="transaction.type === 'CREDIT'" [class.debit]="transaction.type === 'DEBIT'">
          {{ (transaction.type === 'DEBIT' ? '-' : '+') }} {{ transaction.amount | currency:'INR':'symbol':'1.2-2' }}
        </td>
      </ng-container>

      <!-- Status Column -->
      <ng-container matColumnDef="status">
        <th mat-header-cell *matHeaderCellDef mat-sort-header> Status </th>
        <td mat-cell *matCellDef="let transaction">
          <span class="status-badge" [class.success]="transaction.transactionStatus === 'SUCCESS'" [class.failed]="transaction.transactionStatus === 'FAILED'">
            {{transaction.transactionStatus}}
          </span>
        </td>
      </ng-container>

      <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
      <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
    </table>
  `,
  styles: [`
    .full-width { width: 100%; }
    .credit { color: #2e7d32; font-weight: 500; }
    .debit { color: #c62828; font-weight: 500; }
    .status-badge {
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 0.8rem;
    }
    .status-badge.success { background-color: #e8f5e9; color: #2e7d32; }
    .status-badge.failed { background-color: #ffebee; color: #c62828; }
  `]
})
export class TransactionTableComponent implements OnChanges {
  @Input() transactions: Transaction[] = [];

  displayedColumns: string[] = ['date', 'type', 'party', 'amount', 'status'];
  dataSource = new MatTableDataSource<Transaction>([]);

  @ViewChild(MatSort) sort!: MatSort;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['transactions']) {
      this.dataSource.data = this.transactions || [];
      if (this.sort) {
        this.dataSource.sort = this.sort;
      }
    }
  }

  ngAfterViewInit() {
    this.dataSource.sort = this.sort;
  }
}
