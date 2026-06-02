package com.mediarium.softged.processing.service;

import com.mediarium.softged.indexing.businessmodel.IndexingReport;
import com.mediarium.softged.indexing.service.DocumentIndexingService;
import com.mediarium.softged.ingestion.businessmodel.ExtractionResult;
import com.mediarium.softged.ingestion.service.IngestionService;
import com.mediarium.softged.processing.businessmodel.DocumentProcessingReport;
import com.mediarium.softged.vision.businessmodel.VisualSummaryReport;
import com.mediarium.softged.vision.service.VisualSummaryService;
import org.springframework.stereotype.Service;

@Service
public class DocumentProcessingService {

    private final IngestionService ingestionService;
    private final VisualSummaryService visualSummaryService;
    private final DocumentIndexingService documentIndexingService;

    public DocumentProcessingService(
            IngestionService ingestionService,
            VisualSummaryService visualSummaryService,
            DocumentIndexingService documentIndexingService
    ) {
        this.ingestionService = ingestionService;
        this.visualSummaryService = visualSummaryService;
        this.documentIndexingService = documentIndexingService;
    }

    public DocumentProcessingReport processDocument(Long documentId, String ownerUid) {
        ExtractionResult extractionReport = ingestionService.extractDocument(
                documentId,
                ownerUid
        );

        VisualSummaryReport visualSummaryReport = visualSummaryService.generateVisualSummaries(
                documentId,
                ownerUid
        );

        IndexingReport indexingReport = documentIndexingService.indexDocument(
                documentId,
                ownerUid
        );

        return new DocumentProcessingReport(
                documentId,
                extractionReport.extractedPages(),
                visualSummaryReport.analyzedPages(),
                indexingReport.indexedPages()
        );
    }
}