import express from 'express';
import path from 'path';
import { fileURLToPath } from 'url';
import { decryptReferralToken, normalizeReferralCode } from '../utils/referralLinkCrypto.js';

const router = express.Router();
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

/**
 * Download page with referral code
 * GET /download?ref=RS3_ABC123
 */
router.get('/', (req, res) => {
    const encryptedToken = typeof req.query.r === 'string' ? req.query.r : '';
    const plainCode = typeof req.query.ref === 'string' ? req.query.ref : '';
    const referralCode = decryptReferralToken(encryptedToken) || normalizeReferralCode(plainCode) || '';
    const forwardedProto = req.get('x-forwarded-proto');
    const protocol = forwardedProto ? forwardedProto.split(',')[0].trim() : req.protocol;
    const serverUrl = `${protocol}://${req.get('host')}`;
    const safeReferralCode = String(referralCode).replace(/[&<>"']/g, (char) => {
        const map = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' };
        return map[char] || char;
    });
  
  const html = `
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Download CinePass - RS³ FILMS</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        
        .container {
            background: white;
            border-radius: 20px;
            padding: 40px;
            max-width: 500px;
            width: 100%;
            box-shadow: 0 20px 60px rgba(0,0,0,0.3);
            text-align: center;
        }
        
        .logo {
            font-size: 48px;
            margin-bottom: 10px;
        }
        
        h1 {
            color: #333;
            font-size: 28px;
            margin-bottom: 10px;
        }
        
        .subtitle {
            color: #666;
            font-size: 16px;
            margin-bottom: 30px;
        }
        
        .referral-box {
            background: linear-gradient(135deg, #ffd166 0%, #f4a261 100%);
            border-radius: 15px;
            padding: 20px;
            margin: 20px 0;
            ${referralCode ? '' : 'display: none;'}
        }
        
        .referral-label {
            color: #333;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 8px;
        }
        
        .referral-code {
            font-size: 32px;
            font-weight: bold;
            color: #fff;
            letter-spacing: 2px;
            text-shadow: 0 2px 4px rgba(0,0,0,0.2);
            margin-bottom: 10px;
        }
        
        .copy-btn {
            background: white;
            color: #f4a261;
            border: none;
            padding: 10px 20px;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s;
        }
        
        .copy-btn:active {
            transform: scale(0.95);
        }
        
        .download-btn {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            padding: 18px 40px;
            border-radius: 12px;
            font-size: 18px;
            font-weight: bold;
            cursor: pointer;
            width: 100%;
            margin: 20px 0;
            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
            transition: transform 0.2s;
        }
        
        .download-btn:hover {
            transform: translateY(-2px);
        }
        
        .download-btn:active {
            transform: translateY(0);
        }
        
        .info {
            color: #666;
            font-size: 14px;
            margin-top: 20px;
            line-height: 1.6;
        }
        
        .steps {
            text-align: left;
            margin-top: 20px;
            padding: 20px;
            background: #f8f9fa;
            border-radius: 10px;
        }
        
        .step {
            margin: 10px 0;
            color: #555;
            font-size: 14px;
        }
        
        .step-number {
            display: inline-block;
            width: 24px;
            height: 24px;
            background: #667eea;
            color: white;
            border-radius: 50%;
            text-align: center;
            line-height: 24px;
            font-size: 12px;
            font-weight: bold;
            margin-right: 10px;
        }
        
        .toast {
            position: fixed;
            bottom: 20px;
            left: 50%;
            transform: translateX(-50%);
            background: #4caf50;
            color: white;
            padding: 15px 30px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            display: none;
            animation: slideUp 0.3s ease;
        }
        
        @keyframes slideUp {
            from { bottom: -50px; opacity: 0; }
            to { bottom: 20px; opacity: 1; }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">🎬</div>
        <h1>RS³ FILMS</h1>
        <p class="subtitle">Download CinePass App</p>
        
        <div class="referral-box" id="referralBox">
            <div class="referral-label">Your Referral Code</div>
            <div class="referral-code" id="referralCode">${safeReferralCode}</div>
            <button class="copy-btn" onclick="copyCode()">📋 Copy Code</button>
        </div>
        
        <button class="download-btn" onclick="tryOpenApp()" style="background: linear-gradient(135deg, #45b649 0%, #43a047 100%); ${referralCode ? '' : 'display: none;'}">
            📱 Open CinePass (if installed)
        </button>
        
        <button class="download-btn" onclick="downloadApp()">
            ⬇️ Download CinePass
        </button>
        
        <div class="steps">
            <div class="step">
                <span class="step-number">1</span>
                Click "Download CinePass" button above
            </div>
            <div class="step">
                <span class="step-number">2</span>
                Install the APK (allow installation from unknown sources)
            </div>
            <div class="step">
                <span class="step-number">3</span>
                Open the app and register
            </div>
            ${referralCode ? `
            <div class="step">
                <span class="step-number">4</span>
                Referral code <strong>${safeReferralCode}</strong> is auto-copied! 📋
            </div>
            <div class="step">
                <span class="step-number">5</span>
                Earn bonus coins! 🎉
            </div>
            ` : ''}
        </div>
        
        <div class="info">
            Download size: ~20 MB<br>
            Minimum Android version: 8.0 (Oreo)
        </div>
    </div>
    
    <div class="toast" id="toast">Referral code copied!</div>
    
    <script>
        // Store referral code in localStorage for later retrieval
        const referralCode = ${JSON.stringify(referralCode)};

        function copyReferralCodeSilently() {
            if (!referralCode) return Promise.resolve(false);

            if (navigator.clipboard && window.isSecureContext) {
                return navigator.clipboard.writeText(referralCode)
                    .then(() => true)
                    .catch(() => false);
            }

            // Fallback (works better during user gesture on many browsers)
            try {
                const textarea = document.createElement('textarea');
                textarea.value = referralCode;
                textarea.style.position = 'fixed';
                textarea.style.opacity = '0';
                document.body.appendChild(textarea);
                textarea.focus();
                textarea.select();
                const copied = document.execCommand('copy');
                document.body.removeChild(textarea);
                return Promise.resolve(copied);
            } catch (_) {
                return Promise.resolve(false);
            }
        }

        if (referralCode) {
            localStorage.setItem('cinepass_referral', referralCode);
            // Best-effort auto-copy on load
            copyReferralCodeSilently().catch(() => {});
        }
        
        function tryOpenApp() {
            if (!referralCode) {
                showToast('No referral code found');
                return;
            }
            
            // Try to open app with deep link
            const deepLink = 'cinepass://register?ref=' + encodeURIComponent(referralCode);
            window.location.href = deepLink;
            
            // Show instructions after a delay
            setTimeout(() => {
                showToast("If app didn't open, download it first below");
            }, 2000);
        }
        
        function copyCode() {
            if (navigator.clipboard) {
                navigator.clipboard.writeText(referralCode).then(() => {
                    showToast('Referral code copied to clipboard!');
                }).catch(() => {
                    showToast('Failed to copy code');
                });
            } else {
                // Fallback for older browsers
                const textarea = document.createElement('textarea');
                textarea.value = referralCode;
                document.body.appendChild(textarea);
                textarea.select();
                document.execCommand('copy');
                document.body.removeChild(textarea);
                showToast('Referral code copied!');
            }
        }
        
        async function downloadApp() {
            // Re-copy during user click (more reliable on mobile browsers)
            await copyReferralCodeSilently();

            // Trigger APK download
            window.location.href = '${serverUrl}/download/app';
            
            // Show instructions
            setTimeout(() => {
                showToast('Download started! Check your notifications.');
            }, 500);
        }
        
        function showToast(message) {
            const toast = document.getElementById('toast');
            toast.textContent = message;
            toast.style.display = 'block';
            
            setTimeout(() => {
                toast.style.display = 'none';
            }, 3000);
        }
        
        // Log analytics event
        if (referralCode) {
            console.log('Referral code:', referralCode);
        }
    </script>
</body>
</html>
  `;
  
  res.send(html);
});

/**
 * Download APK file
 * GET /download/app
 */
router.get('/app', (req, res) => {
  const apkPath = path.join(__dirname, '../../apk/cinepass.apk');

    // Avoid stale responses during rapid retries from mobile browsers.
    res.set({
        'Cache-Control': 'no-store, no-cache, must-revalidate, proxy-revalidate',
        Pragma: 'no-cache',
        Expires: '0',
        'Surrogate-Control': 'no-store',
    });
  
  res.download(apkPath, 'CinePass.apk', (err) => {
    if (err) {
            // Client cancelled/closed connection mid-download; this is normal on mobile.
            if (err.code === 'ECONNABORTED' || err.code === 'ECONNRESET') {
                console.warn('APK download aborted by client');
                return;
            }

            // If headers/body already started, never try to send another response.
            if (res.headersSent) {
                console.error('APK download error after headers sent:', err);
                return;
            }

      console.error('Error downloading APK:', err);

            if (err.code === 'ENOENT') {
                return res.status(404).json({
                    success: false,
                    message: 'APK file not found. Please build and place the APK in server/apk/ folder.',
                });
            }

            return res.status(500).json({
                success: false,
                message: 'Failed to start APK download. Please try again.',
            });
    }
  });
});

export default router;
