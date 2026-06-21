import { Component, OnInit, OnDestroy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { AnalyticsService } from '../../services/analytics.service';
import { AccountService } from '../../services/account.service';
import { AuthService } from '../../services/auth.service';
import { Chart, registerables } from 'chart.js';
import { Subject, takeUntil } from 'rxjs';

Chart.register(...registerables);

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatCardModule,
    MatButtonModule
  ],
  template: `
    <div class="analytics-container">
      <div class="header-section">
        <div>
          <h1 class="title">Spend Analytics</h1>
          <p class="subtitle">Understand your spending patterns and manage your finances better.</p>
        </div>
        <div class="account-selector" *ngIf="accounts.length > 1">
          <select [value]="selectedAccountId" (change)="onAccountChange($event)">
            <option *ngFor="let acc of accounts" [value]="acc.accountId">
              {{ acc.accountType }} · {{ acc.accountId }}
            </option>
          </select>
        </div>
      </div>

      <div class="stats-grid">
        <div class="stat-card glass box-shadow">
          <div class="stat-icon bg-blue">
            <mat-icon>account_balance_wallet</mat-icon>
          </div>
          <div class="stat-content">
            <div class="label">Total Spend (Life)</div>
            <div class="value">₹{{ totalSpend | number:'1.2-2' }}</div>
          </div>
        </div>
        <div class="stat-card glass box-shadow">
          <div class="stat-icon bg-green">
            <mat-icon>category</mat-icon>
          </div>
          <div class="stat-content">
            <div class="label">Top Category</div>
            <div class="value">{{ topCategory || 'N/A' }}</div>
          </div>
        </div>
        <div class="stat-card glass box-shadow">
          <div class="stat-icon bg-purple">
            <mat-icon>trending_up</mat-icon>
          </div>
          <div class="stat-content">
            <div class="label">Total Transactions</div>
            <div class="value">{{ transactionCount }}</div>
          </div>
        </div>
      </div>

      <div class="charts-layout">
        <div class="chart-main glass box-shadow">
          <div class="chart-header">
            <h3>Category Breakdown</h3>
            <p>Where your money goes</p>
          </div>
          <div class="chart-container">
            <canvas id="spendingDoughnutChart"></canvas>
            <div class="no-data" *ngIf="!isLoading && (!analyticsData || hasNoData)">
               <mat-icon>analytics</mat-icon>
               <p>No spending data found for this account.</p>
               <button mat-flat-button color="primary" routerLink="/transfer">Make a transfer</button>
            </div>
            <div class="loader" *ngIf="isLoading">
               <mat-spinner diameter="40"></mat-spinner>
            </div>
          </div>
        </div>

        <div class="details-section glass box-shadow">
          <div class="chart-header">
            <h3>Detailed Breakdown</h3>
          </div>
          <div class="legend-list">
            <div class="legend-item" *ngFor="let cat of sortedCategories; let i = index">
              <div class="legend-info">
                <span class="color-dot" [style.background-color]="chartColors[i % chartColors.length]"></span>
                <span class="category-name">{{ cat | titlecase }}</span>
              </div>
              <div class="legend-values">
                <span class="category-amount">₹{{ analyticsData[cat] | number:'1.0-0' }}</span>
                <span class="category-percent">{{ getPercentage(analyticsData[cat]) }}%</span>
              </div>
            </div>
            <div class="empty-legend" *ngIf="!analyticsData || hasNoData">
              Explore your spending once you start transacting!
            </div>
          </div>
        </div>
      </div>
      
      <div class="bottom-actions">
          <button mat-button color="primary" routerLink="/dashboard">
              <mat-icon>arrow_back</mat-icon>
              Back to Dashboard
          </button>
      </div>
    </div>
  `,
  styles: [`
    .analytics-container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 24px;
      animation: fadeIn 0.6s ease-out;
    }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(10px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .header-section {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 32px;
    }

    .title {
      font-size: 32px;
      font-weight: 800;
      color: #0f172a;
      margin: 0 0 8px;
    }

    .subtitle {
      color: #64748b;
      font-size: 16px;
      margin: 0;
    }

    .account-selector select {
      background: #ffffff;
      border: 1px solid #e2e8f0;
      padding: 8px 16px;
      border-radius: 8px;
      font-weight: 600;
      color: #1e293b;
      cursor: pointer;
      outline: none;
      transition: border-color 0.2s;
    }

    .account-selector select:hover {
      border-color: #cbd5e1;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 24px;
      margin-bottom: 32px;
    }

    .glass {
      background: rgba(255, 255, 255, 0.7);
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.5);
      border-radius: 20px;
    }

    .box-shadow {
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.05), 0 8px 10px -6px rgba(0, 0, 0, 0.05);
    }

    .stat-card {
      padding: 24px;
      display: flex;
      align-items: center;
      gap: 20px;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
    }

    .bg-blue { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
    .bg-green { background: linear-gradient(135deg, #10b981, #059669); }
    .bg-purple { background: linear-gradient(135deg, #8b5cf6, #6d28d9); }

    .stat-content .label {
      font-size: 14px;
      color: #64748b;
      font-weight: 500;
      margin-bottom: 4px;
    }

    .stat-content .value {
      font-size: 24px;
      font-weight: 700;
      color: #1e293b;
    }

    .charts-layout {
      display: grid;
      grid-template-columns: 3fr 2fr;
      gap: 24px;
      margin-bottom: 32px;
    }

    @media (max-width: 900px) {
      .charts-layout { grid-template-columns: 1fr; }
    }

    .chart-main, .details-section {
      padding: 24px;
      min-height: 450px;
      display: flex;
      flex-direction: column;
    }

    .chart-header {
      margin-bottom: 24px;
    }

    .chart-header h3 {
      font-size: 20px;
      font-weight: 700;
      color: #1e293b;
      margin: 0 0 4px;
    }

    .chart-header p {
      font-size: 14px;
      color: #64748b;
      margin: 0;
    }

    .chart-container {
      flex: 1;
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    canvas {
      max-width: 100%;
      max-height: 400px;
    }

    .no-data {
      text-align: center;
      color: #64748b;
    }

    .no-data mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
      opacity: 0.3;
    }

    .no-data button {
      margin-top: 16px;
    }

    .loader {
      position: absolute;
    }

    .legend-list {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .legend-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: rgba(255, 255, 255, 0.4);
      border-radius: 12px;
      transition: transform 0.2s;
    }

    .legend-item:hover {
      transform: translateX(5px);
      background: rgba(255, 255, 255, 0.6);
    }

    .legend-info {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    .color-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;
    }

    .category-name {
      font-weight: 600;
      color: #1e293b;
    }

    .legend-values {
      text-align: right;
    }

    .category-amount {
      display: block;
      font-weight: 700;
      color: #1e293b;
    }

    .category-percent {
      font-size: 12px;
      color: #64748b;
    }

    .empty-legend {
        text-align: center;
        padding: 40px;
        color: #94a3b8;
        font-style: italic;
    }
    
    .bottom-actions {
        display: flex;
        justify-content: center;
    }
  `]
})
export class AnalyticsComponent implements OnInit, OnDestroy {
  accounts: any[] = [];
  selectedAccountId: string | null = null;
  analyticsData: { [key: string]: number } = {};
  isLoading = false;
  totalSpend = 0;
  topCategory = '';
  transactionCount = 0;
  hasNoData = true;

