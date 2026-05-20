package hr.oblivion.countryroute.routing;

import hr.oblivion.countryroute.data.CountryGraph;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StubRouteFinder implements RouteFinder {

    private final CountryGraph graph;

    public StubRouteFinder(CountryGraph graph) {
        this.graph = graph;
    }

    @Override
    public Optional<List<String>> find(String origin, String destination) {
        if (origin.equals(destination)) {
            return Optional.of(List.of(origin));
        }
        if (graph.neighbors(origin).contains(destination)) {
            return Optional.of(List.of(origin, destination));
        }
        return Optional.empty();
    }
}
