// ── Types ──────────────────────────────────────────────────────────────────────

export interface Stat {
  label: string;
  value: string;
  delta: string;
  icon: string;
  color: string;
}

export interface FeedPost {
  id: number;
  layout: string;
  title: string;
  subtitle: string;
  type: string;
  status: string;
  author: string;
  date: string;
  body?: string;
  link?: string;
  media_url?: string;
  thumbnail_url?: string;
}

export type RewardType = "movie_ticket" | "event_pass" | "coupon_code" | "merchandise" | "custom";
export type OfferPage = "referral" | "wallet" | "home" | "global";

export interface Offer {
  id: number;
  title: string;
  description?: string;
  reward_type: RewardType;
  reward_value: string;
  coin_cost: number;
  target_referrals: number | null;
  page: OfferPage;
  active: boolean;
  max_claims: number | null;
  claims_count: number;
  valid_until?: string;
}

export type RuleType = "referral_level" | "registration_bonus" | "event_bonus";

export interface PointsRule {
  id: number;
  rule_key: string;
  label: string;
  coins: number;
  rule_type: RuleType;
  level_number: number | null;
  active: boolean;
  description: string;
}

export type MemberStatus = "ambassador" | "verified" | "pending";

export interface Member {
  id: number;
  name: string;
  phone: string;
  email: string;
  coins: number;
  referrals: number;
  status: MemberStatus;
  joined: string;
}

export type AdminRole = "super_admin" | "content" | "analytics" | "moderator";

export interface Admin {
  id: number;
  name: string;
  email: string;
  role: AdminRole;
  active: boolean;
  lastLogin: string;
}

export interface Redemption {
  id: number;
  user: string;
  offer: string;
  reward: string;
  status: "claimed" | "fulfilled";
  date: string;
}

export interface CmsItem {
  key: string;
  label: string;
  type: "text" | "image_url" | "richtext" | "boolean" | "number" | "url";
  value: string;
  section: string;
}

export interface CmsContent {
  [section: string]: CmsItem[];
}

export interface NavItem {
  id: string;
  label: string;
  icon: string;
}

export interface LayoutOption {
  id: string;
  label: string;
  icon: string;
  desc: string;
}

// ── Mock Data ─────────────────────────────────────────────────────────────────

export const MOCK_STATS: Stat[] = [
  { label: "Total Members", value: "4,821", delta: "+142 this week", icon: "👥", color: "rs3-blue" },
  { label: "Active Referrals", value: "1,247", delta: "+89 today", icon: "🔗", color: "gold" },
  { label: "Coins Issued", value: "2.4M", delta: "+18,400 today", icon: "🪙", color: "rs3-amber" },
  { label: "Offers Claimed", value: "386", delta: "+24 today", icon: "🎟️", color: "rs3-green" },
];

export const MOCK_FEED: FeedPost[] = [
  { id: 1, layout: "hero", title: "Premiere Night", subtitle: "PVR Mar 15", type: "image", status: "live", author: "Priya", date: "Mar 2" },
  { id: 2, layout: "card", title: "Fan Milestone", subtitle: "1000 members!", type: "text", status: "live", author: "Rajan", date: "Mar 1" },
  { id: 3, layout: "reel", title: "Behind the Scenes", subtitle: "Exclusive BTS", type: "video", status: "draft", author: "Priya", date: "Feb 28" },
  { id: 4, layout: "banner", title: "New Poster Drop", subtitle: "Official RS³", type: "image", status: "live", author: "Admin", date: "Feb 27" },
  { id: 5, layout: "update", title: "App Update v1.2", subtitle: "New features out", type: "link", status: "scheduled", author: "Rajan", date: "Feb 26" },
];

export const MOCK_OFFERS: Offer[] = [
  { id: 1, title: "Free Movie Ticket", reward_type: "movie_ticket", reward_value: "TICKET-FMT-001", coin_cost: 0, target_referrals: 5, page: "referral", active: true, max_claims: 100, claims_count: 34 },
  { id: 2, title: "Premiere Pass", reward_type: "event_pass", reward_value: "PASS-PREM-MAR15", coin_cost: 0, target_referrals: 10, page: "referral", active: true, max_claims: 50, claims_count: 12 },
  { id: 3, title: "10% Off Merchandise", reward_type: "coupon_code", reward_value: "RS3MERCH10", coin_cost: 200, target_referrals: null, page: "wallet", active: true, max_claims: null, claims_count: 28 },
  { id: 4, title: "VIP Event Invite", reward_type: "event_pass", reward_value: "VIP-EVENT-APR", coin_cost: 0, target_referrals: 20, page: "referral", active: false, max_claims: 20, claims_count: 0 },
];

