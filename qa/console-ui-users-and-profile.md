# QA — user management and the account page (console-ui Module 8)

`console-ui` gains `/users`, `/profile` and `/accept-invitation`, and `store-commons` gains one permission
mapping. Two of these do something the old console cannot do at all: it has no invitations, and its
change-password screen has never worked.

- **Scope** — console-ui · store-commons/autoconfigure · tenancy (read-only; not modified)
- **Change** — plan `.claude/plans/agents-requirments-console-ui-go-live-m-woolly-candy.md`, Module 8
- **Cases** — 24
- **seller-ui** — not modified. It is the comparison, not the subject.

Each case is tagged:

- **[verified]** — run against the live stack during the build and passed.
- **[unit only]** — covered by a spec, never driven through a browser.
- **[not verified]** — never run end to end by anyone. These are where a tester is most likely to find
  something, and they are called out rather than buried.

Sections [PERM](#perm--the-permission-fix) and [99](#99--known-gaps) are the highest-value reading: one is
the defect this module fixed in a shared library, the other is behaviour that looks wrong and is expected.

---

## 00 — Before you start

```bash
sudo ./extra/scripts/configure-domain.sh        # once per machine
./extra/scripts/run-lcl.sh                      # stop with SIGTERM, never SIGINT
```

**Sign-in.** `http://console-ui.gateway.com:8000` — `org1-admin` / `admin`. Every page here is scoped to the
store in the sidebar switcher; open `ORG1-STORE1` unless a case says otherwise.

**The old console, for comparison.** `http://gateway.com:8000/pages/user-management/users`, same credentials.

**Accounts the seed gives you** (`store-core/uaa/src/main/resources/init-sql/data-test-stores.sql`), all with
password `admin` until a case changes one:

| Username | Role | Store metadata |
|---|---|---|
| `org1-admin` | ORG_ADMIN | **none** — this is the point of [U-05](#u-05) |
| `org1-store1-admin` | STORE_ADMIN | ORG1-STORE1 |
| `org1-store1-moderator` | STORE_MODERATOR | ORG1-STORE1 |
| `org1-store2-admin` | STORE_ADMIN | ORG1-STORE2 |
| `org2-admin` | ORG_ADMIN | another org — for isolation cases |

**If you change a password, change it back**, or note it: the seed only runs on a clean database.

---

## PERM — the permission fix

The one backend change in this module. `UserAccountApi.resetPassword` declared
`STORE-CORE.USERS.RESET_PASSWORD`, and that token was matched by no `case` in `CustomPermissionEvaluator` —
it fell through every switch to `default -> false`. **The endpoint was 403 for every caller, including a
super admin, from the day it was written.** Nothing reported it because an unmapped token is
indistinguishable from a refused one, and no frontend called the endpoint: seller-ui's change-password
screen points at `PATCH /v1/private/user/{id}/password`, which is mapped nowhere either.

### PERM-01 — Setting a password works at all · critical · [verified]

As `org1-admin`, `/users` → choose `Store1 Moderator` → **Set password** → `Passw0rdQA` twice → confirm.

**Expect** — a success toast, and `POST …/user-account/reset` **200** in the network tab.

- **Seen** — 403 before the fix and 200 after, in the same tab, with the only variable being a restart of
  tenancy onto the rebuilt `store-commons`. That pair is the evidence for this whole section.
- **Watch for** — the request must carry `userId=` as uaa's **UUID**, not the username, and `?store=`.

### PERM-02 — The new password actually signs in · critical · [not verified]

After PERM-01, sign out and sign in as `org1-store1-moderator` / `Passw0rdQA`.

**Expect** — the console opens. Set it back to `admin` afterwards, or note that you changed it.

### PERM-03 — A moderator may not set a password · critical · [not verified]

The token resolves to `hasMaintainAccessOnUsers` — org admin or store admin — deliberately **not** the read
audience. As `org1-store1-moderator`, call `POST …/user-account/reset?store=…&userId=…` directly.

**Expect** — **403.** The console does not offer the button to a moderator ([U-09](#u-09)); this proves the
server refuses it too, which is the half that matters.

### PERM-04 — The regression guard · high · [verified]

```bash
./gradlew :store-commons:autoconfigure:test
```

**Expect** — 17 tests pass. Delete the `STORE-CORE.USERS.RESET_PASSWORD` case from
`CustomPermissionEvaluator` and exactly two fail. These are the first tests in `store-commons`.

---

## U — the team list

### U-01 — The same people as the old console · critical · [verified]

`/users` in the new console, `/pages/user-management/users` in the old, both on ORG1-STORE1.

**Expect** — the same usernames, emails and active flags, and the same total.
- **Seen** — two users in both: `org1-store1-admin` and `org1-store1-moderator`.

### U-02 — Paging is by `count`, not `size` · critical · [verified]

Network tab on `/users`.

**Expect** — `GET …/user-account/list?page=0&count=20&store=…&pod=…`, and the response honours it.
- **Why it is called out** — `UserAccountApi.list` takes a bare `Pageable`, and nothing in tenancy names the
  parameter. It is `count` because `store-commons:autoconfigure`'s `ServletWebConfig` renames it
  platform-wide and tenancy depends on that module. seller-ui sends `count` while rendering ten rows a page
  and its author assumed the server ignored it; it does not.
- **Seen** — `count=20` on the wire, 200.

### U-03 — Every private call is store-scoped, and nothing fires twice · high · [verified]

**Expect** — on one load of `/users`: `store-manager/list`, `user-account/list`, `user-account/assignable-roles`
and `org-member/invitations`. Four requests, each with `?store=` and `?pod=`, none repeated.
- **Seen** — exactly that.

### U-04 — Switching store changes the team · critical · [not verified]

Switch the sidebar to ORG1-STORE2.

**Expect** — the list becomes ORG1-STORE2's staff, the page resets to the first page, and the notice above the
table names the new store.

### U-05 — An org admin is in no list, and the page says so · critical · [verified]

Look for `org1-admin` in ORG1-STORE1's list, then in ORG1-STORE2's.

**Expect** — **absent from both.** `ManagedUserAccountServiceImpl.list` filters uaa on `{org, store}` and
`org1-admin` has no `store` in its metadata, so it is in no store's list — including its own. This is a
platform gap, not a console one: `lessons.md`, "Users — the user list is store-scoped, so an org admin is in
no list".
- **Expect also** — the blue notice above the table explains it. Without that, an operator counts heads and
  gets the wrong number with no way to know.
- **Seen** — absent, notice shown.

### U-06 — Create, edit, enable, disable, delete · critical · [not verified]

Create `qa-newbie` with an email, a password and the Store moderator role. Then edit the name, block sign-in,
allow it again, and delete.

**Expect** — each round-trips: reload the old console and it agrees. A write **re-reads** rather than patching
the row, so the page shows what the server normalised.
- **Delete only accounts you created.**

### U-07 — A taken username is reported, not swallowed · high · [not verified]

Create a user with the username `org1-store1-admin`.

**Expect** — a 409 surfaced as a message. It arrives as `COMMON.DATA_INTEGRITY_VIOLATION` with no
`fieldErrors[]`, so it lands as a toast rather than on the username field — the same shape signup hits. There
is no pre-flight check because none is reachable: `lessons.md`, "Users — a taken username cannot be checked
before submitting".

### U-08 — You cannot act on yourself · high · [unit only]

Sign in as `org1-store1-admin` and open your own row.

**Expect** — **Block sign-in** and **Delete** are disabled with a reason on hover. Disabling yourself ends
your own session on the next request; the server stops neither, so the console does.

### U-09 — A moderator reads and cannot write · critical · [not verified]

Sign in as `org1-store1-moderator`.

**Expect** — the list renders in full, and **no Add user, no row pencil, no rail actions**. `USERS.LIST`
resolves to the store's read audience while every write resolves to `hasMaintainAccessOnUsers`.
Then confirm the server agrees by calling `create` directly — it must be **403**, not 200. The console mirrors
the server; it does not replace it.

### U-10 — The role picker never offers platform superuser · critical · [unit only]

Open the create form.

**Expect** — **Store administrator** and **Store moderator**, and nothing else.
`GET …/assignable-roles` really does return `SUPER_ADMIN` to an org admin — it filters uaa's role table by
removing only `USER` and `ORG_ADMIN`. The console intersects rather than filters one name, so a role added to
uaa later cannot appear unreviewed either. **This is defence in depth, not a fix**: `lessons.md`, "Users —
assignable-roles offers SUPER_ADMIN to an org admin".

### U-11 — A role the console has never seen · high · [unit only]

Add a role to `uaa.roles` and grant it to a user.

**Expect** — the row humanizes it (`REGIONAL_BUYER` → `Regional Buyer`) rather than the page going blank.
Transloco throws on a missing key and a role is a database row, not an enum.

---

## INV — invitations

`OrgMemberApi` has been complete and tested since tenancy phase 11 and **no frontend has ever called it**.
seller-ui has no invitations at all, so there is nothing to compare against — read
`qa/tenancy-and-pod-registry-split.md` LIF-04 for the API-level run.

### INV-01 — The token is shown once · critical · [verified]

`/users` → **Invitations** → **Invite user** → `QaNewbie@Example.COM`, Store administrator → create.

**Expect** — a dialog with a copyable link and a warning that this is the only time it can be shown. It is a
**dialog, not a toast**: only the token's hash is stored, so closing it loses the link for good.
- **Expect also** — the row appears immediately as `qanewbie@example.com` — the server lowercases it.
- **Seen** — both.

### INV-02 — The list never leaks a token · critical · [verified]

`GET …/org-member/invitations` in the network tab.

**Expect** — no `token` field on any row. `InvitationDto` deliberately omits it.
- **Seen** — absent.

### INV-03 — Only a pending invitation is actionable · high · [verified]

**Expect** — Resend and Revoke on `PENDING` rows, and **no actions** on `ACCEPTED`, `REVOKED` or `EXPIRED`.
- **Seen** — two actions on the pending row, none on the revoked one.

### INV-04 — Resend rotates rather than repeats · high · [unit only]

Resend a pending invitation.

**Expect** — a link dialog with a **different** token. The old link stops working. There is no "show me that
link again" because it is impossible.

### INV-05 — A duplicate invitation is refused · high · [not verified]

Invite the same address twice while the first is pending.

**Expect** — **409** `INVITATION.ALREADY_EXISTS`, surfaced as a message. Revoke first, or resend.

### INV-06 — Accepting · critical · [not verified]

Sign in as a user who is **not** in ORG1 and open the link.

**Expect** — the accept page (auth chrome, not the console shell), a button, then "You are in" and a link to
the console. Afterwards the invitation reads `ACCEPTED` and the user is in `GET …/org-member/list`.
- **Note** — the page is deliberately outside the console shell: an invitee is authenticated and not yet a
  member, so `consoleContext` and `requiresStore` would both refuse them. `OrgMemberApi.accept` carries no
  permission token for the same reason — the token in the link is the authorization.

### INV-07 — Accepting is never automatic · critical · [unit only]

**Expect** — opening the link sends **nothing**. The request only fires when the button is pressed, and a
second press does not fire it again. The token is single-use and burned on the first success, so accepting on
load would turn a refresh or a link preview into "already used".

### INV-08 — A used, revoked or bogus token · high · [verified]

Open `/accept-invitation?token=not-a-real-token`, and `/accept-invitation` with no token at all.

**Expect** — "This invitation cannot be used", the reason on the page rather than in a toast, and advice to
ask for a new one. A toast dismisses itself, and this is the whole content of the screen.
- **Seen** — the no-token case reads correctly.

### INV-09 — A store admin cannot invite · high · [unit only]

Sign in as `org1-store1-admin`.

**Expect** — **no Invitations tab at all**, and the pending count on the KPI row reads **—**, not 0.
`OrgMemberApi` is org-admin-only class-wide, so a store admin can create accounts in their own store and
cannot invite anyone into the organization — and "nobody is waiting to join" is a claim the page has not
earned.

---

## P — the account page

### P-01 — The toolbar goes somewhere · high · [verified]

Profile menu → **Profile**.

**Expect** — `/profile`. There is **no Settings item** beside it any more: there is no console-wide settings
page and settings are per store, at `/store-management`.

### P-02 — What it shows, and what it says instead · high · [not verified]

**Expect** — the username, the roles, and a notice explaining that the console can see nothing else.
**No name, email, avatar, phone, job title, timezone, date format or bio** — none has a column anywhere, and
the account record is unreachable twice over (`lessons.md`, "Users — the JWT carries no user id"). Empty
fields would read as "you have not filled these in"; the notice is the honest version.

### P-03 — No password control · high · [unit only]

**Expect** — nothing about passwords on `/profile`. A self-service change needs the caller's own user id and
the JWT carries the username instead, so the action lives on `/users` where a row has a real id.

### P-04 — The preferences are real, and say what they are · high · [not verified]

Switch language and theme.

**Expect** — both take effect immediately, and the page says they are remembered **in this browser only**.
There is nowhere to keep them against an account: `lessons.md`, "Shell — no user-preferences endpoint".

### P-05 — `/profile` works without a store · high · [not verified]

An account with no store at all (a fresh signup, before create-store).

**Expect** — the page opens. It is the only console route without `requiresStore`, deliberately: a personal
page is not a reading of a store.

---

## L — layout, language and themes

### L-01 — The master-detail layout appears when it fits · high · [verified]

Open a user on `/users`, then collapse the sidebar or widen the window.

**Expect** — narrow: the rail sits **under** a full-width table. Wide (container ≥ 1120px): the rail sits
**beside** it and the table is still a table.
- **Why 1120** — `app-data-table` drops to stacked cards below its own 45rem container query, so the table
  needs 720px; 720 + 1rem + 24rem of rail is 1120. Splitting sooner gave a two-pane layout whose left pane
  was no longer a table.
- **Seen** — both, and the intermediate state that motivated the number.

### L-02 — Arabic · critical · [not verified]

Every screen in this document, in Arabic.

**Expect** — the page mirrors; **emails, usernames and the invitation link stay left-to-right** inside it
(`unicode-bidi: plaintext`); dates and counts are in Arabic digits.

### L-03 — Three themes, three widths · high · [not verified]

Forest, Midnight and Daylight at 1440 / 900 / 420.

**Expect** — no literal colours, no clipped dialog, and the four dialogs on `/users` (delete, set password,
invite, invitation link) all readable at 420.

---

## 99 — Known gaps

Behaviour that looks like a defect and is not. Every one has a `lessons.md` entry.

| What you will notice | Why |
|---|---|
| **No search box**, though the design has one | uaa's list matches on metadata equality only — no name, email or username query exists. A box filtering the twenty rows on screen would answer a different question than it appears to. |
| **No "last active" column** | `ReadableUser.lastAccess` and `.loginTime` are on the DTO and set by **no mapper**. uaa has no `last_login` column. |
| **Export produces a PDF, not a CSV** | No export endpoint exists anywhere — the third time this repo has reached that finding. |
| **Invitations are unpaged** | `OrgMemberApi.invitations` answers a `List`, not a page. It will not stay small forever. |
| **Nothing is emailed** | There is no mail sender on the platform, stated in `CreatedInvitationDto`'s own javadoc. The link dialog is the entire delivery mechanism. |
| **No current-password field when setting one** | `UserPassword.password` is read by nothing; only `changePassword` reaches uaa. Asking for a value nothing verifies would be theatre. |
| **The password rule is client-side only** | There is no server-side password policy at all — `AdminService.resetPassword` encodes whatever it is handed. |
| **Creating a user is not atomic** | uaa is called twice: create, then set the password. If the second fails the account exists and cannot be signed into. |
| **No two-factor, no session list, no notification preferences** | Zero backend hits for any of them. |
| **The Organization card is missing from `/profile`** | `org-manager/find-one` is `hasAnyRole('ROLE_SUPER_ADMIN')`, so an org admin cannot read its own organization. |
