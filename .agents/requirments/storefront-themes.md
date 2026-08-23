I need you to **completely rethink and redesign the current theme system and its architecture**.

The existing themes are not acceptable. They feel visually weak, generic, inconsistent, and do not look like production-quality modern e-commerce storefronts. Do **not** simply polish the existing designs or make small CSS changes. Treat this as a fresh design and architecture exercise.

## Goal

Design a **professional, modern, reusable e-commerce theme system** suitable for a multi-tenant SaaS platform where different stores can have significantly different visual identities while sharing the same underlying platform.

I want the themes to look like they were designed by experienced product/UI designers, not like variations of the same template with different colors.

## 1. Review the Current System First

Before changing anything:

* Inspect the existing theme architecture and implementation.
* Understand how themes, layouts, components, design tokens, colors, typography, and configuration currently work.
* Identify architectural problems and unnecessary coupling.
* Identify why the existing themes look too similar or generic.
* Identify components that are difficult to customize or reuse.
* Identify duplicated CSS/components across themes.
* Identify anything that prevents us from creating substantially different storefront designs.

Do not preserve an existing abstraction merely because it already exists.

## 2. Propose a Better Theme Architecture

Design an architecture that clearly separates:

* shared storefront/domain functionality
* reusable UI primitives
* reusable e-commerce components
* theme-specific components
* page layouts
* sections
* design tokens
* typography
* spacing
* responsive behavior
* theme configuration
* merchant customization

The architecture should make it possible to create a new theme without copying an entire application.

A theme should be able to control much more than colors.

It should be able to define its own:

* header/navigation structure
* footer
* product cards
* product grids
* category presentation
* hero sections
* banners
* typography
* spacing/density
* border radius
* buttons
* imagery style
* product-detail layout
* category/listing layout
* search experience
* cart presentation
* content sections
* responsive behavior

At the same time, business logic should remain shared wherever practical.

## 3. Create New Themes

Replace/rework the existing weak designs with multiple **genuinely distinct themes**.

Do not create:

> Theme A = blue
> Theme B = green
> Theme C = black

I want meaningful visual and structural differences.

For example, explore directions such as:

* premium / luxury
* clean modern retail
* bold fashion/editorial
* minimal Scandinavian
* technology/electronics
* lifestyle/home
* high-density marketplace

You don't have to use exactly these categories, but every theme must have a clear design concept and target merchant/customer.

Each theme should feel like a different professionally designed storefront.

## 4. Design Complete Storefront Experiences

Do not focus only on the homepage.

Consider the complete customer journey:

Home → Category → Search → Product Listing → Product Details → Cart → Checkout

Also consider:

* empty states
* loading states
* errors
* promotions
* sale badges
* unavailable products
* product variants
* mobile navigation
* filters
* sorting
* breadcrumbs
* related products
* recommendations

The experience must be coherent throughout the entire storefront.

## 5. Responsive Design

Mobile must be treated as a first-class experience.

Design and verify:

* desktop
* tablet
* mobile

Do not simply shrink desktop components.

Navigation, product grids, filters, product details, cart interactions, spacing, and typography should adapt appropriately.

## 6. Design System

Establish a clear token system for things such as:

* colors
* typography
* font scale
* spacing
* radius
* borders
* shadows
* container widths
* breakpoints
* transitions

Avoid random hard-coded values scattered throughout theme CSS.

However, do not over-engineer the design system to the point where every theme becomes visually identical.

Shared tokens should provide structure while theme-specific tokens and components provide personality.

## 7. Quality Bar

The final result should feel comparable to a professionally designed commercial e-commerce storefront.

Avoid:

* generic AI-looking layouts
* excessive cards
* unnecessary borders around everything
* excessive gradients
* random shadows
* oversized rounded corners everywhere
* identical layouts across themes
* arbitrary spacing
* inconsistent typography
* duplicated components
* giant monolithic theme components
* CSS hacks

Prioritize:

* strong visual hierarchy
* typography
* whitespace
* composition
* product imagery
* consistency
* accessibility
* responsiveness
* maintainability
* performance

## 8. Work in Phases

Do not immediately rewrite the whole project.

First:

1. Analyze the existing implementation.
2. Document the major design and architecture problems.
3. Propose the new architecture.
4. Define what should be shared versus theme-specific.
5. Propose several distinct theme directions.
6. Explain the migration strategy.
7. Then begin implementation.

When implementing, work theme-by-theme and page-by-page so quality can be evaluated before propagating the architecture everywhere.

Most importantly: **do not be constrained by the current visual design.** Preserve useful business functionality, but feel free to substantially restructure the presentation layer if that produces a cleaner architecture and significantly better storefronts.

---

## Status — where the answer lives (2026-08-21)

- Analysis, architecture decision, migration and verification: `.claude/plans/using-skills-impeccable-understand-stateless-lightning.md`
- The architecture itself: `store-pod/landing-ui/` — `storefront/` (single Next app), `libs/theme` (contract + token
  schema + colour bridge), `libs/ui`, `libs/i18n`, `themes/starter` (reference), `scripts/new-theme.mjs`
- How to create a theme (impeccable flow, contract checklist, verification):
  `.agents/skills/project-structure/references/new-landing-ui-template.md`
- Theme-direction catalog (briefs for the distinct themes): `store-pod/landing-ui/themes/README.md`
- Impeccable product truth: `store-pod/landing-ui/PRODUCT.md`
