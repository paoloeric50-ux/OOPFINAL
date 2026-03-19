package com.motorph.payroll.repository;

import com.motorph.payroll.document.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserDocument, String> {

    @Query("{ 'email': ?0 }")
    Optional<UserDocument> findByEmail(String email);

    @Query("{ 'id': ?0 }")
    Optional<UserDocument> findByUserId(String userId);
}
