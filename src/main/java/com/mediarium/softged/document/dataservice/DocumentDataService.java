package com.mediarium.softged.document.dataservice;

import com.mediarium.softged.document.businessmodel.DocumentStatus;
import com.mediarium.softged.document.businessmodel.GedDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DocumentDataService {

    private final DocumentMapper documentMapper;

    public List<GedDocument> findAllByProjectIdAndOwnerUid(Long projectId, String ownerUid) {
        return documentMapper.findAllByProjectIdAndOwnerUid(projectId, ownerUid);
    }

    public Optional<GedDocument> findByIdAndOwnerUid(Long documentId, String ownerUid) {
        return documentMapper.findByIdAndOwnerUid(documentId, ownerUid);
    }

    public GedDocument save(GedDocument document) {
        documentMapper.insert(document);
        return document;
    }

    public boolean deleteByIdAndOwnerUid(Long documentId, String ownerUid) {
        return documentMapper.deleteByIdAndOwnerUid(documentId, ownerUid) > 0;
    }

    public boolean updateStatus(Long documentId, String ownerUid, DocumentStatus status) {
        return documentMapper.updateStatus(documentId, ownerUid, status) > 0;
    }
}