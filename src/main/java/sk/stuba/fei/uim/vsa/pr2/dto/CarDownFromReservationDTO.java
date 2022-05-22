/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.domain.Car;

/**
 *
 * @author sheax
 */
public class CarDownFromReservationDTO {
    public Long id;
    public String brand;
    public String model;
    public String vrp;
    public String colour;
    public CarTypeDTO type;
    
    public List<IdDTO> reservations;
    public UserDownFromCarDTO owner;
    
    public CarDownFromReservationDTO(Car car)
    {
        this.id = car.getId();
        this.brand = car.getBrand();
        this.model = car.getModel();
        this.vrp = car.getVrp();
        this.colour = car.getColour();
        
        this.reservations = new ArrayList<>();
        
        if (car.getReservations() != null) {
            this.reservations = car.getReservations().stream().map(r -> {
                return new IdDTO(r.getId());
            }).collect(Collectors.toList());
        }
        
        if (car.getUser() != null) {
            this.owner = new UserDownFromCarDTO(car.getUser());
        }
        
        if (car.getType() != null) {
            this.type = new CarTypeDTO(car.getType());
        }
    }
    
    public CarDownFromReservationDTO(){}
}
