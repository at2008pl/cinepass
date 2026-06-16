import { useState } from "react";
import Sidebar from "@/components/layout/Sidebar";
import TopBar from "@/components/layout/TopBar";
import DashboardPage from "@/pages/DashboardPage";
import FeedPage from "@/pages/FeedPage";
import OffersPage from "@/pages/OffersPage";
import RedemptionsPage from "@/pages/RedemptionsPage";
import PointsPage from "@/pages/PointsPage";
import CmsPage from "@/pages/CmsPage";
import MembersPage from "@/pages/MembersPage";
import AdminsPage from "@/pages/AdminsPage";
import AnalyticsPage from "@/pages/AnalyticsPage";
import { login, getAuthToken, clearAuth } from "@/services/api";

// ── Admin Login Gate ───────────────────────────────────
const LoginGate = ({ onLogin }: { onLogin: () => void }) => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    try {
      await login(email, password);
      onLogin();
    } catch (err: any) {
      setError(err.message || "Login failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-background flex items-center justify-center">
      <div className="bg-card border border-border rounded-lg p-8 w-full max-w-sm shadow-lg">
        <div className="text-center mb-6">
          <div className="font-display text-2xl text-gold mb-1">RS³ Films</div>
          <div className="text-xs text-muted-foreground tracking-widest uppercase">Admin Dashboard</div>
        </div>
        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="text-xs font-semibold tracking-[1.5px] text-muted-foreground uppercase block mb-1.5">Email</label>
            <input
              type="email" value={email} onChange={e => setEmail(e.target.value)} required
              className="w-full px-3.5 py-2.5 border border-border rounded text-sm bg-popover text-foreground focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10"
              placeholder="admin@rs3films.com"
            />
          </div>
          <div>
            <label className="text-xs font-semibold tracking-[1.5px] text-muted-foreground uppercase block mb-1.5">Password</label>
            <input
              type="password" value={password} onChange={e => setPassword(e.target.value)} required
              className="w-full px-3.5 py-2.5 border border-border rounded text-sm bg-popover text-foreground focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10"
              placeholder="••••••••"
            />
          </div>
          {error && <div className="text-xs text-rs3-red bg-rs3-red-pale border border-rs3-red/20 rounded p-2.5">{error}</div>}
          <button
            type="submit" disabled={loading}
            className="w-full gold-gradient text-background font-semibold text-sm py-2.5 rounded cursor-pointer border-none disabled:opacity-60"
          >
            {loading ? "Signing in…" : "Sign In"}
          </button>
        </form>
      </div>
    </div>
  );
};

const Index = () => {
  const [page, setPage] = useState("dashboard");
  const [isAuthed, setIsAuthed] = useState(() => !!getAuthToken());

  if (!isAuthed) return <LoginGate onLogin={() => setIsAuthed(true)} />;

  const handleLogout = () => { clearAuth(); setIsAuthed(false); };

  const pages: Record<string, React.ReactNode> = {
    dashboard: <DashboardPage />,
    feed: <FeedPage />,
    offers: <OffersPage />,
    redemptions: <RedemptionsPage />,
    points: <PointsPage />,
    cms: <CmsPage />,
    members: <MembersPage />,
    admins: <AdminsPage />,
    analytics: <AnalyticsPage />,
  };

  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar active={page} onChange={setPage} />
      <div className="flex-1 overflow-auto">
        <TopBar activePageId={page} onLogout={handleLogout} />
        <div className="p-8 pb-16">{pages[page]}</div>
      </div>
    </div>
  );
};

export default Index;
