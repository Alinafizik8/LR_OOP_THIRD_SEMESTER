import api from './api';

export const login = async (username, password) => {
  const credentials = btoa(`${username}:${password}`);
  // Проверка: делаем запрос /function-types (публичный)
  await api.get('/function-types', {
    headers: { Authorization: `Basic ${credentials}` }
  });

  let user = { username, credentials };
  try {
    const profileRes = await api.get(`/users/by-username/${username}`);
    user.id = profileRes.data.id;
    user.role = profileRes.data.role;
  } catch (err) {
    console.warn('Не удалось получить профиль пользователя');
  }

  localStorage.setItem('user', JSON.stringify(user));
  return user;
};

export const logout = () => {
  localStorage.removeItem('user');
};

export const getCurrentUser = () => {
  const data = localStorage.getItem('user');
  return data ? JSON.parse(data) : null;
};