import { ApiUserSuccessLoginOrSignUpDto } from '../api/backend-contracts';

export type AppUserRole = 'ADMIN' | 'USER' | 'UNKNOWN';

function decodeTokenPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.');
    if (parts.length < 2) return null;

    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    const json = atob(padded);
    return JSON.parse(json) as Record<string, unknown>;
  } catch {
    return null;
  }
}

export function getRoleFromToken(token: string | null | undefined): AppUserRole {
  if (!token) return 'UNKNOWN';
  const payload = decodeTokenPayload(token);
  if (!payload) return 'UNKNOWN';

  const rawRole = String(payload['role'] ?? '').toUpperCase();
  if (rawRole.includes('ADMIN')) return 'ADMIN';
  if (rawRole.includes('USER')) return 'USER';
  return 'UNKNOWN';
}

export function getRoleFromLoginResponse(dto: ApiUserSuccessLoginOrSignUpDto): AppUserRole {
  const responseRole = String(dto.role ?? '').toUpperCase();
  if (responseRole.includes('ADMIN')) return 'ADMIN';
  if (responseRole.includes('USER')) return 'USER';
  return getRoleFromToken(dto.token);
}
