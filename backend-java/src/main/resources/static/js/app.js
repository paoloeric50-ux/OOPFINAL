// MotorPH Payroll System - Shared JavaScript

const API_BASE = '/api';

// Auth helpers
const Auth = {
  getToken: () => localStorage.getItem('token'),
  getUser: () => { try { return JSON.parse(localStorage.getItem('user')); } catch(e) { return null; } },
  setSession: (token, user) => {
    localStorage.setItem('token', token);
    localStorage.setItem('user', JSON.stringify(user));
  },
  clearSession: () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  },
  isLoggedIn: () => !!localStorage.getItem('token'),
  requireAuth: () => {
    if (!Auth.isLoggedIn()) { window.location.href = '/login'; return false; }
    return true;
  },
  redirectIfLoggedIn: () => {
    if (Auth.isLoggedIn()) { window.location.href = '/dashboard'; }
  }
};

// API helpers
const API = {
  request: async (method, path, body = null) => {
    const headers = { 'Content-Type': 'application/json' };
    const token = Auth.getToken();
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const opts = { method, headers };
    if (body) opts.body = JSON.stringify(body);

    const res = await fetch(API_BASE + path, opts);
    if (res.status === 401) {
      Auth.clearSession();
      window.location.href = '/login';
      throw new Error('Unauthorized');
    }
    const data = await res.json().catch(() => ({}));
    if (!res.ok) throw new Error(data.message || data.error || `HTTP ${res.status}`);
    return data;
  },
  get: (path) => API.request('GET', path),
  post: (path, body) => API.request('POST', path, body),
  put: (path, body) => API.request('PUT', path, body),
  delete: (path) => API.request('DELETE', path),
};

// Toast notifications
const Toast = {
  show: (message, type = 'success') => {
    let container = document.querySelector('.toast-container-custom');
    if (!container) {
      container = document.createElement('div');
      container.className = 'toast-container-custom';
      document.body.appendChild(container);
    }
    const toast = document.createElement('div');
    toast.className = `toast-custom toast-${type}`;
    const icon = type === 'success' ? '✓' : type === 'error' ? '✕' : 'ℹ';
    toast.innerHTML = `<div class="d-flex align-items-center gap-2"><span>${icon}</span><span>${message}</span></div>`;
    container.appendChild(toast);
    setTimeout(() => { toast.style.opacity = '0'; toast.style.transition = 'opacity 0.3s'; setTimeout(() => toast.remove(), 300); }, 3500);
  },
  success: (msg) => Toast.show(msg, 'success'),
  error: (msg) => Toast.show(msg, 'error'),
  info: (msg) => Toast.show(msg, 'info'),
};

// Sidebar user info
function initSidebar() {
  const user = Auth.getUser();
  if (!user) return;
  const nameEl = document.getElementById('sidebar-user-name');
  const emailEl = document.getElementById('sidebar-user-email');
  const avatarEl = document.getElementById('sidebar-user-avatar');
  if (nameEl) nameEl.textContent = user.fullName || user.email || 'User';
  if (emailEl) emailEl.textContent = user.email || '';
  if (avatarEl) avatarEl.textContent = (user.fullName || user.email || 'U')[0].toUpperCase();

  const logoutBtn = document.getElementById('logout-btn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      Auth.clearSession();
      window.location.href = '/login';
    });
  }

  // Set active nav link
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar-nav .nav-link').forEach(link => {
    const href = link.getAttribute('href');
    if (href && (path === href || (href !== '/' && path.startsWith(href)))) {
      link.classList.add('active');
    }
  });
}

// Format currency
function formatCurrency(amount) {
  return new Intl.NumberFormat('en-PH', { style: 'currency', currency: 'PHP' }).format(amount || 0);
}

// Format date
function formatDate(dateStr) {
  if (!dateStr) return '-';
  const d = new Date(dateStr);
  if (isNaN(d)) return dateStr;
  return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

// Format time
function formatTime(timeStr) {
  if (!timeStr) return '-';
  if (timeStr.includes('T')) {
    const d = new Date(timeStr);
    return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
  }
  return timeStr;
}

// Get employee type badge
function getTypeBadge(type) {
  const map = {
    'full-time': '<span class="badge badge-full-time px-2 py-1 rounded-pill">Full-Time</span>',
    'part-time': '<span class="badge badge-part-time px-2 py-1 rounded-pill">Part-Time</span>',
    'contract': '<span class="badge badge-contract px-2 py-1 rounded-pill">Contract</span>',
  };
  return map[type] || `<span class="badge bg-secondary">${type}</span>`;
}

// Get status badge
function getStatusBadge(status) {
  if (!status) return '';
  const s = status.toLowerCase();
  if (s === 'active') return '<span class="badge badge-active px-2 py-1 rounded-pill">Active</span>';
  return '<span class="badge badge-inactive px-2 py-1 rounded-pill">Inactive</span>';
}

// Get attendance status badge
function getAttendanceBadge(status) {
  if (!status) return '';
  const s = status.toLowerCase();
  if (s === 'present') return '<span class="badge badge-present px-2 py-1 rounded-pill">Present</span>';
  if (s === 'late') return '<span class="badge badge-late px-2 py-1 rounded-pill">Late</span>';
  return '<span class="badge badge-absent px-2 py-1 rounded-pill">Absent</span>';
}

// Generate initials avatar
function getInitials(name) {
  if (!name) return '?';
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
}

document.addEventListener('DOMContentLoaded', initSidebar);
