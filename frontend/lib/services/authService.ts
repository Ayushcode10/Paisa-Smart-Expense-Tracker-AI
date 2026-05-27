import { apiClient } from '../apiClient';
import type { AuthResponse, SendOtpRequest, VerifyOtpRequest } from '../types';

export const authService = {
  async sendOtp(data: SendOtpRequest): Promise<{ message: string }> {
    const res = await apiClient.post('/api/auth/send-otp', data);
    return res.data;
  },

  async verifyOtp(data: VerifyOtpRequest): Promise<AuthResponse> {
    const res = await apiClient.post('/api/auth/verify-otp', data);
    return res.data;
  },
};

