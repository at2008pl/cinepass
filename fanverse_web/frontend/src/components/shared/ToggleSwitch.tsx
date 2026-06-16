interface ToggleSwitchProps {
  on: boolean;
  onChange: (val: boolean) => void;
}

const ToggleSwitch = ({ on, onChange }: ToggleSwitchProps) => (
  <label className="relative w-10 h-[22px] cursor-pointer inline-block">
    <input
      type="checkbox"
      checked={on}
      onChange={(e) => onChange(e.target.checked)}
      className="opacity-0 w-0 h-0 absolute"
    />
    <span
      className={`absolute inset-0 rounded-full transition-colors duration-200 ${on ? "bg-primary" : "bg-border"}`}
    >
      <span
        className={`absolute h-4 w-4 left-[3px] bottom-[3px] bg-popover rounded-full transition-transform duration-200 ${on ? "translate-x-[18px]" : ""}`}
      />
    </span>
  </label>
);

export default ToggleSwitch;
