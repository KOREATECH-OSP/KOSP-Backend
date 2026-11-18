package io.swkoreatech.kosp.domain.resume.export;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import kr.dogfoot.hwpxlib.object.HWPXFile;
import kr.dogfoot.hwpxlib.object.common.ObjectList;
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.HorizontalAlign2;
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.LineType2;
import kr.dogfoot.hwpxlib.object.content.header_xml.enumtype.LineWidth;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.BorderFill;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.CharPr;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.ParaPr;
import kr.dogfoot.hwpxlib.object.content.header_xml.references.borderfill.Border;
import kr.dogfoot.hwpxlib.object.content.section_xml.SectionXMLFile;
import kr.dogfoot.hwpxlib.object.content.section_xml.SubList;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.DropCapStyle;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.HeightRelTo;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.HorzAlign;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.HorzRelTo;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.LineWrapMethod;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.NumberingType;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.TablePageBreak;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.TextDirection;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.TextFlowSide;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.TextWrapMethod;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.VertAlign;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.VertRelTo;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.VerticalAlign2;
import kr.dogfoot.hwpxlib.object.content.section_xml.enumtype.WidthRelTo;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Para;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.Run;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.T;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.Table;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.Tc;
import kr.dogfoot.hwpxlib.object.content.section_xml.paragraph.object.table.Tr;
import kr.dogfoot.hwpxlib.tool.blankfilemaker.BlankFileMaker;
import kr.dogfoot.hwpxlib.writer.HWPXWriter;

/**
 * hwpxlib 을 감싸 문단·표 단위로 문서를 조립하는 빌더.
 *
 * <p>hwpxlib 에 대한 의존을 이 클래스 한 곳에 가둔다. 상위 계층
 * ({@link ResumeHwpxExporter}, {@link ResumeHwpxTableLayout})은 "제목을 넣는다 / 표를 넣는다"
 * 수준만 다룬다.</p>
 *
 * <h2>설계 메모</h2>
 * <ul>
 *   <li><b>빈 문서 기반</b> — {@code BlankFileMaker.make()} 가 만든 문서에서 시작한다.
 *       패키지 구성(manifest·content.hpf 등)은 직접 만들지 않는다.</li>
 *   <li><b>header.xml 확장</b> — 표에 필요한 테두리(borderFill)·글자모양(charPr)·문단모양(paraPr)은
 *       빈 문서의 기존 항목을 {@code clone()} 해서 필요한 속성만 바꿔 추가한다. 기존 항목은 수정하지 않는다.
 *       {@code itemCnt} 는 쓰기 시점에 목록 크기로 자동 계산된다.</li>
 *   <li><b>첫 문단 재사용</b> — 빈 문서의 첫 문단은 그 안에 {@code SecPr}(용지·여백 등
 *       구역 설정)을 품고 있어 지우면 안 된다. 첫 내용(제목 문단 또는 머리 표)을 이 문단에 넣는다.</li>
 *   <li><b>{@code lineSegArray} 미설정</b> — 줄 배치 캐시라 선택 항목이며, 비워 두고
 *       한글이 직접 계산하게 한다. 표 셀 안 문단도 같다.</li>
 *   <li><b>XML 이스케이프</b> — {@code XMLStringBuilder} 가 {@code & < >} 를 처리한다.
 *       XML 금지 제어문자는 {@link HwpxTextSanitizer} 가 미리 제거한다.</li>
 *   <li><b>mimetype</b> — {@code HWPXWriter} 는 mimetype 을 DEFLATE 로 쓴다. OCF 규격(STORED·첫 항목)에
 *       맞도록 {@link #toBytes()} 에서 zip 을 다시 쓴다.</li>
 * </ul>
 */
public class HwpxDocumentBuilder {

    // ── 빈 문서에 이미 정의된 글자모양 ID ──
    /** 10pt 검정. 본문용. */
    private static final String CHAR_BODY = "0";
    /** 9pt. 부가정보(기간·소속 등)용. */
    private static final String CHAR_META = "2";
    /** 16pt #2E74B5. 문서 제목용. */
    private static final String CHAR_TITLE = "5";

