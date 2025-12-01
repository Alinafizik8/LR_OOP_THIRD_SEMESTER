import { useState, useEffect } from 'react';
import { Button, Radio, Paper, Title, Text } from '@mantine/core';
import api from '../api/client';
import { handleApiError } from '../utils/handleApiError';

export default function SettingsPage() {
  const [factory, setFactory] = useState<'ARRAY' | 'LINKED_LIST'>('ARRAY');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await api.get<{ factory: 'ARRAY' | 'LINKED_LIST' }>('/v1/settings/factory');
        setFactory(res.data.factory);
      } catch (err) {
        handleApiError(err);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  const save = async () => {
    try {
      await api.put('/v1/settings/factory', { factory });
      handleApiError({ response: { status: 200,  { message: 'Настройки сохранены' } } }, 'Готово!');
    } catch (err) {
      handleApiError(err);
    }
  };

  return (
    <div style={{ maxWidth: 600, margin: '0 auto', padding: '2rem' }}>
      <Paper p="xl" radius="md" withBorder>
        <Title order={2} mb="md">⚙️ Настройки</Title>
        <Text mb="lg" c="dimmed">
          Выберите реализацию фабрики табулированных функций
        </Text>

        <Radio.Group
          value={factory}
          onChange={(val) => setFactory(val as any)}
          label="Фабрика"
          size="md"
          mb="xl"
        >
          <Radio value="ARRAY" label="Массив (по умолчанию)" />
          <Radio value="LINKED_LIST" label="Связный список" />
        </Radio.Group>

        <Button onClick={save} loading={loading} fullWidth>
          Сохранить
        </Button>
      </Paper>
    </div>
  );
}