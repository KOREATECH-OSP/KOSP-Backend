package io.swkoreatech.kosp.domain.resume.export;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

@DisplayName("ResumeHwpxExporter - 표 기반 정형 양식")
class ResumeHwpxTableLayoutTest {

    private static final ResumeProfile OWNER =
        new ResumeProfile("홍길동", "hong@koreatech.ac.kr", "https://github.com/hong");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ResumeHwpxExporter exporter = new ResumeHwpxExporter(objectMapper);

    private record Entry(String name, int method, byte[] data) {
    }

    private List<Entry> entries(byte[] hwpx) throws Exception {
        List<Entry> result = new ArrayList<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(hwpx))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                result.add(new Entry(entry.getName(), entry.getMethod(), zis.readAllBytes()));
            }
        }
        return result;
    }

    private String entry(byte[] hwpx, String name) throws Exception {
        for (Entry e : entries(hwpx)) {
            if (e.name().equals(name)) {
                return new String(e.data(), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private String section(byte[] hwpx) throws Exception {
        return entry(hwpx, "Contents/section0.xml");
    }

    private int tableCount(byte[] hwpx) throws Exception {
        return section(hwpx).split("<hp:tbl ", -1).length - 1;
    }

    /** section0.xml 의 모든 {@code <hp:t>} 텍스트를 이어 붙인다. */
    private String text(byte[] hwpx) throws Exception {
        Matcher m = Pattern.compile("<hp:t>([^<]*)</hp:t>").matcher(section(hwpx));
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            sb.append(m.group(1)).append('\n');
        }
        return sb.toString();
    }

    @Nested
    @DisplayName("구조")
    class Structure {

        @Test
        @DisplayName("전 섹션이 채워지면 머리·꼬리 포함 표 12개가 생긴다")
        void allSectionsBecomeTables() throws Exception {
            byte[] bytes = exporter.export(fullResumeJson(), 1L, OWNER);
            assertThat(tableCount(bytes)).isEqualTo(12);
        }

        @Test
        @DisplayName("header.xml 에 SOLID 테두리(borderFill 3·4·5)가 있다")
        void solidBorderFills() throws Exception {
            String header = entry(exporter.export(fullResumeJson(), 1L, OWNER), "Contents/header.xml");
            assertThat(header).contains("<hh:leftBorder type=\"SOLID\" width=\"0.12 mm\" color=\"#C9D0D8\"/>");
            assertThat(header).contains("faceColor=\"#E7ECF2\"").contains("faceColor=\"#F2F5F9\"");
        }

        @Test
        @DisplayName("mimetype 은 STORED 이고 첫 항목이며 필수 8개 항목이 모두 있다")
        void packageStructure() throws Exception {
            List<Entry> entries = entries(exporter.export(fullResumeJson(), 1L, OWNER));
            assertThat(entries.get(0).name()).isEqualTo("mimetype");
            assertThat(entries.get(0).method()).isEqualTo(ZipEntry.STORED);
            assertThat(new String(entries.get(0).data(), StandardCharsets.UTF_8)).isEqualTo("application/hwp+zip");
            assertThat(entries.stream().map(Entry::name)).contains(
                "mimetype", "version.xml", "META-INF/manifest.xml", "META-INF/container.xml",
                "Contents/content.hpf", "Contents/header.xml", "Contents/section0.xml", "settings.xml");
        }

        @Test
        @DisplayName("모든 XML 이 well-formed 다")
        void wellFormed() throws Exception {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            for (Entry e : entries(exporter.export(fullResumeJson(), 1L, OWNER))) {
                if (e.name().endsWith(".xml") || e.name().endsWith(".hpf")) {
                    factory.newDocumentBuilder().parse(new ByteArrayInputStream(e.data()));
                }
            }
        }

        @Test
        @DisplayName("table-layout=false 면 표 없이 기존 문단 방식으로 돌아간다")
        void paragraphFallback() throws Exception {
            ResumeHwpxExporter paragraphExporter = new ResumeHwpxExporter(objectMapper);
            paragraphExporter.setTableLayout(false);
            byte[] bytes = paragraphExporter.export(fullResumeJson(), 1L, OWNER);
            assertThat(tableCount(bytes)).isZero();
            assertThat(text(bytes)).startsWith("홍길동 이력서"); // 문단 방식은 이력서 제목으로 시작한다
            assertThat(entries(bytes).get(0).method()).isEqualTo(ZipEntry.STORED);
        }
    }

    @Nested
    @DisplayName("내용")
    class Content {

        @Test
        @DisplayName("머리에 이름·이메일·GitHub, 꼬리에 작성일이 들어간다 (이력서 제목이 아니라 이름으로 시작)")
        void headAndFoot() throws Exception {
            String text = text(exporter.export(fullResumeJson(), 1L, OWNER));
            assertThat(text).startsWith("홍길동\n");
            assertThat(text).contains("직무 백엔드 개발자, DevOps  |  이메일 hong@koreatech.ac.kr  |  GitHub github.com/hong");
            assertThat(text).containsPattern("\\d{4}년 \\d{1,2}월 \\d{1,2}일 · 홍길동");
            assertThat(text).doesNotContain("홍길동 이력서");
        }

        @Test
        @DisplayName("공개 다운로드처럼 이메일이 없으면 라벨과 구분자까지 빠진다")
        void contactWithoutEmail() throws Exception {
            var publicProfile = new ResumeProfile("홍길동", "", "https://github.com/hong");
            String text = text(exporter.export(fullResumeJson(), 1L, publicProfile));
            assertThat(text).contains("직무 백엔드 개발자, DevOps  |  GitHub github.com/hong");
            assertThat(text).doesNotContain("이메일");
        }

        @Test
        @DisplayName("값이 빈 프로젝트 라벨 행은 행째 빠진다")
        void emptyProjectRowsRemoved() throws Exception {
            String text = text(exporter.export(fullResumeJson(), 1L, OWNER));
            assertThat(text).contains("GitHub\nhttps://github.com/kosp");
            assertThat(text).doesNotContain("배포\n").doesNotContain("문서\n");
        }

        @Test
        @DisplayName("데이터가 0건인 섹션은 표를 만들지 않는다 — 빈 이력서는 머리·꼬리 표 2개뿐")
        void emptySectionsHaveNoTable() throws Exception {
            byte[] bytes = exporter.export("{}", 1L, OWNER);
            assertThat(tableCount(bytes)).isEqualTo(2);
            assertThat(text(bytes)).doesNotContain("학력").doesNotContain("프로젝트");
        }

        @Test
        @DisplayName("visibleSections 가 false 인 섹션은 표가 없다")
        void hiddenSection() throws Exception {
            String json = """
                {"techStack": ["Java"], "education": [{"school": "한국기술교육대"}],
                 "visibleSections": {"sec-techStack": false}}
                """;
            String text = text(exporter.export(json, 1L, OWNER));
            assertThat(text).doesNotContain("Java").contains("한국기술교육대");
        }

        @Test
        @DisplayName("커스텀 섹션은 표 양식에 포함하지 않는다 (확정된 범위)")
        void customSectionsExcluded() throws Exception {
            String text = text(exporter.export(fullResumeJson(), 1L, OWNER));
            assertThat(text).doesNotContain("취미").doesNotContain("등산");
        }

        @Test
        @DisplayName("사용자 정보가 없으면 이름 자리에 이력서 제목을 쓴다")
        void nameFallsBackToTitle() throws Exception {
            String text = text(exporter.export(fullResumeJson(), 1L, ResumeProfile.EMPTY));
            assertThat(text).startsWith("홍길동 이력서\n");
        }
    }

    private static String fullResumeJson() {
        return """
            {
              "resumeTitle": "홍길동 이력서",
              "headline": "백엔드 개발자입니다",
              "bio": "안녕하세요.\\n잘 부탁드립니다.",
              "jobRole": ["백엔드 개발자", "DevOps"],
              "techStack": ["Java", "Spring", "PostgreSQL"],
              "links": [{"id":"1","label":"GitHub","url":"https://github.com/test"},
                        {"id":"2","label":"블로그","url":"https://blog.test"}],
              "education": [{"id":"1","school":"한국기술교육대","major":"컴퓨터공학",
                             "startDate":"2020.03.02","endDate":"2026.02.20"}],
              "career": [{"id":"1","company":"테스트회사","role":"백엔드",
                          "startDate":"2026.03.01","endDate":""}],
              "experience": [{"id":"1","title":"부트캠프","description":"6개월 과정",
                              "period":"2025 하반기"}],
              "projects": [{"id":"1","name":"K-OSP","period":"2026",
                            "summary":"오픈소스 포털","role":"백엔드",
                            "techStack":["Java","Spring"],
                            "mainFeatures":"이력서 관리","myContributions":"API 설계",
                            "problemSolving":"N+1 해결","result":"응답 개선",
                            "githubLink":"https://github.com/kosp",
                            "deployLink":"","docLink":""}],
              "awards": [{"id":"1","name":"대상","organization":"교내","date":"2026.05.01",
                          "description":"수상 내역"}],
              "certifications": [{"id":"1","name":"정보처리기사","organization":"한국산업인력공단",
                                  "date":"2025.08.01","status":"ACQUIRED"}],
              "coverLetters": [{"id":"1","title":"지원동기","content":"첫 문단.\\n둘째 문단."}],
              "customSections": [{"id":"1","title":"기타",
                                  "fields":[{"id":"1","label":"취미","value":"등산"}]}],
              "isPublic": true
            }
            """;
    }
}
