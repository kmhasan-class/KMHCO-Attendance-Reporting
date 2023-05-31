package bd.com.kmhasan.attendance_reporting.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
public
class Record {
    private Employee employee;
    private LocalDateTime scanningTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Record record = (Record) o;
        return employee.equals(record.employee) && scanningTime.equals(record.scanningTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employee, scanningTime);
    }
}