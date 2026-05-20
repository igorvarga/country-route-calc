package hr.oblivion.countryroute.routing;

import hr.oblivion.countryroute.data.CountryGraph;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoutingController {

  private static final Pattern CCA3_PATTERN = Pattern.compile("[A-Z]{3}");

  private final CountryGraph graph;
  private final RouteFinder finder;

  public RoutingController(CountryGraph graph, RouteFinder finder) {
    this.graph = graph;
    this.finder = finder;
  }

  @GetMapping("/routing/{origin}/{destination}")
  public RouteResponse route(@PathVariable String origin, @PathVariable String destination) {
    String o = normalize(origin, "origin");
    String d = normalize(destination, "destination");

    if (!graph.contains(o)) {
      throw new UnknownCountryException(o, "origin");
    }
    if (!graph.contains(d)) {
      throw new UnknownCountryException(d, "destination");
    }

    return finder.find(o, d).map(RouteResponse::new).orElseThrow(() -> new NoRouteException(o, d));
  }

  private String normalize(String value, String field) {
    String upper = value.toUpperCase(Locale.ROOT);
    if (!CCA3_PATTERN.matcher(upper).matches()) {
      throw new InvalidCountryCodeException(value, field);
    }
    return upper;
  }
}
