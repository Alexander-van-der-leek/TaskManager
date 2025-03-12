package com.taskmanagement.repository;

import com.taskmanagement.model.Epic;
import com.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EpicRepository extends JpaRepository<Epic, UUID> {
    List<Epic> findByOwner(User owner);
    List<Epic> findByName(String name);
}
