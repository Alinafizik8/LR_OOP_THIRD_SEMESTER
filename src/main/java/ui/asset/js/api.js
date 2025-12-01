const api = {
  // Все запросы идут с Basic Auth (браузер добавляет заголовок автоматически)
  get: (url) => fetch(`/api${url}`),
  post: (url, data) => fetch(`/api${url}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }),
  put: (url, data) => fetch(`/api${url}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data)
  }),

  // Конкретные методы
  getFunctions: () => api.get('/v1/functions').then(r => r.json()),
  createFunction: (fn) => api.post('/v1/functions', fn).then(r => r.json()),
  createFromMath: (name, key, from, to, steps) =>
    api.post('/v1/functions/from-math', { name, mathFunctionKey: key, from, to, steps }).then(r => r.json()),
  getMathFunctions: () => api.get('/v1/functions/meta').then(r => r.json()),
  applyOperation: (op, leftId, rightId) =>
    api.post('/v1/operations', { op, leftId, rightId }).then(r => r.json()),
  differentiate: (id) => api.post('/v1/differentiate', { id }).then(r => r.json()),
  integrate: (id, threads) => api.post('/v1/integrate', { id, threads }).then(r => r.json()),
  apply: (id, x) => api.post(`/v1/functions/${id}/apply`, { x }).then(r => r.json())
};
//const api = {
//  client: {
//    get: (url) => fetch(`/api${url}`, { headers: getAuthHeaders() }),
//    post: (url, data) => fetch(`/api${url}`, {
//      method: 'POST',
//      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
//      body: JSON.stringify(data)
//    }),
//    put: (url, data) => fetch(`/api${url}`, {
//      method: 'PUT',
//      headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
//      body: JSON.stringify(data)
//    }),
//    delete: (url) => fetch(`/api${url}`, { method: 'DELETE', headers: getAuthHeaders() })
//  },
//
//  login: async (username, password) => {
//    const res = await fetch('/api/v1/auth/login', {
//      method: 'POST',
//      headers: { 'Content-Type': 'application/json' },
//      body: JSON.stringify({ username, password })
//    });
//    if (!res.ok) throw { response: { status: res.status, data: await res.json() } };
//    return await res.json();
//  },
//
//  register: async (username, password) => {
//    const res = await fetch('/api/v1/auth/register', {
//      method: 'POST',
//      headers: { 'Content-Type': 'application/json' },
//      body: JSON.stringify({ username, password })
//    });
//    if (!res.ok) throw { response: { status: res.status, data: await res.json() } };
//  },
//
//  getFunctions: () => api.client.get('/v1/functions').then(r => r.json()),
//  createFunction: (fn) => api.client.post('/v1/functions', fn).then(r => r.json()),
//  createFromMath: (name, key, from, to, steps) =>
//    api.client.post('/v1/functions/from-math', { name, mathFunctionKey: key, from, to, steps }).then(r => r.json()),
//  getMathFunctions: () => api.client.get('/v1/functions/meta').then(r => r.json()),
//  differentiate: (id) => api.client.post('/v1/differentiate', { id }).then(r => r.json()),
//  integrate: (id, threads) => api.client.post('/v1/integrate', { id, threads }).then(r => r.json()),
//  applyOperation: (op, leftId, rightId) =>
//    api.client.post('/v1/operations', { op, leftId, rightId }).then(r => r.json())
//};
//
//function getAuthHeaders() {
//  const token = localStorage.getItem('token');
//  return token ? { 'Authorization': `Bearer ${token}` } : {};
//}
//
//// Авто-выход при 401
//window.addEventListener('unhandledrejection', (event) => {
//  if (event.reason?.response?.status === 401) {
//    localStorage.removeItem('token');
//    if (!window.location.pathname.includes('index.html')) {
//      window.location.href = 'index.html';
//    }
//  }
//});
//
//export { api, showError, showSuccess };