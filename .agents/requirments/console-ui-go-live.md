
Console UI Migration Requirements
1. Objective

We want to make @store-core/console-ui/ the new production UI and fully replace @store-core/seller-ui/.

The new console-ui already has:

The new layout.
Basic application structure.
Some shared UI components.
A complete static design/template in @store-core/console-template.

The primary goal of this migration is to move from the current mocked/static data implementation to a fully API-driven implementation, while preserving the functionality currently available in @store-core/seller-ui/.

2. Existing Backend/API Logic

@store-core/seller-ui/projects/ already contains the HTTP/API integration logic used by the existing Seller UI, including:

API calls.
Services.
Models.
HTTP-related logic.

The new console-ui should reuse or migrate this existing API logic wherever applicable instead of creating duplicated implementations.

The migration should start from the landing/marketing page and progressively implement the functionality of the existing Seller UI in the new Console UI.

The target is for the new UI to provide the same functional capabilities as the existing Seller UI, while following the new Console design.

3. Design vs. Backend Gaps

The new Console design may contain blocks, components, or features that do not currently have an equivalent backend/API implementation.

When this happens:

Do not invent or mock backend behavior.
Add a clear TODO placeholder in the code.
Document the missing backend/API requirement in lessons.md.
Describe what backend feature or API is required to fully implement the UI feature.
Continue implementing the rest of the module using the available APIs.

This allows frontend migration to continue without blocking on backend work.

4. Console Template

@store-core/console-template is the main design reference for console-ui.

It contains the complete static HTML design and provides ideas for:

Overall page structure.
Layout.
Components.
Visual patterns.
UI blocks.
Interactions and possible future features.

The template should be treated as the primary reference for how the new Console UI should look and how its components should be structured.

However, static template content must eventually be connected to real API data wherever a backend capability already exists.

5. Migration Strategy

This is a large migration and must be performed incrementally.

Do not plan or implement all modules at once.

Each module/page should be handled independently and should have its own lifecycle:

Phase 1 — Planning

Before implementing a module:

Understand the equivalent functionality in seller-ui.
Identify the relevant existing API calls, services, and models.
Review the corresponding Console UI design/template.
Identify differences between the old functionality and the new design.
Identify any missing backend/API capabilities.
Define the implementation scope for that module.
Record backend gaps and future requirements in lessons.md.
Phase 2 — Implementation

Implement the planned module in console-ui.

The implementation should:

Use real APIs instead of mocked data.
Reuse existing Seller UI API/service/model logic where appropriate.
Follow the new Console UI design.
Preserve the functionality of the existing Seller UI.
Add TODO placeholders for design features that currently have no backend support.
Keep the implementation isolated to the current migration scope.
Phase 3 — Testing

Test the implemented module using Chrome.

The old and new applications can be run simultaneously against the same backend, allowing direct comparison:

Old: seller-ui.gateway.com:8000
New: console-ui.gateway.com:8000

Use both applications in separate browser tabs to compare behavior, data, flows, and edge cases.

Phase 4 — Commit

Each phase should result in exactly one commit.

For example:

Planning phase → one commit.
Implementation phase → one commit.
Testing/fix phase → one commit.

The commit history should make the migration progress easy to understand and review.

6. Functional Parity

The new Console UI should ultimately provide the same functionality as the existing Seller UI.

For each module, compare:

Pages and routes.
API interactions.
Data loading.
Forms.
CRUD operations.
Validation.
Loading states.
Empty states.
Error handling.
Permissions/authorization behavior.
Navigation.
User workflows.
Edge cases.

The new design should not result in existing functionality being silently removed.

If the new design intentionally changes behavior, document the difference during the planning phase.

7. Backend Feature Tracking

Whenever frontend implementation discovers functionality that requires backend work, document it in lessons.md.

Each entry should explain:

Which module/page requires the feature.
What the Console UI needs.
What API/backend capability is currently missing.
Why the backend capability is required.
Any expected request/response behavior that can already be determined.

These entries will become a reference for future backend implementation.

8. Important Constraints
   Do not migrate all modules in a single planning exercise.
   Do not create one large migration plan covering the entire application.
   Each module must be planned independently before implementation.
   Each module may introduce new components, edge cases, API requirements, or backend gaps.
   Do not assume that one module's implementation approach will work for every other module.
   Prefer existing seller-ui API/service/model logic over duplicating HTTP implementations.
   Do not use mocked data when a real API already exists.
   Do not block the entire migration because a new design feature has no backend support; mark it as a TODO and document the required backend work in lessons.md.
9. Expected Approach Going Forward

When a new module is requested for migration, first provide the planning phase for that module only.

The plan should cover:

Existing Seller UI functionality.
Relevant API/services/models.
New Console UI/template design.
Mapping between old functionality and new UI.
New components required.
API/backend gaps.
TODOs and lessons.md entries.
Testing approach using old and new UIs.
The implementation scope and expected commit structure.

Only after the module's plan is approved should implementation begin.

The goal is to progressively migrate the entire application from seller-ui to console-ui while maintaining functional parity, using real backend APIs, and keeping the migration reviewable and safe through small, independent steps.