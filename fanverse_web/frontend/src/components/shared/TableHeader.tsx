interface TableHeaderProps {
  cols: Array<{ label: string; w: string }>;
}

const TableHeader = ({ cols }: TableHeaderProps) => (
  <div
    className="px-5 py-2.5 bg-background border-b border-border"
    style={{ display: "grid", gridTemplateColumns: cols.map((c) => c.w).join(" ") }}
  >
    {cols.map((c) => (
      <div key={c.label} className="text-[10px] font-semibold tracking-[1.5px] text-muted-foreground uppercase">
        {c.label}
      </div>
    ))}
  </div>
);

export default TableHeader;
