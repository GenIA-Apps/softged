package com.mediarium.softged.document.dataservice;

import com.mediarium.softged.document.businessmodel.DocumentStatus;
import com.mediarium.softged.document.businessmodel.GedDocument;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface DocumentMapper {
    @Select("""
        SELECT
            id,
            project_id,
            owner_uid,
            original_filename,
            stored_filename,
            content_type,
            size_bytes,
            status,
            storage_path,
            created_at,
            updated_at
        FROM documents
        WHERE project_id = #{projectId}
          AND owner_uid = #{ownerUid}
        ORDER BY created_at DESC
    """)
    List<GedDocument> findAllByProjectIdAndOwnerUid(
            @Param("projectId") Long projectId,
            @Param("ownerUid") String ownerUid
    );

    @Select("""
        SELECT
            id,
            project_id,
            owner_uid,
            original_filename,
            stored_filename,
            content_type,
            size_bytes,
            status,
            storage_path,
            created_at,
            updated_at
        FROM documents
        WHERE id = #{documentId}
          AND owner_uid = #{ownerUid}
    """)
    Optional<GedDocument> findByIdAndOwnerUid(
            @Param("documentId") Long documentId,
            @Param("ownerUid") String ownerUid
    );

    @Insert("""
        INSERT INTO documents (
            project_id,
            owner_uid,
            original_filename,
            stored_filename,
            content_type,
            size_bytes,
            status,
            storage_path,
            created_at,
            updated_at
        )
        VALUES (
            #{projectId},
            #{ownerUid},
            #{originalFilename},
            #{storedFilename},
            #{contentType},
            #{sizeBytes},
            #{status},
            #{storagePath},
            NOW(),
            NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(GedDocument document);

    @Delete("""
        DELETE FROM documents
        WHERE id = #{documentId}
          AND owner_uid = #{ownerUid}
    """)
    int deleteByIdAndOwnerUid(
            @Param("documentId") Long documentId,
            @Param("ownerUid") String ownerUid
    );

    @Update("""
    UPDATE documents
    SET
        status = #{status},
        updated_at = NOW()
    WHERE id = #{documentId}
      AND owner_uid = #{ownerUid}
""")
    int updateStatus(
            @Param("documentId") Long documentId,
            @Param("ownerUid") String ownerUid,
            @Param("status") DocumentStatus status
    );
}