interface ModalProps {
  title: string;
  children: React.ReactNode;
  onClose: () => void;
  wide?: boolean;
}

const Modal = ({
  title,
  children,
  onClose,
  wide = false,
}: ModalProps) => {
  return (
    <div
      className="fixed inset-0 z-[1000] flex items-start justify-center pt-[0vh] bg-foreground/60"
      onClick={onClose}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className={`
          w-full
          flex flex-col
          bg-popover
          rounded-md
          shadow-2xl
          fade-in
          ${wide ? "max-w-[700px]" : "max-w-[560px]"}
        `}
        style={{ maxHeight: "90vh" }}
      >
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border shrink-0">
          <h3 className="font-display text-lg font-normal text-foreground">
            {title}
          </h3>

          <button
            onClick={onClose}
            className="px-1 text-2xl leading-none transition-colors text-muted-foreground hover:text-foreground"
            aria-label="Close modal"
          >
            ×
          </button>
        </div>

        {/* Body */}
        <div className="p-6 overflow-y-auto">
          {children}
        </div>
      </div>
    </div>
  );
};

export default Modal;