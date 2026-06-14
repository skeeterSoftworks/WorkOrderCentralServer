# JasperReports **v7**: Serbian / Unicode font fix

> **Version note:** This guide is for **JasperReports 7.x** (modular artifacts, Jackson XML parsers).  
> For **JasperReports 6.x** (single `jasperreports` JAR as used in WorkOrderCentral today), see [JasperFonts.MD](./JasperFonts.MD).

JasperReports PDF export still defaults to **Helvetica** with legacy encodings unless you register a Unicode-capable font family. Serbian letters such as **č**, **ć**, **ž**, **š**, **đ** (and **Č**, **Ć**, **Ž**, **Š**, **Đ**) then appear as boxes, `#`, or missing glyphs.

In **v7**, font extensions work the same conceptually as in v6, but dependency layout, extension property keys, and strict font validation differ. Follow the steps below.

---

## What changed in JasperReports 7 (relevant to fonts & PDF)

| Topic | v6 | v7 |
|--------|----|----|
| Library layout | Mostly one `jasperreports` JAR | **Core + optional modules** (`jasperreports-pdf`, `jasperreports-jdt`, …) |
| PDF export | Included in core | **`jasperreports-pdf`** module |
| JRXML compile at runtime | ECJ often bundled | **`jasperreports-jdt`** module (recommended) |
| Font pack | `jasperreports-fonts` | Same artifact name, **match version to core** |
| Extension registry key | Older projects sometimes used `…simple.fonts…` | Official key: **`…simple.font.families…`** |
| Missing font at runtime | Often silent fallback to Helvetica | **`JRFontNotFoundException`** by default |
| `.jrxml` from v6 | Loaded directly | May require **conversion in Jaspersoft Studio 7** (Jackson XML) |
| Compiled `.jasper` | Compatible across minor versions | **Not compatible** v6 → v7; recompile templates |

Always keep **the same version number** on every `net.sf.jasperreports:*` artifact (e.g. all `7.0.7`).

---

## Quick fix (recommended): `jasperreports-fonts`

Use the official **DejaVu Sans** extension. It covers Latin Extended (including Serbian) and embeds correctly in PDF with `Identity-H`.

### Step 1 — Add Maven dependencies (v7 minimal PDF stack)

Example for generating PDF from `.jrxml` at runtime (versions shown: **7.0.7** — adjust to your latest 7.x patch):

```xml
<properties>
    <jasperreports.version>7.0.7</jasperreports.version>
</properties>

<dependencies>
    <!-- Core library (javaflow variant = thread-safe fill; use jasperreports artifact if you prefer non-javaflow) -->
    <dependency>
        <groupId>net.sf.jasperreports</groupId>
        <artifactId>jasperreports-javaflow</artifactId>
        <version>${jasperreports.version}</version>
    </dependency>

    <!-- PDF export (required in v7 — not part of core alone) -->
    <dependency>
        <groupId>net.sf.jasperreports</groupId>
        <artifactId>jasperreports-pdf</artifactId>
        <version>${jasperreports.version}</version>
    </dependency>

    <!-- JRXML compilation at runtime (recommended; brings ECJ transitively) -->
    <dependency>
        <groupId>net.sf.jasperreports</groupId>
        <artifactId>jasperreports-jdt</artifactId>
        <version>${jasperreports.version}</version>
    </dependency>

    <!-- DejaVu font families -->
    <dependency>
        <groupId>net.sf.jasperreports</groupId>
        <artifactId>jasperreports-fonts</artifactId>
        <version>${jasperreports.version}</version>
    </dependency>
</dependencies>
```

Gradle Kotlin DSL:

```kotlin
val jasper = "7.0.7"
implementation("net.sf.jasperreports:jasperreports-javaflow:$jasper")
implementation("net.sf.jasperreports:jasperreports-pdf:$jasper")
implementation("net.sf.jasperreports:jasperreports-jdt:$jasper")
implementation("net.sf.jasperreports:jasperreports-fonts:$jasper")
```

**Do not** mix v6 and v7 JARs on the classpath.

