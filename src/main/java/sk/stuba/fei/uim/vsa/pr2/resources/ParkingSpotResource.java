/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.CarPark;
import sk.stuba.fei.uim.vsa.pr1.domain.ParkingSpot;
import sk.stuba.fei.uim.vsa.pr2.dto.ParkingSpotDTO;
import sk.stuba.fei.uim.vsa.pr1.domain.CarParkFloor;

/**
 *
 * @author sheax
 */
@Path("carparks/{id}/spots")
public class ParkingSpotResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public ParkingSpotResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, @QueryParam("free") Boolean free) throws JsonProcessingException
    {
        
        Object carParkObject = this.carParkService.getCarPark(id);
        if (carParkObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        CarPark carPark = (CarPark) carParkObject;
        Map<String, List<Object>> parkingSpotMapObjects = new HashMap<>();
        
        List<Object> parkingSpotObjects = new ArrayList<>();
        
        if (free == null) {
            parkingSpotMapObjects = this.carParkService.getParkingSpots(id);
            
        } else if (free) {
            parkingSpotMapObjects = this.carParkService.getAvailableParkingSpots(carPark.getName());
        } else {
            parkingSpotMapObjects = this.carParkService.getOccupiedParkingSpots(carPark.getName());
        }
        
        for (List<Object> spotObjects: parkingSpotMapObjects.values()) {
            parkingSpotObjects.addAll(spotObjects);
        }
        return Response.ok(
            this.jsonMapper.writeValueAsString(
                parkingSpotObjects.stream().map(
                        p -> {
                            return new ParkingSpotDTO((ParkingSpot)p );
                        }
                ).collect(Collectors.toList())
            )
        ).build();
    }
    
    
    
    
    
    
}
