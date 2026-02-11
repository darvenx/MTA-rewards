import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { CommonModule } from '@angular/common';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDividerModule } from '@angular/material/divider';

@Component({
	selector: 'app-header',
	standalone: true,
		imports: [CommonModule, MatToolbarModule, MatMenuModule, MatIconModule, MatButtonModule, MatBadgeModule, MatTooltipModule, MatDividerModule],
	template: `
		<mat-toolbar color="primary" class="app-header">
			<div class="left">
				<span class="logo">BankApp</span>
			</div>
			<span class="spacer"></span>
				<div class="right">
					<button
						mat-icon-button
						[matBadge]="unreadCount"
						matBadgeColor="warn"
						aria-label="Notifications"
						(click)="openNotifications($event)"
						matTooltip="{{ unreadCount }} unread notifications"
						class="header-notification"
						[class.has-unread]="unreadCount > 0"
					>
						<mat-icon>notifications</mat-icon>
					</button>
					<mat-divider vertical></mat-divider>
					<div class="date">{{ today | date:'EEE, d MMM y' }}</div>
					<span class="username">{{ username }}</span>
					<button mat-icon-button [matMenuTriggerFor]="menu" aria-label="User menu">
						<mat-icon>account_circle</mat-icon>
					</button>
					<mat-menu #menu="matMenu">
						<button mat-menu-item (click)="goToProfile()">Profile</button>
						<button mat-menu-item (click)="logout()">Logout</button>
					</mat-menu>
				</div>
		</mat-toolbar>
	`,
	styles: [
		`:host { display: block; }
		 .app-header {
			 position: sticky;
			 top: 0;
			 z-index: 20;
			 /* Reuse balance-card gradient for visual consistency */
			 background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 40%, #22c1c3 100%);
			 box-shadow: 0 14px 32px rgba(15,23,42,0.35);
			 color: #ffffff;
			 transition: background 220ms ease-out, box-shadow 220ms ease-out;
		 }
		 .logo { font-weight: 700; font-size: 18px; }
		 .spacer { flex: 1 1 auto; }
		 .right { display: flex; align-items: center; gap: 8px; }
		 .username { margin-right: 4px; }
		 .date { color: rgba(255,255,255,0.9); margin-right: 12px; font-size: 0.95rem; }
		 .header-notification mat-icon { color: #ffffff; }
		`
	]
})
export class HeaderComponent {
	username = localStorage.getItem('holderName') || 'User';
	today = new Date();
	unreadCount = 3;

	constructor(private auth: AuthService, private router: Router) { }

	logout() {
		this.auth.logout();
	}

	goToProfile() {
		this.router.navigate(['/profile']);
	}
  
		openNotifications(e: Event) {
			// For now just open a small menu or navigate to a notifications page
			// Could be replaced with a real menu; keep it simple and show an alert
			// Using window.alert is crude; better to use MatSnackBar but header doesn't import it.
			// We'll use a simple native alert for now to avoid more imports.
			alert('No new notifications (mock)');
		}
}
