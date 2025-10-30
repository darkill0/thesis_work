package org.example.workspacelib.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class FollowerToProjectId implements Serializable {
    private Long follower;
    private Long project;
}
