/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.domain.ParkingSpot;
import sk.stuba.fei.uim.vsa.pr1.domain.Reservation;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;

/**
 *
 * @author sheax
 */
public class ParkingSpotDownFromReservationDTO {
    public Long id;
    public String carParkFloor;
    public Long carPark;
    public Boolean free;
    public String identifier;
    public List<IdDTO> reservations;
    
    public ParkingSpotDownFromReservationDTO(ParkingSpot parkingSpot)
    {
        this.id = parkingSpot.getId();
        this.identifier = parkingSpot.getSpotIdentifier();
        CarParkService service = new CarParkService();
        this.free =  service.isParkingSpotAvailable(parkingSpot);
        
        this.carParkFloor = parkingSpot.getCarParkFloor().getEmbeddedId().getIdentifier();
        this.carPark = parkingSpot.getCarParkFloor().getCarPark().getId();
        this.reservations = new ArrayList<>();
        
        if (parkingSpot.getReservations() != null) {
            this.reservations = parkingSpot.getReservations().stream().map(o -> {
                Reservation r = (Reservation) o;
                return new IdDTO(r.getId());
            }).collect(Collectors.toList());
        }
    }
    
    public ParkingSpotDownFromReservationDTO(){}
}
