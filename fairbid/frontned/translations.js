// ==========================================
// FAIRBID CENTRAL MULTILINGUAL TRANSLATIONS
// English (en), Tamil (ta), Hindi (hi)
// ==========================================

const FAIRBID_TRANSLATIONS = {
    en: {
        // Navigation & Brand
        brandName: "FairBid",
        home: "Home",
        buyerPortal: "Buyer Portal",
        farmerPortal: "Farmer Portal",
        buyerMarketplace: "Buyer Marketplace",
        farmerDashboard: "Farmer Dashboard",
        logOut: "Log Out",
        language: "Language",
        roleBuyer: "Buyer",
        roleFarmer: "Farmer",
        
        // Identity Verification
        govtVerified: "Government Verified",
        demoVerified: "Demo Verified",
        pendingVerification: "Pending Verification",
        verifyIdentity: "Verify Identity",
        verifyAadhaar: "Aadhaar e-KYC Verification",
        verifyFarmerId: "AgriStack Farmer Verification",
        consentAadhaar: "I give my explicit consent to verify my identity via Aadhaar OTP authentication according to UIDAI compliance standards.",
        consentFarmer: "I give my explicit consent to verify my producer credentials with the Government AgriStack / State Farmer Registry.",
        demoModeNotice: "Demo Verification Mode is active for evaluation. Real UIDAI/AgriStack production access requires government API credentials.",
        aadhaarLast4: "Last 4 Digits of Aadhaar",
        enterOtp: "Enter 6-digit OTP",
        govtFarmerIdLabel: "Government Farmer Registry ID",
        btnVerifyNow: "Verify Identity Now",
        btnClose: "Close",
        
        // Notifications
        notifications: "Notifications",
        noNotifications: "No new notifications.",
        markAllRead: "Mark All as Read",
        newBidOn: "New bid on",
        belowMspAlert: "Below MSP",
        
        // Revise MSP
        reviseMsp: "Revise MSP",
        reviseMspTitle: "Revise Minimum Support Price (MSP)",
        currentMsp: "Current MSP",
        newMspLabel: "New MSP (₹/kg) *",
        btnUpdateMsp: "Update MSP",
        mspUpdateSuccess: "MSP updated successfully!",
        mspUpdateError: "Failed to update MSP. Please check ownership and price.",
        
        // Farmer Dashboard
        welcomeProducer: "Welcome, Producer",
        farmerSummary: "Manage your produce lots, monitor incoming competitive bids, and accept winning offers.",
        statTotalLots: "Total Lots Listed",
        statOpenAuctions: "Active Live Auctions",
        statSoldLots: "Sold Lots",
        newProduceListing: "New Produce Listing",
        listCropTitle: "List Crop for Competitive Auction",
        listCropSubtitle: "Specify minimum reserve price per kg. Bids will never drop below your threshold.",
        selectCrop: "Select Crop *",
        chooseCrop: "-- Choose Crop --",
        quantityKg: "Quantity (in Kg) *",
        reservePriceMsp: "Reserve Price / MSP (₹/Kg) *",
        qualityGrade: "Quality Grade *",
        locationState: "Location / State *",
        additionalNotes: "Additional Notes (Optional)",
        btnListProduce: "List Produce for Live Auction",
        myListedLots: "My Listed Produce Lots",
        myListedSubtitle: "Direct lots submitted by your farmer ID",
        refresh: "Refresh",
        cropLotCol: "Crop Lot",
        qtyCol: "Qty",
        mspCol: "MSP (₹/kg)",
        gradeRegionCol: "Grade & Region",
        statusCol: "Status",
        actionCol: "Action",
        liveAuction: "LIVE AUCTION",
        soldBadge: "SOLD",
        enterAuctionRoom: "Enter Auction Room →",
        reviewDeal: "Review Deal",
        
        // Incoming Bids
        incomingBidsTitle: "Incoming Live Buyer Bids",
        refreshBids: "Refresh Bids",
        buyerCol: "Buyer",
        bidAmountCol: "Bid (₹)",
        bidTimeCol: "Submitted (IST)",
        acceptAnyway: "Accept Anyway",
        acceptDeal: "Accept Deal",
        rejectBid: "Reject",
        
        // Buyer Dashboard
        produceMarketplaceHeading: "Produce Auctions Marketplace",
        produceMarketplaceSubtitle: "Discover direct farm produce lots listed across India. Submit competitive bids above reserve price.",
        statBuyerTotalBids: "Total Bids Placed",
        statBuyerPendingBids: "Pending Active Bids",
        statBuyerWonBids: "Auctions Won & Accepted",
        searchByCrop: "Search by Crop Name",
        filterByRegion: "Filter by Region / State",
        allRegions: "All Regions",
        applyFilters: "Apply Filters",
        reset: "Reset",
        liveLotsInAuction: "Live Produce Lots in Auction",
        myPlacedBidsTitle: "My Placed Bids & Purchase History",
        myPlacedBidsSubtitle: "Live record of bids associated with your registered email",
        bidIdCol: "Bid ID",
        produceLotColBuyer: "Produce Lot",
        yourOfferCol: "Your Offer (₹/kg)",
        lotStatusCol: "Lot Status",
        bidStatusCol: "Bid Status",
        
        // Voice & Accessibility
        voiceCommand: "Voice Command",
        listening: "Listening... Speak a command",
        voiceHelp: "Try: 'Show incoming bids', 'Show notifications', 'Read latest bid', 'Show my crops'",
        voiceUnsupported: "Voice recognition is not supported in this browser.",
        speechPlaying: "Speaking...",
        
        // Common Crops
        cropTomatoes: "Tomatoes",
        cropPotatoes: "Potatoes",
        cropOnions: "Onions",
        cropWheat: "Wheat",
        cropPaddyRice: "Paddy / Rice"
    },
    
    ta: {
        // Navigation & Brand
        brandName: "FairBid",
        home: "முகப்பு",
        buyerPortal: "வாங்குபவர் தளம்",
        farmerPortal: "விவசாயி தளம்",
        buyerMarketplace: "விவசாய சந்தை",
        farmerDashboard: "விவசாயி பலகை",
        logOut: "வெளியேறு",
        language: "மொழி",
        roleBuyer: "வாங்குபவர்",
        roleFarmer: "விவசாயி",
        
        // Identity Verification
        govtVerified: "அரசு சரிபார்க்கப்பட்டது",
        demoVerified: "மாதிரி சரிபார்ப்பு",
        pendingVerification: "சரிபார்ப்பு நிலுவையில்",
        verifyIdentity: "அடையாள சரிபார்ப்பு",
        verifyAadhaar: "ஆதார் e-KYC சரிபார்ப்பு",
        verifyFarmerId: "அக்ரிஸ்டாக் விவசாயி சரிபார்ப்பு",
        consentAadhaar: "UIDAI விதிமுறைகளின்படி எனது அடையாளத்தை ஆதார் OTP மூலம் சரிபார்க்க எனது முழு சம்மதத்தை அளிக்கிறேன்.",
        consentFarmer: "அரசு அக்ரிஸ்டாக் / மாநில விவசாயி பதிவேட்டின் மூலம் எனது விவரங்களை சரிபார்க்க சம்மதிக்கிறேன்.",
        demoModeNotice: "மதிப்பீட்டிற்காக மாதிரி சரிபார்ப்பு முறை இயக்கத்தில் உள்ளது. உண்மையான சேவைக்கு அரசு அனுமதி தேவை.",
        aadhaarLast4: "ஆதாரின் கடைசி 4 இலக்கங்கள்",
        enterOtp: "6 இலக்க OTP-யை உள்ளிடவும்",
        govtFarmerIdLabel: "அரசு விவசாயி அடையாள எண்",
        btnVerifyNow: "இப்போதே சரிபார்க்கவும்",
        btnClose: "மூடு",
        
        // Notifications
        notifications: "அறிவிப்புகள்",
        noNotifications: "புதிய அறிவிப்புகள் இல்லை.",
        markAllRead: "அனைத்தையும் படித்ததாகக் குறி",
        newBidOn: "புதிய ஏலக் கோரிக்கை:",
        belowMspAlert: "MSP-க்கு கீழ்",
        
        // Revise MSP
        reviseMsp: "MSP திருத்து",
        reviseMspTitle: "குறைந்தபட்ச ஆதரவு விலையை (MSP) மாற்றவும்",
        currentMsp: "தற்போதைய MSP",
        newMspLabel: "புதிய MSP (₹/கிலோ) *",
        btnUpdateMsp: "விலையை புதுப்பி",
        mspUpdateSuccess: "குறைந்தபட்ச ஆதரவு விலை வெற்றிகரமாக மாற்றப்பட்டது!",
        mspUpdateError: "MSP மாற்ற முடியவில்லை. உங்கள் பயிர் விவரங்களை சரிபார்க்கவும்.",
        
        // Farmer Dashboard
        welcomeProducer: "வணக்கம், விவசாய பெருமக்களே",
        farmerSummary: "உங்கள் விளைபொருட்களை நிர்வகிக்கவும், நேரலை ஏலங்களை கண்காணிக்கவும், சிறந்த விலையை ஏற்கவும்.",
        statTotalLots: "மொத்த விளைபொருட்கள்",
        statOpenAuctions: "நேரலை ஏலங்கள்",
        statSoldLots: "விற்கப்பட்டவை",
        newProduceListing: "புதிய விளைபொருள் பதிவு",
        listCropTitle: "நேரலை ஏலத்திற்கு பயிரைப் பதிவு செய்க",
        listCropSubtitle: "ஒரு கிலோவிற்கான குறைந்தபட்ச விலையைக் குறிப்பிடவும். ஏலம் உங்கள் விலையை விட குறையாது.",
        selectCrop: "பயிரைத் தேர்ந்தெடுக்கவும் *",
        chooseCrop: "-- பயிரைத் தேர்வு செய்க --",
        quantityKg: "அளவு (கிலோவில்) *",
        reservePriceMsp: "குறைந்தபட்ச ஆதரவு விலை / MSP (₹/கிலோ) *",
        qualityGrade: "தர வகை *",
        locationState: "பகுதி / மாநிலம் *",
        additionalNotes: "கூடுதல் குறிப்புகள் (விருப்பம்)",
        btnListProduce: "விளைபொருளை ஏலத்தில் இடுக",
        myListedLots: "எனது விளைபொருட்கள்",
        myListedSubtitle: "உங்கள் விவசாயி அடையாளத்தில் பதிவு செய்யப்பட்ட பயிர்கள்",
        refresh: "புதுப்பி",
        cropLotCol: "விளைபொருள்",
        qtyCol: "அளவு",
        mspCol: "MSP (₹/கிலோ)",
        gradeRegionCol: "தரம் & பகுதி",
        statusCol: "நிலை",
        actionCol: "செயல்பாடு",
        liveAuction: "நேரலை ஏலம்",
        soldBadge: "விற்கப்பட்டது",
        enterAuctionRoom: "ஏல அறைக்கு செல் →",
        reviewDeal: "ஒப்பந்தத்தை பார்",
        
        // Incoming Bids
        incomingBidsTitle: "நேரலை வாங்குபவர் ஏலக் கோரிக்கைகள்",
        refreshBids: "ஏலங்களைப் புதுப்பி",
        buyerCol: "வாங்குபவர்",
        bidAmountCol: "ஏல விலை (₹)",
        bidTimeCol: "சமர்ப்பிக்கப்பட்ட நேரம் (IST)",
        acceptAnyway: "இருப்பினும் ஏல்",
        acceptDeal: "ஏலத்தை ஏற்றுக்கொள்",
        rejectBid: "நிராகரி",
        
        // Buyer Dashboard
        produceMarketplaceHeading: "விவசாய விளைபொருட்கள் சந்தை",
        produceMarketplaceSubtitle: "இந்தியா முழுவதிலும் உள்ள விவசாயிகளின் புதிய விளைபொருட்களை நேரடியாக வாங்குங்கள்.",
        statBuyerTotalBids: "மொத்த ஏலங்கள்",
        statBuyerPendingBids: "செயலில் உள்ள ஏலங்கள்",
        statBuyerWonBids: "வென்ற ஏலங்கள்",
        searchByCrop: "பயிரின் பெயரைத் தேடுக",
        filterByRegion: "மாநிலம் வாரியாக வடிகட்டுக",
        allRegions: "அனைத்து பகுதிகள்",
        applyFilters: "வடிகட்டு",
        reset: "மீட்டமை",
        liveLotsInAuction: "ஏலத்தில் உள்ள நேரலை பயிர்கள்",
        myPlacedBidsTitle: "எனது ஏலங்கள் & கொள்முதல் வரலாறு",
        myPlacedBidsSubtitle: "உங்கள் மின்னஞ்சலில் வைக்கப்பட்ட நேரலை ஏல விவரங்கள்",
        bidIdCol: "ஏல எண்",
        produceLotColBuyer: "விளைபொருள்",
        yourOfferCol: "உங்கள் விலை (₹/கிலோ)",
        lotStatusCol: "பயிர் நிலை",
        bidStatusCol: "ஏல நிலை",
        
        // Voice & Accessibility
        voiceCommand: "குரல் கட்டளை",
        listening: "கேட்கிறது... கட்டளையை பேசவும்",
        voiceHelp: "'ஏலங்களைக் காட்டு', 'அறிவிப்புகளைக் காட்டு', 'பயிர்களைக் காட்டு' என பேசலாம்",
        voiceUnsupported: "இந்த உலாவியில் குரல் அறிதல் ஆதரிக்கப்படவில்லை.",
        speechPlaying: "பேசுகிறது...",
        
        // Common Crops
        cropTomatoes: "தக்காளி",
        cropPotatoes: "உருளைக்கிழங்கு",
        cropOnions: "வெங்காயம்",
        cropWheat: "கோதுமை",
        cropPaddyRice: "நெல் / அரிசி"
    },
    
    hi: {
        // Navigation & Brand
        brandName: "FairBid",
        home: "होम",
        buyerPortal: "खरीदार पोर्टल",
        farmerPortal: "किसान पोर्टल",
        buyerMarketplace: "कृषि बाज़ार",
        farmerDashboard: "किसान डैशबोर्ड",
        logOut: "लॉग आउट",
        language: "भाषा",
        roleBuyer: "खरीदार",
        roleFarmer: "किसान",
        
        // Identity Verification
        govtVerified: "सरकार द्वारा सत्यापित",
        demoVerified: "डेमो सत्यापित",
        pendingVerification: "सत्यापन लंबित",
        verifyIdentity: "पहचान सत्यापित करें",
        verifyAadhaar: "आधार e-KYC सत्यापन",
        verifyFarmerId: "एग्रीस्टैक किसान सत्यापन",
        consentAadhaar: "मैं UIDAI नियमों के अनुसार आधार OTP के माध्यम से अपनी पहचान सत्यापित करने की पूर्ण सहमति देता हूँ।",
        consentFarmer: "मैं सरकारी एग्रीस्टैक / राज्य किसान रजिस्ट्री के माध्यम से अपने विवरण सत्यापित करने की सहमति देता हूँ।",
        demoModeNotice: "मूल्यांकन के लिए डेमो सत्यापन मोड सक्रिय है। वास्तविक सेवा के लिए सरकारी क्रेडेंशियल आवश्यक हैं।",
        aadhaarLast4: "आधार के अंतिम 4 अंक",
        enterOtp: "6-अंकों का OTP दर्ज करें",
        govtFarmerIdLabel: "सरकारी किसान आईडी",
        btnVerifyNow: "अभी सत्यापित करें",
        btnClose: "बंद करें",
        
        // Notifications
        notifications: "सूचनाएं",
        noNotifications: "कोई नई सूचना नहीं है।",
        markAllRead: "सभी पढ़ी गईं मार्क करें",
        newBidOn: "नई बोली:",
        belowMspAlert: "MSP से कम",
        
        // Revise MSP
        reviseMsp: "MSP संशोधित करें",
        reviseMspTitle: "न्यूनतम समर्थन मूल्य (MSP) संशोधित करें",
        currentMsp: "वर्तमान MSP",
        newMspLabel: "नया MSP (₹/किग्रा) *",
        btnUpdateMsp: "MSP अपडेट करें",
        mspUpdateSuccess: "MSP सफलतापूर्वक अपडेट किया गया!",
        mspUpdateError: "MSP अपडेट करने में विफल। कृपया स्वामित्व और मूल्य जांचें।",
        
        // Farmer Dashboard
        welcomeProducer: "स्वागत है, कृषक बंधु",
        farmerSummary: "अपनी फसल लॉट प्रबंधित करें, लाइव बोलियों की निगरानी करें और सर्वोत्तम मूल्य स्वीकार करें।",
        statTotalLots: "कुल सूचीबद्ध फसलें",
        statOpenAuctions: "सक्रिय लाइव नीलामी",
        statSoldLots: "बिक चुकी फसलें",
        newProduceListing: "नई फसल सूची",
        listCropTitle: "प्रतिस्पर्धी नीलामी के लिए फसल सूचीबद्ध करें",
        listCropSubtitle: "प्रति किलो न्यूनतम आरक्षित मूल्य दर्ज करें। बोलियां आपकी सीमा से नीचे नहीं जाएंगी।",
        selectCrop: "फसल चुनें *",
        chooseCrop: "-- फसल का चयन करें --",
        quantityKg: "मात्रा (किग्रा में) *",
        reservePriceMsp: "आरक्षित मूल्य / MSP (₹/किग्रा) *",
        qualityGrade: "गुणवत्ता ग्रेड *",
        locationState: "स्थान / राज्य *",
        additionalNotes: "अतिरिक्त विवरण (वैकल्पिक)",
        btnListProduce: "लाइव नीलामी के लिए फसल दर्ज करें",
        myListedLots: "मेरी सूचीबद्ध फसलें",
        myListedSubtitle: "आपकी किसान आईडी द्वारा दर्ज की गई फसलें",
        refresh: "रिफ्रेश",
        cropLotCol: "फसल लॉट",
        qtyCol: "मात्रा",
        mspCol: "MSP (₹/किग्रा)",
        gradeRegionCol: "ग्रेड व क्षेत्र",
        statusCol: "स्थिति",
        actionCol: "कार्य",
        liveAuction: "लाइव नीलामी",
        soldBadge: "बिक गया",
        enterAuctionRoom: "नीलामी कक्ष में जाएं →",
        reviewDeal: "सौदा देखें",
        
        // Incoming Bids
        incomingBidsTitle: "आने वाली लाइव खरीदार बोलियां",
        refreshBids: "बोलियां रिफ्रेश करें",
        buyerCol: "खरीदार",
        bidAmountCol: "बोली (₹)",
        bidTimeCol: "समय (IST)",
        acceptAnyway: "फिर भी स्वीकार करें",
        acceptDeal: "सौदा स्वीकार करें",
        rejectBid: "अस्वीकार करें",
        
        // Buyer Dashboard
        produceMarketplaceHeading: "कृषि उत्पाद नीलामी बाज़ार",
        produceMarketplaceSubtitle: "सीधे भारतीय किसानों से ताज़ा उपज खरीदें। आरक्षित मूल्य पर प्रतिस्पर्धी बोलियां लगाएं।",
        statBuyerTotalBids: "कुल लगाई गई बोलियां",
        statBuyerPendingBids: "सक्रिय लंबित बोलियां",
        statBuyerWonBids: "जीती हुई नीलामियां",
        searchByCrop: "फसल के नाम से खोजें",
        filterByRegion: "राज्य / क्षेत्र द्वारा फ़िल्टर करें",
        allRegions: "सभी क्षेत्र",
        applyFilters: "फ़िल्टर लागू करें",
        reset: "रीसेट",
        liveLotsInAuction: "नीलामी में लाइव फसल लॉट",
        myPlacedBidsTitle: "मेरी लगाई गई बोलियां व इतिहास",
        myPlacedBidsSubtitle: "आपके पंजीकृत ईमेल से संबद्ध बोलियों का लाइव रिकॉर्ड",
        bidIdCol: "बोली आईडी",
        produceLotColBuyer: "फसल",
        yourOfferCol: "आपकी पेशकश (₹/किग्रा)",
        lotStatusCol: "फसल स्थिति",
        bidStatusCol: "बोली स्थिति",
        
        // Voice & Accessibility
        voiceCommand: "आवाज़ आदेश",
        listening: "सुन रहे हैं... बोलिए",
        voiceHelp: "'आने वाली बोलियां दिखाओ', 'सूचनाएं दिखाओ', 'मेरी फसलें दिखाओ' बोल सकते हैं",
        voiceUnsupported: "इस ब्राउज़र में वॉइस पहचान समर्थित नहीं है।",
        speechPlaying: "बोल रहे हैं...",
        
        // Common Crops
        cropTomatoes: "टमाटर",
        cropPotatoes: "आलू",
        cropOnions: "प्याज",
        cropWheat: "गेहूं",
        cropPaddyRice: "धान / चावल"
    }
};

