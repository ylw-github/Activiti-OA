package com.ylw.oa.leave.mapper;

import com.ylw.oa.leave.po.Role;
import com.ylw.oa.leave.po.Role_permission;
import com.ylw.oa.leave.po.User_role;

import java.util.List;


public interface RoleMapper {

    List<Role> getRoles();

    void adduserrole(User_role ur);

    Role getRoleidbyName(String rolename);

    List<Role> getRoleinfo();

    void addRole(Role role);

    void addRolePermission(Role_permission rp);

    void deleterole(int rid);

    void deleterole_permission(int roleid);

    void deleteuser_role(int roleid);

    Role getRolebyid(int rid);
}
