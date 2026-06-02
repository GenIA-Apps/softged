package com.mediarium.softged.document.service;

import com.mediarium.softged.document.businessmodel.DocumentStatus;
import com.mediarium.softged.document.businessmodel.GedDocument;
import com.mediarium.softged.document.dataservice.DocumentDataService;
import com.mediarium.softged.project.service.ProjectService;
import com.mediarium.softged.shared.exception.BadRequestException;
import com.mediarium.softged.shared.exception.ResourceNotFoundException;
import com.mediarium.softged.shared.exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentDataService documentDataService;
    private final ProjectService projectService;

    @Value("${app.storage.documents-path}")
    private String documentsStoragePath;

    public List<GedDocument> findAllByProject(Long projectId, String ownerUid) {
        projectService.findById(projectId, ownerUid);

        return documentDataService.findAllByProjectIdAndOwnerUid(projectId, ownerUid);
    }

    public GedDocument findById(Long documentId, String ownerUid) {
        return getDocumentOrThrow(documentId, ownerUid);
    }

    @Transactional
    public GedDocument upload(Long projectId, MultipartFile file, String ownerUid) {
        projectService.findById(projectId, ownerUid);
        validatePdf(file);

        String originalFilename = file.getOriginalFilename();
        String storedFilename = UUID.randomUUID() + ".pdf";

        Path projectDirectory = Path.of(documentsStoragePath, ownerUid, String.valueOf(projectId));
        Path targetPath = projectDirectory.resolve(storedFilename);

        try {
            Files.createDirectories(projectDirectory);
            file.transferTo(targetPath);
        } catch (Exception exception) {
            throw new TechnicalException("Unable to store uploaded document", exception);
        }

        GedDocument document = GedDocument.builder()
                .projectId(projectId)
                .ownerUid(ownerUid)
                .originalFilename(originalFilename)
                .storedFilename(storedFilename)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .status(DocumentStatus.UPLOADED)
                .storagePath(targetPath.toString())
                .build();

        GedDocument savedDocument = documentDataService.save(document);

        return getDocumentOrThrow(savedDocument.getId(), ownerUid);
    }

    @Transactional
    public void delete(Long documentId, String ownerUid) {
        GedDocument document = getDocumentOrThrow(documentId, ownerUid);

        boolean deleted = documentDataService.deleteByIdAndOwnerUid(documentId, ownerUid);

        if (!deleted) {
            throw new ResourceNotFoundException("Document not found");
        }

        try {
            Files.deleteIfExists(Path.of(document.getStoragePath()));
        } catch (Exception exception) {
            throw new TechnicalException("Document metadata deleted but file deletion failed", exception);
        }
    }

    private GedDocument getDocumentOrThrow(Long documentId, String ownerUid) {
        return documentDataService.findByIdAndOwnerUid(documentId, ownerUid)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found"));
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Document file is required");
        }

        String filename = file.getOriginalFilename();

        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            throw new BadRequestException("Only PDF files are accepted");
        }
    }
}