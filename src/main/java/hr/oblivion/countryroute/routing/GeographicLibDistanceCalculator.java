package hr.oblivion.countryroute.routing;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicMask;

public final class GeographicLibDistanceCalculator implements DistanceCalculator {

    private static final double METERS_PER_KILOMETER = 1000.0;

    @Override
    public double distanceKm(double lat1, double lng1, double lat2, double lng2) {
        return Geodesic.WGS84.Inverse(lat1, lng1, lat2, lng2, GeodesicMask.DISTANCE).s12
                / METERS_PER_KILOMETER;
    }
}
