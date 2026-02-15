export interface ApiFieldError {
  field: string;
  message: string;
}

type UnknownRecord = Record<string, unknown>;

function isRecord(value: unknown): value is UnknownRecord {
  return typeof value === 'object' && value !== null;
}

function toNonEmptyString(value: unknown): string | null {
  if (typeof value !== 'string') {
    return null;
  }

  const normalized = value.trim();
  return normalized.length > 0 ? normalized : null;
}

function appendUnique(target: string[], message: unknown): void {
  const normalized = toNonEmptyString(message);
  if (!normalized || target.includes(normalized)) {
    return;
  }
  target.push(normalized);
}

function collectMessagesFromRecord(input: UnknownRecord, target: string[]): void {
  appendUnique(target, input['message']);
  appendUnique(target, input['error']);
  appendUnique(target, input['detail']);
  appendUnique(target, input['title']);
  appendUnique(target, input['description']);

  const messages = input['messages'];
  if (Array.isArray(messages)) {
    messages.forEach((item) => appendUnique(target, item));
  }

  const fieldErrors = input['fieldErrors'];
  if (Array.isArray(fieldErrors)) {
    fieldErrors.forEach((item) => {
      if (!isRecord(item)) return;
      appendUnique(target, item['message']);
    });
  }

  const errors = input['errors'];
  if (isRecord(errors)) {
    Object.values(errors).forEach((value) => {
      if (Array.isArray(value)) {
        value.forEach((item) => appendUnique(target, item));
        return;
      }
      appendUnique(target, value);
    });
  }

  const nestedError = input['error'];
  if (isRecord(nestedError)) {
    collectMessagesFromRecord(nestedError, target);
  }
}

export function extractApiFieldErrors(error: unknown): ApiFieldError[] {
  if (!isRecord(error)) {
    return [];
  }

  const payload = error['error'];
  if (!isRecord(payload)) {
    return [];
  }

  const normalized: ApiFieldError[] = [];

  const fieldErrors = payload['fieldErrors'];
  if (Array.isArray(fieldErrors)) {
    fieldErrors.forEach((item) => {
      if (!isRecord(item)) return;
      const message = toNonEmptyString(item['message']);
      if (!message) return;
      normalized.push({
        field: toNonEmptyString(item['field']) ?? '',
        message
      });
    });
  }

  const errors = payload['errors'];
  if (isRecord(errors)) {
    Object.entries(errors).forEach(([field, value]) => {
      if (Array.isArray(value)) {
        value.forEach((item) => {
          const message = toNonEmptyString(item);
          if (!message) return;
          normalized.push({ field, message });
        });
        return;
      }

      const message = toNonEmptyString(value);
      if (!message) return;
      normalized.push({ field, message });
    });
  }

  return normalized;
}

export function extractApiErrorMessages(error: unknown): string[] {
  const messages: string[] = [];

  if (typeof error === 'string') {
    appendUnique(messages, error);
    return messages;
  }

  if (!isRecord(error)) {
    return messages;
  }

  const payload = error['error'];
  if (typeof payload === 'string') {
    appendUnique(messages, payload);
  } else if (isRecord(payload)) {
    collectMessagesFromRecord(payload, messages);
  }

  collectMessagesFromRecord(error, messages);
  return messages;
}

export function extractApiErrorMessage(error: unknown, fallback: string): string {
  if (isRecord(error) && error['status'] === 0) {
    return 'Cannot connect to server. Please check your connection.';
  }

  const messages = extractApiErrorMessages(error);
  if (messages.length > 0) {
    return messages[0];
  }

  return fallback;
}
