import { environment } from '../../environments/environment';

// Centralized backend endpoint registry. Use this file to connect services to
// actual API routes. Keep paths relative to `environment.apiUrl` so the base
// URL can change per environment (dev/prod).

const BASE = environment.apiUrl;

// Centralised HTTP configuration for edge cases where the backend
// uses a non-standard verb (e.g. GET for login). The integration
// team can change these in one place without touching services.
export const ApiHttpConfig = {
  user: {
    // Integration team: switch to 'GET' if the backend keeps
    // the GET-based login endpoint.
    loginMethod: 'POST' as 'GET' | 'POST'
  }
} as const;

export const ApiEndpoints = {
  user: {
    /**
     * Single logical `/user` resource used for signup, login and update.
     * All user flows should go through this base URL so that switching
     * between legacy and new backend is a one-line change.
     *
     * Backend target: `/api/v1/user`
     */
    base: () => `${BASE}/user`,

    // Convenience builders so existing code does not hardcode paths.
    // Today they are all identical; the integration team can keep them
    // if they later introduce sub-routes.
    signup: () => `${BASE}/user/signup`, // POST UserSignUpDto -> UserSuccessLoginOrSignUpDto
    login: () => `${BASE}/user/login`,  // POST UserLoginDto -> UserSuccessLoginOrSignUpDto
    update: () => `${BASE}/user/update`, // PUT UserSignUpDto -> boolean
    forgotPassword: () => `${BASE}/user/forgot-password`
  },
  account: {
    /**
     * Backend-supported operation: account creation.
     * Backend target: POST `/api/v1/account` (AccountCreateDto).
     */
    create: () => `${BASE}/account`,

    /**
     * UI-only convenience read endpoints. These do not currently exist
     * on the backend and are served by the mock interceptor. They are
     * kept behind this abstraction so the integration team can either
     * implement matching endpoints or refactor the UI in one place.
     */
    get: (id: string | number) => `${BASE}/account/${id}`,
    balance: (id: string | number) => `${BASE}/account/${id}/balance`
  },
  transfer: {
    /**
     * Money transfer endpoint.
     * Backend target: POST `/api/v1/transaction` (TransferRequestDto).
     */
    create: () => `${BASE}/transaction`
  },
  transaction: {
    /**
     * Transaction history for a given account.
     * Backend target: GET `/api/v1/transactions/{id}` (List<TransactionsDto>).
     */
    listByAccount: (id: string | number) => `${BASE}/transactions/${id}`
  }
};

export default ApiEndpoints;
