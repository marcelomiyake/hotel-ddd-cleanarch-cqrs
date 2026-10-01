# Wayfarer Stays

Wayfarer is a hotel reservation system based on the [ByteByteGo hotel reservation system case study](https://bytebytego.com/courses/system-design-interview/hotel-reservation-system). It supports city search, hotel and room details, date based availability, reservation creation, reservation history, and cancellation.

![Wayfarer Stays landing page captured by the Lighthouse mobile audit](docs/wayfarer-home-lighthouse.jpg)

*Landing page screenshot from the Lighthouse mobile audit.*

## Product scope

- Search the hotel catalog by city and inspect hotel details and room types.
- Check nightly availability for a date range, guest count, and room count.
- Create a reservation with a client supplied idempotency key.
- Find and cancel reservations using the guest email address.
- Browse a responsive editorial landing page, hotel collection, checkout, confirmation, and trip history.

Reservation commands run transactionally. PostgreSQL advisory locks serialize requests with the same idempotency key, and inventory rows are locked by stay date before availability changes. The catalog and reservation services use separate PostgreSQL schemas in one local PostgreSQL instance.

## Architecture

The backend is Java 25 with Spring Boot. The web application is React 19.3 and TypeScript. PostgreSQL migrations and the Kubernetes manifests are in the same monorepo.

| Area | Responsibilities |
| --- | --- |
| `services/catalog-service` | Hotel search and hotel detail queries; catalog domain and PostgreSQL adapter |
| `services/reservation-service` | Availability queries, reservation and cancellation commands, inventory and reservation domains, PostgreSQL adapters |
| `web/src/domain` | Browser side hotel, reservation, and stay rules and models |
| `web/src/application` | Search and reservation queries/commands expressed against ports |
| `web/src/infrastructure` | HTTP adapters for catalog and reservation APIs |
| `web/src/presentation` | React screens and components; reservation screens are lazy loaded |
| `k8s` | PostgreSQL StatefulSet and two replica Deployments for catalog, reservation, and web |

The backend packages follow domain, application, inbound adapter, and outbound adapter boundaries. Application use cases depend on ports; the web layer uses the same dependency direction with browser side domain rules, application operations, infrastructure gateways, and presentation components. Catalog reads and reservation commands/queries have separate use cases and HTTP endpoints, while sharing the local database instance.

## API routes

The web server proxies these paths to the two backend services:

| Method | Web path | Purpose |
| --- | --- | --- |
| `GET` | `/api/catalog/hotels?city=...` | Search hotels |
| `GET` | `/api/catalog/hotels/{hotelId}` | Read hotel details and room types |
| `GET` | `/api/booking/availability?checkIn=...&checkOut=...&guests=...` | Read room availability |
| `POST` | `/api/booking/reservations` | Create a reservation; requires `Idempotency-Key` |
| `GET` | `/api/booking/reservations?email=...` | List a guest’s reservations |
| `GET` | `/api/booking/reservations/{reservationId}?email=...` | Read a reservation |
| `DELETE` | `/api/booking/reservations/{reservationId}?email=...` | Cancel a reservation |

## Run locally with Kind

Prerequisites: Java 25, Node.js 24, Docker, Kind, and `kubectl`.

```bash
npm ci
./mvnw -f services/pom.xml verify
npm --workspace web run test -- --coverage
npm --workspace web run build
bash scripts/kind-up.sh
kubectl --context kind-hotel-system -n hotel-reservation get pods
kubectl --context kind-hotel-system -n hotel-reservation port-forward svc/web 4173:80
```

Open <http://127.0.0.1:4173>. `kind-up.sh` builds the three local images, loads them into Kind, applies the PostgreSQL and service manifests, and restarts each application Deployment so rebuilt local images are used. Each application Deployment has two replicas; PostgreSQL has one StatefulSet replica. To remove the application namespace, run `bash scripts/kind-down.sh`.

## Verification and quality

### SonarCloud

The monorepo is analyzed as the existing SonarCloud project [`marcelomiyake_hotel-ddd-cleanarch-cqrs`](https://sonarcloud.io/project/overview?id=marcelomiyake_hotel-ddd-cleanarch-cqrs). The latest analysis passed its quality gate and reports **0 open issues**, **0 bugs**, **0 vulnerabilities**, **0 code smells**, and **90.4% overall coverage** across **4,083 non-comment lines of code**.

Run `bash scripts/sonar-scan.sh` with `SONAR_TOKEN` available in the environment. It runs Maven verification, frontend tests with coverage, the frontend build, and the scanner. The scan configuration and project key are in `sonar-project.properties`; no token is stored in the repository.

### Tests and line coverage

The current local run passed 19 Java tests and 33 frontend tests.

| Component | Line coverage |
| --- | ---: |
| Catalog service (JaCoCo) | 98.20% |
| Reservation service (JaCoCo) | 97.92% |
| React application (Vitest/V8) | 87.82% |
| SonarCloud monorepo analysis | 90.4% |

### Lighthouse and metadata

The production frontend build, served by Vite preview with the local Kind services behind its API proxy, scored **100** in Performance, Accessibility, Best Practices, SEO, and Agentic Browsing. The report is generated at `web/lighthouse-report.json` and is ignored by Git.

The document includes a localized title and description, `lang`, viewport and theme metadata, robots directive, Open Graph and Twitter cards, favicon, hero image preload, and discovery links. `robots.txt`, `llms.txt`, and the `.well-known` AI catalog and ARD documents are served by the frontend. The Lighthouse SEO category scored 100.

The requested Chrome extension evaluation with **SEO META in 1 Click** could not be run: the connected Chrome session blocked its extension-management surface. I checked the document metadata directly and verified the Lighthouse SEO audit instead; this is not a result from the extension itself.

### OpenDesign handoff

The user supplied a self-hosted OpenDesign workspace and handoff path. The workspace endpoint responded, but the connected handoff could not return its active project or source artifact during this session. The frontend was implemented in the repository from the available design brief, but an OpenDesign source bundle was not imported or independently verified.

## Session, LOC, and cost estimate

**Harness:** Codex, GPT-6 Luna with max effort.

**LOC at the initial implementation scan:** SonarCloud reported **3,862 non-comment lines of code** (`ncloc`) across 65 analyzed files before the reservation abandonment feature. This uses the SonarCloud source scope, not physical lines in documentation, generated files, images, or dependencies.

The session usage snapshot below was read after implementation and verification, before the final usage figures and response were written. The earlier session and cache state were empty. Cached input is included in the input total; reasoning tokens are a subset of output tokens.

| Session usage | Tokens |
| --- | ---: |
| Input, including cached input | 39,684,921 |
| Cached input (subset of input) | 38,729,984 |
| Reasoning (subset of output) | 148,324 |
| Output | 276,569 |

Worked for 3h 46m 53s

The estimated cost is **US$0.62** at the [OpenAI API’s current standard short-context GPT-6 Luna rates](https://developers.openai.com/api/docs/pricing): $0.10 per million uncached input tokens, $0.01 per million cached input tokens, and $0.50 per million output tokens. The estimate prices 954,937 uncached input tokens separately from cached input and counts reasoning once as part of output. It is an API list-rate estimate, not a Codex subscription invoice.

## Original prompt

```text
Implement https://bytebytego.com/courses/system-design-interview/hotel-reservation-system in Java 25, React 19.3 (handoff from OpenDesign [To start it, from ~/.local/share/open-design, run ./node_modules/.bin/tools-dev start. Web UI: http://127.0.0.1:41919, self-hosted]), PostgreSQL, and Kubernetes via local Kind (2 replicas for each microservice). Use SonarQube Cloud via Chrome (https://sonarcloud.io/organizations/marcelomiyake/) to create and manage these monorepo projects, and complete this job with zero SonarQube issues and test coverage above 80%. If you need to run the scanner from the command line, I updated ~/.zshrc with the SONAR_TOKEN, but you can also use GitHub Actions and push commits in a loop until the issues are clean; if you generate another SONAR_KEY, update it in the GitHub project or in .zshrc. The frontend should have a perfect Lighthouse grade and a good SEO META evaluation in 1 Click. Finally, update the README.md with an analysis that includes this prompt, the harness used here (Codex, GPT-6 Luna with max effort), and the token costs from the sessions to complete this task (input tokens, cache tokens, reasoning tokens, output tokens) and LOC. The cache and sessions were empty just before starting this session. Consult the OpenAI official documentation for token prices to estimate total costs. This implementation must follow DDD, Clean Architecture, SOLID, and CQRS in the backend and frontend.
```

## Reservation abandonment analytics

### Prompt analysis and implementation

The requested feature needs to distinguish a checkout that was started from one that finished, preserve the last screen visited, and make the event data available for future analysis without an administrative UI. The browser now emits events for reservation start, later screen views, and successful reservation confirmation. Events are stored in the reservation service's PostgreSQL schema, and the `reservation_funnel_abandonments` view exposes attempts that have been inactive for 30 minutes, were started, and have no confirmation. The view includes the last screen plus hotel and room identifiers.

The current checkout explicitly offers **pay at the property** and has no online payment step. For this implementation, a successful reservation confirmation is the completion event; an attempt that remains unconfirmed becomes an abandonment after 30 minutes without activity. The event model stores random session and attempt UUIDs, screen names, and hotel and room identifiers. It does not collect a guest name or email. Analytics writes are best effort and do not block reservation actions. Events share the existing PostgreSQL database and schema; no admin frontend or new user-facing booking step was added.

Tracked screens are `home`, `results`, `hotel`, `checkout`, `confirmation`, and `trips`. The screen at the start event is `checkout`; subsequent screen events update the last screen in the database view. A real online payment flow can later replace the confirmation event as the conversion signal.

### Change summary

| Change type | Count |
| --- | ---: |
| Files created | 15 |
| Files modified | 11 |
| Files deleted | 0 |
| Lines added | 590 |
| Lines deleted | 31 |
| Changed line pairs (min of additions and deletions in modified files) | 31 |
| Net line delta | +559 |

**Feature source LOC delta:** SonarCloud reports **+221 ncloc** (4,083 current versus 3,862 at the prior analysis).

### Verification, LOC, and session cost

**Harness:** Codex, GPT-6 Luna, max effort, as specified for this task.

**LOC:** SonarCloud reports **4,083 non-comment lines of code** (`ncloc`) across 88 analyzed files after this feature. LOC excludes generated output and dependencies.

The regression suites report 33 frontend tests with 87.82% line coverage. Java verification passed 19 tests; JaCoCo line coverage was 98.20% for Catalog and 97.92% for Reservation. SonarCloud reports **0 unresolved issues** and 90.4% overall coverage. The production frontend Lighthouse run scored 100 in Performance, Accessibility, Best Practices, SEO, and Agentic Browsing. Lighthouse SEO scored 100. The Chrome extension evaluation with **SEO META in 1 Click** could not be opened because browser policy rejected the Chrome extension-management URL; the static document metadata and Lighthouse SEO audit were checked instead. This was not a result from the extension.

The prompt states that session and cache usage were empty before this task. I found one local Codex session log for 2026-10-01. It contains repeated cumulative `thread_token_usage` snapshots, so the totals below use the final snapshot rather than adding snapshots together. Input includes cached input; reasoning is a subset of output. The log reports zero cache-write input tokens.

| Session usage | Tokens |
| --- | ---: |
| Implementation time | 56m 5s |
| Input, including cached input | 19,341,292 |
| Cached input (subset of input) | 19,002,624 |
| Uncached input (derived) | 338,668 |
| Cache-write input | 0 |
| Reasoning (subset of output) | 65,429 |
| Output, including reasoning | 94,710 |
| Estimated API token cost | $0.27124804 |

Using the [official OpenAI API pricing page](https://developers.openai.com/api/docs/pricing), this estimate applies GPT-6 Luna Standard short-context rates: **$0.10 per million uncached input tokens**, **$0.01 per million cached input tokens**, and **$0.50 per million output tokens**. Calculation: `((19,341,292 - 19,002,624) × 0.10 + 19,002,624 × 0.01 + 94,710 × 0.50) / 1,000,000 = $0.27124804`. Reasoning is already included in output and is not priced twice. This API list-rate estimate is not a Codex subscription invoice.

### Exact prompt

```text
I want you to implement a new feature that identifies when a user starts a reservation but abandons it before paying. I want us to track which screen the user stopped at before abandoning the reservation. No administrative frontend implementation is needed; I want the data stored in the database (it doesn't need to be the same existing relational database) so we can use it later to improve the system. So it's not necessary to change the frontend features for the user, but you can change the structure to track user events. In this case, if you change it, the frontend should have a perfect Lighthouse grade and good SEO META in 1 Click. Complete this job with zero SonarQube issues (not only new, but zero in total) and test coverage above 80%. I also want to add a new section to README.md with statistics for this new feature. Include the number of changes (how much was deleted, created, changed, etc.), an analysis that includes this prompt, the harness used here (Codex, GPT-6 Luna with max effort), and the token costs from the sessions to complete this task (input tokens, cache tokens, reasoning tokens, output tokens), plus LOC. The cache and sessions were empty just before starting this session. Consult the OpenAI official documentation for token prices to estimate total costs. Commit following https://www.conventionalcommits.org/and push to GitHub after all.
```
