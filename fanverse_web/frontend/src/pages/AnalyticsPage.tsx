import { useState, useEffect } from "react";
import SectionTitle from "@/components/shared/SectionTitle";
import { getAnalyticsOverview, getAnalyticsGrowth } from "@/services/api";

const AnalyticsPage = () => {
  const [overview, setOverview] = useState({ conversion_rate: 0, avg_coins_per_member: 0, total_offers_claimed: 0, avg_chain_depth: 0, churn_30d: 0 });
  const [growth, setGrowthData] = useState<{ labels: string[]; members: number[]; referrals: number[]; coins: number[]; offers: number[] }>({
    labels: [], members: [], referrals: [], coins: [], offers: [],
  });

  useEffect(() => {
    getAnalyticsOverview().then(setOverview).catch(console.error);
    getAnalyticsGrowth(7).then(setGrowthData).catch(console.error);
  }, []);

  const months = growth.labels;
  const growthMembers = growth.members;
  const refs = growth.referrals;
  const coins = growth.coins;
  const offers = growth.offers;
  const maxG = growthMembers.length ? Math.max(...growthMembers) : 1;

  return (
    <div className="fade-in">
      <SectionTitle>Analytics</SectionTitle>

      {/* KPI cards */}
      <div className="grid grid-cols-5 gap-3 mb-6">
        {[
          { label: "Conversion Rate", value: `${overview.conversion_rate}%`, sub: "Referral → Register", color: "gold" },
          { label: "Avg Coins/Member", value: `${overview.avg_coins_per_member}`, sub: "Per active member", color: "rs3-amber" },
          { label: "Offers Claimed", value: `${overview.total_offers_claimed}`, sub: "All time", color: "rs3-green" },
          { label: "Chain Depth Avg", value: `${overview.avg_chain_depth}`, sub: "Levels per referrer", color: "rs3-blue" },
          { label: "Churn (30d)", value: `${overview.churn_30d}%`, sub: "Inactive members", color: "rs3-red" },
        ].map((k, i) => (
          <div key={i} className="bg-card border border-border rounded p-4">
            <div className="text-[9px] font-semibold tracking-[2px] text-muted-foreground uppercase mb-2">{k.label}</div>
            <div className={`font-display text-2xl text-${k.color} mb-0.5`}>{k.value}</div>
            <div className="text-[10px] text-muted-foreground">{k.sub}</div>
          </div>
        ))}
      </div>

      {/* Charts row 1 */}
      <div className="grid grid-cols-2 gap-4 mb-4">
        <div className="bg-card border border-border rounded p-6">
          <div className="text-sm font-semibold text-foreground mb-1">Member Growth</div>
          <div className="text-xs text-muted-foreground mb-4">Cumulative registrations</div>
          <div className="flex items-end gap-2.5 h-[100px]">
            {growthMembers.map((v, i) => (
              <div key={i} className="flex-1 flex flex-col items-center gap-1">
                <div className="text-[9px] text-gold font-semibold">{v > 999 ? `${(v / 1000).toFixed(1)}k` : v}</div>
                <div className={`w-full rounded-t-sm ${i === growthMembers.length - 1 ? "gold-gradient" : "bg-gold-bright/20"}`}
                  style={{ height: `${(v / maxG) * 90}px` }} />
                <div className="text-[9px] text-muted-foreground">{months[i]}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-card border border-border rounded p-6">
          <div className="text-sm font-semibold text-foreground mb-1">Referral Conversion Rate</div>
          <div className="text-xs text-muted-foreground mb-4">% of registrations via referral link</div>
          {months.map((m, i) => {
            const pct = growthMembers[i] ? Math.round((refs[i] / growthMembers[i]) * 100) : 0;
            return (
              <div key={i} className="flex items-center gap-2.5 mb-1.5">
                <div className="w-7 text-[10px] text-muted-foreground">{m}</div>
                <div className="flex-1 h-2 bg-border/50 rounded overflow-hidden">
                  <div className="h-full gold-gradient rounded" style={{ width: `${pct}%` }} />
                </div>
                <div className="w-8 text-[10px] font-semibold text-gold">{pct}%</div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Charts row 2 */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-card border border-border rounded p-6">
          <div className="text-sm font-semibold text-foreground mb-1">Coin Issuance</div>
          <div className="text-xs text-muted-foreground mb-4">Total coins distributed per month</div>
          <div className="flex items-end gap-2.5 h-20">
            {coins.map((v, i) => (
              <div key={i} className="flex-1 flex flex-col items-center gap-1">
                <div className={`w-full rounded-t-sm ${i === coins.length - 1 ? "gold-gradient" : "bg-gold-bright/25"}`}
                  style={{ height: `${(v / (Math.max(...coins, 1))) * 76}px` }} />
                <div className="text-[9px] text-muted-foreground">{months[i]}</div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-card border border-border rounded p-6">
          <div className="text-sm font-semibold text-foreground mb-1">Offer Claims Over Time</div>
          <div className="text-xs text-muted-foreground mb-4">Cumulative redemptions</div>
          <div className="flex items-end gap-2.5 h-20">
            {offers.map((v, i) => (
              <div key={i} className="flex-1 flex flex-col items-center gap-1">
                <div className={`w-full rounded-t-sm ${i === offers.length - 1 ? "bg-rs3-green" : "bg-rs3-green/25"}`}
                  style={{ height: `${Math.max((v / (Math.max(...offers, 1))) * 76, 2)}px` }} />
                <div className="text-[9px] text-muted-foreground">{months[i]}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default AnalyticsPage;
