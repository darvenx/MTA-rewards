export enum TransactionType {
    DEBIT = 'DEBIT',
    CREDIT = 'CREDIT'
}

export enum TransactionStatus {
    SUCCESS = 'SUCCESS',
    FAILED = 'FAILED'
}

export interface Transaction {
    id: string;
    accountId: string;
    type: TransactionType;
    amount: number;
    date: string; // ISO string
    status: TransactionStatus;
    description?: string;
    // other party information (for debit: receiver; for credit: sender)
    otherPartyName?: string;
    otherAccountNumber?: string;
}
