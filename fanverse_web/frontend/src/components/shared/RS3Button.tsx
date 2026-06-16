import { cn } from "@/lib/utils";

interface RS3ButtonProps {
  children: React.ReactNode;
  onClick?: () => void;
  variant?: "gold" | "ghost" | "red";
  small?: boolean;
  disabled?: boolean;
  className?: string;
}

const RS3Button = ({ children, onClick, variant = "gold", small = false, disabled = false, className }: RS3ButtonProps) => {
  const base = "rounded cursor-pointer text-xs font-medium tracking-wide transition-all border-none whitespace-nowrap";
  const size = small ? "px-3.5 py-1.5 text-[11px]" : "px-5 py-2.5 text-xs";

  const variants: Record<string, string> = {
    gold: "gold-gradient text-foreground hover:brightness-110",
    ghost: "bg-transparent text-foreground border border-border hover:bg-secondary",
    red: "bg-transparent text-rs3-red border border-rs3-red/30 hover:bg-rs3-red-pale",
  };

  return (
    <button
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
      className={cn(base, size, variants[variant], disabled && "opacity-50 cursor-default", className)}
      style={variant === "ghost" || variant === "red" ? { borderWidth: 1, borderStyle: "solid" } : undefined}
    >
      {children}
    </button>
  );
};

export default RS3Button;
