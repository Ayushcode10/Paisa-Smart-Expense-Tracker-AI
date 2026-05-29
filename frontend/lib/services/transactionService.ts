//transactionService.ts

import * as SecureStore from "expo-secure-store";
import { BASE_URL } from "./config";

// ── Types (from OpenAPI spec) ─────────────────────────────────

export type TransactionType = "DEBIT" | "CREDIT";
export type TransactionSource =
  | "UPI" | "CARD" | "CASH" | "WALLET"
  | "NET_BANKING" | "IMPS" | "NEFT" | "RTGS"
  | "ATM" | "EMI" | "UNKNOWN";

export interface TransactionResponse {
  pagination: {
  id: string;
  amount: number;
  merchant: string;
  category: string;
  categoryConfidence: number; 
  type: TransactionType;
  source: TransactionSource;
  paymentMethod: string;
  note: string;
  tags: string[];
  bankName: string;
  accountLast4: string;
  smsImported: boolean;
  transactionDate: string;      // ISO date-time
  createdAt: string;
}
}

export interface TransactionListResponse {
  content: TransactionResponse[];
  pagination: {
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    last: boolean;
  };
}

export interface TransactionSummaryResponse {
  totalDebit: number;
  totalCredit: number;
  netBalance: number;
  transactionCount: number;
  spendByCategory: Record<string, number>;
  spendBySource: Record<string, number>;
}

export interface CreateTransactionRequest {
  amount: number;
  merchant: string;
  type: TransactionType;
  category?: string;
  source?: TransactionSource;
  paymentMethod?: string;
  note?: string;
  tags?: string[];
  transactionDate?: string;
}

export interface GetTransactionsParams {
  page?: number;
  size?: number;
  category?: string;
  type?: TransactionType;
  source?: TransactionSource;
  from?: string;
  to?: string;
  search?: string;
}

// ── Helpers ───────────────────────────────────────────────────

async function getToken(): Promise<string> {
  const token = await SecureStore.getItemAsync("auth_token");
  if (!token) throw new Error("Not authenticated");
  return token;
}

async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const token = await getToken();
  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
      ...options.headers,
    },
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body?.error ?? `HTTP ${res.status}`);
  }
  return res.json();
}

function buildQuery(params: Record<string, any>): string {
  const q = Object.entries(params)
    .filter(([, v]) => v !== undefined && v !== null && v !== "")
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join("&");
  return q ? `?${q}` : "";
}

// ── API calls ─────────────────────────────────────────────────

export const transactionService = {
  /** GET /api/transactions  — paginated + filtered list */
  getAll(params: GetTransactionsParams = {}): Promise<TransactionListResponse> {
    return apiFetch(`/api/transactions${buildQuery(params)}`);
  },

  /** GET /api/transactions/summary */
  getSummary(from: string, to: string): Promise<TransactionSummaryResponse> {
    return apiFetch(`/api/transactions/summary${buildQuery({ from, to })}`);
  },

  /** GET /api/transactions/:id */
  getById(id: string): Promise<TransactionResponse> {
    return apiFetch(`/api/transactions/${id}`);
  },

  /** POST /api/transactions */
  create(data: CreateTransactionRequest): Promise<TransactionResponse> {
    return apiFetch("/api/transactions", {
      method: "POST",
      body: JSON.stringify(data),
    });
  },

  /** PUT /api/transactions/:id */
  update(
    id: string,
    data: Partial<CreateTransactionRequest>
  ): Promise<TransactionResponse> {
    return apiFetch(`/api/transactions/${id}`, {
      method: "PUT",
      body: JSON.stringify(data),
    });
  },

  /** DELETE /api/transactions/:id */
  delete(id: string): Promise<void> {
    return apiFetch(`/api/transactions/${id}`, { method: "DELETE" });
  },
};