import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { LoginComponent } from './features/login/login.component';
import { SignupComponent } from './features/signup/signup.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { TransferComponent } from './features/transfer/transfer.component';
import { HistoryComponent } from './features/history/history.component';
import { ProfileComponent } from './features/profile/profile.component';
import { ForgotPasswordComponent } from './features/forgot-password/forgot-password.component';
import { LayoutComponent } from './shared/layout/layout.component';
import { AdminComponent } from './features/admin/admin.component';
import { AuthGuard } from './core/guards/auth.guard';
import { AdminGuard } from './core/guards/admin.guard';
import { RewardsComponent } from './features/rewards/rewards.component';

const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'admin-log', component: LoginComponent },
    { path: 'admin-login', component: LoginComponent },
    { path: 'admin', component: AdminComponent, canActivate: [AuthGuard, AdminGuard] },
    { path: 'signup', component: SignupComponent },
    { path: 'forgot-password', component: ForgotPasswordComponent },
    {
        path: '', component: LayoutComponent, children: [
            { path: 'dashboard', component: DashboardComponent },
            { path: 'transfer', component: TransferComponent },
            { path: 'history', component: HistoryComponent },
            { path: 'profile', component: ProfileComponent },
            { path: 'rewards', component: RewardsComponent, canActivate: [AuthGuard] },
            { path: 'analytics', loadComponent: () => import('./features/analytics/analytics.component').then(m => m.AnalyticsComponent), canActivate: [AuthGuard] },
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
        ]
    },
    { path: '**', redirectTo: '' }
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: 'reload' })],
    exports: [RouterModule]
})
export class AppRoutingModule { }
