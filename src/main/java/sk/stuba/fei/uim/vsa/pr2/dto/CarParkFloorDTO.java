/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import sk.stuba.fei.uim.vsa.pr1.domain.CarParkFloor;
import sk.stuba.fei.uim.vsa.pr1.domain.ParkingSpot;

/**
 *
 * @author sheax
 */
public class CarParkFloorDTO {
    public String identifier;
    public Long carPark;
    public List<ParkingSpotDTO> spots;
    public Long id;
    
    public CarParkFloorDTO(CarParkFloor floor)
    {
        this.identifier = floor.getEmbeddedId().getIdentifier();
        this.carPark = floor.getCarPark().getId();
        this.spots = new ArrayList<>();
        
        if (floor.getParkingSpots() != null) {
            for (ParkingSpot spot: floor.getParkingSpots()) {
                
                
                this.spots.add(new ParkingSpotDTO(spot));
            }
        }
        
        
    }
    
    public CarParkFloorDTO(){}
}
