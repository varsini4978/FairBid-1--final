# FairBid – Progress Tracker

**Current Phase**: Phase 3 → Phase 4 (Backend fixes + Frontend wire-up)  
**Time**: 2026-11-18 18:39 IST  
**Last action**: Phase 1 audit completed. Fixing critical bugs before first WAR build.

---

## Environment
| Tool | Version / Path |
|------|----------------|
| Java | 21.0.12.1 (Oracle) at `C:\Program Files\Java\jdk-21.0.12.1` |
| Maven | 3.10.0 at `C:\apache-maven-3.10.0` |
| Tomcat | 10.1.61 at `C:\apache-tomcat-10.1.61` |
| MySQL | 8.0 at `C:\Program Files\MySQL\MySQL Server 8.0` |
| MySQL password | `1023` (root) |
| DB name | `fairbid_db` |

---

## Credentials
| Email | Password | Role |
|-------|----------|------|
| farmer1@fairbid.com | Farmer@123 | Farmer |
| farmer2@fairbid.com | Farmer@123 | Farmer |
| farmer3@fairbid.com | Farmer@123 | Farmer |
| buyer1@fairbid.com | Buyer@123 | Buyer |
| buyer2@fairbid.com | Buyer@123 | Buyer |
| buyer3@fairbid.com | Buyer@123 | Buyer |
| buyer4@fairbid.com | Buyer@123 | Buyer |

---

## Phase 1 Audit

### Compile status
- `mvn compile` → BUILD SUCCESS (52 source files) ✅

### Database
- fairbid_db exists with all 8 tables ✅
- Sample data: 3 farmers, 4 buyers, 9 lots, 7 auctions, 12 bids, 3 results, 4 agent offers ✅
- BCrypt hashes in SQL verified against jBCrypt implementation ✅

### Critical Bugs Found (must fix before deploy)

| # | Bug | File | Fix |
|---|-----|------|-----|
| 1 | BidServlet uses non-spec wildcard `/api/auctions/*/bids` — Tomcat won't match | BidServlet.java | Move bid logic to AuctionServlet.doPost() |
| 2 | auction-result.js uses ES module imports against global api.js | auction-result.js | Rewrite as plain global script |
| 3 | unsold-recovery.js, agent-comparison.js same module problem | 2 files | Rewrite as plain global scripts |
| 4 | profile.js missing entirely | — | Create from scratch |
| 5 | ProduceServlet missing `agent-offer` and `result` sub-paths | ProduceServlet.java | Add handlers |
| 6 | AuctionResult has no `lotId` field — recovery links broken | AuctionResult.java, ResultDAO.java | Add lotId field |
| 7 | auction-result.js field names wrong (lot.minimumPrice etc.) | auction-result.js | Use r.cropName, r.finalPricePerUnit etc. |
| 8 | unsold-recovery.js sends `minimumPrice` but backend wants `newMinPrice` | unsold-recovery.js | Fix key name |
| 9 | unsold-recovery.js reopen sends no body, server requires `newEndTime` | unsold-recovery.js | Add newEndTime (now+24h default) |
| 10 | agent-comparison.js field names don't match PriceComparison JSON | agent-comparison.js | Fix to use expectedAuctionRevenueMin, netAuctionEarnings etc. |
| 11 | auction.js links to `?id=` but auction-result.js reads `?auctionId=` | auction.js | Fix to `?auctionId=` |
| 12 | unsold-recovery.js uses `offer.offerPricePerUnit` but model has `offeredPricePerUnit` | unsold-recovery.js | Fix field name |

---

## Exact Next Steps
1. Fix backend: AuctionResult.java (add lotId), ResultDAO.java (add lot_id to SQL), AuctionServlet (add bid handling), ProduceServlet (add agent-offer + result endpoints)
2. Fix frontend: Rewrite auction-result.js, unsold-recovery.js, agent-comparison.js as plain scripts; create profile.js; fix auction.js link
3. Run mvn clean package, deploy WAR to Tomcat
4. Load fairbid.sql into MySQL
5. Start Tomcat, test all workflows

---

## Key Paths
- Project: `c:\Users\varsi\OneDrive\Desktop\Java- Fairbid\`
- WAR output: `backend\target\FairBid.war`
- Tomcat webapps: `C:\apache-tomcat-10.1.61\webapps\`
- SQL script: `database\fairbid.sql`
- App URL: `http://localhost:8080/FairBid/`
- API base: `http://localhost:8080/FairBid/api`
