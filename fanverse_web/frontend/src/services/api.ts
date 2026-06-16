/**
 * API Service Layer — Live backend integration
 * Backend: Express.js @ http://localhost:4001/v1
 */

import {
  MOCK_STATS, MOCK_FEED, MOCK_OFFERS, MOCK_RULES,
  MOCK_MEMBERS, MOCK_ADMINS, MOCK_REDEMPTIONS, MOCK_CMS,
  type Stat, type FeedPost, type Offer, type PointsRule,
  type Member, type Admin, type Redemption, type CmsContent,
} from '@/data/mockData';

// ── Configuration ──────────────────────────────────────
const USE_MOCK = false;
const API_BASE = 'http://localhost:4001/v1';

// ── Helpers ────────────────────────────────────────────
function formatDate(iso: string | null | undefined): string {
  if (!iso) return '';
  try {
    return new Date(iso).toLocaleDateString('en', { month: 'short', day: 'numeric' });
  } catch {
    return iso;
  }
}

// ── Token Management ───────────────────────────────────
let authToken: string | null = null;

export function setAuthToken(token: string) {
  authToken = token;
  localStorage.setItem('rs3_token', token);
}

export function getAuthToken(): string | null {
  if (!authToken) authToken = localStorage.getItem('rs3_token');
  return authToken;
}

export function clearAuth() {
  authToken = null;
  localStorage.removeItem('rs3_token');
}

// ── HTTP Helper ────────────────────────────────────────
async function apiFetch<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = getAuthToken();
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers,
    },
  });

  if (!res.ok) {
    const err = await res.json().catch(() => ({ error: { message: res.statusText } }));
    throw new Error(err.error?.message || 'API Error');
  }

  return res.json();
}

// ── Auth ───────────────────────────────────────────────
export async function login(email: string, password: string) {
  if (USE_MOCK) {
    const admin = MOCK_ADMINS.find(a => a.email === email);
    if (!admin) throw new Error('Invalid credentials');
    setAuthToken('mock-jwt-token');
    return { token: 'mock-jwt-token', admin };
  }
  const data = await apiFetch<{ token: string; admin: Admin }>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });
  setAuthToken(data.token);
  return data;
}

// ── Dashboard ──────────────────────────────────────────
export async function getDashboardStats(): Promise<Stat[]> {
  if (USE_MOCK) return MOCK_STATS;
  const data = await apiFetch<any>('/dashboard/stats');
  return [
    { label: 'Total Members', value: data.total_members.toLocaleString(), delta: `+${data.deltas.members_this_week} this week`, icon: '👥', color: 'rs3-blue' },
    { label: 'Active Referrals', value: data.active_referrals.toLocaleString(), delta: `+${data.deltas.referrals_today} today`, icon: '🔗', color: 'gold' },
    { label: 'Coins Issued', value: (data.coins_issued / 1000000).toFixed(1) + 'M', delta: `+${data.deltas.coins_today.toLocaleString()} today`, icon: '🪙', color: 'rs3-amber' },
    { label: 'Offers Claimed', value: data.offers_claimed.toLocaleString(), delta: `+${data.deltas.offers_today} today`, icon: '🎟️', color: 'rs3-green' },
  ];
}

