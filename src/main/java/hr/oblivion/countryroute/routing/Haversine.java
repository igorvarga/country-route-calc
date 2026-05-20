package hr.oblivion.countryroute.routing;

import java.util.List;

public final class Haversine {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private Haversine() {}

    public static double distanceKm(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() < 2 || b.size() < 2) {
            return Double.POSITIVE_INFINITY;
        }
        return distanceKm(a.get(0), a.get(1), b.get(0), b.get(1));
    }

    public static double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sinDLat = Math.sin(dLat / 2);
        double sinDLng = Math.sin(dLng / 2);
        double h = sinDLat * sinDLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinDLng * sinDLng;
        return 2 * EARTH_RADIUS_KM * Math.atan2(Math.sqrt(h), Math.sqrt(1 - h));
    }
}
