import { useState, useEffect, useRef } from "react";

/* ─────────────────────────────────────────────────────────────────────────────
   RS³ FILMS — REGISTRATION  |  Light · Luxury · Editorial
   Palette: Warm ivory parchment + champagne gold + near-black ink
   Typefaces: Cormorant Garamond (display) · Jost (body)
───────────────────────────────────────────────────────────────────────────── */

const T = {
  bg:          "#FAF7F2",
  bgAlt:       "#F5F0E8",
  surface:     "#FFFFFF",
  card:        "#FFFDF9",
  line:        "#E4DDD0",
  lineLight:   "#EDE8DE",
  gold:        "#A67C2E",
  goldBright:  "#C9A84C",
  goldDim:     "#7A5C1E",
  goldPale:    "#FDF6E3",
  goldAccent:  "rgba(166,124,46,0.08)",
  ink:         "#1C1408",
  inkMid:      "#3D2E10",
  inkLight:    "#7A6A50",
  muted:       "#A89880",
  mutedLight:  "#C4B49A",
  red:         "#8B2E2E",
  redPale:     "#FDF2F2",
  redLine:     "#C0707044",
  green:       "#2E6B45",
  greenPale:   "#F2FAF5",
  greenDim:    "#3D7A50",
};

const G = {
  gold:   `linear-gradient(135deg, #7A5C1E 0%, #C9A84C 45%, #E8C96A 70%, #A67C2E 100%)`,
  goldH:  `linear-gradient(90deg,  #7A5C1E 0%, #A67C2E 40%, #C9A84C 70%, #E8C96A 100%)`,
  bg:     `linear-gradient(160deg, #FAF7F2 0%, #F5F0E8 50%, #EDE8DE 100%)`,
};

/* ── Font injection ──────────────────────────────────────────────────────────*/
const injectFont = () => {
  if(document.getElementById("rs3-fonts")) return;
  const l=document.createElement("link");
  l.id="rs3-fonts"; l.rel="stylesheet";
  l.href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:ital,wght@0,300;0,400;0,500;0,600;1,300;1,400&family=Jost:wght@300;400;500;600&display=swap";
  document.head.appendChild(l);
};
injectFont();

/* ── Global styles ───────────────────────────────────────────────────────────*/
const injectStyles = () => {
  if(document.getElementById("rs3-styles")) return;
  const s=document.createElement("style");
  s.id="rs3-styles";
  s.textContent=`
    *, *::before, *::after { box-sizing: border-box; }
    input::placeholder, textarea::placeholder { color: ${T.muted}; font-weight: 300; }
    input[type=date]::-webkit-calendar-picker-indicator { opacity: 0.4; cursor: pointer; }
    select option { background: #fff; color: ${T.ink}; }
    input:-webkit-autofill, input:-webkit-autofill:focus {
      -webkit-box-shadow: 0 0 0 100px #fff inset !important;
      -webkit-text-fill-color: ${T.ink} !important;
    }
    @keyframes fadeUp {
      from { opacity: 0; transform: translateY(10px); }
      to   { opacity: 1; transform: translateY(0); }
    }
    @keyframes spin { to { transform: rotate(360deg); } }
    .rs3-input-row:focus-within .rs3-underline {
      background: ${G.goldH} !important;
      opacity: 1 !important;
    }
    .rs3-submit:hover { background: ${T.goldAccent} !important; border-color: ${T.goldBright} !important; }
    .rs3-otp-cell:focus { border-color: ${T.gold} !important; background: ${T.goldPale} !important; }
  `;
  document.head.appendChild(s);
};
injectStyles();