export async function getReferralActivity(days = 7) {
  if (USE_MOCK) return { data: [42, 68, 55, 82, 91, 74, 89], labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'] };
  return apiFetch<{ data: number[]; labels: string[] }>(`/dashboard/referral-activity?days=${days}`);
}

// ── Members ────────────────────────────────────────────
export async function getMembers(params?: { page?: number; limit?: number; search?: string; status?: string }): Promise<{ data: Member[]; total: number }> {
  if (USE_MOCK) {
    let filtered = MOCK_MEMBERS;
    if (params?.search) {
      const s = params.search.toLowerCase();
      filtered = filtered.filter(m => m.name.toLowerCase().includes(s) || m.email.toLowerCase().includes(s));
    }
    if (params?.status) filtered = filtered.filter(m => m.status === params.status);
    return { data: filtered, total: filtered.length };
  }
  const qs = new URLSearchParams();
  if (params?.page) qs.set('page', String(params.page));
  if (params?.limit) qs.set('limit', String(params.limit));
  if (params?.search) qs.set('search', params.search);
  if (params?.status) qs.set('status', params.status);
  const res = await apiFetch<{ data: any[]; total: number }>(`/members?${qs}`);
  return {
    total: res.total,
    data: res.data.map(m => ({ ...m, joined: formatDate(m.joined || m.created_at) })),
  };
}

// ── Feed ───────────────────────────────────────────────
function extractYouTubeId(url?: string): string | null {
  if (!url) return null;
  const patterns = [
    /youtu\.be\/([A-Za-z0-9_-]{11})/,
    /youtube\.com\/watch\?.*v=([A-Za-z0-9_-]{11})/,
    /youtube\.com\/shorts\/([A-Za-z0-9_-]{11})/,
    /youtube\.com\/embed\/([A-Za-z0-9_-]{11})/,
  ];
  for (const p of patterns) { const m = p.exec(url); if (m) return m[1]; }
  return null;
}

function mapFeedPost(p: any): FeedPost {
  const link = p.link_url || p.link;
  const ytId = extractYouTubeId(link);
  const thumb = p.thumbnail_url || (ytId ? `https://img.youtube.com/vi/${ytId}/hqdefault.jpg` : undefined);
  return {
    id: p.id,
    layout: p.layout,
    title: p.title,
    subtitle: p.subtitle,
    type: p.content_type || p.type,
    status: p.status,
    author: p.author || 'Admin',
    date: formatDate(p.created_at) || p.date,
    body: p.body,
    link,
    media_url: p.media_url,
    thumbnail_url: thumb,
  };
}

export async function getFeed(status?: string): Promise<FeedPost[]> {
  if (USE_MOCK) return status ? MOCK_FEED.filter(f => f.status === status) : MOCK_FEED;
  const qs = status ? `?status=${status}` : '';
  const res = await apiFetch<{ data: any[] }>(`/feed${qs}`);
  return res.data.map(mapFeedPost);
}

export async function createFeedPost(post: Partial<FeedPost>) {
  if (USE_MOCK) return { ...post, id: Date.now() };
  const payload = { ...post, content_type: post.type, link_url: post.link };
  const p = await apiFetch<any>('/feed', { method: 'POST', body: JSON.stringify(payload) });
  return mapFeedPost(p);
}

export async function updateFeedPost(id: number, post: Partial<FeedPost>) {
  if (USE_MOCK) return { ...post, id };
  const payload = { ...post, content_type: post.type, link_url: post.link };
  const p = await apiFetch<any>(`/feed/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
  return mapFeedPost(p);
}

export async function updateFeedPostStatus(id: number, status: string) {
  if (USE_MOCK) return { id, status };
  const p = await apiFetch<any>(`/feed/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) });
  return mapFeedPost(p);
}

export async function deleteFeedPost(id: number) {
  if (USE_MOCK) return { message: 'Deleted' };
  return apiFetch(`/feed/${id}`, { method: 'DELETE' });
}

export async function uploadMedia(file: File, folder = 'feed'): Promise<string> {
  const token = getAuthToken();
  const formData = new FormData();
  formData.append('file', file);
  formData.append('folder', folder);
  const res = await fetch(`${API_BASE}/upload`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData,
  });
  if (!res.ok) throw new Error('Upload failed');
  const data = await res.json();
  return data.url as string;
}

// ── Offers ─────────────────────────────────────────────
export async function getOffers(): Promise<Offer[]> {
  if (USE_MOCK) return MOCK_OFFERS;
  const res = await apiFetch<{ data: Offer[] }>('/offers');
  return res.data;
}

export async function createOffer(offer: Partial<Offer>) {
  if (USE_MOCK) return { ...offer, id: Date.now() };
  return apiFetch('/offers', { method: 'POST', body: JSON.stringify(offer) });
}

export async function updateOffer(id: number, offer: Partial<Offer>) {
  if (USE_MOCK) return { ...offer, id };
  return apiFetch(`/offers/${id}`, { method: 'PUT', body: JSON.stringify(offer) });
}

export async function deleteOffer(id: number) {
  if (USE_MOCK) return { message: 'Deleted' };
  return apiFetch(`/offers/${id}`, { method: 'DELETE' });
}

export async function toggleOffer(id: number, active: boolean) {
  if (USE_MOCK) return { id, active };
  return apiFetch(`/offers/${id}/toggle`, { method: 'PATCH', body: JSON.stringify({ active }) });
}

// ── Redemptions ────────────────────────────────────────
export async function getRedemptions(status?: string): Promise<Redemption[]> {
  if (USE_MOCK) return status ? MOCK_REDEMPTIONS.filter(r => r.status === status) : MOCK_REDEMPTIONS;
  const qs = status ? `?status=${status}` : '';
  const res = await apiFetch<{ data: any[] }>(`/redemptions${qs}`);
  return res.data.map(r => ({
    id: r.id,
    user: r.user_name,
    offer: r.offer_title,
    reward: r.reward_sent,
    status: r.status as 'claimed' | 'fulfilled',
    date: formatDate(r.claimed_at),
  }));
}

