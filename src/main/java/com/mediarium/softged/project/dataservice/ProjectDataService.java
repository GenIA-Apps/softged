package com.mediarium.softged.project.dataservice;

import com.mediarium.softged.project.businessmodel.Project;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectDataService {

    private final ProjectMapper projectMapper;

    public ProjectDataService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    public List<Project> findAllByOwnerUid(String ownerUid) {
        return projectMapper.findAllByOwnerUid(ownerUid);
    }

    public Optional<Project> findByIdAndOwnerUid(Long projectId, String ownerUid) {
        return projectMapper.findByIdAndOwnerUid(projectId, ownerUid);
    }

    public Project save(Project project) {
        projectMapper.insert(project);
        return project;
    }

    public boolean update(Project project) {
        return projectMapper.update(project) > 0;
    }

    public boolean deleteByIdAndOwnerUid(Long projectId, String ownerUid) {
        return projectMapper.deleteByIdAndOwnerUid(projectId, ownerUid) > 0;
    }
}