package com.taskmanagement.repository;

import com.taskmanagement.model.Epic;
import com.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Integer> {
    List<Epic> findByOwner(User owner);
    List<Epic> findByNameIgnoreCase(String name);
}