export const MOCK_RULES: PointsRule[] = [
  { id: 1, rule_key: "referral_l1", label: "Direct Referral (Level 1)", coins: 100, rule_type: "referral_level", level_number: 1, active: true, description: "Awarded when your direct referral registers and verifies OTP" },
  { id: 2, rule_key: "referral_l2", label: "Indirect Referral (Level 2)", coins: 40, rule_type: "referral_level", level_number: 2, active: true, description: "Awarded when your L1 referral brings in another member" },
  { id: 3, rule_key: "referral_l3", label: "Chain Referral (Level 3)", coins: 15, rule_type: "referral_level", level_number: 3, active: true, description: "Awarded two levels below you in the chain" },
  { id: 4, rule_key: "profile_complete", label: "Profile Completion Bonus", coins: 50, rule_type: "registration_bonus", level_number: null, active: true, description: "One-time bonus when user completes their full profile" },
  { id: 5, rule_key: "first_event", label: "First Event Booking Bonus", coins: 150, rule_type: "event_bonus", level_number: null, active: true, description: "One-time reward on user's first event ticket purchase" },
  { id: 6, rule_key: "ambassador_unlock", label: "Ambassador Unlock Bonus", coins: 500, rule_type: "registration_bonus", level_number: null, active: true, description: "Awarded when user reaches Fan Ambassador status" },
];

export const MOCK_MEMBERS: Member[] = [
  { id: 1, name: "Arjun Kumar", phone: "+91 98765 43210", email: "arjun@mail.com", coins: 1240, referrals: 24, status: "ambassador", joined: "Jan 10" },
  { id: 2, name: "Priya Mehta", phone: "+91 90123 45678", email: "priya@mail.com", coins: 560, referrals: 5, status: "verified", joined: "Jan 15" },
  { id: 3, name: "Rahul Das", phone: "+91 88765 12345", email: "rahul@mail.com", coins: 340, referrals: 3, status: "pending", joined: "Feb 1" },
  { id: 4, name: "Sunita Reddy", phone: "+91 77654 32109", email: "sunita@mail.com", coins: 2100, referrals: 18, status: "ambassador", joined: "Dec 20" },
  { id: 5, name: "Anil Sharma", phone: "+91 99887 76655", email: "anil@mail.com", coins: 80, referrals: 0, status: "verified", joined: "Feb 28" },
];

export const MOCK_ADMINS: Admin[] = [
  { id: 1, name: "Priya Anand", email: "priya@rs3.com", role: "super_admin", active: true, lastLogin: "2h ago" },
  { id: 2, name: "Rajan Mehta", email: "rajan@rs3.com", role: "content", active: true, lastLogin: "1d ago" },
  { id: 3, name: "Sneha Rao", email: "sneha@rs3.com", role: "analytics", active: false, lastLogin: "5d ago" },
  { id: 4, name: "Kiran Bhat", email: "kiran@rs3.com", role: "content", active: true, lastLogin: "30m ago" },
];

export const MOCK_REDEMPTIONS: Redemption[] = [
  { id: 1, user: "Arjun Kumar", offer: "Free Movie Ticket", reward: "TICKET-FMT-001", status: "fulfilled", date: "Mar 2" },
  { id: 2, user: "Sunita Reddy", offer: "Premiere Pass", reward: "PASS-PREM-MAR15", status: "claimed", date: "Mar 3" },
  { id: 3, user: "Priya Mehta", offer: "10% Off Merchandise", reward: "RS3MERCH10", status: "claimed", date: "Mar 3" },
  { id: 4, user: "Rahul Das", offer: "Free Movie Ticket", reward: "TICKET-FMT-002", status: "fulfilled", date: "Mar 1" },
];

