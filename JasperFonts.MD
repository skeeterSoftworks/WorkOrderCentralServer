# JasperReports: Serbian / Unicode font fix

> **Version note:** This guide targets **JasperReports 6.x** (as used in WorkOrderCentral: `6.21.3`).  
> For **JasperReports 7.x** (modular JARs, updated extension keys, stricter font checks), see [JasperFonts-v7.MD](./JasperFonts-v7.MD).

JasperReports PDF export defaults to **Helvetica** with **Cp1252** (Windows Latin-1). That encoding does not include Serbian letters such as **č**, **ć**, **ž**, **š**, **đ** (or uppercase **Č**, **Ć**, **Ž**, **Š**, **Đ**). Missing glyphs show as empty boxes, `#`, or wrong characters.

This guide documents the fix used in **WorkOrderCentral** and how to apply the same pattern in other Java/Jasper projects.

---

## Quick fix (recommended): `jasperreports-fonts`

Use the official **DejaVu Sans** font pack shipped with JasperReports. DejaVu covers Latin Extended and embeds correctly in PDF.

### Step 1 — Add the Maven dependency

Use the **same version** as your `jasperreports` artifact (here: `6.21.3`):

```xml
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports</artifactId>
    <version>6.21.3</version>
</dependency>
<dependency>
    <groupId>net.sf.jasperreports</groupId>
    <artifactId>jasperreports-fonts</artifactId>
    <version>6.21.3</version>
</dependency>
```

Gradle equivalent:

```gradle
implementation 'net.sf.jasperreports:jasperreports:6.21.3'
implementation 'net.sf.jasperreports:jasperreports-fonts:6.21.3'
```

No extra Java code is required. The fonts JAR registers DejaVu families via Jasper’s extension mechanism when it is on the classpath.

### Step 2 — Define report styles (`.jrxml`)

At the top of each report (inside `<jasperReport>`), add default styles that point to DejaVu and use Unicode PDF settings:

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
| `fontName` | AWT / layout font during report fill |
| `pdfFontName` | Font used in the generated PDF |
| `pdfEncoding="Identity-H"` | Unicode (required for č, ž, …) |
| `isPdfEmbedded="true"` | Embeds glyphs so PDF viewers do not depend on local fonts |

Apply styles on elements, e.g.:

```xml
<reportElement x="0" y="0" width="140" height="18" style="BaseBold"/>
```

Elements without an explicit style inherit `Base` when `isDefault="true"`.

**Do not** rely on bare `<font isBold="true"/>` without `pdfFontName` / `pdfEncoding` — that keeps the broken Helvetica defaults for PDF.

### Step 3 — Store labels as UTF-8

- Save `.jrxml` and `.properties` as **UTF-8**.
- In Java `ResourceBundle` files you may use `\u010d` escapes or literal UTF-8 (e.g. `Količina`, `Porudžbenica`).

Example Serbian bundle (`stock-assignment-order_sr.properties`):

```properties
purchaseOrder=Porudžbenica
quantity=Količina
```

### Step 4 — Rebuild and restart

If your app caches compiled `.jasper` / `JasperReport` objects in memory, **restart the server** (or clear the cache) after changing fonts or `.jrxml`.

### Step 5 — Verify

Generate a PDF containing test text:

```
Čč Ćć Žž Šš Đđ
Porudžbenica — Količina
```

In this repo, run the manual test (see below) and open the output PDF.

---

## Manual PDF sample test (WorkOrderCentral)

Class: `ManualStockAssignmentOrderPdfGenerationTest`

1. Open the test class and **remove `@Disabled`** from `dumpSampleStockAssignmentOrderPdf()`.
2. Run the test from the IDE, **or** from Maven (PowerShell — quote `-D` flags):

   ```powershell
   mvn test "-Dtest=ManualStockAssignmentOrderPdfGenerationTest"
   ```

