import { useState, useEffect } from "react";

/* ─────────────────────────────────────────────────────────────────────────────
   RS³ FILMS — Complete Android App UI
   All screens: Splash → Onboarding → Register → Login → Home → Referral →
   Wallet → Profile → Offer Detail → Referral Tree → My Rewards → Settings
   Aesthetic: Warm cinematic gold × deep ebony — editorial luxury × Bollywood soul
────────────────────────────────────────────────────────────────────────────── */

const FONTS = `
@import url('https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;0,500;0,600;1,300;1,400&family=Outfit:wght@300;400;500;600;700&display=swap');
`;

const CSS = `
  *,*::before,*::after{box-sizing:border-box;margin:0;padding:0}
  :root{
    --gold:#C9973A; --gold2:#E8B84B; --gold3:#F5D78E; --goldpale:#FBF4E3;
    --ink:#0E0C08; --ink2:#1E1A10; --ink3:#2E2818; --ink4:#3D3520;
    --mid:#6B5C3A; --muted:#9A8A6A; --faint:#E8E0CC; --surface:#FDFAF3;
    --white:#FFFFFF; --red:#C0392B; --green:#1E7B4A; --blue:#1A3A7A;
    --screen-w:390px; --screen-h:844px;
    --font-display:'Cormorant Garamond',serif;
    --font-body:'Outfit',sans-serif;
    --r:20px; --r2:14px; --r3:8px;
    --shadow:0 8px 32px rgba(14,12,8,0.18);
    --shadow2:0 2px 12px rgba(14,12,8,0.10);
  }
  body{font-family:var(--font-body);background:#1A1410;min-height:100vh;display:flex;align-items:flex-start;justify-content:center;padding:32px 16px;gap:24px;flex-wrap:wrap;}
  .phone{
    width:var(--screen-w);height:var(--screen-h);
    background:var(--surface);border-radius:40px;overflow:hidden;
    position:relative;flex-shrink:0;
    box-shadow:0 32px 80px rgba(0,0,0,0.5),0 0 0 1px rgba(255,255,255,0.06);
  }
  .phone::before{content:'';position:absolute;top:14px;left:50%;transform:translateX(-50%);width:120px;height:5px;background:#111;border-radius:3px;z-index:100;}
  .screen{width:100%;height:100%;overflow:hidden;position:relative;animation:fadeIn .4s ease both;}
  @keyframes fadeIn{from{opacity:0;transform:scale(.98)}to{opacity:1;transform:scale(1)}}
  @keyframes slideUp{from{opacity:0;transform:translateY(20px)}to{opacity:1;transform:translateY(0)}}
  @keyframes shimmer{0%{background-position:-200% 0}100%{background-position:200% 0}}
  @keyframes pulse{0%,100%{opacity:1}50%{opacity:.5}}
  @keyframes spin{to{transform:rotate(360deg)}}
  @keyframes float{0%,100%{transform:translateY(0)}50%{transform:translateY(-8px)}}
  @keyframes glow{0%,100%{box-shadow:0 0 20px rgba(201,151,58,0.3)}50%{box-shadow:0 0 40px rgba(201,151,58,0.6)}}

  /* Status bar */
  .status-bar{display:flex;justify-content:space-between;align-items:center;padding:16px 28px 8px;font-size:11px;font-weight:600;letter-spacing:.3px;position:relative;z-index:10;}
  .status-dark{color:var(--ink4);}
  .status-light{color:rgba(255,255,255,0.85);}

  /* Nav bar */
  .bottom-nav{position:absolute;bottom:0;left:0;right:0;display:flex;background:var(--white);border-top:1px solid var(--faint);padding:8px 0 20px;z-index:50;}
  .nav-tab{flex:1;display:flex;flex-direction:column;align-items:center;gap:3px;cursor:pointer;padding:4px 0;transition:all .2s;}
  .nav-tab span{font-size:9px;font-family:var(--font-body);font-weight:600;letter-spacing:.8px;text-transform:uppercase;}
  .nav-tab.active svg,.nav-tab.active span{color:var(--gold);}
  .nav-tab svg,.nav-tab span{color:var(--muted);transition:color .2s;}

  /* Buttons */
  .btn-gold{background:linear-gradient(135deg,#A0701A,#C9973A,#E8B84B);color:var(--ink);border:none;border-radius:var(--r2);padding:15px 28px;font-family:var(--font-body);font-size:14px;font-weight:600;letter-spacing:.5px;cursor:pointer;width:100%;transition:all .2s;}
  .btn-gold:hover{filter:brightness(1.05);}
  .btn-ghost{background:transparent;color:var(--mid);border:1.5px solid var(--faint);border-radius:var(--r2);padding:14px 28px;font-family:var(--font-body);font-size:14px;font-weight:500;cursor:pointer;width:100%;transition:all .2s;}
  .btn-icon{width:44px;height:44px;border-radius:12px;border:1.5px solid var(--faint);background:var(--white);display:flex;align-items:center;justify-content:center;cursor:pointer;flex-shrink:0;}

  /* Input */
  .inp-wrap{margin-bottom:14px;}
  .inp-label{font-size:10px;font-weight:700;letter-spacing:1.5px;text-transform:uppercase;color:var(--muted);margin-bottom:6px;display:block;}
  .inp{width:100%;padding:13px 16px;border:1.5px solid var(--faint);border-radius:var(--r3);background:var(--white);font-family:var(--font-body);font-size:14px;color:var(--ink2);outline:none;transition:border-color .2s;}
  .inp:focus{border-color:var(--gold);}
  .inp-row{display:flex;gap:10px;}

  /* Cards */
  .card{background:var(--white);border-radius:var(--r);border:1px solid var(--faint);overflow:hidden;}
  .card-gold{background:linear-gradient(135deg,#1A1208,#2E2214);border-radius:var(--r);overflow:hidden;position:relative;}

  /* Scrollable content area */
  .scroll-area{overflow-y:auto;scrollbar-width:none;}
  .scroll-area::-webkit-scrollbar{display:none;}

  /* Coin chip */
  .coin-chip{display:inline-flex;align-items:center;gap:5px;background:linear-gradient(135deg,#FBF0D8,#F5E4B8);border:1px solid #E8C96A;border-radius:20px;padding:4px 10px;font-size:11px;font-weight:700;color:#8A6020;}

  /* Badge */
  .badge{display:inline-block;padding:3px 9px;border-radius:20px;font-size:9px;font-weight:700;letter-spacing:1px;text-transform:uppercase;}

  /* Progress bar */
  .prog-track{height:6px;background:var(--faint);border-radius:3px;overflow:hidden;}
  .prog-fill{height:100%;border-radius:3px;background:linear-gradient(90deg,var(--gold),var(--gold2));transition:width .6s ease;}

  /* Shimmer loader */
  .shimmer{background:linear-gradient(90deg,#F0E8D4 25%,#FAF4E8 50%,#F0E8D4 75%);background-size:200% 100%;animation:shimmer 1.5s infinite;}

  /* Section heading */
  .sec-head{font-family:var(--font-display);font-size:22px;font-weight:400;color:var(--ink2);letter-spacing:.5px;}

  /* Avatar */
  .avatar{border-radius:50%;object-fit:cover;border:2px solid var(--gold);}

  /* Tab pills */
  .tab-pills{display:flex;background:var(--faint);border-radius:10px;padding:3px;gap:2px;}
  .tab-pill{flex:1;padding:7px;text-align:center;border-radius:8px;font-size:11px;font-weight:600;letter-spacing:.5px;cursor:pointer;transition:all .2s;color:var(--muted);}
  .tab-pill.active{background:var(--white);color:var(--gold);box-shadow:var(--shadow2);}

  /* label tags */
  .tag-gold{background:rgba(201,151,58,.12);color:var(--gold);border-radius:4px;padding:2px 8px;font-size:10px;font-weight:700;letter-spacing:.8px;}
  .tag-green{background:rgba(30,123,74,.1);color:var(--green);border-radius:4px;padding:2px 8px;font-size:10px;font-weight:700;letter-spacing:.8px;}
  .tag-ink{background:rgba(30,26,16,.08);color:var(--ink3);border-radius:4px;padding:2px 8px;font-size:10px;font-weight:700;letter-spacing:.8px;}

  /* Screen label (outside phone for nav) */
  .screen-label{font-family:var(--font-body);font-size:11px;font-weight:600;letter-spacing:1.5px;text-transform:uppercase;color:rgba(255,255,255,0.45);text-align:center;margin-top:10px;}

  /* Global nav */
  .global-nav{position:fixed;top:0;left:0;right:0;z-index:1000;background:rgba(18,14,10,0.92);backdrop-filter:blur(20px);border-bottom:1px solid rgba(201,151,58,.2);padding:14px 24px;display:flex;align-items:center;gap:12px;flex-wrap:wrap;}
  .gn-btn{background:transparent;border:1px solid rgba(201,151,58,.3);color:rgba(255,255,255,.7);padding:6px 14px;border-radius:6px;font-family:var(--font-body);font-size:11px;font-weight:500;letter-spacing:.5px;cursor:pointer;transition:all .2s;white-space:nowrap;}
  .gn-btn:hover,.gn-btn.active{background:rgba(201,151,58,.15);color:var(--gold2);border-color:var(--gold);}
  .gn-logo{font-family:var(--font-display);font-size:18px;color:var(--gold);letter-spacing:3px;margin-right:8px;}
`;

