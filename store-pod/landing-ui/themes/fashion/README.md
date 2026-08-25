# fashion theme — The Wheatpaste Wall

Streetwear / drops storefront. The page is a fly-posted wall: a rendered ground (the merchant's background
mixed toward its foreground, with grain) under pasted paper sheets — every product and every slider image is
a poster with its own small tilt and an offset shadow; the merchant's PRIMARY is the day-glo paper stock that
carries every primary action (SHOP NOW, add, cart stub, checkout, selected option); state is a rubber stamp
(SALE, SOLD OUT, ONLY N LEFT, ADDED), never a tint. Type: Anton (poster caps, Latin) + Changa 800 (Arabic
display) + Rubik (body, Latin/Cyrillic/Arabic).

- Direction contract: the comment at the top of `src/layout/Root.tsx`; decision record `.impeccable/decision-fashion.json`.
- Design system: `DESIGN.md` (written by the impeccable documenter from the built theme).
- World grammar lives in `src/tokens.css` inside `@layer components` (`.sheet`, `.strip`, `.glo`, `.stamp`,
  `.peel`, `.wall`, `.typo-poster`) so Tailwind utilities on the same element still win.
- Built with the impeccable flow (direction roll seed `569a4b15`, code-led). See `themes/README.md` and
  `.agents/skills/project-structure/references/new-landing-ui-template.md`.
