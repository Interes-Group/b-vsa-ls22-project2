/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr1.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * @author sheax
 */
@Entity
public class Holiday implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(columnDefinition = "DATE", name = "\"DAY\"", nullable = false, unique = true)
    private LocalDate day;

    public LocalDate getDay() {
        return day;
    }

    public void setDay(LocalDate day) {
        LocalDate dateWithoutYear = day.withYear(1);
        this.day = dateWithoutYear;
    }

    @Column(nullable = false, unique = true)
    private String name;

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


}
