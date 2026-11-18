/**
 * Controller for Registration Pages (farmer-register.html & buyer-register.html)
 */

document.addEventListener('DOMContentLoaded', () => {
  const farmerForm = document.getElementById('farmer-register-form');
  const buyerForm = document.getElementById('buyer-register-form');
  const submitBtn = document.getElementById('reg-submit');

  const handleRegister = async (role) => {
    const fullName = document.getElementById('fullName').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const phone = document.getElementById('phone').value.trim();
    const location = document.getElementById('location').value.trim();

    if (!fullName || !email || !password || !phone) {
      showToast('Please fill in all required fields marked with *', 'error');
      return;
    }

    if (password.length < 8) {
      showToast('Password must be at least 8 characters long', 'error');
      return;
    }

    const payload = {
      role,
      fullName,
      email,
      password,
      phone,
      location
    };

    if (role === 'FARMER') {
      payload.farmName = document.getElementById('farmName').value.trim();
      payload.village = document.getElementById('village').value.trim();
      payload.district = document.getElementById('district').value.trim();
      if (!payload.farmName) {
        showToast('Farm name is required', 'error');
        return;
      }
    } else {
      payload.businessName = document.getElementById('businessName').value.trim();
      payload.buyerType = document.getElementById('buyerType').value;
      if (!payload.businessName) {
        showToast('Business name is required', 'error');
        return;
      }
    }

    submitBtn.disabled = true;
    submitBtn.textContent = 'Creating account...';

    try {
      const res = await apiRequest('/auth/register', {
        method: 'POST',
        body: payload
      });

      if (res && res.success) {
        showToast('Account created successfully! Please log in.', 'success');
        const loginPage = role === 'FARMER' ? 'farmer-login.html' : 'buyer-login.html';
        setTimeout(() => {
          window.location.href = loginPage;
        }, 1200);
      } else {
        showToast(res.message || 'Registration failed', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Create Account';
      }
    } catch (err) {
      showToast(err.message || 'Registration failed', 'error');
      submitBtn.disabled = false;
      submitBtn.textContent = 'Create Account';
    }
  };

  if (farmerForm) {
    farmerForm.addEventListener('submit', (e) => {
      e.preventDefault();
      handleRegister('FARMER');
    });
  }

  if (buyerForm) {
    buyerForm.addEventListener('submit', (e) => {
      e.preventDefault();
      handleRegister('BUYER');
    });
  }
});
