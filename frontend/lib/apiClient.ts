import axios from 'axios';

const BASE_URL = "http://192.168.1.8:8080";
// const POSTMAN_API_KEY = process.env.EXPO_PUBLIC_POSTMAN_API_KEY!;

export const apiClient = axios.create({
  baseURL: BASE_URL,
  headers: {
    'Content-Type': 'application/json',
    // 'x-api-key': POSTMAN_API_KEY,
  },
  timeout: 10000,
});

export default BASE_URL;