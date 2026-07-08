// Add this variable at the top of the file
let isDownloadInProgress = false;
function generateSRTContent(ayah, startTime, endTime) {
    const secondsToTime = (secs) => {
        const hours = Math.floor(secs / 3600).toString().padStart(2, '0');
        const minutes = Math.floor((secs % 3600) / 60).toString().padStart(2, '0');
        const seconds = Math.floor(secs % 60).toString().padStart(2, '0');
        const milliseconds = Math.floor((secs % 1) * 1000).toString().padStart(3, '0');
        return `${hours}:${minutes}:${seconds},${milliseconds}`;
    };
    // Handle potential undefined values
    const ayahNumber = ayah && ayah.numberInSurah ? ayah.numberInSurah : '1';
    const ayahText = ayah && ayah.text ? ayah.text.trim() : '';
    return `${ayahNumber}\n${secondsToTime(startTime)} --> ${secondsToTime(endTime)}\n${ayahText}\n\n`;
}
const surahSelect = document.getElementById('surah');
const ayahStartSelect = document.getElementById('ayahStart');
const ayahEndSelect = document.getElementById('ayahEnd');
const generateBtn = document.getElementById('generateBtn');
const imageContainer = document.getElementById('imageContainer');
const previewContainer = document.getElementById('previewContainer');
const previewImage = document.getElementById('previewImage');
const fontSelect = document.getElementById('fontSelect');
const reciterDropdown = document.getElementById('reciter');
const downloadAllAudiosBtn = document.getElementById('downloadAllAudiosBtn');
const downloadCompleteSurahBtn = document.getElementById('downloadCompleteSurahBtn');
const downloadAllImagesBtn = document.getElementById('downloadAllImagesBtn');
const loadingIndicator = document.getElementById('loadingIndicator');
const errorMessage = document.getElementById('errorMessage');
const successMessage = document.getElementById('successMessage');
const textSizeSlider = document.getElementById('textSize');
const textSizeValue = document.getElementById('textSizeValue');
const textColorPicker = document.getElementById('textColor');
const bgColorPicker = document.getElementById('bgColor');
const customBackgroundInput = document.getElementById('customBackground');
const showTranslationSelect = document.getElementById('showTranslation');
const textPositionSelect = document.getElementById('textPosition');
const quranTextAlignSelect = document.getElementById('quranTextAlign');
const realTimePreviewContent = document.getElementById('realTimePreviewContent');
const aspectRatioSelect = document.getElementById('aspectRatio');
const customRatioInput = document.getElementById('customRatio');
const customRatioContainer = document.getElementById('customRatioContainer');
const textShadowSelect = document.getElementById('textShadow');
const fontWeightSelect = document.getElementById('fontWeight');
const borderStyleSelect = document.getElementById('borderStyle');
const borderColorPicker = document.getElementById('borderColor');
const backgroundOpacitySlider = document.getElementById('backgroundOpacity');
const backgroundOpacityValue = document.getElementById('backgroundOpacityValue');
const translationColorPicker = document.getElementById('translationColor');
const translationSizeSlider = document.getElementById('translationSize');
const translationSizeValue = document.getElementById('translationSizeValue');
const showSurahInfoSelect = document.getElementById('showSurahInfo');
const surahInfoLanguageSelect = document.getElementById('surahInfoLanguage');
const surahInfoPositionSelect = document.getElementById('surahInfoPosition');
const surahInfoSizeSlider = document.getElementById('surahInfoSize');
const surahInfoSizeValue = document.getElementById('surahInfoSizeValue');
const surahInfoHAlignSelect = document.getElementById('surahInfoHAlign');
const surahInfoColorPicker = document.getElementById('surahInfoColor');
const surahInfoMarginVSlider = document.getElementById('surahInfoMarginV');
const surahInfoMarginVValue = document.getElementById('surahInfoMarginVValue');
const surahInfoMarginHSlider = document.getElementById('surahInfoMarginH');
const surahInfoMarginHValue = document.getElementById('surahInfoMarginHValue');
const surahInfoOpacitySlider = document.getElementById('surahInfoOpacity');
const surahInfoOpacityValue = document.getElementById('surahInfoOpacityValue');
const bismillahOptionGroup = document.getElementById('bismillahOptionGroup');
const includeBismillahLineCheckbox = document.getElementById('includeBismillahLine');

const _BISMILLAH_ARABIC = 'بِسۡمِ ٱللَّهِ ٱلرَّحۡمَـٰنِ ٱلرَّحِیمِ';
let totalAyahs = 0;
let audioElements = [];
let imageElements = [];
let surahAyahCounts = [];
let currentPlayingAudio = null;
let currentPlayingButton = null;
let customBackgroundImage = null;
let translationCache = {};
let currentModalAyahIndex = 0;
let generatedAyahs = [];
const settings = {
  fontSize: 40,
  textColor: '#ffffff',
  bgColor: '#000000',
  backgroundOpacity: 100,
  textPosition: 'center',
  quranTextAlign: 'right',
  showTranslation: 'none',
  customBackground: null,
  font: 'Amiri',
  aspectRatio: '16:9',
  customRatio: '16:9',
  textShadow: 'none',
  fontWeight: 'normal',
  borderStyle: 'none',
  borderColor: '#ffc107',
  translationColor: '#cccccc',
  translationSize: 24,
  showSurahInfo: 'yes',
  surahInfoLanguage: 'english',
  surahInfoPosition: 'top',
  surahInfoSize: 16,
  surahInfoHAlign: 'center',
  surahInfoColor: '#ffffff',
  surahInfoMarginV: 10,
  surahInfoMarginH: 0,
  surahInfoOpacity: 90,
  includeBismillahLine: false
};

function _surahHasBismillah(surahNumber) {
  const n = parseInt(surahNumber, 10);
  return n >= 2 && n <= 114 && n !== 9;
}

function _stripTashkeel(s) {
  return s.replace(/[\u064B-\u0652\u0670\u06D6-\u06ED\u06E1-\u06E4\u08D4-\u08E1\u08F0-\u08FF\u0610-\u061A\u0640]/g, '');
}

function _stripBismillah(text, surahNumber, ayahNumber) {
  if (parseInt(ayahNumber, 10) !== 1) return text;
  if (!_surahHasBismillah(surahNumber)) return text;
  const skeleton = _stripTashkeel(text);
  const bismSkeleton = _stripTashkeel(_BISMILLAH_ARABIC);
  if (!skeleton.startsWith(bismSkeleton)) return text;
  const skeletonIdx = bismSkeleton.length;
  let origIdx = 0, stripped = 0;
  while (origIdx < text.length && stripped < skeletonIdx) {
    if (_stripTashkeel(text[origIdx]) !== '') stripped++;
    origIdx++;
  }
  while (origIdx < text.length && /[\u064B-\u0652\u0670\u06D6-\u06ED\u06E1-\u06E4\u08D4-\u08E1\u08F0-\u08FF\u0610-\u061A\u0640\s]/.test(text[origIdx])) {
    origIdx++;
  }
  return text.substring(origIdx).trim();
}

function updateBismillahOption() {
  if (!bismillahOptionGroup) return;
  const surahNumber = parseInt(surahSelect?.value, 10);
  const startAyah = parseInt(ayahStartSelect?.value, 10);
  const show = !isNaN(surahNumber) && _surahHasBismillah(surahNumber)
    && (isNaN(startAyah) || startAyah === 1);
  bismillahOptionGroup.style.display = show ? '' : 'none';
  if (!show && includeBismillahLineCheckbox) {
    includeBismillahLineCheckbox.checked = false;
    settings.includeBismillahLine = false;
  }
}

function _shouldSplitBismillahLine(ayah) {
  if (!includeBismillahLineCheckbox?.checked) return false;
  const surahNumber = ayah.surah?.number;
  return ayah.numberInSurah === 1 && _surahHasBismillah(surahNumber);
}

