export type Locale = "fa" | "en";

export const release = {
  repo: "TssHack/vispar-app",
  version: "1.0.0",
  // Sizes of the v1.0.0 assets in bytes; the page refreshes them from the latest release.
  bytes: {
    universal: 3672306,
    "arm64-v8a": 3622233,
    "armeabi-v7a": 3619391,
    x86_64: 3622894,
    x86: 3621415,
  },
} as const;

export type Abi = keyof typeof release.bytes;
export const abis: Abi[] = ["universal", "arm64-v8a", "armeabi-v7a", "x86_64", "x86"];
export type Edition = "android" | "android-tv";
export type Material = "clear" | "glass" | "frosted" | "none";

export const dictionary = {
  fa: {
    meta: {
      title: "ویسپار",
      description:
        "ویسپار، برنامهٔ رایگان فیلم و سریال برای اندروید و Android TV، با رابط شیشهٔ مایع. فارسی، راست‌به‌چپ، حدود ۳٫۷ مگابایت.",
    },
    skip: "رفتن به محتوا",
    brand: "ویسپار",
    nav: { label: "اصلی", glass: "شیشه", features: "امکانات", tv: "تلویزیون", download: "دانلود", other: "English", otherHref: "/en/" },
    hero: {
      title: ["فیلم و سریال،", "پشت شیشهٔ مایع."],
      lead: "ویسپار برنامهٔ رایگان اندروید و Android TV است. هر پنل هرچه پشتش هست را محو می‌کند و می‌شکند؛ حتی کنترل‌های پخش، تصویر فیلمی را که در حال پخش است.",
      cta: "دانلود برای اندروید",
      ctaTv: "نسخهٔ تلویزیون",
      facts: { version: "نسخهٔ", size: "حدود {n} مگابایت", android: "اندروید ۷ به بالا", free: "رایگان و متن‌باز" },
      wordmark: "ویسپار",
      lensLabel: "عدسی شیشه‌ای. با کشیدن یا کلیدهای جهت جابه‌جایش کنید.",
      hint: "عدسی را بکشید",
    },
    glass: {
      title: "جنس شیشه را خودتان انتخاب کنید.",
      body: "همان چهار گزینهٔ تنظیمات برنامه، روی عدسی بالا. «بدون شفافیت» سریع‌ترین حالت است و روی تلویزیون پیش‌فرض است.",
      note: "محوشدگی کامل از اندروید ۱۲ و شکست نور از اندروید ۱۳. دستگاه‌های قدیمی‌تر یک لایهٔ مات می‌بینند.",
      materials: { clear: "شفاف", glass: "شیشه", frosted: "مات", none: "بدون شفافیت" } as Record<Material, string>,
    },
    tabs: {
      title: "نوار پایین، مثل یک قطره.",
      body: "سه برگه در یک کپسول شیشه‌ای و جست‌وجو در دکمهٔ گرد کنارش. عدسی بین برگه‌ها کش می‌آید و جا می‌افتد. همین‌جا امتحانش کنید.",
      items: ["فیلم", "سریال", "تنظیمات"],
      search: "جست‌وجو",
      screens: [
        { title: "فیلم", line: "تازه‌ترین فیلم‌های جهان، یک لمس فاصله" },
        { title: "سریال", line: "فصل به فصل، قسمت به قسمت" },
        { title: "تنظیمات", line: "ظاهر و پخش را آن‌طور که دوست دارید بچینید" },
        { title: "جست‌وجو", line: "فیلم یا سریالی را که می‌خواهید پیدا کنید" },
      ],
    },
    features: {
      title: "هرچه برای تماشا لازم است، و نه بیشتر.",
      lead: "فارسی پیش‌فرض است و چیدمان از زبان پیروی می‌کند. انگلیسی با یک لمس فعال می‌شود.",
      items: [
        { icon: "play", t: "پخش‌کننده", b: "بر پایهٔ Media3 ExoPlayer با HLS و DASH. سرعت از ۰٫۷۵ تا ۲ برابر، بازهٔ پرش دلخواه، پخش خودکار و زیرنویس با رنگ و اندازهٔ دلخواه." },
        { icon: "resume", t: "ادامهٔ تماشا", b: "ردیفی در برگه‌های فیلم و سریال که از همان ثانیه‌ای که متوقف کردید ادامه می‌دهد." },
        { icon: "devices", t: "یک حساب، گوشی و تلویزیون", b: "ورود با شمارهٔ موبایل و کد پیامکی، بدون رمز. علاقه‌مندی‌ها و ادامهٔ تماشا روی همهٔ دستگاه‌ها یکی است و دستگاه‌های دیگر را از راه دور خارج می‌کنید." },
        { icon: "heart", t: "علاقه‌مندی‌ها", b: "فیلم و سریال را نگه دارید، تک‌تک حذف کنید یا کل فهرست را پاک کنید." },
        { icon: "download", t: "دانلود", b: "کیفیت را انتخاب کنید و فایل را به مرورگر، ADM یا VLC بسپارید." },
      ],
    },
    tv: {
      title: "روی تلویزیون، با همان ریموت.",
      body: "نسخهٔ تلویزیون نوار کناری شیشه‌ای و حلقهٔ فوکوس روی هر دکمه دارد، و همیشه رابط تلویزیون را نشان می‌دهد؛ حتی روی باکس‌هایی که خودشان را تبلت معرفی می‌کنند.",
      caption: "کلیدهای ریموت در پخش‌کننده",
      keys: [
        { key: "ok", k: "OK", d: "پخش یا توقف" },
        { key: "lr", k: "چپ و راست", d: "پرش به عقب و جلو" },
        { key: "ud", k: "بالا و پایین", d: "نمایش کنترل‌ها" },
        { key: "back", k: "بازگشت", d: "بستن پخش‌کننده" },
        { key: "media", k: "کلیدهای رسانه", d: "پخش، توقف، پرش" },
      ],
    },
    download: {
      title: "دانلود",
      lead: "مطمئن نیستید کدام؟ «همه‌کاره» روی همهٔ دستگاه‌ها نصب می‌شود. هر دو نسخه یک امضا دارند، پس هرکدام روی دیگری نصب می‌شود.",
      editionLabel: "نسخه",
      editions: { android: "گوشی و تبلت", "android-tv": "تلویزیون" } as Record<Edition, string>,
      caption: "فایل‌های نصبی بر اساس معماری پردازنده",
      cols: { arch: "معماری", for: "مناسب برای", size: "حجم", get: "دانلود" },
      universal: "همه‌کاره",
      for: {
        universal: "همهٔ دستگاه‌ها. اگر مطمئن نیستید، همین.",
        "arm64-v8a": "بیشتر گوشی‌های چند سال اخیر، تلویزیون‌ها و باکس‌های جدید",
        "armeabi-v7a": "گوشی‌های قدیمی‌تر و بسیاری از باکس‌های تلویزیون",
        x86_64: "شبیه‌سازها و دستگاه‌های اینتل ۶۴ بیتی",
        x86: "دستگاه‌های قدیمی اینتل ۳۲ بیتی",
      } as Record<Abi, string>,
      get: "دریافت",
      mb: "{n} مگابایت",
      version: "نسخهٔ",
      sums: "چک‌سام SHA-256",
      all: "همهٔ انتشارها",
    },
    footer: {
      made: "ساختهٔ",
      author: "احسان فضلی",
      license: "متن‌باز با مجوز MIT.",
      source: "کد منبع",
      cross: "ویسپار برای iOS، وب و دسکتاپ",
      issues: "گزارش مشکل",
    },
  },
  en: {
    meta: {
      title: "Vispar",
      description: "Vispar is a free movie and series app for Android and Android TV, drawn as liquid glass. Persian first, about 3.7 MB.",
    },
    skip: "Skip to content",
    brand: "Vispar",
    nav: { label: "Main", glass: "Glass", features: "Features", tv: "TV", download: "Download", other: "فارسی", otherHref: "/" },
    hero: {
      title: ["Movies and series,", "behind liquid glass."],
      lead: "Vispar is a free app for Android and Android TV. Every panel blurs and bends whatever is behind it, even the player controls, which refract the film that is playing.",
      cta: "Download for Android",
      ctaTv: "TV edition",
      facts: { version: "Version", size: "About {n} MB", android: "Android 7.0 and newer", free: "Free and open source" },
      wordmark: "Vispar",
      lensLabel: "Glass lens. Drag it, or move it with the arrow keys.",
      hint: "Drag the lens",
    },
    glass: {
      title: "Pick the glass yourself.",
      body: "The same four options as the app's Settings, on the lens above. No transparency is the fastest and the default on TV.",
      note: "Full blur needs Android 12 and refraction Android 13. Older devices show a frosted tint.",
      materials: { clear: "Clear", glass: "Glass", frosted: "Frosted", none: "No transparency" } as Record<Material, string>,
    },
    tabs: {
      title: "A tab bar like a drop of water.",
      body: "Three tabs in one glass capsule, with search in the round button beside it. The lens stretches between tabs and settles. Try it here.",
      items: ["Movies", "Series", "Settings"],
      search: "Search",
      screens: [
        { title: "Movies", line: "The world's newest films, one tap away" },
        { title: "Series", line: "Season by season, episode by episode" },
        { title: "Settings", line: "Make it look and play your way" },
        { title: "Search", line: "Find the film or series you want" },
      ],
    },
    features: {
      title: "What you need to watch, and nothing more.",
      lead: "Persian is the default and the layout follows the language. English is one tap away.",
      items: [
        { icon: "play", t: "Player", b: "Built on Media3 ExoPlayer with HLS and DASH. Speed from 0.75x to 2x, your own skip interval, autoplay, and subtitles in the colour and size you choose." },
        { icon: "resume", t: "Continue watching", b: "A row on the Movies and Series tabs that picks up at the second you stopped." },
        { icon: "devices", t: "One account, phone and TV", b: "Sign in with your mobile number and an SMS code, no password. Favourites and Continue Watching match on every device, and you can sign other devices out remotely." },
        { icon: "heart", t: "Favourites", b: "Keep films and series, remove them one by one, or clear the list." },
        { icon: "download", t: "Downloads", b: "Pick a quality and hand the file to your browser, ADM or VLC." },
      ],
    },
    tv: {
      title: "On the TV, with the remote you have.",
      body: "The TV edition has a glass sidebar and a focus ring on every control, and always uses the TV interface, even on boxes that say they are tablets.",
      caption: "Remote keys in the player",
      keys: [
        { key: "ok", k: "OK", d: "Play or pause" },
        { key: "lr", k: "Left and right", d: "Skip back and forward" },
        { key: "ud", k: "Up and down", d: "Show the controls" },
        { key: "back", k: "Back", d: "Close the player" },
        { key: "media", k: "Media keys", d: "Play, pause, skip" },
      ],
    },
    download: {
      title: "Download",
      lead: "Not sure which? Universal installs on every device. Both editions share one signature, so either installs over the other.",
      editionLabel: "Edition",
      editions: { android: "Phone and tablet", "android-tv": "TV" } as Record<Edition, string>,
      caption: "Installers by processor architecture",
      cols: { arch: "Architecture", for: "For", size: "Size", get: "Download" },
      universal: "Universal",
      for: {
        universal: "Every device. If you are not sure, take this one.",
        "arm64-v8a": "Most phones from recent years, newer TVs and boxes",
        "armeabi-v7a": "Older phones and many TV boxes",
        x86_64: "Emulators and 64-bit Intel devices",
        x86: "Older 32-bit Intel devices",
      } as Record<Abi, string>,
      get: "Download",
      mb: "{n} MB",
      version: "Version",
      sums: "SHA-256 checksums",
      all: "All releases",
    },
    footer: {
      made: "Made by",
      author: "Ehsan Fazli",
      license: "Open source under the MIT license.",
      source: "Source code",
      cross: "Vispar for iOS, web and desktop",
      issues: "Report a problem",
    },
  },
};

export type Dictionary = (typeof dictionary)["fa"];

const faDigits = "۰۱۲۳۴۵۶۷۸۹";
export function localNumber(value: string, locale: Locale): string {
  if (locale === "en") return value;
  return value.replace(/\d/g, (d) => faDigits[Number(d)]).replace(/\./g, "٫");
}

export function megabytes(bytes: number, locale: Locale): string {
  return localNumber((bytes / 1e6).toFixed(1), locale);
}

export function assetUrl(version: string, name: string): string {
  return `https://github.com/${release.repo}/releases/download/v${version}/${name}`;
}

export function apkName(version: string, edition: Edition, abi: Abi): string {
  return `Vispar-${version}-${edition}-${abi}.apk`;
}
