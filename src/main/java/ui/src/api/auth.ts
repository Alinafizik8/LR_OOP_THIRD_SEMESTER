import api from './client';

export const login = async (username: string, password: string) => {
  const res = await api.post('/v1/auth/login', { username, password });
  return res.data; // { token: '...', user: User }
};

export const register = async (username: string, password: string) => {
  await api.post('/v1/auth/register', { username, password });
};