package org.example.workspacelib.models;

import jakarta.persistence.*;
import lombok.*;
import org.example.workspacelib.entity.UserEntity;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Role {

    Long roleId;
    String role;
    List<User> users;

}
