package com.mediarium.softged.ingestion.dataservice;

import com.mediarium.softged.ingestion.businessmodel.DocumentPage;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface DocumentPageMapper {

    @Insert("""
        INSERT INTO document_pages (
            document_id,
            project_id,
            owner_uid,
            page_number,
            extracted_text,
            image_path,
            image_width,
            image_height,
            visual_summary,
            created_at,
            updated_at
        )
        VALUES (
            #{documentId},
            #{projectId},
            #{ownerUid},
            #{pageNumber},
            #{extractedText},
            #{imagePath},
            #{imageWidth},
            #{imageHeight},
            #{visualSummary},
            NOW(),
            NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(DocumentPage page);

    @Delete("""
        DELETE FROM document_pages
        WHERE document_id = #{documentId}
          AND owner_uid = #{ownerUid}
    """)
    int deleteByDocumentIdAndOwnerUid(
            @Param("documentId") Long documentId,
            @Param("ownerUid") String ownerUid
    );

    @Select("""
        SELECT
            id,
            document_id,
            project_id,
            owner_uid,
            page_number,
            extracted_text,
            image_path,
            image_width,
            image_height,
            visual_summary,
            created_at,
            updated_at
        FROM document_pages
        WHERE document_id = #{documentId}
          AND owner_uid = #{ownerUid}
        ORDER BY page_number ASC
    """)
    List<DocumentPage> findAllByDocumentIdAndOwnerUid(
            @Param("documentId") Long documentId,
            @Param("ownerUid") String ownerUid
    );

    @Update("""
    UPDATE document_pages
    SET
        visual_summary = #{visualSummary},
        updated_at = NOW()
    WHERE id = #{pageId}
      AND owner_uid = #{ownerUid}
    """)
    int updateVisualSummary(
            @Param("pageId") Long pageId,
            @Param("ownerUid") String ownerUid,
            @Param("visualSummary") String visualSummary
    );
}