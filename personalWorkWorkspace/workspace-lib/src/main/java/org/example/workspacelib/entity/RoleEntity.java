package org.example.workspacelib.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="roles")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roleId")
    Long roleId;


    @Column(name = "role")
    String role;

    @OneToMany(mappedBy = "roleId", cascade = CascadeType.ALL)
    List<UserEntity> users;


}
