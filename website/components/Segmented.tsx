"use client";

import { motion, useReducedMotion } from "motion/react";
import { useId, useRef } from "react";

type Option<T extends string> = { value: T; label: string };

/** A radio group drawn as a capsule; the selected pill slides between options. */
export function Segmented<T extends string>({
  options, value, onChange, labelledBy, label, id,
}: {
  options: Option<T>[];
  value: T;
  onChange: (v: T) => void;
  labelledBy?: string;
  label?: string;
  id?: string;
}) {
  const reduce = useReducedMotion();
  const layoutId = useId();
  const refs = useRef<(HTMLButtonElement | null)[]>([]);

  const move = (index: number) => {
    const next = (index + options.length) % options.length;
    onChange(options[next].value);
    refs.current[next]?.focus();
  };

  const onKeyDown = (e: React.KeyboardEvent, i: number) => {
    const rtl = document.documentElement.dir === "rtl";
    const map: Record<string, number> = {
      ArrowRight: rtl ? i - 1 : i + 1,
      ArrowLeft: rtl ? i + 1 : i - 1,
      ArrowDown: i + 1,
      ArrowUp: i - 1,
      Home: 0,
      End: options.length - 1,
    };
    if (!(e.key in map)) return;
    e.preventDefault();
    move(map[e.key]);
  };

  return (
    <div className="segmented" role="radiogroup" aria-labelledby={labelledBy} aria-label={label} id={id}>
      {options.map((o, i) => {
        const on = o.value === value;
        return (
          <button
            key={o.value}
            ref={(el) => { refs.current[i] = el; }}
            type="button"
            role="radio"
            aria-checked={on}
            tabIndex={on ? 0 : -1}
            onClick={() => onChange(o.value)}
            onKeyDown={(e) => onKeyDown(e, i)}
          >
            {on && (
              <motion.span
                className="pill"
                layoutId={layoutId}
                transition={reduce ? { duration: 0 } : { type: "spring", stiffness: 420, damping: 34 }}
              />
            )}
            {o.label}
          </button>
        );
      })}
    </div>
  );
}
