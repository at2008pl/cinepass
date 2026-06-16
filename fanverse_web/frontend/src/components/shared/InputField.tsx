interface InputFieldProps {
  label?: string;
  value: string;
  onChange: (val: string) => void;
  type?: string;
  placeholder?: string;
  hint?: string;
  rows?: number;
  small?: boolean;
  mono?: boolean;
}

const InputField = ({ label, value, onChange, type = "text", placeholder, hint, rows, small, mono }: InputFieldProps) => (
  <div className="mb-4">
    {label && (
      <div className="text-[10px] font-semibold tracking-[2px] text-muted-foreground uppercase mb-1.5">{label}</div>
    )}
    {rows ? (
      <textarea
        value={value}
        placeholder={placeholder}
        rows={rows}
        onChange={(e) => onChange(e.target.value)}
        className={`w-full px-3.5 py-2.5 border border-border rounded bg-popover text-sm text-foreground resize-y focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 ${mono ? "font-mono" : ""}`}
      />
    ) : (
      <input
        type={type}
        value={value}
        placeholder={placeholder}
        onChange={(e) => onChange(e.target.value)}
        className={`w-full border border-border rounded bg-popover text-sm text-foreground focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10 ${small ? "px-3 py-2" : "px-3.5 py-2.5"} ${mono ? "font-mono" : ""}`}
      />
    )}
    {hint && <div className="text-xs text-muted-foreground mt-1 leading-relaxed">{hint}</div>}
  </div>
);

export default InputField;
