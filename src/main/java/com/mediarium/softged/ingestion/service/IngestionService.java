package com.mediarium.softged.ingestion.service;

import com.mediarium.softged.document.businessmodel.DocumentStatus;
import com.mediarium.softged.document.businessmodel.GedDocument;
import com.mediarium.softged.document.dataservice.DocumentDataService;
import com.mediarium.softged.document.service.DocumentService;
import com.mediarium.softged.ingestion.businessmodel.DocumentPage;
import com.mediarium.softged.ingestion.businessmodel.ExtractionResult;
import com.mediarium.softged.ingestion.businessmodel.RenderedPage;
import com.mediarium.softged.ingestion.dataservice.DocumentPageDataService;
import com.mediarium.softged.shared.exception.TechnicalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;

@Service
@RequiredArgsConstructor
public class IngestionService {

    private final DocumentService documentService;
    private final DocumentDataService documentDataService;
    private final DocumentPageDataService documentPageDataService;
    private final PdfTextExtractionService pdfTextExtractionService;
    private final PdfPageRenderingService pdfPageRenderingService;

    @Transactional
    public ExtractionResult extractDocument(Long documentId, String ownerUid) {
        GedDocument document = documentService.findById(documentId, ownerUid);

        documentDataService.updateStatus(
                documentId,
                ownerUid,
                DocumentStatus.EXTRACTING
        );

        try {
            documentPageDataService.deleteByDocument(documentId, ownerUid);

            Path pdfPath = Path.of(document.getStoragePath());
            int pageCount = pdfTextExtractionService.getPageCount(pdfPath);

            int extractedPages = 0;

            for (int pageNumber = 1; pageNumber <= pageCount; pageNumber++) {
                String extractedText = pdfTextExtractionService.extractTextFromPage(
                        pdfPath,
                        pageNumber
                );

                RenderedPage renderedPage = pdfPageRenderingService.renderPage(
                        pdfPath,
                        document.getId(),
                        document.getOwnerUid(),
                        pageNumber
                );

                DocumentPage page = DocumentPage.builder()
                        .documentId(document.getId())
                        .projectId(document.getProjectId())
                        .ownerUid(document.getOwnerUid())
                        .pageNumber(pageNumber)
                        .extractedText(normalizeText(extractedText))
                        .imagePath(renderedPage.imagePath())
                        .imageWidth(renderedPage.width())
                        .imageHeight(renderedPage.height())
                        .visualSummary(null)
                        .build();

                documentPageDataService.save(page);
                extractedPages++;
            }

            documentDataService.updateStatus(
                    documentId,
                    ownerUid,
                    DocumentStatus.EXTRACTED
            );

            return new ExtractionResult(
                    documentId,
                    pageCount,
                    extractedPages
            );

        } catch (Exception exception) {
            documentDataService.updateStatus(
                    documentId,
                    ownerUid,
                    DocumentStatus.FAILED
            );

            throw new TechnicalException("Document extraction failed", exception);
        }
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\u0000", "")
                .trim();
    }
}