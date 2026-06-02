package com.mediarium.softged.document.controller;

import com.mediarium.softged.document.businessmodel.GedDocument;
import com.mediarium.softged.document.dto.DocumentResponse;
import com.mediarium.softged.document.service.DocumentService;
import com.mediarium.softged.shared.exception.ResourceNotFoundException;
import com.mediarium.softged.shared.security.FirebasePrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}/documents")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping
    public List<DocumentResponse> findAllByProject(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);

        return documentService.findAllByProject(projectId, principal.uid())
                .stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DocumentResponse upload(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);
        GedDocument document = documentService.upload(projectId, file, principal.uid());
        return DocumentResponse.from(document);
    }

    @GetMapping("/{documentId}")
    public DocumentResponse findById(
            @PathVariable Long projectId,
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);
        GedDocument document = documentService.findById(documentId, principal.uid());
        if (!document.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Document not found");
        }
        return DocumentResponse.from(document);
    }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long projectId,
            @PathVariable Long documentId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);
        GedDocument document = documentService.findById(documentId, principal.uid());
        if (!document.getProjectId().equals(projectId)) {
            throw new ResourceNotFoundException("Document not found");
        }
        documentService.delete(documentId, principal.uid());
    }

    private FirebasePrincipal getPrincipal(Authentication authentication) {
        return (FirebasePrincipal) authentication.getPrincipal();
    }
}