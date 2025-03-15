package com.taskmanagement.controller;

import com.taskmanagement.dto.EpicDTO;
import com.taskmanagement.model.Epic;
import com.taskmanagement.service.EpicService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/epics")
public class EpicController {
    private final EpicService epicService;

    public EpicController(EpicService epicService) {
        this.epicService = epicService;
    }

    @PostMapping
    public ResponseEntity<Epic> createEpic(@RequestBody EpicDTO epicDTO) {
        Epic createdEpic = epicService.createEpic(epicDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEpic);
    }

    @GetMapping
    public ResponseEntity<List<Epic>> getAllEpics() {
        List<Epic> epics = epicService.getAllEpics();
        return ResponseEntity.ok(epics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Epic> getEpicById(@PathVariable Integer id) {
        return epicService.getEpicById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Epic> updateEpic(@PathVariable Integer id, @RequestBody EpicDTO epicDTO) {
        Epic updatedEpic = epicService.updateEpic(id, epicDTO);
        return ResponseEntity.ok(updatedEpic);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEpic(@PathVariable Integer id) {
        epicService.deleteEpic(id);
        return ResponseEntity.noContent().build();
    }
}
