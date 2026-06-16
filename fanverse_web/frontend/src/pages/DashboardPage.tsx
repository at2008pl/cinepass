import { useState, useEffect } from "react";
import { getDashboardStats, getReferralActivity, getOffers, getRedemptions } from "@/services/api";
import type { Stat, Offer, Redemption } from "@/data/mockData";
import RS3Badge from "@/components/shared/RS3Badge";
import SectionTitle from "@/components/shared/SectionTitle";

const DashboardPage = () => {
  const [stats, setStats] = useState<Stat[]>([]);
  const [activity, setActivity] = useState<{ data: number[]; labels: string[] }>({ data: [], labels: [] });
  const [offers, setOffers] = useState<Offer[]>([]);
  const [redemptions, setRedemptions] = useState<Redemption[]>([]);

  useEffect(() => {
    getDashboardStats().then(setStats).catch(console.error);
    getReferralActivity(7).then(setActivity).catch(console.error);
    getOffers().then(setOffers).catch(console.error);
    getRedemptions().then(r => setRedemptions(r.slice(0, 5))).catch(console.error);
  }, []);

  const colorMap: Record<string, "blue" | "gold" | "amber" | "green"> = {
    "rs3-blue": "blue", "gold": "gold", "rs3-amber": "amber", "rs3-green": "green",
  };
  const maxActivity = Math.max(...(activity.data.length ? activity.data : [1]));

  return (
    <div className="fade-in">
      <SectionTitle sub="No bank/cash integration. Offers are movie tickets, event passes & coupon codes.">
        Overview
      </SectionTitle>

      {/* Stats */}
      <div className="grid grid-cols-4 gap-4 mb-7">
        {stats.map((s, i) => (
          <div key={i} className="bg-card border border-border rounded p-5 hover:shadow-lg hover:-translate-y-px transition-all"
            style={{ borderTopWidth: 3, borderTopColor: `hsl(var(--${s.color}))` }}>
            <div className="flex justify-between items-start">
              <div className="text-[10px] font-semibold tracking-[1.5px] text-muted-foreground uppercase mb-2.5">{s.label}</div>
              <span className="text-lg">{s.icon}</span>
            </div>
            <div className="font-display text-[28px] text-foreground leading-none mb-1.5">{s.value}</div>
            <div className="text-xs font-medium text-rs3-green">{s.delta}</div>
          </div>
        ))}
      </div>

      {/* Info strip */}
      <div className="bg-gold-pale border border-gold-line border-l-[3px] border-l-gold rounded p-3 px-4 mb-6 flex gap-3 items-start">
        <span className="text-base shrink-0 mt-px">ℹ️</span>
        <div className="text-xs text-ink-light leading-relaxed">
          <b className="text-gold">Wallet model:</b> Coins are spent on movie tickets, event passes, and coupon codes. There is no bank account or cash payout system. Offers are fully controlled from the Offers module.
        </div>
      </div>

      {/* Chart + Active Offers */}
      <div className="grid grid-cols-[2fr_1fr] gap-4 mb-6">
        <div className="bg-card border border-border rounded p-6">
          <div className="text-sm font-semibold text-foreground mb-5">Referral Activity — Last 7 Days</div>
          <div className="flex items-end gap-2 h-[120px]">
            {activity.data.length > 0 ? activity.data.map((h, i) => (
              <div key={i} className="flex-1 flex flex-col items-center gap-1">
                <div
                  className={`w-full rounded-t-sm ${i === activity.data.length - 1 ? "gold-gradient" : "bg-gold-bright/25"}`}
                  style={{ height: `${Math.round((h / maxActivity) * 100)}%` }}
                />
                <div className="text-[9px] text-muted-foreground">{activity.labels[i]?.slice(0, 1) || i}</div>
              </div>
            )) : (
              <div className="flex-1 text-xs text-muted-foreground flex items-center justify-center">No data yet</div>
            )}
          </div>
        </div>

        <div className="bg-card border border-border rounded p-6">
          <div className="text-sm font-semibold text-foreground mb-4">Active Offers</div>
          {offers.filter((o) => o.active).slice(0, 3).map((o, i) => (
            <div key={i} className="py-2 border-b border-border/50 flex justify-between items-center">
              <div>
                <div className="text-xs font-medium text-foreground">{o.title}</div>
                <div className="text-[10px] text-muted-foreground">{o.reward_type.replace("_", " ")}</div>
              </div>
              <div className="text-xs font-semibold text-gold">{o.claims_count} claims</div>
            </div>
          ))}
          {offers.filter(o => o.active).length === 0 && (
            <div className="text-xs text-muted-foreground py-2">No active offers</div>
          )}
        </div>
      </div>

      {/* Recent Redemptions */}
      <div className="bg-card border border-border rounded p-6">
        <div className="text-sm font-semibold text-foreground mb-4">Recent Redemptions</div>
        {redemptions.length > 0 ? redemptions.map((r) => (
          <div key={r.id} className="flex items-center gap-3.5 py-2.5 border-b border-border/50 hover:bg-background/50 transition-colors">
            <div className="w-8 h-8 rounded bg-gold-bright/15 flex items-center justify-center text-sm shrink-0">🎟️</div>
            <div className="flex-1">
              <div className="text-xs font-medium text-foreground">{r.user} — {r.offer}</div>
              <div className="text-xs text-muted-foreground font-mono">{r.reward} · {r.date}</div>
            </div>
            <RS3Badge variant={r.status === "fulfilled" ? "green" : "amber"}>{r.status}</RS3Badge>
          </div>
        )) : (
          <div className="text-xs text-muted-foreground py-2">No redemptions yet</div>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
