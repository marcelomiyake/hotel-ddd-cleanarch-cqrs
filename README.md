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

The monorepo is analyzed as the existing SonarCloud project [`marcelomiyake_hotel-ddd-cleanarch-cqrs`](https://sonarcloud.io/project/overview?id=marcelomiyake_hotel-ddd-cleanarch-cqrs). The final analysis passed its quality gate and showed **0 open issues**, **0 security issues**, and **89.1% overall coverage**.

Run `bash scripts/sonar-scan.sh` with `SONAR_TOKEN` available in the environment. It runs Maven verification, frontend tests with coverage, the frontend build, and the scanner. The scan configuration and project key are in `sonar-project.properties`; no token is stored in the repository.

### Tests and line coverage

The final local run passed 17 Java tests and 26 frontend tests.

| Component | Line coverage |
| --- | ---: |
| Catalog service (JaCoCo) | 98.20% |
| Reservation service (JaCoCo) | 97.59% |
| React application (Vitest/V8) | 85.47% |
| SonarCloud monorepo analysis | 89.1% |

### Lighthouse and metadata

The final Lighthouse run against the Kind hosted frontend scored **100** in Performance, Accessibility, Best Practices, SEO, and Agentic Browsing. The report is generated at `web/lighthouse-report.json` and is ignored by Git.

The document includes a localized title and description, `lang`, viewport and theme metadata, robots directive, Open Graph and Twitter cards, favicon, hero image preload, and discovery links. `robots.txt`, `llms.txt`, and the `.well-known` AI catalog and ARD documents are served by the frontend. The Lighthouse SEO category scored 100.

The requested Chrome extension evaluation with **SEO META in 1 Click** could not be run: the connected Chrome session blocked its extension-management surface. I checked the document metadata directly and verified the Lighthouse SEO audit instead; this is not a result from the extension itself.

### OpenDesign handoff

The user supplied a self-hosted OpenDesign workspace and handoff path. The workspace endpoint responded, but the connected handoff could not return its active project or source artifact during this session. The frontend was implemented in the repository from the available design brief, but an OpenDesign source bundle was not imported or independently verified.

## Session, LOC, and cost estimate

**Harness:** Codex, GPT-6 Luna with max effort.

**LOC:** SonarCloud reported **3,862 non-comment lines of code** (`ncloc`) across 65 analyzed files at the final scan. This uses the SonarCloud source scope, not physical lines in documentation, generated files, images, or dependencies.

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
