package io.swkoreatech.kosp.domain.resume.export;

import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swkoreatech.kosp.common.exception.ExceptionMessage;
import io.swkoreatech.kosp.common.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code resume_data}(JSONB) 를 hwpx 바이트로 변환한다.
 *
 * <p>섹션 순서와 표시 규칙은 프론트엔드 {@code ResumeReadOnlyView} 를 따른다.
 * {@code visibleSections} 가 없으면 전체 표시, 있으면 값이 {@code false} 가 아닌 섹션만 출력한다.
 * 커스텀 섹션은 프론트에도 가드가 없으므로 항상 출력한다.</p>
 *
 * <p>모든 값 접근은 {@link ResumeDataReader} 를 통하므로, 타입이 어긋난 데이터가 들어와도
 * 예외 없이 빈 값으로 흡수된다. 문자열은 {@link HwpxTextSanitizer} 로 정제한 뒤 기록한다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeHwpxExporter {

    private static final String DEFAULT_TITLE = "이력서";

    private final ObjectMapper objectMapper;

    /**
     * 이력서 JSON 을 hwpx 바이트로 변환한다.
     *
     * @param resumeData {@code user_resume.resume_data} 원본 JSON 문자열
     * @param resumeId   로깅용 이력서 식별자
     * @return hwpx 파일 바이트
     * @throws GlobalException 문서 생성에 실패한 경우
     */
    public byte[] export(String resumeData, Long resumeId) {
        ResumeDataReader reader = ResumeDataReader.of(parse(resumeData, resumeId));
        HwpxDocumentBuilder doc = new HwpxDocumentBuilder();

        writeTitle(doc, reader, resumeId);
        writeBasic(doc, reader, resumeId);
        writeJobRole(doc, reader);
        writeBio(doc, reader, resumeId);
        writeTechStack(doc, reader);
        writeLinks(doc, reader, resumeId);
        writeEducation(doc, reader, resumeId);
        writeCareer(doc, reader, resumeId);
        writeProjects(doc, reader, resumeId);
        writeExperience(doc, reader, resumeId);
        writeAwards(doc, reader, resumeId);
        writeCertifications(doc, reader, resumeId);
        writeCoverLetters(doc, reader, resumeId);
        writeCustomSections(doc, reader, resumeId);

        try {
            return doc.toBytes();
        } catch (Exception e) {
            log.error("hwpx 생성 실패: resumeId={}", resumeId, e);
            throw new GlobalException(ExceptionMessage.SERVER_ERROR);
        }
    }

    /**
     * 문서 제목으로 쓸 이력서 제목을 반환한다 (파일명 생성에도 쓴다).
     */
    public String resolveTitle(String resumeData) {
        ResumeDataReader reader = ResumeDataReader.of(parse(resumeData, null));
        String title = HwpxTextSanitizer.singleLine(reader.text("resumeTitle"));
        return title.isBlank() ? DEFAULT_TITLE : title;
    }

    /**
     * JSON 을 파싱한다. 깨진 데이터면 500 대신 404 로 흡수한다.
     */
    private JsonNode parse(String resumeData, Long resumeId) {
        if (resumeData == null || resumeData.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(resumeData);
        } catch (Exception e) {
            log.warn("resume_data 파싱 실패: resumeId={}, error={}", resumeId, e.getMessage());
            return null;
        }
    }

    // ── 섹션별 출력 ───────────────────────────────────────────────────

    private void writeTitle(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        String title = field(reader.text("resumeTitle"), resumeId, "resumeTitle");
        doc.addTitle(title.isBlank() ? DEFAULT_TITLE : title);
        doc.addBlankLine();
    }

    private void writeBasic(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-basic")) {
            return;
        }
        String headline = field(reader.text("headline"), resumeId, "headline");
        if (headline.isBlank()) {
            return;
        }
        doc.addBody(headline);
        doc.addBlankLine();
    }

    private void writeJobRole(HwpxDocumentBuilder doc, ResumeDataReader reader) {
        if (!reader.isSectionVisible("sec-jobRole")) {
            return;
        }
        List<String> roles = reader.strings("jobRole");
        if (roles.isEmpty()) {
            return;
        }
        doc.addHeading("개발 직무");
        doc.addBody(joinSanitized(roles));
        doc.addBlankLine();
    }

    private void writeBio(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-bio")) {
            return;
        }
        String raw = reader.text("bio");
        logIfTruncated(raw, resumeId, "bio");
        List<String> paragraphs = HwpxTextSanitizer.paragraphs(raw);
        if (paragraphs.isEmpty()) {
            return;
        }
        doc.addHeading("간단소개");
        doc.addBodyParagraphs(paragraphs);
        doc.addBlankLine();
    }

    private void writeTechStack(HwpxDocumentBuilder doc, ResumeDataReader reader) {
        if (!reader.isSectionVisible("sec-techStack")) {
            return;
        }
        List<String> stack = reader.strings("techStack");
        if (stack.isEmpty()) {
            return;
        }
        doc.addHeading("기술스택");
        doc.addBody(joinSanitized(stack));
        doc.addBlankLine();
    }

    private void writeLinks(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-links")) {
            return;
        }
        List<JsonNode> items = reader.objects("links");
        boolean headingWritten = false;
        for (JsonNode item : items) {
            String url = field(ResumeDataReader.text(item, "url"), resumeId, "links.url");
            if (url.isBlank()) {
                continue;
            }
            String label = field(ResumeDataReader.text(item, "label"), resumeId, "links.label");
            headingWritten = writeHeadingOnce(doc, "링크", headingWritten);
            doc.addBody(label.isBlank() ? url : label + " — " + url);
        }
        closeSection(doc, headingWritten);
    }

    private void writeEducation(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-education")) {
            return;
        }
        boolean headingWritten = false;
        for (JsonNode item : reader.objects("education")) {
            String school = field(ResumeDataReader.text(item, "school"), resumeId, "education.school");
            if (school.isBlank()) {
                continue;
            }
            headingWritten = writeHeadingOnce(doc, "학력", headingWritten);
            doc.addBody(school);
            String major = field(ResumeDataReader.text(item, "major"), resumeId, "education.major");
            if (!major.isBlank()) {
                doc.addMeta(major);
            }
            addPeriod(doc, item, resumeId, "education");
        }
        closeSection(doc, headingWritten);
    }

    private void writeCareer(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-career")) {
            return;
        }
        boolean headingWritten = false;
        for (JsonNode item : reader.objects("career")) {
            String company = field(ResumeDataReader.text(item, "company"), resumeId, "career.company");
            if (company.isBlank()) {
                continue;
            }
            headingWritten = writeHeadingOnce(doc, "경력", headingWritten);
            doc.addBody(company);
            String role = field(ResumeDataReader.text(item, "role"), resumeId, "career.role");
            if (!role.isBlank()) {
                doc.addMeta(role);
            }
            addPeriod(doc, item, resumeId, "career");
        }
        closeSection(doc, headingWritten);
    }

    private void writeProjects(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-projects")) {
            return;
        }
        boolean headingWritten = false;
        for (JsonNode item : reader.objects("projects")) {
            String name = field(ResumeDataReader.text(item, "name"), resumeId, "projects.name");
            if (name.isBlank()) {
                continue;
            }
            headingWritten = writeHeadingOnce(doc, "프로젝트", headingWritten);

            String period = HwpxTextSanitizer.singleLine(ResumeDataReader.period(item));
            doc.addBody(period.isBlank() ? name : name + " (" + period + ")");

            List<String> stack = ResumeDataReader.strings(item, "techStack");
            if (!stack.isEmpty()) {
                doc.addLabeled("기술스택", joinSanitized(stack));
            }
            doc.addLabeled("역할", field(ResumeDataReader.text(item, "role"), resumeId, "projects.role"));
            addLongField(doc, item, "summary", "요약", resumeId);
            addLongField(doc, item, "mainFeatures", "주요 기능", resumeId);
            addLongField(doc, item, "myContributions", "기여한 부분", resumeId);
            addLongField(doc, item, "problemSolving", "문제 해결", resumeId);
            addLongField(doc, item, "result", "성과", resumeId);
            doc.addLabeled("GitHub", field(ResumeDataReader.text(item, "githubLink"), resumeId, "projects.githubLink"));
            doc.addLabeled("배포", field(ResumeDataReader.text(item, "deployLink"), resumeId, "projects.deployLink"));
            doc.addLabeled("문서", field(ResumeDataReader.text(item, "docLink"), resumeId, "projects.docLink"));
            doc.addBlankLine();
        }
        closeSection(doc, headingWritten);
    }

    private void writeExperience(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-experience")) {
            return;
        }
        boolean headingWritten = false;
        for (JsonNode item : reader.objects("experience")) {
            String title = field(ResumeDataReader.text(item, "title"), resumeId, "experience.title");
            if (title.isBlank()) {
                continue;
            }
            headingWritten = writeHeadingOnce(doc, "교육이력", headingWritten);
            String period = HwpxTextSanitizer.singleLine(ResumeDataReader.period(item));
            doc.addBody(period.isBlank() ? title : title + " (" + period + ")");
            addLongText(doc, ResumeDataReader.text(item, "description"), resumeId, "experience.description");
        }
        closeSection(doc, headingWritten);
    }

    private void writeAwards(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-awards")) {
            return;
        }
        boolean headingWritten = false;
        for (JsonNode item : reader.objects("awards")) {
            String name = field(ResumeDataReader.text(item, "name"), resumeId, "awards.name");
            if (name.isBlank()) {
                continue;
            }
            headingWritten = writeHeadingOnce(doc, "수상이력", headingWritten);
            String date = field(ResumeDataReader.text(item, "date"), resumeId, "awards.date");
            doc.addBody(date.isBlank() ? name : name + " (" + date + ")");
            String organization = field(ResumeDataReader.text(item, "organization"), resumeId, "awards.organization");
            if (!organization.isBlank()) {
                doc.addMeta(organization);
            }
            addLongText(doc, ResumeDataReader.text(item, "description"), resumeId, "awards.description");
        }
        closeSection(doc, headingWritten);
    }

    private void writeCertifications(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-certifications")) {
            return;
        }
        boolean headingWritten = false;
        for (JsonNode item : reader.objects("certifications")) {
            String name = field(ResumeDataReader.text(item, "name"), resumeId, "certifications.name");
            if (name.isBlank()) {
                continue;
            }
            headingWritten = writeHeadingOnce(doc, "자격증", headingWritten);

            StringBuilder line = new StringBuilder(name);
            String organization = field(
                ResumeDataReader.text(item, "organization"), resumeId, "certifications.organization");
            if (!organization.isBlank()) {
                line.append(" — ").append(organization);
            }
            String date = field(ResumeDataReader.text(item, "date"), resumeId, "certifications.date");
            if (!date.isBlank()) {
                line.append(" — ").append(date);
            }
            String status = statusLabel(ResumeDataReader.text(item, "status"));
            if (!status.isBlank()) {
                line.append(" — ").append(status);
            }
            doc.addBody(line.toString());
        }
        closeSection(doc, headingWritten);
    }

    private void writeCoverLetters(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        if (!reader.isSectionVisible("sec-coverLetters")) {
            return;
        }
        boolean headingWritten = false;
        for (JsonNode item : reader.objects("coverLetters")) {
            String content = ResumeDataReader.text(item, "content");
            logIfTruncated(content, resumeId, "coverLetters.content");
            List<String> paragraphs = HwpxTextSanitizer.paragraphs(content);
            if (paragraphs.isEmpty()) {
                continue;
            }
            headingWritten = writeHeadingOnce(doc, "자기소개서", headingWritten);
            String title = field(ResumeDataReader.text(item, "title"), resumeId, "coverLetters.title");
            if (!title.isBlank()) {
                doc.addBody(title);
            }
            doc.addBodyParagraphs(paragraphs);
            doc.addBlankLine();
        }
        closeSection(doc, headingWritten);
    }

    /**
     * 커스텀 섹션. 프론트와 동일하게 {@code visibleSections} 가드를 적용하지 않는다.
     */
    private void writeCustomSections(HwpxDocumentBuilder doc, ResumeDataReader reader, Long resumeId) {
        for (JsonNode section : reader.objects("customSections")) {
            String title = field(ResumeDataReader.text(section, "title"), resumeId, "customSections.title");
            if (title.isBlank()) {
                continue;
            }

            JsonNode fieldsNode = section.get("fields");
            if (fieldsNode == null || !fieldsNode.isArray()) {
                continue;
            }

            boolean headingWritten = false;
            for (JsonNode fieldNode : fieldsNode) {
                if (fieldNode == null || !fieldNode.isObject()) {
                    continue;
                }
                String label = field(ResumeDataReader.text(fieldNode, "label"), resumeId, "customSections.label");
                String value = field(ResumeDataReader.text(fieldNode, "value"), resumeId, "customSections.value");
                if (label.isBlank() && value.isBlank()) {
                    continue;
                }
                headingWritten = writeHeadingOnce(doc, title, headingWritten);
                doc.addBody(label.isBlank() ? value : label + ": " + value);
            }
            closeSection(doc, headingWritten);
        }
    }

    // ── 헬퍼 ─────────────────────────────────────────────────────────

    /**
     * 섹션 제목을 최초 1회만 쓴다. 출력할 항목이 하나도 없으면 제목도 남지 않는다.
     */
    private boolean writeHeadingOnce(HwpxDocumentBuilder doc, String heading, boolean alreadyWritten) {
        if (!alreadyWritten) {
            doc.addHeading(heading);
        }
        return true;
    }

    private void closeSection(HwpxDocumentBuilder doc, boolean headingWritten) {
        if (headingWritten) {
            doc.addBlankLine();
        }
    }

    private void addPeriod(HwpxDocumentBuilder doc, JsonNode item, Long resumeId, String fieldPath) {
        String period = field(ResumeDataReader.period(item), resumeId, fieldPath + ".period");
        if (!period.isBlank()) {
            doc.addMeta(period);
        }
    }

    private void addLongField(HwpxDocumentBuilder doc, JsonNode item, String key, String label, Long resumeId) {
        String raw = ResumeDataReader.text(item, key);
        logIfTruncated(raw, resumeId, "projects." + key);
        List<String> paragraphs = HwpxTextSanitizer.paragraphs(raw);
        if (paragraphs.isEmpty()) {
            return;
        }
        doc.addLabeled(label, paragraphs.get(0));
        for (int i = 1; i < paragraphs.size(); i++) {
            doc.addBody(paragraphs.get(i));
        }
    }

    private void addLongText(HwpxDocumentBuilder doc, String raw, Long resumeId, String fieldPath) {
        logIfTruncated(raw, resumeId, fieldPath);
        doc.addBodyParagraphs(HwpxTextSanitizer.paragraphs(raw));
    }

    /**
     * 한 줄 값 정제. 상한을 넘으면 절단 표시를 붙이고 로그를 남긴다.
     */
    private String field(String raw, Long resumeId, String fieldPath) {
        logIfTruncated(raw, resumeId, fieldPath);
        return HwpxTextSanitizer.singleLine(raw);
    }

    /**
     * 절단이 발생했다면 어떤 이력서의 어떤 필드였는지 로그로 남긴다.
     *
     * <p>문서 본문에는 {@link HwpxTextSanitizer#TRUNCATION_MARKER} 가 남고,
     * 운영자는 이 로그로 원인을 추적한다.</p>
     */
    private void logIfTruncated(String raw, Long resumeId, String fieldPath) {
        if (HwpxTextSanitizer.exceedsLimit(raw)) {
            log.warn("[ResumeHwpx] 필드 절단: resumeId={}, field={}, length={}, limit={}",
                resumeId, fieldPath, raw.length(), HwpxTextSanitizer.MAX_FIELD_LENGTH);
        }
    }

    private String joinSanitized(List<String> values) {
        return values.stream()
            .map(HwpxTextSanitizer::singleLine)
            .filter(s -> !s.isBlank())
            .reduce((a, b) -> a + ", " + b)
            .orElse("");
    }

    private String statusLabel(String status) {
        if ("ACQUIRED".equals(status)) {
            return "취득";
        }
        if ("EXPIRED".equals(status)) {
            return "만료";
        }
        return "";
    }
}
