# FairBid Project Completion & Audit Report

**Date & Time:** November 19, 2026  
**System:** Windows 11 • Java 21 LTS • Apache Maven 3.10 • MySQL Server 8.0 • Apache Tomcat 10.1.61  
**Project Workspace:** `FairBid-Application/`

---

## 1. Safety Checkpoint & Versioning

- **Backup Directory:** `FairBid-Application_BACKUP_20261119-1955` (created as a sibling directory prior to any source modification).
- **Git Branch:** Project was not an initialized Git repository at execution time; relied on the full sibling backup folder per Section 3.2.

---

## 2. Files Changed or Created

### Backend (`fairbid/backend/`)
- [pom.xml](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/pom.xml): Added explicit `<finalName>fairbid-backend-1.0-SNAPSHOT</finalName>` to guarantee standard WAR naming and context path.
- [Crop.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/model/Crop.java): Extended model with `id`, `qualityGrade`, `farmerName`, `status`, `description`, `acceptedBidId`, `agreedPrice`, `soldAt` matching MySQL columns.
- [Bid.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/model/Bid.java): Extended model with `id`, `buyerName`, `status`, `createdAt`, and display join fields (`cropName`, `quantity`, `location`, `cropStatus`).
- [CorsUtil.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/util/CorsUtil.java): Unified utility providing consistent CORS headers for `http://127.0.0.1:5500` and `http://localhost:5500`, HTTP 200 `doOptions` handling, and JSON error responses.
- [FarmerDAO.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/dao/FarmerDAO.java): Added `getFarmer(id, pwd)` and `getFarmerById(id)` to return profile data for session context.
- [BuyerDAO.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/dao/BuyerDAO.java): Added `getBuyer(email, pwd)` and `getBuyerByEmail(email)` to return profile data for session context.
- [CropDAO.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/dao/CropDAO.java): Implemented `saveCrop` returning generated ID, `getOpenCrops(crop, location)` for marketplace, `getCropsByFarmer(farmerId)`, and `getCropById(id)`.
- [AuctionDAO.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/dao/AuctionDAO.java): Implemented validated bidding (MSP rule, strictly increasing rule, open status check), bid history querying, and atomic transaction methods `acceptBid` and `rejectBid`.
- [AuthServlet.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/controller/AuthServlet.java): Added CORS support, duplicate registration checks (HTTP 409), wrong password checks (HTTP 401), and profile serialization.
- [CropServlet.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/controller/CropServlet.java): Added HTTP `GET` handler for querying crops by farmer ID, crop ID, and marketplace filters.
- [AuctionServlet.java](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/backend/src/main/java/com/controller/AuctionServlet.java): Added HTTP `GET` handler for querying bids by crop ID or buyer email, plus transactional `/accept` and `/reject` routing with server-side farmer ownership validation.

