class FunctionChart {
  constructor(canvasId) {
    this.ctx = document.getElementById(canvasId)?.getContext('2d');
    if (!this.ctx) return;
    this.chart = null;
  }

  render(points, label = 'f(x)', options = {}) {
    if (!this.ctx) return;

    const data = {
      datasets: [{
        label,
         points.map(p => ({ x: p.x, y: p.y })),
        borderColor: '#64b5f6',
        backgroundColor: 'rgba(100, 181, 246, 0.1)',
        borderWidth: 2,
        pointRadius: points.length <= 50 ? 3 : 1,
        pointHoverRadius: 6,
        fill: true,
        tension: 0.3,
        spanGaps: false
      }]
    };

    const config = {
      type: 'line',
      data,
      options: {
        responsive: true,
        maintainAspectRatio: false,
        interaction: {
          intersect: false,
          mode: 'index'
        },
        scales: {
          x: {
            type: 'linear',
            position: 'bottom',
            grid: { color: 'rgba(255,255,255,0.05)' },
            ticks: { color: '#aaa' }
          },
          y: {
            grid: { color: 'rgba(255,255,255,0.05)' },
            ticks: { color: '#aaa' }
          }
        },
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: (ctx) => `${ctx.dataset.label}: ${ctx.parsed.y.toFixed(4)}`
            }
          },
          zoom: {
            zoom: {
              wheel: { enabled: true, speed: 0.1 },
              pinch: { enabled: true },
              mode: 'xy'
            },
            pan: {
              enabled: true,
              mode: 'xy',
              modifierKey: 'ctrl'
            }
          }
        }
      }
    };

    // Уничтожаем старый график
    if (this.chart) this.chart.destroy();

    // Регистрируем плагин zoom один раз
    if (!Chart.registered) {
      Chart.register(window.ChartZoom);
      Chart.registered = true;
    }

    this.chart = new Chart(this.ctx, config);
    return this.chart;
  }

  destroy() {
    if (this.chart) {
      this.chart.destroy();
      this.chart = null;
    }
  }
}

// Глобальная функция для удобства (как в graph.html)
function renderChart(canvasId, points, label) {
  const chart = new FunctionChart(canvasId);
  return chart.render(points, label);
}

// Авто-регистрация плагина zoom при первом использовании
if (typeof Chart !== 'undefined' && typeof ChartZoom !== 'undefined') {
  Chart.register(ChartZoom);
}

// Экспорт (для ES6)
if (typeof module !== 'undefined') module.exports = { FunctionChart, renderChart };