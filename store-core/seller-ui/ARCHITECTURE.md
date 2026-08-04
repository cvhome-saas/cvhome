# Angular Component Architecture Rules

## 1. Core Principle

A **Component** is a dumb renderer. It has exactly three responsibilities:

1. Bind template to state exposed by a **Facade**.
2. Forward user events (click, submit, change) to Facade methods.
3. Manage local, purely visual state only (e.g. "is this dropdown open" — never business/data state).

Everything else — HTTP calls, form building, validation, mapping, orchestration, notifications, navigation, dialogs, formatting, pagination, filtering — lives outside the component class.

---

## 2. Layers

| Layer | File suffix | Responsibility | Must NOT do |
|---|---|---|---|
| **Component** | `.component.ts` | Template binding, event forwarding | Any logic listed in section 3 |
| **Facade** | `.facade.ts` | Orchestrates everything below; exposes ready-to-bind state (Signals/Observables) and intent-named methods (`onSubmit()`, `loadPage(n)`) | Direct HTTP calls, DOM/template concerns |
| **ApiService** | `.api.service.ts` | HTTP calls only, one per resource/domain | Business logic, state, UI shaping |
| **StateService** | `.state.ts` | Holds/exposes state (Signals or BehaviorSubjects); owns loading/error state | HTTP calls, form building |
| **FormService** | `.form.service.ts` | Builds `FormGroup`s, sets/updates validators, patches values | Calling APIs directly |
| **MapperService** | `.mapper.ts` | DTO ↔ ViewModel transforms, request payload construction | Side effects |
| **ValidationService** *(optional, as needed)* | `.validators.ts` | Custom sync/async validators | — |

**Scope decision:** Facades are **shared per feature**, not strictly 1:1 with a component, since UI composition may change and multiple components may need the same feature logic. A feature may expose one Facade consumed by a container component and any of its presentational children.

---

## 3. Hard Rules for Components

A component file must **never** contain:

- ❌ `HttpClient` or any `ApiService` injected directly
- ❌ `FormBuilder` / `new FormGroup(...)` usage
- ❌ `.subscribe()` with transform logic inside the callback. Prefer `async` pipe / Signals. If a facade must subscribe internally, use `takeUntilDestroyed()`.
- ❌ Business conditionals (`if (user.role === 'admin' && ...)`) — the decision must already be exposed as a boolean/state from the Facade
- ❌ Manual loading-flag toggling (`this.loader = true/false`)
- ❌ Repeated/inline error handling — no `catchError`/`error:` blocks with logic in the component
- ❌ Direct calls to `NbToastrService` / any notification service
- ❌ Direct calls to `NbDialogService` / any dialog service
- ❌ Direct `Router.navigate(...)` calls
- ❌ Third-party formatting/business libraries (`moment`, `libphonenumber-js`, etc.)
- ❌ Inline DTO/request-payload construction
- ❌ Hardcoded option lists / magic numbers / magic strings (use enums or shared constants)
- ❌ `console.log` or any debug statement (enforce via `no-console` lint rule)
- ❌ Orchestration of multiple calls/streams (`zip`, `forkJoin`, sequential subscribe chains)
- ❌ Cross-field / cascading business logic (e.g. "changing country resets zone")
- ❌ Pagination/filtering/table-state logic (`onPageEvent`, `list()` overrides, `Object.assign(request, filter)`, guard conditions like `!params.store`)
- ❌ **Extending an abstract base class to inherit shared business/data logic** (see section 4 — composition only)

A component file **may only** contain:

- ✅ Property bindings and event bindings in the template
- ✅ Calls to Facade methods (`facade.onSubmit()`, `facade.onCountryChange(value)`, `facade.onPageEvent(e)`)
- ✅ `ngOnInit`/`ngOnDestroy` that at most call `facade.init(...)` / `facade.destroy()`
- ✅ Purely visual local state (e.g. `isMenuOpen = false`) that has no business meaning
- ✅ Simple display-only template logic (`*ngIf`, already-computed pipes) — no computation of business values in the template

---

## 4. Composition Over Inheritance

**No shared logic via component/class inheritance.** Shared behavior — pagination, filtering, store-scoping, table state, or any other cross-cutting concern — must be implemented as an **injectable, composable service** consumed by a Facade. Components and Facades must never extend an abstract base class to acquire business/data behavior.

