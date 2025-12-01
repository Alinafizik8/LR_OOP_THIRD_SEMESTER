import { Button, Title, Text, Container, Grid, Card } from '@mantine/core';
import { IconSettings, IconFunction, IconMathFunction, IconCalculator, IconChartBar, IconIntegral } from '@tabler/icons-react';
import { Link } from 'react-router-dom';

export default function MainPage() {
  return (
    <Container size="lg" py="xl">
      <Title order={1} ta="center" mb="lg">
        📊 Табулированные функции
      </Title>
      <Text size="lg" ta="center" mb="xl" c="dimmed">
        Создавайте, редактируйте и анализируйте функции. Без дублирования точек!
      </Text>

      <Grid>
        <Grid.Col span={{ base: 12, sm: 6, md: 4 }}>
          <Card withBorder padding="xl" radius="md">
            <IconSettings size={48} stroke={1.5} />
            <Title order={3} mt="md">Настройки</Title>
            <Text mt="xs" c="dimmed">
              Выберите фабрику: массив или связный список
            </Text>
            <Button component={Link} to="/settings" mt="md" fullWidth>
              Открыть
            </Button>
          </Card>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 4 }}>
          <Card withBorder padding="xl" radius="md">
            <IconFunction size={48} stroke={1.5} />
            <Title order={3} mt="md">Создать функцию</Title>
            <Text mt="xs" c="dimmed">
              Из массивов x/y или из MathFunction
            </Text>
            <Button component={Link} to="/create" mt="md" fullWidth>
              Создать
            </Button>
          </Card>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 4 }}>
          <Card withBorder padding="xl" radius="md">
            <IconMathFunction size={48} stroke={1.5} />
            <Title order={3} mt="md">Операции</Title>
            <Text mt="xs" c="dimmed">
              + − × ÷ над двумя функциями
            </Text>
            <Button component={Link} to="/operations" mt="md" fullWidth>
              Открыть
            </Button>
          </Card>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 4 }}>
          <Card withBorder padding="xl" radius="md">
            <IconCalculator size={48} stroke={1.5} />
            <Title order={3} mt="md">Дифференцирование</Title>
            <Text mt="xs" c="dimmed">
              Найдите производную функции
            </Text>
            <Button component={Link} to="/differentiate" mt="md" fullWidth>
              Открыть
            </Button>
          </Card>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 4 }}>
          <Card withBorder padding="xl" radius="md">
            <IconChartBar size={48} stroke={1.5} />
            <Title order={3} mt="md">График</Title>
            <Text mt="xs" c="dimmed">
              Визуализация + apply(x)
            </Text>
            <Button component={Link} to="/graph" mt="md" fullWidth>
              Открыть
            </Button>
          </Card>
        </Grid.Col>

        <Grid.Col span={{ base: 12, sm: 6, md: 4 }}>
          <Card withBorder padding="xl" radius="md">
            <IconIntegral size={48} stroke={1.5} />
            <Title order={3} mt="md">Интеграл</Title>
            <Text mt="xs" c="dimmed">
              Параллельное вычисление
            </Text>
            <Button component={Link} to="/integral" mt="md" fullWidth>
              Открыть
            </Button>
          </Card>
        </Grid.Col>
      </Grid>
    </Container>
  );
}