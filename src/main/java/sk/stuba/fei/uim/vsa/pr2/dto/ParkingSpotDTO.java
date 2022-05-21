/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.ParkingSpot;
import sk.stuba.fei.uim.vsa.pr1.domain.Reservation;

/**
 *
 * @author sheax
 */
public class ParkingSpotDTO {
    public Long id;
    public String identifier;
    public String carParkFloor;
    public Long carPark;
    public Boolean free;
    public List<ReservationDownFromParkingSpotDTO> reservations;
    public CarTypeDTO type;
    
    public ParkingSpotDTO(ParkingSpot parkingSpot)
    {
        this.id = parkingSpot.getId();
        this.identifier = parkingSpot.getSpotIdentifier();
        this.carParkFloor = parkingSpot.getCarParkFloor().getEmbeddedId().getIdentifier();
        this.carPark = parkingSpot.getCarParkFloor().getCarPark().getId();
        CarParkService service = new CarParkService();
        this.free =  service.isParkingSpotAvailable(parkingSpot);
        
        this.reservations = new ArrayList<>();
        if (parkingSpot.getReservations() != null) {
            parkingSpot.getReservations().stream().map(o -> {
                Reservation r = (Reservation) o;
                return new ReservationDownFromParkingSpotDTO(r);
            }).collect(Collectors.toList());
        }
        
        if (parkingSpot.getType() != null) {
            this.type = new CarTypeDTO(parkingSpot.getType());
        }
    }
    
    public ParkingSpotDTO(){}
}


