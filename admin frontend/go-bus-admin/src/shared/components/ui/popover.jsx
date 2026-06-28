import * as React from "react";
import * as ReactDOM from "react-dom";
import { cn } from "lib/utils";

const PopoverContext = React.createContext({});

export function Popover({ children }) {
  const [open, setOpen] = React.useState(false);
  const triggerRef = React.useRef(null);
  
  return (
    <PopoverContext.Provider value={{ open, setOpen, triggerRef }}>
      <div className="relative inline-block w-full" ref={triggerRef}>
        {children}
      </div>
    </PopoverContext.Provider>
  );
}

export function PopoverTrigger({ children, asChild }) {
  const { open, setOpen } = React.useContext(PopoverContext);
  
  const handleClick = (e) => {
    e.stopPropagation();
    setOpen(!open);
  };

  if (asChild && React.isValidElement(children)) {
    return React.cloneElement(children, {
      onClick: (e) => {
        handleClick(e);
        // Call original onClick if it exists
        if (children.props.onClick) {
          children.props.onClick(e);
        }
      },
    });
  }

  return (
    <button type="button" onClick={handleClick}>
      {children}
    </button>
  );
}

export function PopoverContent({ children, className, align = "start", side = "bottom" }) {
  const { open, setOpen, triggerRef } = React.useContext(PopoverContext);
  const contentRef = React.useRef(null);
  const [position, setPosition] = React.useState({ top: 0, left: 0 });
  // `ready` keeps the content hidden until it has been measured & positioned,
  // so it never flashes at the top-left corner before jumping into place.
  const [ready, setReady] = React.useState(false);

  // Position the content next to the trigger. We portal to <body> and use
  // document-relative `absolute` coordinates (rect + scroll offset), which
  // keeps the popover glued to the trigger as the page scrolls.
  const updatePosition = React.useCallback(() => {
    if (!triggerRef.current || !contentRef.current) return;

    const rect = triggerRef.current.getBoundingClientRect();
    const content = contentRef.current.getBoundingClientRect();
    const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
    const viewportWidth = document.documentElement.clientWidth;
    const viewportHeight = window.innerHeight;
    const gap = 8;       // distance between trigger and popover
    const margin = 8;    // min distance from the viewport edge

    // Vertical: flip above the trigger when there isn't room below.
    const spaceBelow = viewportHeight - rect.bottom;
    const spaceAbove = rect.top;
    let placeAbove = side === "top";
    if (side === "bottom" && spaceBelow < content.height + gap && spaceAbove > spaceBelow) {
      placeAbove = true;
    } else if (side === "top" && spaceAbove < content.height + gap && spaceBelow > spaceAbove) {
      placeAbove = false;
    }
    const top = placeAbove
      ? rect.top + scrollTop - content.height - gap
      : rect.bottom + scrollTop + gap;

    // Horizontal: align to the trigger, then clamp inside the viewport.
    let left;
    if (align === "center") {
      left = rect.left + rect.width / 2 - content.width / 2;
    } else if (align === "end") {
      left = rect.right - content.width;
    } else {
      left = rect.left;
    }
    const maxLeft = viewportWidth - content.width - margin;
    left = Math.max(margin, Math.min(left, maxLeft)) + scrollLeft;

    setPosition({ top, left });
    setReady(true);
  }, [align, side, triggerRef]);

  // Measure synchronously before paint to avoid a visible jump, and keep the
  // popover anchored while open (handles window resize and nested scrolling).
  React.useLayoutEffect(() => {
    if (!open) {
      setReady(false);
      return;
    }
    updatePosition();
    window.addEventListener("scroll", updatePosition, true); // capture: catch scroll on any ancestor
    window.addEventListener("resize", updatePosition);
    return () => {
      window.removeEventListener("scroll", updatePosition, true);
      window.removeEventListener("resize", updatePosition);
    };
  }, [open, updatePosition]);

  React.useEffect(() => {
    const handleClickOutside = (event) => {
      // Don't close if clicking inside the content or trigger
      if (contentRef.current && !contentRef.current.contains(event.target) &&
          triggerRef.current && !triggerRef.current.contains(event.target)) {
        setOpen(false);
      }
    };

    if (open) {
      // Small delay to prevent immediate closure
      const timeoutId = setTimeout(() => {
        document.addEventListener("mousedown", handleClickOutside);
      }, 100);

      return () => {
        clearTimeout(timeoutId);
        document.removeEventListener("mousedown", handleClickOutside);
      };
    }
  }, [open, setOpen, triggerRef]);

  if (!open) return null;

  return ReactDOM.createPortal(
    <div
      ref={contentRef}
      className={cn(
        "absolute z-[99999] rounded-2xl border shadow-2xl pointer-events-auto",
        className
      )}
      style={{
        top: `${position.top}px`,
        left: `${position.left}px`,
        visibility: ready ? "visible" : "hidden",
      }}
      onClick={(e) => e.stopPropagation()}
      onMouseDown={(e) => e.stopPropagation()}
    >
      {children}
    </div>,
    document.body
  );
}
