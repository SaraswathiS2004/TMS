# TMS — Invitation Tracking System

A wedding/event invitation management system with a Servlet-based REST API and an Angular client.

---

## Prerequisites

- Java 17
- Maven 3.6+
- Node.js v22.12.0 (installed automatically by the Maven build via frontend-maven-plugin)
- MySQL 8.x running on `localhost:3306`
- A GitHub Personal Access Token (PAT) with `read:packages` scope — required to pull the `configm` dependency from GitHub Packages

---

## First-time machine setup

### 1. Create `~/.m2/settings.xml`

Maven needs credentials to pull the `configm` library from GitHub Packages.
Create the file if it doesn't exist:

```bash
mkdir -p ~/.m2
```

Then create `~/.m2/settings.xml` with the following content:

```xml
<settings>
    <servers>
        <server>
            <id>github-configM</id>
            <username>RamS-Dot-Dev</username>
            <password>YOUR_GITHUB_PAT</password>
        </server>
    </servers>
</settings>
```

Replace `YOUR_GITHUB_PAT` with a GitHub Personal Access Token that has the `read:packages` scope.

**To generate a PAT:**
1. Go to GitHub → Settings → Developer settings → Personal access tokens → Tokens (classic)
2. Click **Generate new token**
3. Select scope: `read:packages`
4. Copy the token and paste it into `settings.xml`

> This file lives outside the repo and is never committed — it contains your credentials.

---

### 2. Set up the MySQL database

```sql
CREATE DATABASE Tms;
```

Tables are created automatically on first startup via `SchemaInit.java`.

Update DB credentials in `TmsDB.java` if your MySQL user/password differs from the defaults.

---

## Build

```bash
# Build WAR (includes Angular client)
JAVA_HOME=/path/to/jdk17 mvn clean package -DskipTests

# Skip Angular build (faster, backend only)
JAVA_HOME=/path/to/jdk17 mvn clean package -DskipTests -P skipFrontend
```

The WAR is output to `target/Tms.war`.

---

## Run

### Backend via Tomcat plugin

```bash
JAVA_HOME=/path/to/jdk17 mvn tomcat7:run
# API available at http://localhost:8080
```

### Angular dev server (separate terminal)

```bash
cd client
npm install        # first time only
npm start          # http://localhost:4200
```

Start the backend first, then the Angular dev server.

---

## Google Sheet Sync (optional)

TMS can back up and restore data to/from a Google Sheet.

### Setup

1. Create a Google Cloud service account and download its JSON credentials file.
2. Place the file in `WEB-INF/classes/` (default name: `google-credentials.json`).
3. Go to the Admin page in the UI and link or create a spreadsheet.

### Server modes

| Mode | Behaviour |
|------|-----------|
| `READ_WRITE` | All operations allowed. Every data change is synced to the sheet automatically. Default. |
| `READ_ONLY` | Only GET requests allowed. No sync tasks run. Use this on a standby server. |

Switch mode via the Admin page or directly:

```bash
curl -X POST http://localhost:8080/api/admin/set-mode \
     -H "Content-Type: application/json" \
     -d '{"mode":"READ_ONLY"}'
```

The mode is persisted in `WEB-INF/classes/sheet-config.properties` and survives server restarts.

### Restore data from sheet (fresh server)

```bash
curl -X POST http://localhost:8080/api/admin/restore-from-sheet
```

---

## GitHub Actions CI

| Event | Result |
|-------|--------|
| Push to `main` | Builds WAR, uploads as Actions artifact (90 days) |
| Push tag `v1.0.0` | Builds WAR, publishes to GitHub Packages, creates GitHub Release |

The CI workflow uses the `PACKAGES_TOKEN` secret (a PAT with `read:packages` and `write:packages`)
stored in the repository's Actions secrets. No `settings.xml` is needed on GitHub Actions —
the `.github/maven-settings.xml` in the repo handles authentication.

**To cut a release:**
```bash
git tag v1.0.0
git push origin v1.0.0
```
