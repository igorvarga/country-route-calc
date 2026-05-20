package hr.oblivion.countryroute.routing;

import java.util.List;
import java.util.Optional;

public interface RouteFinder {
  Optional<List<String>> find(String origin, String destination);
}
