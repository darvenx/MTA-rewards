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
			`:host { display: block; background: #fafbfd; height: 100%; position: relative; }
			 .app-sidenav { padding: 20px 14px; border-right: 1px solid rgba(16,24,40,0.04); height: 100%; box-sizing: border-box; background: #fbfcfd; }
			 nav a { position:relative; display:flex; gap:12px; align-items:center; padding:10px 12px; color: rgba(0,0,0,0.82); text-decoration:none; border-radius:8px; margin-bottom:6px; transition: background 160ms ease-out, color 160ms ease-out, transform 140ms ease-out; outline:none; }
			 nav a::before { content:''; position:absolute; left:4px; top:6px; bottom:6px; width:0; border-radius:999px; background: linear-gradient(to bottom, #1d4ed8, #2563eb); transition: width 180ms ease-out, opacity 180ms ease-out; opacity:0; }
			 nav a.active { background: rgba(25,118,210,0.08); color:#0f172a; transform: translateX(1px); }
			 nav a.active::before { width:3px; opacity:1; }
			 nav a.active mat-icon { color:#1d4ed8; }
			 nav a:hover:not(.active) { background: rgba(15,23,42,0.03); transform: translateX(1px); }
			 nav a:focus-visible { box-shadow: 0 0 0 2px rgba(37,99,235,0.4); background: rgba(25,118,210,0.06); }
			 nav mat-icon { font-size:20px; color: rgba(55,65,81,0.95); }
			 .section { font-size:12px; color:rgba(0,0,0,0.55); margin-top:8px; margin-bottom:6px; letter-spacing:0.6px; }
			 .version { position: absolute; bottom: 12px; left: 16px; font-size:12px; color:rgba(0,0,0,0.45); }
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
