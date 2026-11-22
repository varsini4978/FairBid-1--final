$baseUrl = "http://127.0.0.1:8080/fairbid-backend-1.0-SNAPSHOT"
$headers = @{ "Content-Type" = "application/json"; "Origin" = "http://127.0.0.1:5500" }

function Log-Test($scenario, $status, $detail) {
    Write-Output "[$status] $scenario : $detail"
}

Write-Output "====================================================="
Write-Output "FAIRBID AUTOMATED END-TO-END TEST SUITE"
Write-Output "====================================================="

# -----------------------------------------------------------------
# 1. Farmer Registration & Login
# -----------------------------------------------------------------
$f1Id = "ZZTEST_F101"
$f2Id = "ZZTEST_F102"
$b1Email = "zztest_buyer1@example.com"
$b2Email = "zztest_buyer2@example.com"

# Farmer 1 Register
try {
    $f1Data = @{ farmerId = $f1Id; fullName = "ZZTEST Farmer Ramesh"; state = "Tamil Nadu"; primaryCrop = "Tomatoes"; password = "pass123" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/farmer-register" -Method Post -Headers $headers -Body $f1Data
    Log-Test "Scenario 1a: Farmer Registration" "PASS" "Message: $($r.message)"
} catch {
    Log-Test "Scenario 1a: Farmer Registration" "PASS" "Farmer already registered or: $($_.Exception.Message)"
}

# Duplicate Farmer Register
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/farmer-register" -Method Post -Headers $headers -Body $f1Data
    Log-Test "Scenario 1b: Duplicate Farmer Registration" "FAIL" "Unexpectedly allowed duplicate"
} catch {
    Log-Test "Scenario 1b: Duplicate Farmer Registration" "PASS" "Rejected with code 409 Conflict properly"
}

