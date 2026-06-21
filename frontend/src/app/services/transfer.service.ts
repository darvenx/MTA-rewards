import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TransferRequest } from '../core/models/transfer-request.model';
import ApiEndpoints from '../config/api.endpoints';
import { ApiTransferRequestDto, ApiTransferResponseDto } from '../core/api/backend-contracts';

@Injectable({
    providedIn: 'root'
})
export class TransferService {
    constructor(private http: HttpClient) { }

    // Uses the new backend transfer DTOs at the transport layer while
    // keeping the UI-facing `TransferRequest` model stable.
    transfer(request: TransferRequest): Observable<ApiTransferResponseDto> {
        const payload: ApiTransferRequestDto = {
            senderAccountNumber: request.senderAccountNumber,
            receiverAccountNumber: request.receiverAccountNumber,
            senderAccountPin: request.senderAccountPin ?? '',
            amount: request.amount,
            idempotencyKey: request.idempotencyKey,
            category: request.category
        };

        return this.http.post<ApiTransferResponseDto>(ApiEndpoints.transfer.create(), payload);
    }
}
