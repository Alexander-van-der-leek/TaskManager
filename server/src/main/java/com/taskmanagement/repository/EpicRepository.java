package com.taskmanagement.repository;

import com.taskmanagement.dto.EpicDTO;
import com.taskmanagement.model.Epic;
import com.taskmanagement.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EpicRepository extends JpaRepository<Epic, Integer> {
    List<Epic> findByOwner(User owner);

    @Query("SELECT new com.taskmanagement.dto.EpicDTO(e.id, e.name, e.owner.name) FROM Epic e")
    List<EpicDTO> findAllWithOwner();

    @Query("SELECT e FROM Epic e WHERE LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Epic> findByNameContainingIgnoreCase(@Param("name") String name);
}
