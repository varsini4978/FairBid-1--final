/**
 * Controller for Browse Produce Page (browse-produce.html)
 */

let selectedCategory = 'ALL';
let searchTimeout = null;

document.addEventListener('DOMContentLoaded', () => {
  setupFilters();
  loadBrowseItems();
});

function setupFilters() {
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('input', () => {
      clearTimeout(searchTimeout);
      searchTimeout = setTimeout(() => {
        loadBrowseItems();
      }, 300);
    });
  }

  const pills = document.querySelectorAll('.category-btn');
  pills.forEach(btn => {
    btn.addEventListener('click', () => {
      pills.forEach(b => {
        b.classList.remove('btn-primary');
        b.classList.add('btn-secondary');
      });
      btn.classList.remove('btn-secondary');
      btn.classList.add('btn-primary');
      selectedCategory = btn.getAttribute('data-category');
      loadBrowseItems();
    });
  });
}

async function loadBrowseItems() {
  const grid = document.getElementById('browse-grid');
  if (!grid) return;

  const searchVal = document.getElementById('search-input') ? document.getElementById('search-input').value.trim() : '';

  try {
    // Query both open auctions and all lots
    const query = new URLSearchParams();
    if (selectedCategory && selectedCategory !== 'ALL') query.append('category', selectedCategory);
    if (searchVal) query.append('search', searchVal);

    const [lotsRes, auctionsRes] = await Promise.all([
      apiRequest(`/produce?${query.toString()}`).catch(() => null),
      apiRequest(`/auctions?${query.toString()}`).catch(() => null)
    ]);

    const lots = (lotsRes && lotsRes.success && lotsRes.data) ? lotsRes.data : [];
    const auctions = (auctionsRes && auctionsRes.success && auctionsRes.data) ? auctionsRes.data : [];

    // Map open auctions by lotId
    const auctionMap = new Map();
    auctions.forEach(a => {
      auctionMap.set(a.lotId, a);
    });

    if (lots.length === 0) {
      grid.innerHTML = `
        <div style="grid-column: 1/-1; text-align: center; padding: 4rem 1rem;" class="card">
          <p style="font-size: 1.15rem; color: var(--text-secondary); margin-bottom: 0.5rem;">
            No produce lots match your search criteria.
          </p>
          <p style="color: var(--text-muted); font-size: 0.9rem;">
            Try clearing filters or searching for another crop.
          </p>
        </div>
      `;
      return;
    }

    grid.innerHTML = '';
    lots.forEach(lot => {
      const card = document.createElement('div');
      card.className = 'produce-card';

      const imgSrc = getCropImageSrc(lot.cropName);
      const minPrice = formatCurrency(lot.minPricePerUnit);
      const activeAuction = auctionMap.get(lot.id);

      let statusBadge = `<span class="badge badge-${lot.status}">${lot.status.replace(/_/g, ' ')}</span>`;
      let timeText = '';
      let highestBidText = 'No bids yet';
      let buttonHtml = '';

      if (activeAuction) {
        timeText = `<span class="countdown-box" style="font-size: 0.75rem; padding: 0.2rem 0.5rem;">⏳ ${formatTimeRemaining(activeAuction.timeRemainingSeconds)}</span>`;
        if (activeAuction.highestBid) {
          highestBidText = formatCurrency(activeAuction.highestBid.amountPerUnit);
        }
        buttonHtml = `
          <a href="auction.html?id=${activeAuction.id}" class="btn btn-primary" style="width: 100%;">
            Enter Auction Room →
          </a>
        `;
      } else if (lot.status === 'AVAILABLE') {
        buttonHtml = `
          <div style="text-align: center; color: var(--text-muted); font-size: 0.9rem; padding: 0.5rem 0;">
            Auction Pending Launch
          </div>
        `;
      } else {
        buttonHtml = `
          <div style="text-align: center; color: var(--text-muted); font-size: 0.9rem; padding: 0.5rem 0;">
            Round Closed (${lot.status.replace(/_/g, ' ')})
          </div>
        `;
      }

      card.innerHTML = `
        <div class="produce-image-wrap">
          <img src="${imgSrc}" alt="${escapeHtml(lot.cropName)}" class="produce-image">
        </div>
        <div class="produce-body">
          <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.5rem;">
            ${statusBadge}
            ${timeText}
          </div>
          <h3 class="produce-title">${escapeHtml(lot.cropName)}</h3>
          <div class="produce-meta">
            📍 ${escapeHtml(lot.farmerLocation || 'Local Farm')} • <strong>${lot.quantity}</strong> ${escapeHtml(lot.unit)}
          </div>
          <div class="produce-prices">
            <div class="price-item">
              <div class="label">Min. Price</div>
              <div class="val" style="font-size: 1rem; color: var(--text-secondary);">${minPrice}</div>
            </div>
            <div class="price-item" style="text-align: right;">
              <div class="label">${activeAuction ? 'Highest Bid' : 'Agent Offer'}</div>
              <div class="val">${activeAuction ? highestBidText : (lot.agentOfferPerUnit ? formatCurrency(lot.agentOfferPerUnit) : '₹0.00')}</div>
            </div>
          </div>
          ${buttonHtml}
        </div>
      `;
      grid.appendChild(card);
    });
  } catch (err) {
    grid.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 2rem; color: var(--status-unsold);">
        Failed to load marketplace listings: ${err.message}
      </div>
    `;
  }
}