function _appendAyahTextLines(parent, ayah) {
  const lineStyle = {
    fontSize: `${settings.fontSize}px`,
    textAlign: settings.quranTextAlign,
    direction: 'rtl',
    fontFamily: settings.font,
    fontWeight: settings.fontWeight
  };
  if (_shouldSplitBismillahLine(ayah)) {
    const bismLine = document.createElement('div');
    Object.assign(bismLine.style, lineStyle);
    bismLine.style.marginBottom = '12px';
    bismLine.innerHTML = _BISMILLAH_ARABIC;
    const restLine = document.createElement('div');
    Object.assign(restLine.style, lineStyle);
    restLine.innerHTML = _stripBismillah(ayah.text, ayah.surah.number, 1) || ayah.text;
    parent.appendChild(bismLine);
    parent.appendChild(restLine);
    return;
  }
  const textElement = document.createElement('div');
  Object.assign(textElement.style, lineStyle);
  textElement.innerHTML = ayah.text;
  parent.appendChild(textElement);
}
// Updated global variable for reciter bitrates mapping based on the complete directory structure
const reciterBitrateMap = {
    // Reciters with multiple bitrates
    "ar.abdulbasitmurattal": ["64", "192"],
    "ar.abdulbasitmurattal-2": ["64", "192"],
    "ar.abdullahbasfar": ["32", "64", "192"],
    "ar.abdullahbasfar-2": ["32", "64", "192"],
    "ar.abdulsamad": ["64"],
    "ar.abdurrahmaansudais": ["64", "192"],
    "ar.abdurrahmaansudais-2": ["64", "192"],
    "ar.ahmedajamy": ["64", "128"],
    "ar.alafasy": ["64", "128"],
    "ar.alafasy-2": ["64", "128"],
    "ar.aymanswoaid": ["64"],
    "ar.aymanswoaid-2": ["64"],
    "ar.hanirifai": ["64", "192"],
    "ar.hanirifai-2": ["64", "192"],
    "ar.hudhaify": ["32", "64", "128"],
    "ar.hudhaify-2": ["32", "64", "128"],
    "ar.husary": ["64", "128"],
    "ar.husary-2": ["64", "128"],
    "ar.husarymujawwad": ["64", "128"],
    "ar.husarymujawwad-2": ["64", "128"],
    "ar.mahermuaiqly": ["64", "128"],
    "ar.mahermuaiqly-2": ["64", "128"],
    "ar.minshawi": ["128"],
    "ar.minshawimujawwad": ["64"],
    "ar.minshawimujawwad-2": ["64"],
    "ar.muhammadayyoub": ["128"],
    "ar.muhammadayyoub-2": ["128"],
    "ar.muhammadjibreel": ["128"],
    "ar.muhammadjibreel-2": ["128"],
    "ar.saoodshuraym": ["64"],
    "ar.saoodshuraym-2": ["64"],
    "ar.shaatree": ["64", "128"],
    "ar.shaatree-2": ["64", "128"],
    "ur.khan": ["64"],
    // Additional reciters with single bitrates
    "ar.ibrahimakhbar": ["32"],
    "fa.hedayatfarfooladvand": ["40"],
    "ar.parhizgar": ["48"],
    "fr.leclerc": ["128"],
    "ru.kuliev-audio": ["128", "320"],
    "ru.kuliev-audio-2": ["320"],
    "zh.chinese": ["128"],
    "en.walk": ["192"]
};
// Default preferred bitrate for audio playback
let preferredBitrate = "64"; // Start with 64 as default since more reciters have it
// Add a global variable to store reciter metadata
let reciterMetadata = [];
document.addEventListener('DOMContentLoaded', initializeApp);
// Add a check for html2canvas after the page has fully loaded
window.addEventListener('load', function() {
  // Window loaded, checking html2canvas availability
  if (typeof html2canvas === 'function') {
    // html2canvas is available after window load
  } else {
    // html2canvas is still not available after window load
    // Try to load it one more time
    const script = document.createElement('script');
    script.src = 'https://html2canvas.hertzen.com/dist/html2canvas.min.js';
    script.onload = () => {
      // html2canvas loaded after window load event
      updateRealTimePreview();
    };
    document.head.appendChild(script);
  }
});
// Function to initialize the download bitrate selector
function initializeDownloadBitrateSelector() {
  const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
  if (!downloadBitrateSelect) return;
  // Load preference from localStorage
  const savedDownloadBitrate = localStorage.getItem('preferredDownloadBitrate');
  if (savedDownloadBitrate) {
    for (let i = 0; i < downloadBitrateSelect.options.length; i++) {
      if (downloadBitrateSelect.options[i].value === savedDownloadBitrate) {
        downloadBitrateSelect.selectedIndex = i;
        break;
      }
    }
  }
  // Initialize based on current reciter
  const reciterSelect = document.getElementById('reciter');
  if (reciterSelect && reciterSelect.value && reciterSelect.value !== "Select Reciter") {
    updateDownloadBitrateSelector(reciterSelect.value);
  }
}
function initializeApp() {
  try {
    // Add custom CSS for reciter dropdown
    const style = document.createElement('style');
    style.textContent = `
      #reciter option {
        font-size: 14px;
      }
      .reciter-name {
        font-family: 'Traditional Arabic', 'Scheherazade New', 'Amiri', serif;
        font-size: 16px;
      }
    `;
    document.head.appendChild(style);
    
    // Verify DOM elements exist instead of reassigning them
    const elements = [
      { name: 'surahSelect', element: surahSelect },
      { name: 'ayahStartSelect', element: ayahStartSelect },
      { name: 'ayahEndSelect', element: ayahEndSelect },
      { name: 'reciterDropdown', element: reciterDropdown },
      { name: 'fontSelect', element: fontSelect },
      { name: 'textSizeSlider', element: document.getElementById('textSize') },
      { name: 'textSizeValue', element: document.getElementById('textSizeValue') },
      { name: 'textColorPicker', element: document.getElementById('textColor') },
      { name: 'bgColorPicker', element: document.getElementById('bgColor') },
      { name: 'showTranslationSelect', element: document.getElementById('showTranslation') },
      { name: 'textPositionSelect', element: document.getElementById('textPosition') },
      { name: 'quranTextAlignSelect', element: document.getElementById('quranTextAlign') },
      { name: 'aspectRatioSelect', element: document.getElementById('aspectRatio') },
      { name: 'customRatioInput', element: document.getElementById('customRatio') },
      { name: 'customRatioContainer', element: document.getElementById('customRatioContainer') },
      { name: 'textShadowSelect', element: document.getElementById('textShadow') },
      { name: 'fontWeightSelect', element: document.getElementById('fontWeight') },
      { name: 'borderStyleSelect', element: document.getElementById('borderStyle') },
      { name: 'borderColorPicker', element: document.getElementById('borderColor') },
      { name: 'translationColorPicker', element: document.getElementById('translationColor') },
      { name: 'translationSizeSlider', element: document.getElementById('translationSize') },
      { name: 'translationSizeValue', element: document.getElementById('translationSizeValue') },
      { name: 'backgroundOpacitySlider', element: document.getElementById('backgroundOpacity') },
      { name: 'backgroundOpacityValue', element: document.getElementById('backgroundOpacityValue') },
      { name: 'realTimePreviewContent', element: document.getElementById('realTimePreviewContent') }
    ];
    // Check if any elements are missing
    const missingElements = elements.filter(item => !item.element);
    if (missingElements.length > 0) {
    } else {
    }
  // Load user preferences
    try {
  loadUserPreferences();
    } catch (prefError) {
    }
  // Initialize user if not already registered
    try {
  initializeUser();
    } catch (userError) {
    }
  // Set up event listeners
    try {
  setupEventListeners();
    } catch (eventError) {
    }
  // Fetch Surah list
    try {
  fetchSurahList();
    } catch (surahError) {
      showError('Failed to load Surahs. Please refresh the page.');
    }
  // Fetch Reciters
    try {
  fetchReciters();
    } catch (reciterError) {
    }
  // Initialize translation settings visibility
    try {
  const translationSettings = document.querySelector('.translation-settings');
      if (translationSettings && settings && settings.showTranslation !== 'none') {
    translationSettings.classList.add('visible');
      }
    } catch (error) {
  }
  // Check if html2canvas is loaded
    try {
  if (typeof html2canvas !== 'function') {
    showError('Warning: Image generation features may not work correctly. Attempting to load required libraries...');
    // Try to reload the script with a more reliable approach
    const script = document.createElement('script');
    script.src = 'https://html2canvas.hertzen.com/dist/html2canvas.min.js';
    script.async = false; // Load synchronously to ensure it's available
    script.onload = () => {
      showSuccess('Image generation library loaded successfully!');
      // Force update the preview after library is loaded
      setTimeout(updateRealTimePreview, 1000);
    };
    script.onerror = () => {
      showError('Error: Could not load image generation library. Please try refreshing the page or using a different browser.');
      // Try an alternative CDN as fallback
      const fallbackScript = document.createElement('script');
      fallbackScript.src = 'https://cdn.jsdelivr.net/npm/html2canvas@1.4.1/dist/html2canvas.min.js';
      fallbackScript.async = false;
      fallbackScript.onload = () => {
        showSuccess('Image generation library loaded from alternative source!');
        setTimeout(updateRealTimePreview, 1000);
      };
      fallbackScript.onerror = () => {
        showError('Error: Could not load image generation library from alternative source. Please check your internet connection.');
      };
      document.head.appendChild(fallbackScript);
    };
    document.head.appendChild(script);
  } else {
      }
    } catch (error) {
  }
  // Initialize UI states
    try {
  const customSettings = document.getElementById('customSettings');
  const settingsToggle = document.getElementById('settingsToggle');
  const previewToggle = document.getElementById('previewToggle');
      const realTimePreviewContent = document.getElementById('realTimePreviewContent');
  // Set initial states for collapsible sections
  if (customSettings) {
    customSettings.classList.add('collapsed');
    if (settingsToggle) {
      settingsToggle.setAttribute('aria-expanded', 'false');
      const settingsIcon = settingsToggle.querySelector('.toggle-icon');
      if (settingsIcon) {
        settingsIcon.textContent = '▲';
      }
    }
  }
  // Ensure preview is expanded by default
  if (realTimePreviewContent) {
    realTimePreviewContent.classList.remove('collapsed');
    if (previewToggle) {
      previewToggle.setAttribute('aria-expanded', 'true');
      const previewIcon = previewToggle.querySelector('.toggle-icon');
      if (previewIcon) {
        previewIcon.textContent = '▼';
      }
    }
      }
    } catch (error) {
  }
    // Initialize modals
    try {
  initializeModal();
    } catch (modalError) {
    }
  // Initialize real-time preview
    try {
  setTimeout(updateRealTimePreview, 1000); // Slight delay to allow data to load
    } catch (error) {
    }
  // Update user activity
    try {
  updateUserActivity();
  // Set interval to update user activity periodically
  setInterval(updateUserActivity, 5 * 60 * 1000); // Every 5 minutes
    } catch (error) {
    }
    // Initialize download bitrate selector
    initializeDownloadBitrateSelector();
  } catch (error) {
    showError('Application failed to initialize properly. Please refresh the page.');
  }
  // Initialize settings panel
  const settingsToggle = document.getElementById('settingsToggle');
  const customSettings = document.getElementById('customSettings');
  if (settingsToggle && customSettings) {
    // Set initial state
    settingsToggle.setAttribute('aria-expanded', 'false');
    customSettings.setAttribute('aria-hidden', 'true');
    // Ensure settings panel is properly collapsed on load
    customSettings.classList.add('collapsed');
    customSettings.classList.remove('expanded');
    customSettings.style.display = 'none';
    // Set the toggle icon
    const settingsIcon = settingsToggle.querySelector('.toggle-icon');
    if (settingsIcon) {
      settingsIcon.textContent = '▲';
    }
  }
}
// Function to initialize user
function initializeUser() {
  // Check if user is already registered
  const userId = localStorage.getItem('userId');
  const username = localStorage.getItem('username');
  if (!userId || !username) {
    // Generate a default username
    const defaultUsername = 'Guest_' + Math.floor(Math.random() * 10000);
    // Register the user
    registerUser(defaultUsername);
  } else {
  }
}
// Initialize modal functionality
function initializeModal() {
  // Contact modal
  const contactBtn = document.getElementById('contactBtn');
  const contactModal = document.getElementById('contactModal');
  if (contactModal) {
    const contactClose = contactModal.querySelector('.close');
    if (contactBtn) contactBtn.addEventListener('click', function(e) { e.preventDefault(); contactModal.style.display = 'block'; });
    if (contactClose) contactClose.addEventListener('click', function() { contactModal.style.display = 'none'; });
  }
  // FAQ modal
  const faqBtn = document.getElementById('faqBtn');
  const faqModal = document.getElementById('faqModal');
  if (faqModal) {
    const faqClose = faqModal.querySelector('.close');
    if (faqBtn) faqBtn.addEventListener('click', function(e) { e.preventDefault(); faqModal.style.display = 'block'; });
    if (faqClose) faqClose.addEventListener('click', function() { faqModal.style.display = 'none'; });
  }
  // How to use modal
  const howToUseBtn = document.getElementById('howToUseBtn');
  const howToUseModal = document.getElementById('howToUseModal');
  if (howToUseModal) {
    const howToUseClose = howToUseModal.querySelector('.close');
    if (howToUseBtn) howToUseBtn.addEventListener('click', function(e) { e.preventDefault(); howToUseModal.style.display = 'block'; });
    if (howToUseClose) howToUseClose.addEventListener('click', function() { howToUseModal.style.display = 'none'; });
  }
  // Image preview modal
  const imagePreviewModal = document.getElementById('imagePreviewModal');
  const closeImagePreviewModal = document.getElementById('closeImagePreviewModal');
  const prevAyahBtn = document.getElementById('prevAyahBtn');
  const nextAyahBtn = document.getElementById('nextAyahBtn');
  const modalAyahNumber = document.getElementById('modalAyahNumber');
  const downloadModalImageBtn = document.getElementById('downloadModalImageBtn');
  const downloadModalAudioBtn = document.getElementById('downloadModalAudioBtn');
  const downloadModalSrtBtn = document.getElementById('downloadModalSrtBtn');
  closeImagePreviewModal.addEventListener('click', function() {
    imagePreviewModal.style.display = 'none';
  });
  // Navigation buttons for the modal
  prevAyahBtn.addEventListener('click', function() {
    if (currentModalAyahIndex > 0) {
      currentModalAyahIndex--;
      updateModalContent();
    }
  });
  nextAyahBtn.addEventListener('click', function() {
    if (currentModalAyahIndex < generatedAyahs.length - 1) {
      currentModalAyahIndex++;
      updateModalContent();
    }
  });
  // Download button for the modal image
  downloadModalImageBtn.addEventListener('click', function() {
    const modalImage = document.getElementById('modalImage');
    if (modalImage && modalImage.src) {
      const ayahData = generatedAyahs[currentModalAyahIndex];
      let surahNumber = '';
      let ayahNumber = '';
      if (ayahData) {
        surahNumber = ayahData.surah && ayahData.surah.number ? ayahData.surah.number : 'unknown';
        ayahNumber = ayahData.numberInSurah || 'unknown';
      }
      const filename = `surah_${surahNumber}_ayah_${ayahNumber}.png`;
      downloadImage(modalImage.src, filename);
    }
  });
  // Download button for the modal audio
  downloadModalAudioBtn.addEventListener('click', function() {
    if (audioElements.length > 0 && currentModalAyahIndex < audioElements.length) {
      const audioElement = audioElements[currentModalAyahIndex];
      const ayahData = generatedAyahs[currentModalAyahIndex];
      let surahNumber = '';
      let ayahNumber = '';
      if (ayahData) {
        surahNumber = ayahData.surah && ayahData.surah.number ? ayahData.surah.number : 'unknown';
        ayahNumber = ayahData.numberInSurah || 'unknown';
      }
      const filename = `surah_${surahNumber}_ayah_${ayahNumber}.mp3`;
      if (audioElement && audioElement.src) {
        downloadFile(audioElement.src, filename);
      } else {
        showError('Audio not available for this ayah');
      }
    } else {
      showError('Audio not available for this ayah');
    }
  });
  // Download button for the modal SRT
  downloadModalSrtBtn.addEventListener('click', function() {
    if (generatedAyahs.length > 0 && currentModalAyahIndex < generatedAyahs.length) {
      const ayahData = generatedAyahs[currentModalAyahIndex];
      const audioElement = audioElements[currentModalAyahIndex];
      if (ayahData) {
        try {
          // Generate SRT content for a single ayah
          const startTime = 0;
          const endTime = audioElement && !isNaN(audioElement.duration) ? audioElement.duration : 5;
          const srtContent = generateSRTContent(ayahData, startTime, endTime);
          // Create a blob and download
          const srtBlob = new Blob([srtContent], { type: 'text/plain' });
          const srtUrl = URL.createObjectURL(srtBlob);
          let surahNumber = '';
          let ayahNumber = '';
          if (ayahData) {
            surahNumber = ayahData.surah && ayahData.surah.number ? ayahData.surah.number : 'unknown';
            ayahNumber = ayahData.numberInSurah || 'unknown';
          }
          const filename = `surah_${surahNumber}_ayah_${ayahNumber}.srt`;
          downloadFile(srtUrl, filename);
          // Clean up
          setTimeout(() => URL.revokeObjectURL(srtUrl), 100);
        } catch (error) {
          showError('Error generating SRT file');
        }
      } else {
        showError('SRT content not available for this ayah');
      }
    } else {
      showError('SRT content not available for this ayah');
    }
  });
  // Close modals when clicking outside
  window.addEventListener('click', function(event) {
    if (event.target === contactModal) {
      contactModal.style.display = 'none';
    }
    if (event.target === faqModal) {
      faqModal.style.display = 'none';
    }
    if (event.target === howToUseModal) {
      howToUseModal.style.display = 'none';
    }
    if (event.target === imagePreviewModal) {
      imagePreviewModal.style.display = 'none';
    }
  });
  // Close modals with Escape key
  document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
      contactModal.style.display = 'none';
      faqModal.style.display = 'none';
      howToUseModal.style.display = 'none';
      imagePreviewModal.style.display = 'none';
    }
  });
}
// Function to update the modal content based on the current ayah index
function updateModalContent() {
  const modalImage = document.getElementById('modalImage');
  const modalAyahNumber = document.getElementById('modalAyahNumber');
  const prevAyahBtn = document.getElementById('prevAyahBtn');
  const nextAyahBtn = document.getElementById('nextAyahBtn');
  const imagePreviewModalTitle = document.getElementById('imagePreviewModalTitle');
  const downloadModalAudioBtn = document.getElementById('downloadModalAudioBtn');
  const downloadModalSrtBtn = document.getElementById('downloadModalSrtBtn');
  if (generatedAyahs.length === 0 || currentModalAyahIndex < 0 || currentModalAyahIndex >= generatedAyahs.length) {
    return;
  }
  const ayahData = generatedAyahs[currentModalAyahIndex];
  const imageDataUrl = imageElements[currentModalAyahIndex];
  // Update the modal title with proper null checks
  let surahName = "Surah";
  if (ayahData && ayahData.surah) {
    surahName = ayahData.surah.englishName || `Surah ${ayahData.surah.number || ''}`;
  }
  let ayahNumber = "";
  if (ayahData && ayahData.numberInSurah) {
    ayahNumber = ayahData.numberInSurah;
  }
  imagePreviewModalTitle.textContent = `${surahName} - Ayah ${ayahNumber}`;
  // Update the ayah number display with proper null checks
  let totalAyahs = "";
  if (ayahData && ayahData.surah && ayahData.surah.numberOfAyahs) {
    totalAyahs = ayahData.surah.numberOfAyahs;
  }
  modalAyahNumber.textContent = `Ayah ${ayahNumber} of ${totalAyahs}`;
  // Update the image with null check
  if (modalImage && imageDataUrl) {
    modalImage.src = imageDataUrl;
  } else if (modalImage) {
    // Set a placeholder image if the image data is not available
    modalImage.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSIjZjBmMGYwIi8+PHRleHQgeD0iNTAlIiB5PSI1MCUiIGZvbnQtZmFtaWx5PSJBcmlhbCIgZm9udC1zaXplPSIyNCIgdGV4dC1hbmNob3I9Im1pZGRsZSIgZmlsbD0iIzk5OTk5OSI+SW1hZ2UgTm90IEF2YWlsYWJsZTwvdGV4dD48L3N2Zz4=';
  }
  // Update button states
  prevAyahBtn.disabled = currentModalAyahIndex === 0;
  nextAyahBtn.disabled = currentModalAyahIndex === generatedAyahs.length - 1;
  // Update audio button state
  const hasAudio = audioElements.length > currentModalAyahIndex && 
                   audioElements[currentModalAyahIndex] && 
                   audioElements[currentModalAyahIndex].src;
  downloadModalAudioBtn.disabled = !hasAudio;
  downloadModalAudioBtn.title = hasAudio ? 'Download audio for this ayah' : 'Audio not available';
  // SRT is always available if we have ayah data
  downloadModalSrtBtn.disabled = !ayahData;
  downloadModalSrtBtn.title = ayahData ? 'Download SRT for this ayah' : 'SRT not available';
}
function openImagePreviewModal(imgData, ayahIndex) {
  const imagePreviewModal = document.getElementById('imagePreviewModal');
  // Set the current ayah index
  currentModalAyahIndex = ayahIndex;
  // Update the modal content
  updateModalContent();
  // Show the modal
  imagePreviewModal.style.display = 'block';
}
function loadUserPreferences() {
  try {
    const savedSettings = localStorage.getItem('quranGeneratorSettings');
  if (savedSettings) {
    const parsedSettings = JSON.parse(savedSettings);
      // Update settings object with saved values - without reassigning the constant
    Object.assign(settings, parsedSettings);
      // Apply saved settings to UI elements
    textSizeSlider.value = settings.fontSize;
    textSizeValue.textContent = `${settings.fontSize}px`;
    textColorPicker.value = settings.textColor;
    bgColorPicker.value = settings.bgColor;
    showTranslationSelect.value = settings.showTranslation;
    textPositionSelect.value = settings.textPosition;
    // Set quran text alignment if it exists
    if (quranTextAlignSelect && settings.quranTextAlign) {
      quranTextAlignSelect.value = settings.quranTextAlign;
    }
    if (settings.font) {
      fontSelect.value = settings.font;
    }
    // Set background opacity
    if (settings.backgroundOpacity !== undefined) {
      backgroundOpacitySlider.value = settings.backgroundOpacity;
      backgroundOpacityValue.textContent = `${settings.backgroundOpacity}%`;
    }
    // Set translation color and size if they exist
    if (translationColorPicker && settings.translationColor) {
      translationColorPicker.value = settings.translationColor;
    }
    if (translationSizeSlider && settings.translationSize) {
      translationSizeSlider.value = settings.translationSize;
      if (translationSizeValue) {
        translationSizeValue.textContent = `${settings.translationSize}px`;
      }
    }
    // Set aspect ratio
    if (settings.aspectRatio) {
      if (settings.aspectRatio === settings.customRatio) {
        aspectRatioSelect.value = 'custom';
        customRatioInput.value = settings.customRatio;
        customRatioContainer.style.display = 'block';
      } else {
        aspectRatioSelect.value = settings.aspectRatio;
        customRatioContainer.style.display = 'none';
      }
    }
    // Set new advanced settings
    if (settings.textShadow) {
      textShadowSelect.value = settings.textShadow;
    }
    if (settings.fontWeight) {
      fontWeightSelect.value = settings.fontWeight;
    }
    if (settings.borderStyle) {
      borderStyleSelect.value = settings.borderStyle;
    }
    if (settings.borderColor) {
      borderColorPicker.value = settings.borderColor;
    }
      // Apply new surah info settings
      if (showSurahInfoSelect && settings.showSurahInfo !== undefined) {
        showSurahInfoSelect.value = settings.showSurahInfo;
      }
      if (surahInfoLanguageSelect && settings.surahInfoLanguage !== undefined) {
        surahInfoLanguageSelect.value = settings.surahInfoLanguage;
      }
      if (surahInfoPositionSelect && settings.surahInfoPosition !== undefined) {
        surahInfoPositionSelect.value = settings.surahInfoPosition;
      }
      // Apply additional surah info settings
      if (surahInfoSizeSlider && settings.surahInfoSize !== undefined) {
        surahInfoSizeSlider.value = settings.surahInfoSize;
        if (surahInfoSizeValue) surahInfoSizeValue.textContent = `${settings.surahInfoSize}px`;
      }
      if (surahInfoHAlignSelect && settings.surahInfoHAlign !== undefined) {
        surahInfoHAlignSelect.value = settings.surahInfoHAlign;
      }
      if (surahInfoColorPicker && settings.surahInfoColor !== undefined) {
        surahInfoColorPicker.value = settings.surahInfoColor;
      }
      if (surahInfoMarginVSlider && settings.surahInfoMarginV !== undefined) {
        surahInfoMarginVSlider.value = settings.surahInfoMarginV;
        if (surahInfoMarginVValue) surahInfoMarginVValue.textContent = `${settings.surahInfoMarginV}px`;
      }
      if (surahInfoMarginHSlider && settings.surahInfoMarginH !== undefined) {
        surahInfoMarginHSlider.value = settings.surahInfoMarginH;
        if (surahInfoMarginHValue) surahInfoMarginHValue.textContent = `${settings.surahInfoMarginH}px`;
      }
      if (surahInfoOpacitySlider && settings.surahInfoOpacity !== undefined) {
        surahInfoOpacitySlider.value = settings.surahInfoOpacity;
        if (surahInfoOpacityValue) surahInfoOpacityValue.textContent = `${settings.surahInfoOpacity}%`;
      }
      if (includeBismillahLineCheckbox && settings.includeBismillahLine !== undefined) {
        includeBismillahLineCheckbox.checked = !!settings.includeBismillahLine;
      }
    }
  } catch (error) {
  }
  // Set default values if not already set
  if (settings.showSurahInfo === undefined) settings.showSurahInfo = 'yes';
  if (settings.surahInfoLanguage === undefined) settings.surahInfoLanguage = 'english';
  if (settings.surahInfoPosition === undefined) settings.surahInfoPosition = 'top';
  if (settings.surahInfoSize === undefined) settings.surahInfoSize = 16;
  if (settings.surahInfoHAlign === undefined) settings.surahInfoHAlign = 'center';
  if (settings.surahInfoColor === undefined) settings.surahInfoColor = '#ffffff';
  if (settings.surahInfoMarginV === undefined) settings.surahInfoMarginV = 10;
  if (settings.surahInfoMarginH === undefined) settings.surahInfoMarginH = 0;
  if (settings.surahInfoOpacity === undefined) settings.surahInfoOpacity = 90;
  if (settings.includeBismillahLine === undefined) settings.includeBismillahLine = false;
  updateBismillahOption();
  const userPreferences = JSON.parse(localStorage.getItem('userPreferences') || '{}');
  if (userPreferences.preferredBitrate) {
      preferredBitrate = userPreferences.preferredBitrate;
      // Update UI
      const bitrateSelect = document.getElementById('bitrateSelect');
      if (bitrateSelect) {
          for (let i = 0; i < bitrateSelect.options.length; i++) {
              if (bitrateSelect.options[i].value === preferredBitrate) {
                  bitrateSelect.selectedIndex = i;
                  break;
              }
          }
      }
  }
  // Load preferred download bitrate
  const preferredDownloadBitrate = localStorage.getItem('preferredDownloadBitrate');
  if (preferredDownloadBitrate) {
    const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
    if (downloadBitrateSelect) {
      for (let i = 0; i < downloadBitrateSelect.options.length; i++) {
        if (downloadBitrateSelect.options[i].value === preferredDownloadBitrate) {
          downloadBitrateSelect.selectedIndex = i;
          break;
        }
      }
    }
  }
}
function saveUserPreferences() {
  try {
    const settingsToSave = {
      fontSize: settings.fontSize,
      textColor: settings.textColor,
      bgColor: settings.bgColor,
      font: settings.font,
      textPosition: settings.textPosition,
      quranTextAlign: settings.quranTextAlign,
      showTranslation: settings.showTranslation,
      aspectRatio: settings.aspectRatio,
      customWidth: settings.customWidth,
      customHeight: settings.customHeight,
      textShadow: settings.textShadow,
      fontWeight: settings.fontWeight,
      borderStyle: settings.borderStyle,
      borderColor: settings.borderColor,
      backgroundOpacity: settings.backgroundOpacity,
      translationColor: settings.translationColor,
      translationSize: settings.translationSize,
      // Add the new surah info settings
      showSurahInfo: settings.showSurahInfo,
      surahInfoLanguage: settings.surahInfoLanguage,
      surahInfoPosition: settings.surahInfoPosition,
      surahInfoSize: settings.surahInfoSize,
      surahInfoHAlign: settings.surahInfoHAlign,
      surahInfoColor: settings.surahInfoColor,
      surahInfoMarginV: settings.surahInfoMarginV,
      surahInfoMarginH: settings.surahInfoMarginH,
      surahInfoOpacity: settings.surahInfoOpacity,
      includeBismillahLine: settings.includeBismillahLine
    };
    localStorage.setItem('quranGeneratorSettings', JSON.stringify(settingsToSave));
  } catch (error) {
  }
}
function setupEventListeners() {
  // Debounce function to prevent too many updates
  function debounce(func, wait) {
    let timeout;
    return function(...args) {
      clearTimeout(timeout);
      timeout = setTimeout(() => func.apply(this, args), wait);
    };
  }
  // Use debounced version for text size changes
  const debouncedUpdatePreview = debounce(updateRealTimePreview, 300);
  // Setup Back to Top button
  setupBackToTopButton();
  textSizeSlider.addEventListener('input', function() {
    updateTextSize();
    // Update the text size value immediately
    textSizeValue.textContent = `${settings.fontSize}px`;
    // But debounce the preview update
    debouncedUpdatePreview();
  });
  // Add event listeners for settings navigation
  setupSettingsNavigation();
  textSizeSlider.addEventListener('input', function() {
    updateTextSize();
    // Update the text size value immediately
    textSizeValue.textContent = `${settings.fontSize}px`;
    // But debounce the preview update
    debouncedUpdatePreview();
  });
  // Add event listeners for translation color and size if they exist
  if (translationColorPicker) {
    translationColorPicker.addEventListener('input', function() {
      updateTranslationColor();
      updateRealTimePreview();
    });
  }
  if (translationSizeSlider) {
    translationSizeSlider.addEventListener('input', function() {
      updateTranslationSize();
      // Update the translation size value immediately
      if (translationSizeValue) {
        translationSizeValue.textContent = `${settings.translationSize}px`;
      }
      // But debounce the preview update
      debouncedUpdatePreview();
    });
  }
  textColorPicker.addEventListener('input', function() {
    updateTextColor();
    updateRealTimePreview();
  });
  bgColorPicker.addEventListener('input', function() {
    updateBgColor();
    updateRealTimePreview();
  });
  backgroundOpacitySlider.addEventListener('input', function() {
    updateBackgroundOpacity();
    // Update the opacity value immediately
    backgroundOpacityValue.textContent = `${settings.backgroundOpacity}%`;
    // But debounce the preview update
    debouncedUpdatePreview();
  });
  customBackgroundInput.addEventListener('change', function(event) {
    handleCustomBackground(event);
    updateRealTimePreview();
  });
  showTranslationSelect.addEventListener('change', function() {
    updateTranslationOption();
    // Show/hide translation settings based on selection
    const translationSettings = document.querySelector('.translation-settings');
    if (translationSettings) {
      if (this.value !== 'none') {
        translationSettings.classList.add('visible');
      } else {
        translationSettings.classList.remove('visible');
      }
    }
    updateRealTimePreview();
  });
  textPositionSelect.addEventListener('change', function() {
    updateTextPosition();
    updateRealTimePreview();
  });
  quranTextAlignSelect.addEventListener('change', function() {
    updateQuranTextAlign();
    updateRealTimePreview();
  });
  fontSelect.addEventListener('change', function() {
    updateFont();
    updateRealTimePreview();
  });
  aspectRatioSelect.addEventListener('change', function() {
    if (this.value === 'custom') {
      customRatioContainer.style.display = 'block';
      settings.aspectRatio = customRatioInput.value || '16:9';
    } else {
      customRatioContainer.style.display = 'none';
      settings.aspectRatio = this.value;
    }
    saveUserPreferences();
    updateRealTimePreview();
  });
  customRatioInput.addEventListener('input', function() {
    if (aspectRatioSelect.value === 'custom') {
      settings.customRatio = this.value;
      settings.aspectRatio = this.value || '16:9';
      saveUserPreferences();
      debouncedUpdatePreview();
    }
  });
  generateBtn.addEventListener('click', generateAyahs);
  downloadAllAudiosBtn.addEventListener('click', downloadAllAudios);
  downloadCompleteSurahBtn.addEventListener('click', downloadCompleteSurah);
  downloadAllImagesBtn.addEventListener('click', downloadAllImages);
  surahSelect.addEventListener('change', function() {
    handleSurahChange();
    updateBismillahOption();
    updateRealTimePreview();
  });
  // Add listeners for ayah selection changes
  ayahStartSelect.addEventListener('change', function() {
    // Call any existing handlers
    if (typeof handleAyahStartChange === 'function') {
      handleAyahStartChange();
    }
    updateBismillahOption();
    updateRealTimePreview();
  });
  ayahEndSelect.addEventListener('change', updateRealTimePreview);
  // Setup toggle functionality for preview and settings sections
  const previewToggle = document.getElementById('previewToggle');
  const settingsToggle = document.getElementById('settingsToggle');
  const customSettings = document.getElementById('customSettings');
  if (previewToggle) {
    previewToggle.addEventListener('click', function() {
      const isExpanded = this.getAttribute('aria-expanded') === 'true';
      this.setAttribute('aria-expanded', !isExpanded);
      // Toggle the collapsed class
      if (realTimePreviewContent) {
        realTimePreviewContent.classList.toggle('collapsed');
      }
      // Update the toggle icon
      const toggleIcon = this.querySelector('.toggle-icon');
      if (toggleIcon) {
        toggleIcon.textContent = isExpanded ? '▲' : '▼';
      }
    });
  }
  if (settingsToggle) {
    settingsToggle.addEventListener('click', function() {
      const isExpanded = this.getAttribute('aria-expanded') === 'true';
      // Toggle the aria-expanded attribute
      this.setAttribute('aria-expanded', !isExpanded);
      // Toggle the display of the settings panel
      if (customSettings) {
        if (isExpanded) {
          // Collapse the settings panel
          customSettings.classList.add('collapsed');
          customSettings.classList.remove('expanded');
          customSettings.style.display = 'none';
          customSettings.setAttribute('aria-hidden', 'true');
        } else {
          // Expand the settings panel
          customSettings.classList.remove('collapsed');
          customSettings.classList.add('expanded');
          customSettings.style.display = 'block';
          customSettings.setAttribute('aria-hidden', 'false');
        }
      }
      // Update the toggle icon
      const toggleIcon = this.querySelector('.toggle-icon');
      if (toggleIcon) {
        toggleIcon.textContent = isExpanded ? '▲' : '▼';
      }
    });
  }
  // Add event listeners for new advanced settings
  textShadowSelect.addEventListener('change', function() {
    updateTextShadow();
    updateRealTimePreview();
  });
  fontWeightSelect.addEventListener('change', function() {
    updateFontWeight();
    updateRealTimePreview();
  });
  borderStyleSelect.addEventListener('change', function() {
    updateBorderStyle();
    updateRealTimePreview();
  });
  borderColorPicker.addEventListener('input', function() {
    updateBorderColor();
    updateRealTimePreview();
  });
  // Add bitrate selector change handler
  const bitrateSelect = document.getElementById('bitrateSelect');
  if (bitrateSelect) {
      bitrateSelect.addEventListener('change', function() {
          preferredBitrate = this.value;
          // Update localStorage preference
          const userPrefs = JSON.parse(localStorage.getItem('userPreferences') || '{}');
          userPrefs.preferredBitrate = preferredBitrate;
          localStorage.setItem('userPreferences', JSON.stringify(userPrefs));
          // Refresh audio preview if available
          if (document.getElementById('previewAudioContainer').style.display !== 'none') {
              previewAyah();
          }
      });
  }
  // Update reciter dropdown to trigger bitrate selector update
  const reciterDropdown = document.getElementById('reciterSelect');
  if (reciterDropdown) {
      const originalOnChange = reciterDropdown.onchange;
      reciterDropdown.onchange = function(e) {
          // Call the original handler if it exists
          if (originalOnChange) originalOnChange.call(this, e);
          // Update the bitrate selector for this reciter
          updateBitrateSelector(this.value);
      };
  }
  // Add event listener for reciter change to update bitrate selectors
  const reciterSelect = document.getElementById('reciter');
  if (reciterSelect) {
    reciterSelect.addEventListener('change', function() {
      const selectedReciter = reciterSelect.value;
      if (selectedReciter && selectedReciter !== "Select Reciter") {
        // Update both bitrate selectors
        updateBitrateSelectors(selectedReciter);
      }
    });
  }
  // Add event listener for download bitrate selector
  const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
  if (downloadBitrateSelect) {
    downloadBitrateSelect.addEventListener('change', function() {
      // Save preference to localStorage
      localStorage.setItem('preferredDownloadBitrate', downloadBitrateSelect.value);
    });
    // Load preference from localStorage
    const savedDownloadBitrate = localStorage.getItem('preferredDownloadBitrate');
    if (savedDownloadBitrate) {
      for (let i = 0; i < downloadBitrateSelect.options.length; i++) {
        if (downloadBitrateSelect.options[i].value === savedDownloadBitrate) {
          downloadBitrateSelect.selectedIndex = i;
          break;
        }
      }
    }
  }
  // Add event listeners for surah info settings
  if (showSurahInfoSelect) {
    showSurahInfoSelect.addEventListener('change', function() {
      updateSurahInfoDisplay();
      updateRealTimePreview();
    });
  }
  if (surahInfoLanguageSelect) {
    surahInfoLanguageSelect.addEventListener('change', function() {
      updateSurahInfoLanguage();
      updateRealTimePreview();
    });
  }
  if (surahInfoPositionSelect) {
    surahInfoPositionSelect.addEventListener('change', function() {
      updateSurahInfoPosition();
      updateRealTimePreview();
    });
  }
  // Add event listeners for additional surah info settings
  if (surahInfoSizeSlider) {
    surahInfoSizeSlider.addEventListener('input', function() {
      updateSurahInfoSize();
      updateRealTimePreview();
    });
  }
  if (surahInfoHAlignSelect) {
    surahInfoHAlignSelect.addEventListener('change', function() {
      updateSurahInfoHAlign();
      updateRealTimePreview();
    });
  }
  if (surahInfoColorPicker) {
    surahInfoColorPicker.addEventListener('input', function() {
      updateSurahInfoColor();
      updateRealTimePreview();
    });
  }
  if (surahInfoMarginVSlider) {
    surahInfoMarginVSlider.addEventListener('input', function() {
      updateSurahInfoMarginV();
      updateRealTimePreview();
    });
  }
  if (surahInfoMarginHSlider) {
    surahInfoMarginHSlider.addEventListener('input', function() {
      updateSurahInfoMarginH();
      updateRealTimePreview();
    });
  }
  if (surahInfoOpacitySlider) {
    surahInfoOpacitySlider.addEventListener('input', function() {
      updateSurahInfoOpacity();
      updateRealTimePreview();
    });
  }
  if (includeBismillahLineCheckbox) {
    includeBismillahLineCheckbox.addEventListener('change', function() {
      settings.includeBismillahLine = includeBismillahLineCheckbox.checked;
      saveUserPreferences();
      updateRealTimePreview();
    });
  }
}
// Function to handle settings navigation
function setupSettingsNavigation() {
  const navItems = document.querySelectorAll('.settings-nav-item');
  const categories = document.querySelectorAll('.settings-category');
  // Add click event listeners to navigation items
  navItems.forEach(item => {
    item.addEventListener('click', function() {
      const targetId = this.getAttribute('data-target');
      // Handle "Back to Top" button
      if (targetId === 'back-to-top') {
        // Scroll to the top of the settings container
        const settingsContainer = document.getElementById('customSettings');
        if (settingsContainer) {
          settingsContainer.scrollTop = 0;
        }
        return;
      }
      // Remove active class from all nav items and categories
      navItems.forEach(navItem => navItem.classList.remove('active'));
      categories.forEach(category => category.classList.remove('active'));
      // Add active class to clicked nav item
      this.classList.add('active');
      // Show the corresponding category
      const targetCategory = document.getElementById(targetId);
      if (targetCategory) {
        targetCategory.classList.add('active');
        // Scroll to the category with a small offset
        setTimeout(() => {
          targetCategory.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 100);
      }
    });
  });
  // Initialize: ensure the first category is active
  if (navItems.length > 0 && !document.querySelector('.settings-nav-item.active')) {
    navItems[0].click();
  }
}
function updateTextSize() {
  settings.fontSize = parseInt(textSizeSlider.value);
  textSizeValue.textContent = `${settings.fontSize}px`;
  saveUserPreferences();
}
function updateTextColor() {
  settings.textColor = textColorPicker.value;
  saveUserPreferences();
}
function updateBgColor() {
  settings.bgColor = bgColorPicker.value;
  saveUserPreferences();
}
function handleCustomBackground(event) {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = function(e) {
      customBackgroundImage = e.target.result;
      settings.customBackground = true;
      saveUserPreferences();
    };
    reader.readAsDataURL(file);
  }
}
function updateTranslationOption() {
  settings.showTranslation = showTranslationSelect.value;
  saveUserPreferences();
}
function updateTextPosition() {
  settings.textPosition = textPositionSelect.value;
  saveUserPreferences();
}
function updateQuranTextAlign() {
  settings.quranTextAlign = quranTextAlignSelect.value;
  saveUserPreferences();
}
function updateFont() {
  settings.font = fontSelect.value;
  saveUserPreferences();
}
function fetchSurahList() {
  const cachedSurahs = localStorage.getItem('surahList');
  if (cachedSurahs) {
    try {
    const surahsData = JSON.parse(cachedSurahs);
    populateSurahDropdown(surahsData);
    return;
    } catch (error) {
      localStorage.removeItem('surahList'); // Clear invalid cache
    }
  }
  showLoading('Fetching Surah list...');
  // Primary API endpoint
  fetchFromEndpoint('https://api.alquran.cloud/v1/surah')
    .catch(error => {
      // Fallback API endpoint
      return fetchFromEndpoint('https://api.quran.com/api/v4/chapters');
    })
    .catch(error => {
      showError('Could not load surahs. Please check your internet connection and refresh the page.');
    });
  function fetchFromEndpoint(url) {
    return fetch(url)
      .then(response => {
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
      })
      .then(data => {
      hideLoading();
        // Handle different API response formats
        let surahData;
        if (url.includes('api.quran.com')) {
          // Format data from quran.com API
          surahData = data.chapters.map(chapter => ({
            number: chapter.id,
            name: chapter.name_arabic,
            englishName: chapter.name_simple,
            numberOfAyahs: chapter.verses_count
          }));
        } else {
          // Use data from alquran.cloud API
          surahData = data.data;
        }
        localStorage.setItem('surahList', JSON.stringify(surahData));
        populateSurahDropdown(surahData);
        return surahData;
      });
  }
}
function populateSurahDropdown(surahs) {
  surahAyahCounts = surahs.map(surah => surah.numberOfAyahs);
  // Clear existing options first, keeping only the default option
  while (surahSelect.options.length > 1) {
    surahSelect.remove(1);
  }
  surahs.forEach(surah => {
    const option = document.createElement('option');
    option.value = surah.number;
    option.text = `${surah.number}. ${surah.englishName} (${surah.name})`;
    surahSelect.appendChild(option);
  });
  // Only trigger change event if we have options
  if (surahSelect.options.length > 1) {
  surahSelect.dispatchEvent(new Event('change'));
  } else {
  }
}
// Fix fetchReciters function to use the original API endpoint
async function fetchReciters() {
    try {
        // Fetch reciters metadata for proper bitrate info
        const metadataResponse = await fetch('https://raw.githubusercontent.com/islamic-network/cdn/master/info/cdn_surah_audio.json');
        if (metadataResponse.ok) {
            reciterMetadata = await metadataResponse.json();
        } else {
        }
        // Go back to using the original API endpoint that was working
        const response = await fetch('https://api.alquran.cloud/v1/edition?format=audio&language=ar');
        if (!response.ok) {
            throw new Error('Failed to fetch reciters');
        }
        const data = await response.json();
        // Cache the reciters list
        localStorage.setItem('recitersList', JSON.stringify(data.data));
        // Populate the dropdown
        populateRecitersDropdown(data.data);
    } catch (error) {
        showError('Error loading reciters. Please refresh the page.');
        // Try to use cached reciters if available
  const cachedReciters = localStorage.getItem('recitersList');
  if (cachedReciters) {
    const recitersData = JSON.parse(cachedReciters);
    populateRecitersDropdown(recitersData);
        }
    }
}
// Function to get the correct bitrate for a reciter
function getReciterBitrate(reciterId) {
    // Check if we have bitrate info for this reciter
    if (reciterBitrateMap[reciterId] && reciterBitrateMap[reciterId].length > 0) {
        // Try to use the preferred bitrate if available
        if (reciterBitrateMap[reciterId].includes(preferredBitrate)) {
            return preferredBitrate;
        }
        // Otherwise use the first available bitrate
        return reciterBitrateMap[reciterId][0];
    }
    // Fallback logic from reciter metadata if available
    if (reciterMetadata && reciterMetadata.length > 0) {
        const reciter = reciterMetadata.find(r => r.identifier === reciterId);
        if (reciter && reciter.bitrate) {
            return reciter.bitrate;
        }
    }
    return "64"; // Default fallback to 64 since more reciters have this bitrate
}
// Get available bitrates for a reciter
function getAvailableBitrates(reciterId) {
    if (reciterBitrateMap[reciterId] && reciterBitrateMap[reciterId].length > 0) {
        return reciterBitrateMap[reciterId];
    }
    return ["64"]; // Default fallback
}
// Add function to update the audio bitrate selector
function updateBitrateSelector(reciterId) {
    const bitrateContainer = document.getElementById('bitrateContainer');
    const bitrateSelect = document.getElementById('bitrateSelect');
    if (!bitrateContainer || !bitrateSelect) return;
    // Get available bitrates for this reciter
    const bitrates = getAvailableBitrates(reciterId);
    // Only show if there are multiple bitrates
    if (bitrates.length > 1) {
        // Clear existing options
        bitrateSelect.innerHTML = '';
        // Add options for each available bitrate
        bitrates.forEach(bitrate => {
            const option = document.createElement('option');
            option.value = bitrate;
            option.text = `${bitrate} kbps`;
            // Select current preferred bitrate if available
            if (bitrate === preferredBitrate) {
                option.selected = true;
            }
            bitrateSelect.appendChild(option);
        });
        // Show bitrate selector
        bitrateContainer.style.display = 'block';
    } else {
        // Hide if only one bitrate is available
        bitrateContainer.style.display = 'none';
    }
}
// Function to update the download bitrate selector based on reciter
function updateDownloadBitrateSelector(reciterId) {
  const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
  if (!downloadBitrateSelect) return;
  // Get available bitrates for this reciter
  const availableBitrates = getAvailableBitrates(reciterId);
  // Save current selection if possible
  const currentSelection = downloadBitrateSelect.value;
  // Clear existing options
  downloadBitrateSelect.innerHTML = '';
  // Sort bitrates numerically (low to high)
  const sortedBitrates = [...availableBitrates].sort((a, b) => parseInt(a) - parseInt(b));
  // Add options for each available bitrate with descriptive labels
  sortedBitrates.forEach(bitrate => {
    const option = document.createElement('option');
    option.value = bitrate;
    // Add descriptive text based on bitrate
    let qualityLabel = "";
    if (parseInt(bitrate) <= 32) qualityLabel = " (Smallest)";
    else if (parseInt(bitrate) <= 64) qualityLabel = " (Standard)";
    else if (parseInt(bitrate) <= 128) qualityLabel = " (Good)";
    else if (parseInt(bitrate) <= 192) qualityLabel = " (High)";
    else qualityLabel = " (Best)";
    option.text = `${bitrate} kbps${qualityLabel}`;
    // Select current preferred bitrate if available
    if (bitrate === currentSelection) {
      option.selected = true;
    }
    downloadBitrateSelect.appendChild(option);
  });
  // If the previous selection is not available, select the first option
  if (!availableBitrates.includes(currentSelection) && downloadBitrateSelect.options.length > 0) {
    downloadBitrateSelect.selectedIndex = 0;
  }
}
async function loadAudioDirectly(audioUrl, retries = 3) {
    // Log the original URL for debugging
    // Extract information from the URL
    const urlObj = new URL(audioUrl);
    const pathParts = urlObj.pathname.split('/').filter(part => part);
    let reciterId, bitrate, ayahNumber, isIslamicNetwork = false;
    // Handle Islamic Network CDN URLs
    if (audioUrl.includes('cdn.islamic.network')) {
        isIslamicNetwork = true;
        const audioIndex = pathParts.indexOf('audio');
        if (audioIndex >= 0 && audioIndex + 2 < pathParts.length) {
            bitrate = pathParts[audioIndex + 1];
            reciterId = pathParts[audioIndex + 2];
            ayahNumber = pathParts[audioIndex + 3];
        }
    }
    // If we have a reciter ID, ensure we use the correct bitrates
    if (reciterId && isIslamicNetwork) {
        // Get all available bitrates for this reciter
        const availableBitrates = getAvailableBitrates(reciterId);
        // Ensure the correct preferred bitrate is used first
        let correctBitrate = preferredBitrate;
        if (!availableBitrates.includes(preferredBitrate)) {
            correctBitrate = availableBitrates[0];
        }
        // Try loading with our primary bitrate first
        try {
            const primaryUrl = `https://cdn.islamic.network/quran/audio/${correctBitrate}/${reciterId}/${ayahNumber}.mp3`;
            const result = await tryLoadAudio(primaryUrl);
            return result;
        } catch (primaryError) {
            // Try all other available bitrates for this reciter
            for (const altBitrate of availableBitrates) {
                if (altBitrate !== correctBitrate) {
                    try {
                        const altUrl = `https://cdn.islamic.network/quran/audio/${altBitrate}/${reciterId}/${ayahNumber}.mp3`;
                        const result = await tryLoadAudio(altUrl);
                        return result;
                    } catch (bitrateError) {
                    }
                }
            }
            // Try the "-2" version if the original doesn't have it
            if (!reciterId.endsWith('-2')) {
                const altReciterId = `${reciterId}-2`;
                if (reciterBitrateMap[altReciterId]) {
                    const alt2Bitrates = getAvailableBitrates(altReciterId);
                    for (const altBitrate of alt2Bitrates) {
                        try {
                            const altUrl = `https://cdn.islamic.network/quran/audio/${altBitrate}/${altReciterId}/${ayahNumber}.mp3`;
                            const result = await tryLoadAudio(altUrl);
                            return result;
                        } catch (reciterError) {
                        }
                    }
                }
            }
            // Try EveryAyah.com as a final fallback
            try {
                let surahNumber, ayahInSurah;
                // Extract surah and ayah numbers
                if (ayahNumber) {
                    const totalAyahNumber = parseInt(ayahNumber);
                    const surahAyahInfo = getAyahFromCumulative(totalAyahNumber);
                    if (surahAyahInfo) {
                        surahNumber = surahAyahInfo.surah;
                        ayahInSurah = surahAyahInfo.ayah;
                    } else {
                        // Fallback to current selected values
                        surahNumber = parseInt(document.getElementById('surah').value);
                        ayahInSurah = parseInt(document.getElementById('ayahStart').value);
                    }
                    const everyayahUrl = `https://everyayah.com/data/${reciterId.replace(/^ar\./, '')}/${surahNumber.toString().padStart(3, '0')}${ayahInSurah.toString().padStart(3, '0')}.mp3`;
                    const result = await tryLoadAudio(everyayahUrl);
                    return result;
                }
            } catch (everyayahError) {
            }
        }
    } else {
        // For non-Islamic Network URLs or if we couldn't extract info, try the original URL
        try {
            const result = await tryLoadAudio(audioUrl);
            return result;
        } catch (originalError) {
        }
    }
    // If all attempts failed, throw an error
    throw new Error(`Failed to load audio after trying all available sources and bitrates`);
}
// Helper function to load audio and return a promise
async function tryLoadAudio(url, timeout = 5000) {
    return new Promise((resolve, reject) => {
        const audio = new Audio(url);
        audio.addEventListener('loadedmetadata', () => {
            resolve({ audioBlobUrl: url, duration: audio.duration });
        });
        audio.addEventListener('error', (e) => {
            reject(new Error(`Audio failed to load: ${audio.error?.message || 'unknown error'}`));
        });
        // Set a timeout in case the audio never loads
        const timeoutId = setTimeout(() => {
            if (audio.readyState === 0) {
                reject(new Error('Audio load timeout'));
            }
        }, timeout);
        // Clear timeout if audio loads or errors
        audio.addEventListener('loadedmetadata', () => clearTimeout(timeoutId));
        audio.addEventListener('error', () => clearTimeout(timeoutId));
        // Start loading the audio
        audio.load();
    });
}
function populateRecitersDropdown(reciters) {
  // Clear existing options
  reciterDropdown.innerHTML = '';
  
  // Add a default option
  const defaultOption = document.createElement('option');
  defaultOption.value = '';
  defaultOption.text = 'Select Reciter';
  defaultOption.disabled = true;
  defaultOption.selected = true;
  reciterDropdown.add(defaultOption);
  
  // Sort reciters alphabetically by English name
  reciters.sort((a, b) => a.englishName.localeCompare(b.englishName));
  
  reciters.forEach(edition => {
    const option = document.createElement('option');
    option.value = edition.identifier;
    
    // Create custom HTML for option text with separate styling
    const arabicName = edition.name;
    const englishName = edition.englishName;
    
    // Set the option text to both names
    option.text = `${arabicName} - ${englishName}`;
    
    // Store original names as data attributes for potential future use
    option.dataset.arabicName = arabicName;
    option.dataset.englishName = englishName;
    
    reciterDropdown.add(option);
  });
  
  // Add custom styling after populating
  addReciterDropdownStyling();
  
  // Update the bitrate selector for the default reciter if one is selected
  const defaultReciter = reciterDropdown.value;
  if (defaultReciter && defaultReciter !== "Select Reciter") {
    updateBitrateSelectors(defaultReciter);
  }
}

