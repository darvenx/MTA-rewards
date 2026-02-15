import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { Account } from '../core/models/account.model';
import { Transaction } from '../core/models/transaction.model';
import ApiEndpoints from '../config/api.endpoints';
import {
    LegacyAccountDto,
    ApiTransactionsDto,
    ApiAccountCreateRequest,
    ApiAccountSuccessCreation,
    ApiRecentTransactionsDto
} from '../core/api/backend-contracts';
import {
    mapLegacyAccountDtoToAccount,
    mapApiTransactionsDtoToTransaction
} from '../core/api/api-mappers';
import { accountsData } from '../core/models/accounts-data.model';
import { TokenStorageService } from '../core/services/token-storage.service';

@Injectable({
    providedIn: 'root'
})
export class AccountService {
    constructor(private http: HttpClient, private tokenStorage: TokenStorageService) { }

    getAccount(id: string): Observable<Account> {
        return this.http.get<LegacyAccountDto>(ApiEndpoints.account.get(id)).pipe(
            map(mapLegacyAccountDtoToAccount)
        );
    }

    getBalance(id: string): Observable<accountsData> {
        return this.http.get<accountsData>(ApiEndpoints.account.balance(id));
    }

    getUserAccounts(userId: string): Observable<accountsData> {
        // Prefer the user-scoped accounts endpoint which is expected to
        // return all accounts for the logged-in user.
        return this.http.get<accountsData>(ApiEndpoints.user.accounts(userId)).pipe(
            // Fallback to account endpoint for backend variants.
            catchError(() => this.getBalance(userId))
        );
    }

    getTransactions(id: string): Observable<Transaction[]> {
        console.log("get api");
        return this.http.get<ApiTransactionsDto[]>(ApiEndpoints.transaction.listByAccount(id))
        .pipe(
            map((txns) => txns.map((txn) => mapApiTransactionsDtoToTransaction(txn, id)))
        );
    }

    getRecentTransactions(): Observable<ApiRecentTransactionsDto[]> {
        return this.http.get<ApiRecentTransactionsDto[]>(ApiEndpoints.transaction.recent());
    }

    createAccount(accountData: ApiAccountCreateRequest): Observable<ApiAccountSuccessCreation> {
        return this.http.post<ApiAccountSuccessCreation>(ApiEndpoints.account.create(), accountData);
    }

    toggleAccountStatus(accountId: number): Observable<boolean> {
        const token = this.tokenStorage.getToken();
        const options = token
            ? { responseType: 'text' as const, headers: new HttpHeaders({ Authorization: `Bearer ${token}` }) }
            : { responseType: 'text' as const };

        return this.http.post(ApiEndpoints.account.toggleStatus(accountId), options).pipe(
            map((response) => {
                const body = String(response ?? '').trim().toLowerCase();
                if (body === 'false') return false;
                return true;
            })
        );
    }
}
