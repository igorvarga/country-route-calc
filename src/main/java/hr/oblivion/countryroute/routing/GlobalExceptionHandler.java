package hr.oblivion.countryroute.routing;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.net.URI;
import java.util.Iterator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleInvalidCode(ConstraintViolationException ex) {
    Iterator<ConstraintViolation<?>> it = ex.getConstraintViolations().iterator();
    ConstraintViolation<?> first = it.next();
    String field = lastPathSegment(first.getPropertyPath().toString());
    String value = String.valueOf(first.getInvalidValue());
    ProblemDetail pd =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid country code: " + value);
    pd.setType(URI.create("urn:country-route-calc:errors:invalid-country-code"));
    pd.setTitle("Invalid country code");
    pd.setProperty("value", value);
    pd.setProperty("field", field);
    return pd;
  }

  @ExceptionHandler(UnknownCountryException.class)
  public ProblemDetail handleUnknown(UnknownCountryException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("urn:country-route-calc:errors:unknown-country-code"));
    pd.setTitle("Unknown country code");
    pd.setProperty("value", ex.getCode());
    pd.setProperty("field", ex.getField());
    return pd;
  }

  @ExceptionHandler(NoRouteException.class)
  public ProblemDetail handleNoRoute(NoRouteException ex) {
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    pd.setType(URI.create("urn:country-route-calc:errors:no-land-route"));
    pd.setTitle("No land route");
    pd.setProperty("origin", ex.getOrigin());
    pd.setProperty("destination", ex.getDestination());
    return pd;
  }

  private static String lastPathSegment(String propertyPath) {
    return propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
  }
}
