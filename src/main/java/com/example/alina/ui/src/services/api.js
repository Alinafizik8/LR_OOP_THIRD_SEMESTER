import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export const auth = {
  register: (username, password) =>
    api.post('/auth/register', { username, password }),

  login: (username, password) =>
    api.post('/auth/login', { username, password }),
};

export const functions = {
  getAll: () => api.get('/functions'),

  getById: (id) => api.get(`/functions/${id}`),

  createFromArrays: (name, xValues, yValues, functionType) =>
    api.post('/functions/from-arrays', { name, xValues, yValues, functionType }),

  createFromMath: (data) =>
    api.post('/functions/from-math', data),

  previewFromMath: (data) =>
    api.post('/functions/from-math/preview', data),

  update: (id, data) => api.put(`/functions/${id}`, data),

  delete: (id) => api.delete(`/functions/${id}`),

  getMathTypes: () => api.get('/functions/math-types'),

  differentiate: (functionId, functionType) =>
    api.post('/functions/differentiate', { functionId, functionType }),

  operate: (firstFunctionId, secondFunctionId, operation, functionType) =>
    api.post('/functions/operate', { firstFunctionId, secondFunctionId, operation, functionType }),

  download: (id) => api.get(`/functions/${id}/download`),

  upload: (name, base64Data, functionType) =>
    api.post('/functions/upload', { name, base64Data, functionType }),

  apply: (id, x) => api.post(`/functions/${id}/apply`, { x }),

  insertPoint: (id, x, y) => api.post(`/functions/${id}/insert`, { x, y }),

  removePoint: (id, index) => api.delete(`/functions/${id}/remove/${index}`),

  downloadXml: (id) => api.get(`/functions/${id}/download/xml`),

  uploadXml: (name, xmlData, functionType) =>
    api.post('/functions/upload/xml', { name, xmlData, functionType }),

  downloadJson: (id) => api.get(`/functions/${id}/download/json`),

  uploadJson: (name, jsonData, functionType) =>
    api.post('/functions/upload/json', { name, jsonData, functionType }),
};

export default api;
