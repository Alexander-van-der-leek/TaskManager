package com.taskmanagement.repository;

import java.util.List;
import java.util.Optional;

import com.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.taskmanagement.model.Sprint;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    Optional<Sprint> findByScrumMaster(User scrumMasterName);

}