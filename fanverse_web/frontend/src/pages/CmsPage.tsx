import { useState, useEffect } from "react";
import type { CmsContent } from "@/data/mockData";
import { getCms, updateCmsSection } from "@/services/api";
import RS3Badge from "@/components/shared/RS3Badge";
import RS3Button from "@/components/shared/RS3Button";
import SectionTitle from "@/components/shared/SectionTitle";
import ToggleSwitch from "@/components/shared/ToggleSwitch";

const sections = [
  { id: "splash", label: "Splash Screen", icon: "🎬" },
  { id: "onboarding", label: "Onboarding", icon: "👋" },
  { id: "legal", label: "Legal Pages", icon: "📄" },
  { id: "contact", label: "Contact & Social", icon: "📞" },
  { id: "wallet", label: "Wallet & Referral", icon: "🪙" },
  { id: "system", label: "System Config", icon: "⚙️" },
];

const CmsPage = () => {
  const [content, setContent] = useState<CmsContent>({} as CmsContent);
  const [activeSection, setActiveSection] = useState("splash");
  const [saved, setSaved] = useState<Record<string, boolean>>({});

  useEffect(() => {
    getCms().then(setContent).catch(console.error);
  }, []);

  const updateKey = (sectionId: string, key: string, value: string) => {
    setContent((c) => ({
      ...c,
      [sectionId]: c[sectionId].map((item) => (item.key === key ? { ...item, value } : item)),
    }));
  };

  const saveSection = async (sectionId: string) => {
    try {
      const sectionItems = content[sectionId] || [];
      await updateCmsSection(sectionId, sectionItems.map((i) => ({ key: i.key, value: i.value })));
      setSaved((s) => ({ ...s, [sectionId]: true }));
      setTimeout(() => setSaved((s) => ({ ...s, [sectionId]: false })), 2000);
    } catch (e) { console.error(e); }
  };

  const items = content[activeSection] || [];

  const typeVariant = (t: string) =>
    t === "image_url" ? "blue" : t === "richtext" ? "purple" : t === "boolean" ? "green" : t === "number" ? "amber" : "muted";

  return (
    <div className="fade-in">
      <SectionTitle sub="Every piece of text and image in the app is controlled here. Changes go live on next user app launch.">
        App Content — CMS
      </SectionTitle>

      <div className="grid grid-cols-[200px_1fr] gap-5">
        {/* Section nav */}
        <div className="bg-card border border-border rounded p-2 self-start">
          {sections.map((s) => (
            <div key={s.id} onClick={() => setActiveSection(s.id)}
              className={`flex items-center gap-2.5 px-3 py-2.5 rounded cursor-pointer mb-0.5 transition-all
                ${activeSection === s.id ? "bg-gold-pale border-l-2 border-l-gold" : "border-l-2 border-l-transparent hover:bg-secondary"}`}>
              <span className={`text-sm ${activeSection === s.id ? "text-gold" : "text-muted-foreground"}`}>{s.icon}</span>
              <span className={`text-xs ${activeSection === s.id ? "font-medium text-gold" : "text-ink-light"}`}>{s.label}</span>
            </div>
          ))}
        </div>

        {/* Editor */}
        <div className="bg-card border border-border rounded overflow-hidden">
          <div className="px-6 py-4 border-b border-border flex justify-between items-center">
            <div>
              <div className="text-sm font-semibold text-foreground">{sections.find((s) => s.id === activeSection)?.label}</div>
              <div className="text-xs text-muted-foreground mt-0.5">{items.length} configurable items</div>
            </div>
            <RS3Button small onClick={() => saveSection(activeSection)}
              className={saved[activeSection] ? "!bg-rs3-green !text-popover" : ""}>
              {saved[activeSection] ? "✓ Saved" : "Save Changes"}
            </RS3Button>
          </div>

          <div className="p-6">
            {items.map((item) => (
              <div key={item.key} className="mb-6 pb-6 border-b border-border/50">
                <div className="flex justify-between items-start mb-2">
                  <div>
                    <div className="text-sm font-semibold text-foreground mb-0.5">{item.label}</div>
                    <div className="text-[10px] text-muted-foreground font-mono">{item.key}</div>
                  </div>
                  <RS3Badge variant={typeVariant(item.type) as any} small>{item.type}</RS3Badge>
                </div>

                {item.type === "image_url" && (
                  <div>
                    {item.value && (
                      <div className="w-full h-40 rounded overflow-hidden mb-2.5 border border-border">
                        <img src={item.value} alt={item.label} className="w-full h-full object-cover" onError={(e) => { (e.target as HTMLImageElement).style.display = "none"; }} />
                      </div>
                    )}
                    <div className="flex gap-2.5">
                      <input value={item.value} onChange={(e) => updateKey(activeSection, item.key, e.target.value)}
                        placeholder="Cloudinary URL"
                        className="flex-1 px-3 py-2 border border-border rounded text-xs text-foreground font-mono bg-popover focus:outline-none focus:border-primary" />
                      <div className="border border-border rounded px-3.5 py-2 cursor-pointer text-xs text-ink-light whitespace-nowrap bg-background">Upload Image</div>
                    </div>
                  </div>
                )}

                {item.type === "richtext" && (
                  <div>
                    <textarea value={item.value} rows={6} onChange={(e) => updateKey(activeSection, item.key, e.target.value)}
                      className="w-full px-3.5 py-2.5 border border-border rounded text-xs text-foreground resize-y leading-relaxed bg-popover focus:outline-none focus:border-primary" />
                    <div className="text-[10px] text-muted-foreground mt-1">HTML supported. Rendered in app using a rich text component.</div>
                  </div>
                )}

                {item.type === "boolean" && (
                  <div className="flex items-center gap-3.5 p-3 px-4 bg-background rounded border border-border">
                    <ToggleSwitch on={item.value === "true"} onChange={(v) => updateKey(activeSection, item.key, v ? "true" : "false")} />
                    <span className={`text-sm font-medium ${item.value === "true" ? "text-rs3-green" : "text-muted-foreground"}`}>
                      {item.value === "true" ? "Enabled" : "Disabled"}
                    </span>
                    {item.key === "maintenance_mode" && item.value === "true" && (
                      <span className="text-xs text-rs3-red font-semibold bg-rs3-red-pale px-2.5 py-0.5 rounded">⚠ App is in maintenance mode</span>
                    )}
                  </div>
                )}

                {(item.type === "text" || item.type === "url" || item.type === "number") && (
                  <input value={item.value} type={item.type === "number" ? "number" : "text"}
                    onChange={(e) => updateKey(activeSection, item.key, e.target.value)}
                    className="w-full px-3.5 py-2.5 border border-border rounded text-sm text-foreground bg-popover focus:outline-none focus:border-primary" />
                )}
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CmsPage;
