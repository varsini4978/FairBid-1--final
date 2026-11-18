/**
 * FairBid Authentication & Navigation State Manager
 */

let currentUser = null;

async function checkAuth() {
  try {
    const res = await apiRequest('/auth/me');
    if (res && res.success && res.data) {
      currentUser = res.data;
      updateNavigation(currentUser);
      return currentUser;
    }
  } catch (e) {
    currentUser = null;
    updateNavigation(null);
  }
  return null;
}

function updateNavigation(user) {
  const navContainer = document.getElementById('nav-user-slot');
  if (!navContainer) return;

  if (user) {
    const isFarmer = user.role === 'FARMER';
    const dashboardUrl = isFarmer ? 'farmer-dashboard.html' : 'buyer-dashboard.html';

    navContainer.innerHTML = `
      <span class="user-badge">
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
        ${escapeHtml(user.fullName)} (${user.role})
      </span>
      <a href="${dashboardUrl}">Dashboard</a>
      <a href="profile.html">Profile</a>
      <button class="btn btn-secondary btn-sm" onclick="logout()">Logout</button>
    `;
  } else {
    navContainer.innerHTML = `
      <a href="farmer-login.html">Farmer Portal</a>
      <a href="buyer-login.html">Buyer Portal</a>
      <a href="farmer-register.html" class="btn btn-primary btn-sm">Join as Farmer</a>
    `;
  }
}

async function logout() {
  try {
    await apiRequest('/auth/logout', { method: 'POST' });
    showToast('Logged out successfully', 'info');
    setTimeout(() => {
      window.location.href = 'index.html';
    }, 500);
  } catch (err) {
    window.location.href = 'index.html';
  }
}

async function requireAuth(expectedRole = null) {
  const user = await checkAuth();
  if (!user) {
    const isFarmerExpected = expectedRole === 'FARMER';
    window.location.href = isFarmerExpected ? 'farmer-login.html' : 'buyer-login.html';
    return null;
  }

  if (expectedRole && user.role !== expectedRole) {
    showToast(`Access restricted to ${expectedRole} accounts. Redirecting...`, 'warning');
    setTimeout(() => {
      window.location.href = user.dashboardPath || 'index.html';
    }, 1000);
    return null;
  }

  return user;
}

document.addEventListener('DOMContentLoaded', () => {
  checkAuth();
});
