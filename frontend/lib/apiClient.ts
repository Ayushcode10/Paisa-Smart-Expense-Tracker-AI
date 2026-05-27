import axios from 'axios';

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