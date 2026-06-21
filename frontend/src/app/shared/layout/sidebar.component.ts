import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
	selector: 'app-sidebar',
	standalone: true,
	imports: [CommonModule, RouterModule, MatIconModule],
	template: `
		<aside class="app-sidenav">
			<nav>
				<h4 class="section">MAIN</h4>
					<a routerLink="/dashboard" routerLinkActive="active"> 
						<mat-icon>dashboard</mat-icon>
						<span>Dashboard</span>
					</a>
				<a routerLink="/transfer" routerLinkActive="active">
					<mat-icon>send</mat-icon>
					<span>Transfer</span>
				</a>
				<a routerLink="/history" routerLinkActive="active">
					<mat-icon>history</mat-icon>
					<span>History</span>
				</a>
				<a routerLink="/rewards" routerLinkActive="active">
					<mat-icon>stars</mat-icon>
					<span>Rewards</span>
				</a>
				<a routerLink="/analytics" routerLinkActive="active">
					<mat-icon>insights</mat-icon>
					<span>Analytics</span>
				</a>

				<h4 class="section">ACCOUNT</h4>
				<a routerLink="/profile" routerLinkActive="active">
					<mat-icon>person</mat-icon>
					<span>Profile</span>
				</a>
				<a routerLink="/login" (click)="logout()">
					<mat-icon>logout</mat-icon>
					<span>Logout</span>
				</a>
			</nav>
			<div class="version">Version 1.0.0</div>
		</aside>
	`,
	styles: [
		`:host { display: block; background: var(--sidebar-bg); height: 100%; position: relative; transition: background 0.3s ease; }
			 .app-sidenav { padding: 20px 14px; border-right: 1px solid var(--sidebar-border); height: 100%; box-sizing: border-box; background: var(--sidebar-bg); }
			 nav a { position:relative; display:flex; gap:12px; align-items:center; padding:10px 12px; color: var(--sidebar-text); text-decoration:none; border-radius:8px; margin-bottom:6px; transition: background 160ms ease-out, color 160ms ease-out, transform 140ms ease-out; outline:none; }
			 nav a::before { content:''; position:absolute; left:4px; top:6px; bottom:6px; width:0; border-radius:999px; background: linear-gradient(to bottom, #1d4ed8, #2563eb); transition: width 180ms ease-out, opacity 180ms ease-out; opacity:0; }
			 nav a.active { background: var(--sidebar-active-bg); color: var(--text); transform: translateX(1px); }
			 nav a.active::before { width:3px; opacity:1; }
			 nav a.active mat-icon { color: var(--primary); }
			 nav a:hover:not(.active) { background: var(--sidebar-hover); transform: translateX(1px); }
			 nav a:focus-visible { box-shadow: 0 0 0 2px rgba(37,99,235,0.4); background: rgba(25,118,210,0.06); }
			 nav mat-icon { font-size:20px; color: var(--sidebar-text); }
			 .section { font-size:12px; color: var(--sidebar-section); margin-top:8px; margin-bottom:6px; letter-spacing:0.6px; }
			 .version { position: absolute; bottom: 12px; left: 16px; font-size:12px; color: var(--sidebar-section); }
			`
	]
})
export class SidebarComponent {
	logout() {
		// simple client-side logout
		localStorage.removeItem('token');
		localStorage.removeItem('accountId');
		localStorage.removeItem('holderName');
	}
}