    /** 빈 문서 첫 문단이 쓰는 문단모양 ID (양쪽 정렬). 새 문단도 같은 값을 쓴다. */
    private static final String PARA_PR_DEFAULT = "3";
    private static final String STYLE_DEFAULT = "0";

    /** 문단 방식의 섹션 제목(12pt 굵게). */
    private static final String CHAR_HEADING = "100";

    // ── 테두리/배경 (빈 문서의 1·2 는 선이 전부 NONE 이다) ──
    /** 빈 문서 기본 테두리(선 없음, 배경 없음). 테두리 없는 표와 복제 원본으로 쓴다. */
    private static final String BORDER_FILL_BLANK = "1";
    /** 빈 문서 기본 테두리(배경 브러시 있음). 배경 있는 칸의 복제 원본. */
    private static final String BORDER_FILL_BLANK_WITH_BRUSH = "2";
    /** 표 칸: 상하좌우 SOLID 0.12mm #C9D0D8. */
    static final String BORDER_FILL_CELL = "3";
    /** 섹션 제목 행: 표 칸 테두리 + 배경 #E7ECF2. */
    static final String BORDER_FILL_SECTION_HEADER = "4";
    /** 프로젝트 이름 행: 표 칸 테두리 + 배경 #F2F5F9. */
    static final String BORDER_FILL_PROJECT_HEADER = "5";

    private static final String TABLE_LINE_COLOR = "#C9D0D8";

    // ── 추가 문단모양 ──
    private static final String PARA_PR_CENTER = "100";
    private static final String PARA_PR_RIGHT = "101";
    private static final String PARA_PR_LEFT = "102";

    // ── 표 치수 (HWPUNIT, 1mm ≈ 283.46) ──
    /** 표 전체 폭 180mm. A4(210mm) - 좌우 여백 15mm×2. */
    public static final long TABLE_WIDTH = 51024;
    /** 기본 행 최소 높이. 내용이 길면 한글이 늘린다. */
    public static final long ROW_HEIGHT = 1700;
    private static final long CELL_MARGIN_LR = 510;
    private static final long CELL_MARGIN_TB = 141;
    /** 표 아래 간격 2.5mm (PDF 양식의 표 사이 간격). */
    private static final long TABLE_GAP_BOTTOM = 709;
    /** 표 양식일 때 좌우 용지 여백 15mm. */
    private static final int SIDE_MARGIN_FOR_TABLE = 4252;

    /** 표 안 글자 모양. 각 값은 header.xml 에 추가되는 charPr 하나에 대응한다. */
    public enum TextStyle {
        /** 10pt. */
        BODY(CHAR_BODY, 0, false, null),
        /** 9pt (기간 칸). */
        META(CHAR_META, 0, false, null),
        SECTION_TITLE("101", 1050, true, null),
        CELL_BOLD("102", 1000, true, null),
        NAME("103", 2100, true, null),
        TAGLINE("104", 1050, false, "#333333"),
        CONTACT("105", 900, false, "#555555"),
        PHOTO("106", 800, false, "#9AA4B0"),
        PROJECT_NAME("107", 1020, true, null),
        PROJECT_PERIOD("108", 880, false, "#555555"),
        LABEL("109", 920, true, "#444444"),
        LONG_BODY("110", 920, false, null),
        LETTER_TITLE("111", 980, true, null),
        FOOTER("112", 850, false, "#777777");

        private final String id;
        private final int height;
        private final boolean bold;
        private final String color;

        TextStyle(String id, int height, boolean bold, String color) {
            this.id = id;
            this.height = height;
            this.bold = bold;
            this.color = color;
        }

        /** 빈 문서에 이미 있는 글자모양이면 true (새로 추가하지 않는다). */
        private boolean builtIn() {
            return height == 0;
        }
    }

    /** 칸 안 가로 정렬. */
    public enum Align {
        LEFT(PARA_PR_LEFT), CENTER(PARA_PR_CENTER), RIGHT(PARA_PR_RIGHT), JUSTIFY(PARA_PR_DEFAULT);

        private final String paraPrId;

