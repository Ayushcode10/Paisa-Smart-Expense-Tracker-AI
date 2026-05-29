// transactionService.ts

import { apiClient } from '../apiClient';

// ── Types ─────────────────────────────────────────────────────

export type TransactionType = "DEBIT" | "CREDIT";
export type TransactionSource =
  | "UPI" | "CARD" | "CASH" | "WALLET"
  | "NET_BANKING" | "IMPS" | "NEFT" | "RTGS"
  | "ATM" | "EMI" | "UNKNOWN";

export interface TransactionResponse {
  id: string;
  amount: number;
  merchant: string;
  category: string;
  categoryConfidence?: number;
  type: TransactionType;
  source: TransactionSource;
  paymentMethod: string;
  note: string;
  tags: string[];
  bankName?: string;
  accountLast4?: string;
  smsImported: boolean;
  transactionDate: string;
  createdAt: string;
}

export interface TransactionListResponse {
  content: TransactionResponse[];
  last: boolean;
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
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

// ── Service ───────────────────────────────────────────────────

export const transactionService = {
  getAll(params: GetTransactionsParams = {}): Promise<TransactionListResponse> {
    return apiClient.get('/api/transactions', { params }).then(r => r.data);
  },

  getSummary(from: string, to: string): Promise<TransactionSummaryResponse> {
    return apiClient.get('/api/transactions/summary', { params: { from, to } }).then(r => r.data);
  },

  getById(id: string): Promise<TransactionResponse> {
    return apiClient.get(`/api/transactions/${id}`).then(r => r.data);
  },

  create(data: CreateTransactionRequest): Promise<TransactionResponse> {
    return apiClient.post('/api/transactions', data).then(r => r.data);
  },

  update(id: string, data: Partial<CreateTransactionRequest>): Promise<TransactionResponse> {
    return apiClient.put(`/api/transactions/${id}`, data).then(r => r.data);
  },

  delete(id: string): Promise<void> {
    return apiClient.delete(`/api/transactions/${id}`).then(r => r.data);
  },
};