/**
 * Controller for Login Pages (farmer-login.html & buyer-login.html)
 */

function fillCreds(email, password) {
  const emailInput = document.getElementById('email');
  const pwdInput = document.getElementById('password');
  if (emailInput && pwdInput) {
    emailInput.value = email;
    pwdInput.value = password;
  }
}

document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('login-form');
  const submitBtn = document.getElementById('login-submit');

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = document.getElementById('email').value.trim();
      const password = document.getElementById('password').value;

      if (!email || !password) {
        showToast('Please enter both email and password', 'error');
        return;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Signing in...';

      try {
        const res = await apiRequest('/auth/login', {
          method: 'POST',
          body: { email, password }
        });

        if (res && res.success && res.data) {
          showToast('Sign in successful! Redirecting...', 'success');
          // Dynamic method dispatch on user dashboard path
          const dashboardUrl = res.data.dashboardPath || 'index.html';
          setTimeout(() => {
            window.location.href = dashboardUrl;
          }, 600);
        } else {
          showToast(res.message || 'Login failed', 'error');
          submitBtn.disabled = false;
          submitBtn.textContent = 'Sign In';
        }
      } catch (err) {
        showToast(err.message || 'Invalid email or password', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Sign In';
      }
    });
  }
});
