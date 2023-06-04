package bd.com.kmhasan.attendance_reporting.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvReadingService {
    private static final String COMMA_DELIMITER = ",";

    public List<String[]> getRecords(String filename) {
        System.out.println("HELLO WORLD");
        List<String[]> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(COMMA_DELIMITER);
                records.add(values);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return records;
    }
}
