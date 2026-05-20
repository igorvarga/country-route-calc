package hr.oblivion.countryroute.routing;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.oblivion.countryroute.data.CountryGraph;
import hr.oblivion.countryroute.data.EmbeddedCountrySource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BfsRouteFinderTest {

    private final CountryGraph graph = CountryGraph.from(
            new EmbeddedCountrySource(new ObjectMapper(), "data/countries-fixture.json").load());

    private final BfsRouteFinder finder = new BfsRouteFinder(graph);

    @Test
    void findsFewestBorderCrossings() {
        Optional<List<String>> route = finder.find("CZE", "ITA");
        assertThat(route).isPresent();
        assertThat(route.get()).containsExactly("CZE", "AUT", "ITA");
    }

    @Test
    void findsDirectBorder() {
        Optional<List<String>> route = finder.find("CZE", "AUT");
        assertThat(route).isPresent();
        assertThat(route.get()).containsExactly("CZE", "AUT");
    }

    @Test
    void returnsSingletonWhenOriginEqualsDestination() {
        Optional<List<String>> route = finder.find("CZE", "CZE");
        assertThat(route).isPresent();
        assertThat(route.get()).containsExactly("CZE");
    }

    @Test
    void returnsEmptyForDisconnectedIsland() {
        Optional<List<String>> route = finder.find("ISL", "DEU");
        assertThat(route).isEmpty();
    }

    @Test
    void returnsEmptyForUnknownNode() {
        Optional<List<String>> route = finder.find("CZE", "ZZZ");
        assertThat(route).isEmpty();
    }
}
