package com.test.springboot.test.ui.controllers;

import com.test.springboot.test.security.SecurityConstants;
import com.test.springboot.test.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

// Integration test for UsersController, annotation creates application context and injects all beans
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
// for overriding properties in application.properties
@TestPropertySource(locations = "/application-test.properties", properties = "server.port=8083")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// for creating single instance of test class and share it across all test methods jwttoken will be empty other wise
// other methods
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UsersControllerIntegrationTest {

  @Value("${server.port}")
  private int serverPort;

  @Autowired
  private TestRestTemplate testRestTemplate;

  private String jwtToken;

  @Test
  void loadContext() {
    assertEquals(8083, serverPort);
  }

  @Test
  @DisplayName("User can be created")
  @Order(1)
  void testCreateUser_whenValidUserProvided() throws JSONException {
    // arrange
    JSONObject userDetailsRequestModel = new JSONObject();
    userDetailsRequestModel.put("firstName", "John");
    userDetailsRequestModel.put("lastName", "Doe");
    userDetailsRequestModel.put("email", "email@test.com");
    userDetailsRequestModel.put("password", "password");
    userDetailsRequestModel.put("repeatPassword", "password");

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    HttpEntity<String> request = new HttpEntity<>(userDetailsRequestModel.toString(), headers);

    // act
    ResponseEntity<UserRest> response = testRestTemplate.postForEntity("/users", request, UserRest.class);
    UserRest userRest = response.getBody();
    // assert
    assertEquals(200, response.getStatusCodeValue());
    assertEquals(userDetailsRequestModel.get("firstName"), userRest.getFirstName());
  }

  @Test
  @DisplayName("/GET /users required JWT token")
  @Order(2)
  void testGetUsers_whenNoTokenProvided() {
    // arrange
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

    HttpEntity<String> request = new HttpEntity<>(headers);

    // act
    ResponseEntity<String> response = testRestTemplate.exchange("/users", HttpMethod.GET, request, String.class);
    // assert
    assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCodeValue());
  }

  @Test
  @DisplayName("/login works")
  @Order(3)
  void testUserLogin_whenValidCredentialProvided() throws JSONException {
    // arrange
    JSONObject loginRequestModel = new JSONObject();
    loginRequestModel.put("email", "email@test.com");
    loginRequestModel.put("password", "password");

    HttpEntity<String> request = new HttpEntity<>(loginRequestModel.toString());

    // act
    ResponseEntity response = testRestTemplate.postForEntity("/users/login", request, null);

    jwtToken = response.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

    // assert
    assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());

  }

  @Test
  @DisplayName("/GET /users required JWT token")
  void testGetUser_whenValidJWTProvider() {
    // arrange
    HttpHeaders headers = new HttpHeaders();
    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    headers.setBearerAuth(jwtToken);

    HttpEntity request = new HttpEntity(headers);

    // act
    ResponseEntity<String> response = testRestTemplate.exchange("/users", HttpMethod.GET, request, String.class);

    //assert
    assertEquals(HttpStatus.OK.value(), response.getStatusCodeValue());

  }

}
