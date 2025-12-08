import api from './api';

export const register = async (username, password, email = null) => {
  try {
    console.log('Registering user:', username);

    const userData = {
      username,
      password,
      email: email || `${username}@example.com`
    };

    // Используем endpoint из AuthController
    const res = await api.post('/auth/register', userData);
    console.log('Registration response:', res.data);

    // После регистрации можно залогиниться
    return login(username, password);
  } catch (error) {
    console.error('Registration error:', error);
    throw new Error(error.response?.data?.message || 'Registration failed');
  }
};

export const login = async (username, password) => {
  try {
    console.log('Logging in:', username);

    const loginData = {
      usernameOrEmail: username,
      password
    };

    // Используем endpoint из AuthController
    const res = await api.post('/auth/login', loginData);
    console.log('Login response:', res.data);

    // Получаем информацию о пользователе
    let user = { username, credentials: btoa(`${username}:${password}`) };

    try {
      // Пытаемся получить профиль пользователя
      const profileRes = await api.get(`/users/by-username/${username}`);
      user.id = profileRes.data.id;
      user.role = profileRes.data.role;
      user.email = profileRes.data.email;
    } catch (err) {
      console.warn('Не удалось получить профиль пользователя, используем временные данные');
      user.id = Date.now();
      user.role = 'USER';
      user.email = `${username}@example.com`;
    }

    localStorage.setItem('user', JSON.stringify(user));
    return user;

  } catch (error) {
    console.error('Login error:', error);
    throw new Error(error.response?.data || 'Login failed');
  }
};

export const logout = () => {
  localStorage.removeItem('user');
};

export const getCurrentUser = () => {
  const data = localStorage.getItem('user');
  return data ? JSON.parse(data) : null;
};