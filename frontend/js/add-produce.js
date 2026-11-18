/**
 * Controller for Add Produce Lot (add-produce.html)
 */

document.addEventListener('DOMContentLoaded', async () => {
  const user = await requireAuth('FARMER');
  if (!user) return;

  const launchCheckbox = document.getElementById('launch-immediately');
  const closingWrap = document.getElementById('closing-time-wrap');
  const closingInput = document.getElementById('closingTime');

  // Set default closing time to 24 hours from now
  const now = new Date();
  now.setHours(now.getHours() + 24);
  const formattedDefault = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  if (closingInput) closingInput.value = formattedDefault;

  if (launchCheckbox && closingWrap) {
    launchCheckbox.addEventListener('change', () => {
      closingWrap.style.display = launchCheckbox.checked ? 'block' : 'none';
      if (closingInput) closingInput.required = launchCheckbox.checked;
    });
  }

  const form = document.getElementById('add-produce-form');
  const submitBtn = document.getElementById('submit-btn');

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const cropName = document.getElementById('cropName').value.trim();
      const category = document.getElementById('category').value;
      const quantity = parseFloat(document.getElementById('quantity').value);
      const unit = document.getElementById('unit').value;
      const minPricePerUnit = parseFloat(document.getElementById('minPricePerUnit').value);
      const agentVal = document.getElementById('agentOfferPerUnit').value;
      const agentOfferPerUnit = agentVal ? parseFloat(agentVal) : 0;

      if (!cropName || isNaN(quantity) || quantity <= 0 || isNaN(minPricePerUnit) || minPricePerUnit <= 0) {
        showToast('Please fill in valid produce details and prices', 'error');
        return;
      }

      const payload = {
        cropName,
        category,
        quantity,
        unit,
        minPricePerUnit,
        agentOfferPerUnit
      };

      if (launchCheckbox && launchCheckbox.checked) {
        if (!closingInput.value) {
          showToast('Please specify an auction closing date and time', 'error');
          return;
        }
        payload.closingTime = closingInput.value;
      }

      submitBtn.disabled = true;
      submitBtn.textContent = 'Listing produce...';

      try {
        const res = await apiRequest('/produce', {
          method: 'POST',
          body: payload
        });

        if (res && res.success) {
          showToast('Produce lot successfully listed!', 'success');
          setTimeout(() => {
            window.location.href = 'farmer-dashboard.html';
          }, 800);
        } else {
          showToast(res.message || 'Failed to list produce', 'error');
          submitBtn.disabled = false;
          submitBtn.textContent = 'List Produce Lot';
        }
      } catch (err) {
        showToast(err.message || 'Failed to list produce', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'List Produce Lot';
      }
    });
  }
});
