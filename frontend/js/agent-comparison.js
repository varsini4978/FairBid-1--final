/**
 * Controller for Financial Price Comparison (agent-comparison.html)
 * Demonstrates exact monetary math using BigDecimal server responses:
 *  - Quantity x Minimum Price vs Quantity x Agent Price
 *  - Net earnings factoring in transport and miscellaneous handling expenses
 *  - Explicit reminder that the final decision belongs to the farmer
 */

let lotId = null;
let currentComparison = null;

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

  await loadComparison();
});

async function loadComparison(pTransport = 0, pOther = 0, aTransport = 0, aOther = 0) {
  const loadingEl = document.getElementById('loading');
  const containerEl = document.getElementById('comparison-container');

  try {
    const query = `?platformTransport=${pTransport}&platformOther=${pOther}&agentTransport=${aTransport}&agentOther=${aOther}`;
    const res = await apiRequest(`/produce/${lotId}/comparison${query}`);

    if (res && res.success && res.data) {
      currentComparison = res.data;
      loadingEl.style.display = 'none';
      containerEl.style.display = 'block';
      renderComparison(currentComparison, pTransport, pOther, aTransport, aOther);
    } else {
      throw new Error(res ? res.message : 'Failed to calculate comparison');
    }
  } catch (err) {
    console.error('Failed loading comparison:', err);
    loadingEl.innerHTML = `
      <div style="font-size: 2.5rem; margin-bottom: 1rem;">⚠️</div>
      <h3 style="margin-bottom: 0.5rem;">Comparison Unavailable</h3>
      <p style="color: var(--text-secondary); margin-bottom: 1.5rem;">${escapeHtml(err.message || 'Error calculating comparison')}</p>
      <a href="farmer-dashboard.html" class="btn btn-primary">Back to Dashboard</a>
    `;
  }
}

