package com.taskmanagement.controller;

import com.taskmanagement.dto.EpicDTO;
import com.taskmanagement.model.Epic;
import com.taskmanagement.service.EpicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/epics")
public class EpicController {
    // general epics controller

    private final EpicService epicService;

    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    // only owners and admins can create epics, call on epic service
    @PostMapping
    @PreAuthorize("hasRole('PRODUCT_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Epic> createEpic(@RequestBody EpicDTO epicDTO) {
        Epic createdEpic = epicService.createEpic(epicDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEpic);
    }

    @GetMapping
    public ResponseEntity<List<EpicDTO>> getAllEpics() {
        List<EpicDTO> epics = epicService.getAllEpicsWithOwner();
        return ResponseEntity.ok(epics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Epic> getEpicById(@PathVariable Integer id) {
        return epicService.getEpicById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // updating epics can only be done by admins, scrum masters or owners
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SCRUM_MASTER') or @epicService.isOwner(#id, authentication.principal.username)")
    public ResponseEntity<Epic> updateEpic(@PathVariable Integer id, @RequestBody EpicDTO epicDTO) {
        Epic updatedEpic = epicService.updateEpic(id, epicDTO);
        return ResponseEntity.ok(updatedEpic);
    }

    // delete requires admin or owner
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @epicService.isOwner(#id, authentication.principal.username)")
    public ResponseEntity<Void> deleteEpic(@PathVariable Integer id) {
        epicService.deleteEpic(id);
        return ResponseEntity.noContent().build();
    }
}
