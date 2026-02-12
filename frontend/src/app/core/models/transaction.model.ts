export enum TransactionType {
    DEBIT = 'DEBIT',
    CREDIT = 'CREDIT'
}

export enum TransactionStatus {
    SUCCESS = 'SUCCESS',
    FAILED = 'FAILED'
}

export interface Transaction {
    transactionId: string;
    amount: number;
    otherAccountName: string;
    type: TransactionType;
    transactionStatus: TransactionStatus;
    date: string; // ISO string
    description?: string;
    // other party information (for debit: receiver; for credit: sender)
    otherPartyName?: string;
    otherAccountNumber?: string;
}
