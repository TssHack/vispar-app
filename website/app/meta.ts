import type { Metadata, Viewport } from "next";
import { dictionary, type Locale } from "@/content/dictionary";

const basePath = process.env.NEXT_PUBLIC_BASE_PATH ?? "";
const site = process.env.NEXT_PUBLIC_SITE_URL ?? "https://tsshack.github.io/vispar-app";

export function pageMetadata(locale: Locale): Metadata {
  const t = dictionary[locale];
  return {
    metadataBase: new URL(site.endsWith("/") ? site : `${site}/`),
    title: t.meta.title,
    description: t.meta.description,
    alternates: { canonical: locale === "fa" ? "./" : "./en/", languages: { fa: "./", en: "./en/" } },
    icons: { icon: `${basePath}/favicon-32.png`, apple: `${basePath}/icon-180.png` },
    openGraph: {
      type: "website",
      title: t.meta.title,
      description: t.meta.description,
      locale: locale === "fa" ? "fa_IR" : "en_US",
      images: [{ url: "og.png", width: 1200, height: 630 }],
    },
    twitter: { card: "summary_large_image" },
  };
}

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  viewportFit: "cover",
  colorScheme: "dark light",
  themeColor: [
    { media: "(prefers-color-scheme: dark)", color: "#050506" },
    { media: "(prefers-color-scheme: light)", color: "#F2F2F7" },
  ],
};

export function jsonLd() {
  return {
    "@context": "https://schema.org",
    "@type": "SoftwareApplication",
    name: "Vispar",
    alternateName: "ویسپار",
    operatingSystem: "Android 7.0+",
    applicationCategory: "EntertainmentApplication",
    offers: { "@type": "Offer", price: "0", priceCurrency: "USD" },
    downloadUrl: "https://github.com/TssHack/vispar-app/releases/latest",
    author: { "@type": "Person", name: "Ehsan Fazli", url: "https://ehsanfazli.ir" },
    license: "https://opensource.org/licenses/MIT",
  };
}
