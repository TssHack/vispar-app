import { Vazirmatn } from "next/font/google";

// Vazirmatn is the Google Fonts release of Vazir, the face the app ships.
export const vazir = Vazirmatn({
  subsets: ["arabic", "latin"],
  weight: ["400", "500", "700", "800", "900"],
  display: "swap",
  variable: "--font-vazir",
});
