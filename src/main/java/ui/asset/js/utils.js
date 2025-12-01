// Показать уведомление (всплывающее, исчезает через 4 сек)
function showNotification(message, type = 'info') {
  const colors = {
    success: 'bg-success',
    error: 'bg-danger',
    warning: 'bg-warning text-dark',
    info: 'bg-info text-dark'
  };
  const toast = `
    <div class="toast ${colors[type] || 'bg-primary'} text-white hide" role="alert" aria-live="assertive" aria-atomic="true">
      <div class="toast-header ${colors[type] || 'bg-primary'}">
        <strong class="me-auto">${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'}</strong>
        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast" aria-label="Close"></button>
      </div>
      <div class="toast-body">${message}</div>
    </div>`;

  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    container.className = 'toast-container position-fixed top-0 end-0 p-3';
    document.body.appendChild(container);
  }

  container.insertAdjacentHTML('beforeend', toast);
  const toastEl = container.lastElementChild;
  const bsToast = new bootstrap.Toast(toastEl, { delay: 4000 });
  bsToast.show();

  toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

function showSuccess(msg) { showNotification(msg, 'success'); }
function showError(err) {
  let msg = 'Произошла ошибка';
  if (err.response) {
    const { status, data } = err.response;
    switch (status) {
      case 400: msg = data?.message || 'Некорректные данные'; break;
      case 401: msg = 'Сессия истекла. Войдите заново.'; break;
      case 403: msg = 'Недостаточно прав'; break;
      case 404: msg = 'Ресурс не найден'; break;
      case 500: msg = `Сервер: ${data?.message || 'внутренняя ошибка'}`; break;
      default: msg = `HTTP ${status}`;
    }
  } else if (err.request) {
    msg = 'Нет связи с сервером';
  } else {
    msg = err.message || 'Неизвестная ошибка';
  }
  showNotification(msg, 'error');
}