/* ── Validation ──────────────────────────────────────────────────────────────*/
const V = {
  name:             v => !v.trim()?"Full name is required":v.trim().length<3?"Minimum 3 characters":!/^[a-zA-Z\s.'-]+$/.test(v.trim())?"Letters and spaces only":null,
  phone:            v => { const d=v.replace(/\D/g,""); return !d?"Mobile number is required":d.length!==10?"Must be exactly 10 digits":!/^[6-9]/.test(d)?"Must begin with 6, 7, 8 or 9":null; },
  email:            v => !v.trim()?"Email address is required":!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v.trim())?"Invalid email format":null,
  gender:           v => !v?"Please select gender":null,
  dob:              v => { if(!v) return "Date of birth is required"; const a=(Date.now()-new Date(v))/(1000*60*60*24*365.25); return a<13?"Must be at least 13 years old":a>110?"Please enter a valid date":null; },
  address_line:     v => !v.trim()?"Address is required":v.trim().length<5?"Please enter full address":null,
  pincode:          v => !v.trim()?"Pincode is required":!/^\d{6}$/.test(v.trim())?"Must be exactly 6 digits":null,
  city:             v => !v.trim()?"City is required":null,
  state:            v => !v.trim()?"State is required":null,
  referral_code:    v => !v.trim()?"Referral code is mandatory":!/^RS3_[A-Z0-9]{4,10}$/i.test(v.trim())?"Invalid format — expected RS3_XXXXXX":null,
  password:         v => !v?"Password is required":v.length<8?"Minimum 8 characters":!/[A-Z]/.test(v)?"Include one uppercase letter":!/[0-9]/.test(v)?"Include one number":!/[^A-Za-z0-9]/.test(v)?"Include one special character":null,
  confirm_password: (v,a) => !v?"Please confirm your password":v!==a.password?"Passwords do not match":null,
  otp:              v => !v.trim()?"OTP is required":!/^\d{4,6}$/.test(v.trim())?"Enter the 4–6 digit code":null,
};

function pwScore(p) {
  let s=0;
  if(p.length>=8)s++; if(p.length>=12)s++;
  if(/[A-Z]/.test(p))s++; if(/[0-9]/.test(p))s++; if(/[^A-Za-z0-9]/.test(p))s++;
  if(s<=1) return {label:"Weak",   color:"#A03030", w:"18%"};
  if(s<=2) return {label:"Fair",   color:"#A07020", w:"42%"};
  if(s<=3) return {label:"Good",   color:T.gold,    w:"68%"};
           return {label:"Strong", color:T.green,   w:"100%"};
}

/* ── Shared primitives ───────────────────────────────────────────────────────*/

const Logo = () => (
  <div style={{textAlign:"center", padding:"48px 0 32px", animation:"fadeUp .5s ease both"}}>
    <div style={{
      fontFamily:"'Cormorant Garamond', serif",
      fontSize:11, fontWeight:500, letterSpacing:7,
      color:T.muted, marginBottom:14, textTransform:"uppercase",
    }}>An Emotional Journey of Love</div>
    <div style={{
      fontFamily:"'Cormorant Garamond', serif",
      fontSize:42, fontWeight:300, letterSpacing:16,
      background:G.gold, WebkitBackgroundClip:"text", WebkitTextFillColor:"transparent",
      lineHeight:1, marginBottom:18,
    }}>RS³ FILMS</div>
    <div style={{width:48, height:"1px", background:G.goldH, margin:"0 auto 20px"}}/>
    <div style={{
      fontFamily:"'Jost', sans-serif",
      fontSize:10, fontWeight:400, letterSpacing:5,
      color:T.muted, textTransform:"uppercase",
    }}>Member Registration</div>
  </div>
);

/* Progress */
const Progress = ({value}) => (
  <div style={{padding:"0 24px 24px", animation:"fadeUp .4s ease .1s both"}}>
    <div style={{display:"flex", justifyContent:"space-between", marginBottom:7}}>
      <span style={{fontFamily:"'Jost', sans-serif", fontSize:9, letterSpacing:2.5, color:T.muted, textTransform:"uppercase"}}>Profile Completion</span>
      <span style={{fontFamily:"'Jost', sans-serif", fontSize:9, letterSpacing:1.5, color:T.gold, fontWeight:500}}>{value}%</span>
    </div>
    <div style={{height:"1px", background:T.lineLight, borderRadius:0}}>
      <div style={{height:"1px", width:`${value}%`, background:G.goldH, transition:"width .5s ease"}}/>
    </div>
  </div>
);

/* Section divider */
const Section = ({num, title, delay=0}) => (
  <div style={{display:"flex", alignItems:"center", gap:16, margin:"36px 0 24px", animation:`fadeUp .4s ease ${delay}ms both`}}>
    <div style={{
      width:26, height:26, borderRadius:"50%",
      border:`1px solid ${T.goldBright}`, flexShrink:0,
      display:"flex", alignItems:"center", justifyContent:"center",
      fontFamily:"'Jost', sans-serif", fontSize:10, fontWeight:500,
      color:T.gold, letterSpacing:0.5,
    }}>{num}</div>
    <span style={{fontFamily:"'Jost', sans-serif", fontSize:10, fontWeight:500, letterSpacing:4, color:T.inkLight, textTransform:"uppercase", whiteSpace:"nowrap"}}>{title}</span>
    <div style={{flex:1, height:"1px", background:`linear-gradient(90deg, ${T.line} 0%, transparent 100%)`}}/>
  </div>
);

