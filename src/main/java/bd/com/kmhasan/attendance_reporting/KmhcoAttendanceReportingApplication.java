package bd.com.kmhasan.attendance_reporting;

import bd.com.kmhasan.attendance_reporting.model.Record;
import bd.com.kmhasan.attendance_reporting.service.AttendanceRecordsProcessingService;
import bd.com.kmhasan.attendance_reporting.service.CsvReadingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class KmhcoAttendanceReportingApplication {

    public static void main(String[] args) throws IOException {
        ConfigurableApplicationContext run = SpringApplication.run(KmhcoAttendanceReportingApplication.class, args);
        List<String[]> rowData = run.getBean(CsvReadingService.class).getRecords("attendance.csv");
        System.out.println("Read " + rowData.size() + " lines");

        int monthDays[] = {31, 28, 31, 30, 31};

        AttendanceRecordsProcessingService attendanceRecordsProcessingService = run.getBean(AttendanceRecordsProcessingService.class);

        for (int month = 0; month < monthDays.length; month++) {
            List<Record> records = attendanceRecordsProcessingService.getReport(rowData,
                    LocalDate.of(2023, month + 1, 1),
                    LocalDate.of(2023, month + 1, monthDays[month]));
            System.out.println("Read " + records.size() + " lines");
        }

        attendanceRecordsProcessingService.getReport(rowData,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 12, 31));
    }

}
