package iuh.fit.backend.Event.service;

import iuh.fit.backend.Event.Entity.Event;
import iuh.fit.backend.Event.Entity.EventOrganizer;
import iuh.fit.backend.Event.Entity.EventParticipant;
import iuh.fit.backend.Event.dto.response.AttendeeResponse;
import iuh.fit.backend.identity.entity.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class EventExportService {

    private static final String FONT_FAMILY = "Times New Roman";
    private static final String TEMP_FILE_PREFIX = "seminar_plan_";

    public Resource exportEventToWordFile(Event event) throws IOException {
        XWPFDocument document = createWordDocument(event);
        File wordFile = createTempFile();

        try (FileOutputStream out = new FileOutputStream(wordFile)) {
            document.write(out);
        } finally {
            document.close();
        }

        return new FileSystemResource(wordFile);
    }

    private XWPFDocument createWordDocument(Event event) {
        XWPFDocument document = new XWPFDocument();

        // Header với thông tin trường và khoa
        createHeader(document);

        // Ngày tháng
        createDateParagraph(document, event.getTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        // Tiêu đề "KẾ HOẠCH" và chủ đề seminar
        createTitleSection(document, event.getName());


        // Mục đích
        createPurposeSection(document, event.getPurpose());

        // Thời gian và địa điểm
        createTimeLocationSection(document,
                event.getTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                event.getLocation());

        // Đối tượng tham gia
        createParticipantsSection(document);

        // Nội dung chương trình
        createAgendaSection(document, event.getContent());

        // Ban tổ chức
        createOrganizersSection(document, event.getOrganizers());

        // Người tham dự
        createParticipantsSection(document, event.getParticipants());

        // Thành phần tham dự chi tiết
//        createDetailedParticipantsSection(document, event.getAttendees(), event.getParticipants());
        createFooter(document);
        return document;
    }

    private void createHeader(XWPFDocument document) {
        // Tạo đoạn văn cho phần header
        // Dòng 1: Tên trường và quốc hiệu
        XWPFParagraph line1 = document.createParagraph();
        line1.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun run1 = line1.createRun();
        XWPFRun run12 = line1.createRun();
        run1.setText("TRƯỜNG ĐẠI HỌC CÔNG NGHIỆP TP. HCM");
        run1.setFontFamily(FONT_FAMILY);
        run1.setFontSize(10);
        run1.addTab();
        run12.setFontSize(11);
        run12.setFontFamily(FONT_FAMILY);
        run12.setText("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM");
        run12.setBold(true);

        // Dòng 2: Khoa và khẩu hiệu
        XWPFParagraph line2 = document.createParagraph();
        line2.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun run2 = line2.createRun();
        XWPFRun run22 = line2.createRun();

        run2.setText("         KHOA CÔNG NGHỆ THÔNG TIN");
        run2.setFontFamily(FONT_FAMILY);
        run2.setFontSize(10);
        run2.addTab();
        run2.addTab();
        run22.setFontFamily(FONT_FAMILY);
        run22.setFontSize(11);
        run22.addTab();
        run22.setText("Độc lập - Tự Do - Hạnh Phúc");
        run22.setItalic(true);
        run22.setBold(true);

        // Dòng 3: Bộ môn
        XWPFParagraph line3 = document.createParagraph();
        line3.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun run3 = line3.createRun();
        run3.setText("       BỘ MÔN KỸ THUẬT PHẦN MỀM");
        run3.setBold(true);
        run3.setFontFamily(FONT_FAMILY);
        run3.setFontSize(10);
        run3.addTab();
        run3.addTab();
        run3.addTab();
        run3.addTab();
        run3.setText("   ---------");

        // Dòng kẻ ngang
        XWPFParagraph line4 = document.createParagraph();
        XWPFRun run4 = line4.createRun();
        run4.addTab();
        run4.addTab();
        run4.setText("---------");
        run4.setFontFamily(FONT_FAMILY);
//        addEmptyLine(run4, 2);
        run4.setBold(false);

    }
    private void createFooter(XWPFDocument document) {
        XWPFParagraph lineFooter = document.createParagraph();
        lineFooter.setAlignment(ParagraphAlignment.LEFT);

        XWPFRun run1 = lineFooter.createRun();
        addEmptyLine(run1, 1);
        run1.setText("Khoa CNTT duyệt");
        run1.setFontFamily(FONT_FAMILY);
        run1.setFontSize(12);
        run1.addTab();
        run1.addTab();
        run1.addTab();
        run1.addTab();
        run1.addTab();
        run1.addTab();
        run1.addTab();
        run1.addTab();
        run1.setText("Người lập kế hoạch ");
        run1.setBold(true);



    }


    private void createTitleSection(XWPFDocument document, String title) {
        XWPFParagraph titlePara = document.createParagraph();
        titlePara.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun titleRun = titlePara.createRun();
        titleRun.setText("KẾ HOẠCH");
        titleRun.setBold(true);
        titleRun.setFontSize(16);
        titleRun.setFontFamily(FONT_FAMILY);

        addEmptyLine(titleRun, 1);

        XWPFParagraph topicPara = document.createParagraph();
        topicPara.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun topicRun = topicPara.createRun();
        topicRun.setText("V/v: Seminar chuyên đề \"" + title + "\"");
        topicRun.setItalic(true);
        topicRun.setFontSize(12);
        topicRun.setFontFamily(FONT_FAMILY);

        addEmptyLine(topicRun, 2);
    }

    private void createDateParagraph(XWPFDocument document, String dateStr) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.RIGHT);

        // Chuyển đổi từ chuỗi dd/MM/yyyy sang định dạng tiếng Việt
        String[] dateParts = dateStr.split("/");
        String vietnameseDate = String.format("ngày %s tháng %s năm %s",
                dateParts[0], dateParts[1], dateParts[2]);

        XWPFRun run = paragraph.createRun();
        run.setText("Thành phố Hồ Chí Minh, " + vietnameseDate);
        run.setFontFamily(FONT_FAMILY);

        addEmptyLine(run, 2);
    }

    private void createPurposeSection(XWPFDocument document, String purpose) {
        XWPFParagraph sectionPara = document.createParagraph();

        XWPFRun sectionRun = sectionPara.createRun();
        sectionRun.setText("1. Mục đích");
        sectionRun.setBold(true);
        sectionRun.setFontFamily(FONT_FAMILY);

//        addEmptyLine(sectionRun, 1);

        XWPFParagraph contentPara = document.createParagraph();
        contentPara.setIndentationLeft(400);

        XWPFRun contentRun = contentPara.createRun();
        contentRun.setText(purpose);
        contentRun.setFontFamily(FONT_FAMILY);

//        addEmptyLine(contentRun, 2);
    }

    private void createTimeLocationSection(XWPFDocument document, String time, String location) {
        XWPFParagraph sectionPara = document.createParagraph();

        XWPFRun sectionRun = sectionPara.createRun();
        sectionRun.setText("2. Thời gian và địa điểm");
        sectionRun.setBold(true);
        sectionRun.setFontFamily(FONT_FAMILY);

//        addEmptyLine(sectionRun, 1);

        XWPFParagraph timePara = document.createParagraph();
        timePara.setIndentationLeft(400);

        XWPFRun timeRun = timePara.createRun();
        timeRun.setText("- Thời gian: " + time);
        timeRun.setFontFamily(FONT_FAMILY);

        XWPFParagraph locationPara = document.createParagraph();
        locationPara.setIndentationLeft(400);

        XWPFRun locationRun = locationPara.createRun();
        locationRun.setText("- Địa điểm: " + location);
        locationRun.setFontFamily(FONT_FAMILY);

//        addEmptyLine(locationRun, 2);
    }

    private void createParticipantsSection(XWPFDocument document) {
        XWPFParagraph sectionPara = document.createParagraph();

        XWPFRun sectionRun = sectionPara.createRun();
        sectionRun.setText("3. Đối tượng tham gia");
        sectionRun.setBold(true);
        sectionRun.setFontFamily(FONT_FAMILY);

//        addEmptyLine(sectionRun, 1);

        XWPFParagraph attendeesPara = document.createParagraph();
        attendeesPara.setIndentationLeft(400);

        XWPFRun attendeesRun = attendeesPara.createRun();
        attendeesRun.setText("- Giảng viên quan tâm");
        attendeesRun.setFontFamily(FONT_FAMILY);

        XWPFParagraph participantsPara = document.createParagraph();
        participantsPara.setIndentationLeft(400);

        XWPFRun participantsRun = participantsPara.createRun();
        participantsRun.setText("- Sinh viên: năm? Chuyên ngành");
        participantsRun.setFontFamily(FONT_FAMILY);

//        addEmptyLine(participantsRun, 2);
    }

    private void createAgendaSection(XWPFDocument document, String content) {
        XWPFParagraph sectionPara = document.createParagraph();

        XWPFRun sectionRun = sectionPara.createRun();
        sectionRun.setText("4. Nội dung chương trình");
        sectionRun.setBold(true);
        sectionRun.setFontFamily(FONT_FAMILY);

//        addEmptyLine(sectionRun, 1);

        // Tách nội dung thành các dòng
        String[] lines = content.split("\n");

        for (String line : lines) {
            XWPFParagraph para = document.createParagraph();
            para.setIndentationLeft(400); // Thụt lề như trong mẫu

            XWPFRun run = para.createRun();
            run.setText(line.trim());
            run.setFontFamily(FONT_FAMILY);
        }

//        addEmptyLine(sectionPara.createRun(), 2);
    }

    private void createOrganizersSection(XWPFDocument document, Set<EventOrganizer> organizers) {
        XWPFParagraph sectionPara = document.createParagraph();

        XWPFRun sectionRun = sectionPara.createRun();
        sectionRun.setText("5. Ban tổ chức");
        sectionRun.setBold(true);
        sectionRun.setFontFamily(FONT_FAMILY);

        for (EventOrganizer organizer : organizers) {
            XWPFParagraph orgPara = document.createParagraph();
            orgPara.setIndentationLeft(400);

            XWPFRun orgRun = orgPara.createRun();

            String roleName = organizer.getOrganizerRole() != null ?
                    organizer.getOrganizerRole().getName() : "Không có vai trò";

            String positionName = organizer.getPosition() != null ?
                    organizer.getPosition().getName() : "Không có chức vụ";

            orgRun.setText(String.format("- %s - %s - %s",
                    organizer.getUser().getLastName(),
                    roleName,
                    positionName));
            orgRun.setFontFamily(FONT_FAMILY);
        }
    }

    private void createParticipantsSection(XWPFDocument document, Set<EventParticipant> participants) {
        // Tạo phần tiêu đề cho danh sách người tham gia
        XWPFParagraph sectionPara = document.createParagraph();

        XWPFRun sectionRun = sectionPara.createRun();
        sectionRun.setText("6. Người tham gia");
        sectionRun.setBold(true);
        sectionRun.setFontFamily(FONT_FAMILY);

        // Thêm từng người tham gia vào danh sách
        for (EventParticipant participant : participants) {
            XWPFParagraph participantPara = document.createParagraph();
            participantPara.setIndentationLeft(400);

            XWPFRun participantRun = participantPara.createRun();

            String roleName = participant.getOrganizerRole() != null ?
                    participant.getOrganizerRole().getName() : "Không có vai trò";

            String positionName = participant.getPosition() != null ?
                    participant.getPosition().getName() : "Không có chức vụ";

            participantRun.setText(String.format("- %s - %s - %s",
                    participant.getUser().getLastName(),
                    roleName,
                    positionName));
            participantRun.setFontFamily(FONT_FAMILY);
        }
    }



