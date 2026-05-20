package hr.oblivion.countryroute.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

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

  @Test
  void duplicateCca3FailsFast() {
    Country first = new Country("CZE", List.of(), List.of(), new Country.Name("Czechia"));
    Country duplicate =
        new Country("CZE", List.of(), List.of(), new Country.Name("Duplicate Czechia"));
    CountryDataset duplicateDataset =
        new CountryDataset(
            List.of(first, duplicate), new DatasetMetadata("test", "memory", Instant.now(), 2));

    assertThatThrownBy(() -> CountryGraph.from(duplicateDataset))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Duplicate country cca3 in dataset: CZE");
  }

  @Test
  void emptyDatasetFailsFast() {
    CountryDataset emptyDataset =
        new CountryDataset(List.of(), new DatasetMetadata("test", "memory", Instant.now(), 0));

    assertThatThrownBy(() -> CountryGraph.from(emptyDataset))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Country dataset is empty");
  }

  @Test
  void malformedCountryCca3FailsFast() {
    Country malformed = new Country("CZ", List.of(), List.of(), new Country.Name("Malformed"));
    CountryDataset malformedDataset =
        new CountryDataset(
            List.of(malformed), new DatasetMetadata("test", "memory", Instant.now(), 1));

    assertThatThrownBy(() -> CountryGraph.from(malformedDataset))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Invalid country cca3 in dataset: CZ");
  }
}
