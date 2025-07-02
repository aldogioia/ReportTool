import utils.Constants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CSVLoader {
    private static CSVLoader instance;
    private final Map<String, Long> dataScreen;

    private CSVLoader() {
        dataScreen = loadDataScreen();
    }

    public static CSVLoader getInstance() {
        if (instance == null) {
            instance = new CSVLoader();
        }
        return instance;
    }

    public Map<String, Long> getDataScreen(){
        return dataScreen;
    }

    private Map<String, Long> loadDataScreen() {
        Map<String, Long> map = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(Constants.DATA_SCREEN_FILE))) {
           String row;
           while ((row = bufferedReader.readLine()) != null) {
               String[] values = row.split(";");
               if (values.length >= 2)
                   map.put(values[0], Long.parseLong(values[1]));
           }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }
}
