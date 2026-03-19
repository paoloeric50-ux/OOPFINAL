package com.motorph.payroll.repository;

import com.motorph.payroll.document.UserDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDocument, String> {

    Optional<UserDocument> findByEmail(String email);
}
