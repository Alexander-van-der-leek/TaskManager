package com.taskmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanagement.dto.TaskCommentDTO;
import com.taskmanagement.security.JWTFilter;
import com.taskmanagement.security.JWTTokenProvider;
import com.taskmanagement.service.TaskCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskCommentController.class)
public class TaskCommentControllerTest {

    @MockBean
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private JWTFilter jwtFilter;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskCommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID userId;
    private Integer taskId;
    private Integer commentId;
    private TaskCommentDTO commentDTO;
    private User userDetails;

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        userId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        taskId = 1;
        commentId = 1;

        commentDTO = new TaskCommentDTO();
        commentDTO.setId(commentId);
        commentDTO.setTaskId(taskId);
        commentDTO.setUserId(userId);
        commentDTO.setUserName("Test User");
        commentDTO.setContent("Test Comment");
        commentDTO.setCreatedAt(ZonedDateTime.now());
        commentDTO.setUpdatedAt(ZonedDateTime.now());

        Collection<SimpleGrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        userDetails = new User(userId.toString(), "", authorities);

        when(jwtTokenProvider.validateToken(any())).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(userDetails, "", authorities)
        );

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    void getCommentsByTaskId_ShouldReturnComments() throws Exception {
        when(commentService.getCommentsByTaskId(taskId, userId)).thenReturn(Arrays.asList(commentDTO));

        mockMvc.perform(get("/api/comments/task/" + taskId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(commentId)))
                .andExpect(jsonPath("$[0].content", is("Test Comment")));
    }

    @Test
    void addComment_ShouldCreateComment() throws Exception {
        TaskCommentDTO inputDTO = new TaskCommentDTO();
        inputDTO.setTaskId(taskId);
        inputDTO.setContent("New Comment");

        when(commentService.addComment(any(TaskCommentDTO.class), eq(userId))).thenReturn(commentDTO);

        mockMvc.perform(post("/api/comments")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentId)))
                .andExpect(jsonPath("$.content", is("Test Comment")));
    }

    @Test
    void updateComment_ShouldUpdateComment() throws Exception {
        TaskCommentDTO inputDTO = new TaskCommentDTO();
        inputDTO.setContent("Updated Comment");

        TaskCommentDTO updatedDTO = new TaskCommentDTO();
        updatedDTO.setId(commentId);
        updatedDTO.setTaskId(taskId);
        updatedDTO.setUserId(userId);
        updatedDTO.setUserName("Test User");
        updatedDTO.setContent("Updated Comment");
        updatedDTO.setCreatedAt(ZonedDateTime.now());
        updatedDTO.setUpdatedAt(ZonedDateTime.now());

        when(commentService.updateComment(eq(commentId), any(TaskCommentDTO.class), eq(userId))).thenReturn(updatedDTO);

        mockMvc.perform(put("/api/comments/" + commentId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(commentId)))
                .andExpect(jsonPath("$.content", is("Updated Comment")));
    }

    @Test
    void deleteComment_ShouldDeleteComment() throws Exception {
        mockMvc.perform(delete("/api/comments/" + commentId)
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(commentService, times(1)).deleteComment(commentId, userId);
    }
}