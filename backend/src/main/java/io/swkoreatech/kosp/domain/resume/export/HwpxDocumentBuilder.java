package io.swkoreatech.kosp.domain.resume.export;

import java.util.List;

import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.common.ObjectList;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.CharPr;
import kr.dogfoot.hwpxlib.object.content.section_xml.SectionXMLFile;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Para;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Run;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.T;
import kr.dogfoot.hwpxlib.tool.blankfilemaker.BlankFileMaker;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;

/**
 * hwpxlib 을 감싸 문단 단위로 문서를 조립하는 빌더.
 *
 * <p>hwpxlib 에 대한 의존을 이 클래스 한 곳에 가둔다. 상위 계층
 * ({@link ResumeHwpxExporter})은 "제목을 넣는다 / 본문을 넣는다" 수준만 다룬다.</p>
 *
 * <h2>설계 메모</h2>
 * <ul>
 *   <li><b>빈 문서 기반</b> — {@code BlankFileMaker.make()} 가 만든 문서에는
 *       {@code settings.xml} · {@code version.xml} · {@code container.xml} ·
 *       {@code content.hpf} · {@code header.xml} · {@code section0.xml} 이 모두 들어 있다.
 *       패키지 구성을 직접 만들지 않는 이유는, 한컴오피스에서 열어 검증할 수단이 없기 때문이다.</li>
 *   <li><b>첫 문단 재사용</b> — 빈 문서의 첫 문단은 그 안에 {@code SecPr}(용지·여백 등
 *       구역 설정)을 품고 있어 지우면 안 된다. 문서 제목을 이 문단에 채워 재사용한다.</li>
 *   <li><b>{@code lineSegArray} 미설정</b> — 줄 배치 캐시라 선택 항목이며
 *       ({@code ParaWriter} 가 null 을 허용한다), 잘못된 값을 넣는 것보다 비워 두고
 *       한글이 직접 계산하게 하는 편이 안전하다.</li>
 *   <li><b>XML 이스케이프</b> — {@code XMLStringBuilder} 가 {@code & < >} 를 처리한다.
 *       다만 XML 금지 제어문자는 처리하지 않으므로 {@link HwpxTextSanitizer} 가 미리 제거한다.</li>
 * </ul>
 */
public class HwpxDocumentBuilder {

    // ── 빈 문서에 이미 정의된 글자모양 ID (header.xml 을 수정하지 않고 그대로 쓴다) ──
    /** 10pt 검정. 본문용. */
    private static final String CHAR_BODY = "0";
    /** 9pt. 부가정보(기간·소속 등)용. */
    private static final String CHAR_META = "2";
    /** 16pt #2E74B5. 문서 제목용. */
    private static final String CHAR_TITLE = "5";

    /** 빈 문서 첫 문단이 쓰는 문단모양 ID. 새 문단도 같은 값을 쓴다. */
    private static final String PARA_PR_DEFAULT = "3";
    private static final String STYLE_DEFAULT = "0";

    /** 굵은 글씨용으로 새로 추가하는 글자모양 ID (기존 0~6 과 겹치지 않게). */
    private static final String CHAR_HEADING = "100";

    private final HWPXFile hwpxFile;
    private final SectionXMLFile section;

    /** 문단 id 를 문서 내에서 유일하게 만들기 위한 카운터. */
    private long nextParaId = 1_000_000_000L;

    /** 첫 문단(SecPr 보유)을 아직 쓰지 않았는지 여부. */
    private boolean firstParaAvailable = true;

    public HwpxDocumentBuilder() {
        this.hwpxFile = BlankFileMaker.make();
        this.section = hwpxFile.sectionXMLFileList().get(0);
        addHeadingCharPr();
    }

    /**
     * 굵은 글씨 글자모양을 header.xml 의 글자모양 목록에 추가한다.
     *
     * <p>기존 항목을 {@code clone()} 해서 만들기 때문에 하위 요소(fontRef, ratio, spacing 등)가
     * 모두 유효한 값으로 채워진다. {@code itemCnt} 는 쓰기 시점에 목록 크기로 자동 계산되므로
     * 직접 맞출 필요가 없다.</p>
     */
    private void addHeadingCharPr() {
        ObjectList<CharPr> charProperties = hwpxFile.headerXMLFile().refList().charProperties();
        CharPr base = findCharPr(charProperties, CHAR_BODY);
        if (base == null) {
            return; // 기준 글자모양이 없으면 굵은 제목을 포기하고 본문 모양으로 대체한다.
        }
        CharPr heading = base.clone();
        heading.id(CHAR_HEADING);
        heading.height(1200);
        heading.createBold();
        charProperties.add(heading);
    }

    private CharPr findCharPr(ObjectList<CharPr> list, String id) {
        for (CharPr charPr : list.items()) {
            if (id.equals(charPr.id())) {
                return charPr;
            }
        }
        return null;
    }

