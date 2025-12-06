// Сохранение темы в localStorage
function toggleTheme() {
  const body = document.body;
  const isDark = body.classList.contains('theme-dark');
  body.classList.toggle('theme-dark', !isDark);
  body.classList.toggle('theme-light', isDark);

  const themeLink = document.getElementById('theme-stylesheet');
  themeLink.href = isDark
    ? '/css/themes/light.css'
    : '/css/themes/dark.css';

  const themeIcon = document.getElementById('themeIcon');
  if (isDark) {
    themeIcon.className = 'bi bi-sun-fill';
  } else {
    themeIcon.className = 'bi bi-moon-fill';
  }

  localStorage.setItem('theme', isDark ? 'light' : 'dark');
}

// При загрузке — восстановить тему
document.addEventListener('DOMContentLoaded', () => {
  const savedTheme = localStorage.getItem('theme') || 'dark';
  const body = document.body;
  const themeLink = document.getElementById('theme-stylesheet');
  const themeIcon = document.getElementById('themeIcon');

  if (savedTheme === 'light') {
    body.classList.remove('theme-dark');
    body.classList.add('theme-light');
    themeLink.href = '/css/themes/light.css';
    themeIcon.className = 'bi bi-sun-fill';
  } else {
    body.classList.add('theme-dark');
    body.classList.remove('theme-light');
    themeLink.href = '/css/themes/dark.css';
    themeIcon.className = 'bi bi-moon-fill';
  }
});