function renderComparison(c, pTransport, pOther, aTransport, aOther) {
  const container = document.getElementById('comparison-container');
  const cropImg = getCropImageSrc(c.cropName);

  const grossDiff = parseFloat(c.grossDifference || 0);
  const netDiff = parseFloat(c.netDifference || 0);
  const isPlatformBetter = netDiff > 0;
  const isAgentBetter = netDiff < 0;

  let comparisonBadge = '';
  if (isPlatformBetter) {
    comparisonBadge = `
      <div class="card" style="border-left: 5px solid var(--accent); background: linear-gradient(to right, #F0FDF4, #FFFFFF); margin-bottom: 1.5rem; padding: 1.5rem;">
        <div style="display: flex; align-items: center; gap: 1rem;">
          <span style="font-size: 2.25rem;">📈</span>
          <div>
            <h3 style="color: var(--primary); margin-bottom: 0.25rem;">Platform Auction Advantage: +${formatCurrency(netDiff)}</h3>
            <p style="color: var(--text-secondary); margin: 0; font-size: 0.95rem;">
              Selling via FairBid's competitive auction is projected to yield <strong>${formatCurrency(netDiff)}</strong> more net earnings than the offline agent offer.
            </p>
          </div>
        </div>
      </div>
    `;
  } else if (isAgentBetter) {
    comparisonBadge = `
      <div class="card" style="border-left: 5px solid #F59E0B; background: linear-gradient(to right, #FFFBEB, #FFFFFF); margin-bottom: 1.5rem; padding: 1.5rem;">
        <div style="display: flex; align-items: center; gap: 1rem;">
          <span style="font-size: 2.25rem;">🤝</span>
          <div>
            <h3 style="color: #B45309; margin-bottom: 0.25rem;">Agent Offer Net Advantage: +${formatCurrency(Math.abs(netDiff))}</h3>
            <p style="color: var(--text-secondary); margin: 0; font-size: 0.95rem;">
              After deducting estimated transport/handling expenses, the offline agent route yields <strong>${formatCurrency(Math.abs(netDiff))}</strong> more net income.
            </p>
          </div>
        </div>
      </div>
    `;
  } else {
    comparisonBadge = `
      <div class="card" style="border-left: 5px solid #64748B; background: #F8FAFC; margin-bottom: 1.5rem; padding: 1.5rem;">
        <h3 style="margin-bottom: 0.25rem;">⚖️ Financially Equal Net Projections</h3>
        <p style="color: var(--text-secondary); margin: 0;">Both routes yield identical net earnings under current expense estimates.</p>
      </div>
    `;
  }

  container.innerHTML = `
    <!-- Lot Header -->
    <div class="card" style="margin-bottom: 1.5rem; display: flex; gap: 1.25rem; align-items: center;">
      <img src="${cropImg}" alt="${escapeHtml(c.cropName)}" style="width: 64px; height: 64px; border-radius: 8px; object-fit: cover;">
      <div>
        <h2 style="font-size: 1.5rem; font-weight: 800; margin-bottom: 0.25rem;">${escapeHtml(c.cropName)}</h2>
        <p style="color: var(--text-secondary); margin: 0; font-size: 0.95rem;">
          Quantity: <strong>${c.quantity} ${escapeHtml(c.unit)}</strong> • Reserve: <strong>${formatCurrency(c.minPricePerUnit)} / ${escapeHtml(c.unit)}</strong> • Agent Offer: <strong>${formatCurrency(c.agentOfferPerUnit)} / ${escapeHtml(c.unit)}</strong>
        </p>
      </div>
    </div>

    ${comparisonBadge}

    <!-- Side-by-Side Comparison Table -->
    <div class="card" style="margin-bottom: 1.5rem;">
      <h3 style="margin-bottom: 1rem; border-bottom: 1px solid var(--border); padding-bottom: 0.5rem;">Side-by-Side Financial Comparison</h3>
      <table class="table" style="width: 100%;">
        <thead>
          <tr style="background: var(--bg-primary);">
            <th style="width: 40%;">Metric</th>
            <th style="color: var(--primary);">🌐 FairBid Auction (At Reserve)</th>
            <th style="color: #1E40AF;">🤝 Offline Local Agent</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>Price Per Unit</td>
            <td><strong>${formatCurrency(c.minPricePerUnit)} / ${escapeHtml(c.unit)}</strong></td>
            <td><strong>${formatCurrency(c.agentOfferPerUnit)} / ${escapeHtml(c.unit)}</strong></td>
          </tr>
          <tr>
            <td>Gross Projected Revenue<br><small style="color: var(--text-muted);">${c.quantity} ${escapeHtml(c.unit)} × unit price</small></td>
            <td style="font-size: 1.15rem; font-weight: 700; color: var(--primary);">${formatCurrency(c.expectedAuctionRevenueMin)}</td>
            <td style="font-size: 1.15rem; font-weight: 700; color: #1E40AF;">${formatCurrency(c.agentRevenue)}</td>
          </tr>
          <tr>
            <td>Gross Difference</td>
            <td colspan="2" style="text-align: center; font-weight: 700; background: #F8FAFC;">
              ${grossDiff >= 0 ? `+${formatCurrency(grossDiff)} in favor of FairBid` : `${formatCurrency(grossDiff)} in favor of Agent`}
            </td>
          </tr>
          <tr>
            <td>Transport Expenses</td>
            <td style="color: #DC2626;">− ${formatCurrency(c.platformTransportCost)}</td>
            <td style="color: #DC2626;">− ${formatCurrency(c.agentTransportCost)}</td>
          </tr>
          <tr>
            <td>Handling / Other Expenses</td>
            <td style="color: #DC2626;">− ${formatCurrency(c.platformOtherExpenses)}</td>
            <td style="color: #DC2626;">− ${formatCurrency(c.agentOtherExpenses)}</td>
          </tr>
          <tr style="background: #F1F5F9; font-size: 1.15rem;">
            <td><strong>Estimated Net Earnings</strong></td>
            <td style="font-weight: 800; color: var(--accent);"><strong>${formatCurrency(c.netAuctionEarnings)}</strong></td>
            <td style="font-weight: 800; color: #1E40AF;"><strong>${formatCurrency(c.netAgentEarnings)}</strong></td>
          </tr>
        </tbody>
      </table>

      ${c.currentHighestBid ? `
      <div style="background: #ECFDF5; border: 1px solid #A7F3D0; border-radius: 6px; padding: 0.75rem 1rem; margin-top: 1rem; font-size: 0.9rem;">
        🌟 <strong>Live Auction Context:</strong> An active bid of <strong>${formatCurrency(c.currentHighestBid)} / ${escapeHtml(c.unit)}</strong> currently exceeds your reserve!
        Revenue at current highest bid: <strong>${formatCurrency(c.revenueAtHighestBid)}</strong>.
      </div>
      ` : ''}

      <div style="background: #FFFBEB; border: 1px solid #FDE68A; border-radius: 6px; padding: 0.75rem 1rem; margin-top: 1rem; font-size: 0.85rem; color: #92400E;">
        ⚠️ <em>${escapeHtml(c.revenueMinLabel)}</em><br>
        ℹ️ <em>${escapeHtml(c.neutralNote)}</em>
      </div>
    </div>

    <!-- Expense Calculator Form -->
    <div class="card" style="margin-bottom: 1.5rem;">
      <h3 style="margin-bottom: 0.5rem;">Adjust Route Expenses</h3>
      <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1rem;">
        Customize transportation and logistics costs to project your real net take-home earnings:
      </p>

      <form id="expense-form">
        <div class="grid-2">
          <div class="form-group">
            <label class="form-label" for="pt-input">Platform Transport Cost (₹)</label>
            <input type="number" id="pt-input" class="form-control" min="0" step="10" value="${pTransport}">
          </div>
          <div class="form-group">
            <label class="form-label" for="po-input">Platform Handling / Other (₹)</label>
            <input type="number" id="po-input" class="form-control" min="0" step="10" value="${pOther}">
          </div>
        </div>

        <div class="grid-2">
          <div class="form-group">
            <label class="form-label" for="at-input">Agent Transport Cost (₹)</label>
            <input type="number" id="at-input" class="form-control" min="0" step="10" value="${aTransport}">
          </div>
          <div class="form-group">
            <label class="form-label" for="ao-input">Agent Handling / Other (₹)</label>
            <input type="number" id="ao-input" class="form-control" min="0" step="10" value="${aOther}">
          </div>
        </div>

        <div style="display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 1rem;">
          <button type="submit" class="btn btn-primary">Recalculate Net Projections</button>
        </div>
      </form>
    </div>

    <!-- Navigation Actions -->
    <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
      <a href="unsold-recovery.html?lotId=${c.lotId}" class="btn btn-secondary">← Back to Recovery Options</a>
      <a href="farmer-dashboard.html" class="btn btn-outline">Farmer Dashboard</a>
    </div>
  `;

  // Attach recalculate form listener
  const form = document.getElementById('expense-form');
  if (form) {
    form.addEventListener('submit', (e) => {
      e.preventDefault();
      const pt = parseFloat(document.getElementById('pt-input').value) || 0;
      const po = parseFloat(document.getElementById('po-input').value) || 0;
      const at = parseFloat(document.getElementById('at-input').value) || 0;
      const ao = parseFloat(document.getElementById('ao-input').value) || 0;
      loadComparison(pt, po, at, ao);
    });
  }
}
