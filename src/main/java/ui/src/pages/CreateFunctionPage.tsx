import { useState, useEffect } from 'react';
import { Button, Tabs, Paper, Title, TextInput, NumberInput, Select, Group } from '@mantine/core';
import { createFunction, createFromMathFunction, getMathFunctions } from '../api/functions';
import { MathFunctionMeta } from '../types';
import { handleApiError } from '../utils/handleApiError';
import FunctionTable from '../components/FunctionTable';

export default function CreateFunctionPage() {
  // From Arrays
  const [nameArr, setNameArr] = useState('');
  const [size, setSize] = useState(5);
  const [points, setPoints] = useState<{ x: number; y: number }[]>([]);
  const [errorArr, setErrorArr] = useState('');

  // From MathFunction
  const [nameMath, setNameMath] = useState('');
  const [mathFunctions, setMathFunctions] = useState<MathFunctionMeta[]>([]);
  const [selectedFunction, setSelectedFunction] = useState('');
  const [from, setFrom] = useState(0);
  const [to, setTo] = useState(1);
  const [steps, setSteps] = useState(10);

  useEffect(() => {
    const load = async () => {
      try {
        const res = await getMathFunctions();
        setMathFunctions(res.data);
      } catch (err) {
        handleApiError(err);
      }
    };
    load();
  }, []);

  // Обновление таблицы при изменении size
  useEffect(() => {
    if (size <= 0) {
      setErrorArr('Количество точек должно быть > 0');
      return;
    }
    if (size > 1000) {
      setErrorArr('Слишком много точек (>1000). Уменьшите.');
      return;
    }
    setErrorArr('');
    const newPoints = Array.from({ length: size }, (_, i) => ({ x: i, y: 0 }));
    setPoints(newPoints);
  }, [size]);

  const handleCreateFromArray = async () => {
    if (errorArr) return;
    if (!nameArr.trim()) {
      setErrorArr('Введите имя функции');
      return;
    }
    try {
      await createFunction({ name: nameArr, points, type: 'TABULATED' });
      handleApiError({ response: { status: 200,  { message: 'Функция создана' } } }, 'Готово!');
    } catch (err) {
      handleApiError(err);
    }
  };

  const handleCreateFromMath = async () => {
    if (!nameMath.trim() || !selectedFunction) {
      handleApiError({ response: { status: 400,  { message: 'Заполните все поля' } } });
      return;
    }
    try {
      await createFromMathFunction(nameMath, selectedFunction, from, to, steps);
      handleApiError({ response: { status: 200,  { message: 'Функция создана' } } }, 'Готово!');
    } catch (err) {
      handleApiError(err);
    }
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: '2rem' }}>
      <Title order={2} mb="lg">➕ Создать табулированную функцию</Title>

      <Tabs defaultValue="arrays">
        <Tabs.List>
          <Tabs.Tab value="arrays">Из массивов x/y</Tabs.Tab>
          <Tabs.Tab value="math">Из функции MathFunction</Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="arrays" pt="md">
          <Paper p="md" radius="md" withBorder>
            <TextInput
              label="Имя функции"
              value={nameArr}
              onChange={(e) => setNameArr(e.target.value)}
              placeholder="sin(x)"
              mb="md"
            />
            <NumberInput
              label="Количество точек"
              value={size}
              onChange={(val) => setSize(val || 0)}
              min={1}
              max={1000}
              mb="md"
            />
            {errorArr && <div style={{ color: 'red', marginBottom: '1rem' }}>{errorArr}</div>}
            <FunctionTable points={points} editable onPointsChange={setPoints} />
            <Button onClick={handleCreateFromArray} mt="md" fullWidth>
              Создать функцию
            </Button>
          </Paper>
        </Tabs.Panel>

        <Tabs.Panel value="math" pt="md">
          <Paper p="md" radius="md" withBorder>
            <TextInput
              label="Имя функции"
              value={nameMath}
              onChange={(e) => setNameMath(e.target.value)}
              placeholder="sin(x)"
              mb="md"
            />
            <Select
              label="Функция"
              data={mathFunctions.map(f => ({ value: f.key, label: f.localized }))}
              value={selectedFunction}
              onChange={setSelectedFunction}
              placeholder="Выберите функцию"
              mb="md"
            />
            <Group grow mb="md">
              <NumberInput label="От" value={from} onChange={setFrom} />
              <NumberInput label="До" value={to} onChange={setTo} />
              <NumberInput label="Точек" value={steps} onChange={setSteps} min={2} />
            </Group>
            <Button onClick={handleCreateFromMath} fullWidth>
              Создать функцию
            </Button>
          </Paper>
        </Tabs.Panel>
      </Tabs>
    </div>
  );
}