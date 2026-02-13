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

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    constructor(private tokenStorage: TokenStorageService, private router: Router) { }

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
                const isOptionalCompatibilityRequest = isAccountsListRequest || isRecentTransactionsRequest;

                // Keep session intact for optional/compat routes that have local fallback handling.
                if (error.status === 401 && !isOptionalCompatibilityRequest) {
                    // Centralised place to clear auth state on 401s.
                    this.tokenStorage.clear();
                    this.router.navigate(['/login']);
                }
                return throwError(() => error);
            })
        );
    }
}
