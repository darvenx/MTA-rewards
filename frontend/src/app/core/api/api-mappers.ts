import { Account } from '../models/account.model';
import { Transaction, TransactionStatus, TransactionType } from '../models/transaction.model';
import {
  ApiUserSuccessLoginOrSignUpDto,
  LegacyAccountDto,
  ApiTransactionsDto
} from './backend-contracts';
import { SessionUser } from '../models/session-user.model';

// Mapping helpers between transport-layer DTOs and UI models.
// These are the single place where backend response shapes are
// translated into what the Angular components actually use.
//
// When the new backend is integrated, ONLY these mappers should
// need to change (plus endpoint URLs), not the rest of the UI.

// -----------------------
// Auth / Session mapping
// -----------------------

export function mapLoginSuccessToSessionUser(
  dto: ApiUserSuccessLoginOrSignUpDto,
  holderName: string
): SessionUser {
  const firstAccountNumber = dto.accountNumbers?.[0];
  return {
    token: dto.token,
    holderName,
    accountId: firstAccountNumber != null ? String(firstAccountNumber) : ''
  };
}

// -----------------------
// Account mapping
// -----------------------

export function mapLegacyAccountDtoToAccount(dto: LegacyAccountDto): Account {
  return {
    id: String(dto.id),
    holderName: dto.holderName,
    accountNumber: String(dto.accountNumber),
    balance: dto.balance,
    type: dto.type,
    status: dto.status
  };
}

// Target mapping for the new backend `TransactionsDto`.
// The integration team can switch AccountService to use this
// once `/transactions/{id}` is wired up.
export function mapApiTransactionsDtoToTransaction(
  dto: ApiTransactionsDto,
  currentAccountId: string
): Transaction {
  // The new API already classifies debit/credit and includes the
  // counterparty name directly.
  const type =
    dto.type === 'DEBIT'
      ? TransactionType.DEBIT
      : dto.type === 'CREDIT'
      ? TransactionType.CREDIT
      : TransactionType.DEBIT;

  const status =
    dto.transactionStatus === 'SUCCESS'
      ? TransactionStatus.SUCCESS
      : TransactionStatus.FAILED;

  return {
    transactionId: String(dto.transactionId),
    // accountId: currentAccountId,
    type,
    amount: dto.amount,
    date: new Date().toISOString(),
    transactionStatus:status,
    description: dto.otherAccountName,
    otherAccountName: dto.otherAccountName
  };
}

