import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { RewardService } from '../../services/reward.service';
import { ApiRewardLogDto, ApiRewardSummaryDto } from '../../core/api/backend-contracts';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize } from 'rxjs/operators';

@Component({
    selector: 'app-rewards',
    standalone: true,
    imports: [CommonModule, RouterModule],
    template: `
        <div class="rewards-page">
            <!-- Header -->
            <div class="page-header">
                <h1 class="page-title">
                    <span class="star-icon">⭐</span>
                    Reward Points
                </h1>
                <p class="page-subtitle">Earn points on every eligible transfer. 1 point per ₹100 transferred.</p>
            </div>

            <!-- Hero total-points card -->
            <div class="hero-card" [class.loaded]="!loading">
                <div class="hero-inner">
                    <div class="hero-label">Your Total Points</div>
                    <div class="hero-points" *ngIf="!loading; else heroSkeleton">
                        {{ summary?.totalPoints ?? 0 }}
                        <span class="pts-label">pts</span>
                    </div>
                    <ng-template #heroSkeleton>
                        <div class="skeleton hero-skeleton"></div>
                    </ng-template>
                    <div class="hero-hint">₹{{ (summary?.totalPoints ?? 0) * 100 | number }} equivalent transferred</div>

                    <!-- Redeem Action -->
                    <div class="hero-action" *ngIf="!loading && (summary?.totalPoints ?? 0) >= 100">
                        <button class="redeem-btn" [disabled]="redeeming" (click)="redeem()">
                            {{ redeeming ? 'Redeeming...' : 'Redeem to Account' }}
                        </button>
                    </div>
                    <div class="hero-low-points" *ngIf="!loading && (summary?.totalPoints ?? 0) > 0 && (summary?.totalPoints ?? 0) < 100">
                        <span class="lock-icon">🔒</span>
                        Redeem at 100 pts
                    </div>
                </div>
                <div class="hero-glow"></div>
                <div class="hero-badge">
                    <span>🏆</span>
                </div>
            </div>

            <!-- Info chips -->
            <div class="info-chips">
                <div class="chip">
                    <span class="chip-icon">✅</span>
                    <span>Transfers &gt; ₹100 only</span>
                </div>
                <div class="chip">
                    <span class="chip-icon">🔢</span>
                    <span>1 pt per ₹100 (rounded down)</span>
                </div>
                <div class="chip">
                    <span class="chip-icon">🚦</span>
                    <span>SUCCESS status required</span>
                </div>
            </div>

            <!-- Reward History -->
            <div class="history-section">
                <h2 class="section-title">Reward History</h2>

                <!-- Loading skeletons -->
                <div *ngIf="loading" class="skeleton-list">
                    <div *ngFor="let i of [1,2,3,4]" class="skeleton-row">
                        <div class="skeleton sk-date"></div>
                        <div class="skeleton sk-txn"></div>
                        <div class="skeleton sk-pts"></div>
                    </div>
                </div>

                <!-- Empty state -->
                <div *ngIf="!loading && history.length === 0" class="empty-state">
                    <div class="empty-icon">🎁</div>
                    <p class="empty-title">No rewards yet</p>
                    <p class="empty-sub">Make a transfer of more than ₹100 to start earning points!</p>
                </div>

                <!-- History table -->
                <div *ngIf="!loading && history.length > 0" class="table-wrapper">
                    <table class="history-table">
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Date &amp; Time</th>
                                <th>Transaction ID</th>
                                <th>Points Earned</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr *ngFor="let row of history; let i = index" class="history-row">
                                <td class="row-num">{{ i + 1 }}</td>
                                <td class="row-date">{{ formatDate(row.createdOn) }}</td>
                                <td class="row-txn">#{{ row.transactionId }}</td>
                                <td class="row-pts">
                                    <span class="pts-badge">+{{ row.pointsEarned }} pt{{ row.pointsEarned !== 1 ? 's' : '' }}</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <!-- Success state -->
                <div *ngIf="successMessage" class="success-banner">
                    <span>🎉 {{ successMessage }}</span>
                    <button class="close-btn" (click)="successMessage = null">×</button>
                </div>

                <!-- Error state -->
                <div *ngIf="error" class="error-banner">
                    <span>⚠️  {{ error }}</span>
                    <button class="retry-btn" (click)="load()">Retry</button>
                </div>
            </div>
        </div>
    `,
    styles: [`
        :host { display: block; }

        .rewards-page {
            max-width: 820px;
            margin: 0 auto;
            padding: 32px 24px 48px;
            font-family: 'Inter', system-ui, sans-serif;
        }

        /* ---- Header ---- */
        .page-header { margin-bottom: 28px; }
        .page-title {
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 26px;
            font-weight: 700;
            color: #0f172a;
            margin: 0 0 6px;
        }
        .star-icon { font-size: 28px; line-height: 1; }
        .page-subtitle { color: #64748b; font-size: 14px; margin: 0; }

        /* ---- Hero card ---- */
        .hero-card {
            position: relative;
            background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 55%, #3b82f6 100%);
            border-radius: 20px;
            padding: 40px 36px;
            color: #fff;
            overflow: hidden;
            margin-bottom: 24px;
            box-shadow: 0 12px 40px rgba(37, 99, 235, 0.30);
            opacity: 0;
            transform: translateY(12px);
            transition: opacity 500ms ease, transform 500ms ease;
        }
        .hero-card.loaded { opacity: 1; transform: translateY(0); }
        .hero-inner { position: relative; z-index: 2; }
        .hero-label { font-size: 13px; font-weight: 600; letter-spacing: 1.2px; text-transform: uppercase; opacity: 0.75; margin-bottom: 10px; }
        .hero-points {
            font-size: 72px;
            font-weight: 800;
            line-height: 1;
            letter-spacing: -2px;
        }
        .pts-label { font-size: 28px; font-weight: 600; opacity: 0.8; margin-left: 6px; }
        .hero-hint { margin-top: 12px; font-size: 13px; opacity: 0.65; }
        .hero-glow {
            position: absolute;
            inset: -40px;
            background: radial-gradient(circle at 70% 30%, rgba(255,255,255,0.12), transparent 60%);
            pointer-events: none;
            z-index: 1;
        }
        .hero-badge {
            position: absolute;
            right: 36px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 72px;
            opacity: 0.18;
            z-index: 1;
            user-select: none;
        }

        /* Redeem action */
        .hero-action { margin-top: 24px; }
        .redeem-btn {
            background: #fff;
            color: #2563eb;
            border: none;
            padding: 10px 24px;
            border-radius: 10px;
            font-weight: 700;
            font-size: 14px;
            cursor: pointer;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            transition: transform 200ms, box-shadow 200ms;
        }
        .redeem-btn:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 6px 18px rgba(0,0,0,0.15);
        }
        .redeem-btn:disabled { opacity: 0.7; cursor: not-allowed; }

        .hero-low-points {
            margin-top: 18px;
            font-size: 11px;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.8px;
            opacity: 0.6;
            display: flex;
            align-items: center;
            gap: 4px;
        }
        .lock-icon { font-size: 12px; }
        .hero-skeleton {
            width: 180px;
            height: 72px;
            border-radius: 10px;
            background: rgba(255,255,255,0.2);
            margin: 4px 0;
            animation: shimmer 1.4s infinite;
        }

        /* ---- Info chips ---- */
        .info-chips { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 32px; }
        .chip {
            display: flex;
            align-items: center;
            gap: 6px;
            padding: 6px 14px;
            border-radius: 999px;
            background: #f1f5f9;
            font-size: 12.5px;
            color: #475569;
            font-weight: 500;
        }
        .chip-icon { font-size: 14px; }

        /* ---- History ---- */
        .section-title { font-size: 17px; font-weight: 700; color: #0f172a; margin: 0 0 16px; }
        .table-wrapper {
            border: 1px solid #e2e8f0;
            border-radius: 14px;
            overflow: hidden;
        }
        .history-table { width: 100%; border-collapse: collapse; }
        .history-table thead tr { background: #f8fafc; }
        .history-table th {
            padding: 12px 16px;
            font-size: 12px;
            font-weight: 600;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.6px;
            text-align: left;
            border-bottom: 1px solid #e2e8f0;
        }
        .history-row {
            border-bottom: 1px solid #f1f5f9;
            transition: background 150ms;
        }
        .history-row:last-child { border-bottom: none; }
        .history-row:hover { background: #f8fafc; }
        .history-table td { padding: 14px 16px; font-size: 14px; color: #1e293b; }
        .row-num { color: #94a3b8; font-size: 12px; width: 36px; }
        .row-date { color: #475569; }
        .row-txn { font-family: monospace; color: #334155; }
        .pts-badge {
            display: inline-block;
            padding: 3px 10px;
            background: linear-gradient(135deg, #d1fae5, #a7f3d0);
            color: #065f46;
            border-radius: 999px;
            font-weight: 700;
            font-size: 13px;
        }

        /* ---- Skeleton ---- */
        .skeleton {
            background: linear-gradient(90deg, #e2e8f0 25%, #f1f5f9 50%, #e2e8f0 75%);
            background-size: 200% 100%;
            animation: shimmer 1.4s infinite;
            border-radius: 6px;
        }
        @keyframes shimmer {
            0% { background-position: 200% 0; }
            100% { background-position: -200% 0; }
        }
        .skeleton-list { display: flex; flex-direction: column; gap: 12px; }
        .skeleton-row { display: flex; gap: 16px; align-items: center; padding: 4px 0; }
        .sk-date { flex: 2; height: 18px; }
        .sk-txn { flex: 1; height: 18px; }
        .sk-pts { width: 70px; height: 26px; border-radius: 999px; }

        /* ---- Empty ---- */
        .empty-state { text-align: center; padding: 56px 24px; }
        .empty-icon { font-size: 52px; margin-bottom: 12px; }
        .empty-title { font-size: 17px; font-weight: 700; color: #1e293b; margin: 0 0 6px; }
        .empty-sub { color: #64748b; font-size: 14px; margin: 0; }

        /* ---- Success ---- */
        .success-banner {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding: 14px 18px;
            background: #f0fdf4;
            border: 1px solid #bbf7d0;
            border-radius: 10px;
            color: #166534;
            font-size: 14px;
            margin-bottom: 20px;
            animation: slideIn 300ms ease;
        }
        @keyframes slideIn {
            from { opacity: 0; transform: translateY(-10px); }
            to { opacity: 1; transform: translateY(0); }
        }
        .close-btn {
            background: none;
            border: none;
            font-size: 20px;
            color: #166534;
            cursor: pointer;
            opacity: 0.6;
        }
        .close-btn:hover { opacity: 1; }

        /* ---- Error ---- */
        .error-banner {
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 12px;
            padding: 14px 18px;
            background: #fff7ed;
            border: 1px solid #fed7aa;
            border-radius: 10px;
            color: #9a3412;
            font-size: 14px;
        }
        .retry-btn {
            padding: 6px 14px;
            background: #ea580c;
            color: #fff;
            border: none;
            border-radius: 6px;
            cursor: pointer;
            font-size: 13px;
            font-weight: 600;
        }
    `]
})
export class RewardsComponent implements OnInit {

