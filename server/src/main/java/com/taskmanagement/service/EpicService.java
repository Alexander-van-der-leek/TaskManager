package com.taskmanagement.service;

import com.taskmanagement.dto.EpicDTO;
import com.taskmanagement.exception.EpicNotFoundException;
import com.taskmanagement.model.Epic;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.EpicRepository;
import com.taskmanagement.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EpicService {
    private final EpicRepository epicRepository;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(EpicService.class);

    public EpicService(EpicRepository epicRepository, UserRepository userRepository) {
        this.epicRepository = epicRepository;
        this.userRepository = userRepository;
    }

    public Epic createEpic(EpicDTO epicDTO) {
        User owner = userRepository.findById(epicDTO.getOwnerId())
                .orElseThrow(() -> new EpicNotFoundException("User not found with id: " + epicDTO.getOwnerId()));

        Epic epic = new Epic();
        epic.setName(epicDTO.getName());
        epic.setDescription(epicDTO.getDescription());
        epic.setOwner(owner);
        epic.setStoryPoints(Optional.ofNullable(epicDTO.getStoryPoints()).orElse(0));
        epic.setStartDate(Optional.ofNullable(epicDTO.getStartDate()).orElse(ZonedDateTime.now()));
        epic.setTargetEndDate(epicDTO.getTargetEndDate());
        epic.setCreatedAt(ZonedDateTime.now());
        epic.setUpdatedAt(ZonedDateTime.now());

        logger.info("Creating epic with name: {}", epicDTO.getName());

        return epicRepository.save(epic);
    }

    public List<Epic> getAllEpics() {
        return epicRepository.findAll();
    }

    public List<EpicDTO> getAllEpicsWithOwner() {
        return epicRepository.findAllWithOwner();
    }

    public Optional<Epic> getEpicById(int id) {
        return epicRepository.findById(id);
    }

    public Epic updateEpic(int id, EpicDTO epicDTO) {
        return epicRepository.findById(id).map(epic -> {
            epic.setName(epicDTO.getName());
            epic.setDescription(epicDTO.getDescription());
            epic.setStoryPoints(epicDTO.getStoryPoints() != null ? epicDTO.getStoryPoints() : 0); // Default to 0 if null
            epic.setStartDate(epicDTO.getStartDate() != null ? epicDTO.getStartDate() : ZonedDateTime.now());
            epic.setTargetEndDate(epicDTO.getTargetEndDate());
            epic.setUpdatedAt(ZonedDateTime.now());

            logger.info("Updating epic with id: {}", id);

            return epicRepository.save(epic);
        }).orElseThrow(() -> new EpicNotFoundException("Epic not found with id: " + id));
    }

    @Transactional
    public void deleteEpic(int id) {
        epicRepository.findById(id).orElseThrow(() -> new EpicNotFoundException("Epic not found with id: " + id));
        epicRepository.deleteById(id);
        logger.info("Deleted epic with id: {}", id);
    }

    public boolean isOwner(Integer epicId, String name) {
        return epicRepository.findById(epicId)
                .map(epic -> epic.getOwner().getName().equals(name))
                .orElse(false);
    }
}
