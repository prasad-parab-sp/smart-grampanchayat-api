package com.asset.smartgrampanchayatapi.district.service.certificate;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;

import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateApplication;
import com.asset.smartgrampanchayatapi.district.jpa.model.CertificateDocumentFormat;
import com.asset.smartgrampanchayatapi.web.dto.TenantProfileDto;

/**
 * Server-side HTML for issued certificates — mirrors web {@code admin-format-preview.utils.ts} token rules.
 * Dynamic certificate type fields use {@code {$extra.fieldKey}} with values from
 * {@code certificate_application.additional_values_json} (same keys as {@code certificate_type_field.field_key}).
 */
public final class CertificateHtmlExpansionService {

    private static final Pattern EXTRA_JSON_KEY = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private CertificateHtmlExpansionService() {
    }

    public static String buildIssuedHtml(
            TenantProfileDto tenant,
            CertificateDocumentFormat format,
            String lang,
            CertificateApplication app
    ) {
        Objects.requireNonNull(tenant, "tenant");
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(app, "application");
        String body = format.getBodyHtml();
        String documentTitle = format.getDocumentTitle() == null ? "" : format.getDocumentTitle().trim();
        String applicantName = nz(app.getApplicantFullName());
        String mobile = nz(app.getApplicantMobile());
        String address = nz(app.getAddressText());
        String purpose = nz(firstNonBlank(app.getReasonShort(), app.getReasonDetails()));
        String certificateNo = nz(app.getApplicationNumber());
        Instant dateSource = app.getApprovedAt() != null ? app.getApprovedAt() : app.getSubmittedAt();
        String dateDisplay = formatDate(dateSource, lang);
        String sarpanch = nz(tenant.sarpanchName());
        String gramsevak = nz(tenant.gramsevakName());

        String header = buildPrintableHeaderHtml(tenant, lang);
        String footer = buildPrintableFooterHtml(lang, sarpanch, gramsevak);
        String titleBlock = buildDocumentTitleHtml(documentTitle);

        String gpName = gpTitleName(tenant, lang);
        String gpLine = joinNonBlank(gpTitlePrefix(lang), gpName);

        String out = body;
        out = out.replace("{$header}", header);
        out = out.replace("{$footer}", footer);
        out = out.replace("[$header]", header);
        out = out.replace("[$footer]", footer);
        out = out.replace("{$title}", titleBlock);
        out = out.replace("[$title]", titleBlock);

        out = out.replace("[नाव]", escapeHtml(applicantName));
        out = out.replace("[मोबाईल]", escapeHtml(mobile));
        out = out.replace("[पत्ता]", escapeHtml(address));
        out = out.replace("[कशासाठी]", escapeHtml(purpose));
        out = out.replace("[दिनांक]", escapeHtml(dateDisplay));
        out = out.replace("[दाखला_क्र]", escapeHtml(certificateNo));
        out = out.replace("{$name}", escapeHtml(applicantName));
        out = out.replace("{$purpose}", escapeHtml(purpose));
        out = out.replace("{$mobile}", escapeHtml(mobile));
        out = out.replace("{$address}", escapeHtml(address));
        out = out.replace("{$certificate_no}", escapeHtml(certificateNo));
        out = out.replace("{$date}", escapeHtml(dateDisplay));
        out = out.replace("{$gp_line}", escapeHtml(gpLine));
        out = out.replace("{$gp_name}", escapeHtml(gpName));
        out = out.replace("{$sarpanch}", escapeHtml(sarpanch));
        out = out.replace("{$gramsevak}", escapeHtml(gramsevak));
        out = out.replace("[सरपंच]", escapeHtml(sarpanch));
        out = out.replace("[ग्रामसेवक]", escapeHtml(gramsevak));

        out = applyAdditionalJsonPlaceholders(out, app.getAdditionalValuesJson());

        out = maybeInjectDocumentTitle(out, documentTitle);
        String footerNote = format.getFooterNote();
        if (footerNote != null && !footerNote.isBlank()) {
            out = out + "<p class=\"fmt-footer-note\">" + escapeHtml(footerNote.trim()) + "</p>";
        }
        return "<div class=\"fmt-preview-print\">" + out + "</div>";
    }

