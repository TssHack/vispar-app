"use client";

import { motion, useReducedMotion } from "motion/react";

/** The one orchestrated load moment: each headline line rises out of a blur. */
export function HeroTitle({ lines }: { lines: string[] }) {
  const reduce = useReducedMotion();
  return (
    <h1 id="hero-title">
      {lines.map((line, i) => (
        <motion.span
          key={line}
          className="line"
          initial={reduce ? false : { opacity: 0, y: 28, filter: "blur(12px)" }}
          animate={{ opacity: 1, y: 0, filter: "blur(0px)" }}
          transition={{ duration: 0.8, delay: 0.08 + i * 0.12, ease: [0.22, 1, 0.36, 1] }}
        >
          {line}
        </motion.span>
      ))}
    </h1>
  );
}