/* Field */
const Field = ({label, error, touched, hint, children, delay=0}) => {
  const hasErr = touched && !!error;
  const isOk   = touched && !error;
  return (
    <div style={{marginBottom:24, animation:`fadeUp .35s ease ${delay}ms both`}}>
      <div style={{display:"flex", justifyContent:"space-between", alignItems:"baseline", marginBottom:9}}>
        <label style={{
          fontFamily:"'Jost', sans-serif", fontSize:9.5, fontWeight:500,
          letterSpacing:2.5, textTransform:"uppercase",
          color: hasErr ? T.red : isOk ? T.gold : T.inkLight,
          transition:"color .2s",
        }}>{label} <span style={{color:T.red}}>*</span></label>
        {isOk && <span style={{fontFamily:"'Jost', sans-serif", fontSize:10, color:T.green, fontWeight:400, letterSpacing:0.5}}>✓ Verified</span>}
      </div>

      {/* Input surface */}
      <div className="rs3-input-row" style={{
        background: hasErr ? T.redPale : isOk ? T.greenPale : T.surface,
        border:`1px solid ${hasErr ? T.redLine : isOk ? T.green+"33" : T.line}`,
        borderRadius:3,
        transition:"border .2s, background .2s",
        boxShadow: isOk ? `0 1px 8px rgba(46,107,69,0.06)` : hasErr ? `0 1px 8px rgba(139,46,46,0.06)` : `0 1px 4px rgba(60,40,10,0.04)`,
      }}>
        {children}
      </div>

      {/* Underline accent */}
      <div className="rs3-underline" style={{
        height:"1px",
        background: hasErr ? T.red : isOk ? T.greenDim : T.line,
        opacity: hasErr||isOk ? 1 : 0.6,
        transition:"background .2s",
        borderRadius:0,
      }}/>

      {hasErr && (
        <div style={{display:"flex", alignItems:"center", gap:7, marginTop:7, animation:"fadeUp .2s ease both"}}>
          <div style={{width:"2px", height:"2px", borderRadius:"50%", background:T.red, flexShrink:0}}/>
          <span style={{fontFamily:"'Jost', sans-serif", fontSize:11, color:T.red, fontWeight:300, letterSpacing:0.2}}>{error}</span>
        </div>
      )}
      {hint && !hasErr && (
        <div style={{marginTop:7, fontFamily:"'Jost', sans-serif", fontSize:11, color:T.muted, fontWeight:300, lineHeight:1.6, letterSpacing:0.1}}>{hint}</div>
      )}
    </div>
  );
};

const iBase = {
  width:"100%", background:"transparent", border:"none", outline:"none",
  fontFamily:"'Jost', sans-serif", fontSize:14, fontWeight:300,
  color:T.ink, padding:"14px 16px", letterSpacing:0.2, lineHeight:1,
};

const ShowHideBtn = ({show, onToggle}) => (
  <button onClick={onToggle} style={{
    background:"none", border:"none", cursor:"pointer",
    padding:"0 16px", flexShrink:0,
    fontFamily:"'Jost', sans-serif", fontSize:10, fontWeight:500,
    letterSpacing:1.5, color:T.gold, textTransform:"uppercase",
  }}>{show?"Hide":"Show"}</button>
);

