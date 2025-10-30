package org.example.workspacelib.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;
import org.example.workspacelib.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Project {
    Long projectId;
    String title;
    String descrption;
    String urlToProject;
    Date createdAt;
    Date  updatedAt;
    Long createdByUser;

}