        Align(String paraPrId) {
            this.paraPrId = paraPrId;
        }
    }

    /** 칸 테두리/배경. */
    public enum CellBorder {
        NONE(BORDER_FILL_BLANK),
        LINE(BORDER_FILL_CELL),
        SECTION_HEADER(BORDER_FILL_SECTION_HEADER),
        PROJECT_HEADER(BORDER_FILL_PROJECT_HEADER);

        private final String borderFillId;

        CellBorder(String borderFillId) {
            this.borderFillId = borderFillId;
        }
    }

    /** 칸 세로 정렬. */
    public enum VAlign {
        TOP(VerticalAlign2.TOP), CENTER(VerticalAlign2.CENTER);

        private final VerticalAlign2 value;

        VAlign(VerticalAlign2 value) {
            this.value = value;
        }
    }

    /** 칸 안의 한 문단. */
    public record Line(String text, TextStyle style) {
    }

    /** 표의 한 칸. {@code lines} 가 비면 빈 문단 하나를 넣는다. */
    public record Cell(int colSpan, CellBorder border, Align align, VAlign vAlign, List<Line> lines) {
    }

    /** 표의 한 행. {@code minHeight} 는 최소 높이(HWPUNIT). */
    public record Row(long minHeight, List<Cell> cells) {
    }

    private final HWPXFile hwpxFile;
    private final SectionXMLFile section;

    /** 문단·표 id 를 문서 내에서 유일하게 만들기 위한 카운터. */
    private long nextParaId = 1_000_000_000L;

    /** 첫 문단(SecPr 보유)을 아직 쓰지 않았는지 여부. */
    private boolean firstParaAvailable = true;

    public HwpxDocumentBuilder() {
        this.hwpxFile = BlankFileMaker.make();
        this.section = hwpxFile.sectionXMLFileList().get(0);
        addHeadingCharPr();
        addTableBorderFills();
        addTableCharPrs();
        addAlignParaPrs();
    }

    // ── header.xml 확장 ───────────────────────────────────────────────

    /** 문단 방식의 섹션 제목(12pt 굵게) 글자모양을 추가한다. */
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

    /** {@link TextStyle} 의 글자모양을 본문 모양(0)을 clone 해서 추가한다. */
    private void addTableCharPrs() {
        ObjectList<CharPr> charProperties = hwpxFile.headerXMLFile().refList().charProperties();
        CharPr base = findCharPr(charProperties, CHAR_BODY);
        if (base == null) {
            return;
        }
        for (TextStyle style : TextStyle.values()) {
            if (style.builtIn()) {
                continue;
            }
            CharPr charPr = base.clone();
            charPr.id(style.id);
            charPr.height(style.height);
            if (style.bold) {
                charPr.createBold();
            }
            if (style.color != null) {
                charPr.textColor(style.color);
            }
            charProperties.add(charPr);
        }
    }

    /** 기본 문단모양(3, 양쪽 정렬)을 clone 해 가로 정렬만 바꾼 문단모양 3개를 추가한다. */
    private void addAlignParaPrs() {
        ObjectList<ParaPr> paraProperties = hwpxFile.headerXMLFile().refList().paraProperties();
        ParaPr base = null;
        for (ParaPr paraPr : paraProperties.items()) {
            if (PARA_PR_DEFAULT.equals(paraPr.id())) {
                base = paraPr;
            }
        }
        if (base == null || base.align() == null) {
            return;
        }
        paraProperties.add(alignedCopy(base, PARA_PR_CENTER, HorizontalAlign2.CENTER));
        paraProperties.add(alignedCopy(base, PARA_PR_RIGHT, HorizontalAlign2.RIGHT));
        paraProperties.add(alignedCopy(base, PARA_PR_LEFT, HorizontalAlign2.LEFT));
    }

    private ParaPr alignedCopy(ParaPr base, String id, HorizontalAlign2 horizontal) {
        ParaPr copy = base.clone();
        copy.id(id);
        copy.align().horizontal(horizontal);
        return copy;
    }

