/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import sk.stuba.fei.uim.vsa.pr1.domain.CarType;

/**
 *
 * @author sheax
 */
public class CarTypeDTO {
    public Long id;
    public String name;
    
    public CarTypeDTO(CarType type)
    {
        this.id = type.getId();
        this.name = type.getName();
    }
    
    public CarTypeDTO(){}
}