  chartColors = [
    '#2563eb', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899', '#64748b'
  ];

  // Stable sorted category keys (descending by spend amount)
  // Shared between chart & legend so colors always match labels.
  sortedCategories: string[] = [];

  spendingChart: any;
  private destroyed$ = new Subject<void>();

  constructor(
    private analyticsService: AnalyticsService,
    private accountService: AccountService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.loadUserAccounts();
  }

  loadUserAccounts(): void {
    const userId = localStorage.getItem('id');
    if (!userId) return;

    this.accountService.getUserAccounts(userId)
      .pipe(takeUntil(this.destroyed$))
      .subscribe({
        next: (res: any) => {
          // Robust accounting for different formats of user accounts data
          const ids = res.accountIds || res.accounts || res.accountNumbers || [];
          const types = res.accountType || res.accountTypes || [];

          this.accounts = ids.map((id: any, index: number) => ({
            accountId: id,
            accountType: types[index] || 'SAVINGS'
          }));

          const persistedAccountId = this.authService.getAccountId();
          if (persistedAccountId && ids.includes(Number(persistedAccountId))) {
            this.selectedAccountId = persistedAccountId;
          } else if (ids.length > 0) {
            this.selectedAccountId = ids[0].toString();
          }

          if (this.selectedAccountId) {
            this.loadAnalytics(this.selectedAccountId);
          }
          this.cdr.markForCheck();
        },
        error: (err) => console.error('Error loading accounts:', err)
      });
  }

