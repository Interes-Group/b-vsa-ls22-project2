/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.time.format.DateTimeFormatter;
import sk.stuba.fei.uim.vsa.pr1.domain.Holiday;
/**
 *
 * @author sheax
 */
public class HolidayDTO {
    
    public Long id;
    public String name;
    public String date;
    
    public HolidayDTO(Holiday h)
    {
        this.id = h.getId();
        this.name = h.getName();
        this.date = h.getDay().format(DateTimeFormatter.ISO_DATE);
    }
    
    public HolidayDTO(){}
}
