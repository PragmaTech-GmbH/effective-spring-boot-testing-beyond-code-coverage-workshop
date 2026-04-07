package pragmatech.digital.workshops.lab2.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.springframework.http.MediaType;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;

public class OpenLibraryApiStub {

  private final WireMockServer wireMockServer;

  public OpenLibraryApiStub(WireMockServer wireMockServer) {
    this.wireMockServer = wireMockServer;
  }

  public void stubForSuccessfulBookResponse(String isbn) {
    this.wireMockServer.stubFor(
      WireMock.get("/isbn/" + isbn)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile(isbn + "-success.json")));
  }

  public void stubForSuccessfulBookResponseWithFile(String isbn, String responseFileName) {
    this.wireMockServer.stubFor(
      WireMock.get("/isbn/" + isbn)
        .willReturn(
          aResponse()
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBodyFile(responseFileName + "-success.json")));
  }
}
