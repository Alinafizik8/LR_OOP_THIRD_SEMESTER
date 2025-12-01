import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend } from 'chart.js';
import { Line } from 'react-chartjs-2';
import zoomPlugin from 'chartjs-plugin-zoom';
import { Paper, Text } from '@mantine/core';

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Tooltip, Legend, zoomPlugin);

interface FunctionChartProps {
  points: { x: number; y: number }[];
  title?: string;
}

export default function FunctionChart({ points, title = 'График функции' }: FunctionChartProps) {
  const data = {
    labels: points.map(p => p.x.toFixed(2)),
    datasets: [
      {
        label: 'f(x)',
         points.map(p => ({ x: p.x, y: p.y })),
        borderColor: '#64b5f6',
        backgroundColor: 'rgba(100, 181, 246, 0.1)',
        borderWidth: 2,
        pointRadius: 3,
        pointHoverRadius: 6,
        fill: true,
        tension: 0.3,
      },
    ],
  };

  const options = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: true },
      zoom: {
        zoom: { wheel: { enabled: true }, pinch: { enabled: true } },
        pan: { enabled: true, mode: 'xy' as const },
      },
    },
    scales: {
      x: { type: 'linear', position: 'bottom' as const },
      y: { beginAtZero: false },
    },
  };

  return (
    <Paper p="md" radius="md" withBorder style={{ height: 400 }}>
      <Text size="lg" fw={500} mb="sm">{title}</Text>
      <div style={{ height: 'calc(100% - 30px)' }}>
        <Line data={data} options={options} />
      </div>
    </Paper>
  );
}