/* ── Icons ─────────────────────────────────────────────────────────────────── */
const Icon = ({ name, size = 20, color = "currentColor" }) => {
  const icons = {
    home: <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/>,
    home2: <><path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></>,
    share: <><circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/><line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/></>,
    wallet: <><rect x="1" y="4" width="22" height="16" rx="2"/><line x1="1" y1="10" x2="23" y2="10"/></>,
    user: <><path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/><circle cx="12" cy="7" r="4"/></>,
    coin: <><circle cx="12" cy="12" r="10"/><path d="M12 6v12M8 12h8" strokeWidth="1.5"/></>,
    gift: <><polyline points="20 12 20 22 4 22 4 12"/><rect x="2" y="7" width="20" height="5"/><line x1="12" y1="22" x2="12" y2="7"/><path d="M12 7H7.5a2.5 2.5 0 010-5C11 2 12 7 12 7z"/><path d="M12 7h4.5a2.5 2.5 0 000-5C13 2 12 7 12 7z"/></>,
    chevron: <polyline points="9 18 15 12 9 6"/>,
    chevronL: <polyline points="15 18 9 12 15 6"/>,
    chevronD: <polyline points="6 9 12 15 18 9"/>,
    star: <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>,
    check: <polyline points="20 6 9 17 4 12"/>,
    copy: <><rect x="9" y="9" width="13" height="13" rx="2"/><path d="M5 15H4a2 2 0 01-2-2V4a2 2 0 012-2h9a2 2 0 012 2v1"/></>,
    bell: <><path d="M18 8A6 6 0 006 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 01-3.46 0"/></>,
    camera: <><path d="M23 19a2 2 0 01-2 2H3a2 2 0 01-2-2V8a2 2 0 012-2h4l2-3h6l2 3h4a2 2 0 012 2z"/><circle cx="12" cy="13" r="4"/></>,
    edit: <><path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 013 3L12 15l-4 1 1-4 9.5-9.5z"/></>,
    settings: <><circle cx="12" cy="12" r="3"/><path d="M19.4 15a1.65 1.65 0 00.33 1.82l.06.06a2 2 0 010 2.83 2 2 0 01-2.83 0l-.06-.06a1.65 1.65 0 00-1.82-.33 1.65 1.65 0 00-1 1.51V21a2 2 0 01-2 2 2 2 0 01-2-2v-.09A1.65 1.65 0 009 19.4a1.65 1.65 0 00-1.82.33l-.06.06a2 2 0 01-2.83 0 2 2 0 010-2.83l.06-.06A1.65 1.65 0 004.68 15a1.65 1.65 0 00-1.51-1H3a2 2 0 01-2-2 2 2 0 012-2h.09A1.65 1.65 0 004.6 9a1.65 1.65 0 00-.33-1.82l-.06-.06a2 2 0 010-2.83 2 2 0 012.83 0l.06.06A1.65 1.65 0 009 4.68a1.65 1.65 0 001-1.51V3a2 2 0 012-2 2 2 0 012 2v.09a1.65 1.65 0 001 1.51 1.65 1.65 0 001.82-.33l.06-.06a2 2 0 012.83 0 2 2 0 010 2.83l-.06.06A1.65 1.65 0 0019.4 9a1.65 1.65 0 001.51 1H21a2 2 0 012 2 2 2 0 01-2 2h-.09a1.65 1.65 0 00-1.51 1z"/></>,
    trophy: <><path d="M6 9H4.5a2.5 2.5 0 010-5H6"/><path d="M18 9h1.5a2.5 2.5 0 000-5H18"/><path d="M4 22h16"/><path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22"/><path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22"/><path d="M18 2H6v7a6 6 0 0012 0V2z"/></>,
    phone: <><path d="M22 16.92v3a2 2 0 01-2.18 2 19.79 19.79 0 01-8.63-3.07A19.5 19.5 0 013.07 8.81a19.79 19.79 0 01-3.07-8.63A2 2 0 012 0h3a2 2 0 012 1.72c.127.96.361 1.903.7 2.81a2 2 0 01-.45 2.11L6.09 7.91a16 16 0 006 6l1.27-1.27a2 2 0 012.11-.45c.907.339 1.85.573 2.81.7A2 2 0 0122 16.92z"/></>,
    mail: <><path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/><polyline points="22,6 12,13 2,6"/></>,
    lock: <><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0110 0v4"/></>,
    eye: <><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></>,
    info: <><circle cx="12" cy="12" r="10"/><line x1="12" y1="16" x2="12" y2="12"/><line x1="12" y1="8" x2="12.01" y2="8"/></>,
    logout: <><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></>,
    shield: <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"/>,
    film: <><rect x="2" y="2" width="20" height="20" rx="2.18"/><line x1="7" y1="2" x2="7" y2="22"/><line x1="17" y1="2" x2="17" y2="22"/><line x1="2" y1="12" x2="22" y2="12"/><line x1="2" y1="7" x2="7" y2="7"/><line x1="17" y1="7" x2="22" y2="7"/><line x1="17" y1="17" x2="22" y2="17"/><line x1="2" y1="17" x2="7" y2="17"/></>,
    ticket: <><path d="M2 9a3 3 0 010-6h20a3 3 0 010 6"/><path d="M2 15a3 3 0 000 6h20a3 3 0 000-6"/><path d="M2 9h20M2 15h20"/></>,
    plus: <><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></>,
    arrow: <><line x1="5" y1="12" x2="19" y2="12"/><polyline points="12 5 19 12 12 19"/></>,
    users: <><path d="M17 21v-2a4 4 0 00-4-4H5a4 4 0 00-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 00-3-3.87"/><path d="M16 3.13a4 4 0 010 7.75"/></>,
    x: <><line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/></>,
  };
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={color} strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
      {icons[name]}
    </svg>
  );
};

/* ── Status Bar ─────────────────────────────────────────────────────────────── */
const StatusBar = ({ light = false }) => (
  <div className={`status-bar ${light ? "status-light" : "status-dark"}`}>
    <span>9:41</span>
    <div style={{ display:"flex", gap:5, alignItems:"center" }}>
      <span>●●●●</span>
      <span>WiFi</span>
      <span>100%</span>
    </div>
  </div>
);

