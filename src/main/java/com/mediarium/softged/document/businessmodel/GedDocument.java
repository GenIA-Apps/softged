package com.mediarium.softged.document.businessmodel;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GedDocument {
    private Long id;
    private Long projectId;
    private String ownerUid;
    private String originalFilename;
    private String storedFilename;
    private String contentType;
    private Long sizeBytes;
    private DocumentStatus status;
    private String storagePath;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}