Why this matters:
- A component that `extends BaseX` still contains the logic itself (via inherited/overridden methods) — it has only moved one hop, not been removed. It still typically ends up directly injecting an `ApiService`, still directly assigns request payloads, and still subscribes with logic in the callback — all violations of section 3.
- Base classes with constructor side effects (e.g. auto-subscribing to a store observable on construction) create hidden, hard-to-test dependency graphs.
- Inherited state (e.g. `this.page`, `this.isLoading` via `extends BaseTable`) can't be reused outside a component of that exact class, and can't be unit-tested without instantiating a component.
- Composition (Facade → injected shared services) keeps every layer independently testable and reusable, including from presentational components or non-table contexts.

### Reference pattern: shared table/list state

Common list/table behavior (pagination, per-store scoping, loading state) is implemented as a **state-only, generic, injectable service** — with orchestration living explicitly in each feature's own Facade, not in a generic "mega-facade":

```
shared/table/
  table-state.service.ts   // generic, state-only: page, isLoading, params (Signals)
```

`TableStateService<T, R>` responsibilities:
- Holds `page`, `isLoading`, `params` as Signals.
- Exposes plain setters (`setPage(...)`, `setLoading(...)`, `setParams(...)`).
- Uses generic types: `T` for row data type, `R` for request parameter type (defaults to `StorePageRequest`).
- Contains **no** API calls, **no** store-subscription logic, **no** orchestration.
- When custom filtering parameters are required, define a feature-specific interface that extends `StorePageRequest`.

Each feature Facade (e.g. `OrderListFacade`) then:
- Injects `TableStateService<T, R>` and its own feature API service.
- Injects `SelectedStoreService` and subscribes to store changes itself (via `takeUntilDestroyed`), explicitly, in its own `init()`.
- Owns the actual `trigger()`/`list()` orchestration: builds the request from state + filter, calls the API, updates `TableStateService`, handles errors via the shared error-handling helper (section 5).
- Exposes `page`, `isLoading`, `filter` as read-only Signals, and intent methods: `onFilterChange()`, `onPageEvent()`, `resetFilters()`.

This is intentionally more explicit per feature than a generic base class or generic facade — it keeps orchestration readable and lets features diverge (custom filters, extra params, different error handling) without fighting a one-size-fits-all abstraction.

---

## 5. Facade Rules

- Injected into the component's constructor — ideally the **only** dependency the component needs.
- Exposes state as **Signals or Observables already shaped for the template** — no further transformation should be needed in the component or template.
- Exposes methods named after **user intent**, not CRUD verbs: `onSubmit()`, `onDeleteItem(id)`, `loadPage(n)` — not `post()`, `get()`.
- Owns:
  - Orchestration of multiple API/state calls (replaces `zip`/`forkJoin` chains, and replaces base-class `trigger()` methods, previously found in components)
  - Loading state management (via a shared helper, e.g. `withLoading(source$)` — not manual per-call toggling)
  - Error handling (via a shared operator/helper applied consistently — not copy-pasted per call site)
  - Notification triggering (calls a `NotificationService`, not the raw Toastr/Snackbar service)
  - Dialog opening (calls a `DialogService` wrapper, or does so directly but never leaves this to the component)
  - Navigation (wraps `Router` calls, e.g. `facade.goBack()`)
  - Formatting delegation (phone/date/currency formatting calls a formatter utility, not inline in Facade or component)
  - Pagination/filtering orchestration (builds requests, calls the API, updates table state — see section 4)

---

## 6. Error Handling & Loading State

- **No repeated `error: (err) => { this.loader = false; this.errorService.error(...) }` blocks.** Centralize via one of:
  - A shared RxJS operator/pipeable applied in the ApiService or Facade (e.g. `catchApiError()`).
  - A Facade `execute()`/`run()` helper that wraps an action with consistent loading + error handling.
- Loading state is derived, not manually flipped in multiple places. Prefer deriving from the request stream (e.g. `startWith`/`finalize`, or Signal-based `loading` toggled once by the shared helper).
- This applies equally to list/table Facades — no bespoke `_isLoading` flip-flopping duplicated across every table feature.

---

## 7. Routing & Data Resolution

- Route params (`orderID`, `storeID`, etc.) should be resolved via **Angular Resolvers** where practical, rather than parsed manually inside `ngOnInit`/Facade `init()`.
- Facade `init()` should consume already-resolved data instead of parsing `ActivatedRoute.params` and chaining multiple dependent calls (`zip` chains are a smell to eliminate).
- Navigation is always wrapped by the Facade (`facade.goBack()`), never called on `Router` directly from a component.

