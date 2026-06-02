package com.mediarium.softged.project.controller;

import com.mediarium.softged.project.businessmodel.Project;
import com.mediarium.softged.project.dto.CreateProjectRequest;
import com.mediarium.softged.project.dto.ProjectResponse;
import com.mediarium.softged.project.dto.UpdateProjectRequest;
import com.mediarium.softged.project.service.ProjectService;
import com.mediarium.softged.shared.security.FirebasePrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public List<ProjectResponse> findAll(Authentication authentication) {
        FirebasePrincipal principal = getPrincipal(authentication);
        return projectService.findAll(principal.uid())
                .stream()
                .map(ProjectResponse::from)
                .toList();
    }

    @GetMapping("/{projectId}")
    public ProjectResponse findById(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);
        Project project = projectService.findById(projectId, principal.uid());
        return ProjectResponse.from(project);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProjectResponse create(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);
        Project project = projectService.create(
                request.name(),
                request.description(),
                principal.uid()
        );

        return ProjectResponse.from(project);
    }

    @PutMapping("/{projectId}")
    public ProjectResponse update(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);
        Project project = projectService.update(
                projectId,
                request.name(),
                request.description(),
                principal.uid()
        );

        return ProjectResponse.from(project);
    }

    @DeleteMapping("/{projectId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long projectId,
            Authentication authentication
    ) {
        FirebasePrincipal principal = getPrincipal(authentication);
        projectService.delete(projectId, principal.uid());
    }

    private FirebasePrincipal getPrincipal(Authentication authentication) {
        return (FirebasePrincipal) authentication.getPrincipal();
    }
}