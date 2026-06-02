package com.mediarium.softged.indexing.service;

import com.mediarium.softged.document.businessmodel.DocumentStatus;
import com.mediarium.softged.document.businessmodel.GedDocument;
import com.mediarium.softged.document.dataservice.DocumentDataService;
import com.mediarium.softged.document.service.DocumentService;
import com.mediarium.softged.indexing.businessmodel.IndexingReport;
import com.mediarium.softged.ingestion.businessmodel.DocumentPage;
import com.mediarium.softged.ingestion.dataservice.DocumentPageDataService;
import com.mediarium.softged.shared.exception.BadRequestException;
import com.mediarium.softged.shared.exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentIndexingService {

    private final DocumentService documentService;
    private final DocumentDataService documentDataService;
    private final DocumentPageDataService documentPageDataService;
    private final VectorStore vectorStore;

    @Transactional
    public IndexingReport indexDocument(Long documentId, String ownerUid) {
        GedDocument document = documentService.findById(documentId, ownerUid);

        if (document.getStatus() != DocumentStatus.SUMMARIZED
                && document.getStatus() != DocumentStatus.INDEXING
                && document.getStatus() != DocumentStatus.INDEXED) {
            throw new BadRequestException("Document must be SUMMARIZED before indexing");
        }

        documentDataService.updateStatus(
                documentId,
                ownerUid,
                DocumentStatus.INDEXING
        );

        try {
            List<DocumentPage> pages = documentPageDataService.findAllByDocument(
                    documentId,
                    ownerUid
            );

            List<Document> vectorDocuments = pages.stream()
                    .map(page -> toVectorDocument(document, page))
                    .filter(vectorDocument -> !vectorDocument.getText().isBlank())
                    .toList();

            if (vectorDocuments.isEmpty()) {
                throw new BadRequestException("No indexable content found for document");
            }

            deleteExistingVectors(documentId, ownerUid);

            vectorStore.add(vectorDocuments);

            documentDataService.updateStatus(
                    documentId,
                    ownerUid,
                    DocumentStatus.INDEXED
            );

            return new IndexingReport(
                    documentId,
                    vectorDocuments.size()
            );

        } catch (Exception exception) {
            documentDataService.updateStatus(
                    documentId,
                    ownerUid,
                    DocumentStatus.FAILED
            );

            throw new TechnicalException("Document indexing failed", exception);
        }
    }

    private Document toVectorDocument(GedDocument document, DocumentPage page) {
        String content = buildIndexableContent(page);

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("ownerUid", document.getOwnerUid());
        metadata.put("projectId", String.valueOf(document.getProjectId()));
        metadata.put("documentId",  String.valueOf(document.getId()));
        metadata.put("pageId", String.valueOf(page.getId()));
        metadata.put("pageNumber", String.valueOf(page.getPageNumber()));
        metadata.put("originalFilename", document.getOriginalFilename());
        metadata.put("imagePath", page.getImagePath());
        metadata.put("sourceType", "DOCUMENT_PAGE");

        return new Document(content, metadata);
    }

    private String buildIndexableContent(DocumentPage page) {
        List<String> parts = new ArrayList<>();

        if (page.getExtractedText() != null && !page.getExtractedText().isBlank()) {
            parts.add("""
                    Texte extrait de la page :
                    %s
                    """.formatted(page.getExtractedText()));
        }

        if (page.getVisualSummary() != null && !page.getVisualSummary().isBlank()) {
            parts.add("""
                    Résumé visuel de la page :
                    %s
                    """.formatted(page.getVisualSummary()));
        }

        return String.join("\n\n", parts).trim();
    }

    private void deleteExistingVectors(Long documentId, String ownerUid) {
        String filterExpression = "documentId == '%s' && ownerUid == '%s'"
                .formatted(documentId, ownerUid);

        vectorStore.delete(filterExpression);
    }
}