export async function fulfillRedemption(id: number) {
  if (USE_MOCK) return { id, status: 'fulfilled' };
  return apiFetch(`/redemptions/${id}/fulfill`, { method: 'PATCH', body: JSON.stringify({ status: 'fulfilled' }) });
}

// ── Points Rules ───────────────────────────────────────
export async function getPointsRules(): Promise<PointsRule[]> {
  if (USE_MOCK) return MOCK_RULES;
  const res = await apiFetch<{ referral_levels: PointsRule[]; bonus_rules: PointsRule[] }>('/points-rules');
  return [...res.referral_levels, ...res.bonus_rules];
}

export async function createRule(rule: Partial<PointsRule>): Promise<PointsRule> {
  if (USE_MOCK) return { id: Date.now(), ...rule } as PointsRule;
  return apiFetch('/points-rules', { method: 'POST', body: JSON.stringify(rule) });
}

export async function updateRule(id: number, rule: Partial<PointsRule>): Promise<PointsRule> {
  if (USE_MOCK) return { id, ...rule } as PointsRule;
  return apiFetch(`/points-rules/${id}`, { method: 'PUT', body: JSON.stringify(rule) });
}

export async function deleteRule(id: number) {
  if (USE_MOCK) return { message: 'Deleted' };
  return apiFetch(`/points-rules/${id}`, { method: 'DELETE' });
}

export async function toggleRule(id: number, active: boolean) {
  if (USE_MOCK) return { id, active };
  return apiFetch(`/points-rules/${id}/toggle`, { method: 'PATCH', body: JSON.stringify({ active }) });
}

// ── CMS ────────────────────────────────────────────────
export async function getCms(): Promise<CmsContent> {
  if (USE_MOCK) return MOCK_CMS;
  const raw = await apiFetch<Record<string, any[]>>('/cms');
  // Map DB fields (content_key, content_type) to frontend interface (key, type)
  const mapped: CmsContent = {};
  for (const section of Object.keys(raw)) {
    mapped[section] = raw[section].map(item => ({
      key: item.content_key || item.key,
      label: item.label,
      type: (item.content_type || item.type) as any,
      value: item.value ?? '',
      section: item.section,
    }));
  }
  return mapped;
}

export async function updateCmsSection(section: string, items: { key: string; value: string }[]) {
  if (USE_MOCK) return items;
  // Backend expects content_key, not key
  const mapped = items.map(i => ({ content_key: i.key, value: i.value }));
  return apiFetch(`/cms/${section}`, { method: 'PUT', body: JSON.stringify({ items: mapped }) });
}

// ── Admins ─────────────────────────────────────────────
export async function getAdmins(): Promise<Admin[]> {
  if (USE_MOCK) return MOCK_ADMINS;
  const res = await apiFetch<{ data: any[] }>('/admins');
  return res.data.map(a => ({
    id: a.id,
    name: a.name,
    email: a.email,
    role: a.role,
    active: a.active,
    lastLogin: a.last_login ? formatDate(a.last_login) : 'Never',
  }));
}

export async function inviteAdmin(data: { name: string; email: string; role: string }) {
  if (USE_MOCK) return { id: Date.now(), ...data, active: true };
  return apiFetch('/admins/invite', { method: 'POST', body: JSON.stringify(data) });
}

export async function toggleAdmin(id: number, active: boolean) {
  if (USE_MOCK) return { id, active };
  return apiFetch(`/admins/${id}/toggle`, { method: 'PATCH', body: JSON.stringify({ active }) });
}

// ── Analytics ──────────────────────────────────────────
export async function getAnalyticsOverview() {
  if (USE_MOCK) {
    return { conversion_rate: 68, avg_coins_per_member: 496, total_offers_claimed: 386, avg_chain_depth: 1.8, churn_30d: 3.2 };
  }
  return apiFetch('/analytics/overview');
}

export async function getAnalyticsGrowth(months = 7) {
  if (USE_MOCK) {
    return {
      labels: ['Sep', 'Oct', 'Nov', 'Dec', 'Jan', 'Feb', 'Mar'],
      members: [210, 340, 480, 620, 880, 1200, 1450],
      referrals: [80, 140, 210, 320, 480, 760, 1000],
      coins: [12000, 18000, 24000, 32000, 48000, 72000, 94000],
      offers: [2, 8, 14, 28, 54, 96, 138],
    };
  }
  return apiFetch(`/analytics/growth?months=${months}`);
}
