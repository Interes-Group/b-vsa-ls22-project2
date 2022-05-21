package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "COUPON")
public class Coupon implements Serializable {
    private static final long serialVersionUID = 7211729052627410906L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;

    private Integer discount;
    
    @OneToOne
    private Reservation reservation;
    
    public Coupon() {
    }

    public Coupon(String name, Integer discount) {
        this.name = name;
        this.discount = discount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        reservation.setCoupon(this);
        this.reservation = reservation;
    }
}
