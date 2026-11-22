// ==========================================
// FAIRBID VOICE ACCESSIBILITY SUITE
// Text-to-Speech (TTS), Speech-to-Text (STT) & Voice Navigation
// ==========================================

const FairBidVoice = {
    isSpeaking: false,
    isListening: false,
    recognition: null,

    // 1. TEXT-TO-SPEECH (TTS)
    speak: function(text, customLang = null) {
        if (!('speechSynthesis' in window)) {
            console.warn('Speech synthesis not supported');
            return;
        }

        window.speechSynthesis.cancel(); // Stop any ongoing speech

        if (!text || text.trim() === '') return;

        const lang = customLang || (typeof getCurrentLanguage === 'function' ? getCurrentLanguage() : 'en');
        const utterance = new SpeechSynthesisUtterance(text);

        // Language mappings
        if (lang === 'ta') {
            utterance.lang = 'ta-IN';
        } else if (lang === 'hi') {
            utterance.lang = 'hi-IN';
        } else {
            utterance.lang = 'en-IN';
        }

        utterance.rate = 0.95; // Slightly slower for clarity
        utterance.pitch = 1.0;

        utterance.onstart = () => {
            FairBidVoice.isSpeaking = true;
            document.querySelectorAll('.tts-btn-active').forEach(b => b.classList.add('tts-playing'));
        };

        utterance.onend = () => {
            FairBidVoice.isSpeaking = false;
            document.querySelectorAll('.tts-btn-active').forEach(b => b.classList.remove('tts-playing'));
        };

        utterance.onerror = (e) => {
            FairBidVoice.isSpeaking = false;
            console.warn('Speech synthesis error:', e);
        };

        window.speechSynthesis.speak(utterance);
    },

    stopSpeech: function() {
        if ('speechSynthesis' in window) {
            window.speechSynthesis.cancel();
            FairBidVoice.isSpeaking = false;
            document.querySelectorAll('.tts-btn-active').forEach(b => b.classList.remove('tts-playing'));
        }
    },

    // 2. SPEECH-TO-TEXT (STT) FOR INPUT FIELDS
    listenToInput: function(targetInputId, statusIndicatorId = null, onComplete = null) {
        const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRec) {
            alert('Voice input is not supported in this browser. Please use Google Chrome or Microsoft Edge.');
            return;
        }

        const inputEl = document.getElementById(targetInputId);
        if (!inputEl) return;

        const recognition = new SpeechRec();
        const currentLang = typeof getCurrentLanguage === 'function' ? getCurrentLanguage() : 'en';

        if (currentLang === 'ta') recognition.lang = 'ta-IN';
        else if (currentLang === 'hi') recognition.lang = 'hi-IN';
        else recognition.lang = 'en-IN';

        recognition.interimResults = false;
        recognition.maxAlternatives = 1;

        if (statusIndicatorId) {
            const ind = document.getElementById(statusIndicatorId);
            if (ind) ind.textContent = '🎙️ Listening...';
        }

        recognition.onstart = () => {
            inputEl.placeholder = '🎙️ Speak now...';
        };

        recognition.onresult = (event) => {
            const transcript = event.results[0][0].transcript;
            inputEl.value = transcript;
            if (onComplete && typeof onComplete === 'function') {
                onComplete(transcript);
            }
        };

        recognition.onerror = (event) => {
            console.warn('Speech recognition error:', event.error);
            if (statusIndicatorId) {
                const ind = document.getElementById(statusIndicatorId);
                if (ind) ind.textContent = '';
            }
        };

        recognition.onend = () => {
            if (statusIndicatorId) {
                const ind = document.getElementById(statusIndicatorId);
                if (ind) ind.textContent = '';
            }
        };

        recognition.start();
    },

    // 3. VOICE NAVIGATION & COMMANDS
    toggleVoiceCommands: function() {
        const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;
        if (!SpeechRec) {
            alert(typeof t === 'function' ? t('voiceUnsupported') : 'Voice recognition is not supported in this browser.');
            return;
        }

        const btn = document.getElementById('voiceNavBtn');
        const modal = document.getElementById('voiceCommandModal');

        if (FairBidVoice.isListening) {
            if (FairBidVoice.recognition) FairBidVoice.recognition.stop();
            FairBidVoice.isListening = false;
            if (btn) btn.classList.remove('voice-listening');
            if (modal) modal.style.display = 'none';
            return;
        }

        const recognition = new SpeechRec();
        FairBidVoice.recognition = recognition;
        const currentLang = typeof getCurrentLanguage === 'function' ? getCurrentLanguage() : 'en';

        if (currentLang === 'ta') recognition.lang = 'ta-IN';
        else if (currentLang === 'hi') recognition.lang = 'hi-IN';
        else recognition.lang = 'en-IN';

        recognition.continuous = false;
        recognition.interimResults = true;

        if (modal) modal.style.display = 'flex';
        const statusText = document.getElementById('voiceStatusText');
        if (statusText) statusText.textContent = typeof t === 'function' ? t('listening') : 'Listening... Speak a command';

        recognition.onstart = () => {
            FairBidVoice.isListening = true;
            if (btn) btn.classList.add('voice-listening');
        };

        recognition.onresult = (event) => {
            let transcript = '';
            for (let i = event.resultIndex; i < event.results.length; ++i) {
                transcript += event.results[i][0].transcript;
            }
            if (statusText) statusText.textContent = `"${transcript}"`;

            if (event.results[0].isFinal) {
                FairBidVoice.executeCommand(transcript.trim());
            }
        };

        recognition.onerror = (e) => {
            console.warn('Voice command error:', e.error);
            FairBidVoice.isListening = false;
            if (btn) btn.classList.remove('voice-listening');
            if (modal) modal.style.display = 'none';
        };

        recognition.onend = () => {
            FairBidVoice.isListening = false;
            if (btn) btn.classList.remove('voice-listening');
            setTimeout(() => {
                if (modal) modal.style.display = 'none';
            }, 1200);
        };

        recognition.start();
    },

    executeCommand: function(command) {
        const cmd = command.toLowerCase();
        console.log('FairBid Voice Command:', cmd);

        // Command 1: Open dashboard
        if (cmd.includes('dashboard') || cmd.includes('டேஷ்போர்டு') || cmd.includes('डैशबोर्ड')) {
            const farmer = typeof getLoggedInFarmer === 'function' ? getLoggedInFarmer() : null;
            const buyer = typeof getLoggedInBuyer === 'function' ? getLoggedInBuyer() : null;
            if (farmer) window.location.href = 'farmer_dashboard.html';
            else if (buyer) window.location.href = 'buyer_dashboard.html';
            return;
        }

        // Command 2: Open marketplace
        if (cmd.includes('marketplace') || cmd.includes('சந்தை') || cmd.includes('बाजार') || cmd.includes('market')) {
            window.location.href = 'buyer_dashboard.html';
            return;
        }

        // Command 3: Show incoming bids
        if (cmd.includes('incoming') || cmd.includes('bids') || cmd.includes('ஏலம்') || cmd.includes('बोली')) {
            const bidsSec = document.querySelector('.incoming-bids') || document.getElementById('incomingBidsTableBody');
            if (bidsSec) {
                bidsSec.scrollIntoView({ behavior: 'smooth' });
                FairBidVoice.speak(typeof t === 'function' ? t('incomingBidsTitle') : 'Showing incoming buyer bids');
                return;
            }
        }

        // Command 4: Show notifications
        if (cmd.includes('notification') || cmd.includes('அறிவிப்பு') || cmd.includes('सूचना')) {
            const notifDropdown = document.getElementById('notifDropdown');
            if (notifDropdown) {
                notifDropdown.style.display = notifDropdown.style.display === 'block' ? 'none' : 'block';
                return;
            }
        }

        // Command 5: Show crops
        if (cmd.includes('crop') || cmd.includes('பயிர்') || cmd.includes('फसल')) {
            const cropsTable = document.getElementById('farmerCropsTableBody') || document.getElementById('marketplaceGrid');
            if (cropsTable) {
                cropsTable.scrollIntoView({ behavior: 'smooth' });
                return;
            }
        }

        // Command 6: Read latest bid
        if (cmd.includes('latest bid') || cmd.includes('read') || cmd.includes('படி') || cmd.includes('पढ़ो') || cmd.includes('सुनाओ')) {
            const firstBidRow = document.querySelector('#incomingBidsTableBody tr') || document.querySelector('#buyerBidsTableBody tr');
            if (firstBidRow) {
                const text = firstBidRow.innerText.replace(/\s+/g, ' ');
                FairBidVoice.speak(text);
                return;
            }
        }

        FairBidVoice.speak("Command recognized: " + command);
    }
};