    private static String applyAdditionalJsonPlaceholders(String html, JsonNode root) {
        if (root == null || root.isNull() || !root.isObject()) {
            return html;
        }
        String out = html;
        for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext();) {
            Map.Entry<String, JsonNode> e = it.next();
            String key = e.getKey();
            if (key == null || !EXTRA_JSON_KEY.matcher(key).matches()) {
                continue;
            }
            String token = "{$extra." + key + "}";
            if (!out.contains(token)) {
                continue;
            }
            out = out.replace(token, escapeHtml(jsonNodeToPlainText(e.getValue())));
        }
        return out;
    }

    private static String jsonNodeToPlainText(JsonNode n) {
        if (n == null || n.isNull()) {
            return "";
        }
        if (n.isTextual()) {
            return n.asText();
        }
        if (n.isNumber() || n.isBoolean()) {
            return n.asText();
        }
        return n.toString();
    }

    private static String maybeInjectDocumentTitle(String html, String documentTitle) {
        String t = documentTitle == null ? "" : documentTitle.trim();
        if (t.isEmpty() || html.contains("fmt-doc-title")) {
            return html;
        }
        int start = html.indexOf("fmt-bw-header");
        if (start < 0) {
            return html;
        }
        int close = html.indexOf("</div></div>", start);
        if (close < 0) {
            return html;
        }
        close += "</div></div>".length();
        return html.substring(0, close) + "<div class=\"fmt-doc-title\">" + escapeHtml(t) + "</div>" + html.substring(close);
    }

    private static String buildPrintableHeaderHtml(TenantProfileDto tenant, String lang) {
        String gpName = gpTitleName(tenant, lang);
        String meta = formatTalukaDistrictLine(tenant, lang);
        String logo = tenant.logoUrl() != null && !tenant.logoUrl().isBlank()
                ? tenant.logoUrl().trim()
                : "/assets/images/logo.png";
        String helpline = tenant.contactPhone() == null ? "" : tenant.contactPhone().trim();
        String helplineLabel = "en".equals(lang) ? "Helpline" : "हॅल्पलाइन";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"fmt-bw-header\">");
        sb.append("<img class=\"fmt-bw-header__logo\" src=\"").append(escapeHtml(logo)).append("\" alt=\"\" />");
        sb.append("<div class=\"fmt-bw-header__text\">");
        sb.append("<div class=\"fmt-bw-header__prefix\">").append(escapeHtml(gpTitlePrefix(lang))).append("</div>");
        if (!gpName.isEmpty()) {
            sb.append("<div class=\"fmt-bw-header__name\">").append(escapeHtml(gpName)).append("</div>");
        }
        if (!meta.isEmpty()) {
            sb.append("<div class=\"fmt-bw-header__meta\">").append(escapeHtml(meta)).append("</div>");
        }
        if (!helpline.isEmpty()) {
            sb.append("<div class=\"fmt-bw-header__helpline\">")
                    .append(escapeHtml(helplineLabel))
                    .append(": ")
                    .append(escapeHtml(helpline))
                    .append("</div>");
        }
        sb.append("</div></div>");
        return sb.toString();
    }

    private static String buildDocumentTitleHtml(String title) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty()) {
            return "";
        }
        return "<div class=\"fmt-doc-title\">" + escapeHtml(t) + "</div>";
    }

    private static String buildPrintableFooterHtml(String lang, String sarpanchName, String gramsevakName) {
        boolean en = "en".equals(lang);
        String lblS = en ? "Sarpanch" : "सरपंच";
        String lblG = en ? "Gramsevak" : "ग्रामसेवक";
        String stamp = en ? "Official stamp / seal area" : "अधिकृत शिक्का / ठिकाण";
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"fmt-bw-footer\">");
        sb.append("<div class=\"fmt-bw-footer__row\">");
        sb.append("<div class=\"fmt-bw-footer__sig\"><span class=\"fmt-bw-footer__lbl\">")
                .append(escapeHtml(lblS))
                .append("</span><br/><strong>")
                .append(escapeHtml(sarpanchName))
                .append("</strong></div>");
        sb.append("<div class=\"fmt-bw-footer__sig\"><span class=\"fmt-bw-footer__lbl\">")
                .append(escapeHtml(lblG))
                .append("</span><br/><strong>")
                .append(escapeHtml(gramsevakName))
                .append("</strong></div>");
        sb.append("</div>");
        sb.append("<div class=\"fmt-bw-footer__stamp\">").append(escapeHtml(stamp)).append("</div>");
        sb.append("</div>");
        return sb.toString();
    }

    private static String gpTitlePrefix(String lang) {
        return "en".equals(lang) ? "Gram Panchayat" : "ग्रामपंचायत";
    }

    private static String gpTitleName(TenantProfileDto tenant, String lang) {
        if ("en".equals(lang)) {
            if (tenant.displayNameEn() != null && !tenant.displayNameEn().isBlank()) {
                return tenant.displayNameEn().trim();
            }
            return tenant.name() == null ? "" : tenant.name().trim();
        }
        if (tenant.displayNameMr() != null && !tenant.displayNameMr().isBlank()) {
            return tenant.displayNameMr().trim();
        }
        return tenant.name() == null ? "" : tenant.name().trim();
    }

    private static String formatTalukaDistrictLine(TenantProfileDto tenant, String lang) {
        if ("en".equals(lang)) {
            return joinNonBlank(tenant.talukaEn(), tenant.districtNameEn());
        }
        return joinNonBlank(tenant.talukaMr(), tenant.districtNameMr());
    }

    private static String joinNonBlank(String a, String b) {
        String x = a == null ? "" : a.trim();
        String y = b == null ? "" : b.trim();
        if (x.isEmpty()) {
            return y;
        }
        if (y.isEmpty()) {
            return x;
        }
        return x + " · " + y;
    }

    private static String formatDate(Instant instant, String lang) {
        if (instant == null) {
            return "";
        }
        var z = instant.atZone(IST).toLocalDate();
        if ("en".equals(lang)) {
            return z.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.UK));
        }
        return z.format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("mr", "IN")));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a.trim();
        }
        if (b != null && !b.isBlank()) {
            String t = b.trim();
            return t.length() > 200 ? t.substring(0, 200) + "…" : t;
        }
        return "";
    }

    private static String nz(String s) {
        return s == null ? "" : s.trim();
    }

    static String escapeHtml(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
