import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../config/api.endpoints';
import { ApiSpendingAnalyticsDto } from '../core/api/backend-contracts';

@Injectable({
    providedIn: 'root'
})
export class AnalyticsService {

    constructor(private http: HttpClient) { }

    getSpendingAnalysis(accountId: string | number): Observable<ApiSpendingAnalyticsDto> {
        return this.http.get<ApiSpendingAnalyticsDto>(ApiEndpoints.analytics.spending(accountId));
    }
}
