package bd.com.kmhasan.attendance_reporting;

import bd.com.kmhasan.attendance_reporting.model.Record;
import bd.com.kmhasan.attendance_reporting.service.AttendanceRecordsProcessingService;
import bd.com.kmhasan.attendance_reporting.service.CsvReadingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.time.LocalDate;
import java.util.List;

@SpringBootApplication
public class KmhcoAttendanceReportingApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(KmhcoAttendanceReportingApplication.class, args);
        List<String[]> rowData = run.getBean(CsvReadingService.class).getRecords("attendance.csv");
        System.out.println("Read " + rowData.size() + " lines");
        List<Record> records = run.getBean(AttendanceRecordsProcessingService.class).getReport(rowData,
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 5));
        System.out.println("Read " + records.size() + " lines");
    }

}
