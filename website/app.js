(() => {
  "use strict";

  const reduceMotion = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const store = {
    get(key) { try { return localStorage.getItem(key); } catch { return null; } },
    set(key, value) { try { localStorage.setItem(key, value); } catch { /* storage blocked */ } },
  };

  /* ---------- Digits and sizes ---------- */

  const faDigits = "۰۱۲۳۴۵۶۷۸۹";
  const toFa = (s) => String(s).replace(/\d/g, (d) => faDigits[d]).replace(/\./g, "٫");
  let lang = "fa";
  const localNum = (s) => (lang === "fa" ? toFa(s) : String(s));
  const mb = (bytes) => (bytes / 1e6).toFixed(1);

  /* ---------- Lens ---------- */

  const stage = document.getElementById("stage");
  const scene = document.getElementById("scene");
  const lens = document.getElementById("lens");
  const view = document.getElementById("lens-view");

  const materials = {
    clear: { scale: 1.38, blur: 0, bend: 1 },
    glass: { scale: 1.26, blur: 0.6, bend: 0.8 },
    frosted: { scale: 1.08, blur: 9, bend: 0.3 },
    none: { scale: 1, blur: 0, bend: 0 },
  };
  let material = "glass";

  // A copy of the scene inside the lens, magnified around the lens centre.
  const sceneCopy = scene.cloneNode(true);
  sceneCopy.removeAttribute("id");
  view.appendChild(sceneCopy);

  // Barrel distortion: an SVG displacement map that pushes pixels outward near the rim.
  // Browsers that cannot apply url() filters to HTML simply skip it and keep the magnification.
  const svgNS = "http://www.w3.org/2000/svg";
  let bendFilterOk = false;
  try {
    const size = 256;
    const c = document.createElement("canvas");
    c.width = c.height = size;
    const ctx = c.getContext("2d");
    const img = ctx.createImageData(size, size);
    for (let y = 0; y < size; y++) {
      for (let x = 0; x < size; x++) {
        const nx = (x / (size - 1)) * 2 - 1;
        const ny = (y / (size - 1)) * 2 - 1;
        const r = Math.min(1, Math.hypot(nx, ny));
        const k = Math.pow(r, 3);
        const i = (y * size + x) * 4;
        img.data[i] = 128 + nx * k * 127;
        img.data[i + 1] = 128 + ny * k * 127;
        img.data[i + 2] = 128;
        img.data[i + 3] = 255;
      }
    }
    ctx.putImageData(img, 0, 0);
    const svg = document.createElementNS(svgNS, "svg");
    svg.setAttribute("width", "0");
    svg.setAttribute("height", "0");
    svg.setAttribute("aria-hidden", "true");
    svg.style.position = "absolute";
    svg.innerHTML =
      '<filter id="bend" filterUnits="userSpaceOnUse" primitiveUnits="userSpaceOnUse" color-interpolation-filters="sRGB">' +
      '<feImage id="bend-map" preserveAspectRatio="none" result="map"/>' +
      '<feDisplacementMap id="bend-disp" in="SourceGraphic" in2="map" scale="0" xChannelSelector="R" yChannelSelector="G"/>' +
      "</filter>";
    document.body.appendChild(svg);
    svg.querySelector("#bend-map").setAttribute("href", c.toDataURL());
    bendFilterOk = true;
  } catch { /* no canvas: magnification only */ }

  const bendFilter = document.getElementById("bend");
  const bendMap = document.getElementById("bend-map");
  const bendDisp = document.getElementById("bend-disp");
  let pos = { x: 0, y: 0 };
  let dims = { w: 0, h: 0, r: 0 };

  function measure() {
    const rect = stage.getBoundingClientRect();
    dims.w = rect.width;
    dims.h = rect.height;
    dims.r = lens.offsetWidth / 2;
    sceneCopy.style.width = dims.w + "px";
    sceneCopy.style.height = dims.h + "px";
    view.style.width = dims.w + "px";
    view.style.height = dims.h + "px";
  }

  // The filter works in the scene's own (unscaled) coordinates: the lens shows a square of
  // side 2r/scale centred on the lens position, so the map and the filter region follow it.
  function moveBend(m) {
    if (!bendFilterOk || m.bend <= 0) return;
    const half = dims.r / m.scale;
    const x = String(pos.x - half);
    const y = String(pos.y - half);
    const side = String(half * 2);
    for (const el of [bendFilter, bendMap]) {
      el.setAttribute("x", x);
      el.setAttribute("y", y);
      el.setAttribute("width", side);
      el.setAttribute("height", side);
    }
    bendDisp.setAttribute("scale", String(half * 0.55 * m.bend));
  }

  function clamp(v, lo, hi) { return Math.max(lo, Math.min(hi, v)); }

  function place(x, y) {
    const r = dims.r;
    pos.x = clamp(x, r * 0.35, dims.w - r * 0.35);
    pos.y = clamp(y, r * 0.35, dims.h - r * 0.35);
    lens.style.transform = `translate(${pos.x - r}px, ${pos.y - r}px)`;
    const m = materials[material];
    // Content point under the lens centre stays under it; everything else grows away from it.
    const tx = r - pos.x * m.scale;
    const ty = r - pos.y * m.scale;
    view.style.transform = `translate(${tx}px, ${ty}px) scale(${m.scale})`;
    moveBend(m);
  }

  function applyMaterial(name) {
    material = name;
    stage.dataset.material = name;
    const m = materials[name];
    view.style.visibility = name === "none" ? "hidden" : "visible";
    const parts = [];
    if (bendFilterOk && m.bend > 0) parts.push("url(#bend)");
    if (m.blur > 0) parts.push(`blur(${m.blur}px)`);
    parts.push(name === "frosted" ? "saturate(1.4)" : "saturate(1.15)");
    view.style.filter = parts.join(" ");
    place(pos.x, pos.y);
  }

  // Idle drift along a slow figure-eight until someone touches the lens.
  let drifting = !reduceMotion;
  let visible = true;
  let t0 = performance.now();
  function drift(now) {
    if (drifting && visible) {
      const t = (now - t0) / 1000;
      const cx = dims.w * 0.5;
      const cy = dims.h * 0.5;
      place(cx + Math.sin(t * 0.35) * dims.w * 0.3, cy + Math.sin(t * 0.7) * dims.h * 0.18);
    }
    if (drifting) requestAnimationFrame(drift);
  }

  function stopDrift() {
    drifting = false;
    stage.classList.add("touched");
  }

  let dragging = false;
  let grab = { dx: 0, dy: 0 };
  lens.addEventListener("pointerdown", (e) => {
    stopDrift();
    dragging = true;
    lens.setPointerCapture(e.pointerId);
    const rect = stage.getBoundingClientRect();
    grab.dx = e.clientX - rect.left - pos.x;
    grab.dy = e.clientY - rect.top - pos.y;
  });
  lens.addEventListener("pointermove", (e) => {
    if (!dragging) return;
    const rect = stage.getBoundingClientRect();
    place(e.clientX - rect.left - grab.dx, e.clientY - rect.top - grab.dy);
  });
  const endDrag = () => { dragging = false; };
  lens.addEventListener("pointerup", endDrag);
  lens.addEventListener("pointercancel", endDrag);

  lens.addEventListener("keydown", (e) => {
    const step = e.shiftKey ? 48 : 16;
    const moves = { ArrowLeft: [-step, 0], ArrowRight: [step, 0], ArrowUp: [0, -step], ArrowDown: [0, step] };
    const mv = moves[e.key];
    if (!mv) return;
    e.preventDefault();
    stopDrift();
    place(pos.x + mv[0], pos.y + mv[1]);
  });

  new IntersectionObserver((entries) => { visible = entries[0].isIntersecting; }).observe(stage);

  new ResizeObserver(() => {
    const ratioX = dims.w ? pos.x / dims.w : 0.33;
    const ratioY = dims.h ? pos.y / dims.h : 0.5;
    measure();
    applyMaterial(material);
    place(ratioX * dims.w, ratioY * dims.h);
  }).observe(stage);

  measure();
  place(dims.w * 0.33, dims.h * 0.5);
  applyMaterial("glass");
  if (drifting) requestAnimationFrame(drift);

  /* ---------- Segmented controls ---------- */

  function segmented(group, attr, onChange) {
    const buttons = [...group.querySelectorAll("button")];
    const select = (btn, focus) => {
      buttons.forEach((b) => {
        const on = b === btn;
        b.setAttribute("aria-checked", String(on));
        b.tabIndex = on ? 0 : -1;
      });
      if (focus) btn.focus();
      onChange(btn.dataset[attr]);
    };
    buttons.forEach((b) => {
      b.tabIndex = b.getAttribute("aria-checked") === "true" ? 0 : -1;
      b.addEventListener("click", () => select(b, false));
      b.addEventListener("keydown", (e) => {
        const i = buttons.indexOf(b);
        const rtl = document.documentElement.dir === "rtl";
        let next = null;
        if (e.key === "ArrowRight") next = rtl ? i - 1 : i + 1;
        if (e.key === "ArrowLeft") next = rtl ? i + 1 : i - 1;
        if (e.key === "ArrowDown") next = i + 1;
        if (e.key === "ArrowUp") next = i - 1;
        if (e.key === "Home") next = 0;
        if (e.key === "End") next = buttons.length - 1;
        if (next === null) return;
        e.preventDefault();
        select(buttons[(next + buttons.length) % buttons.length], true);
      });
    });
  }

  segmented(document.getElementById("materials"), "material", applyMaterial);

  /* ---------- Downloads ---------- */

  const repo = "TssHack/vispar-app";
  let release = { version: "1.0.0", assets: null };
  let edition = "android";

  function fileName(abi) { return `Vispar-${release.version}-${edition}-${abi}.apk`; }
  function fileUrl(name) { return `https://github.com/${repo}/releases/download/v${release.version}/${name}`; }

  function renderDownloads() {
    document.querySelectorAll("#file-rows tr").forEach((row) => {
      const name = fileName(row.dataset.abi);
      const asset = release.assets && release.assets.find((a) => a.name === name);
      const bytes = asset ? asset.size : Number(row.dataset.bytes);
      row.querySelector('[data-cell="size"]').textContent = lang === "fa" ? `${toFa(mb(bytes))} مگابایت` : `${mb(bytes)} MB`;
      row.querySelector('[data-cell="link"]').href = asset ? asset.browser_download_url : fileUrl(name);
      row.querySelector('[data-cell="link"]').setAttribute("aria-label", `${t("get")} ${name}`);
    });
    const universal = fileName("universal").replace(`-${edition}-`, "-android-");
    document.getElementById("hero-download").href = fileUrl(universal);
    document.getElementById("sums-link").href = fileUrl("SHA256SUMS.txt");
    document.querySelectorAll("[data-version]").forEach((el) => { el.textContent = release.version; });
    const uni = release.assets && release.assets.find((a) => a.name === universal);
    if (uni) document.querySelectorAll("[data-size]").forEach((el) => { el.textContent = t("factSizeN").replace("{n}", localNum(mb(uni.size))); });
  }

  segmented(document.getElementById("editions"), "edition", (e) => { edition = e; renderDownloads(); });

  // Pick up a newer release without editing the page. On any failure the v1.0.0 links stay.
  fetch(`https://api.github.com/repos/${repo}/releases/latest`, { headers: { Accept: "application/vnd.github+json" } })
    .then((r) => (r.ok ? r.json() : Promise.reject(r.status)))
    .then((data) => {
      const v = String(data.tag_name || "").replace(/^v/, "");
      if (!/^\d+\.\d+\.\d+$/.test(v)) return;
      release = { version: v, assets: data.assets || [] };
      renderDownloads();
    })
    .catch(() => {});

  /* ---------- Remote: keys light up with their table row ---------- */

  const remote = document.getElementById("remote");
  document.querySelectorAll(".keys tr[data-key]").forEach((row) => {
    const key = row.dataset.key;
    const on = (state) => {
      row.classList.toggle("lit", state);
      remote.querySelectorAll(`[data-key="${key}"]`).forEach((el) => el.classList.toggle("lit", state));
    };
    row.addEventListener("pointerenter", () => on(true));
    row.addEventListener("pointerleave", () => on(false));
  });

  /* ---------- Language ---------- */

  const en = {
    skip: "Skip to content",
    navLabel: "Main",
    brandLabel: "Vispar, back to top",
    brand: "Vispar",
    navGlass: "Glass",
    navFeatures: "Features",
    navTv: "TV",
    navDownload: "Download",
    heroTitle: "Movies and series behind liquid glass.",
    heroLead: "Vispar is a free app for Android and Android TV. Every panel blurs and bends whatever is behind it, even the player controls, which refract the film that is playing.",
    heroCta: "Download for Android",
    heroCtaTv: "TV edition and architectures",
    factVersion: "Version",
    factSize: "About 3.7 MB",
    factSizeN: "About {n} MB",
    factAndroid: "Android 7.0 and newer",
    factFree: "Free and open source",
    wordmark: "Vispar",
    lensLabel: "Glass lens. Drag it, or move it with the arrow keys.",
    stageHint: "Drag the lens.",
    glassTitle: "Pick the glass yourself.",
    glassBody: "The same four options as the app's Settings, on the lens above. No transparency is the fastest and the default on TV. Full blur needs Android 12 and refraction Android 13; older devices show a frosted tint.",
    matClear: "Clear",
    matGlass: "Glass",
    matFrosted: "Frosted",
    matNone: "No transparency",
    featuresTitle: "What you need to watch, and nothing more.",
    featuresLead: "Persian is the default and the layout follows the language. English is one tap away.",
    f1t: "Player",
    f1b: "Built on Media3 ExoPlayer with HLS and DASH. Speed from 0.75x to 2x, your own skip interval, autoplay, and subtitles in the colour and size you choose.",
    f2t: "Continue watching",
    f2b: "A row on the Movies and Series tabs that picks up at the second you stopped.",
    f3t: "One account, phone and TV",
    f3b: "Sign in with your mobile number and an SMS code, no password. Favourites and Continue Watching match on every device, and you can sign other devices out remotely.",
    f4t: "Favourites",
    f4b: "Keep films and series, remove them one by one, or clear the list.",
    f5t: "Search",
    f5b: "One search for movies and series, from the glass button beside the bottom bar.",
    f6t: "Downloads",
    f6b: "Pick a quality and hand the file to your browser, ADM or VLC.",
    tvTitle: "On the TV, with the remote you have.",
    tvBody: "The TV edition has a glass sidebar and a focus ring on every control, and always uses the TV interface, even on boxes that say they are tablets.",
    keysCaption: "Remote keys in the player",
    kOk: "OK",
    kOkDo: "Play or pause",
    kLr: "Left and right",
    kLrDo: "Skip back and forward",
    kUd: "Up and down",
    kUdDo: "Show the controls",
    kBack: "Back",
    kBackDo: "Close the player",
    kMedia: "Media keys",
    kMediaDo: "Play, pause, skip",
    screensTitle: "A look at the app",
    dlTitle: "Download",
    dlLead: "Not sure which? Universal installs on every device. Both editions share one signature, so either installs over the other.",
    editionLabel: "Edition",
    edPhone: "Phone and tablet",
    edTv: "TV",
    filesCaption: "Installers by processor architecture",
    colArch: "Architecture",
    colFor: "For",
    colSize: "Size",
    colGet: "Download",
    abiUniversal: "Universal",
    abiUniversalFor: "Every device. If you are not sure, take this one.",
    abiArm64For: "Most phones from recent years, newer TVs and boxes",
    abiV7For: "Older phones and many TV boxes",
    abiX64For: "Emulators and 64-bit Intel devices",
    abiX86For: "Older 32-bit Intel devices",
    get: "Download",
    dlFine: "Version",
    sums: "SHA-256 checksums",
    allReleases: "All releases",
    footerMade: 'Made by <a href="https://ehsanfazli.ir">Ehsan Fazli</a>. Open source under the MIT license.',
    footSource: "Source code",
    footCross: "Vispar for iOS, web and desktop",
    footIssues: "Report a problem",
  };

  // Persian strings come from the page itself, so the HTML stays the single source for them.
  const fa = {};
  document.querySelectorAll("[data-i18n]").forEach((el) => {
    const key = el.dataset.i18n;
    if (!(key in fa)) fa[key] = en[key] && en[key].includes("<") ? el.innerHTML : el.textContent;
  });
  document.querySelectorAll("[data-i18n-attr]").forEach((el) => {
    el.dataset.i18nAttr.split(";").forEach((pair) => {
      const [attr, key] = pair.split(":");
      if (!(key in fa)) fa[key] = el.getAttribute(attr);
    });
  });
  fa.factSizeN = "حدود {n} مگابایت";
  fa.get = fa.get || "دریافت";

  function t(key) { return (lang === "en" ? en : fa)[key] ?? fa[key] ?? key; }

  const toggle = document.getElementById("lang-toggle");
  function setLang(next) {
    lang = next;
    const root = document.documentElement;
    root.lang = next;
    root.dir = next === "fa" ? "rtl" : "ltr";
    document.title = next === "fa" ? "ویسپار" : "Vispar";
    document.querySelectorAll("[data-i18n]").forEach((el) => {
      const value = t(el.dataset.i18n);
      if (value.includes("<")) el.innerHTML = value; else el.textContent = value;
    });
    document.querySelectorAll("[data-i18n-attr]").forEach((el) => {
      el.dataset.i18nAttr.split(";").forEach((pair) => {
        const [attr, key] = pair.split(":");
        el.setAttribute(attr, t(key));
      });
    });
    toggle.textContent = next === "fa" ? "EN" : "فا";
    toggle.lang = next === "fa" ? "en" : "fa";
    toggle.setAttribute("aria-label", next === "fa" ? "Read in English" : "نمایش به فارسی");
    renderDownloads();
    measure();
    applyMaterial(material);
    store.set("vispar-lang", next);
  }

  toggle.addEventListener("click", () => setLang(lang === "fa" ? "en" : "fa"));
  const saved = store.get("vispar-lang");
  // Persian is the default for everyone; many Persian speakers run English browsers.
  if (saved === "en") setLang("en");
})();
