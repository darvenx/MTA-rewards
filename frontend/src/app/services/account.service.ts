import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, forkJoin, of } from 'rxjs';
import { map, switchMap, catchError } from 'rxjs/operators';
import { Account } from '../core/models/account.model';
import { Transaction } from '../core/models/transaction.model';
import ApiEndpoints from '../config/api.endpoints';
import { LegacyAccountDto, ApiTransactionsDto } from '../core/api/backend-contracts';
import {
    mapLegacyAccountDtoToAccount,
    mapApiTransactionsDtoToTransaction
} from '../core/api/api-mappers';

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

    getBalance(id: string): Observable<number> {
        return this.http.get<number>(ApiEndpoints.account.balance(id));
    }

    getTransactions(id: string): Observable<Transaction[]> {
        return this.http.get<ApiTransactionsDto[]>(ApiEndpoints.transaction.listByAccount(id)).pipe(
            map((txns) => txns.map((txn) => mapApiTransactionsDtoToTransaction(txn, id))),
            // For each transaction, fetch the other party's account to read the holder name.
            switchMap((transactions: Transaction[]) => {
                if (!transactions || transactions.length === 0) return of([] as Transaction[]);

                const requests = transactions.map(t => {
                    if (!t.otherAccountNumber) return of(t);
                    return this.getAccount(t.otherAccountNumber).pipe(
                        map(acc => ({ ...t, otherPartyName: acc.holderName })),
                        catchError(() => of({ ...t, otherPartyName: 'Unknown' }))
                    );
                });

                return forkJoin(requests);
            })
        );
    }
}
