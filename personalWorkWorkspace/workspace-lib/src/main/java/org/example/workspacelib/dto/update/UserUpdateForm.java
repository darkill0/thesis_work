package org.example.workspacelib.dto.update;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.example.workspacelib.dto.form.ProjectForm;
import org.example.workspacelib.dto.form.TaskForm;
import org.example.workspacelib.dto.form.TicketForm;

import java.util.Date;
import java.util.List;

@Data
@Setter
@Getter
@Builder
public class UserUpdateForm {

    Long roleId;

    public UserUpdateForm() {
    }

    public UserUpdateForm(Long roleId) {
        this.roleId = roleId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }
}
