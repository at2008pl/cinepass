import { useState, useEffect } from "react";
import type { PointsRule } from "@/data/mockData";
import { getPointsRules, createRule, updateRule, deleteRule, toggleRule } from "@/services/api";
import RS3Button from "@/components/shared/RS3Button";
import SectionTitle from "@/components/shared/SectionTitle";
import ToggleSwitch from "@/components/shared/ToggleSwitch";
import Modal from "@/components/shared/Modal";
import InputField from "@/components/shared/InputField";
import SelectField from "@/components/shared/SelectField";

const PointsPage = () => {
  const [rules, setRules] = useState<PointsRule[]>([]);
  const [editId, setEditId] = useState<number | null>(null);
  const [modal, setModal] = useState(false);
  const blank = { rule_key: "", label: "", coins: 50, rule_type: "referral_level" as const, level_number: "" as any, active: true, description: "" };
  const [form, setForm] = useState<any>(blank);
  const sf = (k: string, v: any) => setForm((f: any) => ({ ...f, [k]: v }));

  useEffect(() => {
    getPointsRules().then(setRules).catch(console.error);
  }, []);

  const chainRules = rules.filter((r) => r.rule_type === "referral_level").sort((a, b) => (a.level_number ?? 0) - (b.level_number ?? 0));
  const bonusRules = rules.filter((r) => r.rule_type !== "referral_level");
  const activeDepth = chainRules.filter((r) => r.active).length;

  const openNew = () => { setForm(blank); setEditId(null); setModal(true); };
  const openEdit = (r: PointsRule) => { setForm({ ...r, level_number: r.level_number ?? "" }); setEditId(r.id); setModal(true); };
  const save = async () => {
    const data = { ...form, coins: +form.coins, level_number: form.level_number !== "" ? +form.level_number : null };
    try {
      if (editId) {
        await updateRule(editId, data);
        setRules((rs) => rs.map((r) => (r.id === editId ? { ...r, ...data } : r)));
      } else {
        const created = await createRule(data);
        setRules((rs) => [...rs, created]);
      }
      setModal(false);
    } catch (e) { console.error(e); }
  };
  const quickEdit = async (id: number, coins: string) => {
    try {
      await updateRule(id, { coins: +coins });
      setRules((rs) => rs.map((r) => (r.id === id ? { ...r, coins: +coins } : r)));
    } catch (e) { console.error(e); }
  };
  const handleToggle = async (id: number, active: boolean) => {
    try {
      await toggleRule(id, active);
      setRules((rs) => rs.map((x) => (x.id === id ? { ...x, active } : x)));
    } catch (e) { console.error(e); }
  };
  const handleDelete = async (id: number) => {
    try {
      await deleteRule(id);
      setRules((rs) => rs.filter((x) => x.id !== id));
    } catch (e) { console.error(e); }
  };

  const RuleRow = ({ r }: { r: PointsRule }) => (
    <div className="grid hover:bg-background/50 transition-colors border-b border-border/50 items-center px-5 py-3.5"
      style={{ gridTemplateColumns: "1.8fr 2.2fr 160px 120px 80px" }}>
      <div>
        <div className={`text-sm font-medium ${r.active ? "text-foreground" : "text-muted-foreground"}`}>{r.label}</div>
        <div className="text-[10px] text-muted-foreground mt-0.5 font-mono">{r.rule_key}</div>
      </div>
      <div className="text-xs text-muted-foreground pr-3">{r.description}</div>
      <div>
        <input type="number" defaultValue={r.coins} key={`${r.id}-${r.coins}`}
          onBlur={(e) => quickEdit(r.id, e.target.value)}
          className={`w-20 px-2 py-1 border border-border rounded-sm font-display text-lg text-gold ${r.active ? "bg-card" : "bg-transparent"} focus:outline-none focus:border-primary`}
        />
        <span className="text-xs text-muted-foreground ml-1">coins</span>
      </div>
      <div className="flex gap-2 items-center">
        <ToggleSwitch on={r.active} onChange={() => handleToggle(r.id, !r.active)} />
        <button onClick={() => openEdit(r)} className="border border-border rounded-sm px-2 py-1 text-xs cursor-pointer bg-transparent text-ink-light hover:bg-secondary transition-colors">Edit</button>
      </div>
      <button onClick={() => handleDelete(r.id)}
        className="bg-transparent border-none cursor-pointer text-base text-muted-foreground px-1.5 py-1 rounded-sm hover:bg-rs3-red-pale hover:text-rs3-red transition-colors">×</button>
    </div>
  );

  return (
    <div className="fade-in">
      <SectionTitle action={<RS3Button onClick={openNew}>+ Add Rule</RS3Button>}
        sub="All values read from DB. Changes are live instantly. No app update required.">Points Rules</SectionTitle>

      {/* Chain depth */}
      <div className="bg-card border border-border rounded p-6 mb-5">
        <div className="flex justify-between items-center mb-4">
          <div className="text-sm font-semibold text-foreground">Referral Chain — Active Depth</div>
          <div className="flex items-center gap-2.5">
            <span className="font-display text-[28px] text-gold">{activeDepth}</span>
            <span className="text-xs text-muted-foreground">active level{activeDepth !== 1 ? "s" : ""}</span>
          </div>
        </div>
        <div className="flex items-center">
          {[...Array(Math.max(activeDepth + 1, 4))].map((_, i) => {
            const isUser = i === 0;
            const rule = chainRules[i - 1];
            const isActive = rule?.active;
            if (isUser) return (
              <div key={i} className="text-center">
                <div className="w-11 h-11 rounded-full border-2 border-gold bg-gold-pale flex items-center justify-center mx-auto mb-1.5 text-xs text-gold font-bold">You</div>
                <div className="text-[9px] text-muted-foreground">Referrer</div>
              </div>
            );
            return (
              <div key={i} className="flex items-center">
                <div className={`w-[60px] h-0.5 -mt-6 ${isActive ? "gold-gradient" : "bg-border"}`} />
                <div className="text-center">
                  <div className={`w-11 h-11 rounded-full border-2 flex items-center justify-center mx-auto mb-1.5 text-[10px] font-semibold
                    ${isActive ? "border-gold-bright bg-gold-bright/10 text-gold" : "border-border bg-background text-muted-foreground"}`}>L{i}</div>
                  <div className={`text-[9px] ${isActive ? "text-gold" : "text-muted-foreground"}`}>{isActive ? `+${rule.coins}` : "inactive"}</div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Chain rules */}
      <div className="text-xs font-semibold text-foreground mb-2.5 flex items-center gap-2">
        <span>Referral Level Rules</span>
        <span className="text-[10px] text-muted-foreground font-normal">— Edit coin value inline or toggle active/inactive</span>
      </div>
      <div className="bg-card border border-border rounded overflow-hidden mb-5">
        <div className="grid px-5 py-2.5 bg-background border-b border-border" style={{ gridTemplateColumns: "1.8fr 2.2fr 160px 120px 80px" }}>
          {["Rule", "Description", "Coins (click to edit)", "Toggle / Edit", ""].map((h) => (
            <div key={h} className="text-[10px] font-semibold tracking-[1.5px] text-muted-foreground uppercase">{h}</div>
          ))}
        </div>
        {chainRules.map((r) => <RuleRow key={r.id} r={r} />)}
      </div>

      {/* Bonus rules */}
      <div className="text-xs font-semibold text-foreground mb-2.5">Bonus Rules</div>
      <div className="bg-card border border-border rounded overflow-hidden">
        <div className="grid px-5 py-2.5 bg-background border-b border-border" style={{ gridTemplateColumns: "1.8fr 2.2fr 160px 120px 80px" }}>
          {["Rule", "Description", "Coins (click to edit)", "Toggle / Edit", ""].map((h) => (
            <div key={h} className="text-[10px] font-semibold tracking-[1.5px] text-muted-foreground uppercase">{h}</div>
          ))}
        </div>
        {bonusRules.map((r) => <RuleRow key={r.id} r={r} />)}
      </div>

      {modal && (
        <Modal title={editId ? "Edit Rule" : "Add New Rule"} onClose={() => setModal(false)}>
          <InputField label="Rule Key (unique identifier)" value={form.rule_key} onChange={(v) => sf("rule_key", v)} placeholder="e.g. referral_l4" mono hint="Snake_case. Used internally by server." />
          <InputField label="Label (shown in admin & app)" value={form.label} onChange={(v) => sf("label", v)} placeholder="e.g. Level 4 Referral Bonus" />
          <SelectField label="Rule Type" value={form.rule_type} onChange={(v) => sf("rule_type", v)}
            options={[{ value: "referral_level", label: "Referral Level" }, { value: "registration_bonus", label: "Registration Bonus" }, { value: "event_bonus", label: "Event Bonus" }]} />
          {form.rule_type === "referral_level" && (
            <InputField label="Level Number" type="number" value={String(form.level_number)} onChange={(v) => sf("level_number", v)} placeholder="e.g. 4" />
          )}
          <InputField label="Coins Awarded" type="number" value={String(form.coins)} onChange={(v) => sf("coins", +v)} placeholder="e.g. 50" />
          <InputField label="Description" value={form.description} onChange={(v) => sf("description", v)} placeholder="What triggers this rule?" rows={2} />
          <div className="flex items-center justify-between py-3 border-t border-border mt-1">
            <span className="text-xs text-ink-light">Active</span>
            <ToggleSwitch on={form.active} onChange={(v) => sf("active", v)} />
          </div>
          <div className="flex gap-2.5 justify-end mt-5">
            <RS3Button variant="ghost" onClick={() => setModal(false)}>Cancel</RS3Button>
            <RS3Button onClick={save}>{editId ? "Save Changes" : "Add Rule"}</RS3Button>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default PointsPage;
