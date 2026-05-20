package hr.oblivion.countryroute.routing;

public class UnknownCountryException extends RuntimeException {

  private final String code;
  private final String field;

  public UnknownCountryException(String code, String field) {
    super("Unknown country code: " + code);
    this.code = code;
    this.field = field;
  }

  public String getCode() {
    return code;
  }

  public String getField() {
    return field;
  }
}
