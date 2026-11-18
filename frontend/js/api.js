/**
 * FairBid API Client & Utility Functions
 * Single-origin relative path handling, unified error handling, and XSS-safe rendering.
 */

// Dynamically compute the API base path based on current context
const getApiBase = () => {
  const path = window.location.pathname;
  if (path.startsWith('/FairBid')) {
    return '/FairBid/api';
  }
  return '/api';
};

const API_BASE = getApiBase();

/**
 * Fetch wrapper for FairBid REST API.
 */
async function apiRequest(endpoint, options = {}) {
  const url = `${API_BASE}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;
  const defaultHeaders = {
    'Accept': 'application/json',
  };

  if (options.body && typeof options.body === 'object' && !(options.body instanceof FormData)) {
    defaultHeaders['Content-Type'] = 'application/json';
    options.body = JSON.stringify(options.body);
  }

  const config = {
    ...options,
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
    credentials: 'include', // Ensure session cookies are sent
  };

  try {
    const response = await fetch(url, config);
    const data = await response.json().catch(() => null);

    if (!response.ok) {
      if (response.status === 401 && !url.includes('/api/auth/login') && !url.includes('/api/auth/register')) {
        // Redirect to login if unauthenticated on a protected page
        const isFarmerPage = window.location.pathname.includes('farmer');
        window.location.href = isFarmerPage ? 'farmer-login.html' : 'buyer-login.html';
        throw new Error('Authentication required');
      }

      const errorMessage = data && data.message ? data.message : `Request failed with status ${response.status}`;
      const error = new Error(errorMessage);
      error.status = response.status;
      error.data = data;
      throw error;
    }

    return data;
  } catch (err) {
    console.error('API Error:', err);
    throw err;
  }
}

// -----------------------------------------------------------------------------
// Toast Notifications
// -----------------------------------------------------------------------------
function showToast(message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;

  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateY(10px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// -----------------------------------------------------------------------------
// Confirmation Dialog
// -----------------------------------------------------------------------------
function confirmDialog(title, message, onConfirm) {
  let modal = document.getElementById('global-modal');
  if (!modal) {
    modal = document.createElement('div');
    modal.id = 'global-modal';
    modal.className = 'modal-overlay';
    modal.innerHTML = `
      <div class="modal-content">
        <h3 id="modal-title" style="margin-bottom: 0.75rem;"></h3>
        <p id="modal-message" style="color: var(--text-secondary); margin-bottom: 1.5rem;"></p>
        <div style="display: flex; justify-content: flex-end; gap: 0.75rem;">
          <button class="btn btn-secondary" id="modal-cancel">Cancel</button>
          <button class="btn btn-primary" id="modal-confirm">Confirm</button>
        </div>
      </div>
    `;
    document.body.appendChild(modal);
  }

  const titleEl = modal.querySelector('#modal-title');
  const msgEl = modal.querySelector('#modal-message');
  const cancelBtn = modal.querySelector('#modal-cancel');
  const confirmBtn = modal.querySelector('#modal-confirm');

  titleEl.textContent = title;
  msgEl.textContent = message;

  modal.classList.add('active');

  const cleanup = () => {
    modal.classList.remove('active');
    cancelBtn.onclick = null;
    confirmBtn.onclick = null;
  };

  cancelBtn.onclick = cleanup;
  confirmBtn.onclick = () => {
    cleanup();
    if (typeof onConfirm === 'function') onConfirm();
  };
}

// -----------------------------------------------------------------------------
// Formatting Helpers
// -----------------------------------------------------------------------------
function formatCurrency(amount) {
  if (amount == null) return '₹0.00';
  const num = typeof amount === 'number' ? amount : parseFloat(amount);
  return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDateTime(dateStr) {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  return d.toLocaleString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

function formatTimeRemaining(seconds) {
  if (seconds <= 0) return 'Closed';
  const d = Math.floor(seconds / 86400);
  const h = Math.floor((seconds % 86400) / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = Math.floor(seconds % 60);

  if (d > 0) return `${d}d ${h}h remaining`;
  if (h > 0) return `${h}h ${m}m remaining`;
  return `${m}m ${s}s remaining`;
}

// -----------------------------------------------------------------------------
// Safe Image Resolver
// -----------------------------------------------------------------------------
function getCropImageSrc(cropName) {
  if (!cropName) return 'assets/images/default-crop.svg';
  const name = cropName.toLowerCase();
  if (name.includes('tomato')) return 'assets/images/tomatoes.svg';
  if (name.includes('mango')) return 'assets/images/mango.svg';
  if (name.includes('rice')) return 'assets/images/rice.svg';
  if (name.includes('onion')) return 'assets/images/onion.svg';
  if (name.includes('cardamom')) return 'assets/images/cardamom.svg';
  if (name.includes('wheat')) return 'assets/images/wheat.svg';
  if (name.includes('potato')) return 'assets/images/potato.svg';
  if (name.includes('apple')) return 'assets/images/apple.svg';
  return 'assets/images/default-crop.svg';
}

// -----------------------------------------------------------------------------
// XSS-Safe Escaper
// -----------------------------------------------------------------------------
function escapeHtml(str) {
  if (!str) return '';
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}
