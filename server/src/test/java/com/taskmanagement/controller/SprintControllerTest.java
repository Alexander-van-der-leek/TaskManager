package com.taskmanagement.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.taskmanagement.dto.SprintDTO;
import com.taskmanagement.service.SprintService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class SprintControllerTest {

    @Mock
    private SprintService sprintService;

    @InjectMocks
    private SprintController sprintController;

    private final Integer sprintId = 1;
    private SprintDTO sprintDTO;

    @BeforeEach
    void setUp() {
        sprintDTO = new SprintDTO();
        sprintDTO.setId(sprintId);
        sprintDTO.setName("Sprint 1");
        sprintDTO.setGoal("Complete feature X");
        sprintDTO.setScrumMasterId(UUID.randomUUID());
        sprintDTO.setCapacityPoints(20);
        sprintDTO.setStartDate(ZonedDateTime.now());
        sprintDTO.setEndDate(ZonedDateTime.now().plusDays(14));
        sprintDTO.setActive(false);
    }

    @Test
    void createSprint_shouldReturnCreatedSprint() {
        when(sprintService.createSprint(sprintDTO)).thenReturn(sprintDTO);

        ResponseEntity<SprintDTO> response = sprintController.createSprint(sprintDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sprintDTO, response.getBody());
    }

    @Test
    void getSprintById_shouldReturnSprint() {
        when(sprintService.getSprintById(sprintId)).thenReturn(sprintDTO);

        ResponseEntity<SprintDTO> response = sprintController.getSprintById(sprintId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sprintDTO, response.getBody());
    }

    @Test
    void getAllSprints_shouldReturnListOfSprints() {
        List<SprintDTO> sprintList = List.of(sprintDTO);
        when(sprintService.getAllSprints()).thenReturn(sprintList);

        ResponseEntity<List<SprintDTO>> response = sprintController.getAllSprints();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sprintList, response.getBody());
    }

    @Test
    void updateSprint_shouldReturnUpdatedSprint() {
        when(sprintService.updateSprint(sprintId, sprintDTO)).thenReturn(sprintDTO);

        ResponseEntity<SprintDTO> response = sprintController.updateSprint(sprintId, sprintDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sprintDTO, response.getBody());
    }

    @Test
    void startSprint_shouldReturnStartedSprint() {
        sprintDTO.setActive(true);
        when(sprintService.startSprint(sprintId)).thenReturn(sprintDTO);

        ResponseEntity<SprintDTO> response = sprintController.startSprint(sprintId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sprintDTO, response.getBody());
    }

    @Test
    void endSprint_shouldReturnEndedSprint() {
        sprintDTO.setActive(false);
        when(sprintService.endSprint(sprintId)).thenReturn(sprintDTO);

        ResponseEntity<SprintDTO> response = sprintController.endSprint(sprintId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sprintDTO, response.getBody());
    }
}