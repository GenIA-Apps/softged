package com.mediarium.softged.ingestion.dataservice;

import com.mediarium.softged.ingestion.businessmodel.DocumentPage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentPageDataService {

    private final DocumentPageMapper documentPageMapper;

    public DocumentPage save(DocumentPage page) {
        documentPageMapper.insert(page);
        return page;
    }

    public void deleteByDocument(Long documentId, String ownerUid) {
        documentPageMapper.deleteByDocumentIdAndOwnerUid(documentId, ownerUid);
    }

    public List<DocumentPage> findAllByDocument(Long documentId, String ownerUid) {
        return documentPageMapper.findAllByDocumentIdAndOwnerUid(documentId, ownerUid);
    }

    public boolean updateVisualSummary(
            Long pageId,
            String ownerUid,
            String visualSummary
    ) {
        return documentPageMapper.updateVisualSummary(
                pageId,
                ownerUid,
                visualSummary
        ) > 0;
    }
}