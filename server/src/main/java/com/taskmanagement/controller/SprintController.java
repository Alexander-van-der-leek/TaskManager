package com.taskmanagement.controller;

import com.taskmanagement.dto.SprintDTO;
import com.taskmanagement.service.SprintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<SprintDTO> createSprint(@RequestBody SprintDTO sprintDTO) {
        SprintDTO createdSprint = sprintService.createSprint(sprintDTO);
        return ResponseEntity.ok(createdSprint);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SprintDTO> getSprintById(@PathVariable Integer id) {
        SprintDTO sprint = sprintService.getSprintById(id);
        return ResponseEntity.ok(sprint);
    }

    @GetMapping
    public ResponseEntity<List<SprintDTO>> getAllSprints() {
        List<SprintDTO> sprints = sprintService.getAllSprints();
        return ResponseEntity.ok(sprints);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SprintDTO> updateSprint(@PathVariable Integer id, @RequestBody SprintDTO sprintDTO) {
        SprintDTO updatedSprint = sprintService.updateSprint(id, sprintDTO);
        return ResponseEntity.ok(updatedSprint);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Integer id) {
        sprintService.deleteSprint(id);
        return ResponseEntity.noContent().build();
    }
}
