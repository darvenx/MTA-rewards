import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { Router } from '@angular/router';
import { SessionUser } from '../core/models/session-user.model';
import {
    ApiUserLoginRequest,
    ApiUserSignUpRequest,
    ApiUserSuccessLoginOrSignUpDto
} from '../core/api/backend-contracts';
import ApiEndpoints, { ApiHttpConfig } from '../config/api.endpoints';
import { TokenStorageService } from '../core/services/token-storage.service';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private currentUserSubject = new BehaviorSubject<SessionUser | null>(null);

    constructor(
        private http: HttpClient,
        private router: Router,
        // token + minimal session data are abstracted behind this service so
        // switching storage mechanisms later is localized.
        private tokenStorage: TokenStorageService
    ) {
        const savedToken = this.tokenStorage.getToken();
        const savedAccountId = this.tokenStorage.getAccountId();
        const savedHolderName = this.tokenStorage.getHolderName();

        if (savedToken && savedAccountId && savedHolderName) {
            this.currentUserSubject.next({
                token: savedToken,
                accountId: savedAccountId,
                holderName: savedHolderName
            });
        }
    }

    // Transport-layer login using the NEW backend contract types.
    // The integration team only needs to adjust `ApiEndpoints.user.login`
    // or `ApiHttpConfig.user.loginMethod` if the URL or verb changes;
    // the request/response shapes are encapsulated by the ApiUser* DTOs.
    login(username: string, password: string): Observable<ApiUserSuccessLoginOrSignUpDto> {
        const payload: ApiUserLoginRequest = { username, password };
        const url = ApiEndpoints.user.login();

        if (ApiHttpConfig.user.loginMethod === 'GET') {
            // Legacy fallback if GET-with-body login is required.
            return this.http.get<ApiUserSuccessLoginOrSignUpDto>(url, {
                params: payload as any
            });
        }

        return this.http.post<ApiUserSuccessLoginOrSignUpDto>(url, payload);
    }

    signup(data: {
        phoneNumber: string;
        email: string;
        username: string;
        password: string;
        firstName: string;
        lastName: string;
    }): Observable<ApiUserSuccessLoginOrSignUpDto> {
        const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
        // explicit payload mapping (keeps contract clear and avoids accidental extra properties)
        const payload: ApiUserSignUpRequest = {
            phoneNumber: data.phoneNumber,
            email: data.email,
            username: data.username,
            password: data.password,
            firstName: data.firstName,
            lastName: data.lastName
        };

        // ✅ Clean separation of concerns - just make the HTTP call.
        // Let the component handle the response and side effects.
        return this.http.post<ApiUserSuccessLoginOrSignUpDto>(ApiEndpoints.user.signup(), payload, { headers });
    }

    /**
     * Normalised place to establish/update the current session using
     * a UI-level `SessionUser` model. Components should NOT touch
     * storage directly.
     */
    setSession(user: SessionUser): void {
        this.tokenStorage.setToken(user.token);
        this.tokenStorage.setAccountId(user.accountId);
        this.tokenStorage.setHolderName(user.holderName);
        this.currentUserSubject.next(user);
    }

    logout(): void {
        this.tokenStorage.clear();
        this.currentUserSubject.next(null);
        this.router.navigate(['/login']);
    }

    getToken(): string | null {
        return this.tokenStorage.getToken();
    }

    getAccountId(): string | null {
        return this.tokenStorage.getAccountId();
    }

    isAuthenticated(): boolean {
        return !!this.getToken();
    }

    forgotPassword(payload: { phoneNumber: string; email: string; accountNumber: number; newPassword: string; }) {
        return this.http.post<boolean>(ApiEndpoints.user.forgotPassword(), payload);
    }
}
