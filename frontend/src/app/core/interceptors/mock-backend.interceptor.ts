import { Injectable } from '@angular/core';
import {
    HttpRequest,
    HttpHandler,
    HttpEvent,
    HttpInterceptor,
    HttpResponse
} from '@angular/common/http';
import { Observable, of, delay } from 'rxjs';

@Injectable()
export class MockBackendInterceptor implements HttpInterceptor {

    constructor() { }

    intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
        const { url, method, body } = request;

        // Simulate network delay
        const simulatedDelay = 800;

        // --- Auth API (mocked against unified /user resource) ---
        if (url.endsWith('/user') && method === 'POST') {
            const { username, password } = body as any;
            if (username && password) {
                return of(new HttpResponse({
                    status: 200,
                    body: {
                        token: 'fake-jwt-token-123456',
                        userId: 1,
                        accountNumbers: [1234567890],
                        accountBalance: [15420.50]
                    }
                })).pipe(delay(simulatedDelay));
            }
        }

        // --- Accounts API (UI-only convenience reads) ---
        // GET Account
        if (url.match(/\/account\/\w+$/) && method === 'GET') {
            return of(new HttpResponse({
                status: 200,
                body: {
                    id: 1234567890,
                    holderName: 'Anupam Kashyap',
                    accountNumber: 1234567890,
                    balance: 15420.50,
                    type: 'SAVINGS',
                    status: 'ACTIVE'
                }
            })).pipe(delay(simulatedDelay));
        }

        // GET Balance
        if (url.match(/\/account\/\w+\/balance$/) && method === 'GET') {
            return of(new HttpResponse({
                status: 200,
                body: 15420.50
            })).pipe(delay(simulatedDelay));
        }

        // GET Transactions (normalised to /transactions/{id})
        if (url.match(/\/transactions\/\w+$/) && method === 'GET') {
            return of(new HttpResponse({
                status: 200,
                body: [
                    {
                        transactionId: 1,
                        amount: 5000.00,
                        otherAccountName: 'Anjali Sharma',
                        transactionStatus: 'SUCCESS',
                        type: 'CREDIT'
                    },
                    {
                        transactionId: 2,
                        amount: 120.00,
                        otherAccountName: 'Electricity Bill',
                        transactionStatus: 'SUCCESS',
                        type: 'DEBIT'
                    },
                    {
                        transactionId: 3,
                        amount: 50.00,
                        otherAccountName: 'Coffee Shop',
                        transactionStatus: 'SUCCESS',
                        type: 'DEBIT'
                    }
                ]
            })).pipe(delay(simulatedDelay));
        }

        // --- Transfers API ---
        if (url.endsWith('/transaction') && method === 'POST') {
            return of(new HttpResponse({
                status: 200,
                body: true
            })).pipe(delay(simulatedDelay));
        }

        // Pass through if not matched (will fail if no backend)
        return next.handle(request);
    }
}