// Function to add custom styling to the reciter dropdown
function addReciterDropdownStyling() {
  // Add CSS for better dropdown display if not already added
  if (!document.getElementById('reciter-dropdown-style')) {
    const style = document.createElement('style');
    style.id = 'reciter-dropdown-style';
    style.textContent = `
      #reciter {
        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        padding: 8px 12px;
        border-radius: 4px;
        width: 100%;
        max-width: 400px;
      }
      
      #reciter option {
        padding: 8px;
        line-height: 1.5;
      }
      
      /* For browsers that support styling option elements */
      @supports ((-webkit-appearance: none) or (-moz-appearance: none)) {
        #reciter option {
          display: flex;
          align-items: center;
          padding: 8px 12px;
        }
      }
    `;
    document.head.appendChild(style);
  }
}
function handleSurahChange() {
  const surahNumber = parseInt(surahSelect.value, 10);
  if (isNaN(surahNumber)) return;
  updateBismillahOption();
  const cachedSurah = localStorage.getItem(`surah_${surahNumber}`);
  if (cachedSurah) {
    const surahData = JSON.parse(cachedSurah);
    updateAyahDropdowns(surahData.numberOfAyahs);
    return;
  }
  showLoading('Loading Surah details...');
  fetch(`https://api.alquran.cloud/v1/surah/${surahNumber}`)
    .then(response => response.json())
    .then(surahData => {
      hideLoading();
      localStorage.setItem(`surah_${surahNumber}`, JSON.stringify(surahData.data));
      updateAyahDropdowns(surahData.data.numberOfAyahs);
    })
    .catch(error => {
      hideLoading();
      showError('Error fetching surah data: ' + error.message);
    });
}
function updateAyahDropdowns(numberOfAyahs) {
  totalAyahs = numberOfAyahs;
  populateAyahDropdown(ayahStartSelect);
  populateAyahDropdown(ayahEndSelect);
  updateBismillahOption();
  ayahStartSelect.addEventListener('change', () => {
    const selectedStartAyah = parseInt(ayahStartSelect.value, 10);
    if (!isNaN(selectedStartAyah)) {
      populateAyahDropdown(ayahEndSelect, selectedStartAyah);
    }
  });
  // Update the real-time preview after populating dropdowns
  updateRealTimePreview();
}
function populateAyahDropdown(selectElement, startFrom = 1) {
  selectElement.innerHTML = '';
  const placeholderOption = document.createElement('option');
  placeholderOption.text = selectElement === ayahStartSelect ? 'Start Ayah' : 'End Ayah';
  placeholderOption.disabled = true;
  placeholderOption.selected = true;
  selectElement.appendChild(placeholderOption);
  for (let i = startFrom; i <= totalAyahs; i++) {
    const option = document.createElement('option');
    option.value = i;
    option.text = i;
    selectElement.appendChild(option);
  }
}
function getCumulativeAyahNumber(surahNumber, ayahNumber) {
  let cumulativeAyahNumber = 0;
  for (let i = 0; i < surahNumber - 1; i++) {
    cumulativeAyahNumber += surahAyahCounts[i];
  }
  cumulativeAyahNumber += ayahNumber;
  return cumulativeAyahNumber;
}
// Function to get surah and ayah from cumulative ayah number
function getAyahFromCumulative(cumulativeAyah) {
  let surah = 1;
  let ayah = cumulativeAyah;
  let cumulativeCount = 0;
  for (let i = 0; i < surahAyahCounts.length; i++) {
    if (cumulativeAyah > cumulativeCount && cumulativeAyah <= cumulativeCount + surahAyahCounts[i]) {
      surah = i + 1;
      ayah = cumulativeAyah - cumulativeCount;
      break;
    }
    cumulativeCount += surahAyahCounts[i];
  }
  return { surah, ayah };
}
// Function to reset audio settings and cache
function resetAudioSettings() {
    try {
        // Reset preferred bitrate to default
        preferredBitrate = "64";
        localStorage.setItem('preferredAudioBitrate', preferredBitrate);
        // Update UI to reflect the reset
        const bitrateSelect = document.getElementById('bitrateSelect');
        if (bitrateSelect) {
            // Find the 64kbps option and select it
            for (let i = 0; i < bitrateSelect.options.length; i++) {
                if (bitrateSelect.options[i].value === "64") {
                    bitrateSelect.selectedIndex = i;
                    break;
                }
            }
        }
        // Clear any currently playing audio
        if (currentAudio) {
            currentAudio.pause();
            currentAudio.src = "";
            currentAudio = null;
        }
        // Reset audio elements array
        audioElements = [];
        // Clear any cached audio URLs
        const keys = Object.keys(localStorage);
        for (const key of keys) {
            if (key.startsWith('audioUrl_')) {
                localStorage.removeItem(key);
            }
        }
        showSuccess("Audio settings have been reset. Try loading audio again.");
        return true;
    } catch (error) {
        showError("Failed to reset audio settings. Please try refreshing the page.");
        return false;
    }
}
async function previewAyah() {
    try {
        const surahNumber = parseInt(document.getElementById('surah').value);
        const ayahNumber = parseInt(document.getElementById('ayahStart').value);
        const reciterId = document.getElementById('reciter').value;
        // Validate inputs
        if (isNaN(surahNumber) || isNaN(ayahNumber) || !reciterId || reciterId === "Select Reciter") {
    return;
  }
        // Show the preview container
        const previewContainer = document.getElementById('previewAudioContainer');
        if (previewContainer) {
            previewContainer.style.display = "block";
        }
        // Show loading message
        const previewInfo = document.getElementById('previewInfo');
        if (previewInfo) {
            previewInfo.textContent = "Loading audio...";
            previewInfo.className = "preview-info";
        }
        // Fetch the ayah text
    const ayahData = await fetchAyah(surahNumber, ayahNumber);
        // Display the ayah text
        const previewAyahText = document.getElementById('previewAyahText');
        if (previewAyahText && ayahData && ayahData.text) {
            previewAyahText.textContent = ayahData.text;
            previewAyahText.dir = "rtl";
        }
        // Get the audio URL
        const ayahNumberGlobal = ayahData.number;
        const bitrate = getReciterBitrate(reciterId);
        const audioUrl = `https://cdn.islamic.network/quran/audio/${bitrate}/${reciterId}/${ayahNumberGlobal}.mp3`;
        // Try to load the audio with multiple attempts
        let audioLoaded = false;
        let maxAttempts = 3;
        let lastError;
        for (let attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                const result = await loadAudioDirectly(audioUrl);
                // Audio loaded successfully
                const previewAudio = document.getElementById('previewAudio');
                if (previewAudio) {
                    previewAudio.src = result.audioBlobUrl;
                    previewAudio.load();
                    // Update success message with reciter info
                    if (previewInfo) {
                        // Get the reciter names from the dropdown
                        const selectedOption = reciterDropdown.options[reciterDropdown.selectedIndex];
                        let reciterInfo = '';
                        if (selectedOption) {
                            const arabicName = selectedOption.dataset.arabicName;
                            const englishName = selectedOption.dataset.englishName;
                            reciterInfo = `${arabicName} - ${englishName}`;
                        }
                        
                        previewInfo.textContent = `Audio loaded successfully (${bitrate}kbps)${reciterInfo ? ' - ' + reciterInfo : ''}`;
                        previewInfo.className = "preview-info success";
                    }
                }
                audioLoaded = true;
                break;
  } catch (error) {
                lastError = error;
                if (attempt < maxAttempts) {
                    // Wait a moment before trying again
                    await new Promise(resolve => setTimeout(resolve, 1000));
                }
            }
        }
        // Handle failure after all attempts
        if (!audioLoaded) {
            if (previewInfo) {
                // Prompt user to reset audio settings
                previewInfo.innerHTML = `
                    Failed to load audio after multiple attempts. 
                    <button id="resetAudioSettingsBtn" class="btn-reset">Reset Audio Settings</button>
                `;
                previewInfo.className = "preview-info error";
                // Add event listener to the reset button
                const resetBtn = document.getElementById('resetAudioSettingsBtn');
                if (resetBtn) {
                    resetBtn.addEventListener('click', () => {
                        const resetSuccessful = resetAudioSettings();
                        if (resetSuccessful) {
                            // Try loading the audio again after reset
                            setTimeout(() => previewAyah(), 1000);
                        }
                    });
                }
            }
        }
    } catch (error) {
        // Update error message
        const previewInfo = document.getElementById('previewInfo');
        if (previewInfo) {
            previewInfo.textContent = `Error: ${error.message}`;
            previewInfo.className = "preview-info error";
        }
  }
}
async function fetchAyah(surahNumber, ayahNumber) {
  const cacheKey = `ayah_${surahNumber}_${ayahNumber}`;
  const cachedAyah = localStorage.getItem(cacheKey);
  if (cachedAyah) {
    return JSON.parse(cachedAyah);
  }
  const response = await fetch(`https://api.alquran.cloud/v1/ayah/${surahNumber}:${ayahNumber}`);
  const data = await response.json();
  if (!data.data) {
    throw new Error('Failed to fetch ayah data');
  }
  localStorage.setItem(cacheKey, JSON.stringify(data.data));
  return data.data;
}
async function fetchTranslation(surahNumber, ayahNumber, language) {
  let edition = 'en.sahih';
  if (language === 'es') edition = 'es.asad';
  if (language === 'id') edition = 'id.indonesian';
  if (language === 'tr') edition = 'tr.ates';
  if (language === 'ru') edition = 'ru.kuliev';
  if (language === 'fa') edition = 'fa.makarem';
  if (language === 'de') edition = 'de.aburida';
  if (language === 'hi') edition = 'hi.hindi';
  if (language === 'ur') edition = 'ur.ahmedali';
  if (language === 'fr') edition = 'fr.hamidullah';
  const cacheKey = `translation_${edition}_${surahNumber}_${ayahNumber}`;
  if (translationCache[cacheKey]) {
    return translationCache[cacheKey];
  }
  const response = await fetch(`https://api.alquran.cloud/v1/ayah/${surahNumber}:${ayahNumber}/${edition}`);
  const data = await response.json();
  if (!data.data) {
    throw new Error('Failed to fetch translation');
  }
  translationCache[cacheKey] = data.data.text;
  return data.data.text;
}
function createAyahElement(ayah, translationText = '') {
  const ayahElement = document.createElement('div');
  ayahElement.id = `ayahElement${ayah.numberInSurah}`;
  ayahElement.style.padding = '20px';
  ayahElement.style.fontSize = `${settings.fontSize}px`;
  ayahElement.style.color = settings.textColor;
  ayahElement.style.textAlign = 'center';
  ayahElement.style.fontFamily = settings.font;
  ayahElement.style.fontWeight = settings.fontWeight;
  // Apply text shadow based on settings
  if (settings.textShadow !== 'none') {
    let shadowSize = '2px';
    if (settings.textShadow === 'medium') shadowSize = '4px';
    if (settings.textShadow === 'heavy') shadowSize = '6px';
    ayahElement.style.textShadow = `0 0 ${shadowSize} rgba(0,0,0,0.8)`;
  }
  // Apply border based on settings
  if (settings.borderStyle !== 'none') {
    let borderWidth = '1px';
    if (settings.borderStyle === 'medium') borderWidth = '3px';
    if (settings.borderStyle === 'thick') borderWidth = '5px';
    ayahElement.style.border = `${borderWidth} solid ${settings.borderColor}`;
    ayahElement.style.borderRadius = '8px';
  }
  // Set background color with opacity
  const bgColor = settings.bgColor;
  const opacity = settings.backgroundOpacity / 100;
  // Convert hex to rgba
  if (bgColor.startsWith('#')) {
    const r = parseInt(bgColor.slice(1, 3), 16);
    const g = parseInt(bgColor.slice(3, 5), 16);
    const b = parseInt(bgColor.slice(5, 7), 16);
    ayahElement.style.backgroundColor = `rgba(${r}, ${g}, ${b}, ${opacity})`;
  } else {
    // For non-hex colors, we'll use a wrapper with opacity
    ayahElement.style.backgroundColor = bgColor;
    ayahElement.style.opacity = opacity;
  }
  // Apply aspect ratio
  const [width, height] = settings.aspectRatio.split(':').map(Number);
  if (!isNaN(width) && !isNaN(height)) {
    // Base width on device size
    const isMobile = window.innerWidth < 768;
    const baseWidth = isMobile ? window.innerWidth - 40 : 800;
    const baseHeight = (baseWidth * height) / width;
    ayahElement.style.width = `${baseWidth}px`;
    ayahElement.style.height = `${baseHeight}px`;
    ayahElement.style.maxWidth = '100%';
    ayahElement.style.position = 'relative';
    ayahElement.style.overflow = 'hidden';
  } else {
    // Fallback if aspect ratio is invalid
    ayahElement.style.width = '100%';
    ayahElement.style.maxWidth = '800px';
    ayahElement.style.height = 'auto';
    ayahElement.style.minHeight = '400px';
  }
  if (customBackgroundImage && settings.customBackground) {
    ayahElement.style.backgroundImage = `url(${customBackgroundImage})`;
    ayahElement.style.backgroundSize = 'cover';
    ayahElement.style.backgroundPosition = 'center';
  }
  // Adjust content positioning based on text position setting
  const contentWrapper = document.createElement('div');
  contentWrapper.style.display = 'flex';
  contentWrapper.style.flexDirection = 'column';
  contentWrapper.style.height = '100%';
  contentWrapper.style.justifyContent = settings.textPosition === 'center' ? 'center' : 
                                        settings.textPosition === 'top' ? 'flex-start' : 'flex-end';
  // Add surah info if enabled (read live UI values so toggles apply immediately)
  const showSurahInfo = (showSurahInfoSelect?.value ?? settings.showSurahInfo) === 'yes';
  const surahInfoLang = surahInfoLanguageSelect?.value ?? settings.surahInfoLanguage;
  const displayNumber = ayah.correctNumberInSurah ?? ayah.numberInSurah;

  if (showSurahInfo && ayah.surah) {
    const surahInfoElement = document.createElement('div');
    // Apply all the new styling options
    surahInfoElement.style.fontSize = `${settings.surahInfoSize}px`;
    surahInfoElement.style.color = settings.surahInfoColor;
    surahInfoElement.style.opacity = settings.surahInfoOpacity / 100;
    surahInfoElement.style.textAlign = settings.surahInfoHAlign;
    surahInfoElement.style.fontWeight = 'normal';
    // Apply margins based on position
    if (settings.surahInfoPosition === 'top') {
      surahInfoElement.style.marginBottom = `${settings.surahInfoMarginV}px`;
      surahInfoElement.style.marginTop = '0';
  } else {
      surahInfoElement.style.marginTop = `${settings.surahInfoMarginV}px`;
      surahInfoElement.style.marginBottom = '0';
    }
    // Apply horizontal margins
    surahInfoElement.style.marginLeft = `${settings.surahInfoMarginH}px`;
    surahInfoElement.style.marginRight = `${settings.surahInfoMarginH}px`;
    let surahName, ayahLabel;

    if (surahInfoLang === 'arabic') {
      surahName = ayah.surah.name || '';
      ayahLabel = `الآية ${convertToArabicNumerals(displayNumber)}`;
    } else {
      surahName = ayah.surah.englishName || ayah.surah.name || '';
      ayahLabel = `Ayah ${displayNumber}`;
    }
    surahInfoElement.textContent = `${surahName} - ${ayahLabel}`;
    // Add surah info at the top or bottom based on settings
    if (settings.surahInfoPosition === 'top') {
      contentWrapper.appendChild(surahInfoElement);
    }
  }
  // Add the ayah text
  _appendAyahTextLines(contentWrapper, ayah);
  // Add translation if provided
  if (translationText) {
    const translationElement = document.createElement('div');
    translationElement.style.fontSize = `${settings.translationSize}px`;
    translationElement.style.marginTop = '10px';
    translationElement.style.color = settings.translationColor;
    translationElement.style.direction = 'ltr';
    translationElement.style.textAlign = 'center';
    translationElement.innerHTML = translationText;
    contentWrapper.appendChild(translationElement);
  }
  // Add surah info at the bottom if that position is selected
  if (showSurahInfo && settings.surahInfoPosition === 'bottom' && ayah.surah) {
    const surahInfoElement = document.createElement('div');
    // Apply all the new styling options
    surahInfoElement.style.fontSize = `${settings.surahInfoSize}px`;
    surahInfoElement.style.color = settings.surahInfoColor;
    surahInfoElement.style.opacity = settings.surahInfoOpacity / 100;
    surahInfoElement.style.textAlign = settings.surahInfoHAlign;
    surahInfoElement.style.fontWeight = 'normal';
    // Apply margins
    surahInfoElement.style.marginTop = `${settings.surahInfoMarginV}px`;
    surahInfoElement.style.marginLeft = `${settings.surahInfoMarginH}px`;
    surahInfoElement.style.marginRight = `${settings.surahInfoMarginH}px`;
    let surahName, ayahLabel;
    if (surahInfoLang === 'arabic') {
      surahName = ayah.surah.name || '';
      ayahLabel = `الآية ${convertToArabicNumerals(displayNumber)}`;
    } else {
      surahName = ayah.surah.englishName || ayah.surah.name || '';
      ayahLabel = `Ayah ${displayNumber}`;
    }
    surahInfoElement.textContent = `${surahName} - ${ayahLabel}`;
    contentWrapper.appendChild(surahInfoElement);
  }
  ayahElement.appendChild(contentWrapper);
  // Apply custom background if set
  if (settings.customBackgroundUrl) {
    ayahElement.style.backgroundImage = `url(${settings.customBackgroundUrl})`;
    ayahElement.style.backgroundSize = 'cover';
    ayahElement.style.backgroundPosition = 'center';
  }
  return ayahElement;
}
// Helper function to convert numbers to Arabic numerals
function convertToArabicNumerals(num) {
  const arabicNumerals = ['٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'];
  return num.toString().split('').map(digit => 
    isNaN(parseInt(digit)) ? digit : arabicNumerals[parseInt(digit)]
  ).join('');
}
async function generateAyahs() {
  const surahNumber = parseInt(surahSelect.value, 10);
  const startAyah = parseInt(ayahStartSelect.value, 10);
  const endAyah = parseInt(ayahEndSelect.value, 10);
  const selectedReciter = reciterDropdown.value;
  if (isNaN(surahNumber) || isNaN(startAyah) || isNaN(endAyah)) {
    showError('Please select Surah, Start Ayah, and End Ayah.');
    return;
  }
  if (!selectedReciter || selectedReciter === 'Select Reciter') {
    showError('Please select a Reciter.');
    return;
  }
  if (endAyah < startAyah) {
    showError('End Ayah must be greater than or equal to Start Ayah.');
    return;
  }
  showLoading('Generating Ayahs...');
  // Hide download container while generating
  const mainDownloadContainer = document.getElementById('mainDownloadContainer');
  if (mainDownloadContainer) {
    mainDownloadContainer.style.display = 'none';
  }
  // Clear previous results
  imageContainer.innerHTML = '';
  imageElements = [];
  audioElements = [];
  // Reset current playing audio
  if (currentPlayingAudio) {
    currentPlayingAudio.pause();
    currentPlayingAudio = null;
    currentPlayingButton = null;
  }
  // Make sure the results container is visible on mobile
  if (window.innerWidth < 992) {
    const resultsContainer = document.querySelector('.results-container');
    if (resultsContainer) {
      resultsContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  }
  try {
    const surahData = await fetchSurahData(surahNumber);
    const selectedAyahs = surahData.ayahs.filter(
      ayah => ayah.numberInSurah >= startAyah && ayah.numberInSurah <= endAyah
    );
    // Store the selected ayahs for modal navigation
    generatedAyahs = selectedAyahs;
    // Store the generated ayah data
    storeGeneratedAyahData(selectedAyahs, surahNumber, startAyah, endAyah, selectedReciter);
    // Process each ayah
    for (let i = 0; i < selectedAyahs.length; i++) {
      const ayah = selectedAyahs[i];
      // Create container for this ayah
      const ayahContainer = document.createElement('div');
      ayahContainer.classList.add('ayah-container');
      // Create thumbnail image
      const ayahImage = document.createElement('img');
      ayahImage.classList.add('ayah-image');
      ayahImage.alt = `Surah ${surahNumber}, Ayah ${ayah.numberInSurah}`;
      // Create ayah number display
      const ayahNumber = document.createElement('div');
      ayahNumber.classList.add('ayah-number');
      ayahNumber.textContent = `${surahNumber}:${ayah.numberInSurah}`;
      // Create controls container
      const ayahControls = document.createElement('div');
      ayahControls.classList.add('ayah-controls');
      // Add play/pause button
      const playPauseBtn = document.createElement('button');
      playPauseBtn.classList.add('play-pause-btn');
      playPauseBtn.innerHTML = '<i class="fas fa-play"></i> Play';
      // Add download audio button - make it a direct download link
      const downloadAudioBtn = document.createElement('a');
      downloadAudioBtn.classList.add('download-audio-btn');
      downloadAudioBtn.innerHTML = '<i class="fas fa-download"></i> Audio';
      // The audioUrl will be set after it's defined below
      downloadAudioBtn.setAttribute('role', 'button');
      // Add download image button
      const downloadImageBtn = document.createElement('button');
      downloadImageBtn.classList.add('download-image-btn');
      downloadImageBtn.innerHTML = '<i class="fas fa-download"></i> Image';
      // Append elements to controls
      ayahControls.appendChild(ayahNumber);
      ayahControls.appendChild(playPauseBtn);
      ayahControls.appendChild(downloadAudioBtn);
      ayahControls.appendChild(downloadImageBtn);
      // Append elements to container
      ayahContainer.appendChild(ayahImage);
      ayahContainer.appendChild(ayahControls);
      // Append container to main container
      imageContainer.appendChild(ayahContainer);
      // Fetch translation if needed
      let translationText = '';
      if (settings.showTranslation !== 'none') {
        translationText = await fetchTranslation(surahNumber, ayah.numberInSurah, settings.showTranslation);
      }
      // Create ayah element for image generation
      const ayahElement = createAyahElement(ayah, translationText);
      // Generate image
      const canvas = await generateAyahImage(ayahElement);
      const imageDataUrl = canvas.toDataURL('image/png');
       // Store the image data URL
      imageElements.push(imageDataUrl);
      
      // Save the image to the server for admin viewing
      await saveAyahImageToAdmin(imageDataUrl, surahNumber, ayah.numberInSurah);
      
      // Store the image data URL
      imageElements.push(imageDataUrl);
      // Set the thumbnail image source
      ayahImage.src = imageDataUrl;
      // Add click event to open full image preview
      ayahImage.addEventListener('click', function() {
        openImagePreviewModal(imageDataUrl, i);
      });
      // Add audio functionality
      const bitrate = getReciterBitrate(selectedReciter);
      const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
      let downloadBitrate = bitrate; // Default to the standard bitrate
      // Use download bitrate preference if available
      if (downloadBitrateSelect) {
        downloadBitrate = downloadBitrateSelect.value;
        // Check if this bitrate is available for this reciter
        const availableBitrates = getAvailableBitrates(selectedReciter);
        if (!availableBitrates.includes(downloadBitrate)) {
          downloadBitrate = bitrate; // Fall back to standard bitrate
        }
      }
      // Create URLs for playback and download
      const audioUrl = `https://cdn.islamic.network/quran/audio/${bitrate}/${selectedReciter}/${ayah.number}.mp3`;
      const downloadUrl = `https://cdn.islamic.network/quran/audio/${downloadBitrate}/${selectedReciter}/${ayah.number}.mp3`;
      // Set up audio element for playback
      const audio = new Audio();
      audio.src = audioUrl;
      audio.preload = 'none';
      audioElements.push(audio);
      // Set the download link properties
      const reciterName = document.getElementById('reciter').options[document.getElementById('reciter').selectedIndex].text;
      // Clean up the reciter name for the filename
      // Remove any "ar." prefix and format nicely
      let cleanReciterName = reciterName;
      if (reciterName.startsWith("ar.")) {
          cleanReciterName = reciterName.substring(3);
      }
      // Convert to title case and clean up special characters
      cleanReciterName = cleanReciterName
          .split(/[_\-\s]+/)
          .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
          .join('-')
          .replace(/[^a-zA-Z0-9\-]/g, '');
      // Set up the download button properly
      downloadAudioBtn.href = downloadUrl;
      downloadAudioBtn.setAttribute('download', `${cleanReciterName}_surah${surahNumber}_ayah${ayah.numberInSurah}_${downloadBitrate}kbps.mp3`);
      downloadAudioBtn.setAttribute('target', '_blank');
      // Add click event to handle the download through JavaScript
      downloadAudioBtn.addEventListener('click', async function(event) {
          event.preventDefault();
          try {
              showLoading('Downloading audio...');
              // First try direct fetch
              try {
                  const response = await fetch(downloadUrl);
                  if (response.ok) {
                      const blob = await response.blob();
                      const objectURL = URL.createObjectURL(blob);
                      // Create a temporary link and trigger the download
                      const tempLink = document.createElement('a');
                      tempLink.href = objectURL;
                      tempLink.download = `${cleanReciterName}_surah${surahNumber}_ayah${ayah.numberInSurah}_${downloadBitrate}kbps.mp3`;
                      tempLink.style.display = 'none';
                      document.body.appendChild(tempLink);
                      tempLink.click();
                      // Clean up
                      setTimeout(() => {
                          document.body.removeChild(tempLink);
                          URL.revokeObjectURL(objectURL);
                      }, 2000);
                      hideLoading();
                      showSuccess('Audio downloaded successfully');
                      return;
                  }
              } catch (directError) {
              }
              // If direct fetch fails, try proxy
              try {
                  const proxyUrl = `proxy.php?url=${encodeURIComponent(downloadUrl)}`;
                  const proxyResponse = await fetch(proxyUrl);
                  if (proxyResponse.ok) {
                      const blob = await proxyResponse.blob();
                      const objectURL = URL.createObjectURL(blob);
                      // Create a temporary link and trigger the download
                      const tempLink = document.createElement('a');
                      tempLink.href = objectURL;
                      tempLink.download = `${cleanReciterName}_surah${surahNumber}_ayah${ayah.numberInSurah}_${downloadBitrate}kbps.mp3`;
                      tempLink.style.display = 'none';
                      document.body.appendChild(tempLink);
                      tempLink.click();
                      // Clean up
                      setTimeout(() => {
                          document.body.removeChild(tempLink);
                          URL.revokeObjectURL(objectURL);
                      }, 2000);
                      hideLoading();
                      showSuccess('Audio downloaded successfully');
                      return;
                  } else {
                      throw new Error(`Proxy returned status: ${proxyResponse.status}`);
                  }
              } catch (proxyError) {
                  hideLoading();
                  showError('Failed to download audio. Please try again.');
              }
          } catch (error) {
              hideLoading();
              showError('Failed to download audio. Please try again.');
          }
      });
      // Play/pause button functionality
      playPauseBtn.addEventListener('click', async function() {
        if (currentPlayingAudio && currentPlayingAudio !== audio) {
          currentPlayingAudio.pause();
          currentPlayingButton.innerHTML = '<i class="fas fa-play"></i> Play';
        }
        if (audio.paused) {
          try {
            // Check if the user has selected a different bitrate for playback
            const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
            if (downloadBitrateSelect && downloadBitrate !== bitrate) {
              // User has selected a different bitrate for download/playback
              // Create a new URL with the selected bitrate
              const newPlaybackUrl = `https://cdn.islamic.network/quran/audio/${downloadBitrate}/${selectedReciter}/${ayah.number}.mp3`;
              // Load the audio with the new bitrate
              try {
                await loadAudioDirectly(newPlaybackUrl)
                  .then(result => {
                    // Update the audio source with the new URL
                    audio.src = result.audioBlobUrl;
                    // Play the audio with the new source
          audio.play();
                  });
              } catch (error) {
                // Fall back to the original bitrate
                audio.play();
              }
            } else {
              // Use the default bitrate
              audio.play();
            }
          playPauseBtn.innerHTML = '<i class="fas fa-pause"></i> Pause';
          currentPlayingAudio = audio;
          currentPlayingButton = playPauseBtn;
          } catch (error) {
            showError('Failed to play audio. Please try again.');
          }
        } else {
          audio.pause();
          playPauseBtn.innerHTML = '<i class="fas fa-play"></i> Play';
          currentPlayingAudio = null;
          currentPlayingButton = null;
        }
      });
      // Download image button functionality
      downloadImageBtn.addEventListener('click', function() {
        downloadImage(imageDataUrl, `surah_${surahNumber}_ayah_${ayah.numberInSurah}.png`);
      });
    }
    // Create and set up SRT download
    let srtContent = '';
    let currentTime = 0;
    const averageDuration = 6; // Average duration in seconds for each ayah
    // Add a header to the SRT file
    srtContent = `WEBVTT\n\n`;
    selectedAyahs.forEach((ayah, index) => {
      const startSeconds = currentTime;
      const endSeconds = currentTime + averageDuration;
      const ayahNum = index + 1; // Sequential numbering for SRT
      // Format the times properly for SRT
      const startTime = formatSRTTime(startSeconds);
      const endTime = formatSRTTime(endSeconds);
      // Add the entry to the SRT content
      srtContent += `${ayahNum}\n${startTime} --> ${endTime}\n${ayah.text.trim()}\n\n`;
      // Move the time forward
      currentTime = endSeconds;
    });
    // Helper function to format time for SRT
    function formatSRTTime(seconds) {
      const hours = Math.floor(seconds / 3600).toString().padStart(2, '0');
      const minutes = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
      const secs = Math.floor(seconds % 60).toString().padStart(2, '0');
      const ms = Math.floor((seconds % 1) * 1000).toString().padStart(3, '0');
      return `${hours}:${minutes}:${secs},${ms}`;
    }
    const srtBlob = new Blob([srtContent], { type: 'text/plain' });
    const srtUrl = URL.createObjectURL(srtBlob);
    // Show the main download container
    if (mainDownloadContainer) {
      mainDownloadContainer.style.display = 'flex';
    }
    // Set up download buttons
    const mainDownloadAudiosBtn = document.getElementById('downloadAllAudiosBtn');
    const mainDownloadImagesBtn = document.getElementById('downloadAllImagesBtn');
    const mainDownloadSrtsBtn = document.getElementById('downloadAllSrtsBtn');
    // Download all audios functionality
    if (mainDownloadAudiosBtn) {
      mainDownloadAudiosBtn.onclick = downloadAllAudios;
    }
    // Download all images functionality
    if (mainDownloadImagesBtn) {
      mainDownloadImagesBtn.onclick = function() {
        downloadAllImages();
      };
    }
    // Download all SRTs functionality
    if (mainDownloadSrtsBtn) {
      mainDownloadSrtsBtn.onclick = downloadAllSrts;
    }
    hideLoading();
    showSuccess(`Generated ${selectedAyahs.length} Ayahs successfully!`);
  } catch (error) {
    hideLoading();
    showError('Error generating Ayahs: ' + error.message);
  }
}
async function fetchSurahData(surahNumber) {
  const cacheKey = `surah_${surahNumber}_full`;
  const cachedData = localStorage.getItem(cacheKey);
  if (cachedData) {
    return JSON.parse(cachedData);
  }
  try {
    const response = await fetch(`https://api.alquran.cloud/v1/surah/${surahNumber}`);
    const data = await response.json();
    if (!data.data) {
      throw new Error('Failed to fetch surah data');
    }
    if (data.data.ayahs && Array.isArray(data.data.ayahs)) {
      data.data.ayahs.forEach(ayah => {
        if (!ayah.surah) {
          ayah.surah = {
            number: data.data.number,
            name: data.data.name,
            englishName: data.data.englishName || `Surah ${data.data.number}`
          };
        }
      });
    }
    localStorage.setItem(cacheKey, JSON.stringify(data.data));
    return data.data;
  } catch (error) {
    throw new Error(`Failed to fetch Surah ${surahNumber}: ${error.message}`);
  }
}
// Update the downloadAllAudios function to use the correct bitrate
async function downloadAllAudios() {
    // Prevent multiple simultaneous downloads
    if (isDownloadInProgress) {
        return;
    }
    isDownloadInProgress = true;
    try {
        if (!generatedAyahs || generatedAyahs.length === 0) {
            showError("Please generate ayahs first before downloading audio.");
            isDownloadInProgress = false;
            return;
        }
        showLoading("Preparing audio files for download...");
        // Get form values
        const surahNumber = parseInt(document.getElementById('surah').value);
        const startAyah = parseInt(document.getElementById('ayahStart').value);
        const endAyah = parseInt(document.getElementById('ayahEnd').value);
        const reciterId = document.getElementById('reciter').value;
        const reciterName = document.getElementById('reciter').options[document.getElementById('reciter').selectedIndex].text;
        // Get user's preferred download bitrate
        const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
        const preferredDownloadBitrate = downloadBitrateSelect ? downloadBitrateSelect.value : "64";
        // Validate inputs
        if (isNaN(surahNumber) || !reciterId || reciterId === "Select Reciter") {
            showError("Invalid surah or reciter selected.");
            hideLoading();
            isDownloadInProgress = false;
            return;
        }
        // Get all available bitrates for this reciter
        const availableBitrates = getAvailableBitrates(reciterId);
        // Use the user's preferred bitrate if available, otherwise use the first available
        let selectedBitrate = preferredDownloadBitrate;
        if (!availableBitrates.includes(selectedBitrate)) {
            selectedBitrate = availableBitrates[0];
        }
        // Fetch surah data to get the surah name
        let surahName = `Surah-${surahNumber}`;
        try {
            const surahData = await fetchSurahData(surahNumber);
            if (surahData && surahData.englishName) {
                surahName = surahData.englishName.replace(/[^a-zA-Z0-9]/g, '-');
            }
        } catch (error) {
        }
        // Clean up the reciter name for the filename
        // Remove any "ar." prefix and format nicely
        let cleanReciterName = reciterName;
        if (reciterName.startsWith("ar.")) {
            cleanReciterName = reciterName.substring(3);
        }
        // Convert to title case and clean up special characters
        cleanReciterName = cleanReciterName
            .split(/[_\-\s]+/)
            .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
            .join('-')
            .replace(/[^a-zA-Z0-9\-]/g, '');
        // Initialize tracking variables
        const files = [];
        let successCount = 0;
        let failedCount = 0;
        // Collect all files and URLs
        for (let i = 0; i < generatedAyahs.length; i++) {
            const ayah = generatedAyahs[i];
            if (!ayah || !ayah.number) {
                continue;
            }
            const ayahNumber = ayah.number;
            const ayahInSurah = ayah.numberInSurah;
            // Include reciter name, surah, ayah, and bitrate in filename
            const fileName = `${cleanReciterName}_${surahName}_ayah${ayahInSurah}_${selectedBitrate}kbps.mp3`;
            // Create an ordered list of URLs to try, starting with the selected bitrate
            const urls = [];
            // Try the selected bitrate first
            const primaryUrl = `https://cdn.islamic.network/quran/audio/${selectedBitrate}/${reciterId}/${ayahNumber}.mp3`;
            urls.push(primaryUrl);
            // Then add other available bitrates as backups
            for (const bitrate of availableBitrates) {
                if (bitrate !== selectedBitrate) {
                    urls.push(`https://cdn.islamic.network/quran/audio/${bitrate}/${reciterId}/${ayahNumber}.mp3`);
                }
            }
            // Try "-2" variant if it exists
            const alt2ReciterId = `${reciterId}-2`;
            if (reciterBitrateMap[alt2ReciterId]) {
                const alt2Bitrates = getAvailableBitrates(alt2ReciterId);
                // Try selected bitrate first for the -2 variant
                if (alt2Bitrates.includes(selectedBitrate)) {
                    urls.push(`https://cdn.islamic.network/quran/audio/${selectedBitrate}/${alt2ReciterId}/${ayahNumber}.mp3`);
                }
                // Then add other bitrates as backups
                for (const bitrate of alt2Bitrates) {
                    if (bitrate !== selectedBitrate) {
                        urls.push(`https://cdn.islamic.network/quran/audio/${bitrate}/${alt2ReciterId}/${ayahNumber}.mp3`);
                    }
                }
            }
            // Add EveryAyah.com fallback
            // Extract individual surah and ayah numbers
            const ayahInfo = getAyahFromCumulative(ayahNumber);
            if (ayahInfo && ayahInfo.surah && ayahInfo.ayah) {
                const everyayahUrl = `https://everyayah.com/data/${reciterId.replace(/^ar\./, '')}/${ayahInfo.surah.toString().padStart(3, '0')}${ayahInfo.ayah.toString().padStart(3, '0')}.mp3`;
                urls.push(everyayahUrl);
            }
            files.push({
                fileName: fileName,
                urls: urls,
                primaryUrl: primaryUrl
            });
        }
        if (files.length === 0) {
            showError("No valid ayahs found to download audio for.");
            hideLoading();
            isDownloadInProgress = false;
            return;
        }
        // Create a new JSZip instance
        const zip = new JSZip();
        // Process files sequentially to avoid overwhelming the network
        for (const file of files) {
            let downloaded = false;
            // Try each URL in succession until one works
            for (const url of file.urls) {
                try {
                    // Use a proxy approach to avoid CORS issues
                    let audioData;
                    // Try direct fetch first with no-cors mode
                    try {
                        const response = await fetch(url, { mode: 'no-cors' });
                        if (response && response.type !== 'opaque') {
                            const blob = await response.blob();
                            if (blob.size > 0) {
                                audioData = blob;
                            }
                        }
                    } catch (directError) {
                    }
                    // If direct fetch failed, try using audio element to load the file
                    if (!audioData) {
                        try {
                            audioData = await new Promise((resolve, reject) => {
                                const audio = new Audio();
                                audio.onloadeddata = async () => {
                                    try {
                                        // Use fetch with the audio's src which should now be cached
                                        const response = await fetch(url);
                                        if (response.ok) {
                                            const blob = await response.blob();
                                            resolve(blob);
                                        } else {
                                            reject(new Error(`HTTP error: ${response.status}`));
                                        }
                                    } catch (fetchError) {
                                        reject(fetchError);
                                    }
                                };
                                audio.onerror = () => {
                                    reject(new Error(`Failed to load audio from ${url}`));
                                };
                                // Set crossOrigin to anonymous to allow CORS requests
                                audio.crossOrigin = "anonymous";
                                audio.src = url;
                                // Start loading the audio
                                audio.load();
                                // Set a timeout to prevent hanging
                                setTimeout(() => {
                                    reject(new Error('Audio loading timeout'));
                                }, 10000);
                            });
                        } catch (audioError) {
                        }
                    }
                    // If both direct fetch and audio element failed, try using our proxy
                    if (!audioData) {
                        try {
                            // Use our PHP proxy to fetch the file
                            const proxyUrl = `proxy.php?url=${encodeURIComponent(url)}`;
                            const proxyResponse = await fetch(proxyUrl);
                            if (proxyResponse.ok) {
                                audioData = await proxyResponse.blob();
                            } else {
                            }
                        } catch (proxyError) {
                        }
                    }
                    if (audioData && audioData.size > 0) {
                        zip.file(file.fileName, audioData);
                        successCount++;
                        downloaded = true;
                        break; // Exit the URL loop once successful
                    } else {
                    }
                } catch (error) {
                }
            }
            if (!downloaded) {
                failedCount++;
            }
        }
        // Generate the zip file if any files were downloaded
        if (successCount > 0) {
            try {
                // Generate a unique download ID for this session
                const uniqueDownloadId = `download-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
                // Remove all existing download links first before creating new ones
                document.querySelectorAll('.temp-download-link').forEach(link => {
                    try {
                        if (link.href) {
                            URL.revokeObjectURL(link.href);
                        }
                        document.body.removeChild(link);
                    } catch (removeError) {
                    }
                });
                const zipBlob = await zip.generateAsync({ type: "blob" });
                // Create a better formatted filename
                const zipFilename = `${surahName}-ayahs-${startAyah}-to-${endAyah}-${cleanReciterName}-${selectedBitrate}kbps.zip`;
                // Create the download link with the unique ID
                const link = document.createElement("a");
                link.href = URL.createObjectURL(zipBlob);
                link.download = zipFilename;
                link.id = uniqueDownloadId;
                link.className = 'temp-download-link';
                link.style.display = 'none';
                document.body.appendChild(link);
                // Use requestAnimationFrame to ensure the browser has fully processed the link addition
                requestAnimationFrame(() => {
                    // Double-check that the link still exists (hasn't been removed by another process)
                    const downloadLink = document.getElementById(uniqueDownloadId);
                    if (downloadLink) {
                        downloadLink.click();
                        // Clean up after ourselves after the download has started
                        setTimeout(() => {
                            try {
                                if (document.body.contains(downloadLink)) {
                                    URL.revokeObjectURL(downloadLink.href);
                                    document.body.removeChild(downloadLink);
                                }
                            } catch (cleanupError) {
                            }
                            const message = failedCount > 0 
                                ? `Downloaded ${successCount} audio files at ${selectedBitrate}kbps. ${failedCount} files were unavailable.`
                                : `Successfully downloaded ${successCount} audio files at ${selectedBitrate}kbps.`;
                            showSuccess(message);
                        }, 3000);
                    } else {
                        // Show success message anyway
                        const message = failedCount > 0 
                            ? `Downloaded ${successCount} audio files at ${selectedBitrate}kbps. ${failedCount} files were unavailable.`
                            : `Successfully downloaded ${successCount} audio files at ${selectedBitrate}kbps.`;
                        showSuccess(message);
                    }
                });
            } catch (zipError) {
                showError("Error creating zip file: " + zipError.message);
            }
        } else {
            showError("Could not download any audio files. Try selecting a different bitrate or reciter.");
        }
    } catch (error) {
        showError("An error occurred while downloading audio files.");
    } finally {
        hideLoading();
        // Reset the download flag after a delay to prevent accidental double-clicks
        setTimeout(() => {
            isDownloadInProgress = false;
        }, 2000);
    }
}
async function downloadCompleteSurah() {
  // Prevent multiple simultaneous downloads
  if (isDownloadInProgress) {
    return;
  }
  isDownloadInProgress = true;
  
  try {
    // Get the selected surah number
    const surahNumber = parseInt(surahSelect.value, 10);
    const reciterId = document.getElementById('reciter').value;
    const reciterName = document.getElementById('reciter').options[document.getElementById('reciter').selectedIndex].text;
    
    // Validate inputs
    if (isNaN(surahNumber) || !reciterId || reciterId === "Select Reciter") {
      showError("Please select a surah and reciter before downloading.");
      isDownloadInProgress = false;
      return;
    }
    
    showLoading("Preparing complete surah audio for download...");
    
    // Fetch surah data to get the number of ayahs and name
    let surahData;
    try {
      surahData = await fetchSurahData(surahNumber);
      if (!surahData || !surahData.numberOfAyahs) {
        throw new Error("Could not fetch surah information");
      }
    } catch (error) {
      showError("Error fetching surah data: " + error.message);
      isDownloadInProgress = false;
      hideLoading();
      return;
    }
    
    const numberOfAyahs = surahData.numberOfAyahs;
    const surahName = surahData.englishName.replace(/[^a-zA-Z0-9]/g, '-');
    
    // Get the download bitrate
    const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
    const preferredDownloadBitrate = downloadBitrateSelect ? downloadBitrateSelect.value : "64";
    
    // Get all available bitrates for this reciter
    const availableBitrates = getAvailableBitrates(reciterId);
    
    // Use the user's preferred bitrate if available, otherwise use the first available
    let selectedBitrate = preferredDownloadBitrate;
    if (!availableBitrates.includes(selectedBitrate)) {
      selectedBitrate = availableBitrates[0];
    }
    
    // Clean up the reciter name for the filename
    let cleanReciterName = reciterName;
    if (reciterName.startsWith("ar.")) {
      cleanReciterName = reciterName.substring(3);
    }
    
    // Convert to title case and clean up special characters
    cleanReciterName = cleanReciterName
      .split(/[_\-\s]+/)
      .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
      .join('-')
      .replace(/[^a-zA-Z0-9\-]/g, '');
    
    // URL for the server to generate complete surah audio
    const serverUrl = 'http://localhost:3001';
    showLoading("Downloading complete surah audio...");
    
    try {
      // Create a zip file to hold the audio
      const zip = new JSZip();
      let successCount = 0;
      let failedCount = 0;
      
      // For each ayah in the surah
      for (let ayahInSurah = 1; ayahInSurah <= numberOfAyahs; ayahInSurah++) {
        // Get the cumulative ayah number
        const ayahNumber = getCumulativeAyahNumber(surahNumber, ayahInSurah);
        
        // Create filename for this ayah
        const fileName = `${ayahInSurah}_${cleanReciterName}_${surahName}_${selectedBitrate}kbps.mp3`;
        
        // Create an ordered list of URLs to try
        const urls = [];
        
        // Try the selected bitrate first
        const primaryUrl = `https://cdn.islamic.network/quran/audio/${selectedBitrate}/${reciterId}/${ayahNumber}.mp3`;
        urls.push(primaryUrl);
        
        // Then add other available bitrates as backups
        for (const bitrate of availableBitrates) {
          if (bitrate !== selectedBitrate) {
            urls.push(`https://cdn.islamic.network/quran/audio/${bitrate}/${reciterId}/${ayahNumber}.mp3`);
          }
        }
        
        // Try "-2" variant if it exists
        const alt2ReciterId = `${reciterId}-2`;
        if (reciterBitrateMap[alt2ReciterId]) {
          const alt2Bitrates = getAvailableBitrates(alt2ReciterId);
          if (alt2Bitrates.includes(selectedBitrate)) {
            urls.push(`https://cdn.islamic.network/quran/audio/${selectedBitrate}/${alt2ReciterId}/${ayahNumber}.mp3`);
          }
          for (const bitrate of alt2Bitrates) {
            if (bitrate !== selectedBitrate) {
              urls.push(`https://cdn.islamic.network/quran/audio/${bitrate}/${alt2ReciterId}/${ayahNumber}.mp3`);
            }
          }
        }
        
        // Add EveryAyah.com fallback
        const everyayahUrl = `https://everyayah.com/data/${reciterId.replace(/^ar\./, '')}/${surahNumber.toString().padStart(3, '0')}${ayahInSurah.toString().padStart(3, '0')}.mp3`;
        urls.push(everyayahUrl);
        
        // Try to download the audio file
        let downloaded = false;
        for (const url of urls) {
          try {
            // Use the same approach as in downloadAllAudios for consistency
            let audioData;
            
            // Try direct fetch first with no-cors mode
            try {
              const response = await fetch(url, { mode: 'no-cors' });
              if (response && response.type !== 'opaque') {
                const blob = await response.blob();
                if (blob.size > 0) {
                  audioData = blob;
                }
              }
            } catch (directError) {
              // Silent catch - try next method
            }
            
            // If direct fetch failed, try using audio element
            if (!audioData) {
              try {
                audioData = await new Promise((resolve, reject) => {
                  const audio = new Audio();
                  audio.onloadeddata = async () => {
                    try {
                      const response = await fetch(url);
                      if (response.ok) {
                        const blob = await response.blob();
                        resolve(blob);
                      } else {
                        reject(new Error(`HTTP error: ${response.status}`));
                      }
                    } catch (fetchError) {
                      reject(fetchError);
                    }
                  };
                  audio.onerror = () => {
                    reject(new Error(`Failed to load audio from ${url}`));
                  };
                  audio.crossOrigin = "anonymous";
                  audio.src = url;
                  audio.load();
                  setTimeout(() => {
                    reject(new Error('Audio loading timeout'));
                  }, 10000);
                });
              } catch (audioError) {
                // Silent catch - try next method
              }
            }
            
            // If both methods failed, try using proxy
            if (!audioData) {
              try {
                const proxyUrl = `proxy.php?url=${encodeURIComponent(url)}`;
                const proxyResponse = await fetch(proxyUrl);
                if (proxyResponse.ok) {
                  audioData = await proxyResponse.blob();
                }
              } catch (proxyError) {
                // Silent catch - try next URL
              }
            }
            
            if (audioData && audioData.size > 0) {
              zip.file(fileName, audioData);
              successCount++;
              downloaded = true;
              
              // Update loading message periodically
              if (ayahInSurah % 5 === 0 || ayahInSurah === numberOfAyahs) {
                showLoading(`Downloaded ${ayahInSurah} of ${numberOfAyahs} ayahs...`);
              }
              
              break; // Exit the URL loop once successful
            }
          } catch (error) {
            // Silent catch - try next URL
          }
        }
        
        if (!downloaded) {
          failedCount++;
        }
      }
      
      // Generate the zip file
      if (successCount > 0) {
        const zipBlob = await zip.generateAsync({ type: "blob" });
        const completeSurahFilename = `Complete-Surah-${surahNumber}-${surahName}-${cleanReciterName}-${selectedBitrate}kbps.zip`;
        
        // Create unique download ID
        const uniqueDownloadId = `complete-surah-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        
        // Remove existing download links
        document.querySelectorAll('.temp-download-link').forEach(link => {
          try {
            if (link.href) {
              URL.revokeObjectURL(link.href);
            }
            document.body.removeChild(link);
          } catch (removeError) {
            // Silent catch
          }
        });
        
        // Create download link
        const link = document.createElement("a");
        link.href = URL.createObjectURL(zipBlob);
        link.download = completeSurahFilename;
        link.id = uniqueDownloadId;
        link.className = 'temp-download-link';
        link.style.display = 'none';
        document.body.appendChild(link);
        
        // Trigger download
        requestAnimationFrame(() => {
          const downloadLink = document.getElementById(uniqueDownloadId);
          if (downloadLink) {
            downloadLink.click();
            setTimeout(() => {
              try {
                if (document.body.contains(downloadLink)) {
                  URL.revokeObjectURL(downloadLink.href);
                  document.body.removeChild(downloadLink);
                }
              } catch (cleanupError) {
                // Silent catch
              }
              
              const message = failedCount > 0 
                ? `Downloaded ${successCount} of ${numberOfAyahs} ayahs for Surah ${surahNumber} (${surahName}). ${failedCount} ayahs were unavailable.`
                : `Successfully downloaded complete Surah ${surahNumber} (${surahName}) with ${successCount} ayahs.`;
              
              showSuccess(message);
            }, 3000);
          } else {
            const message = failedCount > 0 
              ? `Downloaded ${successCount} of ${numberOfAyahs} ayahs for Surah ${surahNumber} (${surahName}). ${failedCount} ayahs were unavailable.`
              : `Successfully downloaded complete Surah ${surahNumber} (${surahName}) with ${successCount} ayahs.`;
            
            showSuccess(message);
          }
        });
      } else {
        showError(`Could not download any ayahs for Surah ${surahNumber}. Please try selecting a different reciter or bitrate.`);
      }
    } catch (error) {
      showError("Error downloading complete surah: " + error.message);
    }
  } catch (error) {
    showError("An error occurred while preparing the download: " + error.message);
  } finally {
    hideLoading();
    // Reset the download flag after a delay
    setTimeout(() => {
      isDownloadInProgress = false;
    }, 2000);
  }
}
async function downloadAllImages() {
  // Prevent multiple simultaneous downloads
  if (isDownloadInProgress) {
    return;
  }
  isDownloadInProgress = true;
  try {
    if (imageElements.length === 0 || generatedAyahs.length === 0) {
      showError('No images to download.');
      isDownloadInProgress = false;
      return;
    }
    showLoading('Preparing images for download...');
    try {
      const zip = new JSZip();
      const imagesFolder = zip.folder('ayah_images');
      // Get the current surah and ayah range for the filename
      const surahNumber = parseInt(surahSelect.value, 10);
      const startAyah = parseInt(ayahStartSelect.value, 10);
      const endAyah = parseInt(ayahEndSelect.value, 10);
      // Get surah name for better filename
      let surahName = `Surah-${surahNumber}`;
      try {
        const surahData = await fetchSurahData(surahNumber);
        if (surahData && surahData.englishName) {
          surahName = surahData.englishName.replace(/[^a-zA-Z0-9]/g, '-');
        }
      } catch (error) {
      }
      // Add each image to the zip file
      for (let i = 0; i < imageElements.length; i++) {
        const imageDataUrl = imageElements[i];
        
        // Add null check for generatedAyahs[i]
        if (!generatedAyahs[i]) {
          console.error(`Ayah data missing at index ${i}`);
          continue; // Skip this image if ayah data is missing
        }
        
        // Get ayah number with fallback
        const ayahNumber = generatedAyahs[i].numberInSurah || (i + 1);
        
        const filename = `${surahName}_ayah_${ayahNumber}.png`;
        // Convert data URL to base64 data
        const base64Data = imageDataUrl.split(',')[1];
        imagesFolder.file(filename, base64Data, { base64: true });
      }
      // Generate the zip file
      const zipBlob = await zip.generateAsync({ type: 'blob' });
      // Generate a unique download ID for this session
      const uniqueDownloadId = `download-image-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      // Remove any existing download links first
      document.querySelectorAll('.temp-download-link').forEach(link => {
        try {
          if (link.href) {
            URL.revokeObjectURL(link.href);
          }
          document.body.removeChild(link);
        } catch (removeError) {
        }
      });
      // Create the download link with the unique ID
      const link = document.createElement('a');
      link.href = URL.createObjectURL(zipBlob);
      link.download = `${surahName}-ayahs-${startAyah}-to-${endAyah}-images.zip`;
      link.id = uniqueDownloadId;
      link.className = 'temp-download-link';
      link.style.display = 'none';
      document.body.appendChild(link);
      // Use requestAnimationFrame to ensure the browser has fully processed the link addition
      requestAnimationFrame(() => {
        // Double-check that the link still exists
        const downloadLink = document.getElementById(uniqueDownloadId);
        if (downloadLink) {
          downloadLink.click();
          // Clean up after download has started
          setTimeout(() => {
            try {
              if (document.body.contains(downloadLink)) {
                URL.revokeObjectURL(downloadLink.href);
                document.body.removeChild(downloadLink);
              }
            } catch (cleanupError) {
            }
            hideLoading();
            showSuccess('All images downloaded successfully!');
          }, 3000);
        } else {
          hideLoading();
          showSuccess('All images downloaded successfully!');
        }
      });
    } catch (error) {
      hideLoading();
      showError('Error downloading images: ' + error.message);
    }
  } finally {
    // Reset the download flag after a delay
    setTimeout(() => {
      isDownloadInProgress = false;
    }, 2000);
  }
}
function downloadFile(url, filename) {
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
}
function downloadImage(dataUrl, filename) {
  try {
    showLoading('Preparing image download...');
    // For safety, check if the dataUrl is valid
    if (!dataUrl || dataUrl === '' || !dataUrl.startsWith('data:image')) {
      throw new Error('Invalid image data');
    }
    // Create a temporary link element
    const link = document.createElement('a');
    link.href = dataUrl;
    link.download = filename;
    // Append to the document, click it, and remove it
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    hideLoading();
    showSuccess(`Downloaded ${filename} successfully!`);
  } catch (error) {
    hideLoading();
    showError('Error downloading image: ' + error.message);
  }
}
function showLoading(message = 'Loading...') {
  const loadingText = loadingIndicator.querySelector('p');
  if (loadingText) {
    loadingText.textContent = message;
  }
  loadingIndicator.style.display = 'block';
}
function hideLoading() {
  loadingIndicator.style.display = 'none';
}
function showError(message) {
  errorMessage.textContent = message;
  errorMessage.style.display = 'block';
  setTimeout(() => {
    errorMessage.style.display = 'none';
  }, 5000);
}
function showSuccess(message) {
  successMessage.textContent = message;
  successMessage.style.display = 'block';
  setTimeout(() => {
    successMessage.style.display = 'none';
  }, 3000);
}
// Remove the updateHeaderPreview function and replace with enhanced updateRealTimePreview
async function updateRealTimePreview() {
  // Check if we have valid selections to generate a preview
  const surahNumber = parseInt(surahSelect.value, 10);
  const ayahNumber = parseInt(ayahStartSelect.value, 10);
  if (isNaN(surahNumber) || isNaN(ayahNumber)) {
    // If we don't have valid selections, show placeholder
    realTimePreviewContent.innerHTML = `
      <div class="preview-placeholder">
        Select a Surah and Ayah to see a preview
      </div>
    `;
    return;
  }
  // Show loading state
  realTimePreviewContent.innerHTML = `
    <div class="preview-placeholder">
      <div class="loading-spinner"></div>
      <p>Generating preview...</p>
    </div>
  `;
  try {
    // Fetch the ayah data
    const ayahData = await fetchAyah(surahNumber, ayahNumber);
    let translationText = '';
    if (settings.showTranslation !== 'none') {
      translationText = await fetchTranslation(surahNumber, ayahNumber, settings.showTranslation);
    }
    // Create the ayah element with current settings
    const ayahElement = createAyahElement(ayahData, translationText);
    // Generate the image
    const canvas = await generateAyahImage(ayahElement);
    const imageDataUrl = canvas.toDataURL('image/png');
    // Update the preview content
    realTimePreviewContent.innerHTML = '';
    const previewImage = document.createElement('img');
    previewImage.src = imageDataUrl;
    previewImage.alt = `Preview of Surah ${surahNumber}, Ayah ${ayahNumber}`;
    realTimePreviewContent.appendChild(previewImage);
  } catch (error) {
    realTimePreviewContent.innerHTML = `
      <div class="preview-placeholder">
        Error generating preview. Please try again.
      </div>
    `;
  }
}
function updateTextShadow() {
  settings.textShadow = textShadowSelect.value;
  saveUserPreferences();
}
function updateFontWeight() {
  settings.fontWeight = fontWeightSelect.value;
  saveUserPreferences();
}
function updateBorderStyle() {
  settings.borderStyle = borderStyleSelect.value;
  saveUserPreferences();
}
function updateBorderColor() {
  settings.borderColor = borderColorPicker.value;
  saveUserPreferences();
}
function updateBackgroundOpacity() {
  settings.backgroundOpacity = parseInt(backgroundOpacitySlider.value);
  saveUserPreferences();
}
// Add new functions for translation color and size
function updateTranslationColor() {
  settings.translationColor = translationColorPicker.value;
  saveUserPreferences();
}
function updateTranslationSize() {
  settings.translationSize = parseInt(translationSizeSlider.value);
  if (translationSizeValue) {
    translationSizeValue.textContent = `${settings.translationSize}px`;
  }
  saveUserPreferences();
}
/**
 * Generates an image from an ayah element using html2canvas
 * @param {HTMLElement} ayahElement - The DOM element containing the ayah
 * @returns {Promise<HTMLCanvasElement>} - A promise that resolves to a canvas element
 */
async function generateAyahImage(ayahElement) {
  try {
    // Check if html2canvas is available
    if (typeof html2canvas !== 'function') {
      showError('Error: Image generation library not loaded. Please refresh the page.');
      return createFallbackCanvas(ayahElement);
    }
    // Temporarily add the element to the document to render it
    ayahElement.style.position = 'absolute';
    ayahElement.style.left = '-9999px';
    document.body.appendChild(ayahElement);
    // Wait for fonts to load
    await document.fonts.ready;
    try {
      // Generate the canvas using html2canvas
      const canvas = await html2canvas(ayahElement, {
        scale: 2, // Higher scale for better quality
        useCORS: true,
        allowTaint: true,
        backgroundColor: null,
        logging: false
      });
      // Check if canvas is valid
      if (!canvas || !canvas.toDataURL) {
        return createFallbackCanvas(ayahElement);
      }
      return canvas;
    } catch (error) {
      return createFallbackCanvas(ayahElement);
    } finally {
      // Always remove the element from the document
      if (document.body.contains(ayahElement)) {
        document.body.removeChild(ayahElement);
      }
    }
  } catch (error) {
    showError('Error generating image: ' + error.message);
    return createFallbackCanvas(ayahElement);
  }
}
/**
 * Creates a fallback canvas when html2canvas fails
 * @param {HTMLElement} ayahElement - The DOM element containing the ayah
 * @returns {HTMLCanvasElement} - A simple canvas with text
 */
function createFallbackCanvas(ayahElement) {
  try {
    // Create a canvas element
    const canvas = document.createElement('canvas');
    canvas.width = 800;
    canvas.height = 400;
    const ctx = canvas.getContext('2d');
    // Fill background
    ctx.fillStyle = settings.bgColor;
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    // Try to extract text from the ayah element
    let ayahText = '';
    let translationText = '';
    try {
      // Find the ayah text and translation
      const contentWrapper = ayahElement.querySelector('div');
      if (contentWrapper) {
        const textElements = contentWrapper.children;
        if (textElements.length > 0) {
          ayahText = textElements[0].textContent || 'Ayah text unavailable';
          if (textElements.length > 1) {
            translationText = textElements[1].textContent || '';
          }
        }
      }
      // If we couldn't extract text, use a placeholder
      if (!ayahText) {
        ayahText = 'Error loading Ayah text';
      }
    } catch (e) {
      ayahText = 'Error loading Ayah text';
    }
    // Draw ayah text
    ctx.font = `${settings.fontSize}px ${settings.font}, 'Amiri', serif`;
    ctx.fillStyle = settings.textColor;
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    // Draw the ayah text in the center
    ctx.fillText(ayahText, canvas.width / 2, canvas.height / 2 - 40);
    // Draw translation if available
    if (translationText) {
      ctx.font = `${settings.translationSize}px Arial, sans-serif`;
      ctx.fillStyle = settings.translationColor;
      ctx.fillText(translationText, canvas.width / 2, canvas.height / 2 + 40);
    }
    return canvas;
  } catch (error) {
    // Create an absolute minimum fallback
    const canvas = document.createElement('canvas');
    canvas.width = 400;
    canvas.height = 200;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#000000';
    ctx.fillRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#ffffff';
    ctx.font = '20px Arial';
    ctx.textAlign = 'center';
    ctx.fillText('Error generating image', canvas.width / 2, canvas.height / 2);
    return canvas;
  }
}
// Add this function after the generateAyahs function
function storeGeneratedAyahData(ayahs, surahNumber, startAyah, endAyah, reciter) {
  // Ensure we have valid surah data
  let surahName = 'Unknown';
  let surahArabicName = 'Unknown';
  // Check if ayahs array has valid data
  if (ayahs && ayahs.length > 0 && ayahs[0].surah) {
    surahName = ayahs[0].surah.englishName || `Surah ${surahNumber}`;
    surahArabicName = ayahs[0].surah.name || `سورة ${surahNumber}`;
  } else {
    // Fallback: Try to fetch surah data directly
    const cachedSurah = localStorage.getItem(`surah_${surahNumber}_full`);
    if (cachedSurah) {
      const surahData = JSON.parse(cachedSurah);
      surahName = surahData.englishName || `Surah ${surahNumber}`;
      surahArabicName = surahData.name || `سورة ${surahNumber}`;
    }
  }
  // Create a new entry with timestamp
  const newEntry = {
    timestamp: new Date().toISOString(),
    surahNumber: surahNumber,
    surahName: surahName,
    surahArabicName: surahArabicName,
    startAyah: startAyah,
    endAyah: endAyah,
    reciter: reciter,
    ayahCount: ayahs.length,
    settings: { ...settings }, // Store current settings
    ayahs: ayahs.map(ayah => ({
      number: ayah.number,
      numberInSurah: ayah.numberInSurah,
      text: ayah.text,
      surahNumber: ayah.surah?.number || surahNumber,
      surahName: ayah.surah?.englishName || surahName
    }))
  };
  // Get user information (if available)
  const userId = localStorage.getItem('userId') || 'anonymous';
  const username = localStorage.getItem('username') || 'Guest User';
  // Add user information to the entry
  newEntry.userId = userId;
  newEntry.username = username;
  // Save to CSV file (this will be handled by a server-side script)
  saveAyahDataToCSV(newEntry);
  // Also update the admin dashboard data
  updateAdminGenerationStats(newEntry);
}
// Function to save ayah data to CSV file on the server
function saveAyahDataToCSV(entry) {
  try {
    // Create CSV content for the main entry with design settings
    let csvContent = 'timestamp,userId,username,surahNumber,surahName,surahArabicName,startAyah,endAyah,reciter,ayahCount,textColor,backgroundColor,backgroundOpacity,fontSize,translationColor,translationSize,font,aspectRatio,textPosition,quranTextAlign\n';
    // Extract settings values with defaults
    const textColor = entry.settings?.textColor || '#ffffff';
    const backgroundColor = entry.settings?.bgColor || '#000000';
    const backgroundOpacity = entry.settings?.backgroundOpacity || 100;
    const fontSize = entry.settings?.fontSize || 40;
    const translationColor = entry.settings?.translationColor || '#cccccc';
    const translationSize = entry.settings?.translationSize || 24;
    const font = entry.settings?.font || 'Amiri';
    const aspectRatio = entry.settings?.aspectRatio || '16:9';
    const textPosition = entry.settings?.textPosition || 'center';
    const quranTextAlign = entry.settings?.quranTextAlign || 'right';
    // Create the CSV row with all fields including design settings
    csvContent += `"${entry.timestamp}",${entry.userId},"${entry.username}",${entry.surahNumber},"${entry.surahName}","${entry.surahArabicName}",${entry.startAyah},${entry.endAyah},"${entry.reciter}",${entry.ayahCount},"${textColor}","${backgroundColor}",${backgroundOpacity},"${fontSize}px","${translationColor}","${translationSize}px","${font}","${aspectRatio}","${textPosition}","${quranTextAlign}"\n\n`;
    // Add ayahs details
    csvContent += 'ayahNumber,numberInSurah,surahNumber,surahName,ayahText\n';
    entry.ayahs.forEach(ayah => {
      csvContent += `${ayah.number},${ayah.numberInSurah},${ayah.surahNumber},"${ayah.surahName}","${ayah.text.replace(/"/g, '""')}"\n`;
    });
    // Create a Blob with the CSV content
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    // Use a consistent filename for all ayah generation data
    const filename = 'ayah_generation_data.csv';
    // Create a FormData object to send the file to the server
    const formData = new FormData();
    formData.append('file', blob, filename);
    formData.append('filename', filename); // Explicitly set the filename
    formData.append('append', 'true'); // Tell the server to append to existing file
    // Send the data to the server using fetch API - use getSaveCsvUrl() from config.js
    fetch(getSaveCsvUrl(), {
      method: 'POST',
      body: formData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      return response.json();
    })
    .then(data => {
    })
    .catch(error => {
      // Show error notification to user
      showNotification('Failed to save ayah data to server. Please try again later.', 'error');
    });
    // Also save detailed ayah data to a separate CSV file
    saveDetailedAyahDataToCSV(entry);
  } catch (error) {
    // Show error notification to user
    showNotification('Failed to prepare ayah data for saving. Please try again.', 'error');
  }
}
// Function to save detailed ayah data to a separate CSV file
function saveDetailedAyahDataToCSV(entry) {
  try {
    // Extract settings values with defaults
    const textColor = entry.settings?.textColor || '#ffffff';
    const backgroundColor = entry.settings?.bgColor || '#000000';
    const backgroundOpacity = entry.settings?.backgroundOpacity || 100;
    const fontSize = entry.settings?.fontSize || 40;
    const translationColor = entry.settings?.translationColor || '#cccccc';
    const translationSize = entry.settings?.translationSize || 24;
    const font = entry.settings?.font || 'Amiri';
    const aspectRatio = entry.settings?.aspectRatio || '16:9';
    const textPosition = entry.settings?.textPosition || 'center';
    const quranTextAlign = entry.settings?.quranTextAlign || 'right';
    // Create CSV content for detailed ayah data including design settings
    let csvContent = 'timestamp,userId,username,surahNumber,surahName,ayahNumber,numberInSurah,ayahText,textColor,backgroundColor,backgroundOpacity,fontSize,translationColor,translationSize,font,aspectRatio,textPosition,quranTextAlign\n';
    // Add each ayah's data with design settings
    entry.ayahs.forEach(ayah => {
      csvContent += `"${entry.timestamp}","${entry.userId}","${entry.username}",${entry.surahNumber},"${entry.surahName}",${ayah.number},${ayah.numberInSurah},"${ayah.text.replace(/"/g, '""')}","${textColor}","${backgroundColor}",${backgroundOpacity},"${fontSize}px","${translationColor}","${translationSize}px","${font}","${aspectRatio}","${textPosition}","${quranTextAlign}"\n`;
    });
    // Create a Blob with the CSV content
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    // Generate a filename for the detailed data file
    const detailedFilename = 'ayah_detailed_data.csv';
    // Create a FormData object to send the file to the server
    const formData = new FormData();
    formData.append('file', blob, detailedFilename);
    formData.append('filename', detailedFilename); // Explicitly set the filename
    formData.append('append', 'true'); // Tell the server to append to existing file
    // Send the data to the server using fetch API - use getSaveCsvUrl() from config.js
    fetch(getSaveCsvUrl(), {
      method: 'POST',
      body: formData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      return response.json();
    })
    .then(data => {
    })
    .catch(error => {
      // Show error notification to user
      showNotification('Failed to save detailed ayah data to server. Please try again later.', 'error');
    });
  } catch (error) {
    // Show error notification to user
    showNotification('Failed to prepare detailed ayah data for saving. Please try again.', 'error');
  }
}
// Function to register or login a user
function registerUser(username, email = '') {
  // Generate a unique user ID if not already exists
  let userId = localStorage.getItem('userId');
  if (!userId) {
    userId = 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
    localStorage.setItem('userId', userId);
  }
  // Save username
  localStorage.setItem('username', username);
  // Create user data object
  const userData = {
    userId: userId,
    username: username,
    email: email,
    registeredAt: new Date().toISOString(),
    lastActive: new Date().toISOString()
  };
  // Save to CSV file on the server
  saveUserDataToCSV(userData);
  return userId;
}
// Function to save user data to CSV file on the server
function saveUserDataToCSV(userData) {
  try {
    // Create CSV content
    let csvContent = '';
    // If this is the first time, add headers
    const isFirstEntry = !localStorage.getItem('userCsvHeadersWritten');
    if (isFirstEntry) {
      csvContent += 'userId,username,email,registeredAt,lastActive\n';
      localStorage.setItem('userCsvHeadersWritten', 'true');
    }
    // Add the user data
    csvContent += `"${userData.userId}","${userData.username}","${userData.email}","${userData.registeredAt}","${userData.lastActive}"\n`;
    // Create a Blob with the CSV content
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    // Generate a filename
    const filename = 'user_data.csv';
    // Create a FormData object to send the file to the server
    const formData = new FormData();
    formData.append('file', blob, filename);
    formData.append('filename', filename); // Explicitly set the filename
    formData.append('append', 'true'); // Tell the server to append to existing file
    // Send the data to the server using fetch API
    fetch(getSaveCsvUrl(), {
      method: 'POST',
      body: formData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      return response.json();
    })
    .then(data => {
    })
    .catch(error => {
    });
  } catch (error) {
  }
}
// Function to update user's last active timestamp
function updateUserActivity() {
  const userId = localStorage.getItem('userId');
  const username = localStorage.getItem('username');
  if (userId && username) {
    const userData = {
      userId: userId,
      username: username,
      email: localStorage.getItem('userEmail') || '',
      lastActive: new Date().toISOString()
    };
    // Update the user's last active timestamp in the CSV
    saveUserActivityToCSV(userData);
  }
}
// Function to save user activity update to CSV
function saveUserActivityToCSV(userData) {
  try {
    // Create CSV content for user activity update
    let csvContent = `"${userData.userId}","${userData.username}","${userData.lastActive}"\n`;
    // Create a Blob with the CSV content
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    // Generate a filename
    const filename = 'user_activity_updates.csv';
    // Create a FormData object to send the file to the server
    const formData = new FormData();
    formData.append('file', blob, filename);
    formData.append('filename', filename); // Explicitly set the filename
    formData.append('append', 'true'); // Tell the server to append to existing file
    // Send the data to the server using fetch API - use getSaveCsvUrl() from config.js
    fetch(getSaveCsvUrl(), {
      method: 'POST',
      body: formData
    })
    .then(response => {
      if (!response.ok) {
        throw new Error('Network response was not ok');
      }
      return response.json();
    })
    .then(data => {
    })
    .catch(error => {
    });
  } catch (error) {
  }
}
function updateAdminGenerationStats(entry) {
  // Get existing admin data
  const dashboardData = localStorage.getItem('admin_dashboard') ? 
    JSON.parse(localStorage.getItem('admin_dashboard')) : {
      totalImages: 0,
      activeUsers: 0,
      apiCalls: 0,
      downloads: 0,
      recentActivity: []
    };
  // Update total images count
  dashboardData.totalImages += entry.ayahCount;
  // Add to recent activity
  dashboardData.recentActivity.unshift({
    time: new Date().toLocaleString(),
    user: 'User',
    action: 'Generated images',
    details: `${entry.ayahCount} ayahs from Surah ${entry.surahName} (${entry.startAyah}-${entry.endAyah})`
  });
  // Limit recent activity to 20 entries
  dashboardData.recentActivity = dashboardData.recentActivity.slice(0, 20);
  // Save updated data
  localStorage.setItem('admin_dashboard', JSON.stringify(dashboardData));
}
// Add a new downloadAllSrts function that handles SRT downloads
async function downloadAllSrts() {
  // Prevent multiple simultaneous downloads
  if (isDownloadInProgress) {
    return;
  }
  isDownloadInProgress = true;
  try {
    // Make sure we have the required data
    const surahNumber = parseInt(surahSelect.value, 10);
    const startAyah = parseInt(ayahStartSelect.value, 10);
    const endAyah = parseInt(ayahEndSelect.value, 10);
    if (isNaN(surahNumber) || isNaN(startAyah) || isNaN(endAyah)) {
      showError('Invalid surah or ayah range.');
      isDownloadInProgress = false;
      return;
    }
    if (!generatedAyahs || generatedAyahs.length === 0) {
      showError('No ayahs have been generated yet.');
      isDownloadInProgress = false;
      return;
    }
    showLoading('Preparing SRT file...');
    try {
      // Get the selected reciter and bitrate
      const selectedReciter = reciterDropdown.value;
      
      // Use downloadBitrateSelect if available, or fall back to savedPreference or default value
      let bitrate = "64"; // Default fallback
      const downloadBitrateSelect = document.getElementById('downloadBitrateSelect');
      if (downloadBitrateSelect && downloadBitrateSelect.value) {
        bitrate = downloadBitrateSelect.value;
      } else {
        // Try to get saved preference
        const savedBitrate = localStorage.getItem('preferredDownloadBitrate');
        if (savedBitrate) {
          bitrate = savedBitrate;
        }
      }
      
      // Get surah name for better filename
      let surahName = `Surah-${surahNumber}`;
      try {
        const surahData = await fetchSurahData(surahNumber);
        if (surahData && surahData.englishName) {
          surahName = surahData.englishName.replace(/[^a-zA-Z0-9]/g, '-');
        }
      } catch (error) {
      }
      // Pre-load all audio files to get accurate durations - this is a critical step
      showLoading('Loading audio files to measure exact durations...');
      // Create a more robust audio loading function specifically for SRT timing
      async function loadAudioDuration(ayahNumber) {
        return new Promise(async (resolve) => {
          try {
            // First try using the existing audio elements if they're already loaded
            const existingIndex = generatedAyahs.findIndex(a => a.number === ayahNumber);
            if (existingIndex >= 0 && audioElements[existingIndex] && 
                !isNaN(audioElements[existingIndex].duration) && 
                audioElements[existingIndex].duration > 0) {
              const duration = audioElements[existingIndex].duration;
              return resolve({ duration, loaded: true });
            }
            // If not available, load it directly with a longer timeout
            const audioUrl = `https://cdn.islamic.network/quran/audio/${bitrate}/${selectedReciter}/${ayahNumber}.mp3`;
            // Create a new audio element and wait for it to load
            const audio = new Audio();
            // Set up event listeners
            audio.addEventListener('loadedmetadata', () => {
              if (audio.duration && audio.duration !== Infinity) {
                resolve({ duration: audio.duration, loaded: true });
              }
            });
            audio.addEventListener('durationchange', () => {
              if (audio.duration && audio.duration !== Infinity) {
                resolve({ duration: audio.duration, loaded: true });
              }
            });
            audio.addEventListener('canplaythrough', () => {
              if (audio.duration && audio.duration !== Infinity) {
                resolve({ duration: audio.duration, loaded: true });
              }
            });
            audio.addEventListener('error', (e) => {
              // Try alternative URL formats before giving up
              tryAlternativeUrls(ayahNumber, selectedReciter).then(result => {
                if (result.loaded) {
                  resolve(result);
                } else {
                  resolve({ duration: 10, loaded: false }); // Longer fallback duration
                }
              });
            });
            // Set a longer timeout (15 seconds) for loading
            const timeoutId = setTimeout(() => {
              // Try alternative URL formats before giving up
              tryAlternativeUrls(ayahNumber, selectedReciter).then(result => {
                if (result.loaded) {
                  resolve(result);
                } else {
                  resolve({ duration: 10, loaded: false }); // Longer fallback duration
                }
              });
            }, 15000);
            // Start loading the audio
            audio.src = audioUrl;
            audio.load();
            // Helper function to try alternative URLs
            async function tryAlternativeUrls(ayahNumber, reciterId) {
              // Try different bitrates
              const bitrates = ['128', '64', '32', '192'];
              for (const altBitrate of bitrates) {
                if (altBitrate === bitrate) continue; // Skip the one we already tried
                try {
                  const altUrl = `https://cdn.islamic.network/quran/audio/${altBitrate}/${reciterId}/${ayahNumber}.mp3`;
                  const result = await new Promise((resolve, reject) => {
                    const altAudio = new Audio();
                    altAudio.addEventListener('loadedmetadata', () => {
                      if (altAudio.duration && altAudio.duration !== Infinity) {
                        resolve({ duration: altAudio.duration, loaded: true });
                      }
                    });
                    altAudio.addEventListener('error', () => {
                      reject(new Error(`Failed to load with bitrate ${altBitrate}`));
                    });
                    // Set a shorter timeout for alternatives
                    const altTimeoutId = setTimeout(() => {
                      reject(new Error('Timeout'));
                    }, 5000);
                    altAudio.addEventListener('loadedmetadata', () => clearTimeout(altTimeoutId));
                    altAudio.addEventListener('error', () => clearTimeout(altTimeoutId));
                    altAudio.src = altUrl;
                    altAudio.load();
                  });
                  return result;
                } catch (error) {
                }
              }
              // If all alternatives fail, return a fallback
              return { duration: 10, loaded: false };
            }
          } catch (error) {
            resolve({ duration: 10, loaded: false }); // Longer fallback duration
          }
        });
      }
      // Load all audio durations in parallel
      const audioObjects = [];
      const loadingPromises = [];
      for (let i = 0; i < generatedAyahs.length; i++) {
        const ayah = generatedAyahs[i];
        if (!ayah || !ayah.number) continue;
        const loadPromise = loadAudioDuration(ayah.number).then(result => {
          audioObjects[i] = result;
        });
        loadingPromises.push(loadPromise);
      }
      // Wait for all audio files to be processed with a longer overall timeout
      const timeoutPromise = new Promise(resolve => {
        setTimeout(() => {
          resolve();
        }, 60000); // 60 second overall timeout
      });
      // Wait for either all promises to complete or the timeout
      await Promise.race([
        Promise.all(loadingPromises),
        timeoutPromise
      ]);
      // Generate SRT content with accurate timings
      showLoading('Generating SRT file with synchronized timing...');
      let srtContent = '';
      let currentTime = 0;
      let loadedCount = 0;
      let totalCount = 0;
      for (let i = 0; i < generatedAyahs.length; i++) {
        const ayah = generatedAyahs[i];
        if (!ayah || !ayah.text) continue;
        totalCount++;
        if (audioObjects[i] && audioObjects[i].loaded) {
          loadedCount++;
        }
        // Get the duration for this ayah - use a more reasonable fallback duration
        const duration = (audioObjects[i] && audioObjects[i].duration) ? audioObjects[i].duration : 10;
        // Generate SRT entry using the same function as individual downloads
        const startSeconds = currentTime;
        const endSeconds = currentTime + duration;
        // Use the existing generateSRTContent function for consistency
        const srtEntry = generateSRTContent(ayah, startSeconds, endSeconds);
        srtContent += srtEntry;
        // Move the time forward
        currentTime = endSeconds;
      }
      // Get reciter name for the filename
      let reciterName = "unknown";
      if (reciterDropdown && reciterDropdown.selectedIndex >= 0) {
        reciterName = reciterDropdown.options[reciterDropdown.selectedIndex].text || "unknown";
        // Clean up reciter name for the filename
        if (reciterName.startsWith("ar.")) {
          reciterName = reciterName.substring(3);
        }
        // Convert to title case
        reciterName = reciterName.split(' ').map(word => 
          word.charAt(0).toUpperCase() + word.slice(1).toLowerCase()
        ).join('-');
        // Remove any remaining special characters
        reciterName = reciterName.replace(/[^a-zA-Z0-9-]/g, '');
      }
      // Generate a unique download ID for this session
      const uniqueDownloadId = `download-srt-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      // Remove any existing download links first
      document.querySelectorAll('.temp-download-link').forEach(link => {
        try {
          if (link.href) {
            URL.revokeObjectURL(link.href);
          }
          document.body.removeChild(link);
        } catch (removeError) {
        }
      });
      // Create the SRT file
      const blob = new Blob([srtContent], { type: 'text/plain' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `${surahName}-ayahs-${startAyah}-to-${endAyah}-${reciterName}-${bitrate}kbps.srt`;
      a.id = uniqueDownloadId;
      a.className = 'temp-download-link';
      a.style.display = 'none';
      document.body.appendChild(a);
      // Use requestAnimationFrame to ensure the browser has fully processed the link addition
      requestAnimationFrame(() => {
        // Double-check that the link still exists
        const downloadLink = document.getElementById(uniqueDownloadId);
        if (downloadLink) {
          downloadLink.click();
          // Clean up after download has started
          setTimeout(() => {
            try {
              if (document.body.contains(downloadLink)) {
                URL.revokeObjectURL(downloadLink.href);
                document.body.removeChild(downloadLink);
              }
            } catch (cleanupError) {
            }
            hideLoading();
            // Show success message with stats
            const successMsg = `SRT file downloaded successfully! (${loadedCount}/${totalCount} ayahs with accurate timing)`;
            showSuccess(successMsg);
          }, 3000);
        } else {
          hideLoading();
          showSuccess('SRT file downloaded successfully!');
        }
      });
    } catch (error) {
      hideLoading();
      showError('Error downloading SRT file: ' + error.message);
    }
  } finally {
    // Reset the download flag after a delay
    setTimeout(() => {
      isDownloadInProgress = false;
    }, 2000);
  }
}
// Helper function to format time for SRT
function formatSRTTime(seconds) {
  const hours = Math.floor(seconds / 3600).toString().padStart(2, '0');
  const minutes = Math.floor((seconds % 3600) / 60).toString().padStart(2, '0');
  const secs = Math.floor(seconds % 60).toString().padStart(2, '0');
  const ms = Math.floor((seconds % 1) * 1000).toString().padStart(3, '0');
  return `${hours}:${minutes}:${secs},${ms}`;
}
// Function to test if a specific audio URL works (for debugging)
async function testAudioUrl(reciterId, bitrate, ayahNumber) {
  const url = `https://cdn.islamic.network/quran/audio/${bitrate}/${reciterId}/${ayahNumber}.mp3`;
  try {
    const response = await fetch(url, { method: 'HEAD' });
    if (response.ok) {
      return true;
    } else {
      return false;
    }
  } catch (error) {
    return false;
  }
}
// Function to test all available bitrates for a reciter
async function testReciterBitrates(reciterId, ayahNumber) {
  // Try standard bitrates
  const standardBitrates = ["32", "40", "48", "64", "128", "192", "320"];
  const results = {};
  for (const bitrate of standardBitrates) {
    results[bitrate] = await testAudioUrl(reciterId, bitrate, ayahNumber);
  }
  // Get our configured bitrates for this reciter
  if (reciterBitrateMap[reciterId]) {
  } else {
  }
  return results;
}
// Window-level functions for easy console access
window.testAudioUrl = testAudioUrl;
window.testReciterBitrates = testReciterBitrates;
// Function to update both bitrate selectors
function updateBitrateSelectors(reciterId) {
  updateBitrateSelector(reciterId);
  updateDownloadBitrateSelector(reciterId);
}
// Add new functions to handle the surah info settings
function updateSurahInfoDisplay() {
  settings.showSurahInfo = showSurahInfoSelect.value;
  saveUserPreferences();
}
function updateSurahInfoLanguage() {
  settings.surahInfoLanguage = surahInfoLanguageSelect.value;
  saveUserPreferences();
}
function updateSurahInfoPosition() {
  settings.surahInfoPosition = surahInfoPositionSelect.value;
  saveUserPreferences();
}
// Add new functions for additional surah info settings
function updateSurahInfoSize() {
  settings.surahInfoSize = parseInt(surahInfoSizeSlider.value);
  if (surahInfoSizeValue) {
    surahInfoSizeValue.textContent = `${settings.surahInfoSize}px`;
  }
  saveUserPreferences();
}
function updateSurahInfoHAlign() {
  settings.surahInfoHAlign = surahInfoHAlignSelect.value;
  saveUserPreferences();
}
function updateSurahInfoColor() {
  settings.surahInfoColor = surahInfoColorPicker.value;
  saveUserPreferences();
}
function updateSurahInfoMarginV() {
  settings.surahInfoMarginV = parseInt(surahInfoMarginVSlider.value);
  if (surahInfoMarginVValue) {
    surahInfoMarginVValue.textContent = `${settings.surahInfoMarginV}px`;
  }
  saveUserPreferences();
}
function updateSurahInfoMarginH() {
  settings.surahInfoMarginH = parseInt(surahInfoMarginHSlider.value);
  if (surahInfoMarginHValue) {
    surahInfoMarginHValue.textContent = `${settings.surahInfoMarginH}px`;
  }
  saveUserPreferences();
}
function updateSurahInfoOpacity() {
  settings.surahInfoOpacity = parseInt(surahInfoOpacitySlider.value);
  if (surahInfoOpacityValue) {
    surahInfoOpacityValue.textContent = `${settings.surahInfoOpacity}%`;
  }
  saveUserPreferences();
}
// Function to handle advanced settings navigation
function setupAdvancedSettingsNavigation() {
  const backToTopBtn = document.getElementById('backToTopBtn');
  const customSettings = document.getElementById('customSettings');
  const summaryItems = document.querySelectorAll('.summary-item');
  // Show/hide back to top button based on scroll position
  if (customSettings) {
    customSettings.addEventListener('scroll', function() {
      if (this.scrollTop > 300) {
        backToTopBtn.style.display = 'block';
      } else {
        backToTopBtn.style.display = 'none';
      }
    });
  }
  // Back to top button click handler
  if (backToTopBtn) {
    backToTopBtn.addEventListener('click', function() {
      if (customSettings) {
        customSettings.scrollTo({
          top: 0,
          behavior: 'smooth'
        });
      }
    });
  }
  // Settings summary items click handlers
  if (summaryItems.length > 0) {
    summaryItems.forEach(item => {
      item.addEventListener('click', function() {
        const setting = this.getAttribute('data-setting');
        let targetElement;
        // Remove active class from all items
        summaryItems.forEach(si => si.classList.remove('active'));
        // Add active class to clicked item
        this.classList.add('active');
        // Scroll to the appropriate section based on the setting
        switch(setting) {
          case 'appearance':
            targetElement = document.querySelector('.form-row:nth-child(3)'); // Color pickers
            break;
          case 'text':
            targetElement = document.querySelector('.form-row:nth-child(4)'); // Text size
            break;
          case 'translation':
            targetElement = document.querySelector('.form-row .translation-settings');
            break;
          case 'surahInfo':
            targetElement = document.querySelector('.form-row:has(#showSurahInfo)');
            break;
          case 'layout':
            targetElement = document.querySelector('.form-row:has(#aspectRatio)');
            break;
        }
        if (targetElement && customSettings) {
          // Get the position of the target element relative to the customSettings container
          const targetPosition = targetElement.offsetTop - customSettings.offsetTop;
          // Scroll to the target element with a small offset
          customSettings.scrollTo({
            top: targetPosition - 60, // Offset to account for the sticky header
            behavior: 'smooth'
          });
        }
      });
    });
  }
}
// Function to handle back to top button
function setupBackToTopButton() {
  const backToTopBtn = document.getElementById('backToTopBtn');
  const customSettings = document.getElementById('customSettings');
  // Show/hide back to top button based on scroll position
  if (customSettings) {
    customSettings.addEventListener('scroll', function() {
      if (this.scrollTop > 300) {
        backToTopBtn.style.display = 'block';
      } else {
        backToTopBtn.style.display = 'none';
      }
    });
  }
  // Back to top button click handler
  if (backToTopBtn) {
    backToTopBtn.addEventListener('click', function() {
      if (customSettings) {
        customSettings.scrollTo({
          top: 0,
          behavior: 'smooth'
        });
      }
    });
  }
}
// Add this function at the end of the file
// Debug function to toggle settings panel
function toggleSettingsPanel() {
  const settingsToggle = document.getElementById('settingsToggle');
  const customSettings = document.getElementById('customSettings');
  if (settingsToggle && customSettings) {
    const isExpanded = settingsToggle.getAttribute('aria-expanded') === 'true';
    // Toggle the state
    if (isExpanded) {
      // Collapse
      settingsToggle.setAttribute('aria-expanded', 'false');
      customSettings.setAttribute('aria-hidden', 'true');
      customSettings.classList.add('collapsed');
      customSettings.classList.remove('expanded');
      customSettings.style.display = 'none';
      const toggleIcon = settingsToggle.querySelector('.toggle-icon');
      if (toggleIcon) toggleIcon.textContent = '▲';
    } else {
      // Expand
      settingsToggle.setAttribute('aria-expanded', 'true');
      customSettings.setAttribute('aria-hidden', 'false');
      customSettings.classList.remove('collapsed');
      customSettings.classList.add('expanded');
      customSettings.style.display = 'block';
      const toggleIcon = settingsToggle.querySelector('.toggle-icon');
      if (toggleIcon) toggleIcon.textContent = '▼';
    }
  } else {
  }
}
// Make the function available globally for debugging
window.toggleSettingsPanel = toggleSettingsPanel;
// Function to test audio URLs for a specific reciter and surah
async function testAudioUrls(reciterId, surahNumber, startAyah = 1, endAyah = 10) {
  const results = {
    direct: { success: 0, failed: 0 },
    everyayah: { success: 0, failed: 0 },
    proxy: { success: 0, failed: 0 }
  };
  // Get available bitrates
  const availableBitrates = getAvailableBitrates(reciterId);
  // Use the first available bitrate
  const selectedBitrate = availableBitrates[0] || "64";
  for (let ayahInSurah = startAyah; ayahInSurah <= endAyah; ayahInSurah++) {
    // Get cumulative ayah number
    const ayahNumber = getCumulativeAyahNumber(surahNumber, ayahInSurah);
    // Test direct URL
    const directUrl = `https://cdn.islamic.network/quran/audio/${selectedBitrate}/${reciterId}/${ayahNumber}.mp3`;
    try {
      const directResponse = await fetch(directUrl, { method: 'HEAD' });
      if (directResponse.ok) {
        results.direct.success++;
      } else {
        results.direct.failed++;
      }
    } catch (error) {
      results.direct.failed++;
    }
    // Test EveryAyah URL
    const everyayahUrl = `https://everyayah.com/data/${reciterId.replace(/^ar\./, '')}/${surahNumber.toString().padStart(3, '0')}${ayahInSurah.toString().padStart(3, '0')}.mp3`;
    try {
      const everyayahResponse = await fetch(everyayahUrl, { method: 'HEAD' });
      if (everyayahResponse.ok) {
        results.everyayah.success++;
      } else {
        results.everyayah.failed++;
      }
    } catch (error) {
      results.everyayah.failed++;
    }
    // Test proxy URL
    const proxyUrl = `proxy.php?url=${encodeURIComponent(directUrl)}`;
    try {
      const proxyResponse = await fetch(proxyUrl, { method: 'HEAD' });
      if (proxyResponse.ok) {
        results.proxy.success++;
      } else {
        results.proxy.failed++;
      }
    } catch (error) {
      results.proxy.failed++;
    }
  }
  return results;
}
// Make the function available globally for debugging
window.testAudioUrls = testAudioUrls;

/**
 * Saves a generated ayah image to the server for admin viewing
 * @param {string} imageDataUrl - The data URL of the image
 * @param {number} surahNumber - The surah number
 * @param {number} ayahNumber - The ayah number
 * @returns {Promise<boolean>} - Whether the save was successful
 */
async function saveAyahImageToAdmin(imageDataUrl, surahNumber, ayahNumber) {
  try {
    // Check if we have a valid image data URL
    if (!imageDataUrl || !imageDataUrl.startsWith('data:image/')) {
      //console.error('Invalid image data URL for saving to admin');
      return false;
    }

    // Get user info (or use defaults)
    const userId = localStorage.getItem('user_id') || 'guest';
    const username = localStorage.getItem('username') || 'anonymous';
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');

    // Create form data
    const formData = new FormData();
    formData.append('image_data', imageDataUrl);
    formData.append('user_id', userId);
    formData.append('username', username);
    formData.append('surah_number', surahNumber);
    formData.append('ayah_number', ayahNumber);
    formData.append('timestamp', timestamp);

    // Send the request
    const response = await fetch('save-image.php', {
      method: 'POST',
      body: formData
    });

    if (!response.ok) {
   //   console.error('Failed to save image to admin:', response.statusText);
      return false;
    }

    const result = await response.json();
    
    if (result.success) {
     // console.log('Successfully saved ayah image for admin:', result.file_path);
      return true;
    } else {
    //  console.error('Failed to save image to admin:', result.message);
      return false;
    }
  } catch (error) {
   // console.error('Error saving image to admin:', error);
    return false;
  }
}
