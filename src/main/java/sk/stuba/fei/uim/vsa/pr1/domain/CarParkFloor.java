package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "CAR_PARK_FLOOR")
public class CarParkFloor implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private CarParkEmbeddedId embeddedId;

    @MapsId("carParkId")
    @ManyToOne
    private CarPark carPark;

    @OneToMany(mappedBy = "carParkFloor")
    private final List<ParkingSpot> parkingSpots = new ArrayList<>();

    public CarParkEmbeddedId getEmbeddedId() {
        return embeddedId;
    }

    public void setEmbeddedId(CarParkEmbeddedId embeddedId) {
        this.embeddedId = embeddedId;
    }

    public CarPark getCarPark() {
        return carPark;
    }

    public void setCarPark(CarPark carPark) {
        this.carPark = carPark;
    }

    public List<ParkingSpot> getParkingSpots() {
        return parkingSpots;
    }

    public CarParkFloor addParkingSpot(ParkingSpot spot) {
        if (!this.parkingSpots.contains(spot)) {
            spot.setCarParkFloor(this);
            this.parkingSpots.add(spot);
        }
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash + Objects.hashCode(this.embeddedId);
        hash = 11 * hash + Objects.hashCode(this.carPark);
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
        final CarParkFloor other = (CarParkFloor) obj;
        if (!Objects.equals(this.embeddedId, other.embeddedId)) {
            return false;
        }
        if (!Objects.equals(this.carPark, other.carPark)) {
            return false;
        }
        return true;
    }
}
