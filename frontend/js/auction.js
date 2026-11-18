/**
 * Controller for Live Auction Room (auction.html)
 */

let auctionId = null;
let currentAuction = null;
let pollInterval = null;
let countdownTimer = null;
let remainingSeconds = 0;

document.addEventListener('DOMContentLoaded', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  auctionId = urlParams.get('id');

  if (!auctionId) {
    showToast('Missing auction ID in URL', 'error');
    setTimeout(() => { window.location.href = 'browse-produce.html'; }, 1000);
    return;
  }

  await loadAuction();

  // Polling every 3 seconds for real-time bid updates
  pollInterval = setInterval(loadAuction, 3000);

  // Form submission
  const form = document.getElementById('place-bid-form');
  if (form) {
    form.addEventListener('submit', handlePlaceBid);
  }
});

window.addEventListener('beforeunload', () => {
  if (pollInterval) clearInterval(pollInterval);
  if (countdownTimer) clearInterval(countdownTimer);
});

async function loadAuction() {
  try {
    const res = await apiRequest(`/auctions/${auctionId}`);
    if (res && res.success && res.data) {
      currentAuction = res.data;
      renderAuction(currentAuction);
    } else {
      showToast(res.message || 'Auction not found', 'error');
    }
  } catch (err) {
    console.error('Failed to load auction data', err);
  }
}

function renderAuction(auction) {
  const lot = auction.produceLot || {};
  const cropName = lot.cropName || 'Produce Lot';

  document.getElementById('crop-img').src = getCropImageSrc(cropName);
  document.getElementById('crop-name').textContent = cropName;
  document.getElementById('round-badge').textContent = `Round ${auction.roundNumber}`;
  document.getElementById('lot-meta').textContent = `${lot.quantity} ${lot.unit} • Category: ${lot.category} • Farmer: ${lot.farmerName || 'Registered Producer'}`;
  document.getElementById('min-price').textContent = `${formatCurrency(auction.minPriceSnapshot)} / ${lot.unit}`;

  document.getElementById('spec-quantity').textContent = `${lot.quantity} ${lot.unit}`;
  document.getElementById('spec-category').textContent = lot.category || '-';
  document.getElementById('spec-origin').textContent = lot.farmerLocation || 'Local Farm';
  document.getElementById('spec-started').textContent = formatDateTime(auction.startTime);
  document.getElementById('spec-closing').textContent = formatDateTime(auction.endTime);

  // Render Highest Bid
  const highestEl = document.getElementById('highest-bid-display');
  const minBidHint = document.getElementById('min-bid-hint');
  const bidInput = document.getElementById('bid-amount');

  let currentHigh = auction.minPriceSnapshot;
  if (auction.highestBid) {
    currentHigh = auction.highestBid.amountPerUnit;
    highestEl.textContent = `${formatCurrency(currentHigh)} / ${lot.unit}`;
  } else {
    highestEl.textContent = `No bids yet (Min ${formatCurrency(auction.minPriceSnapshot)})`;
  }

  // Calculate minimum allowed next bid
  const nextMinBid = (parseFloat(currentHigh) + (auction.highestBid ? 0.50 : 0.00)).toFixed(2);
  minBidHint.textContent = `Minimum next acceptable bid: ₹${nextMinBid} / ${lot.unit}`;
  if (!bidInput.value || parseFloat(bidInput.value) < parseFloat(nextMinBid)) {
    bidInput.placeholder = nextMinBid;
  }

  document.getElementById('bid-count-display').textContent = `${auction.bidCount || 0} competitive bids placed`;

  // Countdown clock management
  remainingSeconds = auction.timeRemainingSeconds;
  updateCountdownDisplay();
  if (!countdownTimer) {
    countdownTimer = setInterval(() => {
      if (remainingSeconds > 0) {
        remainingSeconds--;
        updateCountdownDisplay();
      } else {
        clearInterval(countdownTimer);
        countdownTimer = null;
        handleAuctionExpired();
      }
    }, 1000);
  }

  // Check if closed
  if (auction.status === 'CLOSED' || remainingSeconds <= 0) {
    handleAuctionClosed(auction);
  }

  // Render Bids History Table
  renderBidsTable(auction.bids || [], lot.unit);

  // User role check & Bidding Permissions
  evaluateUserPermissions(auction);
}

function updateCountdownDisplay() {
  const countdownEl = document.getElementById('countdown-display');
  if (remainingSeconds <= 0) {
    countdownEl.textContent = '⛔ Auction Bidding Closed';
    countdownEl.style.background = '#FFEBEE';
    countdownEl.style.color = '#C62828';
    countdownEl.style.borderColor = '#FFCDD2';
  } else {
    countdownEl.textContent = `⏳ ${formatTimeRemaining(remainingSeconds)}`;
  }
}

function handleAuctionExpired() {
  showToast('Auction time has ended! Finalizing winning settlement...', 'info');
  setTimeout(() => {
    loadAuction();
  }, 1500);
}

