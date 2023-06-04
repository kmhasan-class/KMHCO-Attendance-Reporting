package bd.com.kmhasan.attendance_reporting.service;

import bd.com.kmhasan.attendance_reporting.model.Employee;
import bd.com.kmhasan.attendance_reporting.model.Record;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class AttendanceRecordsProcessingService {
    private String dateToString(LocalDate localDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE-dd");
        return localDate.format(dateTimeFormatter);
    }

    private String dateToMonthDayYearString(LocalDate localDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return localDate.format(dateTimeFormatter);
    }

    private String timeToString(LocalDateTime startTime, LocalDateTime endTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder builder = new StringBuilder();
        if (startTime != null)
            builder.append(startTime.format(dateTimeFormatter));
        else builder.append('X');
        builder.append('-');
        if (endTime != null)
            builder.append(endTime.format(dateTimeFormatter));
        else builder.append('X');
        return builder.toString();
    }

    public List<Record> getReport(List<String[]> rowData, LocalDate startDate, LocalDate endDate) throws IOException {
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

        if (records.size() != 0) {
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

                int employeeIndex = rowIndices.get(employee);
                int dateIndex = columnIndices.get(scanningTime.toLocalDate());

                if (entriesMatrix[employeeIndex][dateIndex][0] == null) {
                    entriesMatrix[employeeIndex][dateIndex][0] = scanningTime;
                } else if (entriesMatrix[employeeIndex][dateIndex][1] == null) {
                    entriesMatrix[employeeIndex][dateIndex][1] = scanningTime;
                    if (entriesMatrix[employeeIndex][dateIndex][0].isAfter(entriesMatrix[employeeIndex][dateIndex][1])) {
                        LocalDateTime temp = entriesMatrix[employeeIndex][dateIndex][0];
                        entriesMatrix[employeeIndex][dateIndex][0] = entriesMatrix[employeeIndex][dateIndex][1];
                        entriesMatrix[employeeIndex][dateIndex][1] = temp;
                    }
                } else {
                    if (scanningTime.isBefore(entriesMatrix[employeeIndex][dateIndex][0]))
                        entriesMatrix[employeeIndex][dateIndex][0] = scanningTime;
                    if (scanningTime.compareTo(entriesMatrix[employeeIndex][dateIndex][1]) > 0)
                        entriesMatrix[employeeIndex][dateIndex][1] = scanningTime;
                }
            }

            Employee[] employees = new Employee[rowIndices.size()];
            LocalDate[] dates = daysList.toArray(new LocalDate[0]);

            rowIndices.forEach(((employee, index) -> employees[index] = employee));

            BufferedWriter writer = new BufferedWriter(new FileWriter("output/attendance-report.html", false));

            writer.append("<html>");
            writer.append("<head><title>Attendance Data</title><link rel=\"stylesheet\" href=\"styles.css\"></head>");
            writer.append("<body>");
            writer.append("<h1>KMHCO Attendance Data for the period ").append(dateToMonthDayYearString(daysList.get(0))).append(" to ").append(dateToMonthDayYearString(daysList.get(daysList.size() - 1))).append("</h1>");
            writer.append("<table cellspacing=\"0\" cellpadding=\"0\" border=\"1\" bgcolor=\"#FFF\">");
            writer.append("<tr>");
            writer.append("<th>Department</th>");
            writer.append("<th>Employee</th>");
            for (LocalDate localDate : dates)
                writer.append("<th>").append(dateToString(localDate)).append("</th>");
            writer.append("</tr>");
            for (int r = 0; r < employees.length; r++) {
                writer.append("<tr>");
                writer.append("<th>").append(employees[r].getDepartment()).append("</th>");
                writer.append("<td>").append(employees[r].getEmployeeName()).append("</th>");
                for (int c = 0; c < dates.length; c++) {
                    writer.append("<td class=\"");
                    if (entriesMatrix[r][c][0] != null && entriesMatrix[r][c][0].isAfter(entriesMatrix[r][c][0].toLocalDate().atTime(9, 30)))
                        writer.append(" late-entry");
                    if (dates[c].getDayOfWeek() == DayOfWeek.FRIDAY || dates[c].getDayOfWeek() == DayOfWeek.SATURDAY)
                        writer.append(" weekend");
                    writer.append("\">");
                    writer.append(timeToString(entriesMatrix[r][c][0], entriesMatrix[r][c][1]));
                    writer.append("</td>");
                }
                writer.append("</tr>");
            }
            writer.append("</table>");
            writer.append("</body>");
            writer.append("</html>");
            writer.close();
        }

        return records;
    }

}
