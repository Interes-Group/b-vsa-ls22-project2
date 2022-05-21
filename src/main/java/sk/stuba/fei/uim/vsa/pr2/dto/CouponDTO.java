/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;


import sk.stuba.fei.uim.vsa.pr1.domain.Coupon;
/**
 *
 * @author sheax
 */
public class CouponDTO {
    
    public Long id;
    public String name;
    public Integer discount;
    
    public CouponDTO(Coupon coupon)
    {
        this.id = coupon.getId();
        this.name = coupon.getName();
        this.discount = coupon.getDiscount();
    }
    
    public CouponDTO(){}
}