    summary: ApiRewardSummaryDto | null = null;
    history: ApiRewardLogDto[] = [];
    loading = true;
    redeeming = false;
    error: string | null = null;
    successMessage: string | null = null;

    constructor(
        private rewardService: RewardService,
        private cdr: ChangeDetectorRef
    ) { }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        this.loading = true;
        this.error = null;
        this.cdr.markForCheck();

        const accountId = localStorage.getItem('accountId') || localStorage.getItem('id');

        if (!accountId) {
            this.error = 'Account information not found. Please log in again.';
            this.loading = false;
            this.cdr.markForCheck();
            return;
        }

        console.log('RewardsComponent: Loading rewards for account', accountId);

        forkJoin({
            summary: this.rewardService.getTotalPoints(accountId).pipe(
                catchError(err => {
                    console.error('Error loading reward summary:', err);
                    return of({ accountId: Number(accountId), totalPoints: 0 } as ApiRewardSummaryDto);
                })
            ),
            history: this.rewardService.getRewardHistory(accountId).pipe(
                catchError(err => {
                    console.error('Error loading reward history:', err);
                    return of([] as ApiRewardLogDto[]);
                })
            )
        }).pipe(
            finalize(() => {
                this.loading = false;
                this.cdr.markForCheck();
            })
        ).subscribe({
            next: (result) => {
                this.summary = result.summary;
                this.history = result.history;
                console.log('RewardsComponent: Loaded data', result);
            },
            error: (err) => {
                this.error = 'An unexpected error occurred while loading rewards.';
                console.error('RewardsComponent: Unexpected error', err);
            }
        });
    }

    redeem(): void {
        const accountId = localStorage.getItem('accountId') || localStorage.getItem('id');
        if (!accountId) return;

        this.redeeming = true;
        this.error = null;
        this.successMessage = null;
        this.cdr.markForCheck();

        this.rewardService.redeemPoints(accountId)
            .pipe(
                finalize(() => {
                    this.redeeming = false;
                    this.cdr.markForCheck();
                })
            )
            .subscribe({
                next: (res) => {
                    this.summary = res;
                    this.successMessage = 'Successfully redeemed points to your account balance!';
                    // Refresh history to show logs as redeemed
                    this.load();
                },
                error: (err) => {
                    this.error = 'Failed to redeem points. Please try again.';
                    console.error('RewardsComponent: Redemption error', err);
                }
            });
    }

    formatDate(isoString: string): string {
        if (!isoString) return '—';
        return new Date(isoString).toLocaleString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }
}
