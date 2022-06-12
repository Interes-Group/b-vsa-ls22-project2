/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.util.ArrayList;
import java.util.List;
import sk.stuba.fei.uim.vsa.pr1.domain.Car;
import sk.stuba.fei.uim.vsa.pr1.domain.ParkingSpot;
import sk.stuba.fei.uim.vsa.pr1.domain.Reservation;

/**
 *
 * @author sheax
 */
public class CarDownFromUserDTO {
    public Long id;
    public String brand;
    public String model;
    public String vrp;
    public String colour;
    
    public List<ReservationDownFromCarDTO> reservations;
    public IdDTO owner;
    
    public CarTypeDTO type;
    
    public CarDownFromUserDTO(Car c)
    {
        this.id = c.getId();
        this.brand = c.getBrand();
        this.model = c.getModel();
        this.vrp = c.getVrp();
        this.colour = c.getColour();
        
        if (c.getUser() != null) {
            this.owner = new IdDTO(c.getUser().getId());
        }
        
        this.reservations = new ArrayList<>();
        
        if (c.getReservations() != null) {
            for (Reservation r: c.getReservations()) {
                
                
                
                this.reservations.add(new ReservationDownFromCarDTO(r));
                
            }
        }
        
        if (c.getType() != null) {
            this.type = new CarTypeDTO(c.getType());
        }
    }
    
    public CarDownFromUserDTO(){}
}
