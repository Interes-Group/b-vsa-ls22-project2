package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "PARKING_SPOT")
public class ParkingSpot implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private CarParkFloor carParkFloor;

    private String spotIdentifier;

    @ManyToOne
    private CarType type;

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
    public String getIdentifier() {
        return spotIdentifier;
    }

    /**
     * Set the value of identifier
     *
     * @param identifier new value of identifier
     */
    public void setIdentifier(String identifier) {
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
}
