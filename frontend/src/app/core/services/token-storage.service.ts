import { Injectable } from '@angular/core';

// Centralised, backend-agnostic storage for auth/session tokens and
// lightweight session metadata. Today this is just `localStorage`,
// but the integration team can swap this for cookies, SessionStorage
// or any other mechanism without touching the rest of the app.

@Injectable({
  providedIn: 'root'
})
export class TokenStorageService {
  private readonly TOKEN_KEY = 'token';
  private readonly ACCOUNT_ID_KEY = 'accountId';
  private readonly HOLDER_NAME_KEY = 'holderName';

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  setToken(token: string | null): void {
    if (token == null) {
      localStorage.removeItem(this.TOKEN_KEY);
    } else {
      localStorage.setItem(this.TOKEN_KEY, token);
    }
  }

  getAccountId(): string | null {
    return localStorage.getItem(this.ACCOUNT_ID_KEY);
  }

  setAccountId(accountId: string | null): void {
    if (accountId == null) {
      localStorage.removeItem(this.ACCOUNT_ID_KEY);
    } else {
      localStorage.setItem(this.ACCOUNT_ID_KEY, accountId);
    }
  }

  getHolderName(): string | null {
    return localStorage.getItem(this.HOLDER_NAME_KEY);
  }

  setHolderName(holderName: string | null): void {
    if (holderName == null) {
      localStorage.removeItem(this.HOLDER_NAME_KEY);
    } else {
      localStorage.setItem(this.HOLDER_NAME_KEY, holderName);
    }
  }

  clear(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.ACCOUNT_ID_KEY);
    localStorage.removeItem(this.HOLDER_NAME_KEY);
  }
}

