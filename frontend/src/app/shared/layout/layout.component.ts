import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HeaderComponent } from './header.component';
import { SidebarComponent } from './sidebar.component';

@Component({
	selector: 'app-layout',
	standalone: true,
	imports: [CommonModule, RouterModule, HeaderComponent, SidebarComponent],
		template: `
			<div class="layout-root">
				<app-header></app-header>
				<div class="layout-main">
					<app-sidebar></app-sidebar>
					<main class="layout-content">
						<router-outlet></router-outlet>
					</main>
				</div>
			</div>
		`,
		styles: [
			`:host { display: block; min-height: 100vh; background: #f5f7fa; }
			 .layout-main { display: flex; min-height: calc(100vh - 64px); }
			 app-sidebar { flex: 0 0 240px; }
			 .layout-content { flex: 1; overflow: auto; padding: 32px; box-sizing: border-box; }
			 @media (max-width: 1000px) { app-sidebar { display: none; } .layout-content { padding: 16px; } }
			`
		]
})
export class LayoutComponent { }
