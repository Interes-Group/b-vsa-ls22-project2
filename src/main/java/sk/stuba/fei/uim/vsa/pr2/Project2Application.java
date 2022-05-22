/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;
import sk.stuba.fei.uim.vsa.pr2.resources.CarParkFloorResource;
import sk.stuba.fei.uim.vsa.pr2.resources.CarParkResource;
import sk.stuba.fei.uim.vsa.pr2.resources.CarResource;
import sk.stuba.fei.uim.vsa.pr2.resources.CarTypeResource;
import sk.stuba.fei.uim.vsa.pr2.resources.CouponResource;
import sk.stuba.fei.uim.vsa.pr2.resources.HolidayResource;
import sk.stuba.fei.uim.vsa.pr2.resources.ParkingSpotResource;
import sk.stuba.fei.uim.vsa.pr2.resources.ParkingSpotResource2;
import sk.stuba.fei.uim.vsa.pr2.resources.ParkingSpotResource3;
import sk.stuba.fei.uim.vsa.pr2.resources.ReservationResource;
import sk.stuba.fei.uim.vsa.pr2.resources.UserResource;

/**
 *
 * @author sheax
 */
@ApplicationPath("/api")
public class Project2Application extends Application {
    
    
    static final Set<Class<?>> resourceClasses = new HashSet<>();
    
    static {
        resourceClasses.add(ParkingSpotResource.class);
        resourceClasses.add(ParkingSpotResource2.class);
        resourceClasses.add(ParkingSpotResource3.class);
        resourceClasses.add(CarParkResource.class);
        resourceClasses.add(CarParkFloorResource.class);
        resourceClasses.add(CarResource.class);
        resourceClasses.add(UserResource.class);
        resourceClasses.add(CouponResource.class);
        resourceClasses.add(CarTypeResource.class);
        resourceClasses.add(HolidayResource.class);
        resourceClasses.add(ReservationResource.class);
        
    }

    @Override
    public Set<Class<?>> getClasses() {
        //To change body of generated methods, choose Tools | Templates.
        return resourceClasses; 
    }
    
    
    
}
