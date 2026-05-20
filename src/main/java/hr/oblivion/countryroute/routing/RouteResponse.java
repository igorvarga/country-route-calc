package hr.oblivion.countryroute.routing;

import java.util.List;

public record RouteResponse(String origin, String destination, int steps, List<String> route) {

  public static RouteResponse of(String origin, String destination, List<String> route) {
    return new RouteResponse(origin, destination, route.size() - 1, route);
  }
}
