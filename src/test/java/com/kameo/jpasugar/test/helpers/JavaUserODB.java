package com.kameo.jpasugar.test.helpers;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class JavaUserODB {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    long id;
    private String email;
    @ManyToOne(cascade = CascadeType.ALL)
    public JavaAddressODB address;

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
}
