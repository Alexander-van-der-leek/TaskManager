package com.taskmanagement.service;

import com.taskmanagement.dto.SprintDTO;
import com.taskmanagement.exception.SprintNotFoundException;
import com.taskmanagement.exception.UserNotFoundException;
import com.taskmanagement.model.Sprint;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.SprintRepository;
import com.taskmanagement.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SprintService sprintService;

    private Sprint sprint;
    private SprintDTO sprintDTO;
    private User scrumMaster;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID scrumMasterId = UUID.randomUUID();
        scrumMaster = new User();
        scrumMaster.setId(scrumMasterId);
        scrumMaster.setName("Scrum Master");

        sprintDTO = new SprintDTO();
        sprintDTO.setName("Sprint 1");
        sprintDTO.setGoal("Complete important tasks");
        sprintDTO.setScrumMasterId(scrumMasterId);
        sprintDTO.setCapacityPoints(50);
        sprintDTO.setStartDate(ZonedDateTime.now());
        sprintDTO.setEndDate(ZonedDateTime.now().plusWeeks(2));
        sprintDTO.setActive(false);

        sprint = new Sprint();
        sprint.setId(1);
        sprint.setName(sprintDTO.getName());
        sprint.setGoal(sprintDTO.getGoal());
        sprint.setScrumMaster(scrumMaster);
        sprint.setCapacityPoints(sprintDTO.getCapacityPoints());
        sprint.setStartDate(sprintDTO.getStartDate());
        sprint.setEndDate(sprintDTO.getEndDate());
        sprint.setActive(sprintDTO.isActive());
        sprint.setCreatedAt(ZonedDateTime.now());
        sprint.setUpdatedAt(ZonedDateTime.now());

        when(userRepository.findById(scrumMasterId)).thenReturn(Optional.of(scrumMaster));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
        when(sprintRepository.findById(1)).thenReturn(Optional.of(sprint));
        when(sprintRepository.findAll()).thenReturn(List.of(sprint));
    }

    @Test
    public void testCreateSprint_Success() {
        SprintDTO createdSprint = sprintService.createSprint(sprintDTO);

        assertNotNull(createdSprint);
        assertEquals(sprintDTO.getName(), createdSprint.getName());
        assertEquals(sprintDTO.getGoal(), createdSprint.getGoal());
        assertEquals(scrumMaster.getId(), createdSprint.getScrumMasterId());
    }

    @Test
    public void testGetSprintById_Success() {
        SprintDTO fetchedSprint = sprintService.getSprintById(1);

        assertNotNull(fetchedSprint);
        assertEquals(sprint.getName(), fetchedSprint.getName());
    }

    @Test
    public void testGetAllSprints() {
        List<SprintDTO> sprints = sprintService.getAllSprints();

        assertNotNull(sprints);
        assertEquals(1, sprints.size());
    }

    @Test
    public void testUpdateSprint_Success() {
        SprintDTO updatedSprintDTO = new SprintDTO();
        updatedSprintDTO.setName("Updated Sprint");
        updatedSprintDTO.setGoal("Updated goal");
        updatedSprintDTO.setScrumMasterId(scrumMaster.getId());
        updatedSprintDTO.setCapacityPoints(100);
        updatedSprintDTO.setStartDate(ZonedDateTime.now());
        updatedSprintDTO.setEndDate(ZonedDateTime.now().plusWeeks(4));
        updatedSprintDTO.setActive(true);

        SprintDTO updatedSprint = sprintService.updateSprint(1, updatedSprintDTO);

        assertNotNull(updatedSprint);
        assertEquals(updatedSprintDTO.getName(), updatedSprint.getName());
        assertEquals(updatedSprintDTO.getGoal(), updatedSprint.getGoal());
    }

    @Test
    public void testStartSprint_Success() {
        SprintDTO startedSprint = sprintService.startSprint(1);

        assertNotNull(startedSprint);
        assertTrue(startedSprint.isActive());
    }

    @Test
    public void testEndSprint_Success() {
        SprintDTO endedSprint = sprintService.endSprint(1);

        assertNotNull(endedSprint);
        assertFalse(endedSprint.isActive());
    }

    @Test
    public void testCreateSprint_UserNotFound() {
        UUID invalidUserId = UUID.randomUUID();
        sprintDTO.setScrumMasterId(invalidUserId);
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> sprintService.createSprint(sprintDTO));
    }

    @Test
    public void testGetSprintById_NotFound() {
        when(sprintRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(SprintNotFoundException.class, () -> sprintService.getSprintById(99));
    }

    @Test
    public void testUpdateSprint_NotFound() {
        SprintDTO updatedSprintDTO = new SprintDTO();
        updatedSprintDTO.setName("Non-existing Sprint");
        when(sprintRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(SprintNotFoundException.class, () -> sprintService.updateSprint(99, updatedSprintDTO));
    }

    @Test
    public void testDeleteSprint_NotFound() {
        when(sprintRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(SprintNotFoundException.class, () -> sprintService.deleteSprint(99));
    }
}