    /**
     * 표에 쓸 테두리/배경 정의를 header.xml 의 borderFill 목록에 추가한다.
     *
     * <p>빈 문서의 기존 정의(1, 2)를 {@code clone()} 해서 테두리 4면과 배경만 바꾼다.
     * slash·backSlash·diagonal 등 나머지 하위 요소는 원본 값을 그대로 물려받는다.</p>
     */
    private void addTableBorderFills() {
        ObjectList<BorderFill> borderFills = hwpxFile.headerXMLFile().refList().borderFills();
        BorderFill blank = findBorderFill(borderFills, BORDER_FILL_BLANK);
        BorderFill blankWithBrush = findBorderFill(borderFills, BORDER_FILL_BLANK_WITH_BRUSH);
        if (blank == null || blankWithBrush == null) {
            return; // 빈 문서 구조가 예상과 다르면 표 테두리를 포기한다 (문단 출력에는 영향 없음).
        }

        BorderFill cell = blank.clone();
        cell.id(BORDER_FILL_CELL);
        applySolidBorders(cell);
        borderFills.add(cell);

        borderFills.add(withBackground(blankWithBrush, BORDER_FILL_SECTION_HEADER, "#E7ECF2"));
        borderFills.add(withBackground(blankWithBrush, BORDER_FILL_PROJECT_HEADER, "#F2F5F9"));
    }

    private BorderFill withBackground(BorderFill base, String id, String color) {
        BorderFill borderFill = base.clone();
        borderFill.id(id);
        applySolidBorders(borderFill);
        if (borderFill.fillBrush() == null) {
            borderFill.createFillBrush();
        }
        if (borderFill.fillBrush().winBrush() == null) {
            borderFill.fillBrush().createWinBrush();
        }
        borderFill.fillBrush().winBrush().faceColor(color);
        return borderFill;
    }

    private void applySolidBorders(BorderFill borderFill) {
        for (Border border : List.of(
            borderFill.leftBorder(), borderFill.rightBorder(),
            borderFill.topBorder(), borderFill.bottomBorder())) {
            border.type(LineType2.SOLID);
            border.width(LineWidth.MM_0_12);
            border.color(TABLE_LINE_COLOR);
        }
    }

    private BorderFill findBorderFill(ObjectList<BorderFill> list, String id) {
        for (BorderFill borderFill : list.items()) {
            if (id.equals(borderFill.id())) {
                return borderFill;
            }
        }
        return null;
    }

    private CharPr findCharPr(ObjectList<CharPr> list, String id) {
        for (CharPr charPr : list.items()) {
            if (id.equals(charPr.id())) {
                return charPr;
            }
        }
        return null;
    }

    // ── 문단 방식 API ─────────────────────────────────────────────────

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

    /** {@code 라벨: 값} 형태의 한 문단. 값이 비면 아무것도 넣지 않는다. */
    public HwpxDocumentBuilder addLabeled(String label, String value) {
        if (value == null || value.isBlank()) {
            return this;
        }
        return addBody(label + ": " + value);
    }

    /** 장문을 줄바꿈 기준으로 나눠 여러 문단으로 넣는다. */
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

    // ── 표 API ────────────────────────────────────────────────────────

    /**
     * 좌우 용지 여백을 15mm 로 줄인다.
     *
     * <p>빈 문서 기본 여백은 좌우 30mm 라 본문 폭이 150mm 다. 표 폭 180mm(PDF 양식과 동일)를
     * 쓰려면 여백을 15mm 로 맞춰야 표가 오른쪽 여백을 넘지 않는다.
     * 구역 설정(SecPr)은 첫 문단의 첫 Run 에 있다.</p>
     */
    public HwpxDocumentBuilder useTableMargins() {
        if (section.countOfPara() == 0) {
            return this;
        }
        Run run = firstRunOf(section.getPara(0));
        if (run == null || run.secPr() == null || run.secPr().pagePr() == null
            || run.secPr().pagePr().margin() == null) {
            return this;
        }
        run.secPr().pagePr().margin().left(SIDE_MARGIN_FOR_TABLE);
        run.secPr().pagePr().margin().right(SIDE_MARGIN_FOR_TABLE);
        return this;
    }