    /** 문서 제목(가장 큰 글씨). 빈 문서의 첫 문단을 재사용한다. */
    public HwpxDocumentBuilder addTitle(String text) {
        writeParagraph(text, CHAR_TITLE);
        return this;
    }

    /** 섹션 제목(굵게). */
    public HwpxDocumentBuilder addHeading(String text) {
        writeParagraph(text, hasHeadingCharPr() ? CHAR_HEADING : CHAR_BODY);
        return this;
    }

    /** 본문 한 문단. */
    public HwpxDocumentBuilder addBody(String text) {
        writeParagraph(text, CHAR_BODY);
        return this;
    }

    /** 부가정보 한 문단(기간·소속 등, 작은 글씨). */
    public HwpxDocumentBuilder addMeta(String text) {
        writeParagraph(text, CHAR_META);
        return this;
    }

    /**
     * {@code 라벨: 값} 형태의 한 문단. 값이 비면 아무것도 넣지 않는다.
     */
    public HwpxDocumentBuilder addLabeled(String label, String value) {
        if (value == null || value.isBlank()) {
            return this;
        }
        return addBody(label + ": " + value);
    }

    /**
     * 장문을 줄바꿈 기준으로 나눠 여러 문단으로 넣는다.
     */
    public HwpxDocumentBuilder addBodyParagraphs(List<String> paragraphs) {
        for (String paragraph : paragraphs) {
            addBody(paragraph);
        }
        return this;
    }

    /** 섹션 사이 간격을 위한 빈 문단. */
    public HwpxDocumentBuilder addBlankLine() {
        writeParagraph("", CHAR_BODY);
        return this;
    }

    /** 지금까지 쌓인 문단 수 (테스트·검증용). */
    public int paragraphCount() {
        return section.countOfPara();
    }

    /**
     * 문서를 hwpx 바이트로 직렬화한다.
     *
     * <p>파일이나 S3 를 거치지 않고 메모리에서 바로 만든다.</p>
     *
     * @throws Exception hwpxlib 이 던지는 직렬화 오류
     */
    public byte[] toBytes() throws Exception {
        return HWPXWriter.toBytes(hwpxFile);
    }

    /**
     * 내부 문서 객체. 쓰기 전 텍스트 검증(TextExtractor)에 쓴다.
     */
    public HWPXFile hwpxFile() {
        return hwpxFile;
    }

    private boolean hasHeadingCharPr() {
        return findCharPr(hwpxFile.headerXMLFile().refList().charProperties(), CHAR_HEADING) != null;
    }

    /**
     * 문단 하나를 기록한다.
     *
     * <p>첫 호출은 빈 문서가 이미 갖고 있는 문단(SecPr 보유)에 텍스트를 채우고,
     * 이후 호출은 새 문단을 추가한다.</p>
     */
    private void writeParagraph(String text, String charPrId) {
        if (firstParaAvailable) {
            firstParaAvailable = false;
            fillExistingFirstPara(text, charPrId);
            return;
        }

        Para para = section.addNewPara();
        para.idAnd(String.valueOf(nextParaId++))
            .paraPrIDRefAnd(PARA_PR_DEFAULT)
            .styleIDRefAnd(STYLE_DEFAULT)
            .pageBreakAnd(false)
            .columnBreakAnd(false)
            .merged(false);

        Run run = para.addNewRun();
        run.charPrIDRef(charPrId);
        T t = run.addNewT();
        if (text != null && !text.isEmpty()) {
            t.addText(text);
        }
    }

    /**
     * 빈 문서의 첫 문단에 텍스트를 채운다.
     *
     * <p>이 문단의 첫 Run 에는 {@code SecPr}(용지·여백·머리말 설정)과 단 설정이 들어 있어
     * 새로 만들면 구역 설정을 잃는다. 그래서 이미 있는 빈 {@code T} 에 텍스트만 넣는다.</p>
     */
    private void fillExistingFirstPara(String text, String charPrId) {
        if (section.countOfPara() == 0) {
            // 빈 문서 구조가 예상과 다르면 일반 문단 추가로 대체한다.
            firstParaAvailable = false;
            writeParagraph(text, charPrId);
            return;
        }

        Para para = section.getPara(0);
        Run run = firstRunOf(para);
        if (run == null) {
            run = para.addNewRun();
        }
        run.charPrIDRef(charPrId);

        T t = firstTextOf(run);
        if (t == null) {
            t = run.addNewT();
        }
        if (text != null && !text.isEmpty()) {
            t.addText(text);
        }
    }

    private Run firstRunOf(Para para) {
        for (Run run : para.runs()) {
            return run;
        }
        return null;
    }

    private T firstTextOf(Run run) {
        for (var item : run.runItems()) {
            if (item instanceof T text) {
                return text;
            }
        }
        return null;
    }
}
