package com.Zeta.SPLITWISE.EQUALLY.repository;

import com.Zeta.SPLITWISE.EQUALLY.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface  UserRepository extends MongoRepository<User ,String > {
    Optional<User> findByPhone(Long phone);
    Optional<User> findByEmail(String email);
}