// ==========================================
// TRANSLATION HELPER FUNCTIONS
// ==========================================

function getCurrentLanguage() {
    return localStorage.getItem('fairbid_lang') || 'en';
}

function setLanguage(lang) {
    if (!FAIRBID_TRANSLATIONS[lang]) lang = 'en';
    localStorage.setItem('fairbid_lang', lang);
    applyTranslations(lang);
    
    // Dispatch custom event so dynamic tables/components re-render text
    window.dispatchEvent(new CustomEvent('languageChanged', { detail: { lang } }));
}

function t(key, fallback = '') {
    const lang = getCurrentLanguage();
    const dict = FAIRBID_TRANSLATIONS[lang] || FAIRBID_TRANSLATIONS.en;
    if (dict && dict[key] != null) return dict[key];
    const enDict = FAIRBID_TRANSLATIONS.en;
    if (enDict && enDict[key] != null) return enDict[key];
    return fallback || key;
}

function applyTranslations(lang) {
    if (!lang) lang = getCurrentLanguage();
    const dict = FAIRBID_TRANSLATIONS[lang] || FAIRBID_TRANSLATIONS.en;

    document.querySelectorAll('[data-i18n]').forEach(el => {
        const key = el.getAttribute('data-i18n');
        if (dict[key]) {
            if (el.tagName === 'INPUT' && (el.type === 'text' || el.type === 'number' || el.type === 'search' || el.type === 'password')) {
                el.placeholder = dict[key];
            } else {
                el.textContent = dict[key];
            }
        }
    });

    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => {
        const key = el.getAttribute('data-i18n-placeholder');
        if (dict[key]) {
            el.placeholder = dict[key];
        }
    });

    // Update language selector active value if present
    const selector = document.getElementById('fairbidLangSelect');
    if (selector && selector.value !== lang) {
        selector.value = lang;
    }
}

// Auto-initialize when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    applyTranslations(getCurrentLanguage());
});
