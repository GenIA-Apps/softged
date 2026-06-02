package com.mediarium.softged.project.service;

import com.mediarium.softged.project.businessmodel.Project;
import com.mediarium.softged.project.dataservice.ProjectDataService;
import com.mediarium.softged.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectDataService projectDataService;

    public ProjectService(ProjectDataService projectDataService) {
        this.projectDataService = projectDataService;
    }

    public List<Project> findAll(String ownerUid) {
        return projectDataService.findAllByOwnerUid(ownerUid);
    }

    public Project findById(Long projectId, String ownerUid) {
        return getProjectOrThrow(projectId, ownerUid);
    }

    @Transactional
    public Project create(String name, String description, String ownerUid) {
        Project project = Project.builder()
                .name(name)
                .description(description)
                .ownerUid(ownerUid)
                .build();

        Project savedProject = projectDataService.save(project);

        return getProjectOrThrow(savedProject.getId(), ownerUid);
    }

    @Transactional
    public Project update(
            Long projectId,
            String name,
            String description,
            String ownerUid
    ) {
        Project existingProject = getProjectOrThrow(projectId, ownerUid);

        existingProject.setName(name);
        existingProject.setDescription(description);

        boolean updated = projectDataService.update(existingProject);

        if (!updated) {
            throw new ResourceNotFoundException("Project not found");
        }

        return getProjectOrThrow(projectId, ownerUid);
    }

    @Transactional
    public void delete(Long projectId, String ownerUid) {
        boolean deleted = projectDataService.deleteByIdAndOwnerUid(projectId, ownerUid);

        if (!deleted) {
            throw new ResourceNotFoundException("Project not found");
        }
    }

    private Project getProjectOrThrow(Long projectId, String ownerUid) {
        return projectDataService.findByIdAndOwnerUid(projectId, ownerUid)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
    }
}