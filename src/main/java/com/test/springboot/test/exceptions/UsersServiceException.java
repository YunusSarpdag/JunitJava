package com.test.springboot.test.exceptions;

public class UsersServiceException extends RuntimeException{
  public UsersServiceException(String message)
  {
    super(message);
  }
}
