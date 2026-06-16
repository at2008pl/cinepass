interface SelectFieldProps {
  label?: string;
  value: string;
  onChange: (val: string) => void;
  options: Array<{ value: string; label: string } | string>;
}

const SelectField = ({ label, value, onChange, options }: SelectFieldProps) => (
  <div className="mb-4">
    {label && (
      <div className="text-[10px] font-semibold tracking-[2px] text-muted-foreground uppercase mb-1.5">{label}</div>
    )}
    <div className="relative">
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        className="w-full px-3.5 py-2.5 pr-9 border border-border rounded bg-popover text-sm text-foreground appearance-none cursor-pointer focus:outline-none focus:border-primary focus:ring-2 focus:ring-primary/10"
      >
        {options.map((o) => {
          const val = typeof o === "string" ? o : o.value;
          const lab = typeof o === "string" ? o : o.label;
          return <option key={val} value={val}>{lab}</option>;
        })}
      </select>
      <span className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground text-xs pointer-events-none">▾</span>
    </div>
  </div>
);

export default SelectField;
