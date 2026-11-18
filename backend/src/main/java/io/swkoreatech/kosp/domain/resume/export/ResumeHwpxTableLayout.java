package io.swkoreatech.kosp.domain.resume.export;

import static io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.ROW_HEIGHT;
import static io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.TABLE_WIDTH;
import static io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.mm;
import static io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.sectionTitleRow;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;

import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.Align;
import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.Cell;
import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.CellBorder;
import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.Line;
import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.PeriodRow;
import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.Row;
import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.TextStyle;
import io.swkoreatech.kosp.domain.resume.export.HwpxDocumentBuilder.VAlign;
import lombok.extern.slf4j.Slf4j;

/**
 * 표 기반 정형 양식으로 이력서를 쓴다. 섹션 구성과 순서는 PDF 출력({@code ResumePrintLayout})을 따른다.
 *
 * <pre>
 * 머리(테두리 없는 표) → 학력 → 경력 → 교육이력 → 자격증 → 수상이력 → 기술스택 → 링크
 * → 프로젝트 → 간단소개 → 자기소개서 → 꼬리(테두리 없는 표)
 * </pre>
 *
 * <p>규칙</p>
 * <ul>
 *   <li>데이터가 0건인 섹션은 표를 만들지 않는다 (제목 행도 표 안에 있으므로 함께 사라진다).</li>
 *   <li>값이 빈 라벨 행(프로젝트)은 행째 뺀다. 머리 연락처 줄의 빈 항목은 라벨·구분자까지 뺀다.</li>
 *   <li>섹션 노출 여부({@code visibleSections})는 문단 방식과 같은 키로 판단한다.</li>
 *   <li>커스텀 섹션은 이 양식에 포함하지 않는다 (확정 사항).</li>
 *   <li>모든 문자열은 {@link HwpxTextSanitizer} 를 거친다.</li>
 * </ul>
 */
@Slf4j
class ResumeHwpxTableLayout {

    private static final String DEFAULT_NAME = "이력서";
    private static final String CONTACT_SEPARATOR = "  |  ";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    /** 1칸 표. (3열 표는 {@link HwpxDocumentBuilder#addPeriodTable} 이 34mm/46mm/나머지로 만든다) */
    private static final long[] SINGLE_COLUMN = {TABLE_WIDTH};
    /** 링크 표: 라벨 34mm / URL. */
    private static final long[] LINK_COLUMNS = {mm(34), TABLE_WIDTH - mm(34)};
    /** 프로젝트 표: 라벨 26mm / 값 / 기간 40mm (값 칸은 기간 칸과 합쳐 colspan 2). */
    private static final long[] PROJECT_COLUMNS = {mm(26), TABLE_WIDTH - mm(26) - mm(40), mm(40)};
    /** 머리 표: 정보 150mm / 사진 30mm. */
    private static final long[] HEAD_COLUMNS = {TABLE_WIDTH - mm(30), mm(30)};
    private static final long PHOTO_HEIGHT = mm(32);

    private final ResumeDataReader reader;
    private final ResumeProfile profile;
    private final Long resumeId;
    private final LocalDate today;

    ResumeHwpxTableLayout(ResumeDataReader reader, ResumeProfile profile, Long resumeId) {
        this(reader, profile, resumeId, LocalDate.now(KST));
    }

    ResumeHwpxTableLayout(ResumeDataReader reader, ResumeProfile profile, Long resumeId, LocalDate today) {
        this.reader = reader;
        this.profile = profile == null ? ResumeProfile.EMPTY : profile;
        this.resumeId = resumeId;
        this.today = today;
    }

