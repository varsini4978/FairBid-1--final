/**
 * Controller for Buyer Dashboard (buyer-dashboard.html)
 */

document.addEventListener('DOMContentLoaded', async () => {
  const user = await requireAuth('BUYER');
  if (!user) return;

  const headingEl = document.getElementById('buyer-heading');
  const summaryEl = document.getElementById('buyer-profile-summary');
  if (headingEl) headingEl.textContent = `Welcome, ${user.fullName}`;
  if (summaryEl && user.profileSummary) summaryEl.textContent = user.profileSummary;

  await loadBuyerData();
});

async function loadBuyerData() {
  await Promise.all([loadBuyerSummary(), loadBuyerBids(), loadBuyerWon()]);
}

async function loadBuyerSummary() {
  try {
    const res = await apiRequest('/buyer/summary');
    if (res && res.success && res.data) {
      const d = res.data;
      document.getElementById('stat-total-bids').textContent = d.totalBidsPlaced || 0;
      document.getElementById('stat-winning-bids').textContent = d.winningActiveBids || 0;
      document.getElementById('stat-won-auctions').textContent = d.wonAuctionsCount || 0;
    }
  } catch (err) {
    console.error('Failed to load buyer summary', err);
  }
}

async function loadBuyerBids() {
  const tbody = document.getElementById('buyer-bids-tbody');
  if (!tbody) return;

  try {
    const res = await apiRequest('/buyer/bids');
    const bids = (res && res.success && res.data) ? res.data : [];

    if (bids.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="5" style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
            You haven't placed any bids yet.<br><br>
            <a href="browse-produce.html" class="btn btn-primary btn-sm">Explore Open Auctions</a>
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = '';
    bids.forEach(b => {
      const tr = document.createElement('tr');
      const bidAmount = formatCurrency(b.amountPerUnit);
      const placedAt = formatDateTime(b.placedAt);

      let badgeClass = 'badge-closed';
      if (b.status === 'WINNING') badgeClass = 'badge-open';
      else if (b.status === 'OUTBID') badgeClass = 'badge-unsold';

      tr.innerHTML = `
        <td>
          <div style="font-weight: 700;">Auction Round #${b.auctionId}</div>
          <div style="font-size: 0.8rem; color: var(--text-muted);">Bid ID #${b.id}</div>
        </td>
        <td style="font-size: 1.05rem; font-weight: 800; color: var(--primary-hover);">${bidAmount}</td>
        <td>${placedAt}</td>
        <td><span class="badge ${badgeClass}">${b.status}</span></td>
        <td>
          <a href="auction.html?id=${b.auctionId}" class="btn btn-secondary btn-sm">
            Enter Auction Room →
          </a>
        </td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" style="text-align: center; padding: 2rem; color: var(--status-unsold);">
          Failed to load bids: ${err.message}
        </td>
      </tr>
    `;
  }
}

async function loadBuyerWon() {
  const tbody = document.getElementById('buyer-won-tbody');
  if (!tbody) return;

  try {
    const res = await apiRequest('/buyer/won');
    const won = (res && res.success && res.data) ? res.data : [];

    if (won.length === 0) {
      tbody.innerHTML = `
        <tr>
          <td colspan="6" style="text-align: center; padding: 2rem; color: var(--text-muted);">
            No won auctions settled yet. Keep bidding in active auctions!
          </td>
        </tr>
      `;
      return;
    }

    tbody.innerHTML = '';
    won.forEach(w => {
      const tr = document.createElement('tr');
      const cropName = w.cropName || `Produce Lot`;
      const finalPrice = formatCurrency(w.finalPricePerUnit);
      const totalAmount = formatCurrency(w.totalAmount);
      const closedAt = formatDateTime(w.closedAt);

      tr.innerHTML = `
        <td>
          <div style="font-weight: 700;">${escapeHtml(cropName)}</div>
          <div style="font-size: 0.8rem; color: var(--text-muted);">Round ${w.roundNumber}</div>
        </td>
        <td><strong>${w.quantity}</strong> ${escapeHtml(w.unit)}</td>
        <td style="font-weight: 700;">${finalPrice} / ${escapeHtml(w.unit)}</td>
        <td style="font-size: 1.1rem; font-weight: 800; color: var(--primary);">${totalAmount}</td>
        <td>${closedAt}</td>
        <td>
          <a href="auction-result.html?id=${w.auctionId}" class="btn btn-secondary btn-sm">
            View Settlement
          </a>
        </td>
      `;
      tbody.appendChild(tr);
    });
  } catch (err) {
    tbody.innerHTML = `
      <tr>
        <td colspan="6" style="text-align: center; padding: 2rem; color: var(--status-unsold);">
          Failed to load won auctions: ${err.message}
        </td>
      </tr>
    `;
  }
}
