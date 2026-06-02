package com.mediarium.softged.ingestion.businessmodel;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentPage {
    private Long id;
    private Long documentId;
    private Long projectId;
    private String ownerUid;
    private Integer pageNumber;
    private String extractedText;
    private String imagePath;
    private Integer imageWidth;
    private Integer imageHeight;
    private String visualSummary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}