    /** 기간·이름·설명 3열 표의 데이터 한 행. */
    public record PeriodRow(String period, String name, List<String> detail) {
        public PeriodRow(String period, String name, String detail) {
            this(period, name, detail == null || detail.isEmpty() ? List.of() : List.of(detail));
        }
    }

    /**
     * 섹션 제목 행 + 3열(기간 34mm / 이름 46mm / 나머지) 데이터 행으로 된 표를 넣는다.
     * 행이 없으면 아무것도 넣지 않는다.
     */
    public HwpxDocumentBuilder addPeriodTable(String title, List<PeriodRow> rows) {
        if (rows == null || rows.isEmpty()) {
            return this;
        }
        long[] widths = {mm(34), mm(46), TABLE_WIDTH - mm(34) - mm(46)};
        List<Row> tableRows = new ArrayList<>();
        tableRows.add(sectionTitleRow(title, widths.length));
        for (PeriodRow row : rows) {
            List<Line> detail = new ArrayList<>();
            for (String paragraph : row.detail()) {
                detail.add(new Line(paragraph, TextStyle.BODY));
            }
            tableRows.add(new Row(ROW_HEIGHT, List.of(
                new Cell(1, CellBorder.LINE, Align.CENTER, VAlign.CENTER, List.of(new Line(row.period(), TextStyle.META))),
                new Cell(1, CellBorder.LINE, Align.LEFT, VAlign.CENTER, List.of(new Line(row.name(), TextStyle.CELL_BOLD))),
                new Cell(1, CellBorder.LINE, Align.LEFT, VAlign.CENTER, detail))));
        }
        return addTable(widths, tableRows, CellBorder.LINE);
    }

    /** 섹션 제목 행(배경 회색, 전체 colspan, 10.5pt 굵게). */
    public static Row sectionTitleRow(String title, int colSpan) {
        return new Row(ROW_HEIGHT, List.of(new Cell(colSpan, CellBorder.SECTION_HEADER, Align.LEFT, VAlign.CENTER,
            List.of(new Line(title, TextStyle.SECTION_TITLE)))));
    }

    /** 밀리미터를 HWPUNIT 으로 바꾼다. */
    public static long mm(double millimeters) {
        return Math.round(millimeters * 7200 / 25.4);
    }

    /**
     * 표를 하나 넣는다.
     *
     * @param colWidths 열 폭(HWPUNIT). 합이 표 폭이 된다.
     * @param rows      행 목록. 각 행의 colSpan 합은 열 수와 같아야 한다.
     * @param tableBorder 표 자체의 테두리 (셀 테두리와 별개, 테두리 없는 표는 {@link CellBorder#NONE})
     */
    public HwpxDocumentBuilder addTable(long[] colWidths, List<Row> rows, CellBorder tableBorder) {
        if (rows == null || rows.isEmpty()) {
            return this;
        }
        long tableWidth = 0;
        for (long width : colWidths) {
            tableWidth += width;
        }
        long tableHeight = 0;
        for (Row row : rows) {
            tableHeight += row.minHeight();
        }

        Table table = newTable(rows.size(), colWidths.length, tableWidth, tableHeight, tableBorder);
        for (int r = 0; r < rows.size(); r++) {
            Row row = rows.get(r);
            Tr tr = table.addNewTr();
            int col = 0;
            for (Cell cell : row.cells()) {
                long width = 0;
                for (int c = col; c < col + cell.colSpan() && c < colWidths.length; c++) {
                    width += colWidths[c];
                }
                addCell(tr, col, r, cell, width, row.minHeight());
                col += cell.colSpan();
            }
        }
        return this;
    }

