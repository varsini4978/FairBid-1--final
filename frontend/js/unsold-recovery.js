/**
 * Controller for Unsold Produce Recovery (unsold-recovery.html)
 * Enables Farmer autonomy:
 *  Option 1: Reopen auction at existing minimum price (Round N+1)
 *  Option 2: Revise minimum price and start fresh round
 *  Option 3: Accept / Reject offline local agent offer with full audit logging
 */

let lotId = null;
let currentLot = null;
let currentOffer = null;

document.addEventListener('DOMContentLoaded', async () => {
  const user = await requireAuth('FARMER');
  if (!user) return;

  const urlParams = new URLSearchParams(window.location.search);
  lotId = urlParams.get('lotId');

  if (!lotId) {
    showToast('Missing produce lot ID in URL', 'error');
    setTimeout(() => { window.location.href = 'farmer-dashboard.html'; }, 1000);
    return;
  }

  await loadRecoveryData();
  setupReviseModal();
});

async function loadRecoveryData() {
  const loadingEl = document.getElementById('loading');
  const containerEl = document.getElementById('recovery-container');

  try {
    const lotRes = await apiRequest(`/produce/${lotId}`);
    if (!lotRes || !lotRes.success || !lotRes.data) {
      throw new Error(lotRes ? lotRes.message : 'Lot not found');
    }
    currentLot = lotRes.data;

    // Fetch agent offer (if any)
    try {
      const offerRes = await apiRequest(`/produce/${lotId}/agent-offer`);
      if (offerRes && offerRes.success && offerRes.data) {
        currentOffer = offerRes.data;
      }
    } catch (e) {
      currentOffer = null;
    }

    loadingEl.style.display = 'none';
    containerEl.style.display = 'block';
    renderRecoveryOptions(currentLot, currentOffer);
  } catch (err) {
    console.error('Failed to load recovery data', err);
    loadingEl.innerHTML = `
      <div style="font-size: 2.5rem; margin-bottom: 1rem;">⚠️</div>
      <h3 style="margin-bottom: 0.5rem;">Failed to Load Lot Details</h3>
      <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">${escapeHtml(err.message || 'Error occurred')}</p>
      <a href="farmer-dashboard.html" class="btn btn-primary">Return to Dashboard</a>
    `;
  }
}

