/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import static java.lang.Math.floor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.domain.CarPark;

/**
 *
 * @author sheax
 */
public class CarParkDTO {
    public Long id;
    public String name;
    public String address;
    public Integer prices;
    public List<CarParkFloorDTO> floors;
    
    public CarParkDTO(CarPark carPark)
    {
        this.id = carPark.getId();
        this.name = carPark.getName();
        this.address = carPark.getAddress();
        this.prices = carPark.getPricePerHour();
        
        this.floors = new ArrayList<>();
        
        if (carPark.getCarParkFloorList() != null) {
             this.floors = carPark.getCarParkFloorList().stream().map(f -> {
                return new CarParkFloorDTO(f);
            }).collect(Collectors.toList());
        }
    }
    
    public CarParkDTO(){}
}
