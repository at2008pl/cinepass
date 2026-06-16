import { useState, useEffect } from "react";
import { getMembers } from "@/services/api";
import type { Member } from "@/data/mockData";
import RS3Badge from "@/components/shared/RS3Badge";
import SectionTitle from "@/components/shared/SectionTitle";
import TableHeader from "@/components/shared/TableHeader";

const MembersPage = () => {
  const [members, setMembers] = useState<Member[]>([]);
  const [search, setSearch] = useState("");

  useEffect(() => {
    getMembers({ search: search || undefined }).then(r => setMembers(r.data)).catch(console.error);
  }, [search]);

  const filtered = members;

  const cols = [
    { label: "Member", w: "2fr" }, { label: "Contact", w: "2fr" }, { label: "Coins", w: "0.8fr" },
    { label: "Referrals", w: "0.8fr" }, { label: "Status", w: "0.8fr" }, { label: "Joined", w: "0.7fr" },
  ];

  const statusVariant = (s: string) => s === "ambassador" ? "gold" : s === "verified" ? "green" : "muted";

  return (
    <div className="fade-in">
      <SectionTitle action={
        <input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search members..."
          className="px-3.5 py-2 border border-border rounded text-sm w-60 text-foreground bg-popover focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10" />
      }>Members</SectionTitle>

      <div className="bg-card border border-border rounded overflow-hidden">
        <TableHeader cols={cols} />
        {filtered.map((u) => (
          <div key={u.id} className="hover:bg-background/50 transition-colors border-b border-border/50 items-center px-5 py-3"
            style={{ display: "grid", gridTemplateColumns: cols.map((c) => c.w).join(" ") }}>
            <div className="flex items-center gap-2.5">
              <div className="w-[30px] h-[30px] rounded-full bg-gold-bright/15 flex items-center justify-center text-xs font-bold text-gold shrink-0">
                {u.name[0]}
              </div>
              <div className="text-sm font-medium text-foreground">{u.name}</div>
            </div>
            <div>
              <div className="text-xs text-ink-light">{u.email}</div>
              <div className="text-xs text-muted-foreground">{u.phone}</div>
            </div>
            <div className="font-display text-base text-gold">{u.coins.toLocaleString()}</div>
            <div className="text-sm text-ink-light">{u.referrals}</div>
            <RS3Badge variant={statusVariant(u.status) as any}>{u.status}</RS3Badge>
            <div className="text-xs text-muted-foreground">{u.joined}</div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default MembersPage;
