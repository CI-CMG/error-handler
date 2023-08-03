package gov.noaa.ncei.mgg.errorhandler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class ErrorHandlerTest {

  @Autowired
  private TestRestTemplate restTemplate;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testHandleInternal() throws JsonProcessingException {

    String url = UriComponentsBuilder.fromPath("/api/v1/test/handle-internal")
        .queryParam("parameter", "")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );
    assertEquals(400, response.getStatusCodeValue());

    assertEquals("Bad Request", objectMapper.readTree(response.getBody()).get("flashErrors").get(0).asText());
  }

  @Test
  public void testJsonProcessingError() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/json-exception")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );

    assertEquals(422, response.getStatusCodeValue());

    JsonNode body = objectMapper.readTree(response.getBody());
    JsonNode flashErrors = body.get("flashErrors");
    JsonNode formErrors = body.get("formErrors");
    assertEquals(1, formErrors.size());
    assertEquals("Invalid Type", formErrors.get("").get(0).asText());
    assertEquals(1, flashErrors.size());
    assertEquals("Invalid Request", flashErrors.get(0).asText());
  }

  @Test
  public void testConstraintViolation() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/constraint-violation")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );

    JsonNode body = objectMapper.readTree(response.getBody());
    assertEquals(400, response.getStatusCodeValue());
    assertEquals("Invalid Request", body.get("flashErrors").get(0).asText());
    assertEquals(1, body.get("flashErrors").size());
    assertNotNull(body.get("formErrors").get("TEST"));
    assertEquals(1, body.get("formErrors").size());
    assertNotNull(body.get("formErrors").get("TEST").get(0));
    assertEquals("Test invalid", body.get("formErrors").get("TEST").get(0).asText());
  }

  @Test
  public void testApiException() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/api-exception")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );

    assertEquals(404, response.getStatusCodeValue());
    JsonNode body = objectMapper.readTree(response.getBody());
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Test flash error", flashErrors.get(0).asText());

    JsonNode formErrors = body.get("formErrors");
    assertNotNull(formErrors);
    assertEquals(1, formErrors.size());
    JsonNode testFieldErrors = formErrors.get("TEST");
    assertNotNull(testFieldErrors);
    assertEquals(1, testFieldErrors.size());
    assertEquals("Test field error", testFieldErrors.get(0).asText());

    JsonNode additionalData = body.get("additionalData");
    assertNotNull(additionalData);
    assertEquals(1, additionalData.size());
    JsonNode testAdditionalData = additionalData.get("TEST");
    assertNotNull(testAdditionalData);
    assertEquals("ADDITIONAL_DATA", testAdditionalData.asText());
  }

  @Test
  public void testException() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/exception")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );
    assertEquals(500, response.getStatusCodeValue());

    JsonNode body = objectMapper.readTree(response.getBody());
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Internal Server Error", flashErrors.get(0).asText());

    assertEquals(0, body.get("formErrors").size());
    assertEquals("null", body.get("additionalData").asText());
  }

  @Test
  public void testMediaTypeException() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/media-type-exception")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );
    assertEquals(406, response.getStatusCodeValue());

    JsonNode body = objectMapper.readTree(response.getBody());
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Not Acceptable", flashErrors.get(0).asText());

    assertEquals(0, body.get("formErrors").size());
    assertEquals("null", body.get("additionalData").asText());
  }

  @Test
  public void testHttpMessageNotReadable() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/message-not-readable")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );
    assertEquals(400, response.getStatusCodeValue());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertNotNull(body);
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Bad Request", flashErrors.get(0).asText());

    assertEquals(0, body.get("formErrors").size());
    assertEquals("null", body.get("additionalData").asText());
  }

  @Test
  public void testHttpMessageNotReadableJson() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/message-not-readable-json")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );
    assertEquals(422, response.getStatusCodeValue());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertNotNull(body);
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Invalid Request", flashErrors.get(0).asText());

    assertEquals(1, body.get("formErrors").size());
    assertEquals("Invalid Type", body.get("formErrors").get("[1].TEST").get(0).asText());
    assertEquals("null", body.get("additionalData").asText());
  }

  @Test
  public void testMissingParameter() throws JsonProcessingException {
   String url = UriComponentsBuilder.fromPath("/api/v1/test/missing-parameter")
       .encode().toUriString();

   ResponseEntity<String> response = restTemplate.exchange(
       url,
       HttpMethod.GET,
       new HttpEntity<>(new HttpHeaders()),
       String.class
   );

    assertEquals(400, response.getStatusCodeValue());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertNotNull(body);
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Missing Request Parameter 'TEST'", flashErrors.get(0).asText());

    assertEquals(0, body.get("formErrors").size());
    assertEquals("null", body.get("additionalData").asText());
  }

  @Test
  public void testTypeMismatch() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/type-mismatch")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );

    assertEquals(400, response.getStatusCodeValue());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertNotNull(body);
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Invalid Parameter 'TEST'", flashErrors.get(0).asText());

    assertEquals(0, body.get("formErrors").size());
    assertEquals("null", body.get("additionalData").asText());
  }

  @Test
  public void testTypeMismatchNoPropertyName() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/type-mismatch-no-name")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );

    assertEquals(400, response.getStatusCodeValue());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertNotNull(body);
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Invalid Parameter 'TEST'", flashErrors.get(0).asText());

    assertEquals(0, body.get("formErrors").size());
    assertEquals("null", body.get("additionalData").asText());
  }

  @Test
  public void testHandleMethodArgumentNotValid() throws JsonProcessingException {
    String url = UriComponentsBuilder.fromPath("/api/v1/test/invalid-argument")
        .encode().toUriString();

    ResponseEntity<String> response = restTemplate.exchange(
        url,
        HttpMethod.GET,
        new HttpEntity<>(new HttpHeaders()),
        String.class
    );

    assertEquals(422, response.getStatusCodeValue());
    JsonNode body = objectMapper.readTree(response.getBody());
    assertNotNull(body);
    JsonNode flashErrors = body.get("flashErrors");
    assertNotNull(flashErrors);
    assertEquals(1, flashErrors.size());
    assertEquals("Invalid Request", flashErrors.get(0).asText());

    assertEquals(0, body.get("formErrors").size());
    assertEquals("null", body.get("additionalData").asText());
  }

}
