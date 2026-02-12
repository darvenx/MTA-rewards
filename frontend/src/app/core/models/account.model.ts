export interface Account {
    id: string;      // maps to /accounts/{id}
    holderName: string;
    username?: string;
    accountNumber?: string;
    balance?: number;
    // Optional additional fields used in the UI
    type?: string;
    status?: string;
    ifsc?: string;
    branch?: string;
    email?: string;
    phone?: string;
    address?: string;
}