Optional: compile all `.jrxml` at **build time** with the official plugin (avoids shipping `jasperreports-jdt` to production):

```xml
<plugin>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports-maven-plugin</artifactId>
    <version>7.0.7</version>
    <executions>
        <execution>
            <phase>process-resources</phase>
            <goals>
                <goal>compile</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <sourceDirectory>${project.basedir}/src/main/resources/reports</sourceDirectory>
        <outputDirectory>${project.build.outputDirectory}/reports</outputDirectory>
    </configuration>
</plugin>
```

Deploy compiled `*.jasper` and load them with `JRLoader.loadObject(...)` instead of compiling on every request.

### Step 2 — Confirm fonts JAR is on the runtime classpath

`jasperreports-fonts` registers **DejaVu Sans** / **DejaVu Serif** via Jasper’s extension mechanism. No extra Java bootstrap code is required if the JAR is present.

Internally (reference only — already inside the fonts JAR), registration looks like:

```properties
net.sf.jasperreports.extension.registry.factory.simple.font.families=net.sf.jasperreports.engine.fonts.SimpleFontExtensionsRegistryFactory
net.sf.jasperreports.extension.simple.font.families.dejavu=net/sf/jasperreports/fonts/fonts.xml
```

Custom font extensions in **your** project must use the same **`simple.font.families`** keys (see [Custom font](#alternative-custom-font-branding--non-dejavu) below).

### Step 3 — Define report styles (`.jrxml`)

Inside `<jasperReport>`, add default styles:

```xml
<style name="Base" isDefault="true" fontName="DejaVu Sans" fontSize="10"
       pdfFontName="DejaVu Sans" pdfEncoding="Identity-H" isPdfEmbedded="true"/>
<style name="BaseBold" fontName="DejaVu Sans" fontSize="10" isBold="true"
       pdfFontName="DejaVu Sans" pdfEncoding="Identity-H" isPdfEmbedded="true"/>
<style name="Title" fontName="DejaVu Sans" fontSize="18" isBold="true"
       pdfFontName="DejaVu Sans" pdfEncoding="Identity-H" isPdfEmbedded="true"/>
```

| Attribute | Purpose |
|-----------|---------|
| `fontName` | Logical family during report fill |
| `pdfFontName` | Family used in PDF output |
| `pdfEncoding="Identity-H"` | Unicode (required for č, ž, …) |
| `isPdfEmbedded="true"` | Embed glyphs in the PDF |

Apply on elements:

```xml
<reportElement x="0" y="0" width="140" height="18" style="BaseBold"/>
```

Unstyled elements inherit `Base` when `isDefault="true"`.

Avoid bare `<font isBold="true"/>` without PDF Unicode settings — that keeps Helvetica/Cp1252 behaviour.

**v7 template migration:** If Studio 7 or runtime fails to load a v6-era `.jrxml`, open the template in **Jaspersoft Studio 7+** and save/convert it. Simple hand-written templates often work unchanged, but complex legacy files may not.

### Step 4 — UTF-8 labels

- Save `.jrxml` and `.properties` as **UTF-8**.
- Prefer literal Serbian text in bundles:

```properties
purchaseOrder=Porudžbenica
quantity=Količina
```

### Step 5 — Rebuild, redeploy, clear caches

- Recompile `.jrxml` → `.jasper` with **v7** tooling.
- Restart the application if compiled `JasperReport` objects are cached in memory.
- Fat JAR / Spring Boot: confirm `jasperreports-fonts` and `jasperreports-pdf` are not stripped by packaging rules.

### Step 6 — Verify

Generate a PDF containing:

```
Čč Ćć Žž Šš Đđ
Porudžbenica — Količina
```

Open in Adobe Reader, Chrome PDF viewer, and Edge — all should show correct glyphs.

---

## v7 Java API (compile → fill → export PDF)

Same entry points as v6, but classes come from the modular JAR set:

```java
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

// Load & compile (or load precompiled .jasper)
try (InputStream in = getClass().getResourceAsStream("/reports/my-report.jrxml")) {
    JasperReport report = JasperCompileManager.compileReport(in);

    Map<String, Object> params = new HashMap<>();
    params.put("reportTitle", "Nalog za dodelu robe");

    JasperPrint print = JasperFillManager.fillReport(
            report, params, new JRBeanCollectionDataSource(rows));

    byte[] pdf = JasperExportManager.exportReportToPdf(print);
    // ...
}
```

If you get compile errors mentioning missing `net.sf.jasperreports.engine` packages inside generated temp Java files, add **`jasperreports-jdt`**.

If PDF export fails with missing exporter classes, add **`jasperreports-pdf`**.

---

## Alternative: custom font (branding / non-DejaVu)

Use when you need **Noto Sans**, a corporate TTF, etc.

### A. Project layout

```
src/main/resources/
    jasperreports_extension.properties
    reports/fonts/
        NotoSans-Regular.ttf
        NotoSans-Bold.ttf
        noto-sans-family.xml
```

### B. Font family descriptor (`noto-sans-family.xml`)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<fontFamilies>
    <fontFamily name="Noto Sans">
        <normal>reports/fonts/NotoSans-Regular.ttf</normal>
        <bold>reports/fonts/NotoSans-Bold.ttf</bold>
        <pdfEncoding>Identity-H</pdfEncoding>
        <pdfEmbedded>true</pdfEmbedded>
        <exportFonts>
            <export key="net.sf.jasperreports.html">'Noto Sans', Arial, Helvetica, sans-serif</export>
        </exportFonts>
    </fontFamily>
</fontFamilies>
```

Paths are **classpath-relative** (from `src/main/resources`).

Optional `<locales>` blocks allow different TTF sets per language while reusing the same `fontName` in reports (see [official font extension docs](https://jasperreports.sourceforge.net/api/net/sf/jasperreports/engine/fonts/package-summary.html)).

### C. Register the extension (`jasperreports_extension.properties`)

**v7 property keys** (note `font.families`, not legacy `fonts`):

```properties
net.sf.jasperreports.extension.registry.factory.simple.font.families=net.sf.jasperreports.engine.fonts.SimpleFontExtensionsRegistryFactory
net.sf.jasperreports.extension.simple.font.families.noto=reports/fonts/noto-sans-family.xml
```

If you also depend on `jasperreports-fonts`, both DejaVu (from the JAR) and your custom family coexist. Use distinct extension IDs (`dejavu` vs `noto`).

**Jaspersoft Studio 7** may also expect:

```properties
net.sf.jasperreports.extension.registry.factory.fonts=net.sf.jasperreports.engine.fonts.SimpleFontExtensionsRegistryFactory
```

When in doubt, register **both** factory lines pointing to the same factory class (harmless duplicate registration).

### D. Use the family in `.jrxml`

```xml
fontName="Noto Sans" pdfFontName="Noto Sans" pdfEncoding="Identity-H" isPdfEmbedded="true"
```

### E. Package as a reusable JAR (optional)

For multiple projects, put TTF + XML + `jasperreports_extension.properties` in a small **`my-company-fonts.jar`** and add it as a Maven dependency — same pattern as `jasperreports-fonts`.

### F. License

Confirm the font license allows **PDF embedding**.

---

## Strict font validation in v7

v7 throws **`JRFontNotFoundException`** when a report references a family that is not installed and not provided by an extension.

**Fix (preferred):** register the font via `jasperreports-fonts` or a custom extension.

**Escape hatch (not recommended for production i18n):**

```properties
# src/main/resources/jasperreports.properties — global
net.sf.jasperreports.awt.ignore.missing.font=true
```

Or per report parameter / property. Silencing the error restores Helvetica fallback and broken Serbian glyphs.

---

## Jaspersoft Studio 7

1. Use **Studio 7+** for templates intended for JR 7 runtime.
2. Add `jasperreports-fonts` (or your font JAR) to the project classpath / Studio preferences.
3. Set styles to **DejaVu Sans** (or your registered family).
4. If the font dropdown does not list the family, type the exact `fontName` and press Enter.
5. Reopen reports after adding a new font extension JAR (Studio caches font lists).
6. For v6 templates: open once in Studio 7 to migrate `.jrxml` / `.jrtx` format if load errors occur.

---

## Migrating from JasperReports 6 → 7 (font checklist)

1. Replace single `jasperreports` dependency with v7 modules (`javaflow` or core + `pdf` + `jdt` + `fonts`).
2. Align **all** `net.sf.jasperreports` artifacts to the same version (e.g. `7.0.7`).
3. Recompile every `.jrxml` / `.jrtx` with Studio 7 or `jasperreports-maven-plugin`.
4. Update custom `jasperreports_extension.properties` keys to **`simple.font.families`** if you used legacy names.
5. Add DejaVu styles (`Identity-H`, embedded) to every report — do not assume v6 PDF font settings carried over.
6. Run PDF visual test with **Čč Žž Šš Đđ**.
7. Remove any reliance on precompiled v6 `.jasper` binaries.

---

## Troubleshooting (v7-specific)

| Symptom | Likely cause | Fix |
|---------|----------------|-----|
| `JRFontNotFoundException: DejaVu Sans` | `jasperreports-fonts` missing | Add fonts JAR; same version as core |
| PDF export class not found | Missing `jasperreports-pdf` | Add PDF module |
| JRXML compile fails in Tomcat | Missing `jasperreports-jdt` | Add JDT module or precompile at build |
| `□` / `#` for č, ž | Helvetica fallback | DejaVu + `Identity-H` + embed in styles |
| Works in Studio, fails in server | Font JAR not in deployed WAR/fat JAR | Check `BOOT-INF/lib` / `WEB-INF/lib` |
| Load `.jrxml` IOException / parse errors | v6 template format | Convert in Jaspersoft Studio 7 |
| Bold renders as regular | No `<bold>` face in font family XML | Add Bold TTF mapping |
| Custom extension ignored | Wrong property key | Use `…simple.font.families…` (v7) |
| Mixed JR versions on classpath | Transitive dependency clash | `mvn dependency:tree`; enforce one version |

Inspect dependencies:

```bash
mvn dependency:tree -Dincludes=net.sf.jasperreports
```

Copy all required JARs to a folder (even for non-Maven builds):

```bash
mvn dependency:copy-dependencies -DincludeGroupIds=net.sf.jasperreports
```

---

## Manual PDF smoke test (template)

Adapt this pattern in your project (disabled by default):

```java
@Test
@Disabled("Manual visual check — writes target/stock-assignment-order-sample.pdf")
void dumpSamplePdf() throws Exception {
    // Build in-memory domain rows + params with "Porudžbenica", "Marko Živković", …
    // Call your report service → decode Base64 or bytes → Files.write(Path.of("target/sample.pdf"), pdf);
}
```

Verify: `target/sample.pdf` shows correct Serbian diacritics in labels and data fields.

---

## Checklist for a new v7 Jasper report

- [ ] `jasperreports-javaflow` (or core) **+** `jasperreports-pdf` **+** `jasperreports-fonts` at the **same version**
- [ ] `jasperreports-jdt` on classpath **or** build-time compile via `jasperreports-maven-plugin`
- [ ] Default `<style>`: `DejaVu Sans`, `pdfEncoding="Identity-H"`, `isPdfEmbedded="true"`
- [ ] No orphan `<font>` tags without Unicode PDF settings
- [ ] UTF-8 `.jrxml` and label bundles
- [ ] Custom extensions use `…simple.font.families…` registry keys
- [ ] PDF manually checked for **Čč Ćć Žž Šš Đđ**
- [ ] No v6 `.jasper` binaries deployed

---

## Further reading

- [Font extensions API (v7 Javadoc)](https://jasperreports.sourceforge.net/api/net/sf/jasperreports/engine/fonts/package-summary.html)
- [JasperReports 7.0.0 release notes / modular split](https://github.com/Jaspersoft/jasperreports/releases/tag/7.0.0)
- [JasperReports Maven Plugin](https://jasperreports.sourceforge.net/sample.reference/maven/README.html)
- [WorkOrderCentral v6 guide](./JasperFonts.MD) — current production setup in this repo
