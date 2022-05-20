package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "USER_COUPON")
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = -8981952380955760238L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    private Coupon coupon;

    private Boolean used;

    public UserCoupon() {
        used = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Coupon getCoupon() {
        return coupon;
    }

    public void setCoupon(Coupon coupon) {
        this.coupon = coupon;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }
}
