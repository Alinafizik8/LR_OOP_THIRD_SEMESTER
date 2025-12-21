import React from 'react';
import Plot from 'react-plotly.js';
import './FunctionChart.css';

const FunctionChart2 = ({ functionData, data, coordinateSystem = 'cartesian' }) => {
  const chartData = functionData || data;

  if (!chartData || !chartData.xValues || !chartData.yValues) {
    return <div className="chart-empty">Нет данных для отображения</div>;
  }

  const { xValues, yValues } = chartData;
  const traces = [];

  switch (coordinateSystem) {
    case 'polar': {
      // θ = x (в радианах), r = y
      const theta = xValues;
      const r = yValues;
      traces.push({
        type: 'scatterpolar',
        mode: 'lines+markers',
        r: r,
        theta: theta,
        name: 'f(θ)',
        line: { shape: 'linear' },
      });
      break;
    }

    case 'cylindrical': {
      // θ = x, r = |y|, z = y → x = r·cosθ, y = r·sinθ
      const theta = xValues;
      const r = yValues.map(Math.abs);
      const z = yValues;

      const x3d = theta.map((t, i) => r[i] * Math.cos(t));
      const y3d = theta.map((t, i) => r[i] * Math.sin(t));

      traces.push({
        type: 'scatter3d',
        mode: 'lines+markers',
        x: x3d,
        y: y3d,
        z: z,
        name: 'f(x) → цилиндр',
        line: { shape: 'linear' },
      });
      break;
    }

    case 'spherical': {
      // θ = x (азимут), ρ = |y| (радиус), φ = π/4 (полярный угол — фиксирован)
      const rho = yValues.map(Math.abs);
      const theta = xValues;
      const phi = Array(xValues.length).fill(Math.PI / 4);

      const x3d = rho.map((ρ, i) => ρ * Math.sin(phi[i]) * Math.cos(theta[i]));
      const y3d = rho.map((ρ, i) => ρ * Math.sin(phi[i]) * Math.sin(theta[i]));
      const z3d = rho.map((ρ, i) => ρ * Math.cos(phi[i]));

      traces.push({
        type: 'scatter3d',
        mode: 'lines+markers',
        x: x3d,
        y: y3d,
        z: z3d,
        name: 'f(x) → сфера',
        line: { shape: 'linear' },
      });
      break;
    }

    case 'cartesian':
    default: {
      traces.push({
        x: xValues,
        y: yValues,
        mode: 'lines+markers',
        type: 'scatter',
        name: 'f(x)',
        line: { shape: 'linear' },
      });
      break;
    }
  }

  const layout = {
    title: {
      text: `График в ${
        {
          cartesian: 'декартовой',
          polar: 'полярной',
          cylindrical: 'цилиндрической',
          spherical: 'сферической',
        }[coordinateSystem]
      } системе`,
      font: { size: 16, color: '#fff' },
    },
    paper_bgcolor: '#1e1e1e',
    plot_bgcolor: '#1e1e1e',
    font: { color: '#b0b0b0' },
    autosize: true,
    height: 400,
    margin: { l: 40, r: 40, t: 60, b: 60 },
  };

  if (coordinateSystem === 'polar') {
    layout.polar = {
      bgcolor: '#1a1a1a',
      radialaxis: { color: '#b0b0b0', gridcolor: '#333' },
      angularaxis: { color: '#b0b0b0', gridcolor: '#333', direction: 'counterclockwise' },
    };
  } else if (coordinateSystem === 'cylindrical' || coordinateSystem === 'spherical') {
    layout.scene = {
      bgcolor: '#1a1a1a',
      xaxis: { title: 'X', color: '#b0b0b0', gridcolor: '#333' },
      yaxis: { title: 'Y', color: '#b0b0b0', gridcolor: '#333' },
      zaxis: { title: 'Z', color: '#b0b0b0', gridcolor: '#333' },
      aspectmode: 'data',
    };
  } else {
    layout.xaxis = { title: 'X', color: '#b0b0b0', gridcolor: '#333' };
    layout.yaxis = { title: 'Y', color: '#b0b0b0', gridcolor: '#333' };
  }

  return (
    <div className="function-chart">
      <Plot
        data={traces}
        layout={layout}
        config={{ responsive: true, displaylogo: false }}
        style={{ width: '100%', height: '100%' }}
        useResizeHandler={true}
      />
    </div>
  );
};

export default FunctionChart2;