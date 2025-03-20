package com.taskmanagement.controller;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.taskmanagement.dto.EpicDTO;
import com.taskmanagement.model.Epic;
import com.taskmanagement.model.User;
import com.taskmanagement.service.EpicService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class EpicControllerTest {

    @Mock
    private EpicService epicService;

    @InjectMocks
    private EpicController epicController;

    private final Integer epicId = 1;
    private Epic epic;
    private EpicDTO epicDTO;

    @BeforeEach
    void setUp() {
        epic = new Epic();
        epic.setId(epicId);
        epic.setName("Test Epic");
        epic.setDescription("This is a test epic");
        epic.setStoryPoints(5);
        epic.setStartDate(ZonedDateTime.now());
        epic.setTargetEndDate(ZonedDateTime.now().plusDays(10));
        epic.setCreatedAt(ZonedDateTime.now());
        epic.setUpdatedAt(ZonedDateTime.now());

        User owner = new User();
        owner.setId(UUID.randomUUID());
        epic.setOwner(owner);

        epicDTO = new EpicDTO();
        epicDTO.setId(epicId);
        epicDTO.setName("Updated Epic");
        epicDTO.setDescription("Updated Description");
        epicDTO.setOwnerId(owner.getId());
        epicDTO.setStoryPoints(8);
        epicDTO.setStartDate(ZonedDateTime.now());
        epicDTO.setTargetEndDate(ZonedDateTime.now().plusDays(15));
    }

    @Test
    void createEpic_shouldReturnCreatedEpic() {
        when(epicService.createEpic(epicDTO)).thenReturn(epic);

        ResponseEntity<Epic> response = epicController.createEpic(epicDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(epic, response.getBody());
    }

    @Test
    void getAllEpics_shouldReturnListOfEpics() {
        List<EpicDTO> epicList = List.of(epicDTO);
        when(epicService.getAllEpicsWithOwner()).thenReturn(epicList);

        ResponseEntity<List<EpicDTO>> response = epicController.getAllEpics();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(epicList, response.getBody());
    }



    @Test
    void getEpicById_whenEpicDoesNotExist_shouldReturnNotFound() {
        when(epicService.getEpicById(epicId)).thenReturn(Optional.empty());

        ResponseEntity<Epic> response = epicController.getEpicById(epicId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void updateEpic_shouldReturnUpdatedEpic() {
        when(epicService.updateEpic(epicId, epicDTO)).thenReturn(epic);

        ResponseEntity<Epic> response = epicController.updateEpic(epicId, epicDTO);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(epic, response.getBody());
    }

    @Test
    void deleteEpic_shouldReturnNoContent() {
        doNothing().when(epicService).deleteEpic(epicId);

        ResponseEntity<Void> response = epicController.deleteEpic(epicId);

        verify(epicService, times(1)).deleteEpic(epicId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