  onAccountChange(event: any): void {
    this.selectedAccountId = event.target.value;
    if (this.selectedAccountId) {
      this.loadAnalytics(this.selectedAccountId);
    }
  }

  loadAnalytics(accountId: string): void {
    this.isLoading = true;
    this.hasNoData = true;
    this.analyticsService.getSpendingAnalysis(accountId)
      .pipe(takeUntil(this.destroyed$))
      .subscribe({
        next: (res: any) => {
          this.analyticsData = res.spendingByCategory || {};
          this.transactionCount = res.transactionCount || 0;
          this.processData();
          this.isLoading = false;
          this.updateChart();
          this.cdr.markForCheck();
        },
        error: (err) => {
          console.error('Error loading analytics:', err);
          this.analyticsData = {};
          this.processData();
          this.isLoading = false;
          this.updateChart();
          this.cdr.markForCheck();
        }
      });
  }

  processData(): void {
    const values = Object.values(this.analyticsData);
    this.totalSpend = values.reduce((a, b) => a + b, 0);
    this.hasNoData = this.totalSpend === 0;

    // Build stable sorted key list (descending by amount) used by both chart and legend
    this.sortedCategories = Object.keys(this.analyticsData)
      .sort((a, b) => this.analyticsData[b] - this.analyticsData[a]);

    if (this.hasNoData) {
      this.topCategory = '';
      this.transactionCount = 0;
    } else {
      const topCat = this.sortedCategories[0] ?? '';
      this.topCategory = topCat.charAt(0).toUpperCase() + topCat.slice(1).toLowerCase();
    }
  }

  getPercentage(value: number): number {
    if (this.totalSpend === 0) return 0;
    return Math.round((value / this.totalSpend) * 100);
  }

  updateChart(): void {
    const ctx = document.getElementById('spendingDoughnutChart') as HTMLCanvasElement;
    if (!ctx) return;

    if (this.spendingChart) {
      this.spendingChart.destroy();
    }

    if (this.hasNoData) return;

    // Use the same stable sorted key order as the legend
    const labels = this.sortedCategories.map(
      c => c.charAt(0).toUpperCase() + c.slice(1).toLowerCase()
    );
    const data = this.sortedCategories.map(c => this.analyticsData[c]);

    this.spendingChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: this.chartColors,
          borderWidth: 2,
          borderColor: '#ffffff',
          hoverOffset: 15
        }]
      },
      options: {
        responsive: true,
        plugins: {
          legend: {
            display: false
          },
          tooltip: {
            backgroundColor: 'rgba(15, 23, 42, 0.9)',
            padding: 12,
            titleFont: { size: 14, weight: 'bold', family: 'Inter' },
            bodyFont: { size: 14, family: 'Inter' },
            cornerRadius: 8,
            callbacks: {
              label: (item: any) => ` ₹${item.raw.toLocaleString('en-IN')}`
            }
          }
        },
        cutout: '75%'
      }
    });
  }

  ngOnDestroy(): void {
    this.destroyed$.next();
    this.destroyed$.complete();
    if (this.spendingChart) {
      this.spendingChart.destroy();
    }
  }
}
