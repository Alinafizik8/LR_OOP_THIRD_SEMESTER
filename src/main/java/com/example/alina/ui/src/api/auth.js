import api from './api';

export const register = async (username, password, email = null) => {
  try {
    console.log('Registering user:', username);

    const userData = {
      email: email || `${username}@example.com`,
      username,
      password
    };

    console.log('Sending registration data:', userData);

    const res = await api.post('/api/auth/register', userData);
    console.log('Registration successful:', res.data);

    return res.data;

  } catch (error) {
    console.error('Registration error:', error.response?.data || error.message);
    throw new Error(
      error.response?.data?.message ||
      error.response?.data?.error ||
      'Registration failed'
    );
  }
};

export const login = async (username, password) => {
  try {
    const loginData = { usernameOrEmail: username, password };
    const res = await api.post(`/api/auth/login`, loginData);

    // Получаем пользователя из ответа login ИЛИ запрашиваем профиль отдельно
    let user = {
      id: res.data.id,
      username: res.data.username,
      email: res.data.email,
      role: res.data.role,
      credentials: btoa(username + ':' + password)
    };

    localStorage.setItem('user', JSON.stringify(user));
    return user;
  } catch (error) {
    console.error('Login error', error.response?.data || error.message);
    throw new Error(error.response?.data?.message || 'Login failed');
  }
};


export const logout = () => {
  localStorage.removeItem('user');
};

export const getCurrentUser = () => {
  const data = localStorage.getItem('user');
  return data ? JSON.parse(data) : null;
};
