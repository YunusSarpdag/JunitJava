package com.test.springboot.test.io;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserEntityIntegrationTest {

  // alternative of entity manager
  @Autowired
  TestEntityManager entityManager;

  @Test
  void testUserEntity_whenValidUserProvide() {
    //arrange
    UserEntity entity = new UserEntity();
    entity.setFirstName("John");
    entity.setLastName("Doe");
    entity.setEmail("test@gmail.com");
    entity.setEncryptedPassword("123");
    entity.setUserId(UUID.randomUUID().toString());

    //act
    UserEntity storedUser = entityManager.persistAndFlush(entity);

    //assert
    assertTrue(storedUser.getId() > 0);
  }

  @Test
  void testUserEntity_whenInvalidUserProvide() {
    UserEntity entity = new UserEntity();
    entity.setFirstName("JohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohnJohn");
    entity.setLastName("Doe");
    entity.setEmail("test@gmail.com");
    entity.setEncryptedPassword("123");
    entity.setUserId(UUID.randomUUID().toString());

    //act and assert
    assertThrows(PersistenceException.class, () -> entityManager.persistAndFlush(entity));
  }
}
