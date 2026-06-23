require('dotenv').config();
const express = require('express');
const cors = require('cors');
const path = require('path');
const fs = require('fs');

const authRoutes = require('./routes/auth');
const dashboardRoutes = require('./routes/dashboard');
const membersRoutes = require('./routes/members');
const feedRoutes = require('./routes/feed');
const offersRoutes = require('./routes/offers');
const redemptionsRoutes = require('./routes/redemptions');
const pointsRulesRoutes = require('./routes/pointsRules');
const cmsRoutes = require('./routes/cms');
const adminsRoutes = require('./routes/admins');
const analyticsRoutes = require('./routes/analytics');
const uploadRoutes = require('./routes/upload');
const appRoutes = require('./routes/app');

const app = express();
const PORT = process.env.PORT || 6055;

console.log('DATABASE_URL at startup:', process.env.DATABASE_URL);

// ── Request/Response Logger ─────────────────────────────
app.use((req, res, next) => {
  const start = Date.now();
  res.on('finish', () => {
    const duration = Date.now() - start;
    console.log(`[${new Date().toISOString()}] ${req.method} ${req.originalUrl} -> ${res.statusCode} (${duration}ms)`);
  });
  next();
});

// ── Middleware ──────────────────────────────────────────
app.use(cors());
app.use(express.json());
app.use('/uploads', express.static('uploads'));

// ── Routes ─────────────────────────────────────────────
app.use('/v1/auth', authRoutes);
app.use('/v1/dashboard', dashboardRoutes);
app.use('/v1/members', membersRoutes);
app.use('/v1/feed', feedRoutes);
app.use('/v1/offers', offersRoutes);
app.use('/v1/redemptions', redemptionsRoutes);
app.use('/v1/points-rules', pointsRulesRoutes);
app.use('/v1/cms', cmsRoutes);
app.use('/v1/admins', adminsRoutes);
app.use('/v1/analytics', analyticsRoutes);
app.use('/v1/upload', uploadRoutes);
app.use('/v1/app', appRoutes);

// ── Health ─────────────────────────────────────────────
app.get('/v1/health', (req, res) => res.json({ status: 'ok', timestamp: new Date().toISOString() }));

