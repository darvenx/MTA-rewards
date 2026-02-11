import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-balance-card',
  standalone: false,
  template: `
    <mat-card class="balance-card">
      <mat-card-header>
        <mat-card-title>Available Balance</mat-card-title>
      </mat-card-header>
      <mat-card-content>
        <div class="balance-amount">
          {{ balance | currency:'USD':'symbol':'1.2-2' }}
        </div>
        <div class="account-name">{{ accountName }}</div>
      </mat-card-content>
    </mat-card>
  `,
  styles: [`
    .balance-card {
      background: linear-gradient(135deg, #1a237e 0%, #283593 100%);
      color: white;
      margin-bottom: 20px;
    }
    .mat-mdc-card-title {
      color: rgba(255, 255, 255, 0.9);
      font-size: 1rem;
    }
    .balance-amount {
      font-size: 2.5rem;
      font-weight: 500;
      margin: 15px 0;
    }
    .account-name {
      color: rgba(255, 255, 255, 0.7);
    }
  `]
})
export class BalanceCardComponent {
  @Input() balance: number = 0;
  @Input() accountName: string = '';
}