    /**
     * 표를 담을 문단과 표 객체를 만든다. 표는 글자처럼 취급(treatAsChar)해 문단 흐름을 따라간다.
     * 속성 값은 한글이 저장한 표의 기본값을 따른다.
     */
    private Table newTable(int rowCount, int colCount, long width, long height, CellBorder tableBorder) {
        Run run = newRunForObject();

        Table table = run.addNewTable();
        table.id(String.valueOf(nextParaId++));
        table.zOrder(0);
        table.numberingType(NumberingType.TABLE);
        table.textWrap(TextWrapMethod.TOP_AND_BOTTOM);
        table.textFlow(TextFlowSide.BOTH_SIDES);
        table.lock(false);
        table.dropcapstyle(DropCapStyle.None);
        table.pageBreak(TablePageBreak.CELL);
        table.repeatHeader(false);
        table.rowCnt((short) rowCount);
        table.colCnt((short) colCount);
        table.cellSpacing(0);
        table.borderFillIDRef(tableBorder.borderFillId);
        table.noAdjust(false);

        table.createSZ();
        table.sz().width(width);
        table.sz().widthRelTo(WidthRelTo.ABSOLUTE);
        table.sz().height(height);
        table.sz().heightRelTo(HeightRelTo.ABSOLUTE);
        table.sz().protect(false);

        table.createPos();
        table.pos().treatAsChar(true);
        table.pos().affectLSpacing(false);
        table.pos().flowWithText(true);
        table.pos().allowOverlap(false);
        table.pos().holdAnchorAndSO(false);
        table.pos().vertRelTo(VertRelTo.PARA);
        table.pos().horzRelTo(HorzRelTo.COLUMN);
        table.pos().vertAlign(VertAlign.TOP);
        table.pos().horzAlign(HorzAlign.LEFT);
        table.pos().vertOffset(0L);
        table.pos().horzOffset(0L);

        // 좌우 바깥 여백 0: 표 폭(180mm)이 본문 폭과 같아서 여백을 주면 오른쪽으로 넘친다.
        table.createOutMargin();
        table.outMargin().set(0L, 0L, 0L, TABLE_GAP_BOTTOM);
        table.createInMargin();
        table.inMargin().set(CELL_MARGIN_LR, CELL_MARGIN_LR, CELL_MARGIN_TB, CELL_MARGIN_TB);

        run.addNewT(); // 한글은 표 뒤에 빈 텍스트 요소를 둔다.
        return table;
    }

    /**
     * 표를 넣을 Run 을 만든다. 첫 문단(SecPr 보유)이 아직 비어 있으면 그 문단에 Run 을 덧붙여
     * 문서 맨 위에 빈 줄이 생기지 않게 한다. SecPr 을 가진 기존 Run 은 건드리지 않는다.
     */
    private Run newRunForObject() {
        Para para;
        if (firstParaAvailable && section.countOfPara() > 0) {
            firstParaAvailable = false;
            para = section.getPara(0);
        } else {
            firstParaAvailable = false;
            para = newParagraph();
        }
        Run run = para.addNewRun();
        run.charPrIDRef(CHAR_BODY);
        return run;
    }

    private void addCell(Tr tr, int colAddr, int rowAddr, Cell cell, long width, long height) {
        Tc tc = tr.addNewTc();
        tc.name("");
        tc.header(false);
        tc.hasMargin(false);
        tc.protect(false);
        tc.editable(false);
        tc.dirty(false);
        tc.borderFillIDRef(cell.border().borderFillId);

        tc.createSubList();
        SubList subList = tc.subList();
        subList.id("");
        subList.textDirection(TextDirection.HORIZONTAL);
        subList.lineWrap(LineWrapMethod.BREAK);
        subList.vertAlign(cell.vAlign().value);
        subList.linkListIDRef("0");
        subList.linkListNextIDRef("0");
        subList.textWidth(0);
        subList.textHeight(0);
        subList.hasTextRef(false);
        subList.hasNumRef(false);

        List<Line> lines = cell.lines().isEmpty() ? List.of(new Line("", TextStyle.BODY)) : cell.lines();
        for (Line line : lines) {
            Para para = subList.addNewPara();
            para.idAnd("0")
                .paraPrIDRefAnd(cell.align().paraPrId)
                .styleIDRefAnd(STYLE_DEFAULT)
                .pageBreakAnd(false)
                .columnBreakAnd(false)
                .merged(false);
            Run run = para.addNewRun();
            run.charPrIDRef(line.style().id);
            T t = run.addNewT();
            if (line.text() != null && !line.text().isEmpty()) {
                t.addText(line.text());
            }
        }

        tc.createCellAddr();
        tc.cellAddr().colAddr((short) colAddr);
        tc.cellAddr().rowAddr((short) rowAddr);
        tc.createCellSpan();
        tc.cellSpan().colSpan((short) cell.colSpan());
        tc.cellSpan().rowSpan((short) 1);
        tc.createCellSz();
        tc.cellSz().width(width);
        tc.cellSz().height(height);
        tc.createCellMargin();
        tc.cellMargin().set(CELL_MARGIN_LR, CELL_MARGIN_LR, CELL_MARGIN_TB, CELL_MARGIN_TB);
    }

