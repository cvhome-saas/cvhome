import type {IconName} from '@cvhome-saas/ui-kit';

// Re-exported so the fifty-odd call sites that import `IconName` from here keep working; the union
// itself is declared in `@models/ui`, where a view model may name an icon without importing upward.
export type {IconName};

/**
 * The path for every glyph.
 *
 * Typed as a total map over `IconName` rather than inferred, so the two cannot drift: a name with
 * no path fails to compile here, and a path with no name fails at its `satisfies`.
 */
export const ICON_PATHS: Readonly<Record<IconName, string>> = {
  arrowRight: 'M4 12h16m-6-6 6 6-6 6',
  arrowLeft: 'M20 12H4m6-6-6 6 6 6',
  arrowUp: 'M12 19V5m-6 6 6-6 6 6',
  arrowDown: 'M12 5v14m6-6-6 6-6-6',
  arrowUpRight: 'M7 17 17 7M9 7h8v8',
  bell: 'M18 8a6 6 0 0 0-12 0c0 7-3 7-3 9h18c0-2-3-2-3-9m-7 13h2',
  box: 'M3 7.5 12 3l9 4.5-9 4.5L3 7.5Zm0 0V16l9 5 9-5V7.5M12 12v9',
  building: 'M4 21V5a2 2 0 0 1 2-2h8v18M2 21h20M8 7h2m-2 4h2m-2 4h2m8-5h2v11',
  calendar: 'M7 3v4m10-4v4M4 9h16M5 5h14a1 1 0 0 1 1 1v14H4V6a1 1 0 0 1 1-1Z',
  check: 'm4 12 5 5L20 6',
  chevronLeft: 'm15 18-6-6 6-6',
  chevronRight: 'm9 18 6-6-6-6',
  chevronDown: 'm6 9 6 6 6-6',
  clock: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm0-15v5l3 2',
  cog: 'M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Zm0-6v3m0 14v3M4.93 4.93l2.12 2.12m9.9 9.9 2.12 2.12M2 12h3m14 0h3M4.93 19.07l2.12-2.12m9.9-9.9 2.12-2.12',
  creditCard: 'M3 6h18v12H3V6Zm0 4h18',
  dollar: 'M12 2v20m5-15c-1-2-8-3-8 1 0 5 8 2 8 7 0 4-7 3-9 1',
  download: 'M12 3v12m-5-5 5 5 5-5M5 21h14',
  ellipsisH: 'M5 12h.01M12 12h.01M19 12h.01',
  ellipsisV: 'M12 5h.01M12 12h.01M12 19h.01',
  /* A receipt: a sheet with a torn bottom edge and three ruled lines. Reads as an invoice at
     12px, which a plain document outline does not. */
  receipt: 'M5 3h14v18l-3-2-3 2-3-2-3 2V3Zm3 5h8m-8 4h8m-8 4h4',
  fileEdit: 'M14 3H5a2 2 0 0 0-2 2v14h14v-9M13 14l7-7-3-3-7 7-1 4 4-1Z',
  globe: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm-9-10h18M12 2c3 3 3 17 0 20M12 2c-3 3-3 17 0 20',
  home: 'M3 11 12 3l9 8v10h-6v-6H9v6H3V11Z',
  list: 'M8 6h13M8 12h13M8 18h13M3 6h.01M3 12h.01M3 18h.01',
  menu: 'M4 6h16M4 12h16M4 18h16',
  minus: 'M5 12h14',
  panelLeftClose: 'M4 5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5Zm5-2v18M16 9l-3 3 3 3',
  panelLeftOpen: 'M4 5a2 2 0 0 1 2-2h12a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5Zm5-2v18M13 9l3 3-3 3',
  pin: 'M12 17v5M7 3h10l-2 7 3 4H6l3-4-2-7Z',
  plus: 'M12 5v14M5 12h14',
  search: 'M11 19a8 8 0 1 1 0-16 8 8 0 0 1 0 16Zm6-2 4 4',
  /* An eye, for revealing something that is deliberately hidden — a masked secret, a thumbnail
     worth seeing at full size. Paired with `eyeOff`, which is the same lid struck through. */
  eye: 'M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Zm10 3a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z',
  eyeOff: 'M10.7 6.2A9.6 9.6 0 0 1 12 5c6.4 0 10 7 10 7a17.6 17.6 0 0 1-3.2 3.9M6.4 6.4A17.6 17.6 0 0 0 2 12s3.6 7 10 7c1.4 0 2.7-.3 3.8-.8M3 3l18 18m-11.1-11.1a3 3 0 0 0 4.2 4.2',
  signOut: 'M10 17l5-5-5-5M15 12H3M21 3v18',
  shoppingCart: 'M4 4h2l2 11h10l2-7H7m2 13a1 1 0 1 0 0-2 1 1 0 0 0 0 2Zm9 0a1 1 0 1 0 0-2 1 1 0 0 0 0 2Z',
  sort: 'M8 7h11M8 12h8M8 17h5M4 7h.01M4 12h.01M4 17h.01',
  sparkles: 'M12 3l1.4 4.1L17.5 8l-4.1 1.4L12 13.5l-1.4-4.1L6.5 8l4.1-1.4L12 3Zm6 10 1 2.5 2.5 1-2.5 1-1 2.5-1-2.5-2.5-1 2.5-1 1-2.5ZM5 14l.8 2.2L8 17l-2.2.8L5 20l-.8-2.2L2 17l2.2-.8L5 14Z',
  undo: 'M9 7H4V2M5 7a8 8 0 1 1-1 8',
  user: 'M20 21a8 8 0 0 0-16 0M12 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z',
  userPlus: 'M16 21a6 6 0 0 0-12 0m6-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm9 0v6m3-3h-6',
  users: 'M17 21a5 5 0 0 0-10 0m5-10a4 4 0 1 0 0-8 4 4 0 0 0 0 8Zm8 10a4 4 0 0 0-4-4m3-10a3 3 0 0 0-3-3',
  x: 'M6 6l12 12M18 6 6 18',
  bolt: 'M13 2 4 14h7l-1 8 9-12h-7l1-8Z',
  envelope: 'M3 6h18v12H3V6Zm0 1 9 7 9-7',
  mapPin: 'M12 21s7-5.6 7-11a7 7 0 1 0-14 0c0 5.4 7 11 7 11Zm0-8.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Z',
  messageCircle: 'M21 11.5a8.4 8.4 0 0 1-9 8.4 9 9 0 0 1-4-.9L3 21l1.9-4.6A8.4 8.4 0 0 1 12 3.1a8.4 8.4 0 0 1 9 8.4Z',
  phone: 'M22 16.9v3a2 2 0 0 1-2.2 2 19.8 19.8 0 0 1-8.6-3.1 19.5 19.5 0 0 1-6-6A19.8 19.8 0 0 1 2.1 4.2 2 2 0 0 1 4.1 2h3a2 2 0 0 1 2 1.7c.1 1 .4 1.9.7 2.8a2 2 0 0 1-.5 2.1L8.1 9.9a16 16 0 0 0 6 6l1.3-1.2a2 2 0 0 1 2.1-.5c.9.3 1.8.6 2.8.7a2 2 0 0 1 1.7 2Z',
  star: 'm12 3 2.9 5.9 6.4.9-4.6 4.5 1.1 6.4-5.8-3-5.8 3 1.1-6.4L2.7 9.8l6.4-.9L12 3Z',
  alertCircle: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm0-14v5m0 3h.01',
  chartLine: 'M4 4v16h16M8 14l3.5-4 3 2.5L20 7',
  checkCircle: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm-4-10 3 3 5-5',
  info: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm0-9v5m0-8h.01',
  filter: 'M3 5h18l-7 8v6l-4 2v-8L3 5Z',
  printer: 'M7 8V3h10v5M7 18H4V8h16v10h-3M7 14h10v7H7v-7Z',
  send: 'M22 2 11 13M22 2l-7 20-4-9-9-4 20-7Z',
  truck: 'M3 6h11v10H3V6Zm11 4h4l3 3v3h-7v-6ZM7 19a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm10 0a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z',
  xCircle: 'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm-3-13 6 6m0-6-6 6',
  copy: 'M9 9h11v11H9V9ZM5 15H4V4h11v1',
  desktop: 'M3 5h18v11H3V5Zm6 15h6m-3-4v4',
  externalLink: 'M14 3h7v7M21 3l-9 9M19 14v6H4V5h6',
  file: 'M14 3H6a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h12V7l-4-4Zm0 0v4h4',
  grip: 'M9 5h.01M15 5h.01M9 12h.01M15 12h.01M9 19h.01M15 19h.01',
  server: 'M4 4h16v6H4V4Zm0 10h16v6H4v-6Zm3-7h.01M7 17h.01',
  database: 'M4 6c0-1.7 3.6-3 8-3s8 1.3 8 3-3.6 3-8 3-8-1.3-8-3Zm0 0v12c0 1.7 3.6 3 8 3s8-1.3 8-3V6M4 12c0 1.7 3.6 3 8 3s8-1.3 8-3',
  percent: 'M19 5 5 19M7 7a2 2 0 1 0 0-4 2 2 0 0 0 0 4Zm10 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z',
  images: 'M8 3h13v13H8V3Zm-5 5v13h13m-3.5-16a1 1 0 1 0 0 2 1 1 0 0 0 0-2Zm8.5 12-4.5-5-4 4',
  layoutGrid: 'M4 4h7v7H4V4Zm9 0h7v7h-7V4ZM4 13h7v7H4v-7Zm9 0h7v7h-7v-7Z',
  // A root box with two branches dropping to two leaves — the catalogue's hierarchy, as the
  // nav rail's one-glyph statement of what the page is.
  // A price tag with its hole — what a product type is, and what the design's own `pi pi-tags` drew.
  tag: 'M3 12V5a2 2 0 0 1 2-2h7l9 9-9 9-9-9Zm4.5-5.5h.01',
  // A ribbon bookmark. Brands are marks you recognise, not favourites you starred.
  bookmark: 'M6 3h12v18l-6-4.5L6 21V3Z',
  sitemap: 'M9 3h6v4H9V3ZM2 17h6v4H2v-4Zm14 0h6v4h-6v-4ZM12 7v4M5 17v-3h14v3',

  // ---- Rich text toolbar -------------------------------------------------------------------
  // Drawn as letterforms rather than glyphs, because that is what every editor's toolbar uses and
  // what an operator recognises without a tooltip.
  bold: 'M7 5h6.5a3.5 3.5 0 0 1 0 7H7V5Zm0 7h7.5a3.5 3.5 0 0 1 0 7H7v-7Z',
  italic: 'M15 5H9m6 0-4 14m4-14h-4M9 19h6',
  heading2: 'M4 6v12M4 12h7m0-6v12m4-6.5a2.5 2.5 0 1 1 5 0c0 1.7-5 3.2-5 5.5h5',
  heading3: 'M4 6v12M4 12h7m0-6v12m4-5.5h2a2 2 0 1 0-2-2m0 4h2a2 2 0 1 1-2 2',
  listBullet: 'M9 6h11M9 12h11M9 18h11M4.5 6h.01M4.5 12h.01M4.5 18h.01',
  listOrdered: 'M10 6h10M10 12h10M10 18h10M4 6h2m-1 0v4m-1 0h2M4 15.5a1.5 1.5 0 1 1 2.6 1L4 20h3',
  eraser: 'M8 20h12M4.5 15.5 9 20l10.5-10.5a2.1 2.1 0 0 0 0-3L16 3l-11.5 11.5a.7.7 0 0 0 0 1Z',
  code: 'M9 17 4 12l5-5m6 10 5-5-5-5',
  link: 'M9.5 14.5 14.5 9.5M10 6l1-1a4 4 0 0 1 6 6l-1 1m-2 4-1 1a4 4 0 0 1-6-6l1-1',
  lock: 'M6 11h12v10H6V11Zm3 0V7a3 3 0 0 1 6 0v4',
  palette:
    'M12 21a9 9 0 1 1 0-18c4.4 0 8 2.9 8 6.4 0 2.4-1.9 3.6-4 3.6h-1.4a1.8 1.8 0 0 0-1 3.3A2 2 0 0 1 12 21Zm-4.5-9h.01M9 7.5h.01M13 6.5h.01',
  pencil: 'M4 20h4L19 9l-4-4L4 16v4Zm11-11 4 4',
  /** Closed so it reads as a solid triangle under [filled]. */
  play: 'M8 5.5v13l11-6.5-11-6.5Z',
  questionCircle:
    'M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Zm-2.1-13.1A2.1 2.1 0 1 1 12 11c-.6.3-.7.8-.7 1.4v.6m0 3h.01',
  share:
    'M18 8a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM6 14.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5ZM18 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Zm-2.2-11.3-7.6 3.6m0 2.4 7.6 3.6',
  shield: 'M12 21c5-2 8-6 8-11V5l-8-3-8 3v5c0 5 3 9 8 11Z',
  signIn: 'M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4M10 17l5-5-5-5M15 12H3',
  trash: 'M4 7h16M10 4h4M6 7l1 13h10l1-13M10 11v6m4-6v6',
  upload: 'M12 15V3m-5 5 5-5 5 5M5 21h14',

  /*
   * Provider marks. Drawn in the same 24×24 stroke idiom as the rest rather than shipped as
   * brand SVGs, so they inherit `currentColor` and stay legible in every theme.
   */
  facebook: 'M14 8h3.5V4H14a4 4 0 0 0-4 4v2H7v4h3v8h4v-8h3l.5-4H14V8Z',
  github:
    'M9 21v-3.1c-2.9.6-3.5-1.4-4.5-2.4M15 21v-3.9c0-1 .1-1.4-.5-2 2.4-.3 4.5-1.2 4.5-5.2a4 4 0 0 0-1.1-2.8c.1-.3.4-1.4-.1-2.9 0 0-1.2-.4-3.8 1.4a9.4 9.4 0 0 0-5 0C6.4 3.8 5.2 4.2 5.2 4.2c-.5 1.5-.2 2.6-.1 2.9A4 4 0 0 0 4 9.9c0 4 2.1 4.9 4.5 5.2-.6.6-.6 1.2-.5 2V21',
  google: 'M20.5 12.2H12v3.2h5A5 5 0 1 1 15.4 9.8l2.4-2.4A9 9 0 1 0 21 12.2h-.5Z',
  instagram:
    'M7 3h10a4 4 0 0 1 4 4v10a4 4 0 0 1-4 4H7a4 4 0 0 1-4-4V7a4 4 0 0 1 4-4Zm5 5.5a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7ZM17.2 6.8h.01',
  tiktok: 'M14 4v9.6a3.6 3.6 0 1 1-3.6-3.6M14 4c.5 2.6 2.1 4.2 4.6 4.4',
  /** X, the network. `x` is already the close glyph. */
  xSocial: 'M4 3h4l12 18h-4L4 3Zm0 18 7-8m2-2.3L20 3',
};
