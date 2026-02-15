// Transport-layer DTOs and contracts for the **NEW** backend.
// ------------------------------------------------------------------
// These interfaces intentionally mirror the Java/Spring DTOs exposed
// by the new backend (see `UserSignUpDto`, `UserLoginDto`,
// `UserSuccessLoginOrSignUpDto`, `AccountCreateDto`,
// `AccountSuccessCreation`, `TransferRequestDto`, `TransactionsDto`).
//
// IMPORTANT:
// - These types describe the wire format only (no UI-only fields).
// - Current Angular services are still talking to the existing/mock
//   backend. Where there are differences, we keep the legacy shapes
//   alongside the new ones and use adapters/mappers to bridge them.
// - The integration team should update the services to use ONLY the
//   `Api*` types once the new backend is wired in.

// ---------------------------
// User / Auth DTOs (new API)
// ---------------------------

export interface ApiUserLoginRequest {
  username: string;
  password: string;
}

export interface ApiUserUpdatePasswordRequest {
  username: string;
  oldPassword: string;
  newPassword: string;
}

export interface ApiUserSignUpRequest {
  phoneNumber: string;
  email: string;
  username: string;
  password: string;
  firstName: string;
  lastName: string;
}

// Matches the Swagger schema shown in the backend screenshots.
// The current Java class may be trimmed down, but the integration
// team should treat this as the target contract.
export interface ApiUserSuccessLoginOrSignUpDto {
  id: number;
  token: string;
  role?: string;
  // Legacy/new frontend naming
  accountBalance?: number[];
  accountNumbers?: number[];
  // Current backend naming
  balances?: number[];
  accounts?: number[];
}

// ---------------------------
// Account DTOs (new API)
// ---------------------------

export interface ApiAccountCreateRequest {
  accountHolderName: string;
  accountType: string; // e.g. SAVINGS, CURRENT
  userId: number;
}

export interface ApiAccountSuccessCreation {
  accountNumbers: number[];
  accountBalance: number[];
  accountType: string[];
  accountStatus: string[];
}

// The new backend primarily exposes account information through
// creation and transaction flows. If a dedicated "get account"
// endpoint is added later, its DTO should be modelled here.
export interface ApiAccountSummary {
  accountId: number;
  holderName: string;
  accountNumber: number;
  balance: number;
  type: string;
  status: string;
}

export interface ApiDeactivateAccountRequest {
  accountId: number;
  accountType: string;
}

// ---------------------------
// Transaction / Transfer DTOs
// ---------------------------

export interface ApiTransferRequestDto {
  senderAccountNumber: number;
  receiverAccountNumber: number;
  senderAccountPin: string;
  idempotencyKey: string;
  amount: number;
}

// `POST /transaction` returns `Boolean` (true/false)
export type ApiTransferResponseDto = boolean;

// New backend `TransactionsDto` shape – see Java class and Swagger.
export interface ApiTransactionsDto {
  transactionId: number;
  amount: number;
  otherAccountName: string;
  transactionStatus: string; // SUCCESS / FAILED / etc.
  type: string; // DEBIT / CREDIT
}

// New backend `RecentTransactionsDto` shape for admin dashboard.
export interface ApiRecentTransactionsDto {
  transactionId: number;
  toAccount: number;
  fromAccount: number;
  amount: number;
  transactionStatus: string;
}

// ---------------------------------------------------------
// Legacy shapes currently used by the mock / old backend.
// ---------------------------------------------------------
// These are kept to avoid breaking the existing UI before the
// new backend is integrated. Adapters can convert between the
// legacy and new API representations where necessary.

export interface LegacyAccountDto {
  id?: number;
  holderName?: string;
  fullName?: string;
  userName?: string;
  username?: string;
  email?: string;
  phone?: string;
  phoneNumber?: string;
  accountNumber?: number;
  balance?: number;
  type?: string;
  status?: string;
  address?: string;
}

export interface LegacyTransactionDto {
  transactionId: number;
  amount: number;
  fromAccountNumber: number;
  toAccountNumber: number;
  createdOn: string;
  otherPersonAccountNumber: number;
  transactionStatus: string;
}
