/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.time.format.DateTimeFormatter;
import sk.stuba.fei.uim.vsa.pr1.domain.Reservation;

/**
 *
 * @author sheax
 */
public class ReservationDownFromParkingSpotDTO {
    public Long id;
    public String start;
    public String end;
    public IdDTO spot;
    public Double prices;
    public Object car;
    public CouponDTO coupon;
    
    public ReservationDownFromParkingSpotDTO(Reservation reservation)
    {
        this.id = reservation.getId();
        if (reservation.getStartsAt() != null) {
            this.start = reservation.getStartsAt().format(DateTimeFormatter.ISO_DATE_TIME);
        }
        if (reservation.getEndsAt() != null) {
            this.end = reservation.getEndsAt().format(DateTimeFormatter.ISO_DATE_TIME);
        }
        
        if (reservation.getPrice() != null) {
            this.prices = reservation.getPrice();
        }
        
        if (reservation.getParkingSpot() != null) {
            this.spot = new IdDTO(reservation.getParkingSpot().getId());  
        } 
        
        if (reservation.getCar() != null) {
            this.car = new CarDownFromReservationDTO(reservation.getCar());
        }
        
        if (reservation.getCoupon() != null) {
            this.coupon = new CouponDTO(reservation.getCoupon());
        }
    }
    
    public ReservationDownFromParkingSpotDTO(){}
}
