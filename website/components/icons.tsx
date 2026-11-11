import type { SVGProps } from "react";

const base = { viewBox: "0 0 24 24", "aria-hidden": true } as const;

export const icons = {
  play: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><rect x="3" y="5" width="18" height="14" rx="3" /><path d="m10 9.5 4.5 2.5-4.5 2.5z" /></svg>),
  resume: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><path d="M4 12a8 8 0 1 0 2.4-5.7M4 4v4h4" /><path d="M12 8v4l2.5 2" /></svg>),
  devices: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><rect x="3" y="4" width="13" height="9" rx="2" /><rect x="15" y="9" width="6" height="11" rx="1.5" /><path d="M6 17h6" /></svg>),
  heart: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><path d="M12 20s-7-4.4-7-10a4 4 0 0 1 7-2.6A4 4 0 0 1 19 10c0 5.6-7 10-7 10z" /></svg>),
  download: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><path d="M12 4v11m0 0-4.5-4.5M12 15l4.5-4.5M5 19.5h14" /></svg>),
  movie: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><rect x="3" y="4" width="18" height="16" rx="3" /><path d="M3 9h18M8 4l2 5M14 4l2 5" /></svg>),
  series: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><rect x="3" y="6" width="18" height="13" rx="3" /><path d="m8 2 4 4 4-4" /></svg>),
  settings: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><circle cx="12" cy="12" r="3" /><path d="M19.4 15a1.6 1.6 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.6 1.6 0 0 0-1.8-.3 1.6 1.6 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.6 1.6 0 0 0-1-1.5 1.6 1.6 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.6 1.6 0 0 0 .3-1.8 1.6 1.6 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1a1.6 1.6 0 0 0 1.5-1 1.6 1.6 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.6 1.6 0 0 0 1.8.3H9a1.6 1.6 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.6 1.6 0 0 0 1 1.5 1.6 1.6 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.6 1.6 0 0 0-.3 1.8V9a1.6 1.6 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1a1.6 1.6 0 0 0-1.5 1z" /></svg>),
  search: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><circle cx="11" cy="11" r="6.5" /><path d="m16 16 4 4" /></svg>),
  drag: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><path d="M12 3v18M3 12h18M12 3l-2.5 2.5M12 3l2.5 2.5M12 21l-2.5-2.5M12 21l2.5-2.5M3 12l2.5-2.5M3 12l2.5 2.5M21 12l-2.5-2.5M21 12l-2.5 2.5" /></svg>),
  back: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><path d="M9 7 4 12l5 5M4 12h11a5 5 0 0 1 0 10h-2" /></svg>),
  media: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><path d="m6 5 7 7-7 7zM15 5v14M19 5v14" /></svg>),
  tri: (p: SVGProps<SVGSVGElement>) => (<svg {...base} {...p}><path d="M12 6 19 17H5z" /></svg>),
};

export type IconName = keyof typeof icons;
