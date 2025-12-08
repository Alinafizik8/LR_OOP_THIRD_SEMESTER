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
    console.log('Logging in:', username);

    const loginData = {
      usernameOrEmail: username,
      password
    };

    console.log('Sending login data:', loginData);

    const res = await api.post('/api/auth/login', loginData);
    console.log('Login response:', res.data);

    let user = {
      username,
      credentials: btoa(`${username}:${password}`)
    };

    try {
      // Пытаемся получить профиль пользователя
      const profileRes = await api.get(`/users/by-username/${username}`);
      user.id = profileRes.data.id;
      user.role = profileRes.data.role;
      user.email = profileRes.data.email;
    } catch (err) {
      console.warn('Could not fetch user profile, using temporary data');
      user.id = Date.now();
      user.role = 'USER';
      user.email = `${username}@example.com`;
    }

    localStorage.setItem('user', JSON.stringify(user));
    return user;

  } catch (error) {
    console.error('Login error:', error.response?.data || error.message);
    throw new Error(
      error.response?.data?.message ||
      error.response?.data?.error ||
      'Login failed'
    );
  }
};

export const logout = () => {
  localStorage.removeItem('user');
};

export const getCurrentUser = () => {
  const data = localStorage.getItem('user');
  return data ? JSON.parse(data) : null;
};