    void write(HwpxDocumentBuilder doc) {
        doc.useTableMargins();
        writeHead(doc);
        writePeriodSection(doc, "sec-education", "학력", "education", "school", item -> List.of(
            line(item, "major", "education.major")));
        writePeriodSection(doc, "sec-career", "경력", "career", "company", item -> List.of(
            line(item, "role", "career.role")));
        writePeriodSection(doc, "sec-experience", "교육이력", "experience", "title", item ->
            paragraphs(ResumeDataReader.text(item, "description"), "experience.description"));
        writeCertifications(doc);
        writeAwards(doc);
        writeTechStack(doc);
        writeLinks(doc);
        writeProjects(doc);
        writeBio(doc);
        writeCoverLetters(doc);
        writeFoot(doc);
    }

    // ── 머리 ─────────────────────────────────────────────────────────

    private void writeHead(HwpxDocumentBuilder doc) {
        List<Line> info = new ArrayList<>();
        info.add(new Line(displayName(), TextStyle.NAME));

        if (reader.isSectionVisible("sec-basic")) {
            String headline = field(reader.text("headline"), "headline");
            if (!headline.isBlank()) {
                info.add(new Line(headline, TextStyle.TAGLINE));
            }
        }

        String contact = contactLine();
        if (!contact.isBlank()) {
            info.add(new Line(contact, TextStyle.CONTACT));
        }

        Cell infoCell = new Cell(1, CellBorder.NONE, Align.LEFT, VAlign.TOP, info);
        Cell photoCell = new Cell(1, CellBorder.LINE, Align.CENTER, VAlign.CENTER, List.of(
            new Line("사진", TextStyle.PHOTO),
            new Line("3 × 4", TextStyle.PHOTO)));
        doc.addTable(HEAD_COLUMNS, List.of(new Row(PHOTO_HEIGHT, List.of(infoCell, photoCell))), CellBorder.NONE);
    }

    /** 이름. User 이름이 없으면(공개 이력서 소유자 정보를 못 읽은 경우 등) 이력서 제목으로 대신한다. */
    String displayName() {
        String name = HwpxTextSanitizer.singleLine(profile.name());
        if (!name.isBlank()) {
            return name;
        }
        String title = field(reader.text("resumeTitle"), "resumeTitle");
        return title.isBlank() ? DEFAULT_NAME : title;
    }

    /** "직무 …  |  이메일 …  |  GitHub …". 값이 빈 항목은 라벨과 구분자까지 뺀다. */
    String contactLine() {
        List<String> parts = new ArrayList<>();
        if (reader.isSectionVisible("sec-jobRole")) {
            String roles = joinSanitized(reader.strings("jobRole"));
            if (!roles.isBlank()) {
                parts.add("직무 " + roles);
            }
        }
        String email = HwpxTextSanitizer.singleLine(profile.email());
        if (!email.isBlank()) {
            parts.add("이메일 " + email);
        }
        String github = displayUrl(githubUrl());
        if (!github.isBlank()) {
            parts.add("GitHub " + github);
        }
        return String.join(CONTACT_SEPARATOR, parts);
    }

    /** User 의 GitHub 주소. 없으면 링크 섹션에서 라벨이 GitHub 인 항목을 쓴다. */
    private String githubUrl() {
        String url = HwpxTextSanitizer.singleLine(profile.githubUrl());
        if (!url.isBlank()) {
            return url;
        }
        for (JsonNode item : reader.objects("links")) {
            String label = HwpxTextSanitizer.singleLine(ResumeDataReader.text(item, "label"));
            if ("github".equals(label.toLowerCase(Locale.ROOT))) {
                return HwpxTextSanitizer.singleLine(ResumeDataReader.text(item, "url"));
            }
        }
        return "";
    }

    // ── 3열 표 섹션 ──────────────────────────────────────────────────

    @FunctionalInterface
    private interface DetailReader {
        List<String> read(JsonNode item);
    }

