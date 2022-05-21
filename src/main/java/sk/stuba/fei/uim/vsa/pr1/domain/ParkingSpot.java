package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "PARKING_SPOT")
public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private CarParkFloor carParkFloor;
    
    @Column(name = "SPOT_IDENTIFIER")
    private String spotIdentifier;

    @ManyToOne
    private CarType type;
    
    @OneToMany(mappedBy = "parkingSpot")
    private List<Reservation> reservations;

    public ParkingSpot() {
    }

    public ParkingSpot(String spotIdentifier) {
        this.spotIdentifier = spotIdentifier;
    }

    /**
     * Get the value of identifier
     *
     * @return the value of identifier
     */
    public String getSpotIdentifier() {
        return spotIdentifier;
    }

    /**
     * Set the value of identifier
     *
     * @param identifier new value of identifier
     */
    public void setSpotIdentifier(String identifier) {
        this.spotIdentifier = identifier;
    }


    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    public void setId(Long id) {
        this.id = id;
    }

    public CarParkFloor getCarParkFloor() {
        return carParkFloor;
    }

    public void setCarParkFloor(CarParkFloor carParkFloor) {
        this.carParkFloor = carParkFloor;
    }

    public CarType getType() {
        return type;
    }

    public void setType(CarType type) {
        this.type = type;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.id);
        hash = 59 * hash + Objects.hashCode(this.carParkFloor);
        hash = 59 * hash + Objects.hashCode(this.spotIdentifier);
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
        final ParkingSpot other = (ParkingSpot) obj;
        if (!Objects.equals(this.spotIdentifier, other.spotIdentifier)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.carParkFloor, other.carParkFloor)) {
            return false;
        }
        return true;
    }
    
    public List<Reservation> getReservations() {
        return reservations;
    }
    
}
