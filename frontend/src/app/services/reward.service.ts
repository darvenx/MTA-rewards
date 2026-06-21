import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import ApiEndpoints from '../config/api.endpoints';
import { ApiRewardLogDto, ApiRewardSummaryDto } from '../core/api/backend-contracts';

@Injectable({
    providedIn: 'root'
})
export class RewardService {

    constructor(private http: HttpClient) { }

    getRewardHistory(accountId: string | number): Observable<ApiRewardLogDto[]> {
        return this.http.get<ApiRewardLogDto[]>(ApiEndpoints.reward.history(accountId));
    }

    getTotalPoints(accountId: string | number): Observable<ApiRewardSummaryDto> {
        return this.http.get<ApiRewardSummaryDto>(ApiEndpoints.reward.total(accountId));
    }

    redeemPoints(accountId: string | number): Observable<ApiRewardSummaryDto> {
        return this.http.post<ApiRewardSummaryDto>(ApiEndpoints.reward.redeem(accountId), {});
    }
}