    private void writePeriodSection(HwpxDocumentBuilder doc, String sectionKey, String title,
                                    String arrayKey, String nameKey, DetailReader detailReader) {
        if (!reader.isSectionVisible(sectionKey)) {
            return;
        }
        List<PeriodRow> rows = new ArrayList<>();
        for (JsonNode item : reader.objects(arrayKey)) {
            String name = field(ResumeDataReader.text(item, nameKey), arrayKey + "." + nameKey);
            if (name.isBlank()) {
                continue;
            }
            String period = field(ResumeDataReader.period(item), arrayKey + ".period");
            rows.add(new PeriodRow(period, name, nonBlank(detailReader.read(item))));
        }
        doc.addPeriodTable(title, rows);
    }

    /** 자격증: 취득일 | 자격증명 | 발급기관 · 취득상태. */
    private void writeCertifications(HwpxDocumentBuilder doc) {
        if (!reader.isSectionVisible("sec-certifications")) {
            return;
        }
        List<PeriodRow> rows = new ArrayList<>();
        for (JsonNode item : reader.objects("certifications")) {
            String name = field(ResumeDataReader.text(item, "name"), "certifications.name");
            if (name.isBlank()) {
                continue;
            }
            String organization = line(item, "organization", "certifications.organization");
            String status = statusLabel(ResumeDataReader.text(item, "status"));
            String detail = joinNonBlank(" · ", organization, status);
            rows.add(new PeriodRow(line(item, "date", "certifications.date"), name, nonBlank(List.of(detail))));
        }
        doc.addPeriodTable("자격증", rows);
    }

    /** 수상이력: 수상일 | 수상명 | 주최기관 + 설명(여러 문단). */
    private void writeAwards(HwpxDocumentBuilder doc) {
        if (!reader.isSectionVisible("sec-awards")) {
            return;
        }
        List<PeriodRow> rows = new ArrayList<>();
        for (JsonNode item : reader.objects("awards")) {
            String name = field(ResumeDataReader.text(item, "name"), "awards.name");
            if (name.isBlank()) {
                continue;
            }
            List<String> detail = new ArrayList<>();
            detail.add(line(item, "organization", "awards.organization"));
            detail.addAll(paragraphs(ResumeDataReader.text(item, "description"), "awards.description"));
            rows.add(new PeriodRow(line(item, "date", "awards.date"), name, nonBlank(detail)));
        }
        doc.addPeriodTable("수상이력", rows);
    }

    // ── 1칸 표 섹션 ──────────────────────────────────────────────────

    private void writeTechStack(HwpxDocumentBuilder doc) {
        if (!reader.isSectionVisible("sec-techStack")) {
            return;
        }
        String stack = joinSanitized(reader.strings("techStack"));
        if (stack.isBlank()) {
            return;
        }
        addSingleColumnTable(doc, "기술스택", List.of(new Line(stack, TextStyle.BODY)), Align.LEFT);
    }

    private void writeBio(HwpxDocumentBuilder doc) {
        if (!reader.isSectionVisible("sec-bio")) {
            return;
        }
        List<String> bio = paragraphs(reader.text("bio"), "bio");
        if (bio.isEmpty()) {
            return;
        }
        addSingleColumnTable(doc, "간단소개", lines(bio, TextStyle.LONG_BODY), Align.JUSTIFY);
    }

    private void addSingleColumnTable(HwpxDocumentBuilder doc, String title, List<Line> body, Align align) {
        doc.addTable(SINGLE_COLUMN, List.of(
            sectionTitleRow(title, 1),
            new Row(ROW_HEIGHT, List.of(new Cell(1, CellBorder.LINE, align, VAlign.CENTER, body)))),
            CellBorder.LINE);
    }

    // ── 링크 ─────────────────────────────────────────────────────────

