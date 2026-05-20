package hr.oblivion.countryroute.routing;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(InvalidCountryCodeException.class)
  public ProblemDetail handleInvalidCode(InvalidCountryCodeException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setType(URI.create("urn:country-route-calc:errors:invalid-country-code"));
    pd.setTitle("Invalid country code");
    pd.setProperty("value", ex.getValue());
    pd.setProperty("field", ex.getField());
    return pd;
  }

  @ExceptionHandler(UnknownCountryException.class)
  public ProblemDetail handleUnknown(UnknownCountryException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setType(URI.create("urn:country-route-calc:errors:unknown-country-code"));
    pd.setTitle("Unknown country code");
    pd.setProperty("code", ex.getCode());
    pd.setProperty("field", ex.getField());
    return pd;
  }

  @ExceptionHandler(NoRouteException.class)
  public ProblemDetail handleNoRoute(NoRouteException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    pd.setType(URI.create("urn:country-route-calc:errors:no-land-route"));
    pd.setTitle("No land route");
    pd.setProperty("origin", ex.getOrigin());
    pd.setProperty("destination", ex.getDestination());
    return pd;
  }
}
