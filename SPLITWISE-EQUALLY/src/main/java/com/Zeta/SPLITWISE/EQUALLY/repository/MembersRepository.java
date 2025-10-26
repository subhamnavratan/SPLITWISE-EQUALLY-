package com.Zeta.SPLITWISE.EQUALLY.repository;

import com.Zeta.SPLITWISE.EQUALLY.model.Members;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface MembersRepository extends MongoRepository<Members, String> {
    Optional<Members> findByGroupName(String groupName);
}