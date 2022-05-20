package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CAR_PARK")
public class CarPark implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true, name = "NAME")
    private String name;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "PRICE_PER_HOUR")
    private Integer pricePerHour;

    @OneToMany(mappedBy = "carPark")
    private final List<CarParkFloor> carParkFloorList = new ArrayList<>();

    public List<CarParkFloor> getCarParkFloorList() {
        return carParkFloorList;
    }

    public CarPark addCarParkFloor(CarParkFloor floor) {
        if (!this.carParkFloorList.contains(floor)) {
            floor.setCarPark(this);
            floor.getEmbeddedId().setCarParkId(this.id);
            this.carParkFloorList.add(floor);
        }

        return this;
    }

    /**
     * Get the value of name
     *
     * @return the value of name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name new value of name
     */
    public void setName(String name) {
        this.name = name;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(Integer pricePerHour) {
        this.pricePerHour = pricePerHour;
    }
}
