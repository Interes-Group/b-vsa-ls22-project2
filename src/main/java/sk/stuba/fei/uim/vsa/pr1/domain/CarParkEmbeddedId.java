/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author sheax
 */
@Embeddable
public class CarParkEmbeddedId implements Serializable {
    private static final long serialVersionUID = 1L;

    @Column(name = "CAR_PARK_ID")
    private Long carParkId;

    @Column(name = "IDENTIFIER")
    private String identifier;

    /**
     * Get the value of identifier
     *
     * @return the value of identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the value of identifier
     *
     * @param identifier new value of identifier
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }


    /**
     * Get the value of carParkId
     *
     * @return the value of carParkId
     */
    public Long getCarParkId() {
        return carParkId;
    }

    /**
     * Set the value of carParkId
     *
     * @param carParkId new value of carParkId
     */
    public void setCarParkId(Long carParkId) {
        this.carParkId = carParkId;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Objects.hashCode(this.carParkId);
        hash = 67 * hash + Objects.hashCode(this.identifier);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CarParkEmbeddedId other = (CarParkEmbeddedId) obj;
        if (!Objects.equals(this.identifier, other.identifier)) {
            return false;
        }
        if (!Objects.equals(this.carParkId, other.carParkId)) {
            return false;
        }
        return true;
    }

}
