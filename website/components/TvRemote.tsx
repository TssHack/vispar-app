"use client";

import { useState } from "react";
import type { Dictionary } from "@/content/dictionary";
import { icons } from "./icons";

/** Remote keys and their table rows light up together, from either side. */
export function TvRemote({ t }: { t: Dictionary }) {
  const [lit, setLit] = useState<string | null>(null);
  const on = (key: string) => ({
    onPointerEnter: () => setLit(key),
    onPointerLeave: () => setLit(null),
    onFocus: () => setLit(key),
    onBlur: () => setLit(null),
  });
  const cls = (key: string, base: string) => `${base}${lit === key ? " lit" : ""}`;
  const label = (key: string) => t.tv.keys.find((k) => k.key === key)?.k ?? key;

  return (
    <div className="tv-grid">
      <div className="section-head">
        <h2 id="tv-title">{t.tv.title}</h2>
        <p>{t.tv.body}</p>
        <table className="keys">
          <caption className="sr-only">{t.tv.caption}</caption>
          <tbody>
            {t.tv.keys.map((k) => (
              <tr key={k.key} className={lit === k.key ? "lit" : undefined} onPointerEnter={() => setLit(k.key)} onPointerLeave={() => setLit(null)}>
                <th scope="row">{k.k}</th>
                <td>{k.d}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div className="remote">
        <div className="dpad">
          <button type="button" className={cls("ud", "arrow up")} aria-label={label("ud")} {...on("ud")}>{icons.tri({})}</button>
          <button type="button" className={cls("ud", "arrow down")} aria-label={label("ud")} {...on("ud")}>{icons.tri({ style: { rotate: "180deg" } })}</button>
          <button type="button" className={cls("lr", "arrow left")} aria-label={label("lr")} {...on("lr")}>{icons.tri({ style: { rotate: "-90deg" } })}</button>
          <button type="button" className={cls("lr", "arrow right")} aria-label={label("lr")} {...on("lr")}>{icons.tri({ style: { rotate: "90deg" } })}</button>
          <button type="button" className={cls("ok", "ok")} aria-label={label("ok")} {...on("ok")}>OK</button>
        </div>
        <div className="remote-row">
          <button type="button" className={cls("back", "rkey")} aria-label={label("back")} {...on("back")}>{icons.back({})}</button>
          <button type="button" className={cls("media", "rkey")} aria-label={label("media")} {...on("media")}>{icons.media({})}</button>
        </div>
      </div>
    </div>
  );
}
