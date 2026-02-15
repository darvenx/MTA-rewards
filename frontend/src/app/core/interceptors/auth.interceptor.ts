import { Injectable } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpErrorResponse
} from '@angular/common/http';
import { Observable, catchError, throwError } from 'rxjs';
import { TokenStorageService } from '../services/token-storage.service';
import { Router } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(
        private tokenStorage: TokenStorageService,
        private router: Router,
        private snackBar: MatSnackBar
    ) { }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const token = this.tokenStorage.getToken();

        if (token) {
            request = request.clone({
                setHeaders: {
                    Authorization: `Bearer ${token}`
                }
            });
        }

        return next.handle(request).pipe(
            catchError((error: HttpErrorResponse) => {
                const isAccountsListRequest = request.url.includes('/api/v1/user/accounts/');
                const isRecentTransactionsRequest = request.url.includes('/api/v1/recent-transactions');
                const isAuthRequest =
                    request.url.includes('/api/v1/user/login') ||
                    request.url.includes('/api/v1/user/signup') ||
                    request.url.includes('/api/v1/user/forgot-password');
                const isOptionalCompatibilityRequest = isAccountsListRequest || isRecentTransactionsRequest;

                // Keep session intact for optional/compat routes that have local fallback handling.
                if (error.status === 401 && !isOptionalCompatibilityRequest && !isAuthRequest) {
                    // Centralised place to clear auth state on 401s.
                    this.tokenStorage.clear();
                    this.snackBar.open('Session expired. Please log in again.', 'Close', {
                        duration: 3500,
                        panelClass: ['error-snackbar']
                    });
                    this.router.navigate(['/login']);
                }
                return throwError(() => error);
            })
        );
    }
}
