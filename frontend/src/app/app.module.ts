import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SharedModule } from './shared/shared.module';

// Feature Components
import { LoginComponent } from './features/login/login.component';
import { SignupComponent } from './features/signup/signup.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { TransferComponent } from './features/transfer/transfer.component';
import { HistoryComponent } from './features/history/history.component';

// Interceptors
import { AuthInterceptor } from './core/interceptors/auth.interceptor';

@NgModule({
    declarations: [
        // AppComponent is now standalone and bootstrapped via bootstrapApplication
        LoginComponent,
        SignupComponent,
    DashboardComponent,
    TransferComponent,
        HistoryComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        HttpClientModule,
        AppRoutingModule,
        SharedModule
    ],
    providers: [
        { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
    ],
    // bootstrap is handled by bootstrapApplication(AppComponent, { providers: [ importProvidersFrom(AppModule) ] })
})
export class AppModule { }
