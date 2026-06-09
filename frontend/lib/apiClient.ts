import axios from 'axios';
import * as SecureStore from 'expo-secure-store';

const BASE_URL = process.env.EXPO_PUBLIC_API_URL!;
const POSTMAN_API_KEY = process.env.EXPO_PUBLIC_POSTMAN_API_KEY!;

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    'x-api-key': POSTMAN_API_KEY,
  },
  timeout: 10000,
});

// Attach JWT token to every request automatically
apiClient.interceptors.request.use(async (config) => {
  const token = await SecureStore.getItemAsync('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default BASE_URL;