package com.taskmanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.ZonedDateTime;

@Entity
@Table(name = "epics")
@Data
public class Epic {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Integer id;

   @Column(nullable = false)
   private String name;

   @Column(nullable = false)
   private String description;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "owner_id", nullable = false)
   private User owner;

   @Column(name = "story_points", nullable = false)
   private Integer storyPoints;

   @Column(name = "start_date", nullable = false)
   private ZonedDateTime startDate;

   @Column(name = "target_end_date", nullable = false)
   private ZonedDateTime targetEndDate;

   @Column(name = "actual_end_date")
   private ZonedDateTime actualEndDate;

   @Column(name = "created_at", nullable = false)
   private ZonedDateTime createdAt;

   @Column(name = "updated_at", nullable = false)
   private ZonedDateTime updatedAt;

   @PrePersist
   protected void onCreate() {
      createdAt = updatedAt = ZonedDateTime.now();
   }

   @PreUpdate
   protected void onUpdate() {
      updatedAt = ZonedDateTime.now();
   }
}
