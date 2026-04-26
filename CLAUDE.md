# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TMS is an invitation tracking system for managing wedding/event invitees. It exposes both a Servlet-based REST API and a menu-driven CLI. Built as a Maven WAR targeting Java 17, deployed to Tomcat.

## Build & Run

### Backend (Java/Maven)
```bash
# Build WAR
mvn clean package

# Run via Tomcat plugin (API at http://localhost:8080)
mvn tomcat7:run

# Run CLI entry point directly
mvn exec:java -Dexec.mainClass="com.tms.Main"
```

### Angular Client
```bash
cd client

# Install dependencies (first time only)
npm install

# Start dev server at http://localhost:4200
npm start

# Production build → dist/tms-client/
npm run build
```

Start the backend first, then the Angular dev server. The client talks to the API at `http://localhost:8080/tms/people`.

## Database Setup

MySQL is required. Database name: `Tms`, table: `Invitations`.

Columns: `ID` (PK), `NAME`, `CITY`, `RELATION_TYPE`, `NUMBER_OF_PEOPLE_WILL_COME`, `INVITED_STATUS`.

Connection is configured in `TmsDB.java` (hardcoded to `localhost:3306`, user `root`).

## Architecture

The project uses a layered architecture with both web and CLI surfaces:

### Layers

| Package | Role |
|---------|------|
| `com.tms.servlets` | HTTP layer — `JsonServlet` (abstract base for JSON I/O) + `PeopleServlet` |
| `com.tms.servlets.actions` | Business logic — `PeopleActions` orchestrates feature calls |
| `com.tms.features` | Feature modules — each feature has a paired `*View` (request parsing) and `*Model` (DB query/response) |
| `com.tms.data` | Data layer — `TmsDB` (singleton JDBC connection), `People`/`PeopleInvited` DTOs, `RelationType`/`InvitedStatus` enums |
| `com.tms.util` | `JackSonUtils` (Jackson wrapper), `Input` (singleton Scanner for CLI) |
| `com.tms.servlets.message` | `Message` DTO with `Status` enum (SUCCESS/FAIL) returned from all servlet responses |

### Request Flow (Web)

```
HTTP → PeopleServlet → PeopleActions → Feature *View (parse) → Feature *Model (SQL) → Message (JSON response)
```

### CLI Flow

```
Main (menu) → Feature *View (console input) → Feature *Model (SQL) → console output
```

### Key Design Patterns

- **Singleton**: `TmsDB` (DB connection), `Input` (Scanner)
- **View+Model per feature**: Each of the 7 features has a `*View` that parses input and a `*Model` that executes SQL and returns results
- **No ORM**: Direct JDBC with raw SQL in `*Model` classes
- **Enum-driven state**: `RelationType` (CLOSE, DISTANCE, FRIENDS) and `InvitedStatus` (NOT_INVITED, MARRIAGE_INVITED, ENGAGEMENT_INVITED, BOTH_INVITED)

### Adding a New Feature

1. Create `<Feature>View.java` and `<Feature>Model.java` in `com.tms.features`
2. Wire into `PeopleActions` for web, and add a menu case in `Main` for CLI
3. Follow the existing View+Model naming and structure

## Angular Client (`client/`)

Angular 17 standalone-component app. Three routes/components, one service, one model file.

| Path | Component | Purpose |
|------|-----------|---------|
| `/dashboard` | `DashboardComponent` | Summary stats (totals, per-event invited counts and expected attendee sums) |
| `/add` | `AddPersonComponent` | Reactive form to add a person; city field uses `<datalist>` populated from existing records |
| `/list` | `PeopleListComponent` | Card grid with filter tabs (default: Not Invited); inline popup to mark as Engagement / Marriage / Both |

**Key files:**
- `client/src/app/models/people.model.ts` — `People`, `ApiMessage`, `RelationType`, `InvitedStatus` types
- `client/src/app/services/people.service.ts` — all API calls (GET all, GET by status, POST add, PUT update status)
- `client/src/styles.css` — shared badge and card classes used across all components

**API calls made by the client:**
- `GET /tms/people` → all people (dashboard + city autocomplete)
- `GET /tms/people?type=<STATUS>` → filtered list
- `POST /tms/people` → add person
- `PUT /tms/people` body `{ id, invitedStatus }` → update status

CORS is handled by `CorsFilter.java` (allows `http://localhost:4200`).

## Key Files

- `src/main/java/com/tms/data/respository/TmsDB.java` — DB connection singleton (update credentials here)
- `src/main/java/com/tms/data/dto/People.java` — primary DTO
- `src/main/java/com/tms/servlets/CorsFilter.java` — CORS filter for Angular client
- `src/main/webapp/WEB-INF/web.xml` — servlet and filter mappings
- `pom.xml` — Maven dependencies and Tomcat plugin config
- `client/src/app/services/people.service.ts` — single Angular service for all API calls
