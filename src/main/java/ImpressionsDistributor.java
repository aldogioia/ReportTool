import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ImpressionsDistributor {
    private static final Random random = new Random();

    public static int distributeImpressionsForScreen(int weekday, int totalWeeklyImpressions) {
        // Pesi giornalieri: lunedì (1) ha peso maggiore, domenica (7) ha peso minore
        double[] weights = {0.22, 0.19, 0.17, 0.14, 0.11, 0.09, 0.08}; // Totale = 1.00
        if (weekday < 1 || weekday > 7) {
            throw new IllegalArgumentException("Il giorno della settimana deve essere tra 1 (lunedì) e 7 (domenica)");
        }

        double base = totalWeeklyImpressions * weights[weekday - 1];

//        // Aggiungi variazione casuale (±5%)
//        double variation = base * (0.05 * (random.nextDouble() - 0.5)); // da -2.5% a +2.5%
//        int result = (int) Math.round(base + variation);

        return Math.toIntExact(Math.round(base));
    }

    public static Map<String, Integer> distributeImpressionsForCreative(int dailyImpressions, List<String> creatives) {
        int n = creatives.size();
        double[] weights = new double[n];
        double totalWeight = 0;

        for (int i = 0; i < n; i++) {
            weights[i] = 0.9 + random.nextDouble() * 0.2;
            totalWeight += weights[i];
        }

        double[] quotas = new double[n];
        int[] floorValues = new int[n];
        int allocated = 0;

        for (int i = 0; i < n; i++) {
            quotas[i] = dailyImpressions * (weights[i] / totalWeight);
            floorValues[i] = (int) Math.floor(quotas[i]);
            allocated += floorValues[i];
        }

        int remaining = dailyImpressions - allocated;

        List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);
        java.util.Collections.shuffle(indices, random);

        for (int i = 0; i < remaining; i++) {
            floorValues[indices.get(i)] += 1;
        }

        Map<String, Integer> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(creatives.get(i), floorValues[i]);
        }

        return result;
    }
}

