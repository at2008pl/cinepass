import { useState, useEffect } from "react";
import type { Redemption } from "@/data/mockData";
import { getRedemptions, fulfillRedemption } from "@/services/api";
import RS3Badge from "@/components/shared/RS3Badge";
import RS3Button from "@/components/shared/RS3Button";
import SectionTitle from "@/components/shared/SectionTitle";
import TableHeader from "@/components/shared/TableHeader";

const RedemptionsPage = () => {
  const [items, setItems] = useState<Redemption[]>([]);
  const [filter, setFilter] = useState("all");

  useEffect(() => {
    getRedemptions().then(setItems).catch(console.error);
  }, []);

  const handleFulfill = async (id: number) => {
    try {
      await fulfillRedemption(id);
      setItems((it) => it.map((x) => (x.id === id ? { ...x, status: "fulfilled" as const } : x)));
    } catch (e) { console.error(e); }
  };

  const filtered = filter === "all" ? items : items.filter((r) => r.status === filter);
  const cols = [
    { label: "User", w: "1.5fr" }, { label: "Offer", w: "1.5fr" }, { label: "Reward Code", w: "1.5fr" },
    { label: "Status", w: "1fr" }, { label: "Date", w: "0.8fr" }, { label: "", w: "100px" },
  ];

  return (
    <div className="fade-in">
      <SectionTitle sub="Track all user redemptions. Mark as fulfilled when ticket/pass is delivered.">Redemptions</SectionTitle>

      <div className="flex gap-2 mb-5">
        {["all", "claimed", "fulfilled"].map((f) => (
          <button key={f} onClick={() => setFilter(f)}
            className={`px-4 py-2 text-xs font-medium tracking-wide capitalize bg-transparent border-none cursor-pointer transition-colors
              ${filter === f ? "text-gold border-b-2 border-b-gold" : "text-muted-foreground border-b-2 border-b-transparent"}`}>
            {f}
            <span className={`ml-1.5 text-[10px] px-1.5 rounded-full ${filter === f ? "bg-gold/10 text-gold" : "bg-muted text-muted-foreground"}`}>
              {f === "all" ? items.length : items.filter((r) => r.status === f).length}
            </span>
          </button>
        ))}
      </div>

      <div className="bg-card border border-border rounded overflow-hidden">
        <TableHeader cols={cols} />
        {filtered.map((r) => (
          <div key={r.id} className="hover:bg-background/50 transition-colors border-b border-border/50 items-center px-5 py-3"
            style={{ display: "grid", gridTemplateColumns: cols.map((c) => c.w).join(" ") }}>
            <div className="text-sm font-medium text-foreground">{r.user}</div>
            <div className="text-xs text-ink-light">{r.offer}</div>
            <div className="text-xs text-rs3-blue font-mono bg-rs3-blue-pale px-2 py-0.5 rounded inline-block w-fit">{r.reward}</div>
            <RS3Badge variant={r.status === "fulfilled" ? "green" : "amber"}>{r.status}</RS3Badge>
            <div className="text-xs text-muted-foreground">{r.date}</div>
            {r.status === "claimed" ? (
              <RS3Button small variant="ghost" onClick={() => handleFulfill(r.id)}>
                Mark Fulfilled
              </RS3Button>
            ) : (
              <span className="text-xs text-rs3-green">✓ Done</span>
            )}
          </div>
        ))}
      </div>
    </div>
  );
};

export default RedemptionsPage;
