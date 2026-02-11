// UI-level representation of the authenticated user/session.
// This is intentionally separate from any backend DTO so that
// the UI does not depend directly on transport-layer shapes.

export interface SessionUser {
  token: string;
  holderName: string;
  accountId: string;
}