/* ═══════════════════════════════════════════════════════════════════════════════
   REGISTER SCREEN
═══════════════════════════════════════════════════════════════════════════════ */
export default function RegisterScreen({onRegister, onGoLogin}) {

  const INIT = {
    name:"", phone:"", email:"", gender:"", dob:"",
    address_line:"", pincode:"", city:"", state:"",
    referral_code:"", password:"", confirm_password:"", otp:"",
  };

  const [vals,      setVals]     = useState(INIT);
  const [touched,   setTouched]  = useState({});
  const [showPw,    setShowPw]   = useState(false);
  const [showCf,    setShowCf]   = useState(false);
  const [selfie,    setSelfie]   = useState(null);
  const [selfieErr, setSelfieErr]= useState("");
  const [selfieT,   setSelfieT]  = useState(false);
  const [pinLoad,   setPinLoad]  = useState(false);
  const [pinStatus, setPinStatus]= useState(null);
  const [otpSent,   setOtpSent]  = useState(false);
  const [otpTimer,  setOtpTimer] = useState(0);
  const [submitted, setSubmitted]= useState(false);
  const timerRef = useRef(null);
  const fileRef  = useRef(null);

  const set   = (k,v) => setVals(p=>({...p,[k]:v}));
  const touch = k     => setTouched(p=>({...p,[k]:true}));
  const isTch = k     => !!(touched[k]||submitted);

  const errs = {};
  Object.keys(V).forEach(k => { errs[k] = V[k](vals[k], vals); });

  /* Pincode auto-fill */
  useEffect(()=>{
    const pin=vals.pincode.replace(/\D/g,"");
    if(pin.length!==6){ if(pin.length>0&&pin.length<6){set("city","");set("state","");setPinStatus(null);} return; }
    setPinLoad(true); setPinStatus(null);
    fetch(`https://api.postalpincode.in/pincode/${pin}`)
      .then(r=>r.json())
      .then(d=>{
        if(d[0]?.Status==="Success"){
          const po=d[0].PostOffice[0];
          set("city",po.District); set("state",po.State);
          setPinStatus("ok"); touch("city"); touch("state");
        } else { set("city",""); set("state",""); setPinStatus("err"); }
      })
      .catch(()=>{ set("city",""); set("state",""); setPinStatus("err"); })
      .finally(()=>setPinLoad(false));
  },[vals.pincode]);

  /* OTP */
  const sendOtp = () => {
    touch("phone"); if(errs.phone) return;
    setOtpSent(true); setOtpTimer(30);
    clearInterval(timerRef.current);
    timerRef.current=setInterval(()=>{
      setOtpTimer(t=>{ if(t<=1){clearInterval(timerRef.current);return 0;} return t-1; });
    },1000);
  };

  /* Selfie */
  const pickSelfie = f => {
    setSelfieT(true); if(!f) return;
    if(!f.type.startsWith("image/")){ setSelfieErr("File must be an image — JPG or PNG"); setSelfie(null); return; }
    if(f.size>5*1024*1024){ setSelfieErr("Maximum file size is 5 MB"); setSelfie(null); return; }
    setSelfieErr(""); setSelfie(f);
  };

  /* Submit */
  const submit = () => {
    setSubmitted(true); setSelfieT(true);
    const allT={}; Object.keys(INIT).forEach(k=>{allT[k]=true;}); setTouched(allT);
    if(Object.values(errs).some(Boolean)||!selfie) return;
    onRegister&&onRegister({...vals,selfie});
  };

  const filled   = Object.keys(INIT).filter(k=>!errs[k]&&vals[k]).length+(selfie?1:0);
  const progress = Math.round((filled/(Object.keys(INIT).length+1))*100);
  const pw       = vals.password ? pwScore(vals.password) : null;
  const anyErr   = submitted && (Object.values(errs).some(Boolean)||!selfie);

  return (
    <div style={{minHeight:"100vh", background:G.bg, fontFamily:"'Jost', sans-serif"}}>

      <Logo />
      <Progress value={progress} />

      <div style={{padding:"0 22px 60px"}}>

        {/* ── 01 PERSONAL ──────────────────────────────────────────────── */}
        <Section num="01" title="Personal Information" />

        <Field label="Full Name" error={errs.name} touched={isTch("name")} delay={0}>
          <input value={vals.name} placeholder="Your legal full name"
            onChange={e=>set("name",e.target.value)} onBlur={()=>touch("name")}
            style={iBase}/>
        </Field>

        <div style={{display:"grid", gridTemplateColumns:"1fr 1fr", gap:14}}>
          <Field label="Gender" error={errs.gender} touched={isTch("gender")} delay={40}>
            <div style={{position:"relative"}}>
              <select value={vals.gender}
                onChange={e=>{set("gender",e.target.value);touch("gender");}}
                onBlur={()=>touch("gender")}
                style={{...iBase, appearance:"none", cursor:"pointer", color:vals.gender?T.ink:T.muted, paddingRight:36}}>
                <option value="" disabled>Select</option>
                {["Male","Female","Non-binary","Prefer not to say"].map(g=><option key={g}>{g}</option>)}
              </select>
              <span style={{position:"absolute",right:14,top:"50%",transform:"translateY(-50%)",color:T.muted,fontSize:10,pointerEvents:"none"}}>▾</span>
            </div>
          </Field>

          <Field label="Date of Birth" error={errs.dob} touched={isTch("dob")} hint="Minimum age 13" delay={70}>
            <input type="date" value={vals.dob}
              max={new Date().toISOString().split("T")[0]}
              onChange={e=>set("dob",e.target.value)} onBlur={()=>touch("dob")}
              style={iBase}/>
          </Field>
        </div>

        {/* ── 02 CONTACT ───────────────────────────────────────────────── */}
        <Section num="02" title="Contact Details" delay={80}/>

        <Field label="Email Address" error={errs.email} touched={isTch("email")} delay={0}>
          <input type="email" value={vals.email} placeholder="your@email.com"
            onChange={e=>set("email",e.target.value)} onBlur={()=>touch("email")}
            style={iBase}/>
        </Field>

        <Field label="Mobile Number" error={errs.phone} touched={isTch("phone")}
          hint={otpSent&&!errs.phone?`OTP dispatched to +91 ${vals.phone}`:undefined} delay={40}>
          <div style={{display:"flex", alignItems:"center"}}>
            <div style={{display:"flex", alignItems:"center", padding:"0 14px", borderRight:`1px solid ${T.lineLight}`, flexShrink:0}}>
              <span style={{fontFamily:"'Jost', sans-serif", fontSize:12, color:T.muted, letterSpacing:1, paddingRight:6}}>+91</span>
            </div>
            <input value={vals.phone} placeholder="10-digit mobile number"
              maxLength={10}
              onChange={e=>set("phone",e.target.value.replace(/\D/g,"").slice(0,10))}
              onBlur={()=>touch("phone")}
              style={{...iBase, flex:1}}/>
            <button onClick={sendOtp} disabled={otpTimer>0||!!errs.phone} style={{
              margin:"8px 12px", padding:"8px 14px", flexShrink:0,
              background:"transparent",
              border:`1px solid ${otpTimer>0||errs.phone ? T.lineLight : T.gold}`,
              fontFamily:"'Jost', sans-serif", fontSize:10, fontWeight:500,
              letterSpacing:1.5, textTransform:"uppercase",
              color: otpTimer>0||errs.phone ? T.muted : T.gold,
              cursor: otpTimer>0||errs.phone ? "default" : "pointer",
              borderRadius:2, transition:"all .2s", whiteSpace:"nowrap",
            }}>
              {otpTimer>0 ? `Resend ${otpTimer}s` : otpSent ? "Resend OTP" : "Send OTP"}
            </button>
          </div>
        </Field>

        {/* OTP cells */}
        <Field label="OTP Verification" error={errs.otp} touched={isTch("otp")}
          hint={!otpSent?"Request OTP via the button above":undefined} delay={70}>
          <div style={{display:"flex", alignItems:"center", gap:10, padding:"12px 16px"}}>
            {[0,1,2,3,4,5].map(i=>(
              <input key={i} id={`otp-${i}`} maxLength={1} value={vals.otp[i]||""}
                className="rs3-otp-cell"
                onChange={e=>{
                  const o=vals.otp.split(""); o[i]=e.target.value.replace(/\D/g,"")[0]||"";
                  set("otp",o.join(""));
                  if(e.target.value&&i<5) document.getElementById(`otp-${i+1}`)?.focus();
                }}
                onKeyDown={e=>{if(e.key==="Backspace"&&!vals.otp[i]&&i>0) document.getElementById(`otp-${i-1}`)?.focus();}}
                onBlur={()=>touch("otp")}
                style={{
                  width:40, height:48, textAlign:"center",
                  background:T.bgAlt,
                  border:`1px solid ${T.line}`,
                  borderRadius:3, outline:"none",
                  fontFamily:"'Cormorant Garamond', serif",
                  fontSize:20, fontWeight:500, color:T.ink,
                  transition:"border .2s, background .2s",
                }}
              />
            ))}
          </div>
        </Field>

        {/* ── 03 ADDRESS ───────────────────────────────────────────────── */}
        <Section num="03" title="Address" delay={0}/>

        <Field label="Address Line" error={errs.address_line} touched={isTch("address_line")} delay={0}>
          <input value={vals.address_line} placeholder="House / flat, street, area"
            onChange={e=>set("address_line",e.target.value)} onBlur={()=>touch("address_line")}
            style={iBase}/>
        </Field>

        {/* Pincode */}
        <Field label="Pincode"
          error={errs.pincode || (pinStatus==="err" ? "Pincode not found — please fill city & state manually" : null)}
          touched={isTch("pincode")}
          hint={!errs.pincode&&pinStatus!=="err" ? "City and State populate automatically upon valid entry" : undefined}
          delay={40}>
          <div style={{display:"flex", alignItems:"center"}}>
            <input value={vals.pincode} placeholder="6-digit postal code" maxLength={6}
              onChange={e=>{set("pincode",e.target.value.replace(/\D/g,"").slice(0,6));touch("pincode");}}
              onBlur={()=>touch("pincode")}
              style={{...iBase, flex:1}}/>
            <span style={{padding:"0 16px", flexShrink:0, fontSize:13}}>
              {pinLoad && <span style={{color:T.gold, fontSize:11, letterSpacing:2}}>···</span>}
              {pinStatus==="ok"&&!pinLoad && <span style={{color:T.greenDim, fontWeight:500}}>✓</span>}
              {pinStatus==="err"&&!pinLoad && <span style={{color:T.red}}>✗</span>}
            </span>
          </div>
        </Field>

        <div style={{display:"grid", gridTemplateColumns:"1fr 1fr", gap:14}}>
          {/* City */}
          <Field label="City / District" error={errs.city} touched={isTch("city")} delay={60}>
            <div style={{position:"relative"}}>
              <input value={vals.city} placeholder="Auto-filled"
                onChange={e=>set("city",e.target.value)} onBlur={()=>touch("city")}
                style={{...iBase, paddingRight: pinStatus==="ok"&&vals.city ? 52 : 16}}/>
              {pinStatus==="ok"&&vals.city&&(
                <span style={{position:"absolute",right:12,top:"50%",transform:"translateY(-50%)",
                  fontSize:9,letterSpacing:1.5,color:T.gold,fontWeight:500,textTransform:"uppercase",
                  background:T.goldPale, padding:"2px 6px", borderRadius:2}}>Auto</span>
              )}
            </div>
          </Field>

          {/* State */}
          <Field label="State" error={errs.state} touched={isTch("state")} delay={80}>
            <div style={{position:"relative"}}>
              <input value={vals.state} placeholder="Auto-filled"
                onChange={e=>set("state",e.target.value)} onBlur={()=>touch("state")}
                style={{...iBase, paddingRight: pinStatus==="ok"&&vals.state ? 52 : 16}}/>
              {pinStatus==="ok"&&vals.state&&(
                <span style={{position:"absolute",right:12,top:"50%",transform:"translateY(-50%)",
                  fontSize:9,letterSpacing:1.5,color:T.gold,fontWeight:500,textTransform:"uppercase",
                  background:T.goldPale, padding:"2px 6px", borderRadius:2}}>Auto</span>
              )}
            </div>
          </Field>
        </div>

        {/* ── 04 REFERRAL ──────────────────────────────────────────────── */}
        <Section num="04" title="Referral Code" delay={0}/>

        {/* Info callout */}
        <div style={{
          borderLeft:`2px solid ${T.gold}`,
          background:T.goldAccent,
          padding:"14px 18px", marginBottom:24,
          animation:"fadeUp .3s ease both",
        }}>
          <div style={{fontFamily:"'Jost', sans-serif", fontSize:11, color:T.inkLight, fontWeight:300, lineHeight:1.8, letterSpacing:0.2}}>
            A referral code is <span style={{color:T.gold, fontWeight:500}}>mandatory</span> to register.
            Request it from the person who invited you. Expected format:{" "}
            <span style={{fontFamily:"monospace", color:T.goldBright, fontWeight:600, letterSpacing:1.5}}>RS3_XXXXXX</span>
          </div>
        </div>

        <Field label="Referral Code" error={errs.referral_code} touched={isTch("referral_code")} delay={0}>
          <div style={{display:"flex", alignItems:"center"}}>
            <input value={vals.referral_code} placeholder="e.g. RS3_A7K2MX"
              onChange={e=>set("referral_code",e.target.value.toUpperCase())}
              onBlur={()=>touch("referral_code")}
              style={{...iBase, flex:1, fontFamily:"monospace", letterSpacing:2, fontSize:14}}/>
            {!errs.referral_code&&vals.referral_code&&(
              <span style={{padding:"0 16px", fontFamily:"'Jost', sans-serif", fontSize:10,
                color:T.greenDim, letterSpacing:1, fontWeight:500}}>✓ Valid</span>
            )}
          </div>
        </Field>

        {/* ── 05 SECURITY ──────────────────────────────────────────────── */}
        <Section num="05" title="Security" delay={0}/>

        <Field label="Password" error={errs.password} touched={isTch("password")}
          hint="Min 8 chars · uppercase · number · special character" delay={0}>
          <div style={{display:"flex", alignItems:"center"}}>
            <input type={showPw?"text":"password"} value={vals.password}
              placeholder="Create a strong password"
              onChange={e=>set("password",e.target.value)} onBlur={()=>touch("password")}
              style={{...iBase, flex:1}}/>
            <ShowHideBtn show={showPw} onToggle={()=>setShowPw(!showPw)}/>
          </div>
        </Field>

        {/* Strength indicator */}
        {pw&&(
          <div style={{marginTop:-16, marginBottom:22, animation:"fadeUp .25s ease both"}}>
            <div style={{height:"1px", background:T.lineLight, marginBottom:8}}>
              <div style={{height:"1px", width:pw.w, background:pw.color, transition:"width .35s, background .3s"}}/>
            </div>
            <div style={{display:"flex", alignItems:"center", justifyContent:"space-between"}}>
              <div style={{display:"flex", gap:18}}>
                {[
                  {t:"8+ chars",   ok:vals.password.length>=8},
                  {t:"Uppercase",  ok:/[A-Z]/.test(vals.password)},
                  {t:"Number",     ok:/[0-9]/.test(vals.password)},
                  {t:"Symbol",     ok:/[^A-Za-z0-9]/.test(vals.password)},
                ].map((r,i)=>(
                  <span key={i} style={{fontFamily:"'Jost', sans-serif", fontSize:10, letterSpacing:0.5,
                    color:r.ok?T.greenDim:T.muted, fontWeight:r.ok?500:300}}>
                    {r.ok?"✓ ":""}{r.t}
                  </span>
                ))}
              </div>
              <span style={{fontFamily:"'Jost', sans-serif", fontSize:10, fontWeight:600,
                letterSpacing:1.5, color:pw.color, textTransform:"uppercase"}}>{pw.label}</span>
            </div>
          </div>
        )}

        <Field label="Confirm Password" error={errs.confirm_password} touched={isTch("confirm_password")} delay={50}>
          <div style={{display:"flex", alignItems:"center"}}>
            <input type={showCf?"text":"password"} value={vals.confirm_password}
              placeholder="Re-enter your password"
              onChange={e=>set("confirm_password",e.target.value)} onBlur={()=>touch("confirm_password")}
              style={{...iBase, flex:1}}/>
            <ShowHideBtn show={showCf} onToggle={()=>setShowCf(!showCf)}/>
          </div>
        </Field>

        {/* ── 06 SELFIE ────────────────────────────────────────────────── */}
        <Section num="06" title="Identity Verification" delay={0}/>

        <div style={{fontFamily:"'Jost', sans-serif", fontSize:11, color:T.muted, fontWeight:300,
          lineHeight:1.9, marginBottom:20, letterSpacing:0.1}}>
          Upload a clear portrait photograph for identity verification. Ensure your face is visible in good lighting against a plain background. JPG or PNG, max 5 MB.
        </div>

        <input ref={fileRef} type="file" accept="image/*" style={{display:"none"}}
          onChange={e=>pickSelfie(e.target.files[0])}/>

        <div onClick={()=>fileRef.current?.click()} style={{
          border:`1px solid ${selfieT&&!selfie ? T.red+"66" : selfie ? T.greenDim+"55" : T.line}`,
          borderRadius:3,
          padding:"36px 24px",
          textAlign:"center",
          background: selfie ? T.greenPale : selfieT&&!selfie ? T.redPale : T.surface,
          cursor:"pointer",
          transition:"all .25s",
          marginBottom:8,
          boxShadow:`0 2px 12px rgba(60,40,10,0.04)`,
          position:"relative",
        }}>
          {/* Corner marks */}
          {!selfie&&[
            {top:0,left:0,  w:"1px",h:18, bg:T.gold},
            {top:0,left:0,  w:18,   h:"1px",bg:T.gold},
            {bottom:0,right:0,w:"1px",h:18, bg:T.gold},
            {bottom:0,right:0,w:18,  h:"1px",bg:T.gold},
          ].map((s,i)=>(
            <div key={i} style={{position:"absolute", width:s.w, height:s.h,
              top:s.top, left:s.left, bottom:s.bottom, right:s.right,
              background:s.bg, opacity:0.5}}/>
          ))}

          {selfie ? (
            <>
              <div style={{width:40,height:40,borderRadius:"50%",background:T.greenPale,
                border:`1px solid ${T.greenDim}55`,margin:"0 auto 12px",
                display:"flex",alignItems:"center",justifyContent:"center",
                fontFamily:"'Jost', sans-serif",fontSize:18,color:T.greenDim}}>✓</div>
              <div style={{fontFamily:"'Jost', sans-serif",fontSize:11,fontWeight:500,
                letterSpacing:1.5,textTransform:"uppercase",color:T.greenDim,marginBottom:5}}>
                Photo Selected
              </div>
              <div style={{fontFamily:"'Jost', sans-serif",fontSize:12,fontWeight:300,color:T.inkLight}}>
                {selfie.name}
              </div>
              <div style={{fontFamily:"'Jost', sans-serif",fontSize:11,color:T.muted,marginTop:4}}>
                {(selfie.size/1024).toFixed(0)} KB · Tap to replace
              </div>
            </>
          ) : (
            <>
              <div style={{
                width:44,height:44,borderRadius:"50%",margin:"0 auto 14px",
                border:`1px solid ${selfieT ? T.red+"66" : T.line}`,
                display:"flex",alignItems:"center",justifyContent:"center",
                color: selfieT ? T.red : T.muted, fontSize:18,
              }}>◉</div>
              <div style={{fontFamily:"'Jost', sans-serif",fontSize:11,fontWeight:500,
                letterSpacing:2,textTransform:"uppercase",
                color: selfieT ? T.red : T.inkLight, marginBottom:6}}>
                {selfieT ? "Portrait Required" : "Upload Portrait"}
              </div>
              <div style={{fontFamily:"'Jost', sans-serif",fontSize:11,fontWeight:300,color:T.muted}}>
                JPG or PNG · Maximum 5 MB
              </div>
            </>
          )}
        </div>

        {selfieErr&&(
          <div style={{display:"flex",alignItems:"center",gap:7,marginBottom:20,marginTop:6}}>
            <div style={{width:2,height:2,borderRadius:"50%",background:T.red,flexShrink:0}}/>
            <span style={{fontFamily:"'Jost', sans-serif",fontSize:11,color:T.red,fontWeight:300}}>{selfieErr}</span>
          </div>
        )}

        {/* ── Error summary ─────────────────────────────────────────────── */}
        {anyErr&&(
          <div style={{
            borderLeft:`2px solid ${T.red}`,
            background:T.redPale, padding:"16px 20px", marginBottom:28,
            animation:"fadeUp .3s ease both",
          }}>
            <div style={{fontFamily:"'Jost', sans-serif", fontSize:10, fontWeight:600,
              letterSpacing:2.5, textTransform:"uppercase", color:T.red, marginBottom:12}}>
              Please resolve before continuing
            </div>
            {Object.entries(errs).filter(([,e])=>e).map(([k,e])=>(
              <div key={k} style={{display:"flex",gap:10,marginBottom:6,
                fontFamily:"'Jost', sans-serif",fontSize:11,fontWeight:300,color:"#905050"}}>
                <span style={{color:T.red,flexShrink:0,marginTop:1}}>—</span>
                <span>
                  <span style={{fontWeight:500,color:"#7A3030"}}>
                    {k.replace(/_/g," ").replace(/\b\w/g,c=>c.toUpperCase())}:
                  </span>{" "}{e}
                </span>
              </div>
            ))}
            {!selfie&&(
              <div style={{display:"flex",gap:10,fontFamily:"'Jost', sans-serif",
                fontSize:11,fontWeight:300,color:"#905050"}}>
                <span style={{color:T.red,flexShrink:0}}>—</span>
                <span><span style={{fontWeight:500,color:"#7A3030"}}>Selfie:</span> Portrait photograph is required</span>
              </div>
            )}
          </div>
        )}

        {/* ── Submit ───────────────────────────────────────────────────── */}
        <button onClick={submit} className="rs3-submit" style={{
          width:"100%", padding:"18px", marginBottom:18,
          background:"transparent",
          border:`1px solid ${T.gold}`,
          borderRadius:2,
          fontFamily:"'Jost', sans-serif", fontSize:11, fontWeight:500,
          letterSpacing:6, textTransform:"uppercase", color:T.gold,
          cursor:"pointer", transition:"background .25s, border-color .25s",
          boxShadow:`0 2px 12px rgba(166,124,46,0.08)`,
        }}>
          Create Account
        </button>

        {/* OR divider */}
        <div style={{display:"flex",alignItems:"center",gap:18,margin:"4px 0 20px"}}>
          <div style={{flex:1,height:"1px",background:T.lineLight}}/>
          <span style={{fontFamily:"'Jost', sans-serif",fontSize:10,letterSpacing:2,color:T.muted,textTransform:"uppercase"}}>or</span>
          <div style={{flex:1,height:"1px",background:T.lineLight}}/>
        </div>

        <div style={{textAlign:"center", paddingBottom:20}}>
          <span style={{fontFamily:"'Jost', sans-serif",fontSize:12,color:T.muted,fontWeight:300}}>Already a member?  </span>
          <span onClick={onGoLogin} style={{
            fontFamily:"'Jost', sans-serif",fontSize:12,color:T.gold,
            fontWeight:500,letterSpacing:0.5,cursor:"pointer",
            borderBottom:`1px solid ${T.gold}44`, paddingBottom:1,
          }}>Sign In</span>
        </div>

      </div>
    </div>
  );
}
