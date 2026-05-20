package hr.oblivion.countryroute.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CountryGraphTest {

    private final CountryDataset dataset =
            new EmbeddedCountrySource(new ObjectMapper(), "data/countries-fixture.json").load();

    @Test
    void buildsAdjacencyFromBorders() {
        CountryGraph graph = CountryGraph.from(dataset);

        assertThat(graph.size()).isEqualTo(7);
        assertThat(graph.neighbors("CZE")).containsExactly("AUT", "DEU", "POL", "SVK");
        assertThat(graph.neighbors("AUT")).containsExactly("CZE", "DEU", "ITA", "SVK");
    }

    @Test
    void treatsGraphAsUndirected() {
        CountryGraph graph = CountryGraph.from(dataset);

        // CZE lists POL as border and POL lists CZE — symmetric
        assertThat(graph.neighbors("POL")).contains("CZE");
        // AUT lists ITA but ITA also lists AUT — symmetric
        assertThat(graph.neighbors("ITA")).contains("AUT");
    }

    @Test
    void islandHasNoNeighbors() {
        CountryGraph graph = CountryGraph.from(dataset);
        assertThat(graph.neighbors("ISL")).isEmpty();
    }

    @Test
    void unknownBorderReferencesAreSkipped() {
        // ITA borders FRA, but FRA is not in the fixture → should be silently skipped (with WARN log)
        CountryGraph graph = CountryGraph.from(dataset);
        assertThat(graph.contains("FRA")).isFalse();
        assertThat(graph.neighbors("ITA")).containsExactly("AUT");
    }
}