/* ── Bottom Nav ─────────────────────────────────────────────────────────────── */
const BottomNav = ({ active, onNavigate }) => {
  const tabs = [
    { id:"home",    icon:"home2",  label:"Home"    },
    { id:"refer",   icon:"share",  label:"Refer"   },
    { id:"wallet",  icon:"wallet", label:"Wallet"  },
    { id:"profile", icon:"user",   label:"Profile" },
  ];
  return (
    <div className="bottom-nav">
      {tabs.map(t => (
        <div key={t.id} className={`nav-tab ${active===t.id?"active":""}`} onClick={() => onNavigate(t.id)}>
          <Icon name={t.icon} size={22}/>
          <span>{t.label}</span>
        </div>
      ))}
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   01 — SPLASH SCREEN
══════════════════════════════════════════════════════════════════════════════ */
const SplashScreen = ({ onNext }) => {
  const [loaded, setLoaded] = useState(false);
  useEffect(() => { const t = setTimeout(() => setLoaded(true), 400); return () => clearTimeout(t); }, []);
  return (
    <div className="screen" style={{ background:"#0A0806", display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center", overflow:"hidden" }}>
      {/* Cinematic background */}
      <div style={{ position:"absolute", inset:0, background:"radial-gradient(ellipse at 50% 35%, #2A1E08 0%, #0A0806 70%)" }}/>
      {/* Grain texture */}
      <div style={{ position:"absolute", inset:0, opacity:.4,
        backgroundImage:`url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noise'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noise)' opacity='0.08'/%3E%3C/svg%3E")` }}/>
      {/* Decorative arcs */}
      <svg style={{ position:"absolute", inset:0, width:"100%", height:"100%", opacity:.15 }} viewBox="0 0 390 844">
        <circle cx="195" cy="280" r="180" fill="none" stroke="#C9973A" strokeWidth="1"/>
        <circle cx="195" cy="280" r="220" fill="none" stroke="#C9973A" strokeWidth=".5"/>
        <circle cx="195" cy="280" r="260" fill="none" stroke="#C9973A" strokeWidth=".3"/>
      </svg>

      {/* Logo area */}
      <div style={{ position:"relative", textAlign:"center", animation: loaded ? "slideUp .8s ease both" : "none", animationDelay:".2s" }}>
        {/* Film strip icon */}
        <div style={{ marginBottom:32, display:"flex", justifyContent:"center" }}>
          <div style={{ width:72, height:72, borderRadius:18, background:"linear-gradient(135deg,#8A5C1A,#C9973A)", display:"flex", alignItems:"center", justifyContent:"center", animation:"glow 3s ease-in-out infinite", boxShadow:"0 0 40px rgba(201,151,58,.4)" }}>
            <Icon name="film" size={34} color="#FBF0D8"/>
          </div>
        </div>

        <div style={{ fontFamily:"var(--font-display)", fontSize:52, fontWeight:300, letterSpacing:8, color:"#F5D78E", lineHeight:1, marginBottom:6 }}>RS³</div>
        <div style={{ fontFamily:"var(--font-display)", fontSize:22, fontWeight:300, letterSpacing:12, color:"rgba(245,215,142,.6)", marginBottom:14 }}>FILMS</div>
        <div style={{ width:80, height:1, background:"linear-gradient(90deg,transparent,#C9973A,transparent)", margin:"0 auto 16px" }}/>
        <div style={{ fontFamily:"var(--font-body)", fontSize:11, fontWeight:400, letterSpacing:3, color:"rgba(201,151,58,.6)", textTransform:"uppercase" }}>
          An Emotional Journey of Love
        </div>
      </div>

      {/* Loading dots */}
      <div style={{ position:"absolute", bottom:80, display:"flex", gap:8 }}>
        {[0,1,2].map(i => (
          <div key={i} style={{ width:6, height:6, borderRadius:"50%", background:"#C9973A", opacity: loaded ? 1 : 0.3, animation:`pulse 1.2s ease ${i*.2}s infinite` }}/>
        ))}
      </div>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   02 — ONBOARDING
══════════════════════════════════════════════════════════════════════════════ */
const OnboardingScreen = ({ onNext }) => {
  const [slide, setSlide] = useState(0);
  const slides = [
    {
      bg:"linear-gradient(160deg,#0E0A06,#1E1408)",
      accent:"#C9973A",
      icon:"film",
      title:"Welcome to\nRS³ Films",
      sub:"Join the exclusive fan community of an emotional cinematic journey. Be part of something real.",
      tag:"Fan Community",
    },
    {
      bg:"linear-gradient(160deg,#0A0E08,#141E10)",
      accent:"#4A9A6A",
      icon:"share",
      title:"Refer Friends.\nEarn Coins.",
      sub:"Share your unique code. When friends join, you earn coins across up to 3 levels of your network.",
      tag:"Referral Rewards",
    },
    {
      bg:"linear-gradient(160deg,#0A080E,#14101E)",
      accent:"#6A4A9A",
      icon:"ticket",
      title:"Unlock Exclusive\nOffers",
      sub:"Redeem coins for movie tickets, premiere passes, and exclusive fan merchandise. Real rewards await.",
      tag:"Exclusive Access",
    },
  ];
  const s = slides[slide];
  return (
    <div className="screen" style={{ background:s.bg, display:"flex", flexDirection:"column", transition:"background .5s ease" }}>
      <StatusBar light />

      {/* Skip */}
      <div style={{ display:"flex", justifyContent:"flex-end", padding:"4px 24px" }}>
        <button onClick={onNext} style={{ background:"transparent", border:"none", color:"rgba(255,255,255,.4)", fontFamily:"var(--font-body)", fontSize:12, fontWeight:500, letterSpacing:.5, cursor:"pointer" }}>Skip</button>
      </div>

      {/* Illustration area */}
      <div style={{ flex:1, display:"flex", alignItems:"center", justifyContent:"center", padding:"0 40px" }}>
        <div style={{ textAlign:"center", animation:"float 4s ease-in-out infinite" }}>
          <div style={{ width:160, height:160, borderRadius:40, background:`${s.accent}18`, border:`1px solid ${s.accent}30`, display:"flex", alignItems:"center", justifyContent:"center", margin:"0 auto 0" }}>
            <div style={{ width:120, height:120, borderRadius:30, background:`${s.accent}22`, display:"flex", alignItems:"center", justifyContent:"center" }}>
              <Icon name={s.icon} size={52} color={s.accent}/>
            </div>
          </div>
          {/* Decorative circles */}
          <div style={{ position:"absolute", width:300, height:300, borderRadius:"50%", border:`1px solid ${s.accent}12`, left:"50%", top:"42%", transform:"translate(-50%,-50%)", pointerEvents:"none" }}/>
          <div style={{ position:"absolute", width:240, height:240, borderRadius:"50%", border:`1px solid ${s.accent}18`, left:"50%", top:"42%", transform:"translate(-50%,-50%)", pointerEvents:"none" }}/>
        </div>
      </div>

      {/* Text */}
      <div style={{ padding:"0 32px 40px", animation:"slideUp .5s ease both" }}>
        <div style={{ marginBottom:12 }}>
          <span style={{ background:`${s.accent}20`, color:s.accent, border:`1px solid ${s.accent}40`, borderRadius:20, padding:"3px 12px", fontSize:10, fontWeight:700, letterSpacing:1.5, textTransform:"uppercase" }}>{s.tag}</span>
        </div>
        <div style={{ fontFamily:"var(--font-display)", fontSize:36, fontWeight:300, color:"#FBF0D8", lineHeight:1.2, marginBottom:14, whiteSpace:"pre-line" }}>{s.title}</div>
        <div style={{ fontSize:14, color:"rgba(255,255,255,.5)", lineHeight:1.7, marginBottom:32 }}>{s.sub}</div>

        {/* Dots */}
        <div style={{ display:"flex", gap:8, marginBottom:28 }}>
          {slides.map((_,i) => (
            <div key={i} onClick={() => setSlide(i)} style={{ height:4, width: slide===i?24:8, borderRadius:2, background: slide===i ? s.accent : "rgba(255,255,255,.2)", transition:"all .3s", cursor:"pointer" }}/>
          ))}
        </div>

        {slide < slides.length - 1 ? (
          <button className="btn-gold" style={{ background:`linear-gradient(135deg,${s.accent}80,${s.accent})` }} onClick={() => setSlide(prev => prev+1)}>Continue</button>
        ) : (
          <button className="btn-gold" onClick={onNext}>Get Started →</button>
        )}
      </div>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   03 — REGISTRATION
══════════════════════════════════════════════════════════════════════════════ */
const RegisterScreen = ({ onNext }) => {
  const [step, setStep] = useState(0); // 0=phone, 1=otp, 2=form, 3=selfie
  const [form, setForm] = useState({ phone:"", otp:"", name:"", email:"", dob:"", gender:"", pincode:"", referral:"RS3_DEMO1" });
  const sf = (k,v) => setForm(f => ({...f,[k]:v}));
  const [otpVals, setOtpVals] = useState(["","","","","",""]);

  if (step === 0) return (
    <div className="screen scroll-area" style={{ background:"var(--surface)" }}>
      <StatusBar/>
      <div style={{ padding:"20px 28px 40px" }}>
        <div style={{ marginBottom:32 }}>
          <div style={{ fontFamily:"var(--font-display)", fontSize:11, fontWeight:400, letterSpacing:3, color:"var(--gold)", textTransform:"uppercase", marginBottom:8 }}>Step 1 of 4</div>
          <div style={{ fontFamily:"var(--font-display)", fontSize:30, color:"var(--ink2)", lineHeight:1.2, marginBottom:6 }}>Verify Your<br/>Phone</div>
          <div style={{ fontSize:13, color:"var(--muted)" }}>We'll send a 6-digit OTP to confirm your number.</div>
        </div>

        <div className="inp-wrap">
          <label className="inp-label">Mobile Number</label>
          <div style={{ display:"flex", gap:10 }}>
            <div style={{ width:64, padding:"13px 0", border:"1.5px solid var(--faint)", borderRadius:"var(--r3)", background:"var(--white)", textAlign:"center", fontSize:14, color:"var(--mid)", fontWeight:600 }}>+91</div>
            <input className="inp" style={{ flex:1 }} placeholder="98765 43210" value={form.phone} onChange={e=>sf("phone",e.target.value)}/>
          </div>
        </div>

        <div style={{ background:"var(--goldpale)", border:"1px solid #E8D8A8", borderRadius:10, padding:"12px 14px", marginBottom:24, display:"flex", gap:10 }}>
          <Icon name="info" size={16} color="var(--gold)"/>
          <div style={{ fontSize:12, color:"var(--mid)", lineHeight:1.6 }}>A valid referral code is mandatory to join RS³ Films community.</div>
        </div>

        <div className="inp-wrap">
          <label className="inp-label">Referral Code</label>
          <input className="inp" value={form.referral} onChange={e=>sf("referral",e.target.value)} placeholder="RS3_XXXXXX" style={{ fontFamily:"monospace", letterSpacing:2, fontWeight:600, color:"var(--gold)" }}/>
        </div>

        <button className="btn-gold" onClick={() => setStep(1)} style={{ marginTop:8 }}>Send OTP</button>
        <button className="btn-ghost" onClick={onNext} style={{ marginTop:10 }}>Sign In Instead</button>
      </div>
    </div>
  );

  if (step === 1) return (
    <div className="screen" style={{ background:"var(--surface)" }}>
      <StatusBar/>
      <div style={{ padding:"20px 28px" }}>
        <button onClick={() => setStep(0)} style={{ background:"none", border:"none", cursor:"pointer", marginBottom:24, display:"flex", alignItems:"center", gap:6, color:"var(--muted)" }}>
          <Icon name="chevronL" size={18}/> <span style={{ fontSize:13 }}>Back</span>
        </button>

        <div style={{ marginBottom:36 }}>
          <div style={{ fontFamily:"var(--font-display)", fontSize:11, letterSpacing:3, color:"var(--gold)", textTransform:"uppercase", marginBottom:8 }}>Step 1 of 4</div>
          <div style={{ fontFamily:"var(--font-display)", fontSize:30, color:"var(--ink2)", marginBottom:6 }}>Enter OTP</div>
          <div style={{ fontSize:13, color:"var(--muted)" }}>Sent to +91 {form.phone || "98765 43210"}</div>
        </div>

        {/* OTP boxes */}
        <div style={{ display:"flex", gap:10, marginBottom:28 }}>
          {otpVals.map((v,i) => (
            <div key={i} style={{ flex:1, height:54, border:`2px solid ${v ? "var(--gold)" : "var(--faint)"}`, borderRadius:10, background:"var(--white)", display:"flex", alignItems:"center", justifyContent:"center", fontSize:22, fontWeight:700, color:"var(--ink2)", transition:"border-color .2s" }}>
              {v || <span style={{ color:"var(--faint)", fontSize:18 }}>—</span>}
            </div>
          ))}
        </div>

        {/* Simulated input */}
        <div style={{ display:"flex", gap:8, flexWrap:"wrap", marginBottom:28 }}>
          {[1,2,3,4,5,6,7,8,9,"✗",0,"→"].map(n => (
            <button key={n} onClick={() => {
              if (n === "✗") setOtpVals(v => { const a=[...v]; const idx=a.findLastIndex(x=>x!==""); if(idx>=0) a[idx]=""; return a; });
              else if (n === "→") { if (otpVals.filter(Boolean).length===6) setStep(2); }
              else setOtpVals(v => { const a=[...v]; const idx=a.findIndex(x=>x===""); if(idx>=0) a[idx]=n.toString(); return a; });
            }} style={{ width:"calc(33.33% - 6px)", height:52, borderRadius:12, border:"1.5px solid var(--faint)", background: n==="→"?"var(--gold)":"var(--white)", color: n==="→"?"var(--ink)":"var(--ink2)", fontSize:18, fontWeight:600, cursor:"pointer", fontFamily:"var(--font-body)" }}>
              {n === "→" ? "✓" : n}
            </button>
          ))}
        </div>

        <div style={{ textAlign:"center", fontSize:13, color:"var(--muted)" }}>Didn't receive? <span style={{ color:"var(--gold)", fontWeight:600 }}>Resend in 28s</span></div>
      </div>
    </div>
  );

  if (step === 2) return (
    <div className="screen scroll-area" style={{ background:"var(--surface)" }}>
      <StatusBar/>
      <div style={{ padding:"16px 28px 100px" }}>
        <div style={{ marginBottom:24 }}>
          <div style={{ fontFamily:"var(--font-display)", fontSize:11, letterSpacing:3, color:"var(--gold)", textTransform:"uppercase", marginBottom:8 }}>Step 2 of 4</div>
          <div style={{ fontFamily:"var(--font-display)", fontSize:28, color:"var(--ink2)", marginBottom:4 }}>Your Details</div>
          <div style={{ fontSize:12, color:"var(--muted)" }}>All fields are mandatory</div>
        </div>

        {[["Full Name","name","text","Arjun Kumar"],["Email Address","email","email","arjun@mail.com"]].map(([l,k,t,ph]) => (
          <div className="inp-wrap" key={k}>
            <label className="inp-label">{l}</label>
            <input className="inp" type={t} placeholder={ph} value={form[k]} onChange={e=>sf(k,e.target.value)}/>
          </div>
        ))}

        <div className="inp-row">
          <div className="inp-wrap" style={{ flex:1 }}>
            <label className="inp-label">Date of Birth</label>
            <input className="inp" type="date" value={form.dob} onChange={e=>sf("dob",e.target.value)}/>
          </div>
          <div className="inp-wrap" style={{ flex:1 }}>
            <label className="inp-label">Gender</label>
            <select className="inp" value={form.gender} onChange={e=>sf("gender",e.target.value)} style={{ cursor:"pointer" }}>
              <option value="">Select</option>
              <option>Male</option><option>Female</option><option>Non-binary</option><option>Prefer not to say</option>
            </select>
          </div>
        </div>

        <div className="inp-wrap">
          <label className="inp-label">Pincode</label>
          <input className="inp" placeholder="600001" value={form.pincode} onChange={e=>sf("pincode",e.target.value)}/>
          {form.pincode.length===6 && <div style={{ fontSize:11, color:"var(--green)", marginTop:4 }}>✓ Chennai, Tamil Nadu</div>}
        </div>

        <div className="inp-wrap">
          <label className="inp-label">Password</label>
          <input className="inp" type="password" placeholder="Min 8 characters"/>
          <div style={{ marginTop:8, display:"flex", gap:4 }}>
            {[1,2,3,4,5].map(i=><div key={i} style={{ flex:1, height:3, borderRadius:2, background: i<=3?"var(--gold)":"var(--faint)" }}/>)}
          </div>
          <div style={{ fontSize:11, color:"var(--gold)", marginTop:3 }}>Medium strength</div>
        </div>

        <div style={{ background:"var(--goldpale)", border:"1px solid #E8D8A8", borderRadius:10, padding:"12px 14px", marginBottom:20 }}>
          <div style={{ fontSize:12, fontWeight:600, color:"var(--mid)", marginBottom:2 }}>Referral Code</div>
          <div style={{ fontSize:14, fontFamily:"monospace", color:"var(--gold)", fontWeight:700, letterSpacing:2 }}>{form.referral}</div>
          <div style={{ fontSize:11, color:"var(--muted)", marginTop:2 }}>Auto-filled from your invitation link ✓</div>
        </div>

        <button className="btn-gold" onClick={() => setStep(3)}>Continue to Selfie →</button>
      </div>
    </div>
  );

  return (
    <div className="screen" style={{ background:"var(--surface)" }}>
      <StatusBar/>
      <div style={{ padding:"20px 28px" }}>
        <div style={{ marginBottom:28 }}>
          <div style={{ fontFamily:"var(--font-display)", fontSize:11, letterSpacing:3, color:"var(--gold)", textTransform:"uppercase", marginBottom:8 }}>Step 3 of 4</div>
          <div style={{ fontFamily:"var(--font-display)", fontSize:28, color:"var(--ink2)", marginBottom:4 }}>Your Selfie</div>
          <div style={{ fontSize:12, color:"var(--muted)" }}>A quick verification photo for your fan profile</div>
        </div>

        <div style={{ width:"100%", aspectRatio:"3/4", borderRadius:20, background:"linear-gradient(135deg,#F0EAE0,#E8E0D0)", display:"flex", flexDirection:"column", alignItems:"center", justifyContent:"center", border:"2px dashed var(--faint)", marginBottom:24, cursor:"pointer", position:"relative", overflow:"hidden" }}>
          <div style={{ textAlign:"center" }}>
            <div style={{ width:72, height:72, borderRadius:"50%", background:"var(--faint)", display:"flex", alignItems:"center", justifyContent:"center", margin:"0 auto 14px" }}>
              <Icon name="camera" size={32} color="var(--muted)"/>
            </div>
            <div style={{ fontSize:14, fontWeight:600, color:"var(--mid)", marginBottom:4 }}>Tap to take selfie</div>
            <div style={{ fontSize:12, color:"var(--muted)" }}>Face must be clearly visible</div>
          </div>
          {/* Corner guides */}
          {[["0 auto auto 20px","0 0 1 1"],["0 20px auto auto","0 1 1 0"],["auto auto 20px 20px","1 0 0 1"],["auto 20px 20px auto","1 1 0 0"]].map(([pos,r],i)=>(
            <div key={i} style={{ position:"absolute", [pos.includes("auto auto auto")?"top":pos.includes("20px auto auto")?"right":pos.includes("auto auto 20px")?"bottom":"bottom"]:"20px", ...(i===0?{top:20,left:20}:i===1?{top:20,right:20}:i===2?{bottom:20,left:20}:{bottom:20,right:20}), width:28, height:28, borderTop: [0,1].includes(i)?"2.5px solid var(--gold)":"none", borderBottom: [2,3].includes(i)?"2.5px solid var(--gold)":"none", borderLeft: [0,2].includes(i)?"2.5px solid var(--gold)":"none", borderRight: [1,3].includes(i)?"2.5px solid var(--gold)":"none" }}/>
          ))}
        </div>

        <button className="btn-gold" onClick={onNext}>Complete Registration →</button>
        <div style={{ textAlign:"center", marginTop:12, fontSize:11, color:"var(--muted)" }}>
          By registering you agree to our <span style={{ color:"var(--gold)" }}>Terms & Conditions</span>
        </div>
      </div>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   04 — LOGIN
══════════════════════════════════════════════════════════════════════════════ */
const LoginScreen = ({ onNext }) => (
  <div className="screen scroll-area" style={{ background:"var(--surface)" }}>
    <StatusBar/>
    {/* Top branding */}
    <div style={{ background:"linear-gradient(160deg,#0E0A06 0%,#1E1408 100%)", padding:"32px 28px 40px", borderBottomLeftRadius:32, borderBottomRightRadius:32 }}>
      <div style={{ display:"flex", alignItems:"center", gap:12, marginBottom:24 }}>
        <div style={{ width:40, height:40, borderRadius:10, background:"linear-gradient(135deg,#8A5C1A,#C9973A)", display:"flex", alignItems:"center", justifyContent:"center" }}>
          <Icon name="film" size={20} color="#FBF0D8"/>
        </div>
        <div>
          <div style={{ fontFamily:"var(--font-display)", fontSize:16, color:"#F5D78E", letterSpacing:4 }}>RS³ FILMS</div>
          <div style={{ fontSize:9, color:"rgba(245,215,142,.4)", letterSpacing:2 }}>FAN COMMUNITY</div>
        </div>
      </div>
      <div style={{ fontFamily:"var(--font-display)", fontSize:32, color:"#FBF0D8", lineHeight:1.2, marginBottom:6 }}>Welcome<br/>Back</div>
      <div style={{ fontSize:13, color:"rgba(255,255,255,.4)" }}>Sign in to your fan account</div>
    </div>

    <div style={{ padding:"32px 28px 40px" }}>
      <div className="inp-wrap">
        <label className="inp-label">Email Address</label>
        <input className="inp" type="email" placeholder="arjun@mail.com"/>
      </div>
      <div className="inp-wrap">
        <label className="inp-label">Password</label>
        <div style={{ position:"relative" }}>
          <input className="inp" type="password" placeholder="••••••••" style={{ paddingRight:48 }}/>
          <div style={{ position:"absolute", right:14, top:"50%", transform:"translateY(-50%)", cursor:"pointer" }}>
            <Icon name="eye" size={18} color="var(--muted)"/>
          </div>
        </div>
      </div>

      <div style={{ textAlign:"right", marginBottom:24 }}>
        <span style={{ fontSize:12, color:"var(--gold)", fontWeight:600, cursor:"pointer" }}>Forgot Password?</span>
      </div>

      <button className="btn-gold" onClick={onNext} style={{ marginBottom:12 }}>Sign In</button>
      <button className="btn-ghost" onClick={onNext}>Create Account</button>

      <div style={{ textAlign:"center", marginTop:28 }}>
        <div style={{ fontSize:11, color:"var(--muted)", marginBottom:4 }}>Powered by secure OTP + JWT</div>
        <div style={{ display:"flex", justifyContent:"center", gap:6 }}>
          <Icon name="shield" size={14} color="var(--muted)"/>
          <span style={{ fontSize:11, color:"var(--muted)" }}>End-to-end encrypted</span>
        </div>
      </div>
    </div>
  </div>
);

/* ══════════════════════════════════════════════════════════════════════════════
   05 — HOME FEED
══════════════════════════════════════════════════════════════════════════════ */
const HomeScreen = ({ onNavigate }) => {
  const posts = [
    { layout:"hero", title:"Premiere Night", sub:"PVR Cinemas · March 15, 2025", img:"https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&q=80", tag:"LIVE EVENT" },
    { layout:"card", title:"1000 Fans Strong", sub:"Our community just hit a milestone. Thank you for the love!", img:"https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&q=80", tag:"MILESTONE" },
    { layout:"update", title:"App v1.2 is Live", sub:"New referral tree, coin history, and more. Update now!", img:null, tag:"UPDATE" },
    { layout:"card", title:"Behind the Scenes", sub:"Director's cut — filming the emotional climax.", img:"https://images.unsplash.com/photo-1440404653325-ab127d49abc1?w=600&q=80", tag:"EXCLUSIVE" },
  ];

  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <div style={{ background:"var(--white)", padding:"0 0 0 0", flexShrink:0 }}>
        <StatusBar/>
        <div style={{ display:"flex", alignItems:"center", justifyContent:"space-between", padding:"4px 24px 14px" }}>
          <div>
            <div style={{ fontSize:11, color:"var(--muted)", letterSpacing:1, marginBottom:1 }}>GOOD EVENING</div>
            <div style={{ fontFamily:"var(--font-display)", fontSize:22, color:"var(--ink2)" }}>Arjun Kumar</div>
          </div>
          <div style={{ display:"flex", gap:10, alignItems:"center" }}>
            <div className="coin-chip"><Icon name="coin" size={13} color="#8A6020"/> 1,240</div>
            <div className="btn-icon"><Icon name="bell" size={18} color="var(--ink3)"/></div>
          </div>
        </div>
      </div>

      <div className="scroll-area" style={{ flex:1, paddingBottom:80 }}>
        {/* Hero post */}
        <div style={{ margin:"0 16px 16px", borderRadius:"var(--r)", overflow:"hidden", position:"relative", cursor:"pointer" }}>
          <img src={posts[0].img} alt="" style={{ width:"100%", height:200, objectFit:"cover" }}/>
          <div style={{ position:"absolute", inset:0, background:"linear-gradient(to top, rgba(14,12,8,.85) 0%, transparent 50%)" }}/>
          <div style={{ position:"absolute", bottom:16, left:16, right:16 }}>
            <span style={{ background:"var(--gold)", color:"var(--ink)", padding:"3px 10px", borderRadius:4, fontSize:9, fontWeight:700, letterSpacing:1.5, marginBottom:8, display:"inline-block" }}>{posts[0].tag}</span>
            <div style={{ fontFamily:"var(--font-display)", fontSize:22, color:"#FBF0D8", marginBottom:2 }}>{posts[0].title}</div>
            <div style={{ fontSize:12, color:"rgba(255,255,255,.6)" }}>{posts[0].sub}</div>
          </div>
        </div>

        {/* Card posts */}
        {posts.slice(1).map((p, i) => (
          <div key={i} style={{ margin:"0 16px 12px", background:"var(--white)", borderRadius:16, overflow:"hidden", border:"1px solid var(--faint)", cursor:"pointer" }}>
            {p.img && <img src={p.img} alt="" style={{ width:"100%", height:140, objectFit:"cover" }}/>}
            <div style={{ padding:"14px 16px" }}>
              <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start", marginBottom:6 }}>
                <div style={{ fontFamily:"var(--font-display)", fontSize:18, color:"var(--ink2)", flex:1, paddingRight:10 }}>{p.title}</div>
                <span className={`tag-${i===1?"gold":i===2?"green":"ink"}`}>{p.tag}</span>
              </div>
              <div style={{ fontSize:12, color:"var(--muted)", lineHeight:1.6 }}>{p.sub}</div>
            </div>
          </div>
        ))}
      </div>

      <BottomNav active="home" onNavigate={onNavigate}/>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   06 — REFERRAL SCREEN
══════════════════════════════════════════════════════════════════════════════ */
const ReferralScreen = ({ onNavigate, onDetail }) => {
  const [copied, setCopied] = useState(false);
  const myCode = "RS3_ARJK42";

  const offers = [
    { title:"Free Movie Ticket", desc:"Refer 5 friends", progress:4, target:5, type:"movie_ticket", coins:0 },
    { title:"Premiere Pass", desc:"Refer 10 friends", progress:4, target:10, type:"event_pass", coins:0 },
  ];

  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <div style={{ background:"var(--white)", flexShrink:0 }}>
        <StatusBar/>
        <div style={{ padding:"4px 24px 14px" }}>
          <div style={{ fontFamily:"var(--font-display)", fontSize:22, color:"var(--ink2)" }}>Refer & Earn</div>
          <div style={{ fontSize:12, color:"var(--muted)" }}>Share your code. Earn coins at every level.</div>
        </div>
      </div>

      <div className="scroll-area" style={{ flex:1, padding:"16px 16px 90px" }}>
        {/* Code card */}
        <div className="card-gold" style={{ padding:"24px", marginBottom:16 }}>
          <div style={{ position:"absolute", inset:0, background:"radial-gradient(ellipse at 80% 20%, rgba(201,151,58,.15) 0%, transparent 60%)" }}/>
          <div style={{ position:"relative" }}>
            <div style={{ fontSize:11, color:"rgba(245,215,142,.5)", letterSpacing:2, marginBottom:6, textTransform:"uppercase" }}>Your Referral Code</div>
            <div style={{ fontFamily:"monospace", fontSize:28, fontWeight:700, color:"#F5D78E", letterSpacing:3, marginBottom:16 }}>{myCode}</div>

            <div style={{ display:"flex", gap:10 }}>
              <button onClick={() => { setCopied(true); setTimeout(()=>setCopied(false),2000); }} style={{ flex:1, padding:"11px", background:"rgba(255,255,255,.1)", border:"1px solid rgba(255,255,255,.15)", borderRadius:10, color:"#E8C96A", fontSize:12, fontWeight:600, cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center", gap:6 }}>
                <Icon name={copied?"check":"copy"} size={15} color="#E8C96A"/> {copied ? "Copied!" : "Copy Code"}
              </button>
              <button style={{ flex:1, padding:"11px", background:"linear-gradient(135deg,#8A5C1A,#C9973A)", border:"none", borderRadius:10, color:"var(--ink)", fontSize:12, fontWeight:700, cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center", gap:6 }}>
                <Icon name="share" size={15} color="var(--ink)"/> Share Link
              </button>
            </div>
          </div>
        </div>

        {/* Earnings chain */}
        <div style={{ background:"var(--white)", borderRadius:16, padding:"18px", marginBottom:16, border:"1px solid var(--faint)" }}>
          <div style={{ fontSize:13, fontWeight:700, color:"var(--ink2)", marginBottom:14 }}>How You Earn</div>
          {[["Direct referral joins","L1","+100 coins",true],["Their referral joins","L2","+40 coins",true],["Chain referral joins","L3","+15 coins",true]].map(([label,lvl,coins,active],i) => (
            <div key={i} style={{ display:"flex", alignItems:"center", gap:12, marginBottom: i<2?12:0 }}>
              <div style={{ width:36, height:36, borderRadius:10, background: active?"linear-gradient(135deg,#8A5C1A,#C9973A)":"var(--faint)", display:"flex", alignItems:"center", justifyContent:"center", fontFamily:"var(--font-body)", fontSize:10, fontWeight:700, color: active?"var(--ink)":"var(--muted)", flexShrink:0 }}>{lvl}</div>
              {i < 2 && <div style={{ position:"absolute", width:2, height:12, background:"var(--faint)", marginLeft:17, marginTop:36 }}/>}
              <div style={{ flex:1, fontSize:13, color:"var(--ink3)" }}>{label}</div>
              <div style={{ fontSize:14, fontWeight:700, color:"var(--gold)" }}>{coins}</div>
            </div>
          ))}
        </div>

        {/* Stats */}
        <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr 1fr", gap:10, marginBottom:16 }}>
          {[["24","Total Refs"],["8","This Month"],["₹1,240","Earned"]].map(([v,l]) => (
            <div key={l} style={{ background:"var(--white)", borderRadius:12, padding:"14px 10px", textAlign:"center", border:"1px solid var(--faint)" }}>
              <div style={{ fontFamily:"var(--font-display)", fontSize:22, color:"var(--gold)", marginBottom:2 }}>{v}</div>
              <div style={{ fontSize:10, color:"var(--muted)", fontWeight:600, letterSpacing:.5 }}>{l}</div>
            </div>
          ))}
        </div>

        {/* Offers */}
        {offers.length > 0 && <>
          <div style={{ fontSize:13, fontWeight:700, color:"var(--ink2)", marginBottom:12 }}>Milestone Offers</div>
          {offers.map((o,i) => (
            <div key={i} onClick={onDetail} style={{ background:"var(--white)", borderRadius:16, padding:"16px", marginBottom:10, border:"1px solid var(--faint)", cursor:"pointer" }}>
              <div style={{ display:"flex", justifyContent:"space-between", alignItems:"flex-start", marginBottom:10 }}>
                <div>
                  <div style={{ fontSize:14, fontWeight:700, color:"var(--ink2)", marginBottom:2 }}>{o.title}</div>
                  <div style={{ fontSize:12, color:"var(--muted)" }}>{o.desc} to unlock</div>
                </div>
                <span style={{ fontSize:16 }}>{o.type==="movie_ticket"?"🎬":"🎪"}</span>
              </div>
              <div className="prog-track"><div className="prog-fill" style={{ width:`${(o.progress/o.target)*100}%` }}/></div>
              <div style={{ display:"flex", justifyContent:"space-between", marginTop:6 }}>
                <div style={{ fontSize:11, color:"var(--muted)" }}>{o.progress}/{o.target} referrals</div>
                <div style={{ fontSize:11, color:"var(--gold)", fontWeight:600 }}>1 more to go!</div>
              </div>
            </div>
          ))}
        </>}

        <div style={{ textAlign:"center", marginTop:8 }}>
          <button onClick={() => onNavigate("tree")} style={{ background:"none", border:"none", color:"var(--gold)", fontSize:13, fontWeight:600, cursor:"pointer", display:"flex", alignItems:"center", gap:6, margin:"0 auto" }}>
            <Icon name="users" size={16} color="var(--gold)"/> View My Referral Tree
          </button>
        </div>
      </div>

      <BottomNav active="refer" onNavigate={onNavigate}/>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   07 — WALLET
══════════════════════════════════════════════════════════════════════════════ */
const WalletScreen = ({ onNavigate }) => {
  const [tab, setTab] = useState("history");
  const txns = [
    { type:"earn", label:"Direct Referral — Priya joined", coins:100, date:"Today, 2:34 PM", icon:"users" },
    { type:"earn", label:"Direct Referral — Rahul joined", coins:100, date:"Yesterday", icon:"users" },
    { type:"spend", label:"Free Movie Ticket redeemed", coins:-200, date:"Mar 1", icon:"ticket" },
    { type:"earn", label:"Profile Completion Bonus", coins:50, date:"Feb 28", icon:"star" },
    { type:"earn", label:"L2 Referral — Kiran joined", coins:40, date:"Feb 27", icon:"users" },
    { type:"earn", label:"L3 Referral — Sita joined", coins:15, date:"Feb 26", icon:"users" },
  ];
  const walletOffers = [
    { title:"10% Off Merchandise", cost:200, type:"coupon_code" },
  ];

  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <div style={{ background:"var(--white)", flexShrink:0 }}>
        <StatusBar/>
        <div style={{ padding:"4px 24px 14px" }}>
          <div style={{ fontFamily:"var(--font-display)", fontSize:22, color:"var(--ink2)" }}>Wallet</div>
        </div>
      </div>

      {/* Balance card */}
      <div style={{ margin:"0 16px 16px", flexShrink:0 }}>
        <div className="card-gold" style={{ padding:"24px", overflow:"hidden" }}>
          <div style={{ position:"absolute", inset:0, background:"radial-gradient(ellipse at 20% 80%, rgba(201,151,58,.2) 0%, transparent 60%)" }}/>
          <div style={{ position:"relative" }}>
            <div style={{ fontSize:10, color:"rgba(245,215,142,.5)", letterSpacing:2, textTransform:"uppercase", marginBottom:6 }}>Coin Balance</div>
            <div style={{ display:"flex", alignItems:"baseline", gap:8, marginBottom:4 }}>
              <div style={{ fontFamily:"var(--font-display)", fontSize:48, fontWeight:300, color:"#F5D78E", lineHeight:1 }}>1,240</div>
              <div style={{ fontSize:13, color:"rgba(245,215,142,.5)", fontWeight:300 }}>coins</div>
            </div>
            <div style={{ fontSize:12, color:"rgba(245,215,142,.4)", marginBottom:20 }}>= 12 Movie Vouchers · Min 200 to redeem</div>

            <div style={{ display:"flex", gap:20 }}>
              {[["2,380","Earned All Time"],["1,140","Spent"]].map(([v,l])=>(
                <div key={l}>
                  <div style={{ fontFamily:"var(--font-display)", fontSize:18, color:"#C9973A", marginBottom:2 }}>{v}</div>
                  <div style={{ fontSize:10, color:"rgba(255,255,255,.3)", letterSpacing:.5 }}>{l}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div style={{ padding:"0 16px 14px", flexShrink:0 }}>
        <div className="tab-pills">
          {[["history","History"],["offers","Redeem"]].map(([id,label])=>(
            <div key={id} className={`tab-pill ${tab===id?"active":""}`} onClick={()=>setTab(id)}>{label}</div>
          ))}
        </div>
      </div>

      <div className="scroll-area" style={{ flex:1, padding:"0 16px 90px" }}>
        {tab === "history" ? (
          txns.map((t,i) => (
            <div key={i} style={{ display:"flex", alignItems:"center", gap:12, padding:"13px 0", borderBottom:"1px solid var(--faint)" }}>
              <div style={{ width:40, height:40, borderRadius:12, background: t.type==="earn"?"rgba(30,123,74,.1)":"rgba(192,57,43,.1)", display:"flex", alignItems:"center", justifyContent:"center", flexShrink:0 }}>
                <Icon name={t.icon} size={18} color={t.type==="earn"?"var(--green)":"var(--red)"}/>
              </div>
              <div style={{ flex:1 }}>
                <div style={{ fontSize:13, fontWeight:500, color:"var(--ink2)", marginBottom:2 }}>{t.label}</div>
                <div style={{ fontSize:11, color:"var(--muted)" }}>{t.date}</div>
              </div>
              <div style={{ fontSize:15, fontWeight:700, color: t.type==="earn"?"var(--green)":"var(--red)" }}>
                {t.type==="earn"?"+":""}{t.coins}
              </div>
            </div>
          ))
        ) : (
          walletOffers.map((o,i) => (
            <div key={i} style={{ background:"var(--white)", borderRadius:16, padding:"18px", border:"1px solid var(--faint)", marginBottom:12 }}>
              <div style={{ display:"flex", justifyContent:"space-between", alignItems:"center", marginBottom:10 }}>
                <div>
                  <div style={{ fontSize:14, fontWeight:700, color:"var(--ink2)", marginBottom:3 }}>{o.title}</div>
                  <div className="coin-chip" style={{ marginTop:4 }}><Icon name="coin" size={12} color="#8A6020"/> {o.cost} coins</div>
                </div>
                <span style={{ fontSize:24 }}>🏷️</span>
              </div>
              <button className="btn-gold" style={{ fontSize:13, padding:"11px" }}>Redeem Now</button>
            </div>
          ))
        )}
      </div>

      <BottomNav active="wallet" onNavigate={onNavigate}/>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   08 — PROFILE
══════════════════════════════════════════════════════════════════════════════ */
const ProfileScreen = ({ onNavigate }) => {
  const stats = [["24","Referrals"],["1,240","Coins"],["3","Offers"],["Ambassador","Status"]];
  const menu = [
    { icon:"edit",    label:"Edit Profile",       sub:"Update your details" },
    { icon:"users",   label:"Referral Tree",       sub:"See your full network", action:"tree" },
    { icon:"trophy",  label:"Achievements",        sub:"Your milestones" },
    { icon:"gift",    label:"My Rewards",          sub:"Claimed offers & codes", action:"rewards" },
    { icon:"info",    label:"About RS³ Films",     sub:"Our story" },
    { icon:"shield",  label:"Terms & Privacy",     sub:"Legal information" },
    { icon:"settings",label:"Settings",            sub:"Notifications, account" },
    { icon:"logout",  label:"Sign Out",            sub:"", danger:true },
  ];

  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <StatusBar/>
      <div className="scroll-area" style={{ flex:1, paddingBottom:90 }}>
        {/* Profile header */}
        <div style={{ background:"linear-gradient(160deg,#0E0A06,#1E1408)", padding:"20px 24px 28px", borderBottomLeftRadius:28, borderBottomRightRadius:28 }}>
          <div style={{ display:"flex", alignItems:"center", gap:16, marginBottom:20 }}>
            <div style={{ position:"relative" }}>
              <div style={{ width:72, height:72, borderRadius:"50%", background:"linear-gradient(135deg,#2E2010,#5E4A20)", display:"flex", alignItems:"center", justifyContent:"center", border:"2.5px solid var(--gold)" }}>
                <span style={{ fontFamily:"var(--font-display)", fontSize:28, color:"#F5D78E" }}>A</span>
              </div>
              <div style={{ position:"absolute", bottom:2, right:2, width:18, height:18, borderRadius:"50%", background:"var(--gold)", display:"flex", alignItems:"center", justifyContent:"center", border:"2px solid #0E0A06" }}>
                <Icon name="check" size={10} color="#0E0A06"/>
              </div>
            </div>
            <div style={{ flex:1 }}>
              <div style={{ fontFamily:"var(--font-display)", fontSize:20, color:"#FBF0D8", marginBottom:4 }}>Arjun Kumar</div>
              <div style={{ fontSize:11, color:"rgba(255,255,255,.4)", marginBottom:6 }}>arjun@mail.com · Chennai</div>
              <div style={{ background:"linear-gradient(135deg,#8A5C1A,#C9973A)", display:"inline-flex", alignItems:"center", gap:5, padding:"3px 10px", borderRadius:20 }}>
                <Icon name="star" size={11} color="#0E0A06"/>
                <span style={{ fontSize:10, fontWeight:700, color:"#0E0A06", letterSpacing:.8 }}>FAN AMBASSADOR</span>
              </div>
            </div>
          </div>

          {/* Referral code */}
          <div style={{ background:"rgba(255,255,255,.05)", border:"1px solid rgba(201,151,58,.2)", borderRadius:10, padding:"10px 14px", display:"flex", alignItems:"center", justifyContent:"space-between" }}>
            <div>
              <div style={{ fontSize:9, color:"rgba(245,215,142,.4)", letterSpacing:2, marginBottom:2 }}>YOUR CODE</div>
              <div style={{ fontFamily:"monospace", fontSize:14, color:"#F5D78E", letterSpacing:2, fontWeight:700 }}>RS3_ARJK42</div>
            </div>
            <button style={{ background:"rgba(201,151,58,.2)", border:"1px solid rgba(201,151,58,.3)", borderRadius:8, padding:"6px 12px", color:"#E8C96A", fontSize:11, fontWeight:600, cursor:"pointer" }}>
              Share
            </button>
          </div>
        </div>

        {/* Stats */}
        <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr 1fr 1fr", gap:1, margin:"16px 16px 0", background:"var(--faint)", borderRadius:14, overflow:"hidden" }}>
          {stats.map(([v,l]) => (
            <div key={l} style={{ background:"var(--white)", padding:"12px 6px", textAlign:"center" }}>
              <div style={{ fontFamily:"var(--font-display)", fontSize:l==="Status"?13:20, color:"var(--gold)", fontWeight:400, marginBottom:2, lineHeight:1.2 }}>{v}</div>
              <div style={{ fontSize:9, color:"var(--muted)", fontWeight:700, letterSpacing:.5 }}>{l}</div>
            </div>
          ))}
        </div>

        {/* Menu */}
        <div style={{ margin:"16px 16px 0" }}>
          {menu.map((item, i) => (
            <div key={i} onClick={() => item.action && onNavigate(item.action)} style={{ display:"flex", alignItems:"center", gap:14, padding:"15px 0", borderBottom: i < menu.length-1 ? "1px solid var(--faint)" : "none", cursor:"pointer" }}>
              <div style={{ width:38, height:38, borderRadius:10, background: item.danger ? "rgba(192,57,43,.08)" : "rgba(201,151,58,.08)", display:"flex", alignItems:"center", justifyContent:"center", flexShrink:0 }}>
                <Icon name={item.icon} size={18} color={item.danger ? "var(--red)" : "var(--gold)"}/>
              </div>
              <div style={{ flex:1 }}>
                <div style={{ fontSize:14, fontWeight:500, color: item.danger ? "var(--red)" : "var(--ink2)" }}>{item.label}</div>
                {item.sub && <div style={{ fontSize:11, color:"var(--muted)", marginTop:1 }}>{item.sub}</div>}
              </div>
              {!item.danger && <Icon name="chevron" size={16} color="var(--muted)"/>}
            </div>
          ))}
        </div>

        <div style={{ padding:"16px 24px", textAlign:"center" }}>
          <div style={{ fontSize:11, color:"var(--muted)" }}>RS³ Films · v1.2.0 · Member since Jan 2025</div>
        </div>
      </div>

      <BottomNav active="profile" onNavigate={onNavigate}/>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   09 — OFFER DETAIL
══════════════════════════════════════════════════════════════════════════════ */
const OfferDetailScreen = ({ onBack }) => {
  const [claimed, setClaimed] = useState(false);
  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <StatusBar/>
      {/* Header image */}
      <div style={{ position:"relative", flexShrink:0 }}>
        <div style={{ height:220, background:"linear-gradient(135deg,#0E1A0A,#1A2E12)", display:"flex", alignItems:"center", justifyContent:"center", position:"relative", overflow:"hidden" }}>
          <div style={{ position:"absolute", inset:0, background:"radial-gradient(ellipse at 50% 50%, rgba(30,123,74,.3) 0%, transparent 70%)" }}/>
          <div style={{ textAlign:"center", position:"relative" }}>
            <div style={{ fontSize:72, marginBottom:8, animation:"float 3s ease-in-out infinite" }}>🎬</div>
            <div style={{ fontSize:11, color:"rgba(255,255,255,.4)", letterSpacing:2, textTransform:"uppercase" }}>Movie Ticket Offer</div>
          </div>
        </div>
        <button onClick={onBack} style={{ position:"absolute", top:16, left:16, width:36, height:36, borderRadius:10, background:"rgba(0,0,0,.4)", border:"none", cursor:"pointer", display:"flex", alignItems:"center", justifyContent:"center" }}>
          <Icon name="chevronL" size={20} color="white"/>
        </button>
      </div>

      <div className="scroll-area" style={{ flex:1, padding:"24px 20px 100px" }}>
        <div style={{ marginBottom:6 }}>
          <span className="tag-green" style={{ marginBottom:10, display:"inline-block" }}>MOVIE TICKET</span>
        </div>
        <div style={{ fontFamily:"var(--font-display)", fontSize:26, color:"var(--ink2)", lineHeight:1.2, marginBottom:8 }}>Free Movie Ticket</div>
        <div style={{ fontSize:14, color:"var(--muted)", lineHeight:1.7, marginBottom:24 }}>
          Refer 5 friends to RS³ Films and earn a free movie ticket to any PVR or INOX screen near you. Valid for any show until April 30, 2025.
        </div>

        {/* Progress */}
        <div style={{ background:"var(--white)", borderRadius:14, padding:"16px", marginBottom:20, border:"1px solid var(--faint)" }}>
          <div style={{ display:"flex", justifyContent:"space-between", marginBottom:10 }}>
            <div style={{ fontSize:13, fontWeight:700, color:"var(--ink2)" }}>Your Progress</div>
            <div style={{ fontSize:13, fontWeight:700, color:"var(--gold)" }}>4 / 5</div>
          </div>
          <div className="prog-track" style={{ height:10, marginBottom:8 }}><div className="prog-fill" style={{ width:"80%" }}/></div>
          <div style={{ fontSize:12, color:"var(--green)", fontWeight:600 }}>🎉 1 more referral and you unlock this!</div>
        </div>

        {/* Details */}
        {[["Reward","1 Movie Ticket (any show)"],["Valid Until","April 30, 2025"],["Target","5 Direct Referrals"],["Claims Left","66 / 100"],["Delivery","Code sent via push notification + app"]].map(([k,v])=>(
          <div key={k} style={{ display:"flex", justifyContent:"space-between", padding:"11px 0", borderBottom:"1px solid var(--faint)" }}>
            <div style={{ fontSize:13, color:"var(--muted)" }}>{k}</div>
            <div style={{ fontSize:13, fontWeight:500, color:"var(--ink2)" }}>{v}</div>
          </div>
        ))}
      </div>

      {/* CTA */}
      <div style={{ position:"absolute", bottom:0, left:0, right:0, padding:"16px 20px 28px", background:"linear-gradient(to top, var(--white) 70%, transparent)" }}>
        {!claimed ? (
          <button className="btn-gold" style={{ opacity:.6, cursor:"not-allowed" }}>Refer 1 more to unlock</button>
        ) : (
          <div style={{ background:"rgba(30,123,74,.08)", border:"1px solid rgba(30,123,74,.2)", borderRadius:14, padding:"16px", textAlign:"center" }}>
            <div style={{ fontSize:13, color:"var(--green)", fontWeight:700, marginBottom:4 }}>✓ Ticket Unlocked!</div>
            <div style={{ fontFamily:"monospace", fontSize:18, fontWeight:700, color:"var(--ink2)", letterSpacing:2 }}>TICKET-RS3-4821</div>
            <div style={{ fontSize:11, color:"var(--muted)", marginTop:4 }}>Show this at the cinema counter</div>
          </div>
        )}
      </div>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   10 — REFERRAL TREE
══════════════════════════════════════════════════════════════════════════════ */
const ReferralTreeScreen = ({ onBack }) => {
  const tree = {
    name:"You", coins:1240, count:24,
    l1:[
      { name:"Priya M.", coins:560, count:5, l2:[{ name:"Rahul D.", coins:80 },{ name:"Kavita S.", coins:120 }] },
      { name:"Sunita R.", coins:340, count:3, l2:[{ name:"Anil K.", coins:40 }] },
      { name:"Kiran B.", coins:200, count:2, l2:[] },
    ]
  };

  const PersonNode = ({ name, coins, size="md" }) => (
    <div style={{ textAlign:"center", display:"flex", flexDirection:"column", alignItems:"center" }}>
      <div style={{ width: size==="lg"?52:size==="md"?40:34, height:size==="lg"?52:size==="md"?40:34, borderRadius:"50%", background: size==="lg"?"linear-gradient(135deg,#8A5C1A,#C9973A)":"var(--faint)", border: size==="lg"?"2.5px solid var(--gold)":"1.5px solid var(--faint)", display:"flex", alignItems:"center", justifyContent:"center", marginBottom:4 }}>
        <span style={{ fontFamily:"var(--font-display)", fontSize:size==="lg"?18:size==="md"?14:12, color:size==="lg"?"#FBF0D8":"var(--muted)" }}>{name[0]}</span>
      </div>
      <div style={{ fontSize: size==="lg"?12:size==="md"?11:10, fontWeight:600, color: size==="lg"?"var(--ink2)":"var(--mid)", maxWidth:60, lineHeight:1.2 }}>{name}</div>
      {coins && <div style={{ fontSize:9, color:"var(--gold)", fontWeight:700, marginTop:1 }}>+{coins}</div>}
    </div>
  );

  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <div style={{ background:"var(--white)", flexShrink:0 }}>
        <StatusBar/>
        <div style={{ display:"flex", alignItems:"center", gap:12, padding:"8px 20px 14px" }}>
          <button onClick={onBack} style={{ background:"none", border:"none", cursor:"pointer" }}><Icon name="chevronL" size={22} color="var(--ink3)"/></button>
          <div style={{ fontFamily:"var(--font-display)", fontSize:20, color:"var(--ink2)" }}>Referral Tree</div>
        </div>
      </div>

      <div className="scroll-area" style={{ flex:1, padding:"20px 16px 40px" }}>
        {/* Stats strip */}
        <div style={{ display:"grid", gridTemplateColumns:"1fr 1fr 1fr", gap:10, marginBottom:24 }}>
          {[["24","Network Size"],["3","Active Levels"],["1,240","Coins Earned"]].map(([v,l])=>(
            <div key={l} style={{ background:"var(--white)", borderRadius:12, padding:"12px 8px", textAlign:"center", border:"1px solid var(--faint)" }}>
              <div style={{ fontFamily:"var(--font-display)", fontSize:20, color:"var(--gold)" }}>{v}</div>
              <div style={{ fontSize:9, color:"var(--muted)", fontWeight:600, letterSpacing:.5 }}>{l}</div>
            </div>
          ))}
        </div>

        {/* Tree visual */}
        <div style={{ background:"var(--white)", borderRadius:16, padding:"24px 16px", border:"1px solid var(--faint)" }}>
          {/* Root */}
          <div style={{ display:"flex", justifyContent:"center", marginBottom:8 }}>
            <PersonNode name="You" coins={null} size="lg"/>
          </div>

          {/* L1 connector */}
          <div style={{ display:"flex", justifyContent:"center", marginBottom:8 }}>
            <div style={{ width:2, height:20, background:"var(--gold)", opacity:.4 }}/>
          </div>

          <div style={{ display:"flex", alignItems:"flex-start", justifyContent:"space-between", position:"relative", marginBottom:8 }}>
            {/* Horizontal line */}
            <div style={{ position:"absolute", top:20, left:"16.6%", right:"16.6%", height:2, background:"rgba(201,151,58,.2)" }}/>
            {tree.l1.map((n, i) => (
              <div key={i} style={{ display:"flex", flexDirection:"column", alignItems:"center", flex:1 }}>
                <div style={{ width:2, height:16, background:"rgba(201,151,58,.3)", marginBottom:6 }}/>
                <PersonNode name={n.name} coins={n.count>0?`${n.count} refs`:null} size="md"/>
              </div>
            ))}
          </div>

          {/* L2 */}
          {tree.l1.map((n1, i) => n1.l2.length > 0 && (
            <div key={i} style={{ marginLeft: `${(i/3)*100 + 3}%`, marginBottom:8 }}>
              <div style={{ width:2, height:14, background:"rgba(201,151,58,.2)", marginLeft:18, marginBottom:4 }}/>
              <div style={{ display:"flex", gap:10 }}>
                {n1.l2.map((n2, j) => <PersonNode key={j} name={n2.name} coins={null} size="sm"/>)}
              </div>
            </div>
          ))}

          {/* Legend */}
          <div style={{ marginTop:20, paddingTop:16, borderTop:"1px solid var(--faint)", display:"flex", gap:16, justifyContent:"center" }}>
            {[["var(--gold)","L1 +100"],["rgba(201,151,58,.6)","L2 +40"],["rgba(201,151,58,.3)","L3 +15"]].map(([c,l])=>(
              <div key={l} style={{ display:"flex", alignItems:"center", gap:5 }}>
                <div style={{ width:10, height:10, borderRadius:"50%", background:c }}/>
                <span style={{ fontSize:10, color:"var(--muted)", fontWeight:600 }}>{l}</span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   11 — MY REWARDS
══════════════════════════════════════════════════════════════════════════════ */
const MyRewardsScreen = ({ onBack }) => {
  const rewards = [
    { title:"Free Movie Ticket", code:"TICKET-RS3-4821", type:"movie_ticket", status:"fulfilled", date:"Mar 2", icon:"🎬" },
    { title:"10% Off Merchandise", code:"RS3MERCH10", type:"coupon_code", status:"claimed", date:"Mar 3", icon:"🏷️" },
    { title:"Premiere Night Pass", code:"PASS-PREM-MAR15", type:"event_pass", status:"claimed", date:"Mar 1", icon:"🎪" },
  ];
  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <div style={{ background:"var(--white)", flexShrink:0 }}>
        <StatusBar/>
        <div style={{ display:"flex", alignItems:"center", gap:12, padding:"8px 20px 14px" }}>
          <button onClick={onBack} style={{ background:"none", border:"none", cursor:"pointer" }}><Icon name="chevronL" size={22} color="var(--ink3)"/></button>
          <div style={{ fontFamily:"var(--font-display)", fontSize:20, color:"var(--ink2)" }}>My Rewards</div>
        </div>
      </div>

      <div className="scroll-area" style={{ flex:1, padding:"16px 16px 40px" }}>
        {rewards.map((r,i) => (
          <div key={i} style={{ background:"var(--white)", borderRadius:16, padding:"18px", marginBottom:12, border:"1px solid var(--faint)" }}>
            <div style={{ display:"flex", gap:14, alignItems:"flex-start", marginBottom:12 }}>
              <div style={{ width:48, height:48, borderRadius:12, background:"var(--goldpale)", display:"flex", alignItems:"center", justifyContent:"center", fontSize:24, flexShrink:0 }}>{r.icon}</div>
              <div style={{ flex:1 }}>
                <div style={{ fontSize:14, fontWeight:700, color:"var(--ink2)", marginBottom:4 }}>{r.title}</div>
                <div style={{ display:"flex", gap:8, alignItems:"center" }}>
                  <span className={`badge ${r.status==="fulfilled"?"tag-green":"tag-gold"}`} style={{ fontSize:9 }}>{r.status}</span>
                  <span style={{ fontSize:11, color:"var(--muted)" }}>{r.date}</span>
                </div>
              </div>
            </div>
            <div style={{ background:"var(--faint)", borderRadius:8, padding:"10px 14px", display:"flex", justifyContent:"space-between", alignItems:"center" }}>
              <div style={{ fontFamily:"monospace", fontSize:14, fontWeight:700, color:"var(--ink2)", letterSpacing:1.5 }}>{r.code}</div>
              <button style={{ background:"none", border:"none", cursor:"pointer" }}><Icon name="copy" size={16} color="var(--muted)"/></button>
            </div>
            {r.status==="claimed" && (
              <div style={{ fontSize:11, color:"var(--muted)", marginTop:8, display:"flex", gap:5 }}>
                <Icon name="info" size={13} color="var(--muted)"/>
                Delivery in progress — admin will mark fulfilled
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   12 — SETTINGS
══════════════════════════════════════════════════════════════════════════════ */
const SettingsScreen = ({ onBack }) => {
  const [notifs, setNotifs] = useState({ referral:true, coins:true, offers:true, updates:false });
  const Toggle = ({ on, onChange }) => (
    <div onClick={() => onChange(!on)} style={{ width:44, height:24, borderRadius:12, background: on?"var(--gold)":"var(--faint)", cursor:"pointer", position:"relative", transition:"background .2s", flexShrink:0 }}>
      <div style={{ width:18, height:18, borderRadius:9, background:"white", position:"absolute", top:3, left: on?23:3, transition:"left .2s", boxShadow:"0 1px 4px rgba(0,0,0,.2)" }}/>
    </div>
  );
  return (
    <div className="screen" style={{ background:"var(--surface)", display:"flex", flexDirection:"column" }}>
      <div style={{ background:"var(--white)", flexShrink:0 }}>
        <StatusBar/>
        <div style={{ display:"flex", alignItems:"center", gap:12, padding:"8px 20px 14px" }}>
          <button onClick={onBack} style={{ background:"none", border:"none", cursor:"pointer" }}><Icon name="chevronL" size={22} color="var(--ink3)"/></button>
          <div style={{ fontFamily:"var(--font-display)", fontSize:20, color:"var(--ink2)" }}>Settings</div>
        </div>
      </div>

      <div className="scroll-area" style={{ flex:1, padding:"16px 20px 40px" }}>
        <div style={{ fontSize:10, fontWeight:700, letterSpacing:2, color:"var(--muted)", textTransform:"uppercase", marginBottom:12 }}>Notifications</div>
        {[["referral","New Referrals","When someone joins with your code"],["coins","Coin Updates","Earnings, bonuses, and deductions"],["offers","Offer Alerts","New and expiring offers"],["updates","App Updates","New features and announcements"]].map(([k,l,s])=>(
          <div key={k} style={{ display:"flex", alignItems:"center", justifyContent:"space-between", padding:"14px 0", borderBottom:"1px solid var(--faint)" }}>
            <div>
              <div style={{ fontSize:14, fontWeight:500, color:"var(--ink2)" }}>{l}</div>
              <div style={{ fontSize:11, color:"var(--muted)", marginTop:1 }}>{s}</div>
            </div>
            <Toggle on={notifs[k]} onChange={v=>setNotifs(n=>({...n,[k]:v}))}/>
          </div>
        ))}

        <div style={{ fontSize:10, fontWeight:700, letterSpacing:2, color:"var(--muted)", textTransform:"uppercase", margin:"24px 0 12px" }}>Account</div>
        {[["phone","Mobile Number","+91 98765 43210"],["mail","Email","arjun@mail.com"],["lock","Change Password","Last changed 30 days ago"]].map(([icon,l,v])=>(
          <div key={l} style={{ display:"flex", alignItems:"center", gap:12, padding:"14px 0", borderBottom:"1px solid var(--faint)" }}>
            <div style={{ width:36, height:36, borderRadius:10, background:"rgba(201,151,58,.08)", display:"flex", alignItems:"center", justifyContent:"center", flexShrink:0 }}>
              <Icon name={icon} size={16} color="var(--gold)"/>
            </div>
            <div style={{ flex:1 }}>
              <div style={{ fontSize:13, fontWeight:500, color:"var(--ink2)" }}>{l}</div>
              <div style={{ fontSize:11, color:"var(--muted)" }}>{v}</div>
            </div>
            <Icon name="chevron" size={16} color="var(--muted)"/>
          </div>
        ))}

        <div style={{ fontSize:10, fontWeight:700, letterSpacing:2, color:"var(--muted)", textTransform:"uppercase", margin:"24px 0 12px" }}>Legal</div>
        {["Terms & Conditions","Privacy Policy","About RS³ Films","FAQ"].map(l=>(
          <div key={l} style={{ display:"flex", alignItems:"center", justifyContent:"space-between", padding:"13px 0", borderBottom:"1px solid var(--faint)" }}>
            <div style={{ fontSize:13, color:"var(--ink2)" }}>{l}</div>
            <Icon name="chevron" size={16} color="var(--muted)"/>
          </div>
        ))}

        <div style={{ textAlign:"center", marginTop:32 }}>
          <div style={{ fontSize:11, color:"var(--muted)" }}>RS³ Films · v1.2.0</div>
          <div style={{ fontSize:10, color:"var(--muted)", marginTop:2 }}>© 2025 RS³ Films. All rights reserved.</div>
        </div>
      </div>
    </div>
  );
};

/* ══════════════════════════════════════════════════════════════════════════════
   ROOT APP
══════════════════════════════════════════════════════════════════════════════ */
const SCREENS = [
  { id:"splash",   label:"01 Splash" },
  { id:"onboard",  label:"02 Onboarding" },
  { id:"register", label:"03 Register" },
  { id:"login",    label:"04 Login" },
  { id:"home",     label:"05 Home Feed" },
  { id:"refer",    label:"06 Referral" },
  { id:"wallet",   label:"07 Wallet" },
  { id:"profile",  label:"08 Profile" },
  { id:"offer",    label:"09 Offer Detail" },
  { id:"tree",     label:"10 Ref Tree" },
  { id:"rewards",  label:"11 My Rewards" },
  { id:"settings", label:"12 Settings" },
];

export default function RS3App() {
  const [current, setCurrent] = useState("splash");

  const navigate = (to) => setCurrent(to);

  const renderScreen = (id) => {
    switch(id) {
      case "splash":   return <SplashScreen onNext={() => navigate("onboard")}/>;
      case "onboard":  return <OnboardingScreen onNext={() => navigate("register")}/>;
      case "register": return <RegisterScreen onNext={() => navigate("login")}/>;
      case "login":    return <LoginScreen onNext={() => navigate("home")}/>;
      case "home":     return <HomeScreen onNavigate={navigate}/>;
      case "refer":    return <ReferralScreen onNavigate={navigate} onDetail={() => navigate("offer")}/>;
      case "wallet":   return <WalletScreen onNavigate={navigate}/>;
      case "profile":  return <ProfileScreen onNavigate={navigate}/>;
      case "offer":    return <OfferDetailScreen onBack={() => navigate("refer")}/>;
      case "tree":     return <ReferralTreeScreen onBack={() => navigate("refer")}/>;
      case "rewards":  return <MyRewardsScreen onBack={() => navigate("profile")}/>;
      case "settings": return <SettingsScreen onBack={() => navigate("profile")}/>;
      default: return null;
    }
  };

  return (
    <>
      <style>{FONTS}{CSS}</style>

      {/* Global nav */}
      <div className="global-nav">
        <span className="gn-logo">RS³</span>
        {SCREENS.map(s => (
          <button key={s.id} className={`gn-btn ${current===s.id?"active":""}`} onClick={() => setCurrent(s.id)}>{s.label}</button>
        ))}
      </div>

      {/* All screens visible in grid */}
      <div style={{ marginTop:80, display:"flex", flexWrap:"wrap", gap:28, justifyContent:"center", padding:"20px 16px 60px" }}>
        {SCREENS.map(s => (
          <div key={s.id} style={{ display:"flex", flexDirection:"column", alignItems:"center" }}>
            <div className="phone" onClick={() => setCurrent(s.id)} style={{ cursor: current===s.id ? "default" : "pointer", boxShadow: current===s.id ? "0 32px 80px rgba(0,0,0,.6), 0 0 0 2px #C9973A" : "0 32px 80px rgba(0,0,0,0.5),0 0 0 1px rgba(255,255,255,0.06)", transition:"box-shadow .3s" }}>
              {renderScreen(s.id)}
            </div>
            <div className="screen-label" style={{ color: current===s.id ? "#C9973A" : "rgba(255,255,255,0.35)" }}>{s.label}</div>
          </div>
        ))}
      </div>
    </>
  );
}
