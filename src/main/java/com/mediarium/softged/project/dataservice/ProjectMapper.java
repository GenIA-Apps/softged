package com.mediarium.softged.project.dataservice;

import com.mediarium.softged.project.businessmodel.Project;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ProjectMapper {
    @Select("""
        SELECT
            id,
            name,
            description,
            owner_uid,
            created_at,
            updated_at
        FROM projects
        WHERE owner_uid = #{ownerUid}
        ORDER BY created_at DESC
    """)
    List<Project> findAllByOwnerUid(@Param("ownerUid") String ownerUid);

    @Select("""
        SELECT
            id,
            name,
            description,
            owner_uid,
            created_at,
            updated_at
        FROM projects
        WHERE id = #{id}
          AND owner_uid = #{ownerUid}
    """)
    Optional<Project> findByIdAndOwnerUid(
            @Param("id") Long id,
            @Param("ownerUid") String ownerUid
    );

    @Insert("""
        INSERT INTO projects (
            name,
            description,
            owner_uid,
            created_at,
            updated_at
        )
        VALUES (
            #{name},
            #{description},
            #{ownerUid},
            NOW(),
            NOW()
        )
    """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Project project);

    @Update("""
        UPDATE projects
        SET
            name = #{name},
            description = #{description},
            updated_at = NOW()
        WHERE id = #{id}
          AND owner_uid = #{ownerUid}
    """)
    int update(Project project);

    @Delete("""
        DELETE FROM projects
        WHERE id = #{id}
          AND owner_uid = #{ownerUid}
    """)
    int deleteByIdAndOwnerUid(
            @Param("id") Long id,
            @Param("ownerUid") String ownerUid
    );
}