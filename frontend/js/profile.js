/**
 * Controller for User Profile Management (profile.html)
 * Loads polymorphic user profile and handles updates via PUT /api/profile
 */

let userProfile = null;

document.addEventListener('DOMContentLoaded', async () => {
  const user = await requireAuth();
  if (!user) return;

  await loadProfile();
  setupProfileForm();
});

async function loadProfile() {
  const loadingEl = document.getElementById('loading');
  const containerEl = document.getElementById('profile-container');

  try {
    const res = await apiRequest('/profile');
    if (res && res.success && res.data) {
      userProfile = res.data;
      loadingEl.style.display = 'none';
      containerEl.style.display = 'block';
      renderProfile(userProfile);
    } else {
      throw new Error(res ? res.message : 'Failed to load profile');
    }
  } catch (err) {
    console.error('Error loading profile:', err);
    loadingEl.innerHTML = `
      <div style="font-size: 2.5rem; margin-bottom: 1rem;">⚠️</div>
      <h3 style="margin-bottom: 0.5rem;">Failed to Load Profile</h3>
      <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">${escapeHtml(err.message || 'Error occurred')}</p>
      <a href="index.html" class="btn btn-primary">Go to Home</a>
    `;
  }
}

function renderProfile(user) {
  const isFarmer = user.role === 'FARMER';
  const dashboardUrl = user.dashboardPath || (isFarmer ? 'farmer-dashboard.html' : 'buyer-dashboard.html');

  document.getElementById('role-badge').textContent = `${user.role} ACCOUNT`;
  document.getElementById('profile-title').textContent = user.fullName;
  document.getElementById('profile-summary-text').textContent = user.profileSummary || '';

  document.getElementById('profile-fullName').value = user.fullName || '';
  document.getElementById('profile-email').value = user.email || '';
  document.getElementById('profile-phone').value = user.phone || '';
  document.getElementById('profile-location').value = user.location || '';

  document.getElementById('cancel-profile-btn').href = dashboardUrl;
  const navDash = document.getElementById('nav-dashboard-link');
  if (navDash) {
    navDash.innerHTML = `<a href="${dashboardUrl}">Dashboard</a>`;
  }

  const roleFieldsEl = document.getElementById('role-fields');
  if (isFarmer) {
    roleFieldsEl.innerHTML = `
      <div class="grid-2">
        <div class="form-group">
          <label class="form-label" for="profile-farmName">Farm / Organization Name *</label>
          <input type="text" id="profile-farmName" class="form-control" value="${escapeHtml(user.farmName || '')}" required>
        </div>
        <div class="form-group">
          <label class="form-label" for="profile-village">Village / Taluk</label>
          <input type="text" id="profile-village" class="form-control" value="${escapeHtml(user.village || '')}">
        </div>
      </div>
      <div class="form-group">
        <label class="form-label" for="profile-district">District</label>
        <input type="text" id="profile-district" class="form-control" value="${escapeHtml(user.district || '')}">
      </div>
    `;
  } else {
    // Buyer fields
    roleFieldsEl.innerHTML = `
      <div class="grid-2">
        <div class="form-group">
          <label class="form-label" for="profile-businessName">Business Name *</label>
          <input type="text" id="profile-businessName" class="form-control" value="${escapeHtml(user.businessName || '')}" required>
        </div>
        <div class="form-group">
          <label class="form-label" for="profile-buyerType">Buyer Category</label>
          <select id="profile-buyerType" class="form-control">
            <option value="Wholesale Distributor" ${user.buyerType === 'Wholesale Distributor' ? 'selected' : ''}>Wholesale Distributor</option>
            <option value="Food Processor" ${user.buyerType === 'Food Processor' ? 'selected' : ''}>Food Processor</option>
            <option value="Retail Chain" ${user.buyerType === 'Retail Chain' ? 'selected' : ''}>Retail Chain</option>
            <option value="International Exporter" ${user.buyerType === 'International Exporter' ? 'selected' : ''}>International Exporter</option>
            <option value="Local Trader" ${user.buyerType === 'Local Trader' ? 'selected' : ''}>Local Trader</option>
          </select>
        </div>
      </div>
    `;
  }
}

function setupProfileForm() {
  const form = document.getElementById('profile-form');
  const submitBtn = document.getElementById('save-profile-btn');

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const fullName = document.getElementById('profile-fullName').value.trim();
      const phone = document.getElementById('profile-phone').value.trim();
      const location = document.getElementById('profile-location').value.trim();

      if (!fullName || !phone) {
        showToast('Please fill in required fields (Name and Phone)', 'error');
        return;
      }

      const isFarmer = userProfile.role === 'FARMER';
      let extra1 = '';
      let extra2 = '';
      let extra3 = '';

      if (isFarmer) {
        extra1 = document.getElementById('profile-farmName').value.trim();
        extra2 = document.getElementById('profile-village').value.trim();
        extra3 = document.getElementById('profile-district').value.trim();
        if (!extra1) {
          showToast('Farm name is required', 'error');
          return;
        }
      } else {
        extra1 = document.getElementById('profile-businessName').value.trim();
        extra2 = document.getElementById('profile-buyerType').value;
        if (!extra1) {
          showToast('Business name is required', 'error');
          return;
        }
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Saving...';

      try {
        const payload = {
          fullName,
          phone,
          location,
          extra1,
          extra2,
          extra3
        };

        const res = await apiRequest('/profile', {
          method: 'PUT',
          body: payload
        });

        if (res && res.success && res.data) {
          showToast('Profile updated successfully!', 'success');
          userProfile = res.data;
          renderProfile(userProfile);
        } else {
          showToast(res.message || 'Update failed', 'error');
        }
      } catch (err) {
        showToast(err.message || 'Update failed', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Save Changes';
      }
    });
  }
}