    // ── 직렬화 ────────────────────────────────────────────────────────

    /** 지금까지 쌓인 최상위 문단 수 (테스트·검증용). */
    public int paragraphCount() {
        return section.countOfPara();
    }

    /**
     * 문서를 hwpx 바이트로 직렬화한다. mimetype 을 STORED·첫 항목으로 맞춘다.
     *
     * @throws Exception hwpxlib 이 던지는 직렬화 오류
     */
    public byte[] toBytes() throws Exception {
        return normalizeMimetype(HWPXWriter.toBytes(hwpxFile));
    }

    /** 내부 문서 객체. 쓰기 전 텍스트 검증(TextExtractor)에 쓴다. */
    public HWPXFile hwpxFile() {
        return hwpxFile;
    }

    /**
     * zip 을 다시 써서 {@code mimetype} 을 무압축(STORED) 첫 항목으로 만든다.
     *
     * <p>OCF 규격은 mimetype 을 압축하지 않은 첫 항목으로 요구한다. {@code HWPXWriter} 는
     * {@code new ZipEntry} 기본값(DEFLATED)으로 쓰므로 그대로 두면 규격에 어긋난다.
     * 나머지 항목은 원래 순서대로 DEFLATED 로 다시 쓴다.</p>
     */
    static byte[] normalizeMimetype(byte[] zip) throws IOException {
        byte[] mimetype = null;
        List<String> names = new ArrayList<>();
        List<byte[]> contents = new ArrayList<>();
        try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
            ZipEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                byte[] data = in.readAllBytes();
                if ("mimetype".equals(entry.getName())) {
                    mimetype = data;
                } else {
                    names.add(entry.getName());
                    contents.add(data);
                }
            }
        }
        if (mimetype == null) {
            return zip; // mimetype 이 없으면 손대지 않는다 (상위 검증에서 걸러진다).
        }

        ByteArrayOutputStream bytes = new ByteArrayOutputStream(zip.length + 256);
        try (ZipOutputStream out = new ZipOutputStream(bytes)) {
            ZipEntry mimeEntry = new ZipEntry("mimetype");
            mimeEntry.setMethod(ZipEntry.STORED);
            mimeEntry.setSize(mimetype.length);
            mimeEntry.setCompressedSize(mimetype.length);
            CRC32 crc = new CRC32();
            crc.update(mimetype);
            mimeEntry.setCrc(crc.getValue());
            out.putNextEntry(mimeEntry);
            out.write(mimetype);
            out.closeEntry();

            for (int i = 0; i < names.size(); i++) {
                ZipEntry entry = new ZipEntry(names.get(i));
                entry.setMethod(ZipEntry.DEFLATED);
                out.putNextEntry(entry);
                out.write(contents.get(i));
                out.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    // ── 내부 ─────────────────────────────────────────────────────────

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

        Para para = newParagraph();
        Run run = para.addNewRun();
        run.charPrIDRef(charPrId);
        T t = run.addNewT();
        if (text != null && !text.isEmpty()) {
            t.addText(text);
        }
    }

    /** 본문에 기본 문단모양의 새 문단을 추가한다 (Run 은 호출 측에서 붙인다). */
    private Para newParagraph() {
        Para para = section.addNewPara();
        para.idAnd(String.valueOf(nextParaId++))
            .paraPrIDRefAnd(PARA_PR_DEFAULT)
            .styleIDRefAnd(STYLE_DEFAULT)
            .pageBreakAnd(false)
            .columnBreakAnd(false)
            .merged(false);
        return para;
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
