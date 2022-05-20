package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "CAR_TYPE")
public class CarType implements Serializable {
    private static final long serialVersionUID = 7611477412856622617L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(unique = true)
    private String name;

    public CarType() {
    }

    public CarType(String name) {
        this.name = name;
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
}
