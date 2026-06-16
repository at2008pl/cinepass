import { useState, useEffect } from "react";
import { REWARD_TYPES } from "@/data/mockData";
import type { Offer } from "@/data/mockData";
import { getOffers, createOffer, updateOffer, deleteOffer, toggleOffer } from "@/services/api";
import RS3Badge from "@/components/shared/RS3Badge";
import RS3Button from "@/components/shared/RS3Button";
import SectionTitle from "@/components/shared/SectionTitle";
import TableHeader from "@/components/shared/TableHeader";
import ToggleSwitch from "@/components/shared/ToggleSwitch";
import Modal from "@/components/shared/Modal";
import InputField from "@/components/shared/InputField";
import SelectField from "@/components/shared/SelectField";

const OffersPage = () => {
  const [offers, setOffers] = useState<Offer[]>([]);
  const [modal, setModal] = useState(false);
  const [editId, setEditId] = useState<number | null>(null);
  const blank = { title: "", description: "", reward_type: "movie_ticket" as const, reward_value: "", coin_cost: 0, target_referrals: "", page: "referral" as const, active: true, max_claims: "", valid_until: "" };
  const [form, setForm] = useState<any>(blank);
  const sf = (k: string, v: any) => setForm((f: any) => ({ ...f, [k]: v }));

  useEffect(() => {
    getOffers().then(setOffers).catch(console.error);
  }, []);

  const triggerType = form.target_referrals ? "milestone" : form.coin_cost > 0 ? "coin_cost" : "free";

  const openEdit = (o: Offer) => {
    setForm({ ...o, target_referrals: o.target_referrals ?? "", max_claims: o.max_claims ?? "", valid_until: "" });
    setEditId(o.id);
    setModal(true);
  };

  const save = async () => {
    const data = { ...form, coin_cost: +form.coin_cost, target_referrals: form.target_referrals ? +form.target_referrals : null, max_claims: form.max_claims ? +form.max_claims : null, claims_count: form.claims_count ?? 0 };
    try {
      if (editId) {
        const updated = await updateOffer(editId, data) as Offer;
        setOffers((os) => os.map((o) => (o.id === editId ? updated : o)));
      } else {
        const created = await createOffer(data) as Offer;
        setOffers((os) => [created, ...os]);
      }
    } catch (e) { console.error(e); }
    setModal(false);
    setEditId(null);
    setForm(blank);
  };

  const handleToggle = async (o: Offer) => {
    try {
      await toggleOffer(o.id, !o.active);
      setOffers((os) => os.map((x) => (x.id === o.id ? { ...x, active: !x.active } : x)));
    } catch (e) { console.error(e); }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteOffer(id);
      setOffers((os) => os.filter((x) => x.id !== id));
    } catch (e) { console.error(e); }
  };

  const cols = [
    { label: "Offer", w: "2fr" }, { label: "Type", w: "1fr" }, { label: "Trigger", w: "1fr" },
    { label: "Page", w: "0.7fr" }, { label: "Claims", w: "0.8fr" }, { label: "Active", w: "60px" }, { label: "", w: "100px" },
  ];

  const rewardVariant = (t: string) => t === "movie_ticket" ? "blue" : t === "event_pass" ? "purple" : t === "coupon_code" ? "green" : "amber";

  return (
    <div className="fade-in">
      <SectionTitle action={<RS3Button onClick={() => { setForm(blank); setEditId(null); setModal(true); }}>+ Create Offer</RS3Button>}
        sub="Movie tickets, event passes & coupon codes. No bank or cash payouts.">Offers</SectionTitle>

      <div className="bg-gold-pale border border-gold-line border-l-[3px] border-l-gold rounded p-3 px-4 mb-6">
        <div className="text-xs font-semibold text-gold mb-0.5">Visibility Rule</div>
        <div className="text-xs text-ink-light leading-relaxed">
          If no active offers exist for a page, that section is automatically hidden in the app. The app renders exactly what the server returns — if the offers array is empty, the section is not shown.
        </div>
      </div>

      <div className="bg-card border border-border rounded overflow-hidden">
        <TableHeader cols={cols} />
        {offers.map((o) => (
          <div key={o.id} className="hover:bg-background/50 transition-colors border-b border-border/50 items-center px-5 py-3"
            style={{ display: "grid", gridTemplateColumns: cols.map((c) => c.w).join(" ") }}>
            <div>
              <div className={`text-sm font-medium ${o.active ? "text-foreground" : "text-muted-foreground"}`}>{o.title}</div>
              <div className="text-xs text-muted-foreground font-mono mt-0.5">{o.reward_value}</div>
            </div>
            <RS3Badge variant={rewardVariant(o.reward_type) as any}>
              {REWARD_TYPES.find((r) => r.value === o.reward_type)?.label.slice(3)}
            </RS3Badge>
            <div className="text-xs text-ink-light">
              {o.target_referrals ? `${o.target_referrals} referrals` : o.coin_cost > 0 ? `${o.coin_cost} coins` : "Free"}
            </div>
            <RS3Badge variant={o.page === "referral" ? "gold" : "blue"}>{o.page}</RS3Badge>
            <div className="text-xs text-ink-light">{o.claims_count}{o.max_claims ? `/${o.max_claims}` : ""}</div>
            <ToggleSwitch on={o.active} onChange={() => handleToggle(o)} />
            <div className="flex gap-1.5">
              <button onClick={() => openEdit(o)} className="bg-transparent border border-border rounded-sm px-2.5 py-1 text-xs cursor-pointer text-ink-light hover:bg-secondary transition-colors">Edit</button>
              <button onClick={() => handleDelete(o.id)}
                className="bg-transparent border-none cursor-pointer text-base text-muted-foreground px-1 rounded-sm hover:bg-rs3-red-pale hover:text-rs3-red transition-colors">×</button>
            </div>
          </div>
        ))}
      </div>

      {modal && (
        <Modal title={editId ? "Edit Offer" : "Create Offer"} onClose={() => setModal(false)} wide>
          <InputField label="Offer Title" value={form.title} onChange={(v) => sf("title", v)} placeholder="e.g. Free Movie Ticket on 5 Referrals" />
          <InputField label="Description (shown in app)" value={form.description || ""} onChange={(v) => sf("description", v)} placeholder="Full offer description" rows={2} />
          <SelectField label="Reward Type" value={form.reward_type} onChange={(v) => sf("reward_type", v)} options={REWARD_TYPES} />
          <InputField label="Reward Value" value={form.reward_value} onChange={(v) => sf("reward_value", v)}
            placeholder={form.reward_type === "coupon_code" ? "e.g. RS3MOVIE20" : "e.g. TICKET-REF-001"}
            hint="The actual coupon code, ticket reference, or pass code that gets delivered to the user on claim." mono />

          <div className="bg-background rounded p-4 mb-4">
            <div className="text-xs font-semibold text-ink-light mb-3">TRIGGER TYPE — how is this offer unlocked?</div>
            <div className="grid grid-cols-3 gap-2 mb-3">
              {([["milestone", "🏆 Referral Milestone"], ["coin_cost", "🪙 Spend Coins"], ["free", "🎁 Free / Admin Gift"]] as const).map(([k, l]) => (
                <div key={k} onClick={() => {
                  if (k === "milestone") sf("coin_cost", 0);
                  else if (k === "coin_cost") sf("target_referrals", "");
                  else { sf("coin_cost", 0); sf("target_referrals", ""); }
                }}
                  className={`border rounded p-2.5 text-center cursor-pointer transition-all ${triggerType === k ? "border-gold bg-gold-pale" : "border-border bg-card"}`}>
                  <div className={`text-xs font-semibold ${triggerType === k ? "text-gold" : "text-ink-light"}`}>{l}</div>
                </div>
              ))}
            </div>
            {triggerType === "milestone" && <InputField label="Target Referrals Required" type="number" value={form.target_referrals} onChange={(v) => sf("target_referrals", v)} placeholder="e.g. 5" />}
            {triggerType === "coin_cost" && <InputField label="Coin Cost" type="number" value={String(form.coin_cost)} onChange={(v) => sf("coin_cost", +v)} placeholder="e.g. 200" />}
          </div>

          <div className="grid grid-cols-2 gap-3.5">
            <SelectField label="Show on Page" value={form.page} onChange={(v) => sf("page", v)}
              options={[{ value: "referral", label: "Referral Page" }, { value: "wallet", label: "Wallet Page" }, { value: "home", label: "Home Feed" }, { value: "global", label: "All Pages" }]} />
            <InputField label="Max Claims (blank = unlimited)" type="number" value={String(form.max_claims || "")} onChange={(v) => sf("max_claims", v)} placeholder="e.g. 100" />
          </div>
          <InputField label="Valid Until (optional)" type="date" value={form.valid_until || ""} onChange={(v) => sf("valid_until", v)} />

          <div className="flex items-center justify-between py-3 border-t border-border mt-1">
            <span className="text-xs text-ink-light">Activate immediately</span>
            <ToggleSwitch on={form.active} onChange={(v) => sf("active", v)} />
          </div>
          <div className="flex gap-2.5 justify-end mt-5">
            <RS3Button variant="ghost" onClick={() => setModal(false)}>Cancel</RS3Button>
            <RS3Button onClick={save}>{editId ? "Save Changes" : "Create Offer"}</RS3Button>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default OffersPage;
