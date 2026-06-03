// Standalone budget migration for the TMS app — NOT part of the Maven build.
//
// Loads the "Marriage Budget" sheet data into TMS via its REST API, authenticating
// with a browser session cookie you provide. Single-file, no dependencies.
// Java 11+ compatible (uses java.net.http; no records / no switch-expressions).
//
// Usage:
//   1. Log into the TMS app in your browser, open DevTools > Network, copy the
//      Cookie request header for an /api/* call (e.g. "JSESSIONID=ABC123...").
//   2. Run:
//        java budget-migration/BudgetMigration.java \
//             --base http://localhost:8080/tms \
//             --cookie "JSESSIONID=PASTE_HERE"
//      (or set env TMS_BASE / TMS_COOKIE instead of the flags)
//   3. Re-running is safe: it skips expenses/income if any already exist.
//      Pass --force to load anyway.
//
// Notes:
//   - --base must point at the deployed context root (no trailing /api). For a war
//     named tms.war on Tomcat that is http://localhost:8080/tms
//   - Functions are matched by name (case-insensitive); missing ones are created.
//   - Amounts are whole rupees. "actual" is omitted (null) where the sheet was blank.

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class BudgetMigration {

    // ── Embedded sheet data ─────────────────────────────────────────────────
    static final class Fn {
        final String name, color, date;
        Fn(String name, String color, String date) { this.name = name; this.color = color; this.date = date; }
    }
    static final class Exp {
        final String fn, name, notes;
        final int est, paid;
        final Integer actual;
        Exp(String fn, String name, int est, Integer actual, int paid, String notes) {
            this.fn = fn; this.name = name; this.est = est; this.actual = actual; this.paid = paid; this.notes = notes;
        }
    }
    static final class Inc {
        final String source, date;
        final int est;
        final Integer actual;
        Inc(String source, String date, int est, Integer actual) {
            this.source = source; this.date = date; this.est = est; this.actual = actual;
        }
    }

    static final Fn[] FUNCTIONS = {
        new Fn("Engagement",   "#f59e0b", "2026-06-18"),
        new Fn("Ponnu Urukku", "#0d9488", null),
        new Fn("Marriage",     "#4f46e5", "2026-09-13"),
    };

    static final Exp[] EXPENSES = {
        // Engagement
        new Exp("Engagement", "Ponnu Pattu Saree", 10000, 5900, 0, "Saree(5900)+"),
        new Exp("Engagement", "Van",                5000, null, 0, ""),
        new Exp("Engagement", "Poo",                5000, 5000, 500, ""),
        new Exp("Engagement", "Mappilai dress",     2000, 4300, 0, "Mapilai Kurta(4300)+"),
        new Exp("Engagement", "Fruits",             5000, null, 0, ""),
        new Exp("Engagement", "Makeup Ram",         5000, null, 0, ""),
        new Exp("Engagement", "Makup Saras",        5000, null, 0, ""),
        new Exp("Engagement", "Saras dress",        4000, 2500, 0, ""),
        new Exp("Engagement", "Appa dress",         1500, 0, 0, ""),
        new Exp("Engagement", "Amma dress",         2000, 0, 0, ""),

        // Ponnu Urukku
        new Exp("Ponnu Urukku", "Pown",               60000, null, 0, ""),
        new Exp("Ponnu Urukku", "Sapadu (Afternoon)",  8000, null, 0, ""),
        new Exp("Ponnu Urukku", "Sapadu (Morning)",    4000, null, 0, ""),
        new Exp("Ponnu Urukku", "Poo malai",           2000, null, 0, ""),
        new Exp("Ponnu Urukku", "House Painting",     35000, null, 0, ""),

        // Marriage
        new Exp("Marriage", "Koil Kattanam & Procedures", 7000, null, 0, ""),
        new Exp("Marriage", "Marriage Hall",        30000, 30000, 5000, "M.S Mahal, cell: 9442532189"),
        new Exp("Marriage", "Decoration",            7000, null, 0, ""),
        new Exp("Marriage", "Sound Service & Light Set", 10000, null, 0, ""),
        new Exp("Marriage", "Flower",               30000, null, 0, ""),
        new Exp("Marriage", "Food (afternoon)",     70000, null, 1000, "500 x 140, cell: 8870608409"),
        new Exp("Marriage", "Food (morning)",       15000, null, 0, "200 x 75"),
        new Exp("Marriage", "Food Service & Cooks", 35000, null, 0, ""),
        new Exp("Marriage", "Tent",                 20000, null, 0, ""),
        new Exp("Marriage", "Iyer & Things",        10000, null, 0, ""),
        new Exp("Marriage", "Manjal invitation",     1800, 1800, 1000, ""),
        new Exp("Marriage", "Designed Invitation",  10000, null, 0, "20*500"),
        new Exp("Marriage", "In Hand Expense",      50000, null, 0, ""),
        new Exp("Marriage", "Melam",                12000, 6000, 500, "cell: 9487121634"),
        new Exp("Marriage", "Video & Photos",       40000, 35000, 5000, "cell: 6379353104"),
        new Exp("Marriage", "Makeup Ram",            5000, null, 0, ""),
        new Exp("Marriage", "Makeup Saras",          5000, null, 0, ""),
        new Exp("Marriage", "Mapillai Pattu Westy", 10000, null, 0, ""),
        new Exp("Marriage", "Ponu Pattu Saree",     20000, null, 0, ""),
        new Exp("Marriage", "Manchal Mapilai Dress", 2000, null, 0, ""),
        new Exp("Marriage", "Manchal Ponnu Saree",   2000, null, 0, ""),
        new Exp("Marriage", "Mapillai Thangachi Dress", 10000, null, 0, ""),
        new Exp("Marriage", "Mapillai Amma Dress",  10000, null, 0, ""),
        new Exp("Marriage", "Mapillai Appa Dress",   2000, null, 0, ""),
        new Exp("Marriage", "Ponnu Amma Dress",      2000, null, 0, ""),
        new Exp("Marriage", "Ponnu Appa Dress",      1000, null, 0, ""),
        new Exp("Marriage", "Mapillai Mama Dress (mother)", 1000, null, 0, ""),
        new Exp("Marriage", "Mapillai Athai Dress (mother)", 2000, null, 0, ""),
        new Exp("Marriage", "Mapillai Thatha Dress (mother)", 1000, null, 0, ""),
        new Exp("Marriage", "Mapillai Patti Dress (mother)", 1000, null, 0, ""),
        new Exp("Marriage", "Mappilai Patti (china) Dress (mother)", 500, null, 0, ""),
        new Exp("Marriage", "Mappillai Machan (Ponnu) Dress", 1000, null, 0, ""),
        new Exp("Marriage", "Ponnu Thangachi Dress", 2000, null, 0, ""),
    };

    static final Inc[] INCOME = {
        new Inc("Bonus",   null,         300000, 200000),
        new Inc("Savings", "2026-04-19",  50000, 50000),
        new Inc("Savings", "2026-04-30",  50000, 50000),
        new Inc("Savings", "2026-05-31",  50000, 50000),
        new Inc("Savings", "2026-06-30",  50000, null),
        new Inc("Savings", "2026-07-31",  50000, null),
        new Inc("Savings", "2026-08-31",  50000, null),
    };

    // ── config / HTTP ───────────────────────────────────────────────────────
    static String base;
    static String cookie;
    static boolean force;
    static HttpClient HTTP;

    public static void main(String[] args) throws Exception {
        base   = arg(args, "--base", System.getenv("TMS_BASE"));
        cookie = arg(args, "--cookie", System.getenv("TMS_COOKIE"));
        force  = has(args, "--force");
        HTTP   = insecureClient();
        if (base == null || base.trim().isEmpty()) { base = "http://localhost:8080/tms"; }
        base = base.replaceAll("/+$", "");
        if (cookie == null || cookie.trim().isEmpty()) {
            System.err.println("ERROR: session cookie required. Pass --cookie \"JSESSIONID=...\" or set TMS_COOKIE.");
            System.exit(2);
        }
        System.out.println("Target: " + base + "   (force=" + force + ")");

        Map<String, Integer> fnIds = resolveFunctions(parseMappings(args));
        migrateExpenses(fnIds);
        migrateIncome();
        System.out.println("\nDone.");
    }

    // ── Functions: map each sheet section to an EXISTING function (never create) ──
    // Match priority: explicit --map "<SheetName>=<id>", then exact existing name (case-insensitive).
    static Map<String, Integer> resolveFunctions(Map<String, Integer> mappings) throws Exception {
        String json = get("/api/functions");
        Map<String, Integer> byName = new LinkedHashMap<String, Integer>();
        System.out.println("\nExisting functions in the app:");
        Matcher obj = Pattern.compile("\\{[^{}]*\\}").matcher(json);
        while (obj.find()) {
            String o = obj.group();
            Matcher idm = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(o);
            Matcher nm = Pattern.compile("\"name\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(o);
            if (idm.find() && nm.find()) {
                int id = Integer.parseInt(idm.group(1));
                String name = unescape(nm.group(1));
                byName.put(name.toLowerCase(), id);
                System.out.println("  [" + id + "] " + name);
            }
        }

        Map<String, Integer> result = new LinkedHashMap<String, Integer>();
        List<String> unresolved = new ArrayList<String>();
        for (Fn f : FUNCTIONS) {
            Integer id = mappings.get(f.name.toLowerCase());
            if (id == null) { id = byName.get(f.name.toLowerCase()); }
            if (id != null) {
                result.put(f.name.toLowerCase(), id);
                System.out.println("  -> sheet '" + f.name + "' -> function id " + id);
            } else {
                unresolved.add(f.name);
            }
        }

        if (!unresolved.isEmpty()) {
            System.out.println("\nERROR: could not match these sheet sections to existing functions: " + unresolved);
            System.out.println("Re-run mapping each to an existing function id shown above, e.g.:");
            System.out.println("  --map \"Engagement=<id>\" --map \"Ponnu Urukku=<id>\" --map \"Marriage=<id>\"");
            System.exit(3);
        }
        return result;
    }

    // Collects all "--map Name=Id" pairs into name(lower) -> id.
    static Map<String, Integer> parseMappings(String[] a) {
        Map<String, Integer> m = new LinkedHashMap<String, Integer>();
        for (int i = 0; i < a.length - 1; i++) {
            if (a[i].equals("--map")) {
                String v = a[i + 1];
                int eq = v.lastIndexOf('=');
                if (eq > 0) {
                    String name = v.substring(0, eq).trim().toLowerCase();
                    try { m.put(name, Integer.parseInt(v.substring(eq + 1).trim())); }
                    catch (NumberFormatException ignore) { }
                }
            }
        }
        return m;
    }

    // ── Expenses ─────────────────────────────────────────────────────────
    static void migrateExpenses(Map<String, Integer> fnIds) throws Exception {
        String existing = get("/api/budget");
        if (containsItems(existing) && !force) {
            System.out.println("\n! Budget already has data — skipping expenses (use --force to load anyway).");
            return;
        }
        System.out.println("\nLoading " + EXPENSES.length + " expenses...");
        Map<String, Integer> order = new LinkedHashMap<String, Integer>();
        int ok = 0, fail = 0;
        for (Exp e : EXPENSES) {
            Integer fnId = fnIds.get(e.fn.toLowerCase());
            if (fnId == null) {
                System.out.println("  ! no function for '" + e.fn + "', skipping " + e.name);
                fail++;
                continue;
            }
            int do_ = order.merge(e.fn.toLowerCase(), 0, new java.util.function.BiFunction<Integer, Integer, Integer>() {
                public Integer apply(Integer a, Integer b) { return a + 1; }
            });
            StringBuilder b = new StringBuilder("{");
            b.append(num("functionId", fnId)).append(",")
             .append(str("name", e.name)).append(",")
             .append(num("estimatedAmount", e.est)).append(",")
             .append(nullableNum("actualAmount", e.actual)).append(",")
             .append(num("paidAmount", e.paid)).append(",")
             .append(str("notes", e.notes)).append(",")
             .append(num("displayOrder", do_)).append(",")
             .append("\"splits\":[]}");
            if (post("/api/budget", b.toString(), e.fn + " / " + e.name)) { ok++; } else { fail++; }
        }
        System.out.println("Expenses: " + ok + " ok, " + fail + " failed.");
    }

    // ── Income ─────────────────────────────────────────────────────────
    static void migrateIncome() throws Exception {
        String existing = get("/api/income");
        if (containsItems(existing) && !force) {
            System.out.println("\n! Income already has data — skipping income (use --force to load anyway).");
            return;
        }
        System.out.println("\nLoading " + INCOME.length + " income rows...");
        int ok = 0, fail = 0, i = 0;
        for (Inc in : INCOME) {
            StringBuilder b = new StringBuilder("{");
            b.append(str("source", in.source)).append(",")
             .append(nullableStr("incomeDate", in.date)).append(",")
             .append(num("estimatedAmount", in.est)).append(",")
             .append(nullableNum("actualAmount", in.actual)).append(",")
             .append(str("notes", "")).append(",")
             .append(num("displayOrder", i++)).append("}");
            String label = "income '" + in.source + (in.date != null ? " " + in.date : "") + "'";
            if (post("/api/income", b.toString(), label)) { ok++; } else { fail++; }
        }
        System.out.println("Income: " + ok + " ok, " + fail + " failed.");
    }

    /** HttpClient that trusts all TLS certs and skips hostname verification (local migration tool). */
    static HttpClient insecureClient() throws Exception {
        // Must be set before the client is created — disables HttpClient hostname checks.
        System.setProperty("jdk.internal.httpclient.disableHostnameVerification", "true");
        TrustManager[] trustAll = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                public void checkServerTrusted(X509Certificate[] chain, String authType) { }
            }
        };
        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(null, trustAll, new SecureRandom());
        return HttpClient.newBuilder().sslContext(ssl).build();
    }

    // ── HTTP helpers ─────────────────────────────────────────────────────
    static String get(String path) throws Exception {
        HttpRequest req = base(path).GET().build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new RuntimeException("GET " + path + " -> HTTP " + resp.statusCode() + " : " + brief(resp.body()));
        }
        return resp.body() == null ? "" : resp.body();
    }

    static boolean post(String path, String json, String label) throws Exception {
        HttpRequest req = base(path)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
            .build();
        HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        boolean ok = resp.statusCode() / 100 == 2 && !resp.body().contains("\"status\":\"FAIL\"");
        System.out.println("  " + (ok ? "✓" : "✗") + " " + label
            + (ok ? "" : "  [HTTP " + resp.statusCode() + " " + brief(resp.body()) + "]"));
        return ok;
    }

    static HttpRequest.Builder base(String path) {
        return HttpRequest.newBuilder(URI.create(base + path))
            .header("Cookie", cookie)
            .header("Accept", "application/json");
    }

    static boolean containsItems(String json) {
        return json != null && json.contains("\"id\"");
    }

    // ── tiny JSON builders ───────────────────────────────────────────────
    static String str(String k, String v) { return "\"" + k + "\":\"" + escape(v == null ? "" : v) + "\""; }
    static String num(String k, long v) { return "\"" + k + "\":" + v; }
    static String nullableNum(String k, Integer v) { return "\"" + k + "\":" + (v == null ? "null" : v); }
    static String nullableStr(String k, String v) { return "\"" + k + "\":" + (v == null ? "null" : "\"" + escape(v) + "\""); }

    static String escape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    static String unescape(String s) { return s.replace("\\\"", "\"").replace("\\\\", "\\"); }

    static String brief(String s) {
        if (s == null) { return ""; }
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() > 160 ? s.substring(0, 160) + "…" : s;
    }

    static String arg(String[] a, String flag, String dflt) {
        for (int i = 0; i < a.length - 1; i++) { if (a[i].equals(flag)) { return a[i + 1]; } }
        return dflt;
    }
    static boolean has(String[] a, String flag) {
        for (String s : a) { if (s.equals(flag)) { return true; } }
        return false;
    }
}
