/**
 * Controller for Landing Page (index.html)
 */

document.addEventListener('DOMContentLoaded', async () => {
  await loadFeaturedAuctions();
});

async function loadFeaturedAuctions() {
  const container = document.getElementById('featured-auctions-grid');
  if (!container) return;

  try {
    const res = await apiRequest('/auctions');
    const auctions = (res && res.success && res.data) ? res.data : [];

    if (auctions.length === 0) {
      container.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 3rem 1rem;" class="card">
          <p style="font-size: 1.1rem; color: var(--text-secondary); margin-bottom: 1rem;">
            No active auctions at this moment.
          </p>
          <a href="browse-produce.html" class="btn btn-secondary">Browse Available Lots</a>
        </div>
      `;
      return;
    }

    container.innerHTML = '';
    // Display up to 6 featured auctions
    auctions.slice(0, 6).forEach(auction => {
      const lot = auction.produceLot || {};
      const cropName = lot.cropName || 'Agricultural Produce';
      const imgSrc = getCropImageSrc(cropName);
      const minPrice = formatCurrency(auction.minPriceSnapshot);
      const currentHighest = auction.highestBid ? formatCurrency(auction.highestBid.amountPerUnit) : 'No bids yet';
      const countdownText = formatTimeRemaining(auction.timeRemainingSeconds);

      const card = document.createElement('div');
      card.className = 'produce-card';
      card.innerHTML = `
        <div class="produce-image-wrap">
          <img src="${imgSrc}" alt="${escapeHtml(cropName)}" class="produce-image">
        </div>
        <div class="produce-body">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
            <span class="badge badge-open">Round ${auction.roundNumber}</span>
            <span class="countdown-box" style="font-size: 0.75rem; padding: 0.2rem 0.5rem;">
              ⏳ ${escapeHtml(countdownText)}
            </span>
          </div>
          <h3 class="produce-title">${escapeHtml(cropName)}</h3>
          <div class="produce-meta">
            📍 ${escapeHtml(lot.farmerLocation || 'Local Farm')} • ${lot.quantity} ${escapeHtml(lot.unit)}
          </div>
          <div class="produce-prices">
            <div class="price-item">
              <div class="label">Minimum Reserve</div>
              <div class="val" style="font-size: 1rem; color: var(--text-secondary);">${minPrice}</div>
            </div>
            <div class="price-item" style="text-align: right;">
              <div class="label">Highest Bid</div>
              <div class="val">${currentHighest}</div>
            </div>
          </div>
          <a href="auction.html?id=${auction.id}" class="btn btn-primary" style="width: 100%;">
            Enter Auction Room
          </a>
        </div>
      `;
      container.appendChild(card);
    });
  } catch (err) {
    container.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--status-unsold);">
        Failed to load featured auctions. Please check backend connection.
      </div>
    `;
  }
}
