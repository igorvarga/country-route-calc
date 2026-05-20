package hr.oblivion.countryroute.routing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class RoutingControllerTest {

  @Autowired private MockMvc mvc;

  @Autowired private RouteFinder routeFinder;

  @Test
  void springUsesBfsRouteFinderByDefault() {
    assertThat(routeFinder).isInstanceOf(BfsRouteFinder.class);
  }

  @Test
  void specExampleCzeToItaReturnsThreeStepRouteViaAustria() throws Exception {
    mvc.perform(get("/v1/routes/CZE/ITA"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.origin").value("CZE"))
        .andExpect(jsonPath("$.destination").value("ITA"))
        .andExpect(jsonPath("$.steps").value(2))
        .andExpect(jsonPath("$.route", hasSize(3)))
        .andExpect(jsonPath("$.route[0]").value("CZE"))
        .andExpect(jsonPath("$.route[1]").value("AUT"))
        .andExpect(jsonPath("$.route[2]").value("ITA"));
  }

  @Test
  void directBorderReturns200WithTwoStepRoute() throws Exception {
    mvc.perform(get("/v1/routes/CZE/AUT"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.steps").value(1))
        .andExpect(jsonPath("$.route", hasSize(2)))
        .andExpect(jsonPath("$.route[0]").value("CZE"))
        .andExpect(jsonPath("$.route[1]").value("AUT"));
  }

  @Test
  void originEqualsDestinationReturns200WithSingletonRoute() throws Exception {
    mvc.perform(get("/v1/routes/CZE/CZE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.steps").value(0))
        .andExpect(jsonPath("$.route", hasSize(1)))
        .andExpect(jsonPath("$.route[0]").value("CZE"));
  }

  @Test
  void noLandRouteReturns404WithProblemDetail() throws Exception {
    mvc.perform(get("/v1/routes/ISL/DEU"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("urn:country-route-calc:errors:no-land-route"))
        .andExpect(jsonPath("$.origin").value("ISL"))
        .andExpect(jsonPath("$.destination").value("DEU"));
  }

  @Test
  void unknownOriginReturns404WithProblemDetail() throws Exception {
    mvc.perform(get("/v1/routes/ZZZ/ITA"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("urn:country-route-calc:errors:unknown-country-code"))
        .andExpect(jsonPath("$.value").value("ZZZ"))
        .andExpect(jsonPath("$.field").value("origin"));
  }

  @Test
  void unknownDestinationReturns404WithProblemDetail() throws Exception {
    mvc.perform(get("/v1/routes/CZE/ZZZ"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.type").value("urn:country-route-calc:errors:unknown-country-code"))
        .andExpect(jsonPath("$.value").value("ZZZ"))
        .andExpect(jsonPath("$.field").value("destination"));
  }

  @Test
  void lowercaseInputIsNormalizedToUppercase() throws Exception {
    mvc.perform(get("/v1/routes/cze/aut"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.origin").value("CZE"))
        .andExpect(jsonPath("$.destination").value("AUT"))
        .andExpect(jsonPath("$.route[0]").value("CZE"))
        .andExpect(jsonPath("$.route[1]").value("AUT"));
  }

  @Test
  void malformedCca3Returns400WithProblemDetail() throws Exception {
    mvc.perform(get("/v1/routes/CZ/ITA"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.type").value("urn:country-route-calc:errors:invalid-country-code"))
        .andExpect(jsonPath("$.value").value("CZ"))
        .andExpect(jsonPath("$.field").value("origin"));
  }
}
