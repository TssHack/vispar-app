"use client";

import { useEffect, useState } from "react";
import {
  abis, apkName, assetUrl, megabytes, release,
  type Abi, type Dictionary, type Edition, type Locale,
} from "@/content/dictionary";
import { Segmented } from "./Segmented";

type Asset = { name: string; size: number; browser_download_url: string };
type Latest = { version: string; assets: Asset[] | null };

let latestRequest: Promise<Latest | null> | null = null;

function fetchLatest(): Promise<Latest | null> {
  latestRequest ??= fetch(`https://api.github.com/repos/${release.repo}/releases/latest`, {
    headers: { Accept: "application/vnd.github+json" },
  })
    .then((r) => (r.ok ? r.json() : Promise.reject(r.status)))
    .then((data: { tag_name?: string; assets?: Asset[] }) => {
      const v = String(data.tag_name ?? "").replace(/^v/, "");
      return /^\d+\.\d+\.\d+$/.test(v) ? { version: v, assets: data.assets ?? [] } : null;
    })
    .catch(() => null);
  return latestRequest;
}

/** The latest release, fetched once per page; on any failure the built-in v1.0.0 data stays. */
export function useLatestRelease(): Latest {
  const [latest, setLatest] = useState<Latest>({ version: release.version, assets: null });
  useEffect(() => {
    let live = true;
    fetchLatest().then((l) => { if (live && l) setLatest(l); });
    return () => { live = false; };
  }, []);
  return latest;
}

function link(latest: Latest, edition: Edition, abi: Abi) {
  const name = apkName(latest.version, edition, abi);
  const asset = latest.assets?.find((a) => a.name === name);
  return {
    name,
    href: asset?.browser_download_url ?? assetUrl(latest.version, name),
    bytes: asset?.size ?? release.bytes[abi],
  };
}

export function HeroDownload({ t, locale }: { t: Dictionary; locale: Locale }) {
  const latest = useLatestRelease();
  const uni = link(latest, "android", "universal");
  return (
    <>
      <div className="actions">
        <a className="btn" href={uni.href}>
          <svg aria-hidden="true" viewBox="0 0 24 24"><path d="M12 4v11m0 0-4.5-4.5M12 15l4.5-4.5M5 19.5h14" /></svg>
          {t.hero.cta}
        </a>
        <a className="btn btn-quiet" href="#download">{t.hero.ctaTv}</a>
      </div>
      <ul className="facts">
        <li>{t.hero.facts.version} <b className="tag" dir="ltr">{latest.version}</b></li>
        <li>{t.hero.facts.size.replace("{n}", megabytes(uni.bytes, locale))}</li>
        <li>{t.hero.facts.android}</li>
        <li>{t.hero.facts.free}</li>
      </ul>
    </>
  );
}

export function Downloads({ t, locale }: { t: Dictionary; locale: Locale }) {
  const latest = useLatestRelease();
  const [edition, setEdition] = useState<Edition>("android");
  const editions = (Object.keys(t.download.editions) as Edition[]).map((value) => ({ value, label: t.download.editions[value] }));

  return (
    <div className="panel">
      <div className="download-head">
        <div className="section-head">
          <h2 id="download-title">{t.download.title}</h2>
          <p>{t.download.lead}</p>
        </div>
        <Segmented label={t.download.editionLabel} options={editions} value={edition} onChange={setEdition} />
      </div>

      <table className="files">
        <caption className="sr-only">{t.download.caption}</caption>
        <thead>
          <tr>
            <th scope="col">{t.download.cols.arch}</th>
            <th scope="col">{t.download.cols.for}</th>
            <th scope="col">{t.download.cols.size}</th>
            <th scope="col"><span className="sr-only">{t.download.cols.get}</span></th>
          </tr>
        </thead>
        <tbody>
          {abis.map((abi) => {
            const f = link(latest, edition, abi);
            return (
              <tr key={abi}>
                <th scope="row">{abi === "universal" ? t.download.universal : <code>{abi}</code>}</th>
                <td>{t.download.for[abi]}</td>
                <td className="num">{t.download.mb.replace("{n}", megabytes(f.bytes, locale))}</td>
                <td>
                  <a className={`btn btn-small${abi === "universal" ? "" : " btn-quiet"}`} href={f.href} aria-label={`${t.download.get} ${f.name}`}>
                    {t.download.get}
                  </a>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>

      <p className="fine">
        <span>{t.download.version} <b dir="ltr">{latest.version}</b></span>
        <span aria-hidden="true">/</span>
        <a href={assetUrl(latest.version, "SHA256SUMS.txt")}>{t.download.sums}</a>
        <span aria-hidden="true">/</span>
        <a href={`https://github.com/${release.repo}/releases`}>{t.download.all}</a>
      </p>
    </div>
  );
}
