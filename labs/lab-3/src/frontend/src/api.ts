import { keycloak } from './keycloak';

export type BookStatus = 'AVAILABLE' | 'BORROWED' | 'RESERVED' | 'MAINTENANCE';

export interface Book {
  id: number;
  isbn: string;
  internalName: string;
  availabilityDate: string;
  title: string;
  author: string;
  thumbnailUrl?: string;
  description?: string;
  status: BookStatus;
}

export interface BookCreationRequest {
  isbn: string;
  internalName: string;
  availabilityDate: string;
}

export interface BookUpdateRequest {
  internalName: string;
  availabilityDate: string;
  status: BookStatus;
}

export class ApiError extends Error {
  constructor(public readonly status: number, message: string, public readonly detail?: string) {
    super(message);
  }
}

const API_BASE = '/api/books';

async function authHeaders(): Promise<Record<string, string>> {
  if (!keycloak.authenticated) {
    return {};
  }
  try {
    await keycloak.updateToken(30);
  } catch {
    // token refresh failed — let the request go through and fail with 401
  }
  return { Authorization: `Bearer ${keycloak.token}` };
}

async function handle<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let detail: string | undefined;
    try {
      const body = await response.json();
      detail = body.detail ?? body.title;
    } catch {
      // ignore body parse failures
    }
    throw new ApiError(response.status, `${response.status} ${response.statusText}`, detail);
  }
  if (response.status === 204) {
    return undefined as T;
  }
  const text = await response.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export async function listBooks(): Promise<Book[]> {
  const response = await fetch(API_BASE, { headers: await authHeaders() });
  return handle<Book[]>(response);
}

export async function getBook(bookId: number): Promise<Book> {
  const response = await fetch(`${API_BASE}/${bookId}`, { headers: await authHeaders() });
  return handle<Book>(response);
}

export async function createBook(request: BookCreationRequest): Promise<void> {
  const response = await fetch(API_BASE, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...(await authHeaders()) },
    body: JSON.stringify(request),
  });
  await handle<void>(response);
}

export async function updateBook(bookId: number, request: BookUpdateRequest): Promise<Book> {
  const response = await fetch(`${API_BASE}/${bookId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...(await authHeaders()) },
    body: JSON.stringify(request),
  });
  return handle<Book>(response);
}

export async function deleteBook(bookId: number): Promise<void> {
  const response = await fetch(`${API_BASE}/${bookId}`, {
    method: 'DELETE',
    headers: await authHeaders(),
  });
  await handle<void>(response);
}
