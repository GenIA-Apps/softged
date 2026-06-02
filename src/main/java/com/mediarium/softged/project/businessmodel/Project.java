package com.mediarium.softged.project.businessmodel;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Long id;
    private String name;
    private String description;
    private String ownerUid;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}