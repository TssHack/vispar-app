"use client";

import { AnimatePresence, motion, useReducedMotion } from "motion/react";
import { useRef, useState } from "react";
import type { Dictionary } from "@/content/dictionary";
import { icons } from "./icons";

const TAB_ICONS = [icons.movie, icons.series, icons.settings];

/**
 * The app's bottom bar: three tabs in one glass capsule and search in a round glass
 * button. The drop that marks the current tab stretches while it travels, then settles.
 */
export function TabBarDemo({ t }: { t: Dictionary }) {
  const reduce = useReducedMotion();
  const [current, setCurrent] = useState(0); // 0..2 tabs, 3 = search
  const refs = useRef<(HTMLButtonElement | null)[]>([]);
  const screen = t.tabs.screens[current];

  const select = (i: number, focus = false) => {
    setCurrent(i);
    if (focus) refs.current[i]?.focus();
  };

  const onKeyDown = (e: React.KeyboardEvent, i: number) => {
    const rtl = document.documentElement.dir === "rtl";
    const n = 4;
    const map: Record<string, number> = {
      ArrowRight: rtl ? i - 1 : i + 1,
      ArrowLeft: rtl ? i + 1 : i - 1,
      Home: 0,
      End: n - 1,
    };
    if (!(e.key in map)) return;
    e.preventDefault();
    select((map[e.key] + n) % n, true);
  };

  const spring = reduce ? { duration: 0 } : { type: "spring" as const, stiffness: 380, damping: 30, mass: 0.9 };

  return (
    <div className="device" aria-label={t.tabs.title}>
      <div className="device-scroll" aria-hidden="true">
        <motion.div
          className="device-fields"
          animate={reduce ? undefined : { rotate: current * 40, scale: 1 + (current % 2) * 0.12 }}
          transition={{ duration: 1.2, ease: [0.22, 1, 0.36, 1] }}
        >
          <span /><span /><span />
        </motion.div>
      </div>

      <AnimatePresence mode="wait" initial={false}>
        <motion.div
          key={current}
          id="tab-panel"
          role="tabpanel"
          className="device-screen"
          initial={reduce ? false : { opacity: 0, y: 14, filter: "blur(6px)" }}
          animate={{ opacity: 1, y: 0, filter: "blur(0px)" }}
          exit={reduce ? undefined : { opacity: 0, y: -10, filter: "blur(6px)" }}
          transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
        >
          <h3>{screen.title}</h3>
          <p>{screen.line}</p>
          {current === 2 ? (
            <div className="setting-rows" aria-hidden="true"><span /><span /><span /><span /></div>
          ) : (
            <div className={`poster-row${current === 1 ? " wide" : ""}`} aria-hidden="true">
              {Array.from({ length: current === 1 ? 4 : 6 }, (_, i) => <span key={i} className="poster" />)}
            </div>
          )}
        </motion.div>
      </AnimatePresence>

      <div className="tabbar" role="tablist" aria-label={t.tabs.title}>
        <div className="capsule">
          {t.tabs.items.map((label, i) => {
            const Icon = TAB_ICONS[i];
            const on = current === i;
            return (
              <button
                key={label}
                ref={(el) => { refs.current[i] = el; }}
                type="button"
                role="tab"
                aria-selected={on}
                aria-controls="tab-panel"
                tabIndex={on ? 0 : -1}
                onClick={() => select(i)}
                onKeyDown={(e) => onKeyDown(e, i)}
              >
                {on && (
                  <motion.span
                    layoutId="drop"
                    className="drop"
                    style={{ insetInline: 0 }}
                    transition={spring}
                    initial={false}
                    animate={reduce ? undefined : { scaleY: [1, 0.86, 1] }}
                  />
                )}
                <Icon />
                <span>{label}</span>
              </button>
            );
          })}
        </div>
        <button
          ref={(el) => { refs.current[3] = el; }}
          type="button"
          role="tab"
          className="search-orb"
          aria-selected={current === 3}
          aria-controls="tab-panel"
          aria-label={t.tabs.search}
          tabIndex={current === 3 ? 0 : -1}
          onClick={() => select(3)}
          onKeyDown={(e) => onKeyDown(e, 3)}
        >
          {icons.search({})}
        </button>
      </div>
    </div>
  );
}
