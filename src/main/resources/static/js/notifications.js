function showNotification(message, type = 'info') {
  const types = {
    info: 'alert-info',
    success: 'alert-success',
    warning: 'alert-warning',
    error: 'alert-danger'
  };

  const toast = document.createElement('div');
  toast.className = `alert ${types[type]} position-fixed bottom-3 end-3 m-3 p-3`;
  toast.style.zIndex = 1050;
  toast.style.minWidth = '300px';
  toast.innerHTML = `
    <div class="d-flex justify-content-between align-items-center">
      <span>${message}</span>
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  `;

  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 5000);
}

// Глобально
window.showNotification = showNotification;