# Vispar design

The website (`website/`, Next.js, static export) follows the app: Apple-style monochrome, Persian first, and liquid glass used in one place only, where it is the product itself.

## Direction

- **Subject:** a free Android and Android TV app for watching movies and series, whose interface is drawn as liquid glass.
- **Audience:** Persian-speaking Android and TV-box owners, plus developers who arrive from GitHub.
- **Page job:** let people see what the glass looks like, then download the right APK.
- **The one bold thing:** a glass lens in the hero that bends the wordmark behind it. Visitors can drag it and switch its material, the same four materials the app offers in Settings. Glass appears in only two other places, both copied from the app: the navigation capsule and the interactive tab-bar demo.

## Color

Dark first, because the app's icon ground and player are black. Light mode uses Apple's system grey.

| Token | Dark | Light | Role |
| --- | --- | --- | --- |
| `--ground` | `#050506` | `#F2F2F7` | Page background; `#050506` is the launcher icon's background |
| `--raised` | `#141416` | `#FFFFFF` | Download panel, lens stage |
| `--ink` | `#F5F5F7` | `#111113` | Headings and body text |
| `--ink-2` | `#A1A1A6` | `#55555C` | Secondary text |
| `--line` | `#2A2A2E` | `#D8D8DE` | Hairlines and outlines |
| `--action` | `#F5F5F7` | `#111113` | Primary button fill; the app's monochrome accent |
| `--on-action` | `#050506` | `#FFFFFF` | Text on primary buttons |
| `--gold` | `#FFD60A` | `#B07800` | The app's rating gold; only the version tag and focus ring |

Contrast: `--ink-2` on `--ground` is 7.6:1 dark and 7.4:1 light.

## Typography

One family, Vazirmatn (the Google Fonts release of Vazir, the face the app ships), for Persian and Latin.

| Step | Size | Line height | Weight | Use |
| --- | --- | --- | --- | --- |
| Wordmark | `clamp(7rem, 26vw, 22rem)` | 0.9 | 900 | Stage wordmark only |
| Display | `clamp(2.4rem, 6vw, 4.5rem)` | 1.15 | 800 | Page headline |
| Title | `clamp(1.6rem, 3.4vw, 2.4rem)` | 1.25 | 800 | Section headings |
| Lead | `1.25rem` | 1.9 | 400 | Text under headings |
| Body | `1.0625rem` | 1.95 | 400 | Running text |
| Small | `0.875rem` | 1.7 | 500 | Notes, file sizes |

Persian needs taller lines than Latin; body stays near 65 characters wide.

## Spacing, shape and depth

- 4px base: 4, 8, 12, 16, 24, 32, 48, 72, 112.
- Content width 1120px; sections separated by 112px (72px on phones).
- Radius: 999px for capsules (buttons, nav, lens), 28px for the two large panels. Nothing else is rounded.
- No drop shadows. Depth comes from the glass lens and from `--raised` against `--ground`.

## Components

- **Primary button:** `--action` fill, `--on-action` text, capsule. Hover lifts the fill 6% toward the ground; focus shows a 3px `--gold` ring.
- **Secondary button:** outline in `--line`, `--ink` text.
- **Navigation:** a floating glass capsule like the app's tab bar.
- **Segmented control:** the material switch; selected segment uses `--action`.
- **Keys:** remote buttons drawn as round outlined keys.
- **Download table:** edition tabs above one table of architectures; file names in a code face.

## Imagery and icons

- The 3D bolt from `app/src/main/res/drawable-nodpi/splash_logo.png` is the only illustration.
- Icons are inline SVG strokes at 1.75px, drawn for this page.
- No app screenshots yet. The tab-bar demo uses plain poster blocks, never invented titles or artwork; add a screens section when real screenshots exist.

## Motion

- On load the two headline lines rise out of a blur and the lens stage settles in; that is the only load animation.
- The lens drifts slowly until someone touches it. Material changes animate over 400ms; segmented pills and the tab-bar drop move on a spring.
- With reduced motion, everything appears in place and nothing drifts.