    private void writeLinks(HwpxDocumentBuilder doc) {
        if (!reader.isSectionVisible("sec-links")) {
            return;
        }
        List<Row> rows = new ArrayList<>();
        rows.add(sectionTitleRow("링크", LINK_COLUMNS.length));
        for (JsonNode item : reader.objects("links")) {
            String url = field(ResumeDataReader.text(item, "url"), "links.url");
            if (url.isBlank()) {
                continue;
            }
            String label = field(ResumeDataReader.text(item, "label"), "links.label");
            rows.add(new Row(ROW_HEIGHT, List.of(
                new Cell(1, CellBorder.LINE, Align.CENTER, VAlign.CENTER,
                    List.of(new Line(label.isBlank() ? "링크" : label, TextStyle.LABEL))),
                new Cell(1, CellBorder.LINE, Align.LEFT, VAlign.CENTER, List.of(new Line(url, TextStyle.BODY))))));
        }
        if (rows.size() > 1) {
            doc.addTable(LINK_COLUMNS, rows, CellBorder.LINE);
        }
    }

    // ── 프로젝트 ─────────────────────────────────────────────────────

    private void writeProjects(HwpxDocumentBuilder doc) {
        if (!reader.isSectionVisible("sec-projects")) {
            return;
        }
        List<Row> rows = new ArrayList<>();
        rows.add(sectionTitleRow("프로젝트", PROJECT_COLUMNS.length));
        for (JsonNode item : reader.objects("projects")) {
            String name = field(ResumeDataReader.text(item, "name"), "projects.name");
            if (name.isBlank()) {
                continue;
            }
            String period = field(ResumeDataReader.period(item), "projects.period");
            rows.add(new Row(ROW_HEIGHT, List.of(
                new Cell(2, CellBorder.PROJECT_HEADER, Align.LEFT, VAlign.CENTER,
                    List.of(new Line(name, TextStyle.PROJECT_NAME))),
                new Cell(1, CellBorder.PROJECT_HEADER, Align.RIGHT, VAlign.CENTER,
                    List.of(new Line(period, TextStyle.PROJECT_PERIOD))))));

            addLabelRow(rows, "요약", paragraphs(ResumeDataReader.text(item, "summary"), "projects.summary"));
            addLabelRow(rows, "역할", List.of(line(item, "role", "projects.role")));
            addLabelRow(rows, "기술", List.of(joinSanitized(ResumeDataReader.strings(item, "techStack"))));
            addLabelRow(rows, "주요 기능",
                paragraphs(ResumeDataReader.text(item, "mainFeatures"), "projects.mainFeatures"));
            addLabelRow(rows, "기여",
                paragraphs(ResumeDataReader.text(item, "myContributions"), "projects.myContributions"));
            addLabelRow(rows, "문제 해결",
                paragraphs(ResumeDataReader.text(item, "problemSolving"), "projects.problemSolving"));
            addLabelRow(rows, "성과", paragraphs(ResumeDataReader.text(item, "result"), "projects.result"));
            addLabelRow(rows, "GitHub", List.of(line(item, "githubLink", "projects.githubLink")));
            addLabelRow(rows, "배포", List.of(line(item, "deployLink", "projects.deployLink")));
            // 문단 방식에서 출력하던 문서 링크. 비어 있으면 행이 생기지 않는다.
            addLabelRow(rows, "문서", List.of(line(item, "docLink", "projects.docLink")));
        }
        if (rows.size() > 1) {
            doc.addTable(PROJECT_COLUMNS, rows, CellBorder.LINE);
        }
    }

    /** 라벨 | 값(colspan 2). 값이 모두 비면 행을 만들지 않는다. */
    private void addLabelRow(List<Row> rows, String label, List<String> values) {
        List<String> value = nonBlank(values);
        if (value.isEmpty()) {
            return;
        }
        rows.add(new Row(ROW_HEIGHT, List.of(
            new Cell(1, CellBorder.LINE, Align.CENTER, VAlign.CENTER, List.of(new Line(label, TextStyle.LABEL))),
            new Cell(2, CellBorder.LINE, Align.JUSTIFY, VAlign.CENTER, lines(value, TextStyle.BODY)))));
    }

    // ── 자기소개서 ───────────────────────────────────────────────────

