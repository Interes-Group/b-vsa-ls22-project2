package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "USER")
public class User implements Serializable {
    private static final long serialVersionUID = 8059659837350563206L;

    @Id
    @GeneratedValue
    private Long id;

    private String firstName;
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    @OneToMany(mappedBy = "user")
    private List<Car> cars;

    @OneToMany(mappedBy = "user")
    private List<UserCoupon> coupons;

    public User() {
        this.cars = new ArrayList<>();
    }

    public User(String firstName, String lastName, String email) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public User addCar(Car car) {
        if (!this.cars.contains(car)) {
            car.setUser(this);
            this.cars.add(car);
        }
        return this;
    }

    public List<UserCoupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<UserCoupon> coupons) {
        this.coupons = coupons;
    }
}