function handleAuctionClosed(auction) {
  const form = document.getElementById('place-bid-form');
  const guideText = document.getElementById('bid-guide-text');
  const notice = document.getElementById('bid-notice');
  const liveIndicator = document.getElementById('live-indicator');

  if (form) form.style.display = 'none';
  if (guideText) guideText.style.display = 'none';
  if (liveIndicator) {
    liveIndicator.textContent = 'CLOSED';
    liveIndicator.className = 'badge badge-closed';
    liveIndicator.style.animation = 'none';
  }

  if (notice) {
    notice.style.display = 'block';
    notice.innerHTML = `
      <div style="font-weight: 700; color: var(--text-primary); margin-bottom: 0.5rem;">
        This auction round is officially closed.
      </div>
      <a href="auction-result.html?id=${auction.id}" class="btn btn-primary btn-sm">
        View Official Auction Settlement →
      </a>
    `;
  }
}

function evaluateUserPermissions(auction) {
  const farmerSlot = document.getElementById('farmer-actions-slot');
  const form = document.getElementById('place-bid-form');
  const notice = document.getElementById('bid-notice');

  if (!currentUser) {
    // Guest user
    if (form) form.style.display = 'none';
    if (notice) {
      notice.style.display = 'block';
      notice.innerHTML = `
        <div style="margin-bottom: 0.5rem;">You must be logged in as a Buyer to place bids.</div>
        <a href="buyer-login.html" class="btn btn-secondary btn-sm">Sign In as Buyer</a>
      `;
    }
    return;
  }

  const isOwnerFarmer = (currentUser.role === 'FARMER') &&
      (auction.produceLot && auction.produceLot.farmerId === currentUser.id);

  if (isOwnerFarmer) {
    // Owning farmer: cannot bid on own produce, can close early
    if (form) form.style.display = 'none';
    if (notice) {
      notice.style.display = 'block';
      notice.textContent = 'You are the registered seller of this produce lot. Bidding is disabled for farmers.';
    }

    if (auction.status === 'OPEN' && remainingSeconds > 0) {
      farmerSlot.innerHTML = `
        <button class="btn btn-danger btn-sm" onclick="closeAuctionEarly(${auction.id})">
          Close Auction Early
        </button>
      `;
    } else {
      farmerSlot.innerHTML = '';
    }
  } else if (currentUser.role === 'FARMER') {
    // Other farmer: cannot bid
    if (form) form.style.display = 'none';
    if (notice) {
      notice.style.display = 'block';
      notice.textContent = 'Farmer accounts cannot participate in bidding. Please register as a Buyer to bid.';
    }
  }
}

function renderBidsTable(bids, unit) {
  const tbody = document.getElementById('bids-tbody');
  if (!tbody) return;

  if (bids.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="4" style="text-align: center; padding: 2rem; color: var(--text-muted);">
          No bids placed yet. Be the first to place a competitive bid!
        </td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = '';
  bids.forEach(b => {
    const tr = document.createElement('tr');
    if (b.isMyBid) {
      tr.style.backgroundColor = 'var(--primary-light)';
    }

    const badgeClass = b.status === 'WINNING' ? 'badge-open' : (b.status === 'OUTBID' ? 'badge-unsold' : 'badge-closed');
    const myTag = b.isMyBid ? ' <strong style="color: var(--primary);">(You)</strong>' : '';

    tr.innerHTML = `
      <td>
        <strong>${escapeHtml(b.buyerName)}</strong>${myTag}
        ${b.buyerBusiness ? `<div style="font-size: 0.75rem; color: var(--text-muted);">${escapeHtml(b.buyerBusiness)}</div>` : ''}
      </td>
      <td style="font-size: 1.1rem; font-weight: 800; color: var(--primary-hover);">
        ${formatCurrency(b.amountPerUnit)} / ${escapeHtml(unit)}
      </td>
      <td style="font-size: 0.85rem; color: var(--text-secondary);">
        ${formatDateTime(b.placedAt)}
      </td>
      <td>
        <span class="badge ${badgeClass}">${b.status}</span>
      </td>
    `;
    tbody.appendChild(tr);
  });
}

async function handlePlaceBid(e) {
  e.preventDefault();
  const amountInput = document.getElementById('bid-amount');
  const submitBtn = document.getElementById('place-bid-btn');
  const amount = parseFloat(amountInput.value);

  if (isNaN(amount) || amount <= 0) {
    showToast('Please enter a valid positive bid amount', 'error');
    return;
  }

  submitBtn.disabled = true;
  submitBtn.textContent = 'Submitting bid...';

  try {
    const res = await apiRequest(`/auctions/${auctionId}/bids`, {
      method: 'POST',
      body: { amount }
    });

    if (res && res.success) {
      showToast('Bid placed successfully! You are currently the highest bidder.', 'success');
      amountInput.value = '';
      await loadAuction();
    } else {
      showToast(res.message || 'Bid submission failed', 'error');
    }
  } catch (err) {
    showToast(err.message || 'Bid submission failed', 'error');
  } finally {
    submitBtn.disabled = false;
    submitBtn.textContent = 'Place Bid';
  }
}

function closeAuctionEarly(id) {
  confirmDialog(
    'Close Auction Early?',
    'Are you sure you want to close this auction now? The current highest bidder will win immediately, or the lot will be marked UNSOLD if no bids met your reserve price.',
    async () => {
      try {
        const res = await apiRequest(`/auctions/${id}/close`, { method: 'POST' });
        if (res && res.success) {
          showToast('Auction closed early successfully!', 'success');
          await loadAuction();
        } else {
          showToast(res.message || 'Failed to close auction', 'error');
        }
      } catch (err) {
        showToast(err.message || 'Failed to close auction', 'error');
      }
    }
  );
}
