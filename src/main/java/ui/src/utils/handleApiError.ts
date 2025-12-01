import { showNotification } from '@mantine/notifications';
import { IconAlertCircle, IconInfoCircle, IconCheck } from '@tabler/icons-react';

export const handleApiError = (error: any, fallback = 'Произошла ошибка') => {
  let title = 'Ошибка';
  let message = fallback;
  let color = 'red' as const;
  let icon = IconAlertCircle;

  if (error.response) {
    const { status, data } = error.response;
    switch (status) {
      case 400:
        title = 'Некорректные данные';
        message = data?.message || 'Проверьте введённые значения';
        break;
      case 401:
        title = 'Не авторизован';
        message = 'Сессия истекла. Войдите заново.';
        color = 'yellow';
        break;
      case 403:
        message = 'Недостаточно прав для выполнения операции.';
        break;
      case 404:
        message = 'Ресурс не найден.';
        break;
      case 409:
        title = 'Конфликт';
        message = data?.message || 'Объект уже существует.';
        color = 'orange';
        break;
      case 500:
        title = 'Ошибка сервера';
        message = `Сервер сообщил: ${data?.message || 'неизвестная ошибка'}`;
        break;
      default:
        message = `HTTP ${status}: ${error.message}`;
    }
  } else if (error.request) {
    title = 'Нет связи';
    message = 'Не удаётся подключиться к серверу. Проверьте интернет.';
    color = 'orange';
  } else {
    message = error.message || 'Неизвестная ошибка.';
  }

  showNotification({
    title,
    message,
    color,
    icon: React.createElement(icon),
    autoClose: 5000,
  });
};