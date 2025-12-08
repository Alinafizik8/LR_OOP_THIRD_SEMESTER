import React, { useState } from 'react';
import { Box, Typography, TextField, Button, Link, Paper } from '@mui/material';
import { toast } from 'react-toastify';
import { register } from '../../api/auth';
import { useNavigate } from 'react-router-dom';

const RegisterPage = () => {
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();

    // ✅ Валидация совпадает с backend требованиями
    if (!username || username.trim().length === 0) {
      toast.error('Логин не может быть пустым');
      return;
    }

    if (!/^[a-zA-Z0-9_]+$/.test(username)) {
      toast.error('Логин может содержать только буквы, цифры и нижнее подчеркивание');
      return;
    }

    if (username.length > 50) {
      toast.error('Логин не может превышать 50 символов');
      return;
    }

    if (password.length < 6) {
      toast.error('Пароль должен содержать не менее 6 символов');
      return;
    }

    if (password.length > 100) {
      toast.error('Пароль не может превышать 100 символов');
      return;
    }

    if (!email || !email.includes('@')) {
      toast.error('Введите корректный email');
      return;
    }

    setLoading(true);
    try {
      // ✅ Вызываем register с правильными параметрами
      await register(username, password, email);

      toast.success('Регистрация прошла успешно! Теперь вы можете войти в систему.');

      // Переходим на страницу входа (пользователь должен залогиниться сам)
      navigate('/login');

    } catch (error) {
      console.error('Registration error:', error);
      toast.error('Ошибка регистрации: ' + error.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Box sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', p: 2 }}>
      <Paper elevation={3} sx={{ p: 4, width: '100%', maxWidth: 400 }}>
        <Typography variant="h5" align="center" gutterBottom>
          Регистрация
        </Typography>
        <form onSubmit={handleSubmit}>
          <TextField
            fullWidth
            label="Логин"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            margin="normal"
            required
            placeholder="username"
            helperText="Только буквы, цифры и нижнее подчеркивание"
          />
          <TextField
            fullWidth
            label="Email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            margin="normal"
            required
            placeholder="example@mail.com"
          />
          <TextField
            fullWidth
            label="Пароль"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            margin="normal"
            required
            placeholder="Минимум 6 символов"
          />
          <Button
            type="submit"
            fullWidth
            variant="contained"
            color="primary"
            disabled={loading}
            sx={{ mt: 3, mb: 2 }}
          >
            {loading ? 'Регистрация...' : 'Зарегистрироваться'}
          </Button>
          <Box sx={{ textAlign: 'center' }}>
            <Typography variant="body2">
              Уже есть аккаунт?{' '}
              <Link href="/login" underline="hover">
                Войти
              </Link>
            </Typography>
          </Box>
        </form>
      </Paper>
    </Box>
  );
};

export default RegisterPage;
