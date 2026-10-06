# Berlin Clock

[![CI](https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/actions/workflows/ci.yml/badge.svg)](https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock/actions/workflows/ci.yml)

The [Berlin Clock kata](https://stephane-genicot.github.io/BerlinClock.html), built test-first as
vertical slices: **React UI → Spring Boot REST API → PostgreSQL**.

Watch the current time tick on a live Berlin Clock, type a time to convert it, and find your earlier conversions
in a persisted history.

> Delivery is organised as user stories, each one a full vertical slice you can try in the running app.
> See [`docs/Berlin-Clock-User-Stories.pdf`](docs/Berlin-Clock-User-Stories.pdf).

| Story | Status |
|---|---|
| A1 · Walking skeleton: convert a time, see the seconds lamp, find it in history | ✅ Done |
| A2 · Clear feedback for invalid times | ✅ Done |
| A3 · Five-hours row | ✅ Done |
| A4 · Single-hours row | ✅ Done |
| A5 · Five-minutes row | ✅ Done |
| A6 · Single-minutes row | ✅ Done |
| A7 · The entire Berlin Clock as one 24-character code | ✅ Done |
| B1 · Revisit a past conversion | ✅ Done |
| B2 · Clear the history | ✅ Done |
| C1 · Live clock ticking every second | ✅ Done |
| C2 – D2 | Planned (see the PDF) |

---

## Quick start (Docker)

Requirements: **Git** and **Docker** (Docker Desktop or Docker Engine with Compose v2).

```bash
git clone https://github.com/malwal86/2026-CCE-E-DEV-010-BerlinClock.git
cd 2026-CCE-E-DEV-010-BerlinClock
docker compose up --build
```

Then open **http://localhost:3000**.

| Service | URL | Notes |
|---|---|---|
| UI (nginx) | http://localhost:3000 | Proxies `/api` to the backend: one origin, no CORS |
| API (Spring Boot) | http://localhost:8080/api/conversions | |
| PostgreSQL 17 | `localhost:5433`, db `berlin_clock`, user/password `berlin` | Host port 5433 avoids clashing with a local PostgreSQL |

Stop with `Ctrl+C`, or `docker compose down`. The history survives restarts. To wipe it, use **Clear history** in the page (story B2) or
`docker compose down -v`.

## Try it yourself (story A1)

1. Type `00:00:00` and press **Convert** (or Enter). The round seconds lamp is **yellow** (even second).
2. Type `23:59:59` and convert. The lamp is **off** (odd second), and `23:59:59` is now at the top of **Recent conversions**.
3. Run `docker compose restart`, then refresh the page. Both conversions are still listed (latest 10, newest first).
4. Optional, look at the table: `docker compose exec db psql -U berlin -d berlin_clock -c "select * from conversion"`.

The same through the API:

```bash
curl -i -X POST localhost:3000/api/conversions -H 'Content-Type: application/json' -d '{"time":"00:00:00"}'
# HTTP/1.1 201   Location: http://localhost:3000/api/conversions/1
# {"id":1,"time":"00:00:00","convertedAt":"2026-10-05T15:48:09.127126Z","seconds":"Y"}

curl localhost:3000/api/conversions
# [{"id":1,"time":"00:00:00","convertedAt":"…","seconds":"Y"}]
```

## Try it yourself (story A2)

1. Type `12-00-00` and press **Convert**. The message *Invalid time '12-00-00': expected HH:mm:ss between
   00:00:00 and 23:59:59* appears under the field, and **Recent conversions** does not change.
2. Convert `00:00:00`, then `25:00:00`. The message appears and the previous result is cleared.
3. Clear the field and press **Convert**. The message is *A time is required (HH:mm:ss)*.

Also rejected: `24:00:00`, `12:60:00`, `12:00:60`, `1:2:3`, `12:00`, `noon`. Through the API:

```bash
curl -i -X POST localhost:3000/api/conversions -H 'Content-Type: application/json' -d '{"time":"25:00:00"}'
# HTTP/1.1 400   Content-Type: application/problem+json
# {"detail":"Invalid time '25:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59",
#  "instance":"/api/conversions","status":400,"title":"Invalid time"}
```

## Try it yourself (story A3)

1. Type `16:35:00` and press **Convert**. Under the seconds lamp, the five-hours row shows **three red lamps and
   one dark lamp** (16 ÷ 5 = 3 full blocks of five hours).
2. Look at an older `23:59:59` entry in **Recent conversions**: its five-hours row now shows **four red lamps**,
   even though it was saved before this story. The row is computed on read, so no migration was needed.

Other kata examples: `00:00:00` → `OOOO`, `02:04:00` → `OOOO`, `08:23:00` → `ROOO`. Through the API:

```bash
curl -s -X POST localhost:3000/api/conversions -H 'Content-Type: application/json' -d '{"time":"16:35:00"}'
# {"id":3,"time":"16:35:00","convertedAt":"…","seconds":"Y","fiveHours":"RRRO"}
```

## Try it yourself (story A4)

1. Type `14:35:00` and press **Convert**. Rows 2 and 3 read **`RROO` / `RRRR`**: two blocks of five hours plus
   four single hours (2 × 5 + 4 = 14 h).
2. Type `02:04:00` and press **Convert**. Row 2 is all dark and row 3 shows **two red lamps** (`RROO`).
3. Older entries in **Recent conversions** gain the single-hours row too (`23:59:59` → `RRRO`), computed on read.

Other kata examples: `00:00:00` → `OOOO`, `08:23:00` → `RRRO`. Through the API:

```bash
curl -s -X POST localhost:3000/api/conversions -H 'Content-Type: application/json' -d '{"time":"14:35:00"}'
# {"id":4,"time":"14:35:00","convertedAt":"…","seconds":"Y","fiveHours":"RROO","singleHours":"RRRR"}
```

## Try it yourself (story A5)

1. Type `12:35:00` and press **Convert**. Row 4 shows **Y Y R Y Y R Y** then four dark lamps: seven blocks of five
   minutes (35 min), with the quarter and half hour marked in red.
2. Type `12:15:00` and press **Convert**. Row 4 reads **Y Y R**: the quarter-hour marker is red.
3. Older entries in **Recent conversions** gain the five-minutes row too (`23:59:59` → `YYRYYRYYRYY`), computed on read.

Other kata examples: `00:00:00` and `12:04:00` → `OOOOOOOOOOO`, `12:23:00` → `YYRYOOOOOOO`. Through the API:

```bash
curl -s -X POST localhost:3000/api/conversions -H 'Content-Type: application/json' -d '{"time":"12:35:00"}'
# {"id":5,"time":"12:35:00","convertedAt":"…","seconds":"Y","fiveHours":"RROO","singleHours":"RROO","fiveMinutes":"YYRYYRYOOOO"}
```

## Try it yourself (story A6)

1. Type `12:34:00` and press **Convert**. The bottom row lights **Y Y Y Y**, and the clock is complete: it reads
   12:34 as 2 × 5 h + 2 h, then 6 × 5 min + 4 min.
2. Type `12:35:00` and press **Convert**. The bottom row goes dark (`OOOO`), because the extra minute moves into row 4.
3. Every older entry in **Recent conversions** now shows a complete clock, computed on read.

Other kata examples: `00:00:00` → `OOOO`, `23:59:59` → `YYYY`, `12:32:00` → `YYOO`. Through the API:

```bash
curl -s -X POST localhost:3000/api/conversions -H 'Content-Type: application/json' -d '{"time":"12:34:00"}'
# {"id":6,"time":"12:34:00","convertedAt":"…","seconds":"Y","fiveHours":"RROO","singleHours":"RROO","fiveMinutes":"YYRYYROOOOO","singleMinutes":"YYYY"}
```

## Try it yourself (story A7)

The kata's Feature 1 is complete: the whole clock is also one 24-character code, every lamp from top to bottom
(seconds + five hours + single hours + five minutes + single minutes = 1 + 4 + 4 + 11 + 4).

1. Type `16:50:06` and press **Convert**. Under the clock you see **`YRRROROOOYYRYYRYYRYOOOOO`**. Click **Copy**
   (it reads *Copied* for a moment) and paste it somewhere.
2. Every entry in **Recent conversions** shows its code and its own **Copy** button, including the ones saved
   before this story (computed on read).
3. Turn on VoiceOver (`Cmd+F5`) and move to the clock: it is read as *"Berlin Clock showing 16:50:06"*, so it can
   be read without seeing colours.

Other kata examples: `00:00:00` → `YOOOOOOOOOOOOOOOOOOOOOOO`, `23:59:59` → `ORRRRRRROYYRYYRYYRYYYYYY`,
`11:37:01` → `ORROOROOOYYRYYRYOOOOYYOO`. Through the API:

```bash
curl -s -X POST localhost:3000/api/conversions -H 'Content-Type: application/json' -d '{"time":"16:50:06"}'
# {"id":7,"time":"16:50:06","convertedAt":"…","clock":"YRRROROOOYYRYYRYYRYOOOOO","seconds":"Y","fiveHours":"RRRO",
#  "singleHours":"ROOO","fiveMinutes":"YYRYYRYYRYO","singleMinutes":"OOOO"}
```

## Try it yourself (story B1)

1. In **Recent conversions**, click an older time (for example `23:59:59`). It opens in full in the **Result**
   panel, the address bar changes to `/conversions/<id>`, and the entry is highlighted as the one shown. Nothing
   is added to the history.
2. Copy the address into a new tab, or reload the page: the same conversion opens. Cmd/Ctrl-clicking an entry
   opens it in a new tab too.
3. Click two entries, then use the browser's **Back** and **Forward** buttons: the result follows.
4. Change the id in the address to `999999`: the result panel says *"Conversion 999999 not found"*.

Through the API (the `Location` returned by a POST is the address to read it again):

```bash
curl -s localhost:3000/api/conversions/7
# {"id":7,"time":"16:50:06","convertedAt":"…","clock":"YRRROROOOYYRYYRYYRYOOOOO",…}
curl -s localhost:3000/api/conversions/999999
# {"detail":"Conversion 999999 not found","instance":"/api/conversions/999999","status":404,"title":"Conversion not found"}
```

## Try it yourself (story B2)

1. Under **Recent conversions**, click **Clear history**. The page asks *"Delete all conversions? This cannot be
   undone."* with **Delete** and **Cancel** (no browser pop-up). Click **Cancel**: nothing changes.
2. Click **Clear history** again, then **Delete**: the list says *"No conversions yet"* and the button is disabled.
   If the result panel showed a conversion, it now says *"Conversion <id> not found"*.
3. `docker compose restart`, then refresh the page: the history is still empty.
4. Optional, count the rows: `docker compose exec db psql -U berlin -d berlin_clock -c "select count(*) from conversion"` → 0.

Through the API (clearing an empty history is fine too):

```bash
curl -s -X DELETE localhost:3000/api/conversions -o /dev/null -w '%{http_code}\n'
# 204
curl -s localhost:3000/api/conversions
# []
```

## Try it yourself (story C1)

1. Open http://localhost:3000. The **Live** panel at the top shows your computer's current time as a Berlin Clock,
   with the digital time and the code under it. It ticks every second and the round seconds lamp blinks.
2. Wait a minute: **Recent conversions** is unchanged. Ticks are only read, never saved.
3. DevTools → **Network**: one small `GET /api/berlin-clock?time=…` per second, and no `POST`.
4. Optional, change your computer's time zone: the live clock follows the browser's local time, not the server's.

Through the API (the endpoint is read-only, the database is not touched):

```bash
curl -s 'localhost:3000/api/berlin-clock?time=16:50:06'
# {"time":"16:50:06","clock":"YRRROROOOYYRYYRYYRYOOOOO","seconds":"Y","fiveHours":"RRRO","singleHours":"ROOO",
#  "fiveMinutes":"YYRYYRYYRYO","singleMinutes":"OOOO"}
curl -s 'localhost:3000/api/berlin-clock?time=24:00:00'
# {"detail":"Invalid time '24:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59","instance":"/api/berlin-clock",…}
```

## Run natively (for development)

Requirements: **JDK 21+**, **Node.js 22+**, **Docker** (for the database).

```bash
docker compose up -d db                       # PostgreSQL on localhost:5433
cd backend  && ./mvnw spring-boot:run          # API on http://localhost:8080
cd frontend && npm ci && npm run dev           # UI on http://localhost:5173 (proxies /api to :8080)
```

Database settings can be overridden with `DB_URL`, `DB_USERNAME` and `DB_PASSWORD`.

## Run the tests

```bash
cd backend  && ./mvnw verify      # needs Docker running: tests use a real PostgreSQL via Testcontainers
cd frontend && npm test           # Vitest + Testing Library + MSW
```

| Layer | Tooling | Example |
|---|---|---|
| Domain (Berlin Clock rules) | JUnit 5, AssertJ, parameterised kata tables, a property check over all 86,400 seconds | `BerlinClockTest`, `SecondsLampTest`, `FiveHoursRowTest`, `SingleHoursRowTest`, `FiveMinutesRowTest`, `SingleMinutesRowTest`, `DigitalTimeTest` |
| Use case | JUnit 5 + in-memory history fake + fixed `Clock` | `ConversionServiceTest` |
| Persistence | `@JdbcTest` + Testcontainers PostgreSQL + Flyway | `JdbcConversionHistoryTest` |
| Web | `@WebMvcTest` + `MockMvcTester` | `ConversionControllerTest`, `LiveClockControllerTest` |
| Acceptance (full stack) | `@SpringBootTest` + `RestTestClient` + Testcontainers | `ConversionApiAcceptanceTest`, `LiveClockApiAcceptanceTest` |
| UI | Vitest, React Testing Library, MSW, fake timers for the live clock | `App.test.tsx`, `BerlinClock.test.tsx`, `ClockCode.test.tsx`, `LiveClock.test.tsx` |

Coverage: `backend/target/site/jacoco/index.html` (the build **fails** below 100% line/branch coverage
on the `clock` package) and `npm run coverage` for the frontend.

## Project structure

```
backend/                     Spring Boot 4.1 · Java 21
  src/main/java/com/kata/berlinclock/
    clock/                   Feature: Berlin Clock rules (BerlinClock, Lamp, LampRow) and the strict HH:mm:ss
                             input contract (DigitalTime), pure Java, no framework
    conversion/              Feature: conversion history, layered inside
      web/                   ConversionController, request/response DTOs, ConversionNotFoundException (404)
      application/           ConversionService use case, Conversion, ConversionHistory port
      persistence/           JdbcClient implementation of the port
    live/                    Feature: read-only GET /api/berlin-clock for the live clock (web layer only, no DB)
    error/                   ApiExceptionHandler: errors shared by every feature, as problem+json
    BerlinClockApplicationConfiguration   System Clock bean (JDK type, so declared with @Bean)
  src/main/resources/db/migration/   Flyway migrations
frontend/                    React 19 · TypeScript · Vite
  src/clock/                 Feature: BerlinClock, LampRow and ClockCode components + lamp/row types
  src/conversion/            Feature: ConvertForm, RecentConversions, API client, /conversions/:id route, Conversion type
  src/live/                  Feature: LiveClock panel and the useLiveBerlinClock hook (one GET per second)
  src/App.tsx                Page: wires the features together
docs/                        User stories (PDF + HTML source)
docker-compose.yml           db + backend + frontend
```

## Design decisions (so far)

- **All Berlin Clock logic lives in the backend `clock` package.** React only renders what the API returns.
- **The database stores facts only** (the input time and when it was converted). The Berlin Clock is computed
  on read, so there is one source of truth and no stale data.
- **POST saves, GET reads.** `POST /api/conversions` returns `201 Created` with a `Location` header, and
  `GET /api/conversions/{id}` reads that conversion again (`404` problem detail when there is none).
  `DELETE /api/conversions` clears the history with `204 No Content`, and is idempotent.
- **The live clock sends the browser's time and saves nothing.** Every second the page asks the read-only
  `GET /api/berlin-clock?time=HH:mm:ss` for its own local time, so the rules stay in the backend and the server's
  time zone (UTC inside Docker) never matters. Ticks are not saved: one row a second per tab would bury the real
  history. Requests are numbered and an answer older than the one shown is dropped, so the clock never moves backwards;
  each tick is scheduled for the next whole second.
- **Destructive actions are confirmed inside the page**, not with the browser's `confirm()`: the question stays
  styled, testable and accessible, and focus starts on **Cancel**.
- **The address matches the result.** Whatever the result panel shows lives at `/conversions/<id>`, so it can be
  shared, reloaded or reached with Back. A small path helper replaces a router library; nginx (and Vite in dev)
  serve `index.html` for any page.
- **Strict input, checked in one place.** `DigitalTime` accepts only zero-padded `HH:mm:ss` from 00:00:00 to
  23:59:59 (`24:00:00` is rejected). The API takes the time as text so JSON binding cannot loosen the rule,
  and an invalid time never reaches the database.
- **Errors are RFC 9457 problem details** (`application/problem+json`, with `title`, `status`, `detail`, `instance`).
  The UI shows `detail` as written.
- **Rows travel in the kata notation** (`"fiveHours": "RRRO"`): one character per lamp, left to right. The UI's
  generic `LampRow` draws any row from it, so each later row only adds a field. The whole clock travels as
  `"clock"`, the kata's 24-character code, next to the rows.
- **Readable without colours.** Each clock is one `role="img"` labelled *"Berlin Clock showing HH:mm:ss"*, and
  its code is shown as text with a copy button.
- **TDD, committed per layer.** Each commit is green and lists its red → green cycles in the message.

See Appendix C of the user stories PDF for the full list.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `port is already allocated` for 3000 / 8080 / 5433 | Stop whatever uses the port, or change the left-hand port in `docker-compose.yml` |
| Backend tests fail with "Could not find a valid Docker environment" | Start Docker; Testcontainers needs it |
| `role "berlin" does not exist` when running natively | You are connecting to another PostgreSQL. The Compose database is on port **5433** |
