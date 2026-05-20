package hr.oblivion.countryroute.routing;

import hr.oblivion.countryroute.data.Country;
import hr.oblivion.countryroute.data.CountryGraph;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

@Component("dijkstra")
public class DijkstraRouteFinder implements RouteFinder {

    private final CountryGraph graph;
    private final DistanceCalculator distanceCalculator;

    public DijkstraRouteFinder(CountryGraph graph, DistanceCalculator distanceCalculator) {
        this.graph = graph;
        this.distanceCalculator = distanceCalculator;
    }

    @Override
    public Optional<List<String>> find(String origin, String destination) {
        if (origin.equals(destination)) {
            return Optional.of(List.of(origin));
        }

        Map<String, Double> distance = new HashMap<>();
        Map<String, String> predecessor = new HashMap<>();
        PriorityQueue<QueueEntry> queue = new PriorityQueue<>(Comparator.comparingDouble(QueueEntry::dist));

        distance.put(origin, 0.0);
        queue.add(new QueueEntry(0.0, origin));

        while (!queue.isEmpty()) {
            QueueEntry entry = queue.poll();
            String current = entry.code();
            if (entry.dist() > distance.getOrDefault(current, Double.POSITIVE_INFINITY)) {
                continue;
            }
            if (current.equals(destination)) {
                return Optional.of(reconstruct(predecessor, origin, destination));
            }
            for (String neighbor : graph.neighbors(current)) {
                double newDist = entry.dist() + weight(current, neighbor);
                if (newDist < distance.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distance.put(neighbor, newDist);
                    predecessor.put(neighbor, current);
                    queue.add(new QueueEntry(newDist, neighbor));
                }
            }
        }
        return Optional.empty();
    }

    private double weight(String a, String b) {
        Country ca = graph.country(a);
        Country cb = graph.country(b);
        return distanceCalculator.distanceKm(ca.latlng(), cb.latlng());
    }

    private List<String> reconstruct(Map<String, String> predecessor, String origin, String destination) {
        List<String> path = new ArrayList<>();
        String current = destination;
        while (current != null && !current.equals(origin)) {
            path.add(current);
            current = predecessor.get(current);
        }
        path.add(origin);
        java.util.Collections.reverse(path);
        return path;
    }

    private record QueueEntry(double dist, String code) {}
}
