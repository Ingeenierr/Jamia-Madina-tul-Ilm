# Jamia Madinat-ul-Ilm — React Native rebuild

A ground-up redesign of your Kotlin/Jetpack Compose madrasa management app,
rebuilt in **Expo + React Native + TypeScript** so it can ship to iOS and
Android from one codebase, wired to the **same Firebase Realtime Database**
your existing app already uses (no data migration needed).

## What's actually built (Phase 2)

Full screens, real Firebase reads/writes, real navigation, real animation:

- **Auth** — Login, Sign up (role picker, pending-approval flow for teachers)
- **Admin** — Dashboard (live counts, present-today, 7-day attendance trend chart), Approvals, Leave request review, Students (list + search + add/edit/delete + attendance stats), Teachers (list), Classes, Attendance (per-class marking grid with date stepper), Attendance History (14-day trend + drill into any past day)
- **Teacher** — Dashboard (my classes), Attendance, Leave request form, Classes
- **Shared** — role-aware "More" menu with a working **Light / Dark / System** appearance switch, floating glass tab bar, gradient/glass design system that fully re-themes live

**Finance, Library, and Shop are now fully built out**, not placeholders:

- **Finance hub** — a monthly-summary dashboard (donations, fees collected/outstanding, payroll paid/pending, canteen net) linking to four real sub-modules:
  - **Donations** — log donor/amount/type, running monthly total
  - **Payroll** — one row per teacher per month, auto-created from the teacher list, tap to mark paid/unpaid, paid/pending totals
  - **Student fees** — one row per active student per month, auto-created, tap any student to record a payment (supports partial payments, auto-computes PAID/PARTIAL/UNPAID)
  - **Canteen ledger** — income/expense entries by category, monthly net
- **Library** — book inventory (title/author/category/copies), tap a book for lending history and a "lend to student" flow with due dates and a return action
- **Shop** — inventory with price/stock, one-tap "Sell" flow that deducts stock, plus a monthly sales history screen

All of these share one month-stepper component and write through Firebase
transactions where it matters (see **Concurrency safety** below).

Only **Search** remains a "coming soon" placeholder — see **Extending** below.

## Attendance — the bug fix

Your original `AttendanceViewModel.kt` attached a new Firebase listener every
time the selected date changed (inside `selectedDate.collect { ... }`) but
never removed the previous one, so listeners for every date you'd ever
viewed stayed alive and kept writing into the same `_attendanceRecords`
list. `saveAttendance` then matched an existing record by `studentId` alone,
not `studentId + date` — so a stale listener firing at the wrong moment
could make today's "present" tap land on **yesterday's** record and
overwrite it. That's the vanishing-attendance bug.

The fix here is structural, not just a patched condition: attendance is
stored as `attendance/{yyyy-MM-dd}/{studentId}/{classId} = status`. Every
date is its own top-level database path — writing to today's date can
never touch yesterday's, because there's no query or in-memory matching
involved at all, just a direct path write. See `src/services/attendanceService.ts`.

On top of the fix, this rebuild adds real reporting that wasn't in the
original app:
- **Attendance History** screen — 14-day trend chart, tap any day to view/edit that roster
- **Per-student attendance stats** on the Student Profile (present/absent/leave/rate over the last 30 days)
- **7-day trend chart** right on the admin dashboard
- Attendance screen now has a **date stepper** so you can review or correct any past day, not just today

## Dark / Light / System appearance

Implemented as a real theme system, not a color swap:

