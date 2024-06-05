package com.test.springboot.test.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class UsersRepositoryTest {

  @Autowired
  UsersRepository usersRepository;

  @Autowired
  TestEntityManager entityManager;

  @BeforeEach
  void setUp() {
    UserEntity entity = new UserEntity();
    entity.setFirstName("John");
    entity.setLastName("Doe");
    entity.setEmail("test@gmail.com");
    entity.setEncryptedPassword("123");
    entity.setUserId(UUID.randomUUID().toString());
    entityManager.persistAndFlush(entity);
  }

  @Test
  void testFindByEmail() {
    //act
    UserEntity userEntity = usersRepository.findByEmail("test@gmail.com");
    //assert
    assertEquals("John", userEntity.getFirstName());

  }

}
