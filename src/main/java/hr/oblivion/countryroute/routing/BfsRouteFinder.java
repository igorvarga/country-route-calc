package hr.oblivion.countryroute.routing;

import hr.oblivion.countryroute.data.CountryGraph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component("bfs")
public class BfsRouteFinder implements RouteFinder {

  private final CountryGraph graph;

  public BfsRouteFinder(CountryGraph graph) {
    this.graph = graph;
  }

  @Override
  public Optional<List<String>> find(String origin, String destination) {
    if (!graph.contains(origin) || !graph.contains(destination)) {
      return Optional.empty();
    }
    if (origin.equals(destination)) {
      return Optional.of(List.of(origin));
    }

    Queue<String> queue = new ArrayDeque<>();
    Set<String> visited = new HashSet<>();
    Map<String, String> predecessor = new HashMap<>();

    queue.add(origin);
    visited.add(origin);

    while (!queue.isEmpty()) {
      String current = queue.remove();
      for (String neighbor : graph.neighbors(current)) {
        if (!visited.add(neighbor)) {
          continue;
        }
        predecessor.put(neighbor, current);
        if (neighbor.equals(destination)) {
          return Optional.of(reconstruct(predecessor, origin, destination));
        }
        queue.add(neighbor);
      }
    }

    return Optional.empty();
  }

  private List<String> reconstruct(
      Map<String, String> predecessor, String origin, String destination) {
    List<String> path = new ArrayList<>();
    String current = destination;
    while (!current.equals(origin)) {
      path.add(current);
      current = predecessor.get(current);
    }
    path.add(origin);
    java.util.Collections.reverse(path);
    return path;
  }
}
