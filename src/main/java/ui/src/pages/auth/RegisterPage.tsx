import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button, TextInput, Title, Paper, Stack } from '@mantine/core';
import { register } from '../../api/auth';
import { handleApiError } from '../../utils/handleApiError';

export default function RegisterPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password.length < 4) {
      handleApiError({ response: { status: 400,  { message: 'Пароль должен быть не короче 4 символов' } } });
      return;
    }
    setLoading(true);
    try {
      await register(username, password);
      navigate('/login', { state: { message: 'Регистрация успешна. Войдите.' } });
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
          Регистрация
        </Title>
        <form onSubmit={handleSubmit}>
          <Stack>
            <TextInput
              label="Логин"
              placeholder="ваш логин"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
            <TextInput
              label="Пароль"
              placeholder="не менее 4 символов"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
            <Button type="submit" loading={loading} fullWidth>
              Зарегистрироваться
            </Button>
          </Stack>
        </form>
      </Paper>
    </div>
  );
}