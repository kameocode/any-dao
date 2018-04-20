package com.kameo.jpasugar.test.helpers;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
public class JavaUserODB {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    long id;
    private String email;
    @ManyToOne(cascade = CascadeType.ALL)
    public JavaAddressODB address;
    private boolean valid;
    @ElementCollection
    Map<String, Integer> userRolesMap;
    @ElementCollection
    Set<String> rolesSet;
    @ElementCollection
    List<String> rolesList;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public JavaAddressODB getAddress() {
        return address;
    }

    public void setAddress(JavaAddressODB address) {
        this.address = address;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String variable() {
        return "test";
    }

    public Map<String, Integer> getUserRolesMap() {
        return userRolesMap;
    }

    public void setUserRolesMap(Map<String, Integer> userRolesMap) {
        this.userRolesMap = userRolesMap;
    }

    public Set<String> getRolesSet() {
        return rolesSet;
    }

    public void setRolesSet(Set<String> rolesSet) {
        this.rolesSet = rolesSet;
    }

    public List<String> getRolesList() {
        return rolesList;
    }

    public void setRolesList(List<String> rolesList) {
        this.rolesList = rolesList;
    }
}
