import Image from "next/image";
import Link from "next/link";
import bolt from "@/assets/bolt.png";
import { dictionary, type Locale } from "@/content/dictionary";
import { Downloads, HeroDownload } from "./Downloads";
import { GlassShowcase } from "./GlassShowcase";
import { HeroTitle } from "./HeroTitle";
import { icons, type IconName } from "./icons";
import { TabBarDemo } from "./TabBarDemo";
import { TvRemote } from "./TvRemote";

export function Landing({ locale }: { locale: Locale }) {
  const t = dictionary[locale];
  const home = locale === "fa" ? "/" : "/en/";

  return (
    <>
      <a className="skip" href="#main">{t.skip}</a>

      <header className="nav-wrap">
        <nav className="nav" aria-label={t.nav.label}>
          <Link className="brand" href={home}>
            <Image src={bolt} alt="" width={28} height={28} priority />
            <span>{t.brand}</span>
          </Link>
          <ul className="nav-links">
            <li><a href="#glass">{t.nav.glass}</a></li>
            <li><a href="#features">{t.nav.features}</a></li>
            <li><a href="#tv">{t.nav.tv}</a></li>
          </ul>
          <div className="nav-end">
            <Link className="lang" href={t.nav.otherHref} hrefLang={locale === "fa" ? "en" : "fa"} lang={locale === "fa" ? "en" : "fa"}>
              {t.nav.other}
            </Link>
            <a className="btn btn-small" href="#download">{t.nav.download}</a>
          </div>
        </nav>
      </header>

      <main id="main">
        <section className="hero" aria-labelledby="hero-title">
          <div className="wrap">
            <div className="hero-grid">
              <HeroTitle lines={t.hero.title} />
              <div className="lead-block">
                <p className="lead">{t.hero.lead}</p>
                <HeroDownload t={t} locale={locale} />
              </div>
            </div>
            <GlassShowcase t={t} />
          </div>
        </section>

        <section className="section" aria-labelledby="tabs-title">
          <div className="wrap tabs-grid">
            <div className="section-head">
              <h2 id="tabs-title">{t.tabs.title}</h2>
              <p>{t.tabs.body}</p>
            </div>
            <TabBarDemo t={t} />
          </div>
        </section>

        <section className="section" id="features" aria-labelledby="features-title">
          <div className="wrap features-grid">
            <div className="section-head">
              <h2 id="features-title">{t.features.title}</h2>
              <p>{t.features.lead}</p>
            </div>
            <ul className="feature-list">
              {t.features.items.map((f) => {
                const Icon = icons[f.icon as IconName];
                return (
                  <li key={f.t}>
                    <span className="feature-icon"><Icon /></span>
                    <div>
                      <h3>{f.t}</h3>
                      <p>{f.b}</p>
                    </div>
                  </li>
                );
              })}
            </ul>
          </div>
        </section>

        <section className="section" id="tv" aria-labelledby="tv-title">
          <div className="wrap"><TvRemote t={t} /></div>
        </section>

        <section className="section" id="download" aria-labelledby="download-title">
          <div className="wrap"><Downloads t={t} locale={locale} /></div>
        </section>
      </main>

      <footer className="footer">
        <div className="wrap footer-grid">
          <div className="footer-brand">
            <Image src={bolt} alt="" width={40} height={40} />
            <p>{t.footer.made} <a href="https://ehsanfazli.ir">{t.footer.author}</a>. {t.footer.license}</p>
          </div>
          <ul className="footer-links">
            <li><a href="https://github.com/TssHack/vispar-app">{t.footer.source}</a></li>
            <li><a href="https://github.com/TssHack/vispar">{t.footer.cross}</a></li>
            <li><a href="https://github.com/TssHack/vispar-app/issues">{t.footer.issues}</a></li>
            <li><a href="https://github.com/TssHack" lang="en" dir="ltr">@TssHack</a></li>
          </ul>
        </div>
      </footer>
    </>
  );
}