    private void writeCoverLetters(HwpxDocumentBuilder doc) {
        if (!reader.isSectionVisible("sec-coverLetters")) {
            return;
        }
        List<Row> rows = new ArrayList<>();
        rows.add(sectionTitleRow("자기소개서", 1));
        for (JsonNode item : reader.objects("coverLetters")) {
            List<String> body = paragraphs(ResumeDataReader.text(item, "content"), "coverLetters.content");
            if (body.isEmpty()) {
                continue;
            }
            String title = field(ResumeDataReader.text(item, "title"), "coverLetters.title");
            if (!title.isBlank()) {
                rows.add(new Row(ROW_HEIGHT, List.of(new Cell(1, CellBorder.LINE, Align.LEFT, VAlign.CENTER,
                    List.of(new Line(title, TextStyle.LETTER_TITLE))))));
            }
            rows.add(new Row(ROW_HEIGHT, List.of(new Cell(1, CellBorder.LINE, Align.JUSTIFY, VAlign.TOP,
                lines(body, TextStyle.LONG_BODY)))));
        }
        if (rows.size() > 1) {
            doc.addTable(SINGLE_COLUMN, rows, CellBorder.LINE);
        }
    }

    // ── 꼬리 ─────────────────────────────────────────────────────────

    private void writeFoot(HwpxDocumentBuilder doc) {
        String text = todayText() + " · " + displayName();
        doc.addTable(SINGLE_COLUMN, List.of(new Row(ROW_HEIGHT, List.of(
            new Cell(1, CellBorder.NONE, Align.CENTER, VAlign.CENTER, List.of(new Line(text, TextStyle.FOOTER)))))),
            CellBorder.NONE);
    }

    /** PDF 꼬리와 같은 형식: 2026년 11월 18일. */
    String todayText() {
        return today.getYear() + "년 " + today.getMonthValue() + "월 " + today.getDayOfMonth() + "일";
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────

    private String line(JsonNode item, String key, String fieldPath) {
        return field(ResumeDataReader.text(item, key), fieldPath);
    }

    private String field(String raw, String fieldPath) {
        logIfTruncated(raw, fieldPath);
        return HwpxTextSanitizer.singleLine(raw);
    }

    private List<String> paragraphs(String raw, String fieldPath) {
        logIfTruncated(raw, fieldPath);
        return HwpxTextSanitizer.paragraphs(raw);
    }

    private void logIfTruncated(String raw, String fieldPath) {
        if (HwpxTextSanitizer.exceedsLimit(raw)) {
            log.warn("[ResumeHwpx] 필드 절단: resumeId={}, field={}, length={}, limit={}",
                resumeId, fieldPath, raw.length(), HwpxTextSanitizer.MAX_FIELD_LENGTH);
        }
    }

    private static List<Line> lines(List<String> texts, TextStyle style) {
        List<Line> lines = new ArrayList<>();
        for (String text : texts) {
            lines.add(new Line(text, style));
        }
        return lines;
    }

    private static List<String> nonBlank(List<String> values) {
        List<String> result = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                result.add(value);
            }
        }
        return result;
    }

    private static String joinNonBlank(String separator, String... values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                parts.add(value);
            }
        }
        return String.join(separator, parts);
    }

    private static String joinSanitized(List<String> values) {
        List<String> parts = new ArrayList<>();
        for (String value : values) {
            String clean = HwpxTextSanitizer.singleLine(value);
            if (!clean.isBlank()) {
                parts.add(clean);
            }
        }
        return String.join(", ", parts);
    }

    /** 연락처 표기용으로 URL 의 프로토콜과 끝 슬래시를 뗀다 (PDF 와 동일). */
    private static String displayUrl(String url) {
        if (url == null) {
            return "";
        }
        return url.replaceFirst("(?i)^https?://", "").replaceAll("/+$", "");
    }

    private static String statusLabel(String status) {
        if ("ACQUIRED".equals(status)) {
            return "취득";
        }
        if ("EXPIRED".equals(status)) {
            return "만료";
        }
        return "";
    }
}