function renderRecoveryOptions(lot, offer) {
  const container = document.getElementById('recovery-container');
  const cropImg = getCropImageSrc(lot.cropName);
  const minPrice = formatCurrency(lot.minPricePerUnit);
  const agentPrice = lot.agentOfferPerUnit ? formatCurrency(lot.agentOfferPerUnit) : 'None';

  let agentOfferSection = '';
  if (offer && offer.offeredPricePerUnit && parseFloat(offer.offeredPricePerUnit) > 0) {
    const offerVal = formatCurrency(offer.offeredPricePerUnit);
    const totalVal = formatCurrency(parseFloat(offer.offeredPricePerUnit) * parseFloat(lot.quantity));
    const isAccepted = offer.farmerDecision === 'ACCEPTED';
    const isRejected = offer.farmerDecision === 'REJECTED';

    let decisionStatusBadge = '';
    if (isAccepted) {
      decisionStatusBadge = `<span class="badge badge-open">ACCEPTED (Sold to Agent)</span>`;
    } else if (isRejected) {
      decisionStatusBadge = `<span class="badge badge-unsold">REJECTED by You</span>`;
    } else {
      decisionStatusBadge = `<span class="badge badge-closed">PENDING Your Decision</span>`;
    }

    agentOfferSection = `
      <div class="card" style="margin-bottom: 1.5rem; border-left: 4px solid #3B82F6;">
        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 0.75rem;">
          <div>
            <h3 style="margin-bottom: 0.25rem;">🤝 Offline Local Agent Offer</h3>
            <p style="color: var(--text-secondary); font-size: 0.9rem; margin: 0;">Alternative offline market proposal recorded for this produce lot</p>
          </div>
          ${decisionStatusBadge}
        </div>

        <table class="table" style="width: 100%; margin-bottom: 1rem;">
          <tbody>
            <tr>
              <td style="color: var(--text-secondary); width: 40%;">Agent Offered Price</td>
              <td style="font-size: 1.15rem; font-weight: 700; color: #1E40AF;">${offerVal} / ${escapeHtml(lot.unit)}</td>
            </tr>
            <tr>
              <td style="color: var(--text-secondary);">Total Agent Offer Value</td>
              <td style="font-weight: 700;">${totalVal} (${lot.quantity} ${escapeHtml(lot.unit)})</td>
            </tr>
            ${offer.decidedAt ? `
            <tr>
              <td style="color: var(--text-secondary);">Decision Recorded At</td>
              <td>${formatDateTime(offer.decidedAt)}</td>
            </tr>
            ` : ''}
          </tbody>
        </table>

        ${!isAccepted ? `
        <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
          <a href="agent-comparison.html?lotId=${lot.id}" class="btn btn-secondary btn-sm">
            📊 View Financial Net Comparison
          </a>
          ${offer.farmerDecision === 'PENDING' ? `
          <button class="btn btn-primary btn-sm" onclick="handleAgentDecision('ACCEPTED')">
            Accept Agent Offer
          </button>
          <button class="btn btn-danger btn-sm" onclick="handleAgentDecision('REJECTED')">
            Reject Offer
          </button>
          ` : ''}
        </div>
        ` : `
        <div style="color: var(--accent); font-weight: 600; font-size: 0.95rem;">
          ✅ You accepted this agent offer. The produce lot has been logged as sold offline.
        </div>
        `}
      </div>
    `;
  }

  container.innerHTML = `
    <!-- Lot Header -->
    <div class="card" style="margin-bottom: 1.5rem; display: flex; gap: 1.25rem; align-items: center;">
      <img src="${cropImg}" alt="${escapeHtml(lot.cropName)}" style="width: 64px; height: 64px; border-radius: 8px; object-fit: cover;">
      <div>
        <span class="badge badge-unsold" style="margin-bottom: 0.25rem;">UNSOLD PRODUCE</span>
        <h2 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 0.25rem;">${escapeHtml(lot.cropName)}</h2>
        <p style="color: var(--text-secondary); margin: 0; font-size: 0.95rem;">
          Quantity: <strong>${lot.quantity} ${escapeHtml(lot.unit)}</strong> • Current Reserve: <strong>${minPrice} / ${escapeHtml(lot.unit)}</strong>
        </p>
      </div>
    </div>

    <!-- Options Notice -->
    <div class="card" style="background: #F8FAFC; border: 1px solid var(--border); margin-bottom: 1.5rem;">
      <h3 style="margin-bottom: 0.5rem;">Producer Empowerment Options</h3>
      <p style="color: var(--text-secondary); font-size: 0.95rem; margin: 0;">
        FairBid never forces an outcome. You are in full control of how to proceed with your harvest.
        Choose one of the recovery actions below:
      </p>
    </div>

    <!-- Option 1: Reopen Auction (Same Price) -->
    <div class="card" style="margin-bottom: 1.5rem;">
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div>
          <h3 style="margin-bottom: 0.25rem;">🔄 Option 1: Reopen with Same Reserve Price</h3>
          <p style="color: var(--text-secondary); font-size: 0.9rem; margin: 0;">
            Keep your reserve at <strong>${minPrice} / ${escapeHtml(lot.unit)}</strong> and initiate a fresh auction round for 24 hours.
            Previous bids are preserved in history.
          </p>
        </div>
        <button class="btn btn-primary" onclick="reopenAuctionSamePrice()">
          Reopen Auction Now
        </button>
      </div>
    </div>

    <!-- Option 2: Revise Price & Start New Round -->
    <div class="card" style="margin-bottom: 1.5rem;">
      <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
        <div>
          <h3 style="margin-bottom: 0.25rem;">✏️ Option 2: Revise Minimum Price & Launch Round</h3>
          <p style="color: var(--text-secondary); font-size: 0.9rem; margin: 0;">
            Adjust your reserve price (e.g. slightly lower to attract more bids) and start Round ${((lot.activeAuctionId ? 2 : 1))}.
          </p>
        </div>
        <button class="btn btn-secondary" onclick="openReviseModal()">
          Revise Price & Launch
        </button>
      </div>
    </div>

    <!-- Option 3: Agent Offer (if available) -->
    ${agentOfferSection}

    <div style="margin-top: 1.5rem;">
      <a href="farmer-dashboard.html" class="btn btn-outline">← Back to Farmer Dashboard</a>
    </div>
  `;
}

