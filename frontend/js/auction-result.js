/**
 * Controller for Auction Result Page (auction-result.html)
 * Displays official settlement for SOLD / UNSOLD auctions.
 * Accepts ?id= or ?auctionId= or ?lotId= in URL.
 */

document.addEventListener('DOMContentLoaded', async () => {
  await checkAuth();

  const urlParams = new URLSearchParams(window.location.search);
  const auctionId = urlParams.get('id') || urlParams.get('auctionId');
  const lotId = urlParams.get('lotId');

  if (!auctionId && !lotId) {
    showError('No auction ID or lot ID specified in URL.');
    return;
  }

  await loadResult(auctionId, lotId);
});

async function loadResult(auctionId, lotId) {
  const loadingEl = document.getElementById('loading');
  const containerEl = document.getElementById('result-container');

  try {
    let endpoint = auctionId ? `/auctions/${auctionId}/result` : `/produce/${lotId}/result`;
    const res = await apiRequest(endpoint);

    if (res && res.success && res.data) {
      loadingEl.style.display = 'none';
      containerEl.style.display = 'block';
      renderResult(res.data);
    } else {
      showError(res ? res.message : 'No settlement result found.');
    }
  } catch (err) {
    console.error('Failed to load auction result:', err);
    showError(err.message || 'Auction may still be active or result is not yet available.');
  }
}

