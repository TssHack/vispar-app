"use client";

import Image from "next/image";
import { motion, useReducedMotion } from "motion/react";
import { useCallback, useEffect, useRef, useState } from "react";
import bolt from "@/assets/bolt.png";
import type { Dictionary, Material } from "@/content/dictionary";
import { icons } from "./icons";
import { Segmented } from "./Segmented";

const MATERIALS: Record<Material, { scale: number; blur: number; bend: number }> = {
  clear: { scale: 1.38, blur: 0, bend: 1 },
  glass: { scale: 1.26, blur: 0.6, bend: 0.8 },
  frosted: { scale: 1.08, blur: 9, bend: 0.3 },
  none: { scale: 1, blur: 0, bend: 0 },
};

/** Builds a barrel-distortion displacement map: pixels near the rim are pushed outward. */
function displacementMap(): string | null {
  try {
    const size = 256;
    const canvas = document.createElement("canvas");
    canvas.width = canvas.height = size;
    const ctx = canvas.getContext("2d");
    if (!ctx) return null;
    const img = ctx.createImageData(size, size);
    for (let y = 0; y < size; y++) {
      for (let x = 0; x < size; x++) {
        const nx = (x / (size - 1)) * 2 - 1;
        const ny = (y / (size - 1)) * 2 - 1;
        const k = Math.pow(Math.min(1, Math.hypot(nx, ny)), 3);
        const i = (y * size + x) * 4;
        img.data[i] = 128 + nx * k * 127;
        img.data[i + 1] = 128 + ny * k * 127;
        img.data[i + 2] = 128;
        img.data[i + 3] = 255;
      }
    }
    ctx.putImageData(img, 0, 0);
    return canvas.toDataURL();
  } catch {
    return null;
  }
}

function Scene({ wordmark }: { wordmark: string }) {
  return (
    <>
      <div className="field field-a" />
      <div className="field field-b" />
      <div className="field field-c" />
      <span className="wordmark">{wordmark}</span>
      <Image className="scene-bolt" src={bolt} alt="" width={112} height={112} priority />
      <div className="grain" />
    </>
  );
}

