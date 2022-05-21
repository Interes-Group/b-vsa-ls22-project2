/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.util.ArrayList;
import java.util.List;
import sk.stuba.fei.uim.vsa.pr1.domain.*;

/**
 *
 * @author sheax
 */
public class CarWithoutTypeTopLevelDTO {
    public Long id;
    public String brand;
    public String model;
    public String vrp;
    public String colour;
    public List<ReservationWithoutTypeDownFromCarDTO> reservations;
    public UserDownFromCarDTO owner;
    
    public CarWithoutTypeTopLevelDTO(Car c)
    {
        this.id = c.getId();
        this.brand = c.getBrand();
        this.model = c.getModel();
        this.vrp = c.getVrp();
        this.colour = c.getColour();
        this.reservations = new ArrayList<>();
        
        if (c.getReservations() != null) {
            for (Reservation r: c.getReservations()) {
                this.reservations.add(new ReservationWithoutTypeDownFromCarDTO(r));
                
            }
        }
        
        if (c.getUser() != null) {
            this.owner = new UserDownFromCarDTO(c.getUser());
        }
    }
}
