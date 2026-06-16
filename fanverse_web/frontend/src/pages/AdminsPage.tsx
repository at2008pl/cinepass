import { useState, useEffect } from "react";
import type { Admin, AdminRole } from "@/data/mockData";
import { getAdmins, inviteAdmin } from "@/services/api";
import RS3Badge from "@/components/shared/RS3Badge";
import RS3Button from "@/components/shared/RS3Button";
import SectionTitle from "@/components/shared/SectionTitle";
import TableHeader from "@/components/shared/TableHeader";
import Modal from "@/components/shared/Modal";
import InputField from "@/components/shared/InputField";
import SelectField from "@/components/shared/SelectField";

const ROLE_INFO: Record<AdminRole, { variant: "gold" | "blue" | "green" | "amber"; perms: string }> = {
  super_admin: { variant: "gold", perms: "Full access — all modules" },
  content: { variant: "blue", perms: "Feed + Offers + CMS" },
  analytics: { variant: "green", perms: "Read-only analytics" },
  moderator: { variant: "amber", perms: "Members management" },
};

const AdminsPage = () => {
  const [admins, setAdmins] = useState<Admin[]>([]);
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState({ name: "", email: "", role: "content" as AdminRole });

  useEffect(() => {
    getAdmins().then(setAdmins).catch(console.error);
  }, []);

  const add = async () => {
    if (!form.name || !form.email) return;
    try {
      const created = await inviteAdmin(form);
      setAdmins((a) => [...a, created]);
      setModal(false);
      setForm({ name: "", email: "", role: "content" });
    } catch (e) { console.error(e); }
  };

  return (
    <div className="fade-in">
      <SectionTitle action={<RS3Button onClick={() => setModal(true)}>+ Invite Admin</RS3Button>}>Admin Access</SectionTitle>

      {/* Role cards */}
      <div className="grid grid-cols-4 gap-3 mb-6">
        {(Object.entries(ROLE_INFO) as [AdminRole, typeof ROLE_INFO[AdminRole]][]).map(([role, info]) => (
          <div key={role} className="bg-card border border-border rounded p-4"
            style={{ borderTopWidth: 3, borderTopColor: `hsl(var(--${role === "super_admin" ? "gold" : role === "content" ? "rs3-blue" : role === "analytics" ? "rs3-green" : "rs3-amber"}))` }}>
            <div className={`text-xs font-semibold capitalize mb-0.5 text-${role === "super_admin" ? "gold" : role === "content" ? "rs3-blue" : role === "analytics" ? "rs3-green" : "rs3-amber"}`}>
              {role.replace("_", " ")}
            </div>
            <div className="text-xs text-muted-foreground">{info.perms}</div>
            <div className="text-xs font-semibold text-foreground mt-1.5">
              {admins.filter((a) => a.role === role).length} admin{admins.filter((a) => a.role === role).length !== 1 ? "s" : ""}
            </div>
          </div>
        ))}
      </div>

      <div className="bg-card border border-border rounded overflow-hidden">
        <TableHeader cols={[{ label: "Name", w: "1.5fr" }, { label: "Email", w: "2fr" }, { label: "Role", w: "1fr" }, { label: "Status", w: "0.8fr" }, { label: "Last Active", w: "1fr" }]} />
        {admins.map((a) => (
          <div key={a.id} className="hover:bg-background/50 transition-colors border-b border-border/50 items-center px-5 py-3"
            style={{ display: "grid", gridTemplateColumns: "1.5fr 2fr 1fr 0.8fr 1fr" }}>
            <div className="flex items-center gap-2.5">
              <div className="w-7 h-7 rounded-full bg-gold-bright/15 flex items-center justify-center text-[10px] font-bold text-gold shrink-0">{a.name[0]}</div>
              <div className="text-sm font-medium text-foreground">{a.name}</div>
            </div>
            <div className="text-xs text-ink-light">{a.email}</div>
            <RS3Badge variant={ROLE_INFO[a.role]?.variant || "muted"}>{a.role.replace("_", " ")}</RS3Badge>
            <RS3Badge variant={a.active ? "green" : "muted"}>{a.active ? "Active" : "Inactive"}</RS3Badge>
            <div className="text-xs text-muted-foreground">{a.lastLogin}</div>
          </div>
        ))}
      </div>

      {modal && (
        <Modal title="Invite Admin" onClose={() => setModal(false)}>
          <InputField label="Full Name" value={form.name} onChange={(v) => setForm((f) => ({ ...f, name: v }))} placeholder="Admin's name" />
          <InputField label="Email Address" type="email" value={form.email} onChange={(v) => setForm((f) => ({ ...f, email: v }))} placeholder="admin@rs3films.com" />
          <SelectField label="Role" value={form.role} onChange={(v) => setForm((f) => ({ ...f, role: v as AdminRole }))}
            options={Object.entries(ROLE_INFO).map(([v, i]) => ({ value: v, label: `${v.replace("_", " ")} — ${i.perms}` }))} />
          <div className="text-xs text-muted-foreground py-2.5 border-t border-border mt-1 leading-relaxed">
            An invitation email will be sent with a temporary password. Super Admins can change roles at any time.
          </div>
          <div className="flex gap-2.5 justify-end">
            <RS3Button variant="ghost" onClick={() => setModal(false)}>Cancel</RS3Button>
            <RS3Button onClick={add}>Send Invite</RS3Button>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default AdminsPage;
