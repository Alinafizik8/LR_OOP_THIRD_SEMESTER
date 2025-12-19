import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './FunctionChart.css';

const FunctionChart = ({ functionData, data: propData }) => {
  // Поддерживаем оба варианта передачи данных: functionData и data
  const chartData = propData || functionData;

  if (!chartData || !chartData.xValues || !chartData.yValues) {
    return <div className="chart-empty">Нет данных для отображения</div>;
  }

  // Проверяем, что xValues и yValues - это массивы
  const xValues = Array.isArray(chartData.xValues) ? chartData.xValues : [];
  const yValues = Array.isArray(chartData.yValues) ? chartData.yValues : [];

  if (xValues.length === 0 || yValues.length === 0) {
    return <div className="chart-empty">Нет данных для отображения</div>;
  }

  const data = xValues.map((x, index) => ({
    x: Number(x).toFixed(3),
    y: Number(yValues[index]).toFixed(3),
    xValue: x,
    yValue: yValues[index],
  }));

  return (
    <div className="function-chart">
      <ResponsiveContainer width="100%" height={400}>
        <LineChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="#444" />
          <XAxis
            dataKey="x"
            stroke="#b0b0b0"
            label={{ value: 'X', position: 'insideBottom', offset: -10, fill: '#b0b0b0' }}
          />
          <YAxis
            stroke="#b0b0b0"
            label={{ value: 'Y', angle: -90, position: 'insideLeft', fill: '#b0b0b0' }}
          />
          <Tooltip
            contentStyle={{ backgroundColor: '#2a2a2a', border: '1px solid #444', borderRadius: '6px' }}
            labelStyle={{ color: '#fff' }}
            itemStyle={{ color: '#667eea' }}
          />
          <Legend />
          <Line
            type="monotone"
            dataKey="yValue"
            stroke="#667eea"
            strokeWidth={2}
            dot={{ fill: '#667eea', r: 4 }}
            activeDot={{ r: 6 }}
            name="f(x)"
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
};

export default FunctionChart;