//    private void createDetailedParticipantsSection(XWPFDocument document, Set<User> attendees, Set<User> participants) {
//        XWPFParagraph sectionPara = document.createParagraph();
//        addEmptyLine(sectionPara.createRun(), 1);
//
//        XWPFParagraph titlePara = document.createParagraph();
//        titlePara.setAlignment(ParagraphAlignment.CENTER);
//
//        XWPFRun titleRun = titlePara.createRun();
//        titleRun.setText("DANH SÁCH THÀNH VIÊN THAM DỰ");
//        titleRun.setBold(true);
//        titleRun.setFontFamily(FONT_FAMILY);
//
//        XWPFParagraph subtitlePara = document.createParagraph();
//        subtitlePara.setAlignment(ParagraphAlignment.CENTER);
//
//        XWPFRun subtitleRun = subtitlePara.createRun();
//        subtitleRun.setText("Seminar chuyên đề \"Sử dụng AI Developer Tools hỗ trợ lập trình\"");
//        subtitleRun.setFontFamily(FONT_FAMILY);
//
//        addEmptyLine(subtitleRun, 1);
//
//        // Tạo bảng
//        XWPFTable table = document.createTable();
//        table.setWidth("100%");
//
//        // Header
//        XWPFTableRow headerRow = table.getRow(0);
//        setTableCell(headerRow, 0, "STT", true);
//        setTableCell(headerRow, 1, "Mã giảng viên", true);
//        setTableCell(headerRow, 2, "Họ và tên", true);
//        setTableCell(headerRow, 3, "Đơn vị", true);
//        setTableCell(headerRow, 4, "Ghi chú", true);
//
//        // Kết hợp attendees và participants
//        Set<User> allParticipants = new HashSet<>();
//        allParticipants.addAll(attendees);
//        allParticipants.addAll(participants);
//
//        // Thêm dữ liệu
//        int stt = 1;
//        for (User user : allParticipants) {
//            XWPFTableRow row = table.createRow();
//            setTableCell(row, 0, String.valueOf(stt++), false);
//            setTableCell(row, 1, user.getCode() != null ? user.getCode() : "", false);
//            setTableCell(row, 2, user.getFullName(), false);
//            setTableCell(row, 3, user.getDepartment() != null ? user.getDepartment() : "", false);
//            setTableCell(row, 4, attendees.contains(user) ? "Tham dự" : "Chia sẻ viên", false);
//        }
//
//        addEmptyLine(table.getRow(table.getNumberOfRows()-1).getCell(0).getParagraphs().get(0).createRun(), 2);
//
//        // Footer
//        XWPFParagraph footerPara = document.createParagraph();
//        footerPara.setAlignment(ParagraphAlignment.RIGHT);
//
//        XWPFRun footerRun = footerPara.createRun();
//        footerRun.setText("Khoa CNTT Bộ Môn");
//        footerRun.setFontFamily(FONT_FAMILY);
//
//        addEmptyLine(footerRun, 1);
//
//        XWPFParagraph signPara = document.createParagraph();
//        signPara.setAlignment(ParagraphAlignment.RIGHT);
//
//        XWPFRun signRun = signPara.createRun();
//        signRun.setText("Nguyễn Thị Hạnh");
//        signRun.setFontFamily(FONT_FAMILY);
//    }

    private void setTableCell(XWPFTableRow row, int col, String text, boolean bold) {
        XWPFTableCell cell = col < row.getTableCells().size() ? row.getCell(col) : row.addNewTableCell();
        cell.removeParagraph(0);
        XWPFParagraph para = cell.addParagraph();
        para.setAlignment(ParagraphAlignment.CENTER);

        XWPFRun run = para.createRun();
        run.setText(text);
        run.setFontFamily(FONT_FAMILY);
        run.setBold(bold);
    }

    private void addEmptyLine(XWPFRun run, int count) {
        for (int i = 0; i < count; i++) {
            run.addBreak();
        }
    }

    private File createTempFile() throws IOException {
        File tempDir = new File(System.getProperty("java.io.tmpdir"));
        String fileName = TEMP_FILE_PREFIX + System.currentTimeMillis() + ".docx";
        File tempFile = new File(tempDir, fileName);
        Files.deleteIfExists(tempFile.toPath());
        return tempFile;
    }

    public Resource exportAttendeesToExcel(List<AttendeeResponse> attendees) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách attendees");

        // Tạo header
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Họ và tên");
        headerRow.createCell(1).setCellValue("Mã số sinh viên");
        headerRow.createCell(2).setCellValue("Tham gia");
        headerRow.createCell(3).setCellValue("Thời gian điểm danh");
        // Đổ dữ liệu
        int rowNum = 1;
