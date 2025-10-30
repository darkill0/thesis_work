package org.example.workspacelib.dto.form;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.workspacelib.entity.UserEntity;

import java.util.List;

@Data
@Setter
@Getter
@Builder
public class RoleForm {
    Long roleId;
    String role;
    List<UserForm> users;

}
