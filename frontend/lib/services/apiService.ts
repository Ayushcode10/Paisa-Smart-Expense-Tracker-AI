import BASE_URL from '../apiClient';

const getAuthHeaders = (token: string) => ({
  'Content-Type': 'application/json',
  'Authorization': `Bearer ${token}`,
});

// Auth - no token needed
export const sendOtp = async (phone: string) => {
  const res = await fetch(`${BASE_URL}/auth/send-otp`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone }),
  });
  return res.json();
};

export const verifyOtp = async (phone: string, otp: string, name?: string) => {
  const res = await fetch(`${BASE_URL}/auth/verify-otp`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ phone, otp, name }),
  });
  return res.json();
};

// Transactions - token required
export const getTransactions = async (token: string, page = 0, size = 20) => {
  const res = await fetch(`${BASE_URL}/transactions?page=${page}&size=${size}`, {
    headers: getAuthHeaders(token),
  });
  return res.json();
};

export const createTransaction = async (token: string, data: object) => {
  const res = await fetch(`${BASE_URL}/transactions`, {
    method: 'POST',
    headers: getAuthHeaders(token),
    body: JSON.stringify(data),
  });
  return res.json();
};