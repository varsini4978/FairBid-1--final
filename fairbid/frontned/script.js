// ==========================================
// CONFIGURATION & API BASE
// ==========================================
const BASE_URL = 'http://localhost:8081';

// ==========================================
// HELPER FETCH FUNCTIONS
// ==========================================
async function sendPostRequest(endpoint, payload) {
    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            body: JSON.stringify(payload)
        });

        const data = await response.json().catch(() => null);
        if (!response.ok) {
            const msg = data && data.message ? data.message : `Server returned status code: ${response.status}`;
            return { success: false, message: msg, status: response.status };
        }
        return data || { success: true };
    } catch (error) {
        console.error(`API POST Error at ${endpoint}:`, error);
        return { success: false, message: error.message || 'Network connection failed.' };
    }
}

async function sendGetRequest(endpoint) {
    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        });

        const data = await response.json().catch(() => null);
        if (!response.ok) {
            const msg = data && data.message ? data.message : `Server returned status code: ${response.status}`;
            return { success: false, message: msg, status: response.status };
        }
        return data || { success: true };
    } catch (error) {
        console.error(`API GET Error at ${endpoint}:`, error);
        return { success: false, message: error.message || 'Network connection failed.' };
    }
}

// ==========================================
// UTILITY FUNCTIONS (TOASTS, FORMATTING, XSS)
// ==========================================
function showToast(message, type = 'info') {
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        toast.style.transition = 'all 0.3s ease';
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function escapeHtml(str) {
    if (str == null) return '';
    const div = document.createElement('div');
    div.textContent = String(str);
    return div.innerHTML;
}

function formatCurrency(amount) {
    if (amount == null || isNaN(amount)) return '₹0.00';
    const num = typeof amount === 'number' ? amount : parseFloat(amount);
    return '₹' + num.toLocaleString('en-IN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}

function formatDateTime(dateStr) {
    if (!dateStr) return '-';
    let safeDate = dateStr;
    if (typeof dateStr === 'string' && dateStr.includes(' ') && !dateStr.includes('T')) {
        safeDate = dateStr.replace(' ', 'T') + 'Z';
    }
    const d = new Date(safeDate);
    if (isNaN(d.getTime())) return dateStr;
    return d.toLocaleString('en-IN', {
        timeZone: 'Asia/Kolkata',
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
    });
}

function getCropImageSrc(cropName) {
    if (!cropName) return 'assets/images/default-crop.svg';
    const name = cropName.toLowerCase();
    if (name.includes('tomato')) return 'assets/images/tomatoes.svg';
    if (name.includes('onion')) return 'assets/images/onion.svg';
    if (name.includes('potato')) return 'assets/images/potato.svg';
    if (name.includes('rice') || name.includes('paddy')) return 'assets/images/rice.svg';
    if (name.includes('wheat')) return 'assets/images/wheat.svg';
    return 'assets/images/default-crop.svg';
}

function setButtonLoading(btn, isLoading, loadingText = 'Processing...') {
    if (!btn) return;
    if (isLoading) {
        btn.dataset.originalText = btn.textContent;
        btn.textContent = loadingText;
        btn.disabled = true;
    } else {
        if (btn.dataset.originalText) {
            btn.textContent = btn.dataset.originalText;
        }
        btn.disabled = false;
    }
}

// ==========================================
// SESSION MANAGERS
// ==========================================
function getLoggedInFarmer() {
    try {
        return JSON.parse(sessionStorage.getItem("loggedInFarmer"));
    } catch (e) {
        return null;
    }
}

function getLoggedInBuyer() {
    try {
        return JSON.parse(sessionStorage.getItem("loggedInBuyer"));
    } catch (e) {
        return null;
    }
}

function logoutFarmer() {
    sessionStorage.removeItem("loggedInFarmer");
    window.location.href = "farmer_login.html";
}

function logoutBuyer() {
    sessionStorage.removeItem("loggedInBuyer");
    window.location.href = "buyer.html";
}

// ==========================================
// 1. FARMER AUTHENTICATION & REGISTRATION
// ==========================================
async function handleFarmerLogin(event) {
    event.preventDefault();
    const btn = event.target.querySelector('button[type="submit"]');
    const loginErr = document.getElementById('loginError');
    if (loginErr) loginErr.textContent = "";

    const data = {
        farmerId: document.getElementById('loginId').value.trim(),
        password: document.getElementById('loginPassword').value.trim()
    };

    setButtonLoading(btn, true, 'Signing In...');

    const response = await sendPostRequest('/api/auth/farmer-login', data);
    setButtonLoading(btn, false);

    if (response && response.success) {
        const farmerObj = response.farmer || { farmerId: data.farmerId, fullName: data.farmerId };
        sessionStorage.setItem("loggedInFarmer", JSON.stringify({
            id: farmerObj.farmerId,
            name: farmerObj.fullName,
            state: farmerObj.state || '',
            primaryCrop: farmerObj.primaryCrop || ''
        }));
        window.location.href = 'farmer_dashboard.html';
    } else {
        if (loginErr) {
            loginErr.textContent = (response && response.message) ? response.message : "Invalid Farmer ID or password.";
        }
    }
}

async function handleFarmerRegister(event) {
    event.preventDefault();
    const btn = event.target.querySelector('button[type="submit"]');
    const regMsg = document.getElementById('regMessage');
    if (regMsg) {
        regMsg.textContent = "";
        regMsg.className = "";
    }

    const data = {
        fullName: document.getElementById('regFullName').value.trim(),
        farmerId: document.getElementById('regFarmerId').value.trim(),
        state: document.getElementById('regState').value,
        primaryCrop: document.getElementById('regCrop').value,
        password: document.getElementById('regPassword').value.trim()
    };

    setButtonLoading(btn, true, 'Registering...');

    const response = await sendPostRequest('/api/auth/farmer-register', data);
    setButtonLoading(btn, false);

    if (response && response.success) {
        if (regMsg) {
            regMsg.className = "success";
            regMsg.textContent = "Registration successful! Redirecting to login...";
        }
        showToast("Registration successful! Please login.", "success");
        setTimeout(() => {
            document.getElementById("registerForm").reset();
            if (typeof switchTab === 'function') switchTab('login');
            const loginIdEl = document.getElementById("loginId");
            if (loginIdEl) loginIdEl.value = data.farmerId;
        }, 1200);
    } else {
        if (regMsg) {
            regMsg.className = "error";
            regMsg.textContent = (response && response.message) ? response.message : "Registration failed. ID may already exist.";
        }
    }
}

// ==========================================
// 2. BUYER AUTHENTICATION & REGISTRATION
// ==========================================
async function handleLogin(event) {
    event.preventDefault();
    const btn = event.target.querySelector('button[type="submit"]');
    const errorMsg = document.getElementById('loginError');
    if (errorMsg) errorMsg.style.display = 'none';

    const data = {
        email: document.getElementById('loginEmail').value.trim(),
        password: document.getElementById('loginPassword').value.trim()
    };

    setButtonLoading(btn, true, 'Authenticating...');

    const response = await sendPostRequest('/api/auth/buyer-login', data);
    setButtonLoading(btn, false);

    if (response && response.success) {
        const buyerObj = response.buyer || { email: data.email, name: data.email };
        sessionStorage.setItem("loggedInBuyer", JSON.stringify({
            email: buyerObj.email,
            name: buyerObj.name || buyerObj.email,
            category: buyerObj.category || 'Buyer'
        }));
        window.location.href = "buyer_dashboard.html";
    } else {
        if (errorMsg) {
            errorMsg.textContent = (response && response.message) ? response.message : "Invalid email or password.";
            errorMsg.style.display = 'block';
        }
    }
}

async function handleRegister(event) {
    event.preventDefault();
    const btn = event.target.querySelector('button[type="submit"]');

    const data = {
        name: document.getElementById('regName').value.trim(),
        email: document.getElementById('regEmail').value.trim(),
        password: document.getElementById('regPassword').value.trim(),
        category: document.getElementById('buyerCategory').value,
        docNumber: document.getElementById('docInput') ? document.getElementById('docInput').value.trim() : ""
    };

    setButtonLoading(btn, true, 'Registering...');

    const response = await sendPostRequest('/api/auth/buyer-register', data);
    setButtonLoading(btn, false);

    if (response && response.success) {
        showToast("Registration successful! You can now log in.", "success");
        document.getElementById('registerForm').reset();
        if (typeof switchTab === 'function') switchTab('login');
        const loginEmailEl = document.getElementById('loginEmail');
        if (loginEmailEl) loginEmailEl.value = data.email;
    } else {
        const msg = (response && response.message) ? response.message : "Registration failed or email already exists.";
        showToast(msg, "error");
    }
}

// ==========================================
// 3. CROP LISTING (FARMER)
// ==========================================
async function addProduct(event) {
    event.preventDefault();
    const btn = event.target.querySelector('button[type="submit"]');
    const loggedInFarmer = getLoggedInFarmer();
    const farmerId = loggedInFarmer ? loggedInFarmer.id : "F101";
    const farmerName = loggedInFarmer ? loggedInFarmer.name : "Farmer";

    const cropName = document.getElementById("cropName").value;
    const quantity = parseInt(document.getElementById("quantity").value, 10);
    const minPrice = parseFloat(document.getElementById("minPrice").value);
    const location = document.getElementById("location").value.trim();
    const qualityGrade = document.getElementById("qualityGrade") ? document.getElementById("qualityGrade").value : "Grade A";
    const description = document.getElementById("description") ? document.getElementById("description").value.trim() : "";

    const data = {
        farmerId: farmerId,
        farmerName: farmerName,
        cropName: cropName,
        quantity: quantity,
        minPrice: minPrice,
        location: location,
        qualityGrade: qualityGrade,
        description: description
    };

    setButtonLoading(btn, true, 'Listing Crop...');

    const response = await sendPostRequest('/api/crops', data);
    setButtonLoading(btn, false);

    if (response && response.success) {
        showToast("Crop listed successfully for auction!", "success");
        document.getElementById("productForm").reset();
        if (typeof loadFarmerCrops === 'function') {
            await loadFarmerCrops();
        }
    } else {
        const msg = (response && response.message) ? response.message : "Failed to save crop to database.";
        showToast(msg, "error");
    }
}

// ==========================================
// 4. BIDDING SYSTEM (BUYER & AUCTION ROOM)
// ==========================================
async function submitBid(event) {
    event.preventDefault();
    const urlParams = new URLSearchParams(window.location.search);
    const cropIdParam = urlParams.get('id') || urlParams.get('cropId');
    const cropId = cropIdParam ? parseInt(cropIdParam, 10) : (window.currentCropId || 101);
    const loggedInBuyer = getLoggedInBuyer();
    const buyerEmail = loggedInBuyer ? loggedInBuyer.email : "buyer@example.com";
    const buyerName = loggedInBuyer ? loggedInBuyer.name : "Wholesale Buyer";
    const bidInput = document.getElementById('bidAmount');
    const bidAmount = parseFloat(bidInput.value);
    if (isNaN(bidAmount) || bidAmount <= 0) {
        showToast("Please enter a valid positive bid amount.", "warning");
        return;
    }
    setButtonLoading(event.target.querySelector('button[type="submit"]') || document.getElementById('bidButton'), true, 'Submitting Bid...');
    const data = { cropId, buyerEmail, buyerName, bidAmount };
    const response = await sendPostRequest('/api/bids', data);
    setButtonLoading(event.target.querySelector('button[type="submit"]') || document.getElementById('bidButton'), false);
    if (response && response.success) {
        const msg = response.belowMsp ? "Bid recorded but is below the farmer's MSP." : "Bid recorded successfully!";
        showToast(msg, "success");
        bidInput.value = '';
        if (typeof refreshAuctionRoomData === 'function') { await refreshAuctionRoomData(); }
    } else {
        const msg = (response && response.message) ? response.message : "Bid submission failed.";
        showToast(msg, "error");
    }
}

// Farmer accept/reject bid operations
async function acceptBidAction(cropId, bidId) {
    const loggedInFarmer = getLoggedInFarmer();
    const farmerId = loggedInFarmer ? loggedInFarmer.id : null;

    const data = {
        action: 'accept',
        cropId: cropId,
        bidId: bidId,
        farmerId: farmerId
    };

    const response = await sendPostRequest('/api/bids/accept', data);
    if (response && response.success) {
        showToast(response.message || "Bid accepted! Produce marked as SOLD.", "success");
        if (typeof refreshFarmerAuctionRoom === 'function') {
            await refreshFarmerAuctionRoom();
        }
    } else {
        showToast((response && response.message) ? response.message : "Failed to accept bid.", "error");
    }
}

async function rejectBidAction(cropId, bidId) {
    const loggedInFarmer = getLoggedInFarmer();
    const farmerId = loggedInFarmer ? loggedInFarmer.id : null;

    const data = {
        action: 'reject',
        cropId: cropId,
        bidId: bidId,
        farmerId: farmerId
    };

    const response = await sendPostRequest('/api/bids/reject', data);
    if (response && response.success) {
        showToast((response && response.message) ? response.message : "Bid rejected.", "info");
        if (typeof refreshFarmerAuctionRoom === 'function') {
            await refreshFarmerAuctionRoom();
        }
        if (typeof loadIncomingBids === 'function') {
            await loadIncomingBids();
        }
    } else {
        showToast((response && response.message) ? response.message : "Failed to reject bid.", "error");
    }
}

async function acceptAnywayAction(cropId, bidId) {
    const loggedInFarmer = getLoggedInFarmer();
    const farmerId = loggedInFarmer ? loggedInFarmer.id : null;

    const data = {
        action: 'acceptAnyway',
        cropId: cropId,
        bidId: bidId,
        farmerId: farmerId
    };

    const response = await sendPostRequest('/api/bids/accept-anyway', data);
    if (response && response.success) {
        showToast(response.message || "Below-MSP bid accepted! Lot marked as SOLD.", "success");
        if (typeof refreshFarmerAuctionRoom === 'function') {
            await refreshFarmerAuctionRoom();
        }
        if (typeof loadFarmerCrops === 'function') {
            await loadFarmerCrops();
        }
        if (typeof loadIncomingBids === 'function') {
            await loadIncomingBids();
        }
    } else {
        showToast((response && response.message) ? response.message : "Failed to accept bid.", "error");
    }
}

// ==========================================
// 5. REVISE MSP WORKFLOW
// ==========================================
function openReviseMspModal(cropId, currentMsp, cropName) {
    window.currentReviseCropId = cropId;
    const modal = document.getElementById('reviseMspModal');
    if (!modal) return;
    
    const cropNameEl = document.getElementById('reviseCropName');
    const curMspEl = document.getElementById('currentMspDisplay');
    const inputEl = document.getElementById('newMspInput');
    
    if (cropNameEl) cropNameEl.textContent = cropName || `#LOT-${cropId}`;
    if (curMspEl) curMspEl.textContent = formatCurrency(currentMsp);
    if (inputEl) {
        inputEl.value = currentMsp || '';
        inputEl.focus();
    }
    
    modal.style.display = 'flex';
}

function closeReviseMspModal() {
    const modal = document.getElementById('reviseMspModal');
    if (modal) modal.style.display = 'none';
}

async function submitReviseMsp(event) {
    if (event) event.preventDefault();
    const cropId = window.currentReviseCropId;
    const inputEl = document.getElementById('newMspInput');
    const newMsp = parseFloat(inputEl.value);

    if (isNaN(newMsp) || newMsp <= 0) {
        showToast("Please enter a valid positive MSP amount.", "warning");
        return;
    }

    const loggedInFarmer = getLoggedInFarmer();
    const farmerId = loggedInFarmer ? loggedInFarmer.id : null;

    const data = {
        cropId: cropId,
        newMsp: newMsp,
        farmerId: farmerId
    };

    const btn = document.getElementById('btnSubmitReviseMsp');
    setButtonLoading(btn, true, 'Updating MSP...');

    const res = await sendPostRequest('/api/bids/revise-msp', data);
    setButtonLoading(btn, false);

    if (res && res.success) {
        showToast(res.message || "MSP updated successfully!", "success");
        closeReviseMspModal();
        if (typeof loadFarmerCrops === 'function') await loadFarmerCrops();
        if (typeof loadIncomingBids === 'function') await loadIncomingBids();
        if (typeof refreshFarmerAuctionRoom === 'function') await refreshFarmerAuctionRoom();
    } else {
        showToast((res && res.message) ? res.message : "Failed to update MSP.", "error");
    }
}

// ==========================================
// 6. INCOMING LIVE BUYER BIDS (FARMER DASHBOARD)
// ==========================================
async function loadIncomingBids() {
    const farmer = getLoggedInFarmer();
    if (!farmer) return;

    const tbody = document.getElementById('incomingBidsTableBody');
    if (!tbody) return;

    // First fetch farmer crops to know min prices and crop IDs
    const cropsRes = await sendGetRequest(`/api/crops?farmerId=${encodeURIComponent(farmer.id)}`);
    if (!cropsRes || !cropsRes.success || !cropsRes.crops || cropsRes.crops.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted); padding: 2rem;">No crop lots found. List a produce lot to start receiving bids.</td></tr>`;
        return;
    }

    const cropsMap = {};
    cropsRes.crops.forEach(c => { cropsMap[c.id] = c; });

    // Fetch bids for all crops of this farmer
    const allBids = [];
    await Promise.all(cropsRes.crops.map(async (c) => {
        const bidsRes = await sendGetRequest(`/api/bids?cropId=${c.id}`);
        if (bidsRes && bidsRes.success && bidsRes.bids) {
            bidsRes.bids.forEach(b => {
                b.cropObj = c;
                allBids.push(b);
            });
        }
    }));

    if (allBids.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color: var(--text-muted); padding: 2.5rem;">No buyer bids have been placed yet. Bids will appear here in real time.</td></tr>`;
        return;
    }

    // Sort by createdAt descending (most recent first)
    allBids.sort((a, b) => new Date(b.createdAtIso || b.createdAt) - new Date(a.createdAtIso || a.createdAt));

    tbody.innerHTML = '';
    allBids.forEach(bid => {
        const tr = document.createElement('tr');
        const crop = bid.cropObj || {};
        const isBelowMsp = bid.bidAmount < (crop.minPrice || 0);
        const isPending = (bid.status || '').toUpperCase() === 'PENDING';
        const isSold = (crop.status || '').toUpperCase() === 'SOLD';

        // Badges
        let statusBadge = '';
        if (bid.status === 'ACCEPTED') {
            statusBadge = `<span class="badge badge-sold">✓ ACCEPTED</span>`;
        } else if (bid.status === 'REJECTED') {
            statusBadge = `<span class="badge" style="background:#FFEBEE; color:#C62828;">✕ REJECTED</span>`;
        } else if (isBelowMsp) {
            statusBadge = `<span class="badge" style="background:#FFF3E0; color:#E65100; font-weight:700;">⚠️ BELOW MSP (₹${crop.minPrice})</span>`;
        } else {
            statusBadge = `<span class="badge badge-open">✓ ABOVE MSP</span>`;
        }

        // Actions
        let actionButtons = '';
        if (isPending && !isSold) {
            if (isBelowMsp) {
                actionButtons = `
                    <div style="display:flex; gap:0.4rem; flex-wrap:wrap;">
                        <button class="btn btn-secondary btn-sm" onclick="openReviseMspModal(${crop.id}, ${crop.minPrice}, '${escapeHtml(crop.cropName)}')">✏️ Revise MSP</button>
                        <button class="btn btn-primary btn-sm" onclick="acceptAnywayAction(${crop.id}, ${bid.id})">✓ Accept Anyway</button>
                        <button class="btn btn-sm" style="background:#FFEBEE; color:#C62828; border:1px solid #FFCDD2;" onclick="rejectBidAction(${crop.id}, ${bid.id})">✕ Reject</button>
                    </div>
                `;
            } else {
                actionButtons = `
                    <div style="display:flex; gap:0.4rem; flex-wrap:wrap;">
                        <button class="btn btn-primary btn-sm" onclick="acceptBidAction(${crop.id}, ${bid.id})">✓ Accept Deal</button>
                        <button class="btn btn-sm" style="background:#FFEBEE; color:#C62828; border:1px solid #FFCDD2;" onclick="rejectBidAction(${crop.id}, ${bid.id})">✕ Reject</button>
                    </div>
                `;
            }
        } else {
            actionButtons = `<span style="font-size:0.8rem; color:var(--text-muted);">${bid.status}</span>`;
        }

        // Voice speech text
        const speechText = `${crop.cropName || 'Produce'}, Bid by ${bid.buyerName || 'Buyer'}: ${bid.bidAmount} rupees. Status: ${isBelowMsp ? 'Below MSP' : 'Above MSP'}.`;

        tr.innerHTML = `
            <td>
                <strong>${escapeHtml(crop.cropName || 'Lot #' + bid.cropId)}</strong>
                <button class="tts-btn" title="Listen" onclick="FairBidVoice.speak('${speechText.replace(/'/g, "\\'")}')">🔊</button>
                <div style="font-size:0.75rem; color:var(--text-muted);">#LOT-${bid.cropId} • MSP: ${formatCurrency(crop.minPrice)}</div>
            </td>
            <td>
                <div>${escapeHtml(bid.buyerName || 'Buyer')}</div>
                <div style="font-size:0.75rem; color:var(--text-muted);">${escapeHtml(bid.buyerEmail || '')}</div>
            </td>
            <td><strong style="color:var(--primary); font-size:1.05rem;">${formatCurrency(bid.bidAmount)}</strong>/kg</td>
            <td><span style="font-size:0.85rem; color:var(--text-secondary);">${formatDateTime(bid.createdAtIso || bid.createdAt)}</span></td>
            <td>${statusBadge}</td>
            <td>${actionButtons}</td>
        `;

        tbody.appendChild(tr);
    });
}

// ==========================================
// 7. FARMER NOTIFICATIONS WORKFLOW
// ==========================================
async function loadFarmerNotifications() {
    const farmer = getLoggedInFarmer();
    if (!farmer) return;

    const res = await sendGetRequest(`/api/notifications?farmerId=${encodeURIComponent(farmer.id)}`);
    if (!res || !res.success) return;

    const badge = document.getElementById('notifBadge');
    if (badge) {
        if (res.unreadCount > 0) {
            badge.textContent = res.unreadCount;
            badge.style.display = 'flex';
        } else {
            badge.style.display = 'none';
        }
    }

    const listContainer = document.getElementById('notifList');
    if (!listContainer) return;

    if (!res.notifications || res.notifications.length === 0) {
        listContainer.innerHTML = `<li class="notif-empty">${typeof t === 'function' ? t('noNotifications') : 'No new notifications.'}</li>`;
        return;
    }

    listContainer.innerHTML = '';
    res.notifications.forEach(n => {
        const li = document.createElement('li');
        li.className = `notif-item ${n.read ? '' : 'unread'}`;
        const belowMspBadge = n.belowMsp ? `<span class="badge" style="background:#FFF3E0; color:#E65100; font-size:0.7rem; margin-left:0.3rem;">⚠️ Below MSP</span>` : '';
        const speechMsg = `New bid on ${n.cropName} by ${n.buyerName} of ${n.bidAmount} rupees.`;

        li.innerHTML = `
            <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                <div>
                    <strong>${escapeHtml(n.buyerName)}</strong> placed <strong>${formatCurrency(n.bidAmount)}/kg</strong> on <strong>${escapeHtml(n.cropName)}</strong> ${belowMspBadge}
                </div>
                <button class="tts-btn" title="Listen" onclick="event.stopPropagation(); FairBidVoice.speak('${speechMsg.replace(/'/g, "\\'")}')">🔊</button>
            </div>
            <div style="display:flex; justify-content:space-between; align-items:center; margin-top:0.35rem;">
                <span style="font-size:0.75rem; color:var(--text-muted);">${formatDateTime(n.createdAtIso || n.createdAt)}</span>
                ${n.read ? '' : `<button class="btn btn-sm" style="font-size:0.7rem; padding:0.15rem 0.4rem;" onclick="event.stopPropagation(); markNotificationRead(${n.id})">Mark read</button>`}
            </div>
        `;

        li.onclick = () => {
            markNotificationRead(n.id);
            if (typeof loadIncomingBids === 'function') loadIncomingBids();
        };

        listContainer.appendChild(li);
    });
}

function toggleNotifDropdown() {
    const dropdown = document.getElementById('notifDropdown');
    if (!dropdown) return;
    const isShown = dropdown.style.display === 'block';
    dropdown.style.display = isShown ? 'none' : 'block';
    if (!isShown) {
        loadFarmerNotifications();
    }
}

async function markNotificationRead(notifId) {
    const farmer = getLoggedInFarmer();
    if (!farmer) return;
    await sendPostRequest('/api/notifications', { farmerId: farmer.id, notificationId: notifId });
    loadFarmerNotifications();
}

async function markAllNotificationsRead() {
    const farmer = getLoggedInFarmer();
    if (!farmer) return;
    await sendPostRequest('/api/notifications', { farmerId: farmer.id, all: true });
    loadFarmerNotifications();
}

// ==========================================
// 8. GOVERNMENT IDENTITY VERIFICATION WORKFLOW
// ==========================================
function renderVerificationBadge(status, type) {
    const s = (status || 'PENDING').toUpperCase();
    if (s === 'GOVT_VERIFIED') {
        return `<span class="badge-verified-govt" title="Government Verified via Official Gateway">✓ Government Verified</span>`;
    } else if (s === 'DEMO_VERIFIED') {
        return `<span class="badge-verified-demo" title="${escapeHtml(type || 'Demo Verified')}">⚡ Demo Verified</span>`;
    } else {
        return `<span class="badge-verified-pending" title="Pending Identity Verification">⏳ Pending Verification</span>`;
    }
}

function openVerifyFarmerModal() {
    const modal = document.getElementById('verifyFarmerModal');
    if (modal) modal.style.display = 'flex';
}

function closeVerifyFarmerModal() {
    const modal = document.getElementById('verifyFarmerModal');
    if (modal) modal.style.display = 'none';
}

async function submitFarmerVerification(event) {
    if (event) event.preventDefault();
    const farmer = getLoggedInFarmer();
    if (!farmer) return;

    const consent = document.getElementById('farmerConsentCheckbox').checked;
    if (!consent) {
        showToast("Explicit consent is required to verify identity.", "warning");
        return;
    }

    const govtFarmerId = document.getElementById('govtFarmerIdInput').value.trim();
    if (govtFarmerId.length < 4) {
        showToast("Please enter a valid Government Farmer ID.", "warning");
        return;
    }

    const mode = document.getElementById('farmerVerifMode') ? document.getElementById('farmerVerifMode').value : 'demo';
    const btn = document.getElementById('btnSubmitFarmerVerif');
    setButtonLoading(btn, true, 'Verifying with AgriStack...');

    const res = await sendPostRequest('/api/verification/verify-farmer', {
        farmerId: farmer.id,
        govtFarmerId: govtFarmerId,
        consent: consent,
        mode: mode
    });
    setButtonLoading(btn, false);

    if (res && res.success) {
        farmer.verificationStatus = res.status;
        farmer.verificationType = res.verificationType;
        sessionStorage.setItem('loggedInFarmer', JSON.stringify(farmer));
        showToast(res.message || "Farmer ID verified successfully!", "success");
        closeVerifyFarmerModal();
        updateFarmerProfileBadges();
    } else {
        showToast((res && res.message) ? res.message : "Verification failed.", "error");
    }
}

function openVerifyBuyerModal() {
    const modal = document.getElementById('verifyBuyerModal');
    if (modal) modal.style.display = 'flex';
}

function closeVerifyBuyerModal() {
    const modal = document.getElementById('verifyBuyerModal');
    if (modal) modal.style.display = 'none';
}

async function submitBuyerVerification(event) {
    if (event) event.preventDefault();
    const buyer = getLoggedInBuyer();
    if (!buyer) return;

    const consent = document.getElementById('buyerConsentCheckbox').checked;
    if (!consent) {
        showToast("Explicit consent is required for Aadhaar verification.", "warning");
        return;
    }

    const last4 = document.getElementById('aadhaarLast4Input').value.trim();
    if (last4.length !== 4 || isNaN(last4)) {
        showToast("Please enter the last 4 digits of your Aadhaar number.", "warning");
        return;
    }

    const otp = document.getElementById('aadhaarOtpInput').value.trim();
    if (!otp) {
        showToast("Please enter the 6-digit OTP (use 123456 for Demo).", "warning");
        return;
    }

    const mode = document.getElementById('buyerVerifMode') ? document.getElementById('buyerVerifMode').value : 'demo';
    const btn = document.getElementById('btnSubmitBuyerVerif');
    setButtonLoading(btn, true, 'Verifying Aadhaar e-KYC...');

    const res = await sendPostRequest('/api/verification/verify-buyer', {
        email: buyer.email,
        aadhaarLast4: last4,
        otp: otp,
        consent: consent,
        mode: mode
    });
    setButtonLoading(btn, false);

    if (res && res.success) {
        buyer.verificationStatus = res.status;
        buyer.verificationType = res.verificationType;
        sessionStorage.setItem('loggedInBuyer', JSON.stringify(buyer));
        showToast(res.message || "Aadhaar e-KYC verified successfully!", "success");
        closeVerifyBuyerModal();
        updateBuyerProfileBadges();
    } else {
        showToast((res && res.message) ? res.message : "Verification failed.", "error");
    }
}

function updateFarmerProfileBadges() {
    const farmer = getLoggedInFarmer();
    if (!farmer) return;
    const badgeContainer = document.getElementById('farmerVerifBadgeSlot');
    if (badgeContainer) {
        badgeContainer.innerHTML = renderVerificationBadge(farmer.verificationStatus, farmer.verificationType);
    }
}

function updateBuyerProfileBadges() {
    const buyer = getLoggedInBuyer();
    if (!buyer) return;
    const badgeContainer = document.getElementById('buyerVerifBadgeSlot');
    if (badgeContainer) {
        badgeContainer.innerHTML = renderVerificationBadge(buyer.verificationStatus, buyer.verificationType);
    }
}

