import { NAV_ITEMS } from "@/data/mockData";

interface SidebarProps {
  active: string;
  onChange: (id: string) => void;
}

const Sidebar = ({ active, onChange }: SidebarProps) => (
  <div className="w-[230px] min-h-screen bg-sidebar flex flex-col sticky top-0 shrink-0">
    <div className="px-6 pt-8 pb-6 border-b border-sidebar-border">
      <div className="font-display text-lg tracking-[4px] gold-text leading-none">RS³ FILMS</div>
      <div className="text-[9px] text-sidebar-foreground/40 tracking-[3px] mt-1.5 uppercase">Admin Console v2</div>
    </div>
    <nav className="p-2.5 flex-1">
      {NAV_ITEMS.map((n) => (
        <div
          key={n.id}
          onClick={() => onChange(n.id)}
          className={`flex items-center gap-2.5 px-3 py-2.5 rounded cursor-pointer mb-px transition-colors
            ${active === n.id ? "bg-sidebar-accent" : "hover:bg-sidebar-accent/50"}`}
        >
          <span className={`text-sm shrink-0 ${active === n.id ? "text-sidebar-accent-foreground" : "text-sidebar-foreground"}`}>
            {n.icon}
          </span>
          <span className={`text-xs tracking-wide ${active === n.id ? "font-medium text-sidebar-accent-foreground" : "text-sidebar-foreground"}`}>
            {n.label}
          </span>
          {active === n.id && (
            <div className="ml-auto w-1 h-1 rounded-full bg-gold-bright" />
          )}
        </div>
      ))}
    </nav>
    <div className="px-6 py-4 border-t border-sidebar-border">
      <div className="text-[9px] text-sidebar-foreground/40 tracking-wide uppercase mb-0.5">Signed in as</div>
      <div className="text-xs text-sidebar-foreground font-medium">Priya Anand</div>
      <div className="text-[10px] text-sidebar-foreground/40">Super Admin</div>
    </div>
  </div>
);

export default Sidebar;
