package com.Zeta.SPLITWISE.EQUALLY.repository;

import com.Zeta.SPLITWISE.EQUALLY.model.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    Optional<Group> findByGroupName(String groupName);
    List<Group> findByMemberUserIdsContaining(String userId);
}