package com.taskmanagement.controller;

import com.taskmanagement.dto.SprintDTO;
import com.taskmanagement.service.SprintService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    // need scrum master to create sprints
    @PostMapping
    @PreAuthorize("hasRole('SCRUM_MASTER')")
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

    @PostMapping("/{sprintId}/start")
    public ResponseEntity<SprintDTO> startSprint(@PathVariable Integer sprintId) {
        SprintDTO sprintDTO = sprintService.startSprint(sprintId);
        return ResponseEntity.ok(sprintDTO);
    }

    @PostMapping("/{sprintId}/end")
    public ResponseEntity<SprintDTO> endSprint(@PathVariable Integer sprintId) {
        SprintDTO sprintDTO = sprintService.endSprint(sprintId);
        return ResponseEntity.ok(sprintDTO);
    }
}
