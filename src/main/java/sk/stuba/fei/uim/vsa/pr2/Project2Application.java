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
import sk.stuba.fei.uim.vsa.pr2.resources.CarParkResource;

/**
 *
 * @author sheax
 */
@ApplicationPath("/api")
public class Project2Application extends Application {
    
    
    static final Set<Class<?>> resourceClasses = new HashSet<>();
    
    static {
        resourceClasses.add(CarParkResource.class);
    }

    @Override
    public Set<Class<?>> getClasses() {
        //To change body of generated methods, choose Tools | Templates.
        return resourceClasses; 
    }
    
    
    
}
