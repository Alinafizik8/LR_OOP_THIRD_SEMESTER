import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

//  Автоматическая аутентификация для ВСЕХ запросов
api.interceptors.request.use((config) => {
  console.log(`Request: ${config.method?.toUpperCase()} ${config.url}`);

  const user = JSON.parse(localStorage.getItem('user'));
  if (user?.credentials) {
    config.headers.Authorization = `Basic ${user.credentials}`;
    console.log(' Added Basic Auth');
  }
  if (user?.id) {
    config.headers['X-User-Id'] = user.id;
    console.log(' Added X-User-Id:', user.id);
  }

  return config;
});

// Обработка ошибок
api.interceptors.response.use(
  (response) => {
    console.log(`Response: ${response.status} ${response.config.url}`);
    return response;
  },
  (error) => {
    console.error('API Error:', {
      status: error.response?.status,
      url: error.config?.url,
      data: error.response?.data
    });

    if (error.response?.status === 401) {
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

const user = JSON.parse(localStorage.getItem('user'));
if (user?.id) {
  config.headers['X-User-Id'] = user.id;
}


export default api;
