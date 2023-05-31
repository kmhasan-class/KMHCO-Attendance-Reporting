package bd.com.kmhasan.attendance_reporting.service;

import bd.com.kmhasan.attendance_reporting.model.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import bd.com.kmhasan.attendance_reporting.model.Record;

@Service
public class AttendanceRecordsProcessingService {

    public List<Record> getReport(List<String[]> rowData, LocalDate startDate, LocalDate endDate) {
        System.out.println(Arrays.toString(rowData.get(0)));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm:ss a");

        List<Record> records = new ArrayList<>();
        for (int i = 1; i < rowData.size(); i++) {
            String[] row = rowData.get(i);
            String dateTimeString = row[2];
            LocalDateTime dateTime = LocalDateTime.parse(dateTimeString, formatter);
            Record record = new Record(new Employee(row[0], row[1]), dateTime);
            if ((startDate == null || !dateTime.isBefore(startDate.atStartOfDay()))
                && (endDate == null || dateTime.isBefore(endDate.atStartOfDay().plusDays(1))))
                records.add(record);
        }

        if (records != null || records.size() != 0) {
            if (startDate == null)
                startDate = records.get(0).getScanningTime().toLocalDate();
            if (endDate == null)
                endDate = records.get(0).getScanningTime().toLocalDate();
            startDate = records.stream().map(Record::getScanningTime).min(LocalDateTime::compareTo).get().toLocalDate();
            endDate = records.stream().map(Record::getScanningTime).max(LocalDateTime::compareTo).get().toLocalDate();

            List<LocalDate> daysList = new ArrayList<>();
            Map<LocalDate, Integer> columnIndices = new HashMap<>();

            for (int i = 0; !startDate.plusDays(i).isAfter(endDate); i++) {
                daysList.add(startDate.plusDays(i));
                columnIndices.put(startDate.plusDays(i), i);
            }

            Map<Employee, Integer> rowIndices = new HashMap<>();
            records.stream().forEach(record -> rowIndices.putIfAbsent(record.getEmployee(), rowIndices.size()));

            LocalDateTime[][][] entriesMatrix = new LocalDateTime[rowIndices.size()][columnIndices.size()][2];

            for (Record record : records) {
                Employee employee = record.getEmployee();
                LocalDateTime scanningTime = record.getScanningTime();

                int employeeIndex = rowIndices.get(employee).intValue();
                int dateIndex = columnIndices.get(scanningTime.toLocalDate()).intValue();

                if (entriesMatrix[employeeIndex][dateIndex][0] == null) {
                    entriesMatrix[employeeIndex][dateIndex][0] = scanningTime;
                } else if (entriesMatrix[employeeIndex][dateIndex][1] == null) {
                    entriesMatrix[employeeIndex][dateIndex][1] = scanningTime;
                    if (entriesMatrix[employeeIndex][dateIndex][0].compareTo(entriesMatrix[employeeIndex][dateIndex][1]) > 0) {
                        LocalDateTime temp = entriesMatrix[employeeIndex][dateIndex][0];
                        entriesMatrix[employeeIndex][dateIndex][0] = entriesMatrix[employeeIndex][dateIndex][1];
                        entriesMatrix[employeeIndex][dateIndex][1] = temp;
                    }
                } else {
                    if (scanningTime.compareTo(entriesMatrix[employeeIndex][dateIndex][0]) < 0)
                        entriesMatrix[employeeIndex][dateIndex][0] = scanningTime;
                    if (scanningTime.compareTo(entriesMatrix[employeeIndex][dateIndex][1]) > 1)
                        entriesMatrix[employeeIndex][dateIndex][1] = scanningTime;
                }
            }

            Employee[] employees = new Employee[rowIndices.size()];
            LocalDate[] dates = daysList.toArray(new LocalDate[0]);

            System.out.println("<html>");
            System.out.println("<head><title>Attendance Data</title></head>");
            System.out.println("<body>");
            System.out.println("<table>");
            System.out.println("<tr>");
            System.out.println("<th>Department</th>");
            System.out.println("<th>Employee</th>");
            for (LocalDate localDate : dates)
                System.out.println("<th>" + localDate + "</th>");
            System.out.println("</tr>");
            for (int r = 0; r < employees.length; r++) {
                for (int c = 0; c < dates.length; c++) {

                }
            }
            System.out.println("</table>");
            System.out.println("</body>");
            System.out.println("</html>");
        }

        if (records == null)
            return new ArrayList<>();
        return records;
    }
}
