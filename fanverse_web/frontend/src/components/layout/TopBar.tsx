import { NAV_ITEMS } from "@/data/mockData";

interface TopBarProps {
  activePageId: string;
  onLogout?: () => void;
}

const TopBar = ({ activePageId, onLogout }: TopBarProps) => (
  <div className="sticky top-0 z-10 bg-background/90 backdrop-blur-md border-b border-border px-8 py-3 flex items-center justify-between">
    <div className="text-xs text-muted-foreground tracking-wide">
      {NAV_ITEMS.find((n) => n.id === activePageId)?.label}
    </div>
    <div className="flex items-center gap-4">
      <div className="text-xs text-muted-foreground">
        {new Date().toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" })}
      </div>
      {onLogout && (
        <button onClick={onLogout} className="text-xs text-muted-foreground hover:text-foreground bg-transparent border border-border rounded px-2.5 py-1 cursor-pointer transition-colors">
          Sign out
        </button>
      )}
      <div className="w-7 h-7 rounded-full gold-gradient flex items-center justify-center text-xs font-bold text-foreground">
        A
      </div>
    </div>
  </div>
);

export default TopBar;