export const MOCK_CMS: CmsContent = {
  splash: [
    { key: "splash_image_url", label: "Splash Background Image", type: "image_url", value: "https://picsum.photos/seed/rs3/800/1200", section: "splash" },
    { key: "splash_tagline", label: "Splash Tagline", type: "text", value: "An Emotional Journey of Love", section: "splash" },
  ],
  onboarding: [
    { key: "onboarding_1_title", label: "Slide 1 — Title", type: "text", value: "Welcome to RS³ Films", section: "onboarding" },
    { key: "onboarding_1_image", label: "Slide 1 — Image", type: "image_url", value: "https://picsum.photos/seed/ob1/400/600", section: "onboarding" },
    { key: "onboarding_2_title", label: "Slide 2 — Title", type: "text", value: "Earn Coins by Sharing", section: "onboarding" },
    { key: "onboarding_3_title", label: "Slide 3 — Title", type: "text", value: "Unlock Exclusive Offers", section: "onboarding" },
  ],
  legal: [
    { key: "terms_and_conditions", label: "Terms & Conditions", type: "richtext", value: "<p>These are the Terms and Conditions for RS³ Films...</p>", section: "legal" },
    { key: "privacy_policy", label: "Privacy Policy", type: "richtext", value: "<p>Your privacy is important to us...</p>", section: "legal" },
    { key: "about_us", label: "About Us", type: "richtext", value: "<p>RS³ Films is a fan community platform...</p>", section: "legal" },
    { key: "faq", label: "FAQ", type: "richtext", value: "<p><b>Q: How do I earn coins?</b> A: Refer friends...</p>", section: "legal" },
  ],
  contact: [
    { key: "contact_email", label: "Support Email", type: "text", value: "support@rs3films.com", section: "contact" },
    { key: "contact_phone", label: "Support Phone", type: "text", value: "+91 98765 43210", section: "contact" },
    { key: "contact_address", label: "Office Address", type: "text", value: "123 Film Street, Chennai", section: "contact" },
    { key: "social_instagram", label: "Instagram URL", type: "url", value: "https://instagram.com/rs3films", section: "contact" },
    { key: "social_youtube", label: "YouTube URL", type: "url", value: "https://youtube.com/@rs3films", section: "contact" },
    { key: "social_whatsapp", label: "WhatsApp Link", type: "url", value: "https://wa.me/919876543210", section: "contact" },
  ],
  wallet: [
    { key: "coin_value_label", label: "Coin Value Label", type: "text", value: "100 Coins = 1 Movie Voucher", section: "wallet" },
    { key: "min_redeem_coins", label: "Minimum Coins to Redeem", type: "number", value: "200", section: "wallet" },
    { key: "ambassador_threshold", label: "Ambassador Threshold (Referrals)", type: "number", value: "25", section: "wallet" },
    { key: "referral_headline", label: "Referral Page Headline", type: "text", value: "Share & Earn Together", section: "wallet" },
  ],
  system: [
    { key: "maintenance_mode", label: "Maintenance Mode", type: "boolean", value: "false", section: "system" },
    { key: "maintenance_message", label: "Maintenance Message", type: "text", value: "We'll be back shortly.", section: "system" },
    { key: "app_version_message", label: "Update Banner Message", type: "text", value: "", section: "system" },
  ],
};

export const NAV_ITEMS: NavItem[] = [
  { id: "dashboard", label: "Dashboard", icon: "◈" },
  { id: "feed", label: "Home Feed", icon: "▤" },
  { id: "offers", label: "Offers", icon: "🎟" },
  { id: "redemptions", label: "Redemptions", icon: "◎" },
  { id: "points", label: "Points Rules", icon: "⬡" },
  { id: "cms", label: "App Content (CMS)", icon: "◑" },
  { id: "members", label: "Members", icon: "◐" },
  { id: "admins", label: "Admin Access", icon: "◒" },
  { id: "analytics", label: "Analytics", icon: "◰" },
];

export const FEED_LAYOUTS: LayoutOption[] = [
  { id: "hero", label: "Hero Banner", icon: "▬", desc: "Full-width header" },
  { id: "card", label: "Content Card", icon: "▭", desc: "Image + text" },
  { id: "reel", label: "Video / Reel", icon: "▶", desc: "Vertical video" },
  { id: "banner", label: "Poster", icon: "▮", desc: "Tall portrait" },
  { id: "update", label: "Text Update", icon: "▤", desc: "Text / link" },
  { id: "grid2", label: "2-Col Grid", icon: "▫▫", desc: "Side-by-side" },
];

export const REWARD_TYPES = [
  { value: "movie_ticket", label: "🎬 Movie Ticket" },
  { value: "event_pass", label: "🎪 Event Pass" },
  { value: "coupon_code", label: "🏷️ Coupon Code" },
  { value: "merchandise", label: "👕 Merchandise" },
  { value: "custom", label: "✨ Custom Reward" },
];