//        for (AttendeeResponse attendee : attendees) {
//            Row row = sheet.createRow(rowNum++);
//            row.createCell(0).setCellValue(attendee.getLastName() + " " + attendee.getFirstName());
//            row.createCell(1).setCellValue(attendee.getStudentCode());
//            row.createCell(2).setCellValue(attendee.isAttending() ? "Có" : "Không");
//            row.createCell(3).setCellValue(attendee.getCheckedInAt());
//        }
        // Tạo một CellStyle để định dạng ngày giờ
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

        for (AttendeeResponse attendee : attendees) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendee.getLastName() + " " + attendee.getFirstName());
            row.createCell(1).setCellValue(attendee.getStudentCode());
//            row.createCell(2).setCellValue(attendee.getIsAttending() ? "Có" : "Không");
            row.createCell(2).setCellValue(
                    attendee.getIsAttending() == null ? "Chưa điểm danh" :
                            attendee.getIsAttending() ? "Có" : "Không"
            );
            // Tạo cell cho thời gian và áp dụng định dạng
            Cell dateCell = row.createCell(3);
            if (attendee.getCheckedInAt() != null) {
                dateCell.setCellValue(Date.from(attendee.getCheckedInAt().atZone(ZoneId.systemDefault()).toInstant()));
                dateCell.setCellStyle(dateCellStyle);
            } else {
                dateCell.setCellValue(""); // hoặc "Chưa điểm danh" tùy yêu cầu
            }
        }

        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
        }

        // Ghi ra file tạm
        File tempFile = File.createTempFile("attendees_", ".xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }

        return new FileSystemResource(tempFile);
    }

    public Resource exportAttendeesToExcel(List<AttendeeResponse> attendees, Event event) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Danh sách attendees");

        // Tạo style cho tiêu đề sự kiện
        CellStyle eventInfoStyle = workbook.createCellStyle();
        Font eventInfoFont = workbook.createFont();
        eventInfoFont.setBold(true);
        eventInfoFont.setFontHeightInPoints((short) 12);
        eventInfoStyle.setFont(eventInfoFont);

        // Tạo style cho header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);

        // Tạo style cho định dạng ngày giờ
        CreationHelper createHelper = workbook.getCreationHelper();
        CellStyle dateCellStyle = workbook.createCellStyle();
        dateCellStyle.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm:ss"));

        // Thêm thông tin sự kiện
        int rowNum = 0;
        Row eventNameRow = sheet.createRow(rowNum++);
        eventNameRow.createCell(0).setCellValue("Tên sự kiện:");
        Cell eventNameCell = eventNameRow.createCell(1);
        eventNameCell.setCellValue(event.getName());
        eventNameCell.setCellStyle(eventInfoStyle);

        Row eventTimeRow = sheet.createRow(rowNum++);
        eventTimeRow.createCell(0).setCellValue("Thời gian:");
        Cell eventTimeCell = eventTimeRow.createCell(1);
        if (event.getTime() != null) {
            eventTimeCell.setCellValue(Date.from(event.getTime().atZone(ZoneId.systemDefault()).toInstant()));
            eventTimeCell.setCellStyle(dateCellStyle);
        }

        Row createdByRow = sheet.createRow(rowNum++);
        createdByRow.createCell(0).setCellValue("Người tạo:");
        Cell createdByCell = createdByRow.createCell(1);
        createdByCell.setCellValue(event.getCreatedBy().getFirstName() + "" + event.getCreatedBy().getLastName());
        createdByCell.setCellStyle(eventInfoStyle);


        Row eventLocationRow = sheet.createRow(rowNum++);
        eventLocationRow.createCell(0).setCellValue("Tên địa điểm:");
        Cell eventLocationCell = eventLocationRow.createCell(1);
        eventLocationCell.setCellValue(event.getLocation());
        eventLocationCell.setCellStyle(eventInfoStyle);

        // Thêm một dòng trống
        rowNum++;

        // Tạo header cho danh sách attendees
        Row headerRow = sheet.createRow(rowNum++);
        String[] headers = {"Họ và tên", "Mã số sinh viên", "Tham gia", "Thời gian điểm danh"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Đổ dữ liệu attendees
        for (AttendeeResponse attendee : attendees) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(attendee.getLastName() + " " + attendee.getFirstName());
            row.createCell(1).setCellValue(attendee.getStudentCode());
            row.createCell(2).setCellValue(
                    attendee.getIsAttending() == null ? "Chưa điểm danh" :
                            attendee.getIsAttending() ? "Có" : "Không"
            );

            Cell dateCell = row.createCell(3);
            if (attendee.getCheckedInAt() != null) {
                dateCell.setCellValue(Date.from(attendee.getCheckedInAt().atZone(ZoneId.systemDefault()).toInstant()));
                dateCell.setCellStyle(dateCellStyle);
            } else {
                dateCell.setCellValue("");
            }
        }

        // Tự động điều chỉnh độ rộng cột
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Ghi ra file tạm
        File tempFile = File.createTempFile("attendees_", ".xlsx");
        try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }

        return new FileSystemResource(tempFile);
    }
}