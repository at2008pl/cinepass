import { useState, useEffect, useRef } from "react";
import { FEED_LAYOUTS } from "@/data/mockData";
import type { FeedPost } from "@/data/mockData";
import { getFeed, createFeedPost, updateFeedPostStatus, deleteFeedPost, uploadMedia } from "@/services/api";
import RS3Badge from "@/components/shared/RS3Badge";
import RS3Button from "@/components/shared/RS3Button";
import SectionTitle from "@/components/shared/SectionTitle";
import TableHeader from "@/components/shared/TableHeader";
import Modal from "@/components/shared/Modal";
import InputField from "@/components/shared/InputField";
import SelectField from "@/components/shared/SelectField";

const BLANK = { layout: "hero", title: "", subtitle: "", type: "image", body: "", link: "", media_url: "", status: "live" };

const FeedPage = () => {
  const [posts, setPosts] = useState<FeedPost[]>([]);
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState({ ...BLANK });
  const [uploading, setUploading] = useState(false);
  const [preview, setPreview] = useState<string | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    getFeed().then(setPosts).catch(console.error);
  }, []);

  const sf = (k: string, v: string) => setForm((f) => ({ ...f, [k]: v }));

  const handleFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    setPreview(URL.createObjectURL(file));
    setUploading(true);
    try {
      const url = await uploadMedia(file, "feed");
      sf("media_url", url);
    } catch (err) {
      console.error(err);
    } finally {
      setUploading(false);
    }
  };

  const add = async () => {
    if (!form.title.trim()) return;
    try {
      const newPost = await createFeedPost(form) as FeedPost;
      setPosts((p) => [newPost, ...p]);
      setModal(false);
      setForm({ ...BLANK });
      setPreview(null);
    } catch (e) { console.error(e); }
  };

  const closeModal = () => { setModal(false); setForm({ ...BLANK }); setPreview(null); };

  const toggleStatus = async (p: FeedPost) => {
    const newStatus = p.status === "live" ? "draft" : "live";
    try {
      const updated = await updateFeedPostStatus(p.id, newStatus) as FeedPost;
      setPosts((ps) => ps.map((x) => x.id === p.id ? updated : x));
    } catch (e) { console.error(e); }
  };

  const removePost = async (id: number) => {
    try {
      await deleteFeedPost(id);
      setPosts((ps) => ps.filter((x) => x.id !== id));
    } catch (e) { console.error(e); }
  };

  const cols = [
    { label: "Post", w: "40px" }, { label: "", w: "1.6fr" }, { label: "Layout", w: "0.8fr" }, { label: "Type", w: "0.7fr" },
    { label: "Author", w: "0.7fr" }, { label: "Date", w: "0.7fr" }, { label: "Actions", w: "130px" },
  ];

  const typeIcons: Record<string, string> = { image: "🖼️", video: "🎬", link: "🔗", text: "📝" };
  const layoutColors: Record<string, string> = { hero: "bg-amber-100 text-amber-800", reel: "bg-purple-100 text-purple-800", banner: "bg-blue-100 text-blue-800", card: "bg-green-100 text-green-800", update: "bg-gray-100 text-gray-700", grid2: "bg-pink-100 text-pink-800" };

  return (
    <div className="fade-in">
      <SectionTitle action={<RS3Button onClick={() => setModal(true)}>+ New Post</RS3Button>}
        sub="Admin-controlled Instagram-style feed. Layout is fixed per post type.">
        Home Feed
      </SectionTitle>

      {/* Layout preview — compact chips */}
      <div className="flex flex-wrap gap-2 mb-6">
        {FEED_LAYOUTS.map((l) => (
          <div key={l.id} className="bg-card border border-border rounded px-3 py-1.5 flex items-center gap-1.5">
            <span className="text-sm">{l.icon}</span>
            <span className="text-[11px] font-semibold text-foreground">{l.label}</span>
            <span className="text-[10px] text-muted-foreground hidden sm:inline">— {l.desc}</span>
          </div>
        ))}
      </div>

      {/* Table */}
      <div className="bg-card border border-border rounded overflow-hidden">
        <TableHeader cols={cols} />
        {posts.length === 0 && (
          <div className="text-center py-10 text-muted-foreground text-sm">No posts yet — create your first post above.</div>
        )}
        {posts.map((p) => (
          <div key={p.id} className="hover:bg-background/50 transition-colors border-b border-border/50 items-center px-4 py-2.5"
            style={{ display: "grid", gridTemplateColumns: cols.map((c) => c.w).join(" "), alignItems: "center" }}>
            {/* Thumbnail */}
            <div className="w-10 h-10 rounded overflow-hidden bg-muted flex-shrink-0">
              {(p.thumbnail_url || p.media_url) ? (
                <img src={p.thumbnail_url || p.media_url} alt="" className="w-full h-full object-cover" />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-base">{typeIcons[p.type] || "📝"}</div>
              )}
            </div>
            {/* Title */}
            <div className="min-w-0 pl-2">
              <div className="text-sm font-medium text-foreground truncate">{p.title}</div>
              <div className="text-xs text-muted-foreground truncate">{p.subtitle}</div>
            </div>
            <div><span className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${layoutColors[p.layout] || "bg-muted text-muted-foreground"}`}>{p.layout}</span></div>
            <div className="text-xs text-ink-light">{typeIcons[p.type] || "📝"} {p.type}</div>
            <div className="text-xs text-ink-light">{p.author}</div>
            <div className="text-xs text-muted-foreground">{p.date}</div>
            <div className="flex gap-1.5 items-center">
              <RS3Badge variant={p.status === "live" ? "green" : p.status === "draft" ? "muted" : "amber"}>{p.status}</RS3Badge>
              <button
                onClick={() => toggleStatus(p)}
                className="border border-border rounded-sm px-2 py-0.5 text-[10px] cursor-pointer bg-transparent text-ink-light hover:bg-secondary transition-colors"
              >
                {p.status === "live" ? "Unpub" : "Pub"}
              </button>
              <button
                onClick={() => removePost(p.id)}
                className="bg-transparent border-none cursor-pointer text-base text-muted-foreground px-1 rounded-sm hover:bg-rs3-red-pale hover:text-rs3-red transition-colors"
              >
                ×
              </button>
            </div>
          </div>
        ))}
      </div>

      {modal && (
        <Modal title="Create Feed Post" onClose={closeModal} wide>
          {/* Row 1: Layout + Status side by side */}
          <div className="grid grid-cols-2 gap-3 mb-1">
            <SelectField label="Layout" value={form.layout} onChange={(v) => setForm((f) => ({
              ...f,
              layout: v,
              ...(v === "reel" ? { type: "video" } : {}),
            }))}
              options={FEED_LAYOUTS.map((l) => ({ value: l.id, label: `${l.icon} ${l.label}` }))} />
            <SelectField label="Content Type" value={form.type} onChange={(v) => setForm((f) => ({
              ...f,
              type: v,
              ...(v === "video" && f.layout === "hero" ? { layout: "reel" } : {}),
            }))}
              options={[{ value: "image", label: "🖼️ Image / Poster" }, { value: "video", label: "🎬 Video / Reel" }, { value: "text", label: "📝 Text Only" }, { value: "link", label: "🔗 External Link" }]} />
          </div>

          <InputField label="Title" value={form.title} onChange={(v) => sf("title", v)} placeholder="e.g. Premiere Night — Mar 15" />
          <InputField label="Subtitle / Tagline" value={form.subtitle} onChange={(v) => sf("subtitle", v)} placeholder="Supporting line (optional)" />
          <InputField label="Body" value={form.body} onChange={(v) => sf("body", v)} placeholder="Post content..." rows={2} />

          {/* Media upload */}
          {(form.type === "image" || form.type === "video") && (
            <div className="mb-4">
              <div className="text-[10px] font-semibold tracking-[1.5px] text-muted-foreground uppercase mb-2">
                {form.type === "image" ? "Image / Poster" : "Video"}
              </div>
              <input ref={fileRef} type="file" accept={form.type === "image" ? "image/*" : "video/*"} className="hidden" onChange={handleFile} />
              <div
                onClick={() => fileRef.current?.click()}
                className="border-2 border-dashed border-border rounded p-4 text-center bg-background cursor-pointer hover:border-gold/50 transition-colors"
              >
                {preview ? (
                  form.type === "image"
                    ? <img src={preview} alt="preview" className="max-h-32 mx-auto rounded object-cover" />
                    : <div className="text-xs text-gold">✓ Video selected — click to change</div>
                ) : (
                  <>
                    <div className="text-xl mb-1">{form.type === "image" ? "🖼️" : "🎬"}</div>
                    <div className="text-xs font-medium text-ink-light">Click to upload {form.type}</div>
                    <div className="text-[10px] text-muted-foreground mt-0.5">Max 50 MB</div>
                  </>
                )}
                {uploading && <div className="text-xs text-gold mt-1 animate-pulse">Uploading…</div>}
              </div>
              {form.media_url && !uploading && (
                <div className="mt-1.5 flex items-center gap-1.5">
                  <span className="text-[10px] text-rs3-green">✓ Uploaded</span>
                  <span className="text-[10px] text-muted-foreground font-mono truncate">{form.media_url}</span>
                </div>
              )}
              {/* Fallback: paste URL */}
              <InputField label="Or paste URL directly" value={form.media_url} onChange={(v) => { sf("media_url", v); if (v) setPreview(v); }} placeholder="https://..." />
            </div>
          )}

          {/* Link field — always shown for reel/hero/banner (YouTube), or when type=link */}
          {(form.type === "link" || form.layout === "reel" || form.layout === "hero" || form.layout === "banner") && (
            <InputField
              label={form.type === "link" ? "Link URL" : "YouTube / External Link (optional)"}
              value={form.link}
              onChange={(v) => sf("link", v)}
              placeholder="https://youtube.com/watch?v=... or https://..."
            />
          )}

          <SelectField label="Publish Status" value={form.status} onChange={(v) => sf("status", v)}
            options={[{ value: "draft", label: "Save as Draft" }, { value: "live", label: "Publish Now" }, { value: "scheduled", label: "Schedule" }]} />

          <div className="flex gap-2.5 justify-end mt-3">
            <RS3Button variant="ghost" onClick={closeModal}>Cancel</RS3Button>
            <RS3Button onClick={add} disabled={uploading}>{uploading ? "Uploading…" : "Publish Post"}</RS3Button>
          </div>
        </Modal>
      )}
    </div>
  );
};

export default FeedPage;
