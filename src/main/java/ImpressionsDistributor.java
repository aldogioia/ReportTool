import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ImpressionsDistributor {
    private static final Random random = new Random();

    public static int distributeImpressionsForScreen(int weekday, Long totalWeeklyImpressions) {
        // Pesi giornalieri: lunedì ha peso maggiore, domenica ha peso minore
        double[] weights = {0.22, 0.19, 0.17, 0.14, 0.11, 0.09, 0.08};

        double base = totalWeeklyImpressions * weights[weekday - 1];
        return Math.toIntExact(Math.round(base));
    }

    public static Map<String, Long> distributeImpressionsForCreative(Long dailyImpressions, List<String> creatives) {
        int n = creatives.size();
        double[] weights = new double[n];
        double totalWeight = 0;

        for (int i = 0; i < n; i++) {
            weights[i] = 0.9 + random.nextDouble() * 0.2;
            totalWeight += weights[i];
        }

        double[] quotas = new double[n];
        Long[] floorValues = new Long[n];
        long allocated = 0;

        for (int i = 0; i < n; i++) {
            quotas[i] = dailyImpressions * (weights[i] / totalWeight);
            floorValues[i] = (long) Math.floor(quotas[i]);
            allocated += floorValues[i];
        }

        long remaining = dailyImpressions - allocated;

        List<Integer> indices = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) indices.add(i);
        java.util.Collections.shuffle(indices, random);

        for (int i = 0; i < remaining; i++) {
            floorValues[indices.get(i)] += 1;
        }

        Map<String, Long> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            result.put(creatives.get(i), floorValues[i]);
        }

        return result;
    }
}

