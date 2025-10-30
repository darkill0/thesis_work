package org.example.workspacelib.dto.form;

import lombok.*;

import java.util.Date;
import java.util.List;

@Setter
@Getter
@Data
public class ProjectForm {
    Long projectId;
    String title;
    String descrption;
    String urlToProject;
    Date createdAt;
    Date updatedAt;
    Long createdByUser;
    List<TaskForm> tasks;

    public ProjectForm(Long projectId, String title, String descrption, String urlToProject, Date createdAt, Date updatedAt, Long createdByUser, List<TaskForm> tasks) {
        this.projectId = projectId;
        this.title = title;
        this.descrption = descrption;
        this.urlToProject = urlToProject;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.createdByUser = createdByUser;
        this.tasks = tasks;
    }

    public static ProjectFormBuilder builder() {
        return new ProjectFormBuilder();
    }

    protected boolean canEqual(final Object other) {
        return other instanceof ProjectForm;
    }

    public static class ProjectFormBuilder {
        private Long projectId;
        private String title;
        private String descrption;
        private String urlToProject;
        private Date createdAt;
        private Date updatedAt;
        private Long createdByUser;
        private List<TaskForm> tasks;

        ProjectFormBuilder() {
        }

        public ProjectFormBuilder projectId(Long projectId) {
            this.projectId = projectId;
            return this;
        }

        public ProjectFormBuilder title(String title) {
            this.title = title;
            return this;
        }

        public ProjectFormBuilder descrption(String descrption) {
            this.descrption = descrption;
            return this;
        }

        public ProjectFormBuilder urlToProject(String urlToProject) {
            this.urlToProject = urlToProject;
            return this;
        }

        public ProjectFormBuilder createdAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ProjectFormBuilder updatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ProjectFormBuilder createdByUser(Long createdByUser) {
            this.createdByUser = createdByUser;
            return this;
        }

        public ProjectFormBuilder tasks(List<TaskForm> tasks) {
            this.tasks = tasks;
            return this;
        }

        public ProjectForm build() {
            return new ProjectForm(this.projectId, this.title, this.descrption, this.urlToProject, this.createdAt, this.updatedAt, this.createdByUser, this.tasks);
        }

        public String toString() {
            return "ProjectForm.ProjectFormBuilder(projectId=" + this.projectId + ", title=" + this.title + ", descrption=" + this.descrption + ", urlToProject=" + this.urlToProject + ", createdAt=" + this.createdAt + ", updatedAt=" + this.updatedAt + ", createdByUser=" + this.createdByUser + ", tasks=" + this.tasks + ")";
        }
    }
}