### Frontend (`fairbid/frontned/`)
- [css/fairbid.css](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/css/fairbid.css): Shared design system stylesheet (agricultural green/amber palette, cards, tables, badges, spotlight box, responsive rules).
- [assets/images/*.svg](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/assets/images/): Vector graphics for default produce, tomatoes, onions, potatoes, wheat, and rice.
- [script.js](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/script.js): Centralized API client (`BASE_URL`), toast notifications, XSS-safe escaping, button loading states, and backend fetch wiring.
- [index.html](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/index.html): Clean entry landing and portal selector page.
- [farmer_login.html](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/farmer_login.html): Farmer login & registration rewired to `/api/auth/farmer-*`.
- [buyer.html](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/buyer.html): Buyer login & registration with dynamic category documentation fields rewired to `/api/auth/buyer-*`.
- [farmer_dashboard.html](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/farmer_dashboard.html): Real-time farmer dashboard displaying live listed crops from MySQL, stat cards, and crop listing form.
- [buyer_dashboard.html](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/buyer_dashboard.html): Live buyer marketplace with category/region search filters and buyer bid history table.
- [auction_room.html](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/auction_room.html): Farmer live auction desk with auto-polling (3.5s), highest bid spotlight, and transactional Accept/Reject controls.
- [buyer_auction_room.html](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/buyer_auction_room.html): Buyer live bidding floor with real-time stream, reserve MSP validation, and winner settlement banner.
- [server.js](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/fairbid/frontned/server.js): Static Node.js HTTP server binding to `127.0.0.1:5500`.
- [test_api_suite.ps1](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/test_api_suite.ps1): Automated 11-scenario integration test suite.
- [screenshots/](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/): Full visual captures for all 7 application pages + mobile responsive layout.

### Database
- Sacred database `fairbid_db` inspected read-only. No schema migration required: existing schema already possessed columns `quality_grade`, `description`, `accepted_bid_id`, `agreed_price`, `sold_at`, and `status`.

---

## 3. Contract Table Bugs & Fixes

| Culprit | Root Cause | Fix Applied |
| :--- | :--- | :--- |
| **Missing GET on `/api/crops`** | `CropServlet` only implemented `doPost`. Requests to fetch crop listings returned HTTP 405 Method Not Allowed. | Implemented `doGet` in `CropServlet` supporting farmer crop lists, single lot detail, and marketplace search. |
| **Missing GET on `/api/bids`** | `AuctionServlet` only implemented `doPost`. The live auction room had no way to read bids from MySQL. | Implemented `doGet` in `AuctionServlet` returning bids by `cropId` (with leading bid and status) and by `buyerEmail`. |
| **Mock LocalStorage Overrides** | `farmer_login.html` and `buyer.html` contained inline `<script>` blocks using mock `localStorage` objects (`farmersDB`, `fairbid_buyers`), bypassing servlets entirely. | Removed mock inline scripts; connected submit events directly to `script.js` which performs real `fetch()` calls to Java servlets. |
| **Missing User Session Payload** | `AuthServlet` responded with bare `{"success": true}`, forcing frontend to guess user details. | Updated `FarmerDAO` and `BuyerDAO` to return full user profile upon successful credentials check. |
| **Missing CORS & OPTIONS Preflight** | `AuthServlet`, `CropServlet`, and `AuctionServlet` only permitted `127.0.0.1:5500` and returned default unhandled OPTIONS preflights. | Added `CorsUtil.setCorsHeaders` supporting both `127.0.0.1:5500` and `localhost:5500` and answering `doOptions` with HTTP 200 OK. |
| **Insecure Bidding & Bid Acceptance** | Previous code had no transaction isolation; accept/reject had no ownership check and could leave multiple accepted bids. | Implemented `AuctionDAO.acceptBid` within an atomic MySQL transaction (`setAutoCommit(false)`, row locks, validating farmer owns crop, bid belongs to crop, marking winner ACCEPTED, marking other bids REJECTED, setting crop SOLD). |
| **Client-Supplied ID Vulnerabilities** | Client could submit bids on closed/sold crops or bypass MSP. | Added strict server-side validation: bid amount must be > 0, crop must be OPEN, bid must be ≥ reserve MSP, and bid must be strictly higher than current leading bid. |

---

## 4. Test Results

All test scenarios were executed against live Tomcat 10.1 and MySQL 8.0:

| Scenario | Status | Evidence |
| :--- | :--- | :--- |
| **1a. Farmer Registration** | **PASS** | HTTP 200: `"Farmer registered successfully!"` |
| **1b. Duplicate Farmer Registration** | **PASS** | HTTP 409 Conflict properly returned on duplicate ID |
| **1c. Farmer Login (Wrong Password)** | **PASS** | HTTP 401 Unauthorized properly returned |
| **1d. Farmer Login (Valid Credentials)** | **PASS** | HTTP 200: returned `farmer` object for `ZZTEST_F101` |
| **1e. Farmer 2 Registration** | **PASS** | HTTP 200: registered auxiliary farmer `ZZTEST_F102` |
| **2a. Buyer 1 Registration** | **PASS** | HTTP 200: `"Buyer registered successfully!"` |
| **2b. Duplicate Buyer Registration** | **PASS** | HTTP 409 Conflict properly returned on duplicate email |
| **2c. Buyer 2 Registration** | **PASS** | HTTP 200: registered auxiliary buyer `ZZTEST_Buyer2` |
| **2d. Buyer 1 Login (Valid Credentials)** | **PASS** | HTTP 200: returned `buyer` object for `zztest_buyer1@example.com` |
| **3a. Farmer Add Crop** | **PASS** | HTTP 200: Crop created with generated MySQL ID `#6` |
| **3b. Crop in Farmer "My Products"** | **PASS** | `GET /api/crops?farmerId=ZZTEST_F101` returns Crop `#6` |
| **4a. Crop in Buyer Marketplace** | **PASS** | `GET /api/crops` returns Crop `#6` in open marketplace listings |
| **4b. Marketplace Search & Filters** | **PASS** | `GET /api/crops?crop=Tomatoes` correctly filters Crop `#6` |
| **5a. Reject Negative Bid** | **PASS** | HTTP 400 Bad Request rejected negative amount `-5.0` |
| **5b. Reject Below-MSP Bid** | **PASS** | HTTP 400 Bad Request rejected offer ₹20.00 below reserve ₹25.00 |
| **5c. Valid First Bid** | **PASS** | HTTP 200: Bid `#1` placed at ₹27.00/kg by Buyer 1 |
| **5d. Reject Equal/Lower Bid** | **PASS** | HTTP 400 Bad Request rejected non-increasing bid of ₹27.00 |
| **6a. Two Buyers Bidding on Same Crop** | **PASS** | HTTP 200: Bid `#2` placed at ₹30.00/kg by Buyer 2 |
| **6b. Farmer Auction Room Bids View** | **PASS** | `GET /api/bids?cropId=6` returns both bids with leading bid ₹30.00/kg |
| **7. Farmer Rejects One Bid** | **PASS** | Bid `#3` (₹32.00) marked `REJECTED`; Crop `#6` remained `OPEN` |
| **8. Farmer Accepts Bid (Transaction)** | **PASS** | Transaction finalized: Crop `#6` marked `SOLD` at ₹30.00; Bid `#2` marked `ACCEPTED`; Bid `#1` marked `REJECTED` |
| **9a. Refuse Bid on Sold Crop** | **PASS** | HTTP 400 Bad Request: `"This auction is already closed or sold."` |
| **9b. Refuse Second Accept on Sold Crop**| **PASS** | HTTP 400 Bad Request: `"Auction is already sold or closed."` |
| **10. Cross-Farmer Unauthorized Accept** | **PASS** | HTTP 400 Bad Request: `"Unauthorized: You do not own this produce listing."` |
| **11. State Persistence on Page Reload** | **PASS** | MySQL verification: Crop `#6` status firmly persisted as `SOLD`, `accepted_bid_id = 2` |

---

## 5. Visual Check (Screenshots)

Captured using headless Chrome from `http://127.0.0.1:5500`:
- [index_desktop.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/index_desktop.png) (Landing / portal selector)
- [farmer_login_desktop.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/farmer_login_desktop.png) (Farmer portal sign in & registration)
- [buyer_desktop.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/buyer_desktop.png) (Buyer portal sign in & registration)
- [farmer_dashboard_desktop.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/farmer_dashboard_desktop.png) (Farmer dashboard with live MySQL produce lots)
- [buyer_dashboard_desktop.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/buyer_dashboard_desktop.png) (Buyer marketplace with search, filters, and bid history)
- [auction_room_desktop.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/auction_room_desktop.png) (Farmer live auction control desk with real-time bids & accept deal controls)
- [buyer_auction_room_desktop.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/buyer_auction_room_desktop.png) (Buyer live bidding floor with leading bid & winner announcement)
- [dashboard_mobile.png](file:///c:/Users/Shalu/Desktop/fairbid_antigravity/FairBid-Application/screenshots/dashboard_mobile.png) (Mobile viewport 375x812 responsive layout)

---

## 6. Unresolved Issues & Known Risks

1. **Plain-Text Passwords in Database:** Passwords in the `farmers` and `buyers` tables are stored as plain text per original schema design. Per Hard Rule 6, the storage scheme was not changed to avoid breaking existing users. For production readiness, cryptographic hashing (e.g. BCrypt / PBKDF2) is strongly recommended.
2. **Client-Side Session Tokenization:** Current authentication sets user details in `sessionStorage` upon successful API verification. Server-side ownership checks were added to verify that the farmer owns the target crop and that bids belong to that crop. For high-security environments, HTTP-only signed session cookies (JSESSIONID) or JWTs should be enforced.

---

## 7. Exact Run Instructions

### Prerequisites
- Java 21 LTS installed (`JAVA_HOME` set)
- Apache Maven 3.10+
- MySQL Server 8.0 running on port `3306` with database `fairbid_db`
- Apache Tomcat 10.1+ (installed at `C:\Users\Shalu\Downloads\apache-tomcat-10.1.61-windows-x64 (1)\apache-tomcat-10.1.61`)
- Node.js or Python (for local static frontend server)

### 1. Build Backend
```powershell
cd c:\Users\Shalu\Desktop\fairbid_antigravity\FairBid-Application\fairbid\backend
mvn clean package
```
Generates `target\fairbid-backend-1.0-SNAPSHOT.war`.

### 2. Deploy to Tomcat
```powershell
Copy-Item target\fairbid-backend-1.0-SNAPSHOT.war "C:\Users\Shalu\Downloads\apache-tomcat-10.1.61-windows-x64 (1)\apache-tomcat-10.1.61\webapps\fairbid-backend-1.0-SNAPSHOT.war" -Force
& "C:\Users\Shalu\Downloads\apache-tomcat-10.1.61-windows-x64 (1)\apache-tomcat-10.1.61\bin\catalina.bat" run
```
Tomcat starts on `http://localhost:8080/fairbid-backend-1.0-SNAPSHOT`.

### 3. Start Frontend Server
```powershell
cd c:\Users\Shalu\Desktop\fairbid_antigravity\FairBid-Application\fairbid\frontned
node server.js
```
Frontend is available at `http://127.0.0.1:5500/`.

---

## 8. Test Data Identifiers & Cleanup SQL

The automated test suite created test records with `ZZTEST` markers. These records do not interfere with any preexisting data.

- **Test Crop ID:** `6`
- **Test Bid IDs:** `1, 2, 3`
- **Test Farmer IDs:** `ZZTEST_F101`, `ZZTEST_F102`
- **Test Buyer Emails:** `zztest_buyer1@example.com`, `zztest_buyer2@example.com`

### Cleanup SQL (Provided for Reference — NOT Executed):
```sql
USE fairbid_db;
DELETE FROM bids WHERE id IN (1, 2, 3) OR buyer_email LIKE 'zztest_%';
DELETE FROM crops WHERE id = 6 OR farmer_id LIKE 'ZZTEST_%';
DELETE FROM buyers WHERE email LIKE 'zztest_%';
DELETE FROM farmers WHERE farmer_id LIKE 'ZZTEST_%';
```

---

## 9. Database Approval Status

- **Status:** **NOT NEEDED / NONE PENDING**
- **Justification:** The live `fairbid_db` schema already contained all columns needed to fulfill the entire workflow (`accepted_bid_id`, `agreed_price`, `sold_at`, `quality_grade`, `description`, `status`). No additive migration was required.
