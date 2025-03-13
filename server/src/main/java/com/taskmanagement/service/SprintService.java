package com.taskmanagement.service;

import com.taskmanagement.dto.SprintDTO;
import com.taskmanagement.exception.SprintNotFoundException;
import com.taskmanagement.exception.UserNotFoundException;
import com.taskmanagement.model.Sprint;
import com.taskmanagement.model.User;
import com.taskmanagement.repository.SprintRepository;
import com.taskmanagement.repository.UserRepository;
import com.taskmanagement.exception.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

    @Service
    public class SprintService {
        private final SprintRepository sprintRepository;
        private final UserRepository userRepository;

        public SprintService(SprintRepository sprintRepository, UserRepository userRepository) {
            this.sprintRepository = sprintRepository;
            this.userRepository = userRepository;
        }

        public SprintDTO createSprint(SprintDTO sprintDTO) {
            Sprint sprint = new Sprint();
            sprint.setId(UUID.randomUUID());
            sprint.setName(sprintDTO.getName());
            sprint.setGoal(sprintDTO.getGoal());
            sprint.setCapacityPoints(sprintDTO.getCapacityPoints());
            sprint.setStartDate(sprintDTO.getStartDate());
            sprint.setEndDate(sprintDTO.getEndDate());
            sprint.setActive(sprintDTO.isActive());
            sprint.setCreatedAt(ZonedDateTime.now());
            sprint.setUpdatedAt(ZonedDateTime.now());


            User scrumMaster = userRepository.findById(sprintDTO.getScrumMasterId())
                    .orElseThrow(() -> new UserNotFoundException(sprintDTO.getScrumMasterId()));
            sprint.setScrumMaster(scrumMaster);

            Sprint savedSprint = sprintRepository.save(sprint);
            return mapToDTO(savedSprint);
        }

        public SprintDTO getSprintById(UUID id) {
            Sprint sprint = sprintRepository.findById(id)
                    .orElseThrow(() -> new SprintNotFoundException(id));
            return mapToDTO(sprint);
        }

        public List<SprintDTO> getAllSprints() {
            List<Sprint> sprints = sprintRepository.findAll();
            return sprints.stream().map(this::mapToDTO).collect(Collectors.toList());
        }

        public SprintDTO updateSprint(UUID id, SprintDTO sprintDTO) {
            Sprint sprint = sprintRepository.findById(id)
                    .orElseThrow(() -> new SprintNotFoundException(id));

            sprint.setName(sprintDTO.getName());
            sprint.setGoal(sprintDTO.getGoal());
            sprint.setCapacityPoints(sprintDTO.getCapacityPoints());
            sprint.setStartDate(sprintDTO.getStartDate());
            sprint.setEndDate(sprintDTO.getEndDate());
            sprint.setActive(sprintDTO.isActive());
            sprint.setUpdatedAt(ZonedDateTime.now());

            User scrumMaster = userRepository.findById(sprintDTO.getScrumMasterId())
                    .orElseThrow(() -> new UserNotFoundException(sprintDTO.getScrumMasterId()));
            sprint.setScrumMaster(scrumMaster);

            Sprint updatedSprint = sprintRepository.save(sprint);
            return mapToDTO(updatedSprint);
        }

        public void deleteSprint(UUID id) {
            if (!sprintRepository.existsById(id)) {
                throw new SprintNotFoundException(id);
            }
            sprintRepository.deleteById(id);
        }

        private SprintDTO mapToDTO(Sprint sprint) {
            SprintDTO sprintDTO = new SprintDTO();
            sprintDTO.setId(sprint.getId());
            sprintDTO.setName(sprint.getName());
            sprintDTO.setGoal(sprint.getGoal());
            sprintDTO.setScrumMasterId(sprint.getScrumMaster().getId());
            sprintDTO.setCapacityPoints(sprint.getCapacityPoints());
            sprintDTO.setStartDate(sprint.getStartDate());
            sprintDTO.setEndDate(sprint.getEndDate());
            sprintDTO.setActive(sprint.isActive());
            return sprintDTO;
        }
    }