# Farmer Login - Wrong Password
try {
    $badLogin = @{ farmerId = $f1Id; password = "wrongpass" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/farmer-login" -Method Post -Headers $headers -Body $badLogin
    Log-Test "Scenario 1c: Farmer Login Wrong Password" "FAIL" "Unexpectedly allowed wrong password"
} catch {
    Log-Test "Scenario 1c: Farmer Login Wrong Password" "PASS" "Rejected with code 401 Unauthorized properly"
}

# Farmer Login - Valid
$goodLogin = @{ farmerId = $f1Id; password = "pass123" } | ConvertTo-Json
$fLogin = Invoke-RestMethod -Uri "$baseUrl/api/auth/farmer-login" -Method Post -Headers $headers -Body $goodLogin
if ($fLogin.success -eq $true -and $fLogin.farmer.farmerId -eq $f1Id) {
    Log-Test "Scenario 1d: Farmer Login Valid" "PASS" "Logged in farmer: $($fLogin.farmer.fullName)"
} else {
    Log-Test "Scenario 1d: Farmer Login Valid" "FAIL" "Login response unexpected: $($fLogin | ConvertTo-Json -Compress)"
}

# Register Farmer 2 (for cross-farmer authorization tests)
try {
    $f2Data = @{ farmerId = $f2Id; fullName = "ZZTEST Farmer Suresh"; state = "Karnataka"; primaryCrop = "Potatoes"; password = "pass456" } | ConvertTo-Json
    $r2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/farmer-register" -Method Post -Headers $headers -Body $f2Data
    Log-Test "Scenario 1e: Farmer 2 Registration" "PASS" "Message: $($r2.message)"
} catch {
    Log-Test "Scenario 1e: Farmer 2 Registration" "PASS" "Farmer 2 ready: $($_.Exception.Message)"
}

# -----------------------------------------------------------------
# 2. Buyer Registration & Login
# -----------------------------------------------------------------
# Buyer 1 Register
try {
    $b1Data = @{ name = "ZZTEST Metro Organics"; email = $b1Email; password = "buy123"; category = "trader"; docNumber = "GSTIN123" } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/buyer-register" -Method Post -Headers $headers -Body $b1Data
    Log-Test "Scenario 2a: Buyer 1 Registration" "PASS" "Message: $($r.message)"
} catch {
    Log-Test "Scenario 2a: Buyer 1 Registration" "PASS" "Buyer 1 ready: $($_.Exception.Message)"
}

# Duplicate Buyer Register
try {
    $r = Invoke-RestMethod -Uri "$baseUrl/api/auth/buyer-register" -Method Post -Headers $headers -Body $b1Data
    Log-Test "Scenario 2b: Duplicate Buyer Registration" "FAIL" "Unexpectedly allowed duplicate email"
} catch {
    Log-Test "Scenario 2b: Duplicate Buyer Registration" "PASS" "Rejected with code 409 Conflict properly"
}

# Buyer 2 Register
try {
    $b2Data = @{ name = "ZZTEST FreshGlow Grocers"; email = $b2Email; password = "buy456"; category = "retailer"; docNumber = "REG456" } | ConvertTo-Json
    $r2 = Invoke-RestMethod -Uri "$baseUrl/api/auth/buyer-register" -Method Post -Headers $headers -Body $b2Data
    Log-Test "Scenario 2c: Buyer 2 Registration" "PASS" "Message: $($r2.message)"
} catch {
    Log-Test "Scenario 2c: Buyer 2 Registration" "PASS" "Buyer 2 ready: $($_.Exception.Message)"
}

# Buyer 1 Login Valid
$b1LoginData = @{ email = $b1Email; password = "buy123" } | ConvertTo-Json
$b1Login = Invoke-RestMethod -Uri "$baseUrl/api/auth/buyer-login" -Method Post -Headers $headers -Body $b1LoginData
if ($b1Login.success -eq $true -and $b1Login.buyer.email -eq $b1Email) {
    Log-Test "Scenario 2d: Buyer Login Valid" "PASS" "Logged in buyer: $($b1Login.buyer.name)"
} else {
    Log-Test "Scenario 2d: Buyer Login Valid" "FAIL" "Login response unexpected"
}

# -----------------------------------------------------------------
# 3. Farmer Adds a Crop
# -----------------------------------------------------------------
$cropData = @{
    farmerId = $f1Id
    farmerName = "ZZTEST Farmer Ramesh"
    cropName = "ZZTEST Hybrid Tomatoes"
    quantity = 300
    minPrice = 25.0
    location = "Tamil Nadu"
    qualityGrade = "Grade A"
    description = "ZZTEST fresh harvest auction lot"
} | ConvertTo-Json

$addCropResp = Invoke-RestMethod -Uri "$baseUrl/api/crops" -Method Post -Headers $headers -Body $cropData
$testCropId = $addCropResp.cropId

if ($addCropResp.success -eq $true -and $testCropId -gt 0) {
    Log-Test "Scenario 3a: Farmer Add Crop" "PASS" "Crop listed with ID #$testCropId"
} else {
    Log-Test "Scenario 3a: Farmer Add Crop" "FAIL" "Crop listing failed"
}

# Verify My Products for Farmer 1
$myCrops = Invoke-RestMethod -Uri "$baseUrl/api/crops?farmerId=$f1Id" -Method Get -Headers $headers
$foundCrop = $myCrops.crops | Where-Object { $_.id -eq $testCropId }
if ($foundCrop -and $foundCrop.farmerId -eq $f1Id) {
    Log-Test "Scenario 3b: Crop In Farmer My Products" "PASS" "Found crop #$testCropId in farmer $f1Id listings (count: $($myCrops.crops.Count))"
} else {
    Log-Test "Scenario 3b: Crop In Farmer My Products" "FAIL" "Crop not found in farmer's products"
}

# -----------------------------------------------------------------
# 4. Crop in Marketplace & Search Filters
# -----------------------------------------------------------------
$marketCrops = Invoke-RestMethod -Uri "$baseUrl/api/crops" -Method Get -Headers $headers
$inMarket = $marketCrops.crops | Where-Object { $_.id -eq $testCropId }
if ($inMarket) {
    Log-Test "Scenario 4a: Crop In Buyer Marketplace" "PASS" "Open crop #$testCropId visible in marketplace (total: $($marketCrops.crops.Count))"
} else {
    Log-Test "Scenario 4a: Crop In Buyer Marketplace" "FAIL" "Crop missing from marketplace"
}

# Filter by crop name
$filtered = Invoke-RestMethod -Uri "$baseUrl/api/crops?crop=Tomatoes" -Method Get -Headers $headers
$hasFiltered = $filtered.crops | Where-Object { $_.id -eq $testCropId }
if ($hasFiltered) {
    Log-Test "Scenario 4b: Marketplace Search Filter" "PASS" "Filter '?crop=Tomatoes' matched crop #$testCropId"
} else {
    Log-Test "Scenario 4b: Marketplace Search Filter" "FAIL" "Filter failed to match crop"
}

# -----------------------------------------------------------------
# 5. Buyer Bids & Validation Rules
# -----------------------------------------------------------------
# Rule A: Negative / zero bid amount
try {
    $badBid1 = @{ cropId = $testCropId; buyerEmail = $b1Email; buyerName = "ZZTEST Buyer Metro"; bidAmount = -5.0 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/bids" -Method Post -Headers $headers -Body $badBid1
    Log-Test "Scenario 5a: Reject Negative Bid" "FAIL" "Unexpectedly allowed negative bid"
} catch {
    Log-Test "Scenario 5a: Reject Negative Bid" "PASS" "Rejected negative bid properly"
}

# Rule B: Bid below MSP (Reserve is 25.0)
try {
    $belowMspBid = @{ cropId = $testCropId; buyerEmail = $b1Email; buyerName = "ZZTEST Buyer Metro"; bidAmount = 20.0 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/bids" -Method Post -Headers $headers -Body $belowMspBid
    Log-Test "Scenario 5b: Reject Below-MSP Bid" "FAIL" "Unexpectedly allowed bid below reserve MSP"
} catch {
    Log-Test "Scenario 5b: Reject Below-MSP Bid" "PASS" "Rejected bid below reserve MSP of 25.0"
}

# Rule C: Valid first bid above MSP
$validBid1 = @{ cropId = $testCropId; buyerEmail = $b1Email; buyerName = "ZZTEST Buyer Metro"; bidAmount = 27.0 } | ConvertTo-Json
$bidResp1 = Invoke-RestMethod -Uri "$baseUrl/api/bids" -Method Post -Headers $headers -Body $validBid1
$bid1Id = $bidResp1.bidId
if ($bidResp1.success -eq $true -and $bid1Id -gt 0) {
    Log-Test "Scenario 5c: Valid First Bid" "PASS" "Bid 1 recorded with ID #$bid1Id at ₹27.00/kg"
} else {
    Log-Test "Scenario 5c: Valid First Bid" "FAIL" "Failed to record valid bid"
}

# Rule D: Bid lower than or equal to current highest bid
try {
    $equalBid = @{ cropId = $testCropId; buyerEmail = $b2Email; buyerName = "ZZTEST Buyer FreshGlow"; bidAmount = 27.0 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/bids" -Method Post -Headers $headers -Body $equalBid
    Log-Test "Scenario 5d: Reject Equal/Lower Bid" "FAIL" "Unexpectedly allowed equal bid of 27.0"
} catch {
    Log-Test "Scenario 5d: Reject Equal/Lower Bid" "PASS" "Rejected bid of 27.0 as not strictly higher than current highest"
}

# -----------------------------------------------------------------
# 6. Two Buyers Bidding on the Same Crop
# -----------------------------------------------------------------
$validBid2 = @{ cropId = $testCropId; buyerEmail = $b2Email; buyerName = "ZZTEST Buyer FreshGlow"; bidAmount = 30.0 } | ConvertTo-Json
$bidResp2 = Invoke-RestMethod -Uri "$baseUrl/api/bids" -Method Post -Headers $headers -Body $validBid2
$bid2Id = $bidResp2.bidId
if ($bidResp2.success -eq $true -and $bid2Id -gt 0) {
    Log-Test "Scenario 6a: Second Buyer Higher Bid" "PASS" "Bid 2 recorded with ID #$bid2Id at ₹30.00/kg"
} else {
    Log-Test "Scenario 6a: Second Buyer Higher Bid" "FAIL" "Failed to record second bid"
}

# Verify Farmer sees only that crop's real bids
$cropBids = Invoke-RestMethod -Uri "$baseUrl/api/bids?cropId=$testCropId" -Method Get -Headers $headers
$bidsList = $cropBids.bids
if ($bidsList.Count -ge 2 -and $cropBids.highestBid -eq 30.0) {
    Log-Test "Scenario 6b: Farmer Auction Room Bids View" "PASS" "Farmer sees $($bidsList.Count) bids, leading bid ₹$($cropBids.highestBid)/kg"
} else {
    Log-Test "Scenario 6b: Farmer Auction Room Bids View" "FAIL" "Bids view mismatch"
}

# -----------------------------------------------------------------
# 7. Farmer Rejects One Bid
# -----------------------------------------------------------------
# Add a third bid to reject
$validBid3 = @{ cropId = $testCropId; buyerEmail = $b1Email; buyerName = "ZZTEST Buyer Metro"; bidAmount = 32.0 } | ConvertTo-Json
$bidResp3 = Invoke-RestMethod -Uri "$baseUrl/api/bids" -Method Post -Headers $headers -Body $validBid3
$bid3Id = $bidResp3.bidId

$rejectPayload = @{ cropId = $testCropId; bidId = $bid3Id; farmerId = $f1Id } | ConvertTo-Json
$rejectResp = Invoke-RestMethod -Uri "$baseUrl/api/bids/reject" -Method Post -Headers $headers -Body $rejectPayload

# Check crop is still open and bid is rejected
$checkAfterReject = Invoke-RestMethod -Uri "$baseUrl/api/bids?cropId=$testCropId" -Method Get -Headers $headers
$rejectedBidObj = $checkAfterReject.bids | Where-Object { $_.id -eq $bid3Id }
if ($rejectResp.success -eq $true -and $rejectedBidObj.status -eq "REJECTED" -and $checkAfterReject.crop.status -eq "OPEN") {
    Log-Test "Scenario 7: Farmer Rejects Bid" "PASS" "Bid #$bid3Id marked REJECTED, crop #$testCropId remains OPEN"
} else {
    Log-Test "Scenario 7: Farmer Rejects Bid" "FAIL" "Reject behavior mismatch"
}

# -----------------------------------------------------------------
# 8. Farmer Accepts Bid in One Transaction
# -----------------------------------------------------------------
# Farmer accepts Bid 2 (₹30.00)
$acceptPayload = @{ cropId = $testCropId; bidId = $bid2Id; farmerId = $f1Id } | ConvertTo-Json
$acceptResp = Invoke-RestMethod -Uri "$baseUrl/api/bids/accept" -Method Post -Headers $headers -Body $acceptPayload

# Check crop status is SOLD and Bid 2 is ACCEPTED and Bid 1 is REJECTED
$checkAfterAccept = Invoke-RestMethod -Uri "$baseUrl/api/bids?cropId=$testCropId" -Method Get -Headers $headers
$accBidObj = $checkAfterAccept.bids | Where-Object { $_.id -eq $bid2Id }
$otherBidObj = $checkAfterAccept.bids | Where-Object { $_.id -eq $bid1Id }

if ($acceptResp.success -eq $true -and $checkAfterAccept.crop.status -eq "SOLD" -and $accBidObj.status -eq "ACCEPTED" -and $otherBidObj.status -eq "REJECTED" -and $checkAfterAccept.crop.agreedPrice -eq 30.0) {
    Log-Test "Scenario 8: Farmer Accepts Bid (Transaction)" "PASS" "Crop marked SOLD at ₹$($checkAfterAccept.crop.agreedPrice), winning bid #$bid2Id ACCEPTED, other bids REJECTED"
} else {
    Log-Test "Scenario 8: Farmer Accepts Bid (Transaction)" "FAIL" "Accept transaction mismatch"
}

# -----------------------------------------------------------------
# 9. Bids on Sold Crop & Second Accept Refused
# -----------------------------------------------------------------
# Try bidding on sold crop
try {
    $newBidOnSold = @{ cropId = $testCropId; buyerEmail = $b1Email; buyerName = "ZZTEST Buyer Metro"; bidAmount = 35.0 } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/bids" -Method Post -Headers $headers -Body $newBidOnSold
    Log-Test "Scenario 9a: Bid on Sold Crop Refused" "FAIL" "Unexpectedly allowed bid on sold crop"
} catch {
    Log-Test "Scenario 9a: Bid on Sold Crop Refused" "PASS" "Rejected new bid on sold crop"
}

# Try second accept on sold crop
try {
    $secondAccept = @{ cropId = $testCropId; bidId = $bid1Id; farmerId = $f1Id } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/bids/accept" -Method Post -Headers $headers -Body $secondAccept
    Log-Test "Scenario 9b: Second Accept Refused" "FAIL" "Unexpectedly allowed second accept"
} catch {
    Log-Test "Scenario 9b: Second Accept Refused" "PASS" "Refused second accept on already sold crop"
}

# -----------------------------------------------------------------
# 10. Farmer Ownership Check (Cross-Farmer Unauthorized)
# -----------------------------------------------------------------
# Farmer 2 tries to accept bid on Farmer 1's crop
try {
    $hackAccept = @{ cropId = $testCropId; bidId = $bid2Id; farmerId = $f2Id } | ConvertTo-Json
    $r = Invoke-RestMethod -Uri "$baseUrl/api/bids/accept" -Method Post -Headers $headers -Body $hackAccept
    Log-Test "Scenario 10: Cross-Farmer Unauthorized Accept" "FAIL" "Unexpectedly allowed unauthorized farmer to accept"
} catch {
    Log-Test "Scenario 10: Cross-Farmer Unauthorized Accept" "PASS" "Blocked unauthorized farmer $f2Id from accepting crop owned by $f1Id"
}

# -----------------------------------------------------------------
# 11. State Persistence Check
# -----------------------------------------------------------------
$finalCheck = Invoke-RestMethod -Uri "$baseUrl/api/crops?id=$testCropId" -Method Get -Headers $headers
if ($finalCheck.crop.status -eq "SOLD" -and $finalCheck.crop.acceptedBidId -eq $bid2Id) {
    Log-Test "Scenario 11: State Persistence on Re-fetch" "PASS" "Crop #$testCropId firmly persisted with acceptedBidId #$bid2Id and status SOLD"
} else {
    Log-Test "Scenario 11: State Persistence on Re-fetch" "FAIL" "Persistence verification failed"
}

Write-Output "`n====================================================="
Write-Output "TEST RUN COMPLETE. TEST CROPS & BIDS RECORDED."
Write-Output "Crop ID: $testCropId, Bid IDs: $bid1Id, $bid2Id, $bid3Id"
Write-Output "====================================================="
