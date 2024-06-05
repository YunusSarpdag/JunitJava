package com.test.springboot.test.ui.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.springboot.test.service.UsersService;
import com.test.springboot.test.shared.UserDto;
import com.test.springboot.test.ui.request.UserDetailsRequestModel;
import com.test.springboot.test.ui.response.UserRest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

//this annotations will only load the web layer not create bean for data layer and service layer
//controllers we can load specific controller not all them
@WebMvcTest(controllers = UsersController.class,
excludeAutoConfiguration = {SecurityAutoConfiguration.class})
// disable filters for test controllers without filters
//@AutoConfigureMockMvc
public class UsersControllersWebLayerTest {
  @Autowired
  MockMvc mockMvc;

  // create a mock service which implementation of interface for usersService
  // if there are more than one implementation of the interface then we need to specify with Qualifier annotation
  // Mock annotation create a bean but it will not put mock object in the application context, MockBean will put mock object in the application context
  @MockBean
  //@Qualifier("usersServiceImpl")
  UsersService usersService;

  @Test
  @DisplayName("User can be created")
  void testCreateUser_whenValidUserProvided() throws Exception {
    // arrange
    UserDetailsRequestModel userDetailsRequestModel = new UserDetailsRequestModel();
    userDetailsRequestModel.setFirstName("John");
    userDetailsRequestModel.setLastName("Doe");
    userDetailsRequestModel.setEmail("email@test.com");
    userDetailsRequestModel.setPassword("password");
    userDetailsRequestModel.setRepeatPassword("password");

    UserDto userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
    userDto.setUserId(UUID.randomUUID().toString());
    when(usersService.createUser(any())).thenReturn(userDto);

    // for mock the http request
    // ObjectMapper is used to convert object to json
    RequestBuilder builder = MockMvcRequestBuilders.post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsBytes(userDetailsRequestModel));
    //act
    MvcResult result =  mockMvc.perform(builder).andReturn();
    String responseBody = result.getResponse().getContentAsString();
    UserRest createdUser = new ObjectMapper().readValue(responseBody, UserRest.class);
    //assert
    assertEquals(userDetailsRequestModel.getFirstName(), createdUser.getFirstName(), "First name is not correct");
    assertEquals(userDetailsRequestModel.getLastName(), createdUser.getLastName(), "Last name is not correct");
    assertNotNull(createdUser.getUserId(), "User id is null");
  }


  // we need to add test for filter and validations, we need to be sure validation accidentally removed
  @Test
  @DisplayName("First name must not be less than 2 characters")
  void testCreateUser_whenFirstNameLessThan2Characters() throws Exception {
    // arrange
    UserDetailsRequestModel userDetailsRequestModel = new UserDetailsRequestModel();
    userDetailsRequestModel.setFirstName("J");
    userDetailsRequestModel.setLastName("Doe");
    userDetailsRequestModel.setEmail("email@test.com");
    userDetailsRequestModel.setPassword("password");
    userDetailsRequestModel.setRepeatPassword("password");

    RequestBuilder builder = MockMvcRequestBuilders.post("/users").contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).content(new ObjectMapper().writeValueAsBytes(userDetailsRequestModel));

    //act
    MvcResult result = mockMvc.perform(builder).andReturn();

    //assert
    assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus(), "Status code is not correct");
  }

}
