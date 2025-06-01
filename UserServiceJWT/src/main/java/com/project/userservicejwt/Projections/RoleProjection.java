package com.project.userservicejwt.Projections;

import com.project.userservicejwt.models.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleProjection {
    String value;

    public RoleProjection makeRole(Role role){
        RoleProjection roleProjection = new RoleProjection();
        roleProjection.value = role.getValue();
        return roleProjection;
    }
}
