
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectionHandler {
    public static List<String> generateData(
            LocalDate startDate,
            LocalDate endDate,
            Double cpm,
            Integer impressionsEstimated,
            Integer impressionsGoal,
            List<String> screens,
            List<String> creatives
    ) {
        List<String> projectionData = new ArrayList<>();

        for (LocalDate date = startDate; date.isEqual(endDate) || date.isBefore(endDate); date = date.plusDays(1)) {
            for (String screen : screens) {
                //ottengo il numero di impressioni settimanali per lo schermo
                Long weeklyImpressionsForScreen = CSVLoader.getInstance().getDataScreen().get(screen);

                //distribuisco le impressioni settimanali per il giorno della settimana
                long dailyImpressions = ImpressionsDistributor.distributeImpressionsForScreen(
                        date.getDayOfWeek().getValue(),
                        weeklyImpressionsForScreen
                );

                //scalo le impressions giornaliere in base alle impressions goal
                dailyImpressions = (dailyImpressions * impressionsGoal) / impressionsEstimated;

                //distribuisco le impressioni giornaliere per le varie creatività
                Map<String, Long> creativeImpressions = ImpressionsDistributor
                        .distributeImpressionsForCreative(dailyImpressions, creatives);

                for (Map.Entry<String, Long> creativeImpression : creativeImpressions.entrySet()) {
                    String creative = creativeImpression.getKey();
                    Long impressions = creativeImpression.getValue();
                    double spend = (cpm * impressions) / 1000;
                    String projectionRow = date + ";" + screen + ";" + creative + ";" + impressions + ";" + spend;
                    projectionData.add(projectionRow);
                }
            }
        }
        return projectionData;
    }

}
