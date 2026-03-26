# Design System Document: The Curated Wanderer

## 1. Overview & Creative North Star
**Creative North Star: "The Digital Concierge"**
This design system moves away from the "utility-first" look of standard travel platforms and embraces a high-end editorial feel. It is designed to feel like a premium travel magazine—sophisticated, breathable, and deeply intentional. 

Instead of rigid grids, we use **Intentional Asymmetry**. Elements are layered to mimic the physical experience of laying out maps and polaroids on a desk. We prioritize "White Space as a Luxury," ensuring that the vibrant Sri Lankan palette (Teal, Coral, and Jungle Green) acts as a focused guide rather than an overwhelming presence. The goal is to build trust through polished, calm, and expertly layered surfaces.

---

## 2. Colors & Surface Philosophy
The palette is rooted in the natural vibrancy of Sri Lanka, balanced by a sophisticated neutral foundation.

### Palette Highlights
- **Primary (Ocean Teal):** `#00685f` — Use for brand presence and primary navigation.
- **Secondary (Sunset Coral):** `#9d4300` — Reserved for high-intent CTAs and "Magical Moments" in the itinerary.
- **Tertiary (Jungle Green):** `#006948` — Used for success states and eco-certified provider badges.

### The "No-Line" Rule
**Standard 1px borders are strictly prohibited.** To define sections, use:
1.  **Background Shifts:** Transition from `surface` to `surface-container-low`.
2.  **Tonal Transitions:** Use a soft gradient (e.g., `primary` to `primary-container`) for hero sections to provide "soul" and depth.

### Surface Hierarchy & Glassmorphism
Treat the UI as a series of stacked, semi-transparent layers.
- **Surface Tiering:** Use `surface-container-lowest` for the most prominent foreground cards (e.g., a selected hotel) sitting on a `surface-container` background.
- **The Glass Rule:** Floating elements (like the AI Concierge bubble) must use a **Glassmorphism** effect: `surface-container-lowest` at 80% opacity with a `20px` backdrop-blur. This ensures the lush imagery of the destination bleeds through the UI, making it feel integrated.

---

## 3. Typography
We pair the geometric confidence of **Plus Jakarta Sans** with the high-legibility of **Inter**.

- **Display & Headlines (Plus Jakarta Sans):** These are our "Editorial Voices." Use `display-lg` (3.5rem) for hero titles. Tighten letter-spacing slightly (-2%) for a premium, custom feel.
- **Body & Labels (Inter):** Our "Functional Voice." Use `body-lg` (1rem) for itinerary descriptions. Inter provides a neutral, trustworthy counter-balance to the expressive headings.
- **Hierarchy Tip:** Use `on-surface-variant` for secondary descriptions to create a natural hierarchy without reducing font size too drastically.

---

## 4. Elevation & Depth
Depth is achieved through **Tonal Layering**, not structural lines.

- **The Layering Principle:** Rather than shadows, stack surfaces. A `surface-container-highest` element feels naturally "closer" to the user than a `surface-container-low` background.
- **Ambient Shadows:** For floating components, use an extra-diffused shadow: `box-shadow: 0 12px 40px rgba(23, 29, 28, 0.06)`. Note the tint—we use a 6% opacity of our `on-surface` color, never pure black.
- **The "Ghost Border" Fallback:** If a container lacks sufficient contrast, use the `outline-variant` token at **15% opacity**. It should be felt, not seen.

---

## 5. Components

### AI Chat Bubbles (The Concierge)
- **User Bubble:** `primary-container` background, `on-primary-container` text. Corner radius: `24px` (rounded-xl).
- **AI Bubble:** `surface-container-highest` background with a glassmorphism backdrop-blur. 
- **Interaction:** Use subtle `2.5` (0.85rem) spacing between consecutive bubbles from the same sender.

### Provider Cards (The Showcase)
- **Structure:** No borders. Use `surface-container-lowest` on a `surface` background.
- **Corner Radius:** `12px` (rounded-lg).
- **Content:** Images should have a subtle `inner-shadow` to make the white text overlays pop. Forbid divider lines; use `4` (1.4rem) vertical spacing to separate the provider name from the price.

### Trip Timeline Elements
- **The Path:** A `2px` dashed line using `outline-variant` (20% opacity).
- **Nodes:** Use `primary` for completed stops and `secondary` for the "Current Highlight."
- **Spacing:** Use the `8` (2.75rem) spacing token between stops to allow the itinerary to breathe.

### Booking Status Badges
- **Confirmed:** `tertiary-container` background with `on-tertiary-container` text. 
- **Pending:** `secondary-container` background with `on-secondary-container` text.
- **Style:** All-caps `label-sm` typography with `0.1rem` letter spacing. Corner radius: `full` (9999px).

### Buttons
- **Primary:** `primary` background. Corner radius: `8px` (rounded-md). Use `6` (2rem) horizontal padding.
- **CTA/Highlight:** `secondary` background. Reserve this for "Book Now" or "Generate Plan."
- **Tertiary (Ghost):** No background. Use `primary` text with a `surface-variant` hover state.

---

## 6. Do's and Don'ts

### Do
- **DO** use asymmetric layouts (e.g., an image offset from its text container) to create an editorial feel.
- **DO** use the `12` (4rem) spacing token to separate major sections.
- **DO** favor high-quality imagery of Sri Lanka as the "base layer" behind glassmorphic panels.

### Don't
- **DON'T** use 100% opaque black shadows.
- **DON'T** use 1px solid borders to separate list items; use background color shifts or `3` (1rem) of white space instead.
- **DON'T** crowd the screen. If in doubt, increase the spacing by one level on the scale.
- **DON'T** use "Success Green" for primary actionp�%�Y\]��X�H�܈�]\�[��ۙ�\�X][ۈ�XZ[�Z[�H��X[�X[��[�Y[�]K