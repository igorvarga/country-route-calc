package hr.oblivion.countryroute.routing;

public class InvalidCountryCodeException extends RuntimeException {

  private final String value;
  private final String field;

  public InvalidCountryCodeException(String value, String field) {
    super("Invalid country code: " + value);
    this.value = value;
    this.field = field;
  }

  public String getValue() {
    return value;
  }

  public String getField() {
    return field;
  }
}
