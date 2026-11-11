import type { ReactNode } from "react";
import "@/app/globals.css";
import { vazir } from "@/app/font";
import { jsonLd, pageMetadata, viewport } from "@/app/meta";

export const metadata = pageMetadata("en");
export { viewport };

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <html lang="en" dir="ltr" className={vazir.variable}>
      <body>
        {children}
        <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd()) }} />
      </body>
    </html>
  );
}
