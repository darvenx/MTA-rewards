import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';
import { Account } from '../core/models/account.model';
import { Transaction } from '../core/models/transaction.model';
import ApiEndpoints from '../config/api.endpoints';
import { LegacyAccountDto, ApiTransactionsDto, ApiAccountCreateRequest, ApiAccountSuccessCreation } from '../core/api/backend-contracts';
import {
    mapLegacyAccountDtoToAccount,
    mapApiTransactionsDtoToTransaction
} from '../core/api/api-mappers';
import { accountsData } from '../core/models/accounts-data.model';

@Injectable({
    providedIn: 'root'
})
export class AccountService {
    constructor(private http: HttpClient) { }

    getAccount(id: string): Observable<Account> {
        return this.http.get<LegacyAccountDto>(ApiEndpoints.account.get(id)).pipe(
            map(mapLegacyAccountDtoToAccount)
        );
    }

    getBalance(id: string): Observable<accountsData> {
        return this.http.get<accountsData>(ApiEndpoints.account.balance(id));
    }

    getTransactions(id: string): Observable<Transaction[]> {
        console.log("get api");
        return this.http.get<ApiTransactionsDto[]>(ApiEndpoints.transaction.listByAccount(id))
        .pipe(
            map((txns) => txns.map((txn) => mapApiTransactionsDtoToTransaction(txn, id)))
        );
    }

    createAccount(accountData: ApiAccountCreateRequest): Observable<ApiAccountSuccessCreation> {
        return this.http.post<ApiAccountSuccessCreation>(ApiEndpoints.account.create(), accountData);
    }
}
