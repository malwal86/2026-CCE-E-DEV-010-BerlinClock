# TDD journey

How the Berlin Clock was built test-first, story by story, and where to find each step in the git history.

- [How to read the history](#how-to-read-the-history)
- [Kata scenarios → tests → commits](#kata-scenarios--tests--commits)
- [Story by story](#story-by-story)
- [Refactoring steps](#refactoring-steps)
- [What went wrong, and what changed](#what-went-wrong-and-what-changed)

## How to read the history

Every story is a vertical slice (React UI → Spring Boot API → PostgreSQL) delivered as one pull request. Inside a
story there is **one commit per layer**, built inside-out:

```
feat(domain) → feat(application) → feat(persistence) → feat(api) → feat(ui) → build / ci → docs
```

Each commit is green on its own (it compiles and every test passes), so any of them can be checked out or reverted.
The **red → green → refactor cycles of that layer are listed in the commit body**, in the order they happened:

```
TDD cycles (kata table, verbatim):
- red: 00:00:00 -> OOOO -> green (four lamps, all off)
- red: 23:59:59 -> RRRR -> green (Lamp.RED, light hours / 5 lamps)
- 02:04:00 -> OOOO, 08:23:00 -> ROOO, 16:35:00 -> RRRO: green on arrival
- refactor: extract LampRow.light(lit, size, colour) ...
```

- **red** is a test written before the code, seen failing for the right reason. **green** is the smallest change that
  makes it pass (named in brackets).
- **green on arrival** / **pinned** is a test that passed the first time because an earlier step already covered it.
  It stays, to pin the behaviour down. Where a guard could have made a test pass by accident, the guard was removed
  to watch the test fail, and the commit says so.
- **refactor** is a change with every test green before and after.
- In the `api` commit, the first red is always a **full-stack acceptance test** (real HTTP, real PostgreSQL through
  Testcontainers) that stays red until the layers below and the controller are done.

The plan in the stories PDF (Appendix C) mentions separate red / green / refactor commits. That was tried for A1 and
changed to one commit per layer before the first pull request: every commit stays green and revertible, and the
bodies still record every cycle.

To read it yourself:

```bash
git log --reverse --no-merges --format='%h %s' main     # one line per step
git show 8898279                                         # one step: its cycles and its diff
```

Links below go to the commits on GitHub (`/commit/<hash>`) and to each story's pull request.

## Kata scenarios → tests → commits

Every example of the kata's Feature 1 ("Converting Digital Time to Berlin Time") is a test, written verbatim from
the kata table, in the commit where it first went red.

| Kata scenario | Time | Expected | Test | Red → green in | Story |
|---|---|---|---|---|---|
| Seconds lamp | 00:00:00 | `Y` | `SecondsLampTest` | [`8d94446`][8d94446] | A1 |
| | 23:59:59 | `O` | | | |
| Five hours row | 00:00:00 | `OOOO` | `FiveHoursRowTest` | [`8898279`][8898279] | A3 |
| | 23:59:59 | `RRRR` | | | |
| | 02:04:00 | `OOOO` | | | |
| | 08:23:00 | `ROOO` | | | |
| | 16:35:00 | `RRRO` | | | |
| Single hours row | 00:00:00 | `OOOO` | `SingleHoursRowTest` | [`90b71a0`][90b71a0] | A4 |
| | 23:59:59 | `RRRO` | | | |
| | 02:04:00 | `RROO` | | | |
| | 08:23:00 | `RRRO` | | | |
| | 14:35:00 | `RRRR` | | | |
| Five minutes row | 00:00:00 | `OOOOOOOOOOO` | `FiveMinutesRowTest` | [`d786738`][d786738] | A5 |
| | 23:59:59 | `YYRYYRYYRYY` | | | |
| | 12:04:00 | `OOOOOOOOOOO` | | | |
| | 12:23:00 | `YYRYOOOOOOO` | | | |
| | 12:35:00 | `YYRYYRYOOOO` | | | |
| Single minutes row | 00:00:00 | `OOOO` | `SingleMinutesRowTest` | [`4f030b1`][4f030b1] | A6 |
| | 23:59:59 | `YYYY` | | | |
| | 12:32:00 | `YYOO` | | | |
| | 12:34:00 | `YYYY` | | | |
| | 12:35:00 | `OOOO` | | | |
| Entire Berlin Clock | 00:00:00 | `YOOOOOOOOOOOOOOOOOOOOOOO` | `BerlinClockTest` | [`aa3c691`][aa3c691] | A7 |
| | 23:59:59 | `ORRRRRRROYYRYYRYYRYYYYYY` | `ConversionApiAcceptanceTest` (over HTTP) | [`6d8ac2e`][6d8ac2e] | |
| | 16:50:06 | `YRRROROOOYYRYYRYYRYOOOOO` | | | |
| | 11:37:01 | `ORROOROOOYYRYYRYOOOOYYOO` | | | |

Beyond the kata table:

| Rule | Test | Commit |
|---|---|---|
| Invalid inputs rejected: `25:00:00`, `24:00:00`, `12:60:00`, `12:00:60`, `1:2:3`, `12-00-00`, `12:00`, `noon`, empty | `DigitalTimeTest`, `ConversionApiAcceptanceTest` | [`47574b4`][47574b4], [`b22458b`][b22458b] |
| The quarter-hour marker alone: 12:15:00 → `YYROOOOOOOO` (every lit lamp in the kata examples is followed by a yellow one, so this pins the red marker) | `FiveMinutesRowTest` | [`d786738`][d786738] |
| The code is 24 lamps long for all 86,400 seconds of the day (property check) | `BerlinClockTest` | [`aa3c691`][aa3c691] |

## Story by story

### A1 · Walking skeleton: convert a time, see the seconds lamp, find it in history ([PR #1][pr1])

| Layer | Commit | Cycles |
|---|---|---|
| scaffold | [`f10dea4`][f10dea4] | Spring Boot 4.1 + Vite React TS, JaCoCo gate on the `clock` package |
| domain | [`8d94446`][8d94446] | red 00:00:00 → `Y` → green (return YELLOW); red 23:59:59 → `O` → green (even/odd rule) |
| application | [`7d756b7`][7d756b7] | saves time + conversion moment; latest ten newest first; a conversion shows its clock (in-memory fake, fixed `Clock`) |
| persistence | [`621c6ad`][621c6ad] | `@JdbcTest` + Testcontainers: generated id, exactly what was stored, newest first, ties ordered last-saved first |
| api | [`eff7e7b`][eff7e7b] | red acceptance (HTTP + PostgreSQL) → red `@WebMvcTest` 201 + Location → green, then wiring turns the acceptance green |
| ui | [`237f80a`][237f80a] | lamp lit / off; convert shows the lamp; Enter submits; new entry on top; history on open; empty state |
| build, ci, docs | [`8a99699`][8a99699], [`8a7fcdb`][8a7fcdb], [`172a0ce`][172a0ce] | Docker Compose stack, GitHub Actions, README |

### A2 · Clear feedback for invalid times ([PR #2][pr2])

| Layer | Commit | Cycles |
|---|---|---|
| domain | [`47574b4`][47574b4] | valid times parse; one red per invalid example; null / empty / blank → "A time is required" |
| application | [`f90ef91`][f90ef91] | an invalid or missing time throws and the history stays empty |
| api | [`b22458b`][b22458b] | acceptance: each invalid example → 400 problem+json and no row; `@WebMvcTest`: exact problem detail, body that is not JSON |
| ui | [`2d2f146`][2d2f146] | alert under the field, `aria-invalid`, old result cleared, history unchanged, cleared on success |
| docs + refactor | [`3127b1b`][3127b1b] | shared `web` package renamed `error` |

### A3 · Five-hours row ([PR #3][pr3])

| Layer | Commit | Cycles |
|---|---|---|
| domain | [`8898279`][8898279] | kata table; refactor: extract `LampRow.light(lit, size, colour)` with its own tests |
| api | [`bcd58f3`][bcd58f3] | acceptance: POST 16:35:00 → `RRRO`; a row inserted straight into the table (saved "before" the story) gains the row on read |
| ui | [`42ef32b`][42ef32b] | generic `LampRow` from the kata notation; the fake API answers from a table, so no clock rule lives in the frontend, not even in tests |
| docs | [`b6a089d`][b6a089d] | |

### A4 · Single-hours row ([PR #4][pr4])

| Layer | Commit | Cycles |
|---|---|---|
| domain | [`90b71a0`][90b71a0] | kata table; three examples green on arrival thanks to `LampRow.light` |
| api | [`5e6c5a2`][5e6c5a2] | acceptance POST 14:35:00 and an old row; `@WebMvcTest` JSON |
| ui | [`cfcf1d6`][cfcf1d6] | no new component or CSS |
| docs | [`dbf0ef1`][dbf0ef1] | |

### A5 · Five-minutes row ([PR #5][pr5])

| Layer | Commit | Cycles |
|---|---|---|
| domain | [`d786738`][d786738] | all-off cases → green (yellow lamps); red markers → green (every third lit lamp is red) |
| api | [`0e85e4b`][0e85e4b] | acceptance POST 12:35:00 and an old row; `@WebMvcTest` JSON |
| ui | [`793e361`][793e361] | rows share one width, eleven lamps narrow with no row-specific CSS |
| docs | [`db75faa`][db75faa] | |

### A6 · Single-minutes row ([PR #6][pr6])

| Layer | Commit | Cycles |
|---|---|---|
| domain | [`4f030b1`][4f030b1] | kata table; refactor: single hours and single minutes share `leftOverAfterFiveBlocks` |
| api | [`9a40d29`][9a40d29] | acceptance POST 12:34:00 and an old 12:32:00 row |
| ui | [`6c23902`][6c23902] | fifth row, the clock is complete |
| docs | [`de29d8e`][de29d8e] | |

### A7 · The entire Berlin Clock as one 24-character code ([PR #7][pr7])

| Layer | Commit | Cycles |
|---|---|---|
| domain | [`aa3c691`][aa3c691] | the four kata codes → green; property: 24 characters for every second |
| api | [`6d8ac2e`][6d8ac2e] | the four kata examples via POST and GET over HTTP; an old row gains its code |
| ui | [`3471ad8`][3471ad8] | clock as `role="img"` labelled with its time; code as text; Copy → "Copied" → back |
| docs | [`b634c82`][b634c82] | |

### B1 · Revisit a past conversion ([PR #8][pr8])

| Layer | Commit | Cycles |
|---|---|---|
| persistence | [`f4aea45`][f4aea45] | found by id exactly as stored; unknown id → empty |
| application | [`7a2019a`][7a2019a] | port gains `findById`; finding leaves the history as it was |
| api | [`83c4d40`][83c4d40] | acceptance: GET the POST's `Location` → same body, still one row; unknown id → 404 problem; `abc` → 400 |
| ui | [`48b2e83`][48b2e83] | ten cycles: link per entry, `aria-current`, Cmd-click left to the browser, deep link, unknown id, Back / Forward |
| docs | [`8405578`][8405578] | |

Persistence is committed before application here: adding a method to the port first would break the build until
the adapter implements it, and every commit must stay green.

### B2 · Clear the history ([PR #9][pr9])

| Layer | Commit | Cycles |
|---|---|---|
| persistence | [`c926831`][c926831] | delete every row; pinned: deleting an empty history |
| application | [`f9c8389`][f9c8389] | nothing listed, no id found afterwards |
| api | [`06b22ee`][06b22ee] | acceptance: 204, `[]`, no rows; pinned: idempotent, old id → 404 |
| ui | [`4cc3509`][4cc3509] | inline confirmation, focus on Cancel, disabled when empty |
| docs, test | [`9fa1976`][9fa1976], [`452a047`][452a047] | see [what went wrong](#what-went-wrong-and-what-changed) |

### C1 · Live clock ticking every second ([PR #10][pr10])

| Layer | Commit | Cycles |
|---|---|---|
| api | [`1628b7b`][1628b7b] | acceptance: GET 16:50:06 → clock, table still empty; 24:00:00 → 400 |
| ui | [`d3da47f`][d3da47f] | fake `Date` and timers: ticks each second, aligned to the next whole second, late answers dropped, cleaned up on unmount; a minute of ticks = 61 GETs and no POST. Each guard removed once to watch its test fail |
| docs | [`fc741c5`][fc741c5] | |
| test, fix | [`fe60f90`][fe60f90], [`c4f4ff9`][c4f4ff9] | see [what went wrong](#what-went-wrong-and-what-changed) |

### C2 · Resilience when the backend or database is unavailable ([PR #11][pr11])

| Layer | Commit | Cycles |
|---|---|---|
| api | [`12fbda0`][12fbda0] | in-memory history made to fail: 503 for each endpoint, 500 without internals; pinned acceptance stops its own PostgreSQL container |
| ui | [`69b99ca`][69b99ca] | banner and dimmed clock after a network error or 502, recovery; database down / back / backend down in the page |
| build | [`5b44675`][5b44675] | nginx connect timeout 2 s (measured 39 s before) |
| docs | [`88f70cc`][88f70cc] | |

### D1 · Explorable API documentation ([PR #12][pr12])

| Layer | Commit | Cycles |
|---|---|---|
| api | [`12fbe26`][12fbe26] | springdoc: document served, Swagger UI served, examples and error schemas |
| refactor | [`4040574`][4040574] | hand-written `openapi.yaml`; red `ApiContractTest` (12 answers validated against the file) → green; mutation check: removing a documented 503 fails it |
| docs | [`b1a2c04`][b1a2c04], [`d63f286`][d63f286] | |

### D2 · Reviewer guide and TDD journey

This document, the README as a reviewer guide, and the `v1.0.0` tag. No production code.

## Refactoring steps

| Commit | Refactor | Safety net |
|---|---|---|
| [`8898279`][8898279] | Extract `LampRow.light(lit, size, colour)`, "light n of m lamps", as soon as the first row existed | Row and `LampRow` tests |
| [`42ef32b`][42ef32b] | The frontend's fake API answers from a table of kata results instead of computing the seconds lamp | `App.test.tsx` |
| [`3127b1b`][3127b1b] | Rename the shared `web` package to `error`, after what it holds | Whole backend suite |
| [`4f030b1`][4f030b1] | Single hours and single minutes share `leftOverAfterFiveBlocks(units, colour)` | Every row test |
| [`4040574`][4040574] | Replace springdoc annotations with a hand-written contract, guarded by a new contract test | `ApiContractTest`, acceptance tests |

## What went wrong, and what changed

- **A flaky UI test.** "offers to copy again a moment later" failed on CI twice and never locally. The first fix
  ([`452a047`][452a047]) waited for "Copied" before advancing the fake clock; it failed again, because the render
  had happened but the effect that sets the reset timer had not. The second fix ([`fe60f90`][fe60f90]) advances
  the clock asynchronously and waits for the reset. Both are test-only changes, and both commit bodies explain the
  race.
- **Clearing the history while a conversion was open said "not found".** It was the designed behaviour from B2,
  but it was noise right after clicking Delete. [`c4f4ff9`][c4f4ff9] closes the result instead, starting from a red
  test, and keeps "not found" for old links.
- **API documentation in annotations.** springdoc worked ([`12fbe26`][12fbe26]) but filled the controllers and
  DTOs with documentation. It was replaced by one readable file, and a contract test was added so the file cannot
  drift from the code ([`4040574`][4040574]).
- **Not built from the plan:** the `?limit=` parameter of `GET /api/conversions` (Appendix B). No story's
  acceptance criteria needed it, so the API always returns the latest ten, and the contract documents only that.

[pr1]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/1
[pr2]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/2
[pr3]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/3
[pr4]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/4
[pr5]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/5
[pr6]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/6
[pr7]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/7
[pr8]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/8
[pr9]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/9
[pr10]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/10
[pr11]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/11
[pr12]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/pull/12
[f10dea4]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/f10dea4
[8d94446]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/8d94446
[7d756b7]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/7d756b7
[621c6ad]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/621c6ad
[eff7e7b]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/eff7e7b
[237f80a]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/237f80a
[8a99699]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/8a99699
[8a7fcdb]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/8a7fcdb
[172a0ce]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/172a0ce
[47574b4]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/47574b4
[f90ef91]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/f90ef91
[b22458b]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/b22458b
[2d2f146]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/2d2f146
[3127b1b]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/3127b1b
[8898279]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/8898279
[bcd58f3]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/bcd58f3
[42ef32b]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/42ef32b
[b6a089d]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/b6a089d
[90b71a0]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/90b71a0
[5e6c5a2]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/5e6c5a2
[cfcf1d6]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/cfcf1d6
[dbf0ef1]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/dbf0ef1
[d786738]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/d786738
[0e85e4b]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/0e85e4b
[793e361]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/793e361
[db75faa]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/db75faa
[4f030b1]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/4f030b1
[9a40d29]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/9a40d29
[6c23902]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/6c23902
[de29d8e]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/de29d8e
[aa3c691]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/aa3c691
[6d8ac2e]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/6d8ac2e
[3471ad8]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/3471ad8
[b634c82]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/b634c82
[f4aea45]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/f4aea45
[7a2019a]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/7a2019a
[83c4d40]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/83c4d40
[48b2e83]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/48b2e83
[8405578]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/8405578
[c926831]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/c926831
[f9c8389]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/f9c8389
[06b22ee]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/06b22ee
[4cc3509]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/4cc3509
[9fa1976]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/9fa1976
[452a047]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/452a047
[1628b7b]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/1628b7b
[d3da47f]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/d3da47f
[fc741c5]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/fc741c5
[fe60f90]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/fe60f90
[c4f4ff9]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/c4f4ff9
[12fbda0]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/12fbda0
[69b99ca]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/69b99ca
[5b44675]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/5b44675
[88f70cc]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/88f70cc
[12fbe26]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/12fbe26
[b1a2c04]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/b1a2c04
[4040574]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/4040574
[d63f286]: https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/commit/d63f286
