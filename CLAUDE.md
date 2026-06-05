# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A desktop-style bank statement analyzer (Spanish/Dominican Republic banks: Banco Popular, BHD León, Scotiabank). It ingests a statement file (CSV / Excel / PDF), extracts deductions — taxes, commissions, interest, late-payment fees — and returns a categorized `Summary` with totals. Built on **Quarkus 3.6.1**, targeting **Java 21** (`maven.compiler.release=21` in `pom.xml`; README's "Java 17+" is outdated).

On startup `WebBrowserBean` auto-opens the system browser at `http://localhost:8082/index.html`, so the app behaves like a local desktop tool rather than a hosted service. The HTTP port `8082` is set in `application.properties`.

## Commands

```bash
./mvnw compile quarkus:dev      # Dev mode with live reload; Dev UI at http://localhost:8082/q/dev/
./mvnw package                  # Build quarkus-app/ (run: java -jar target/quarkus-app/quarkus-run.jar)
./mvnw package -Dquarkus.package.type=uber-jar
./mvnw test                     # Run unit tests (Surefire)
./mvnw test -Dtest=GreetingResourceTest          # Single test class
./mvnw test -Dtest=GreetingResourceTest#methodName   # Single test method
./mvnw verify                   # Includes integration tests (*IT); skipITs=true by default
./mvnw package -Pnative         # GraalVM native build (profile in pom.xml)
```

The H2 database is a file DB at `./data/aa` (JDBC URL `jdbc:h2:./data/aa`, user `sa`, no password), schema auto-`update`d by Hibernate. To inspect it via the H2 console use that same JDBC URL.

## Architecture

### Request flow (the analyze pipeline)
`POST /account/analyze` (`web/AccountResource.java`) is the core endpoint. It:
1. Reads multipart fields `bank` and `pass` (PDF password) plus the uploaded `file`.
2. **Selects the parser by name via CDI**, not a switch/factory: it appends `"AccountService"` to the `bank` field, then matches that against the simple class name of every `AccountService` CDI bean — accounting for Quarkus's `_ClientProxy` suffix (`SUFFIX` constant). So the frontend's `bank` value must equal a concrete service's class name minus `AccountService` (e.g. `bank=PopularPDF` → `PopularPDFAccountService`). **Renaming a bank service class changes its public API contract with the frontend.**
3. Saves the upload (`FileUploadService` → `config.upload-dir`, default `files/`), parses transactions, runs one `getTransactionSummary` per `DeductionType`, deletes the temp file, and returns `Summary`.

### Parser strategy hierarchy
`AccountService` (interface) → `BaseBankService` (abstract, shared summary logic) → concrete per **bank + file format**:

| Class | Bank | Format | Reader lib |
|---|---|---|---|
| `PopularAccountService` | Popular | CSV | OpenCSV |
| `PopularPDFAccountService` | Popular | PDF | PDFBox |
| `BHDAccountService` | BHD León | Excel | Apache POI |
| `BHDPDFAccountService` | BHD León | PDF | PDFBox |
| `ScotiabankAccountService` | Scotiabank | PDF | PDFBox |

PDF subclasses extend the bank's base service and override `readFile`. All live in `service/bank/`.

### How deductions are categorized
`DeductionType` (enum: `COMMISSIONS`, `TAXES`, `INTEREST`, `NON_PAYMENT_FEE`, `USER_INTEREST`, `USER_INTEREST_OUT_TOTAL`) drives everything. The `Deduction` JPA entity stores `(type, description)` rows in H2 (managed via `DeductionResource` / `DeductionService` / `DeductionRepository`, a Panache repository). `BaseBankService.buildTransactionSummary` loads the descriptions for a given `DeductionType`, then keeps transactions whose `desc` **contains** any of those strings (`Transaction.descContains`, case-insensitive substring). So categorization rules are **data**, editable at runtime through `/deduction`, not hardcoded. `USER_INTEREST_OUT_TOTAL` is the special case: it's reported but excluded from `Summary`'s grand total, and its descriptions are rendered with the amount appended.

### Domain types
`Transaction` and `TransactionSummary` and `Summary` are immutable `record`s. `Transaction` carries `(date, type, amount, referenceNumber, serial, desc)`; `amount` is a `Float`.

### Frontend
Static assets in `src/main/resources/META-INF/resources/` served at the root. `index.html`/`index.js` (analyze) and `deduction.html`/`deduction.js` (manage deduction rules). Stack is **jQuery + DevExtreme** (`dxFileUploader`, `dxButton`) — `index.css` is the bundled DevExtreme theme (~236 KB; do not hand-edit). The form posts multipart to `/account/analyze`; `addTransactionsDetail` renders each category's transaction list and total. `bClose` hits `GET /account/close`, which calls `System.exit(0)` to shut the app down.

## Conventions & gotchas

- **PDF parsers are positional and fragile.** They split extracted text into lines and iterate over **hardcoded line indices/offsets** (e.g. Scotiabank starts at row `94`, Popular at `9`) tied to each bank's exact statement layout. A statement template change breaks parsing silently. When touching a parser, work from a real sample PDF and verify against it.
- **Money is `Float`** throughout — not `BigDecimal`. Be aware of precision when summing.
- **Logging is `System.out.println`** in most services (despite a `log4j-core` dependency) and `e.printStackTrace()` for errors. `AccountResource.analyze` swallows exceptions and returns `null` on failure (frontend sees an error callback). Match the surrounding style unless explicitly refactoring.
- **Package name `com.guille.reposiitory` is misspelled** but real — import it as-is.
- Tests are effectively empty (`GreetingResourceTest` is commented out); there is no parser test coverage yet.
