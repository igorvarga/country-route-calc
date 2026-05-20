package hr.oblivion.countryroute.routing;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DistanceCalculatorTest {

    private final DistanceCalculator geographicLib = new GeographicLibDistanceCalculator();
    private final DistanceCalculator haversine = new HaversineDistanceCalculator();

    @Test
    void geographicLibUsesWgs84GeodesicDistance() {
        double distanceKm = geographicLib.distanceKm(50.0755, 14.4378, 48.2082, 16.3738);
        assertThat(geographicLib.name()).isEqualTo("geographiclib");
        assertThat(distanceKm).isCloseTo(251.136, withinKilometers(0.001));
    }

    @Test
    void haversineImplementationRemainsAvailable() {
        double distanceKm = haversine.distanceKm(50.0755, 14.4378, 48.2082, 16.3738);
        assertThat(haversine.name()).isEqualTo("haversine");
        assertThat(distanceKm).isCloseTo(250.869, withinKilometers(0.001));
    }

    @Test
    void haversineHandlesAntipodalPoints() {
        assertThat(haversine.distanceKm(0.0, 0.0, 0.0, 180.0)).isFinite();
    }

    @Test
    void invalidCoordinateListsReturnInfiniteDistance() {
        assertThat(geographicLib.distanceKm(List.of(50.0755), List.of(48.2082, 16.3738)))
                .isEqualTo(Double.POSITIVE_INFINITY);
    }

    private static org.assertj.core.data.Offset<Double> withinKilometers(double offset) {
        return org.assertj.core.data.Offset.offset(offset);
    }
}