---

## 8. Smart / Presentational Split

- **Container components** (e.g. `order-details.component.ts`, `order-list.component.ts`) are the only components that inject a Facade.
- **Presentational sub-components** (e.g. `order-billing-form`, `order-shipping-form`, `order-status-form`) receive data via `@Input()` and emit user actions via `@Output()` only. They inject **no services at all**.
- Large components with multiple logical sections (billing, shipping, status, history) should be decomposed into presentational sub-components, each with a narrow, testable template.
- Decomposition can be done incrementally — not required to happen in the same pass as introducing the Facade — but is the target end state per feature.

---

## 9. Static Data & Constants

- Static option lists, enums, and magic values (status lists, dialog type identifiers, language lists, etc.) live in a shared `*.constants.ts` or `*.enum.ts` file — never declared inline inside a component class.
- Example: replace `showDialog(value)` with `value == 1/2/3` by an enum:
  ```ts
  export enum OrderDialogType {
    Transaction = 'TRANSACTION',
    Invoice = 'INVOICE',
    History = 'HISTORY',
  }
  ```

---

## 10. Naming Convention

Per feature/component, in a dedicated folder:

```
order-details/
  components/
    order-details.component.ts
    order-details.component.html
    order-details.component.scss
    order-billing-form/
    order-shipping-form/
    order-status-form/
  facades/
    order-details.facade.ts
  services/
    order-details.api.service.ts
    order-details.form.service.ts
    order-details.mapper.ts
  state/
    order-details.state.ts
  constants/
    order-details.constants.ts

order-list/
  components/
    order-list.component.ts
  facades/
    order-list.facade.ts
  services/
    orders.api.service.ts
  state/
    order-list.state.ts        // feature state built on top of shared TableStateService
  constants/
    order-list.constants.ts

shared/
  table/
    table-state.service.ts     // generic, state-only, reused across all list/table features
```

---

## 11. State Management Style

- Signals and/or RxJS-based plain services — no formal store library (NgRx/Akita/Elf) unless a specific feature's complexity later justifies it.
- State layer owns: current data, loading flag, error flag/message. Facade reads/writes through the State layer rather than holding its own duplicate fields.
- Shared cross-feature state concerns (e.g. table pagination) are implemented as generic, state-only injectable services (section 4) — never as base classes.

---

## 12. Enforcement Checklist (for PR review)

- [ ] No `HttpClient`, `FormBuilder`, `NbDialogService`, `NbToastrService`, or `Router` injected in any `.component.ts`
- [ ] No third-party libraries imported in `.component.ts`
- [ ] No `.subscribe()` with logic in the component (async pipe / Signals used instead)
- [ ] No manual loading flag toggling in the component
- [ ] No repeated inline error handling
- [ ] No magic numbers/strings — enums/constants used
- [ ] No `console.log`
- [ ] Component's `ngOnInit` only calls `facade.init(...)`
- [ ] All request payload construction happens in a Mapper, not inline
- [ ] Presentational sub-components (if any) have zero injected services
- [ ] No component/Facade extends an abstract base class for shared business logic — composition (injected services) used instead
- [ ] Pagination/filtering logic lives in a Facade + shared `TableStateService`, not in a `list()`/`trigger()` override on the component

## 13. List/Table Component Implementation Pattern

List components must follow the "dumb renderer" pattern by delegating all orchestration to the feature's Facade:

- **Initialization**: 
  - Always use `protected readonly facade = inject(FeatureFacade)` for injection to ensure early initialization.
  - `ngOnInit()` must call `facade.init()`. The facade is responsible for retrieving initial parameters (e.g., from `SelectedStoreService` and `TableStateService`) and triggering the initial data load.
- **Event Handling**: Template events (pagination, sorting, filtering) must bind directly to Facade methods, e.g., `(page)="facade.onPageChange($event)"`.
- **Data Binding**: Templates should bind directly to state signals exposed by `TableStateService` via the Facade (e.g., `[rows]="tableState.page().content"`), NOT via local component properties or inherited BaseTable properties.
- **Filter State**: For local component-bound filter models, initialize via `filter = { ...this.facade.filter() }` in the component class. The Facade method should retrieve current filter state from `TableStateService`.