3. Open: `WorkOrderCentral/target/stock-assignment-order-sample.pdf`
4. Confirm Serbian labels and any faker names with diacritics render correctly.
5. Re-enable `@Disabled` before committing if you only wanted a local preview.

---

## Alternative: custom font (other projects / branding)

Use this when you need a corporate TTF/OTF (e.g. **Noto Sans**, **Arial Unicode MS**) instead of DejaVu.

### A. Add font files to the project

Example layout:

```
src/main/resources/reports/fonts/
    NotoSans-Regular.ttf
    NotoSans-Bold.ttf
    noto-sans-family.xml
    jasperreports_extension.properties
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
    </fontFamily>
</fontFamilies>
```

Paths are **classpath-relative** from `src/main/resources`.

### C. Register the extension (`jasperreports_extension.properties`)

Place in `src/main/resources/jasperreports_extension.properties`:

```properties
net.sf.jasperreports.extension.registry.factory.simple.fonts=net.sf.jasperreports.engine.fonts.SimpleFontExtensionsRegistryFactory
net.sf.jasperreports.extension.simple.fonts.noto=reports/fonts/noto-sans-family.xml
```

Only **one** `jasperreports_extension.properties` is merged from the classpath. If you use both `jasperreports-fonts` and custom fonts, either:

- rely on `jasperreports-fonts` alone (simplest), or  
- copy the DejaVu registration from the fonts JAR and add your family to a single properties file (advanced).

### D. Reference the family in `.jrxml`

Same as Step 2 above, but use your family name:

```xml
fontName="Noto Sans" pdfFontName="Noto Sans" pdfEncoding="Identity-H" isPdfEmbedded="true"
```

### E. License

Ensure your font license allows embedding in PDFs (DejaVu and Noto do; many commercial fonts do not).

---

## Jasper Studio / iReport designers

1. Install the same TTF on the designer machine, **or** add the font extension JAR to the designer classpath.
2. Set report styles to **DejaVu Sans** (or your registered family).
3. Export/compile `.jrxml` and commit both the template and the Maven dependency.

Design-time preview may still differ from runtime until the extension JAR is on the designer classpath.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---------|----------------|-----|
| `□` or `#` for č, ž | Helvetica + Cp1252 | DejaVu + `Identity-H` + embed |
| Works in Studio, fails in app | Fonts JAR missing at runtime | Add `jasperreports-fonts` to runtime classpath / fat JAR |
| Works after deploy, then stale | Cached compiled report | Restart app or invalidate cache when `.jrxml` changes |
| Bold broken | Only regular TTF registered | Add `<bold>…Bold.ttf</bold>` in font family XML |
| Cyrillic needed | Latin font only | Use Noto Sans / DejaVu (broader coverage) or a Cyrillic-capable family |

---

## Reference in this repository

| Item | Location |
|------|----------|
| Maven dependency | `pom.xml` → `jasperreports-fonts` |
| Report template | `src/main/resources/reports/stock-assignment-order.jrxml` |
| Serbian labels | `src/main/resources/reports/stock-assignment-order_sr.properties` |
| PDF generator | `StockProductInventoryService.java` |
| Manual PDF test | `src/test/java/.../ManualStockAssignmentOrderPdfGenerationTest.java` |

---

## Checklist for a new Jasper report

- [ ] `jasperreports-fonts` on classpath (same version as `jasperreports`)
- [ ] Default `<style>` with `DejaVu Sans`, `pdfEncoding="Identity-H"`, `isPdfEmbedded="true"`
- [ ] No orphan `<font>` tags without PDF Unicode settings
- [ ] UTF-8 source files for labels
- [ ] Manual PDF opened and checked for **Čč Žž Šš Đđ** (and any language-specific characters)

---

## See also

- [JasperFonts-v7.MD](./JasperFonts-v7.MD) — same font fix for **JasperReports 7.x** (modular dependencies, migration from v6)
