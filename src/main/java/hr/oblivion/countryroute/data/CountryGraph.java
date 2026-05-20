package hr.oblivion.countryroute.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CountryGraph {

  private static final Logger log = LoggerFactory.getLogger(CountryGraph.class);
  private static final Pattern CCA3_PATTERN = Pattern.compile("[A-Z]{3}");

  private final Map<String, Country> byCode;
  private final Map<String, List<String>> adjacency;

  private CountryGraph(Map<String, Country> byCode, Map<String, List<String>> adjacency) {
    this.byCode = byCode;
    this.adjacency = adjacency;
  }

  public static CountryGraph from(CountryDataset dataset) {
    if (dataset.countries().isEmpty()) {
      throw new IllegalStateException("Country dataset is empty");
    }
    dataset.countries().forEach(CountryGraph::validateCountryCode);

    Map<String, Country> byCode = indexByCca3(dataset);
    Map<String, Set<String>> adjacency = buildAdjacency(byCode, dataset);
    return new CountryGraph(Map.copyOf(byCode), freezeAdjacency(adjacency));
  }

  private static Map<String, Country> indexByCca3(CountryDataset dataset) {
    return dataset.countries().stream()
        .collect(
            Collectors.toMap(
                Country::cca3,
                Function.identity(),
                (a, b) -> {
                  throw new IllegalStateException("Duplicate country cca3 in dataset: " + a.cca3());
                }));
  }

  private static Map<String, Set<String>> buildAdjacency(
      Map<String, Country> byCode, CountryDataset dataset) {
    Map<String, Set<String>> adjacency = new HashMap<>();
    for (Country c : dataset.countries()) {
      adjacency.put(c.cca3(), new HashSet<>());
    }

    int orphanRefs = 0;
    int asymmetric = 0;
    for (Country c : dataset.countries()) {
      for (String neighbor : c.borders()) {
        if (!byCode.containsKey(neighbor)) {
          log.warn("Country {} references unknown cca3 in borders: {}", c.cca3(), neighbor);
          orphanRefs++;
          continue;
        }
        adjacency.get(c.cca3()).add(neighbor);
        if (!byCode.get(neighbor).borders().contains(c.cca3())) {
          asymmetric++;
        }
        adjacency.get(neighbor).add(c.cca3());
      }
    }

    if (orphanRefs > 0) {
      log.warn("Skipped {} border references to unknown cca3 codes", orphanRefs);
    }
    if (asymmetric > 0) {
      log.warn("Detected {} asymmetric border declarations (treated as undirected)", asymmetric);
    }
    return adjacency;
  }

  private static Map<String, List<String>> freezeAdjacency(Map<String, Set<String>> adjacency) {
    return adjacency.entrySet().stream()
        .collect(
            Collectors.toUnmodifiableMap(
                Map.Entry::getKey, e -> e.getValue().stream().sorted().toList()));
  }

  private static void validateCountryCode(Country country) {
    if (!CCA3_PATTERN.matcher(country.cca3()).matches()) {
      throw new IllegalStateException("Invalid country cca3 in dataset: " + country.cca3());
    }
  }

  public List<String> neighbors(String cca3) {
    return adjacency.getOrDefault(cca3, List.of());
  }

  public boolean contains(String cca3) {
    return byCode.containsKey(cca3);
  }

  public int size() {
    return byCode.size();
  }
}