// -----------------------------------------------------------------------------
// Action Handlers
// -----------------------------------------------------------------------------
async function reopenAuctionSamePrice() {
  confirmDialog(
    'Reopen Auction?',
    `Start a new auction round for "${currentLot.cropName}" at the current reserve price of ${formatCurrency(currentLot.minPricePerUnit)} / ${currentLot.unit}?`,
    async () => {
      try {
        const res = await apiRequest(`/produce/${lotId}/reopen`, {
          method: 'POST',
          body: {}
        });

        if (res && res.success) {
          showToast('New auction round reopened successfully!', 'success');
          setTimeout(() => {
            window.location.href = 'farmer-dashboard.html';
          }, 1200);
        } else {
          showToast(res.message || 'Failed to reopen auction', 'error');
        }
      } catch (err) {
        showToast(err.message || 'Failed to reopen auction', 'error');
      }
    }
  );
}

function openReviseModal() {
  const modal = document.getElementById('revise-modal');
  const priceInput = document.getElementById('new-min-price');
  if (priceInput && currentLot) {
    priceInput.value = currentLot.minPricePerUnit;
  }
  modal.style.display = 'flex';
}

function setupReviseModal() {
  const modal = document.getElementById('revise-modal');
  const cancelBtn = document.getElementById('cancel-revise-btn');
  const form = document.getElementById('revise-price-form');

  if (cancelBtn) {
    cancelBtn.addEventListener('click', () => {
      modal.style.display = 'none';
    });
  }

  if (form) {
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const newPrice = parseFloat(document.getElementById('new-min-price').value);
      const closingTime = document.getElementById('revise-closing-time').value;

      if (isNaN(newPrice) || newPrice <= 0) {
        showToast('Please enter a valid positive reserve price', 'error');
        return;
      }

      const submitBtn = document.getElementById('submit-revise-btn');
      submitBtn.disabled = true;
      submitBtn.textContent = 'Launching...';

      try {
        const payload = { newMinPrice: newPrice };
        if (closingTime) {
          payload.newEndTime = closingTime;
        }

        const res = await apiRequest(`/produce/${lotId}/revise-price`, {
          method: 'POST',
          body: payload
        });

        if (res && res.success) {
          showToast('Minimum price revised and new auction round launched!', 'success');
          modal.style.display = 'none';
          setTimeout(() => {
            window.location.href = 'farmer-dashboard.html';
          }, 1200);
        } else {
          showToast(res.message || 'Failed to revise price', 'error');
          submitBtn.disabled = false;
          submitBtn.textContent = 'Confirm & Launch Round';
        }
      } catch (err) {
        showToast(err.message || 'Failed to revise price', 'error');
        submitBtn.disabled = false;
        submitBtn.textContent = 'Confirm & Launch Round';
      }
    });
  }
}

async function handleAgentDecision(decision) {
  const actionLabel = decision === 'ACCEPTED' ? 'Accept' : 'Reject';
  const confirmMsg = decision === 'ACCEPTED'
    ? 'Are you sure you want to accept this offline agent offer? The lot will be marked as SOLD_TO_AGENT.'
    : 'Are you sure you want to reject this agent offer? You can still reopen auctions on the FairBid platform.';

  confirmDialog(
    `${actionLabel} Agent Offer?`,
    confirmMsg,
    async () => {
      try {
        const res = await apiRequest(`/produce/${lotId}/agent-decision`, {
          method: 'POST',
          body: { decision }
        });

        if (res && res.success) {
          showToast(`Agent offer ${decision.toLowerCase()} successfully!`, 'success');
          await loadRecoveryData();
        } else {
          showToast(res.message || 'Failed to record decision', 'error');
        }
      } catch (err) {
        showToast(err.message || 'Failed to record decision', 'error');
      }
    }
  );
}
