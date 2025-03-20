package com.taskmanagement.service;

import com.taskmanagement.exception.EpicNotFoundException;
import com.taskmanagement.model.Epic;
import com.taskmanagement.dto.EpicDTO;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.EpicRepository;
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

public class EpicServiceTest {

    @Mock
    private EpicRepository epicRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EpicService epicService;

    private Epic epic;
    private EpicDTO epicDTO;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        UUID userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        user.setName("User Name");

        epicDTO = new EpicDTO();
        epicDTO.setName("Epic 1");
        epicDTO.setDescription("Epic 1 description");
        epicDTO.setOwnerId(userId);
        epicDTO.setStoryPoints(10);
        epicDTO.setStartDate(ZonedDateTime.now());
        epicDTO.setTargetEndDate(ZonedDateTime.now().plusDays(30));

        epic = new Epic();
        epic.setId(1);
        epic.setName(epicDTO.getName());
        epic.setDescription(epicDTO.getDescription());
        epic.setOwner(user);
        epic.setStoryPoints(epicDTO.getStoryPoints());
        epic.setStartDate(epicDTO.getStartDate());
        epic.setTargetEndDate(epicDTO.getTargetEndDate());
        epic.setCreatedAt(ZonedDateTime.now());
        epic.setUpdatedAt(ZonedDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(epicRepository.save(any(Epic.class))).thenReturn(epic);
        when(epicRepository.findById(1)).thenReturn(Optional.of(epic));
        when(epicRepository.findAll()).thenReturn(List.of(epic));
        when(epicRepository.findAllWithOwner()).thenReturn(List.of(new EpicDTO()));
    }

    @Test
    public void testCreateEpic_Success() {
        Epic createdEpic = epicService.createEpic(epicDTO);

        assertNotNull(createdEpic);
        assertEquals(epicDTO.getName(), createdEpic.getName());
        assertEquals(epicDTO.getDescription(), createdEpic.getDescription());
        assertEquals(user, createdEpic.getOwner());
        assertEquals(epicDTO.getStoryPoints(), createdEpic.getStoryPoints());
    }

    @Test
    public void testUpdateEpic_Success() {
        EpicDTO updatedEpicDTO = new EpicDTO();
        updatedEpicDTO.setName("Updated Epic");
        updatedEpicDTO.setDescription("Updated description");
        updatedEpicDTO.setStoryPoints(15);
        updatedEpicDTO.setStartDate(ZonedDateTime.now());
        updatedEpicDTO.setTargetEndDate(ZonedDateTime.now().plusDays(45));

        Epic updatedEpic = epicService.updateEpic(1, updatedEpicDTO);

        assertNotNull(updatedEpic);
        assertEquals(updatedEpicDTO.getName(), updatedEpic.getName());
        assertEquals(updatedEpicDTO.getDescription(), updatedEpic.getDescription());
        assertEquals(updatedEpicDTO.getStoryPoints(), updatedEpic.getStoryPoints());
    }

    @Test
    public void testDeleteEpic_Success() {
        epicService.deleteEpic(1);

        verify(epicRepository, times(1)).deleteById(1);
    }

    @Test
    public void testGetAllEpics() {
        List<Epic> epics = epicService.getAllEpics();

        assertNotNull(epics);
        assertEquals(1, epics.size());
        assertEquals(epic.getName(), epics.get(0).getName());
    }

    @Test
    public void testGetAllEpicsWithOwner() {
        List<EpicDTO> epics = epicService.getAllEpicsWithOwner();

        assertNotNull(epics);
        assertEquals(1, epics.size());
    }

    @Test
    public void testGetEpicById() {
        Optional<Epic> fetchedEpic = epicService.getEpicById(1);

        assertTrue(fetchedEpic.isPresent());
        assertEquals(epic.getName(), fetchedEpic.get().getName());
    }

    @Test
    public void testCreateEpic_UserNotFound() {
        UUID invalidUserId = UUID.randomUUID();
        epicDTO.setOwnerId(invalidUserId);

        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        assertThrows(EpicNotFoundException.class, () -> epicService.createEpic(epicDTO));
    }

    @Test
    public void testUpdateEpic_NotFound() {
        EpicDTO updatedEpicDTO = new EpicDTO();
        updatedEpicDTO.setName("Updated Epic");
        updatedEpicDTO.setDescription("Updated description");
        updatedEpicDTO.setStoryPoints(15);

        when(epicRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EpicNotFoundException.class, () -> epicService.updateEpic(1, updatedEpicDTO));
    }

    @Test
    public void testDeleteEpic_NotFound() {
        when(epicRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(EpicNotFoundException.class, () -> epicService.deleteEpic(1));
    }
}