function renderResult(r) {
  const container = document.getElementById('result-container');
  const isSold = r.outcome === 'SOLD';
  const cropImg = getCropImageSrc(r.cropName);
  const isFarmer = currentUser && currentUser.role === 'FARMER';
  const backUrl = isFarmer ? 'farmer-dashboard.html' : 'buyer-dashboard.html';

  let bannerHtml = '';
  let detailsHtml = '';
  let actionsHtml = '';

  if (isSold) {
    bannerHtml = `
      <div class="card" style="border-left: 5px solid var(--accent); background: linear-gradient(to right, #F0FDF4, #FFFFFF); margin-bottom: 1.5rem; padding: 1.75rem;">
        <div style="display: flex; align-items: center; gap: 1rem;">
          <span style="font-size: 2.5rem;">🏆</span>
          <div>
            <span class="badge badge-open" style="margin-bottom: 0.25rem;">Settlement Complete</span>
            <h1 style="font-size: 1.6rem; font-weight: 800; color: var(--primary);">Auction Closed – SOLD!</h1>
            <p style="color: var(--text-secondary); margin: 0;">Produce lot successfully acquired through competitive transparent bidding.</p>
          </div>
        </div>
      </div>
    `;

    detailsHtml = `
      <div class="card" style="margin-bottom: 1.5rem;">
        <h3 style="margin-bottom: 1rem; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem;">Winning Transaction</h3>
        <table class="table" style="width: 100%;">
          <tbody>
            <tr>
              <td style="color: var(--text-secondary); width: 40%;">Winning Buyer</td>
              <td><strong>${escapeHtml(r.winnerName || 'Verified Buyer')}</strong> ${r.winnerBusiness ? `(${escapeHtml(r.winnerBusiness)})` : ''}</td>
            </tr>
            <tr>
              <td style="color: var(--text-secondary);">Winning Price Per Unit</td>
              <td style="font-size: 1.25rem; font-weight: 800; color: var(--primary);">${formatCurrency(r.finalPricePerUnit)} / ${escapeHtml(r.unit || '')}</td>
            </tr>
            <tr>
              <td style="color: var(--text-secondary);">Total Settlement Amount</td>
              <td style="font-size: 1.35rem; font-weight: 800; color: var(--accent);">${formatCurrency(r.totalAmount)}</td>
            </tr>
            <tr>
              <td style="color: var(--text-secondary);">Auction Closed At</td>
              <td>${formatDateTime(r.closedAt)}</td>
            </tr>
            <tr>
              <td style="color: var(--text-secondary);">Auction Round</td>
              <td>Round ${r.roundNumber || 1}</td>
            </tr>
          </tbody>
        </table>
      </div>
    `;

    actionsHtml = `
      <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
        <a href="${backUrl}" class="btn btn-primary">Return to Dashboard</a>
        <a href="browse-produce.html" class="btn btn-secondary">Browse Other Produce</a>
      </div>
    `;
  } else {
    // UNSOLD
    bannerHtml = `
      <div class="card" style="border-left: 5px solid #F59E0B; background: linear-gradient(to right, #FFFBEB, #FFFFFF); margin-bottom: 1.5rem; padding: 1.75rem;">
        <div style="display: flex; align-items: center; gap: 1rem;">
          <span style="font-size: 2.5rem;">📦</span>
          <div>
            <span class="badge badge-unsold" style="margin-bottom: 0.25rem;">Auction Closed</span>
            <h1 style="font-size: 1.6rem; font-weight: 800; color: #B45309;">Auction Closed – UNSOLD</h1>
            <p style="color: var(--text-secondary); margin: 0;">No bids reached your minimum reserve price of ${formatCurrency(r.minPriceSnapshot)} / ${escapeHtml(r.unit || '')}.</p>
          </div>
        </div>
      </div>
    `;

    detailsHtml = `
      <div class="card" style="margin-bottom: 1.5rem;">
        <h3 style="margin-bottom: 1rem; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem;">Auction Outcome Summary</h3>
        <table class="table" style="width: 100%;">
          <tbody>
            <tr>
              <td style="color: var(--text-secondary); width: 40%;">Reserve Minimum Price</td>
              <td><strong>${formatCurrency(r.minPriceSnapshot)} / ${escapeHtml(r.unit || '')}</strong></td>
            </tr>
            <tr>
              <td style="color: var(--text-secondary);">Total Bids Placed</td>
              <td>${r.totalBids || 0}</td>
            </tr>
            ${r.agentOfferPerUnit && parseFloat(r.agentOfferPerUnit) > 0 ? `
            <tr>
              <td style="color: var(--text-secondary);">Offline Local Agent Offer</td>
              <td><strong>${formatCurrency(r.agentOfferPerUnit)} / ${escapeHtml(r.unit || '')}</strong></td>
            </tr>
            ` : ''}
            <tr>
              <td style="color: var(--text-secondary);">Auction Closed At</td>
              <td>${formatDateTime(r.closedAt)}</td>
            </tr>
          </tbody>
        </table>
      </div>
    `;

    const lotIdForRecovery = r.lotId;
    actionsHtml = `
      <div class="card" style="background: #F8FAFC; border: 1px dashed var(--border); margin-bottom: 1.5rem;">
        <h4 style="margin-bottom: 0.5rem;">Producer Decision Required</h4>
        <p style="font-size: 0.9rem; color: var(--text-secondary); margin-bottom: 1rem;">
          As the produce owner, you maintain complete autonomy. FairBid gives you 3 options:
          reopen with the same reserve, revise your minimum price, or compare and decide on local agent offers.
        </p>
        <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
          ${lotIdForRecovery ? `
          <a href="unsold-recovery.html?lotId=${lotIdForRecovery}" class="btn btn-primary">
            🔄 Unsold Recovery Options
          </a>
          <a href="agent-comparison.html?lotId=${lotIdForRecovery}" class="btn btn-secondary">
            📊 Compare with Agent Offer
          </a>
          ` : ''}
          <a href="${backUrl}" class="btn btn-outline">Back to Dashboard</a>
        </div>
      </div>
    `;
  }

  container.innerHTML = `
    <!-- Lot Header Card -->
    <div class="card" style="margin-bottom: 1.5rem; display: flex; gap: 1.25rem; align-items: center;">
      <img src="${cropImg}" alt="${escapeHtml(r.cropName || '')}" style="width: 64px; height: 64px; border-radius: 8px; object-fit: cover;">
      <div>
        <h2 style="font-size: 1.4rem; font-weight: 800; margin-bottom: 0.25rem;">${escapeHtml(r.cropName || 'Produce Lot')}</h2>
        <p style="color: var(--text-secondary); margin: 0; font-size: 0.95rem;">
          Quantity: <strong>${r.quantity} ${escapeHtml(r.unit || '')}</strong>
          ${r.minPriceSnapshot ? ` • Reserve: <strong>${formatCurrency(r.minPriceSnapshot)}/${escapeHtml(r.unit || '')}</strong>` : ''}
        </p>
      </div>
    </div>

    ${bannerHtml}
    ${detailsHtml}
    ${actionsHtml}
  `;
}

function showError(msg) {
  const loadingEl = document.getElementById('loading');
  const containerEl = document.getElementById('result-container');
  loadingEl.style.display = 'none';
  containerEl.style.display = 'block';
  containerEl.innerHTML = `
    <div class="card" style="text-align: center; padding: 2.5rem;">
      <div style="font-size: 2.5rem; margin-bottom: 1rem;">⚠️</div>
      <h2 style="margin-bottom: 0.5rem;">Unable to Display Settlement</h2>
      <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">${escapeHtml(msg)}</p>
      <a href="index.html" class="btn btn-primary">Go to Home</a>
    </div>
  `;
}
