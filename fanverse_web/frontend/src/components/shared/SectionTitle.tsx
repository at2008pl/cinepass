interface SectionTitleProps {
  children: React.ReactNode;
  action?: React.ReactNode;
  sub?: string;
}

const SectionTitle = ({ children, action, sub }: SectionTitleProps) => (
  <div className="flex items-start justify-between mb-6">
    <div>
      <h2 className="font-display text-[22px] text-foreground font-normal mb-0">{children}</h2>
      {sub && <p className="text-xs text-muted-foreground mt-1">{sub}</p>}
    </div>
    {action}
  </div>
);

export default SectionTitle;
