import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, TextInput, Title, Text, Paper, Stack } from '@mantine/core';
import { login } from '../../api/auth';
import { setAuthToken } from '../../api/client';
import { handleApiError } from '../../utils/handleApiError';

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      const { token, user } = await login(username, password);
      localStorage.setItem('token', token);
      setAuthToken(token);
      navigate('/main');
    } catch (err) {
      handleApiError(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '2rem', maxWidth: 500, margin: '0 auto' }}>
      <Paper p="xl" radius="md" withBorder>
        <Title order={2} ta="center" mb="md">
          Вход в систему
        </Title>
        <form onSubmit={handleSubmit}>
          <Stack>
            <TextInput
              label="Логин"
              placeholder="user / admin"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <TextInput
              label="Пароль"
              placeholder="word / pass"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <Button type="submit" loading={loading} fullWidth>
              Войти
            </Button>
            <Text size="sm" ta="center">
              Нет аккаунта? <a href="/register">Зарегистрироваться</a>
            </Text>
          </Stack>
        </form>
      </Paper>
    </div>
  );
}