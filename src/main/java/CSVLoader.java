import utils.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CSVLoader {
    private static CSVLoader instance;
    private final Map<String, Integer> dataScreen;
    private final List<String> creatives = new ArrayList<>();

    private CSVLoader() {
        dataScreen = loadDataScreen();
    }

    public static CSVLoader getInstance() {
        if (instance == null) {
            instance = new CSVLoader();
        }
        return instance;
    }

    public Map<String, Integer> getDataScreen(){
        return dataScreen;
    }

    public List<String> getCreatives(){
        return creatives;
    }

    private Map<String, Integer> loadDataScreen() {
        Map<String, Integer> map = new HashMap<>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(Constants.DATA_SCREEN_FILE))) {
           String row;
           while ((row = bufferedReader.readLine()) != null) {
               String[] values = row.split(";");
               if (values.length >= 2)
                   map.put(values[0], Integer.parseInt(values[1]));
           }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return map;
    }

    public void loadCreatives(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String row;
            while ((row = bufferedReader.readLine()) != null) {
                creatives.add(row);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
