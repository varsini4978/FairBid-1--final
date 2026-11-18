/**
 * Controller for Farmer Dashboard (farmer-dashboard.html)
 */

document.addEventListener('DOMContentLoaded', async () => {
  const user = await requireAuth('FARMER');
  if (!user) return;

  const headingEl = document.getElementById('welcome-heading');
  const summaryEl = document.getElementById('farmer-profile-summary');
  if (headingEl) headingEl.textContent = `Welcome, ${user.fullName}`;
  if (summaryEl && user.profileSummary) summaryEl.textContent = user.profileSummary;

  await loadFarmerData();

  // Attach start auction form listener
  const form = document.getElementById('start-auction-form');
  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const lotId = document.getElementById('modal-lot-id').value;
      const endTime = document.getElementById('auction-end-time').value;

      if (!endTime) {
        showToast('Please select a closing date and time', 'error');
        return;
      }

      const submitBtn = document.getElementById('start-auction-submit');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Launching...';

      try {
        const res = await apiRequest(`/produce/${lotId}/start-auction`, {
          method: 'POST',
          body: { endTime }
        });

        if (res && res.success) {
          showToast('Live auction launched successfully!', 'success');
          closeStartAuctionModal();
          await loadFarmerData();
        } else {
          showToast(res.message || 'Failed to start auction', 'error');
        }
      } catch (err) {
        showToast(err.message || 'Failed to start auction', 'error');
      } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Launch Auction';
      }
    });
  }
});

async function loadFarmerData() {
  await Promise.all([loadFarmerSummary(), loadFarmerLots()]);
}

async function loadFarmerSummary() {
  try {
    const res = await apiRequest('/farmer/summary');
    if (res && res.success && res.data) {
      const d = res.data;
      document.getElementById('stat-total-lots').textContent = d.totalLots || 0;
      document.getElementById('stat-open-auctions').textContent = d.openAuctions || 0;
      document.getElementById('stat-sold-lots').textContent = d.soldLots || 0;
      document.getElementById('stat-unsold-lots').textContent = d.unsoldLots || 0;
      document.getElementById('stat-earnings').textContent = formatCurrency(d.totalEarnings || 0);
    }
  } catch (err) {
    console.error('Failed to load farmer summary', err);
  }
}

async function loadFarmerLots() {
  const tbody = document.getElementById('farmer-lots-tbody');
  if (!tbody) return;

  try {
    const res = await apiRequest('/produce/mine');
    const lots = (res && res.success && res.data) ? res.data : [];

    if (lots.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="7" style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
            You haven't listed any produce lots yet.<br><br>
            <a href="add-produce.html" class="btn btn-primary btn-sm">+ Add Your First Produce Lot</a>
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = '';
    lots.forEach(lot => {
      const tr = document.createElement('tr');
      const imgSrc = getCropImageSrc(lot.cropName);
      const minPrice = formatCurrency(lot.minPricePerUnit);
      const agentPrice = lot.agentOfferPerUnit ? formatCurrency(lot.agentOfferPerUnit) : '₹0.00';
      const highestBid = lot.currentHighestBid ? formatCurrency(lot.currentHighestBid) : (lot.status === 'IN_AUCTION' ? 'No bids yet' : '-');

      let actionHtml = '';
      if (lot.status === 'AVAILABLE') {
        actionHtml = `
          <button class="btn btn-primary btn-sm" onclick="openStartAuctionModal(${lot.id}, '${escapeHtml(lot.cropName)}')">
            Start Auction
          </button>
        `;
      } else if (lot.status === 'IN_AUCTION') {
        actionHtml = `
          <div style="display: flex; gap: 0.4rem; flex-wrap: wrap;">
            <a href="auction.html?id=${lot.activeAuctionId}" class="btn btn-secondary btn-sm">Enter Room</a>
            <button class="btn btn-danger btn-sm" onclick="closeAuctionEarly(${lot.activeAuctionId}, '${escapeHtml(lot.cropName)}')">Close Early</button>
          </div>
        `;
      } else if (lot.status === 'UNSOLD') {
        actionHtml = `
          <a href="unsold-recovery.html?lotId=${lot.id}" class="btn btn-accent btn-sm">
            Recovery Options →
          </a>
        `;
      } else if (lot.status === 'SOLD') {
        actionHtml = `
          <a href="auction-result.html?lotId=${lot.id}" class="btn btn-secondary btn-sm">
            View Settlement
          </a>
        `;
      } else if (lot.status === 'SOLD_TO_AGENT') {
        actionHtml = `
          <span style="font-size: 0.85rem; color: var(--status-agent); font-weight: 600;">
            ✓ Sold to Local Agent
          </span>
        `;
      }

      tr.innerHTML = `
        <td>
          <div style="display: flex; align-items: center; gap: 0.75rem;">
            <img src="${imgSrc}" alt="${escapeHtml(lot.cropName)}" style="width: 38px; height: 38px; object-fit: contain;">
            <div>
              <div style="font-weight: 700;">${escapeHtml(lot.cropName)}</div>
              <div style="font-size: 0.8rem; color: var(--text-muted);">${escapeHtml(lot.category)}</div>
            </div>
          </div>
        </td>
        <td><strong>${lot.quantity}</strong> ${escapeHtml(lot.unit)}</td>
        <td>${minPrice} / ${escapeHtml(lot.unit)}</td>
        <td style="color: var(--text-secondary);">${agentPrice} / ${escapeHtml(lot.unit)}</td>
        <td><span class="badge badge-${lot.status}">${lot.status.replace(/_/g, ' ')}</span></td>
        <td style="font-weight: 700; color: var(--primary-hover);">${highestBid}</td>
        <td>${actionHtml}</td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="7" style="text-align: center; padding: 2rem; color: var(--status-unsold);">
          Failed to load produce lots: ${err.message}
        </td>
      </tr>
    `;
  }
}

function openStartAuctionModal(lotId, cropName) {
  const modal = document.getElementById('start-auction-modal');
  const lotIdInput = document.getElementById('modal-lot-id');
  const lotDesc = document.getElementById('modal-lot-desc');
  const timeInput = document.getElementById('auction-end-time');

  lotIdInput.value = lotId;
  lotDesc.textContent = `Launch live competitive bidding round for ${cropName}.`;

  // Default to 24 hours from now
  const now = new Date();
  now.setHours(now.getHours() + 24);
  const formattedDate = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
  timeInput.value = formattedDate;

  modal.classList.add('active');
}

function closeStartAuctionModal() {
  const modal = document.getElementById('start-auction-modal');
  if (modal) modal.classList.remove('active');
}

function closeAuctionEarly(auctionId, cropName) {
  confirmDialog(
    'Close Auction Early?',
    `Are you sure you want to close bidding for ${cropName} early? The current highest valid bid will win immediately, or the lot will be marked UNSOLD if no valid bids were received.`,
    async () => {
      try {
        const res = await apiRequest(`/auctions/${auctionId}/close`, { method: 'POST' });
        if (res && res.success) {
          showToast('Auction closed successfully!', 'success');
          await loadFarmerData();
        } else {
          showToast(res.message || 'Failed to close auction', 'error');
        }
      } catch (err) {
        showToast(err.message || 'Failed to close auction', 'error');
      }
    }
  );
}
