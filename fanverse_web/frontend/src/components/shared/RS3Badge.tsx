import { cn } from "@/lib/utils";

interface BadgeProps {
  children: React.ReactNode;
  variant?: "gold" | "green" | "blue" | "amber" | "purple" | "red" | "muted";
  small?: boolean;
}

const variantStyles: Record<string, string> = {
  gold: "bg-gold/10 text-gold",
  green: "bg-rs3-green/10 text-rs3-green",
  blue: "bg-rs3-blue/10 text-rs3-blue",
  amber: "bg-rs3-amber/10 text-rs3-amber",
  purple: "bg-rs3-purple/10 text-rs3-purple",
  red: "bg-rs3-red/10 text-rs3-red",
  muted: "bg-muted text-muted-foreground",
};

const RS3Badge = ({ children, variant = "gold", small = false }: BadgeProps) => (
  <span
    className={cn(
      "inline-block rounded-sm font-semibold uppercase tracking-wider",
      small ? "px-1.5 py-px text-[9px]" : "px-2.5 py-0.5 text-[10px]",
      variantStyles[variant]
    )}
  >
    {children}
  </span>
);

export default RS3Badge;
