
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectionHandler {
    public static List<String> generateData(
            LocalDate startDate,
            LocalDate endDate,
            Double cpm,
            Integer impressionsGoal,
            List<String> screens
    ) {
        List<String> projectionData = new ArrayList<>();

        for (LocalDate date = startDate; date.isEqual(endDate) || date.isBefore(endDate); date = date.plusDays(1)) {
            for (String screen : screens) {
                //ottengo il numero di impressioni settimanali per lo schermo
                Integer weeklyImpressionsForScreen = CSVLoader.getInstance().getDataScreen().get(screen);

                //distribuisco le impressioni settimanali per il giorno della settimana
                int dailyImpressions = ImpressionsDistributor.distributeImpressionsForScreen(
                        date.getDayOfWeek().getValue(),
                        weeklyImpressionsForScreen
                );

                //distribuisco le impressioni giornaliere per le varie creatività
                Map<String, Integer> creativeImpressions = ImpressionsDistributor.distributeImpressionsForCreative(
                        dailyImpressions,
                        CSVLoader.getInstance().getCreatives()
                );

                for (Map.Entry<String, Integer> creativeImpression : creativeImpressions.entrySet()) {
                    String creative = creativeImpression.getKey();
                    Integer impressions = creativeImpression.getValue();
                    double spend = cpm * impressions / 1000;
                    String projectionRow = date + ";" + screen + ";" + creative + ";" + impressions + ";" + spend;
                    projectionData.add(projectionRow);
                }
            }
        }
        return projectionData;
    }

}