export function GlassShowcase({ t }: { t: Dictionary }) {
  const reduce = useReducedMotion();
  const [material, setMaterial] = useState<Material>("glass");
  const [touched, setTouched] = useState(false);
  const [mapUrl, setMapUrl] = useState<string | null>(null);

  const stageRef = useRef<HTMLDivElement>(null);
  const lensRef = useRef<HTMLDivElement>(null);
  const viewRef = useRef<HTMLDivElement>(null);
  const filterRef = useRef<SVGFilterElement>(null);
  const mapRef = useRef<SVGFEImageElement>(null);
  const dispRef = useRef<SVGFEDisplacementMapElement>(null);

  const pos = useRef({ x: 0, y: 0 });
  const dims = useRef({ w: 0, h: 0, r: 0 });
  const materialRef = useRef<Material>("glass");
  const drifting = useRef(true);

  useEffect(() => setMapUrl(displacementMap()), []);

  const place = useCallback((x: number, y: number) => {
    const lens = lensRef.current;
    const view = viewRef.current;
    if (!lens || !view) return;
    const { w, h, r } = dims.current;
    const px = Math.max(r * 0.35, Math.min(w - r * 0.35, x));
    const py = Math.max(r * 0.35, Math.min(h - r * 0.35, y));
    pos.current = { x: px, y: py };
    const m = MATERIALS[materialRef.current];
    lens.style.transform = `translate3d(${px - r}px, ${py - r}px, 0)`;
    // The scene point under the lens centre stays there; the rest grows away from it.
    view.style.transform = `translate3d(${r - px * m.scale}px, ${r - py * m.scale}px, 0) scale(${m.scale})`;

    // The filter works in the scene's own coordinates, so its region follows the lens.
    const f = filterRef.current, map = mapRef.current, disp = dispRef.current;
    if (f && map && disp && m.bend > 0) {
      const half = r / m.scale;
      for (const el of [f, map]) {
        el.setAttribute("x", String(px - half));
        el.setAttribute("y", String(py - half));
        el.setAttribute("width", String(half * 2));
        el.setAttribute("height", String(half * 2));
      }
      disp.setAttribute("scale", String(half * 0.55 * m.bend));
    }
  }, []);

  const measure = useCallback(() => {
    const stage = stageRef.current, lens = lensRef.current, view = viewRef.current;
    if (!stage || !lens || !view) return;
    // offset sizes ignore the stage's entrance transform.
    const w = stage.offsetWidth, h = stage.offsetHeight;
    dims.current = { w, h, r: lens.offsetWidth / 2 };
    view.style.width = `${w}px`;
    view.style.height = `${h}px`;
  }, []);

  // Material: filter chain and lens magnification.
  useEffect(() => {
    materialRef.current = material;
    const view = viewRef.current;
    if (!view) return;
    const m = MATERIALS[material];
    const parts: string[] = [];
    if (mapUrl && m.bend > 0) parts.push("url(#vispar-bend)");
    if (m.blur > 0) parts.push(`blur(${m.blur}px)`);
    parts.push(material === "frosted" ? "saturate(1.4)" : "saturate(1.15)");
    view.style.filter = parts.join(" ");
    view.style.visibility = material === "none" ? "hidden" : "visible";
    place(pos.current.x, pos.current.y);
  }, [material, mapUrl, place]);

  // Size tracking.
  useEffect(() => {
    const stage = stageRef.current;
    if (!stage) return;
    measure();
    place(dims.current.w * 0.32, dims.current.h * 0.5);
    const ro = new ResizeObserver(() => {
      const { w, h } = dims.current;
      const rx = w ? pos.current.x / w : 0.32;
      const ry = h ? pos.current.y / h : 0.5;
      measure();
      place(rx * dims.current.w, ry * dims.current.h);
    });
    ro.observe(stage);
    return () => ro.disconnect();
  }, [measure, place]);

  // Idle drift on a slow figure-eight until someone touches the lens.
  useEffect(() => {
    if (reduce) { drifting.current = false; return; }
    let raf = 0;
    let visible = true;
    const io = new IntersectionObserver(([e]) => { visible = e.isIntersecting; });
    if (stageRef.current) io.observe(stageRef.current);
    const t0 = performance.now();
    const tick = (now: number) => {
      if (!drifting.current) return;
      if (visible) {
        const t = (now - t0) / 1000;
        const { w, h } = dims.current;
        place(w * 0.5 + Math.sin(t * 0.33) * w * 0.3, h * 0.5 + Math.sin(t * 0.66) * h * 0.18);
      }
      raf = requestAnimationFrame(tick);
    };
    raf = requestAnimationFrame(tick);
    return () => { cancelAnimationFrame(raf); io.disconnect(); };
  }, [reduce, place]);

  const stopDrift = () => { drifting.current = false; setTouched(true); };

  const grab = useRef<{ dx: number; dy: number } | null>(null);
  const onPointerDown = (e: React.PointerEvent) => {
    stopDrift();
    e.currentTarget.setPointerCapture(e.pointerId);
    const rect = stageRef.current!.getBoundingClientRect();
    grab.current = { dx: e.clientX - rect.left - pos.current.x, dy: e.clientY - rect.top - pos.current.y };
  };
  const onPointerMove = (e: React.PointerEvent) => {
    if (!grab.current) return;
    const rect = stageRef.current!.getBoundingClientRect();
    place(e.clientX - rect.left - grab.current.dx, e.clientY - rect.top - grab.current.dy);
  };
  const onPointerUp = () => { grab.current = null; };
  const onKeyDown = (e: React.KeyboardEvent) => {
    const step = e.shiftKey ? 48 : 16;
    const moves: Record<string, [number, number]> = { ArrowLeft: [-step, 0], ArrowRight: [step, 0], ArrowUp: [0, -step], ArrowDown: [0, step] };
    const mv = moves[e.key];
    if (!mv) return;
    e.preventDefault();
    stopDrift();
    place(pos.current.x + mv[0], pos.current.y + mv[1]);
  };

  const options = (Object.keys(t.glass.materials) as Material[]).map((value) => ({ value, label: t.glass.materials[value] }));

  return (
    <>
      <svg width="0" height="0" aria-hidden="true" style={{ position: "absolute" }}>
        <filter id="vispar-bend" ref={filterRef} filterUnits="userSpaceOnUse" primitiveUnits="userSpaceOnUse" colorInterpolationFilters="sRGB">
          {mapUrl && <feImage ref={mapRef} href={mapUrl} preserveAspectRatio="none" result="map" />}
          <feDisplacementMap ref={dispRef} in="SourceGraphic" in2="map" scale="0" xChannelSelector="R" yChannelSelector="G" />
        </filter>
      </svg>

      <motion.div
        ref={stageRef}
        className={`stage${touched ? " touched" : ""}`}
        data-material={material}
        initial={reduce ? false : { opacity: 0, y: 24, scale: 0.985 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ duration: 0.9, delay: 0.35, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="scene" aria-hidden="true"><Scene wordmark={t.hero.wordmark} /></div>
        <div
          ref={lensRef}
          className="lens"
          tabIndex={0}
          role="img"
          aria-label={t.hero.lensLabel}
          onPointerDown={onPointerDown}
          onPointerMove={onPointerMove}
          onPointerUp={onPointerUp}
          onPointerCancel={onPointerUp}
          onKeyDown={onKeyDown}
        >
          <div ref={viewRef} className="lens-view" aria-hidden="true"><Scene wordmark={t.hero.wordmark} /></div>
        </div>
        <p className="stage-hint">{icons.drag({})}{t.hero.hint}</p>
      </motion.div>

      <div className="glass-row" id="glass">
        <div className="section-head">
          <h2 id="glass-title">{t.glass.title}</h2>
          <p>{t.glass.body}</p>
          <p className="note">{t.glass.note}</p>
        </div>
        <Segmented labelledBy="glass-title" options={options} value={material} onChange={setMaterial} id="materials" />
      </div>
    </>
  );
}
