/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.domain.User;

/**
 *
 * @author sheax
 */
public class UserDownFromCarDTO {
    
    public Long id;
    public String firstName;
    public String lastName;
    public String email;
    public List<IdDTO> cars;
    
    public List<CouponDTO> coupons;
    
    public UserDownFromCarDTO(User u)
    {
        this.id = u.getId();
        this.firstName = u.getFirstName();
        this.lastName = u.getLastName();
        this.email = u.getEmail();
        this.cars = new ArrayList<>();
        if (u.getCars() != null) {
            this.cars = u.getCars().stream().map(c -> {
                return new IdDTO(c.getId());
            }).collect(Collectors.toList());
        }
        
        this.coupons = new ArrayList<>();
        if (u.getCoupons() != null) {
            this.coupons = u.getCoupons().stream()
                .filter(c -> ! c.getUsed())
                .map(
                    c -> {
                        return new CouponDTO(c.getCoupon());
                    }
                )
                .collect(Collectors.toList());
                    
        }
    }
    
    public UserDownFromCarDTO(){}
}
