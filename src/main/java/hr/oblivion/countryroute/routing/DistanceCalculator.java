package hr.oblivion.countryroute.routing;

import java.util.List;

public interface DistanceCalculator {

    String name();

    default double distanceKm(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() < 2 || b.size() < 2) {
            return Double.POSITIVE_INFINITY;
        }
        return distanceKm(a.get(0), a.get(1), b.get(0), b.get(1));
    }

    double distanceKm(double lat1, double lng1, double lat2, double lng2);
}
