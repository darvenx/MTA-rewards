export interface TransferRequest {
    senderAccountNumber: number;
    receiverAccountNumber: number;
    senderAccountPin?: string;
    amount: number;
    idempotencyKey: string;
}
