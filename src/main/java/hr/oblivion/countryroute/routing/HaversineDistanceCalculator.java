package hr.oblivion.countryroute.routing;

public final class HaversineDistanceCalculator implements DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    @Override
    public double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double sinDLat = Math.sin(dLat / 2);
        double sinDLng = Math.sin(dLng / 2);
        double h = sinDLat * sinDLat
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * sinDLng * sinDLng;
        double clamped = Math.clamp(h, 0.0, 1.0);
        return 2 * EARTH_RADIUS_KM * Math.atan2(Math.sqrt(clamped), Math.sqrt(1 - clamped));
    }
}
