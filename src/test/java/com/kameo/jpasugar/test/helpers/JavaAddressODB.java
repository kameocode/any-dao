package com.kameo.jpasugar.test.helpers;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class JavaAddressODB {
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.AUTO)
    long id;

    public String city;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

}
