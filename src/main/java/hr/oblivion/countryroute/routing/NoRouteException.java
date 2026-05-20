package hr.oblivion.countryroute.routing;

public class NoRouteException extends RuntimeException {

    private final String origin;
    private final String destination;

    public NoRouteException(String origin, String destination) {
        super("No land route from " + origin + " to " + destination);
        this.origin = origin;
        this.destination = destination;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }
}
