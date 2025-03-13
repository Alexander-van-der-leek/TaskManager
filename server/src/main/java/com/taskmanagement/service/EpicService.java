package com.taskmanagement.service;

import com.taskmanagement.dto.EpicDTO;
import com.taskmanagement.model.Epic;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.EpicRepository;
import com.taskmanagement.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EpicService {
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;

    public EpicService(EpicRepository epicRepository, UserRepository userRepository) {
        this.epicRepository = epicRepository;
        this.userRepository = userRepository;
    }

    public Epic createEpic(EpicDTO epicDTO) {
        User owner = userRepository.findById(epicDTO.getOwnerId()).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        Epic epic = new Epic();
        epic.setId(UUID.randomUUID());
        epic.setName(epicDTO.getName());
        epic.setDescription(epicDTO.getDescription());
        epic.setOwner(owner);
        epic.setStoryPoints(epicDTO.getStoryPoints());
        epic.setStartDate(epicDTO.getStartDate() != null ? epicDTO.getStartDate() : ZonedDateTime.now());
        epic.setTargetEndDate(epicDTO.getTargetEndDate());
        epic.setCreatedAt(ZonedDateTime.now());
        epic.setUpdatedAt(ZonedDateTime.now());

        return epicRepository.save(epic);
    }

    public List<Epic> getAllEpics() {
        return epicRepository.findAll();
    }

    public Optional<Epic> getEpicById(UUID id) {
        return epicRepository.findById(id);
    }

    public Epic updateEpic(UUID id, EpicDTO epicDTO) {
        return epicRepository.findById(id).map(epic -> {
            epic.setName(epicDTO.getName());
            epic.setDescription(epicDTO.getDescription());
            epic.setStoryPoints(epicDTO.getStoryPoints());
            epic.setStartDate(epicDTO.getStartDate());
            epic.setTargetEndDate(epicDTO.getTargetEndDate());
            epic.setUpdatedAt(ZonedDateTime.now());
            return epicRepository.save(epic);
        }).orElseThrow(() -> new RuntimeException("Epic not found"));
    }

    public void deleteEpic(UUID id) {
        if (!epicRepository.existsById(id)) {
            throw new RuntimeException("Epic not found");
        }
        epicRepository.deleteById(id);
    }
}