- `src/theme/palettes.ts` — two full palettes (`lightColors`, `darkColors`) sharing identical keys, so every screen just asks "what's `textPrimary` right now."
- `src/theme/ThemeContext.tsx` — `ThemeProvider` + `useTheme()`. Mode is `'light' | 'dark' | 'system'`, resolved against the OS setting via `useColorScheme()` when set to `system`, and persisted with AsyncStorage so it survives app restarts.
- Switch it from **More → Appearance**. Every screen, card, button, and the navigation background itself re-renders live — no restart needed.
- The dusk header/login band intentionally stays dark in both modes (it's the brand band, like a masthead), while all reading surfaces (paper background, glass cards, text) invert properly in dark mode.

## Design direction

Named **"Illuminated Ledger"** — it keeps your original forest-green /
luxury-gold instinct (that palette was already right for a madrasa admin
tool) but pushes it further into a coherent identity:

- **Palette**: deep ink-green dusk gradients behind headers, warm
  manuscript-cream paper for content (deep ink paper in dark mode), gold leaf
  reserved only for actions and record marks — never decoration.
- **Type**: Lora (serif) for headings, evoking a ledger/manuscript feel; Inter
  for interface text; IBM Plex Mono for numbers that are actually data
  (attendance counts, fees) so they read as trustworthy figures, not UI chrome.
- **Signature element**: frosted glass cards with a hairline gold top border
  and a soft "candle glow" — used consistently for every stat tile, form, and
  list row, so the whole app reads as one material, in both themes.
- **Motion**: staggered card entrances, a spring-sliding gold indicator on the
  floating tab bar, animated count-up numbers, an animated SVG bar chart for
  attendance trends, spring-scale button presses with haptics. Kept
  restrained — no animation without a job.

## Things this rebuild does that the Compose app couldn't (easily)

- **Live, persisted dark/light/system theming** across every screen with one context — Compose's `MaterialTheme` can do dark mode, but wiring persisted user override + system-follow + instant re-render across 20 screens is exactly the kind of thing that's a few lines here.
- **A real animated SVG chart** (`WeeklyAttendanceChart`) built from primitives in an afternoon — Compose Canvas can do this too, but with noticeably more boilerplate for animation.
- **One codebase, two native app stores.** `npx expo prebuild` gets you both an `ios/` and `android/` Xcode/Gradle project from the same source.
- **Cross-platform blur/glass** (`expo-blur`) and gradient (`expo-linear-gradient`) primitives that look right on both platforms without platform-specific code.

## Error handling and reliability

Three layers, so a bad network moment or an unexpected value never turns into a blank screen or silent data corruption:

1. **`ErrorBoundary`** wraps the entire app (see `App.tsx`) and has zero dependency on any app context — if a screen throws during render, you get a recoverable "Something went wrong / Try again" screen instead of a white crash, even if the thing that broke was a context provider itself.
2. **`ToastProvider` / `useToast()`** gives every screen a non-blocking success/error banner instead of reaching for `Alert.alert()` for routine feedback ("Donation recorded", "Could not save — check your connection"). Every new service call in Finance/Library/Shop is wrapped in try/catch with a specific, readable error message surfaced this way.
3. **`SkeletonList`** shows an animated shimmer placeholder on first load instead of a spinner or empty flash, on every new list screen.

### Concurrency safety

Two places in the original design could realistically be hit by two people at once — a teacher lending the last copy of a book while someone else does the same, or two staff members selling the last item in stock at the same moment. Both `lendBook` (`libraryService.ts`) and `recordSale` (`shopService.ts`) use a Firebase Realtime Database **transaction** on the count field (`availableCopies`, `stock`) rather than a plain read-then-write. A transaction re-runs against the live server value if it's changed since you read it, so the second of two simultaneous attempts is correctly rejected with a clear error ("Only 2 left in stock") instead of silently taking stock negative.

## Setup

> **On Windows?** Install Node.js LTS, extract this zip to a short path (e.g. `C:\Dev\JamiaMadinaApp`), then follow the same steps below. Use the Expo Go app on your phone to test — it's the fastest path from Windows since Xcode/iOS builds need a Mac or EAS Build.

```bash
npm install
```

Then paste your **existing** Firebase project's web config into
`src/services/firebase.ts` (Firebase console → Project settings → General →
Your apps → add a Web app if you don't have one → copy the config object).
Because it's the same `databaseURL`/`projectId` as your Android app, all
existing students, teachers, classes, and users show up immediately — nothing
to re-enter.

```bash
npx expo start
```

Scan the QR code with Expo Go (or press `i` / `a` for a simulator). For an
iOS build later: `npx expo prebuild` then open the generated `ios/` project
in Xcode, or use EAS Build (`npx eas build --platform ios`) once you're ready
to submit to the App Store.

## Architecture

```
App.tsx                     # font loading, providers, root
src/
  theme/
    palettes.ts              # lightColors / darkColors — identical keys, two values
    ThemeContext.tsx          # ThemeProvider + useTheme(), persisted mode
    typography.ts, spacing.ts # static (theme-independent) tokens
  types/models.ts            # mirrors your Kotlin data classes 1:1
  services/                  # one file per Firebase RTDB path (students, teachers, classes, attendance, auth)
  context/AuthContext.tsx    # Firebase auth state + user profile, exposed via useAuth()
  components/                # GlassCard, PrimaryButton, StatCard, FloatingTabBar, WeeklyAttendanceChart, etc — all theme-aware
  navigation/                 
    RootNavigator             # gates: no user -> AuthStack, ADMIN -> AdminTabs, TEACHER -> TeacherTabs
    AdminTabs / TeacherTabs    # bottom tabs, each tab is its own stack for nested screens
  screens/                   # one folder per feature area
```

Every screen calls `const { colors } = useTheme()` at the top and applies
colors inline rather than importing a static palette — that's what makes
dark/light mode instant across the whole app instead of a partial reskin.

## Debugging notes from the original app

While porting, three real issues turned up in the Kotlin source worth fixing
regardless of which stack you ship:

1. **`data/AppRepository.kt` is a 0-byte file.** Anything meant to reference
   a central repository there is dead code right now — worth checking if
   something upstream expects it to exist.
2. **Duplicate, *diverging* screen implementations.** `ui/theme/students/`,
   `ui/theme/teachers/`, `ui/theme/classes/`, `ui/theme/attendance/`, and
   `ui/theme/dashboard/AdminViewModel.kt` each shadow a same-named file in
   `ui/students/`, `ui/teachers/`, etc. — and they're not identical copies,
   they've drifted apart. Worth confirming which copy is actually wired into
   your nav graph and deleting the other; otherwise it's easy to fix a bug in
   one and not realize the app is using the other.
3. **`User.password` is stored as a field in the Realtime Database.** Firebase
   Auth already manages passwords securely; persisting a plaintext password
   string in `users/{uid}` is unnecessary and a real exposure if that data is
   ever read elsewhere (exports, admin screens, logs). The rebuilt `AppUser`
   type in this project drops that field entirely — Firebase Auth is the only
   place a password lives now.

## Extending (Finance suite, Library, Shop, Search, Lending History)

Each of these is CRUD-shaped like Students/Teachers already are. To add one:

1. Add the model to `src/types/models.ts` (you already have the Kotlin
   equivalents in `data/finance/FinanceEntities.kt` to mirror).
2. Add a `src/services/xService.ts` with `subscribeX` / `addX` / `updateX`,
   copying `studentService.ts`.
3. Add a list screen (copy `StudentsListScreen.tsx`) and, if it needs a
   create/edit form, a detail screen (copy `StudentProfileScreen.tsx`).
4. Swap its `ComingSoonScreen` entry in `AdminTabs.tsx` for the real screen.

Happy to build out any of these next — Finance and Library are the natural
next two given how central they are to daily use.