// ── Referral Download Landing Page ─────────────────────
// Accessible at:  http://<server>/dl?ref=RS3_XXXXXX
app.get('/dl', (req, res) => {
  const refCode = (req.query.ref || '').toString().replace(/[^A-Z0-9_]/gi, '').slice(0, 20);
  const baseUrl = `${req.protocol}://${req.headers.host}`;
  const apkUrl  = `${baseUrl}/apk`;
  const deepLink = refCode ? `cinepass://register?ref=${encodeURIComponent(refCode)}` : null;

  res.setHeader('Content-Type', 'text/html; charset=utf-8');
  res.send(`<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>RS³ Films – Join the Community</title>
  <style>
    *{margin:0;padding:0;box-sizing:border-box}
    body{background:#0A0806;color:#FBF0D8;font-family:Georgia,serif;min-height:100vh;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:24px}
    .card{background:#1A1408;border:1px solid #3A2E18;border-radius:20px;padding:40px 32px;max-width:400px;width:100%;text-align:center}
    .logo{font-size:26px;font-weight:300;letter-spacing:4px;color:#C9973A;margin-bottom:4px}
    .tagline{font-size:9px;letter-spacing:2px;color:#7A6A50;margin-bottom:28px}
    .invite{font-size:15px;color:#E8D8B0;margin-bottom:6px}
    .ref-box{background:#0E0A06;border:1px solid #C9973A44;border-radius:10px;padding:12px 20px;margin:16px 0 24px}
    .ref-label{font-size:9px;letter-spacing:2px;color:#7A6A50;margin-bottom:4px}
    .ref-code{font-size:22px;letter-spacing:4px;color:#C9973A;font-weight:bold}
    .btn{display:block;width:100%;padding:15px;border-radius:12px;font-size:15px;font-weight:bold;text-decoration:none;margin-bottom:10px;cursor:pointer;border:none;font-family:Georgia,serif}
    .btn-gold{background:linear-gradient(135deg,#7A5C1E,#C9A84C,#E8C96A);color:#0E0C08}
    .btn-outline{background:transparent;border:1px solid #3A2E18;color:#C9973A;font-size:13px;padding:12px}
    .note{font-size:11px;color:#5A4A30;margin-top:18px;line-height:1.7}
    .copied{color:#4A9A6A;font-size:12px;margin:6px 0 0;display:none}
    .steps{text-align:left;margin:18px 0 4px;border-top:1px solid #2A2010;padding-top:16px}
    .steps li{font-size:12px;color:#9A8A68;line-height:2;list-style:none;padding-left:4px}
    .steps li::before{content:"→  ";color:#C9973A}
  </style>
</head>
<body>
  <div class="card">
    <div class="logo">RS³ FILMS</div>
    <div class="tagline">FAN COMMUNITY · EXCLUSIVE ACCESS</div>
    ${refCode
      ? `<div class="invite">🎬 You've been invited!</div>
         <div class="ref-box">
           <div class="ref-label">YOUR REFERRAL CODE</div>
           <div class="ref-code" id="rc">${refCode}</div>
         </div>
         <div class="copied" id="cpMsg">✓ Code copied to clipboard!</div>`
      : `<div class="invite">Join India's most exclusive film fan club</div><br>`
    }
    <a href="${apkUrl}" class="btn btn-gold" id="dlBtn">⬇&nbsp; Download App (Android)</a>
    ${deepLink ? `<a href="${deepLink}" class="btn btn-outline">Already installed? Open App →</a>` : ''}
    <ol class="steps">
      <li>Download and install the APK</li>
      ${refCode ? '<li>Your referral code is auto-filled on registration</li>' : ''}
      <li>Register and enjoy exclusive access</li>
    </ol>
    <div class="note">Android 8.0+ required • Enable "Install from unknown sources"</div>
  </div>
  ${refCode ? `<script>
    function copyCode(){
      var c='${refCode}';
      if(navigator.clipboard&&navigator.clipboard.writeText){
        navigator.clipboard.writeText(c).then(function(){
          var m=document.getElementById('cpMsg');if(m)m.style.display='block';
        }).catch(function(){});
      } else {
        var t=document.createElement('textarea');t.value=c;
        document.body.appendChild(t);t.select();
        try{document.execCommand('copy');}catch(e){}
        document.body.removeChild(t);
        var m=document.getElementById('cpMsg');if(m)m.style.display='block';
      }
    }
    window.addEventListener('load',function(){setTimeout(copyCode,600);});
    document.getElementById('dlBtn').addEventListener('click',copyCode);
  </script>` : ''}
</body>
</html>`);
});

// ── APK File Download ──────────────────────────────────
// Place your APK at:  backend/public/fanverse.apk
app.get('/apk', (req, res) => {
  const apkPath = path.join(__dirname, '..', 'public', 'fanverse.apk');
  if (!fs.existsSync(apkPath)) {
    return res.status(404).send(
      'APK not available yet. Upload <code>fanverse.apk</code> to the <code>backend/public/</code> folder.'
    );
  }
  res.download(apkPath, 'fanverse.apk');
});

// ── App Links Verification (needed for https:// deep links) ─
// Fill in your app's SHA-256 fingerprint from:
//   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android
app.get('/.well-known/assetlinks.json', (req, res) => {
  res.json([{
    relation: ['delegate_permission/common.handle_all_urls'],
    target: {
      namespace: 'android_app',
      package_name: 'com.cinepass',
      sha256_cert_fingerprints: [
        // Replace with actual fingerprint e.g. "AB:CD:..."
        process.env.APP_SHA256_FINGERPRINT || ''
      ]
    }
  }]);
});

// ── Error handler ──────────────────────────────────────
app.use((err, req, res, next) => {
  console.error('--- Express Error Handler ---');
  console.error('Request:', req.method, req.originalUrl);
  console.error('Body:', req.body);
  console.error('Params:', req.params);
  console.error('Query:', req.query);
  console.error('Error Stack:', err.stack);
  res.status(err.status || 500).json({
    error: { code: err.code || 'SERVER_ERROR', message: err.message || 'Internal server error' }
  });
});

app.listen(PORT, () => {
  console.log(`🎬 RS³ Films API running on http://localhost:${PORT}`);
});
