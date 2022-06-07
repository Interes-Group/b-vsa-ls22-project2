/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.CarParkFloor;
import sk.stuba.fei.uim.vsa.pr1.domain.CarType;
import sk.stuba.fei.uim.vsa.pr1.domain.ParkingSpot;
import sk.stuba.fei.uim.vsa.pr2.dto.ParkingSpotDTO;

/**
 *
 * @author sheax
 */
@Path("/carparks/{id}/floors/{identifier}/spots")
public class ParkingSpotResource2 {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public ParkingSpotResource2()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, @PathParam("identifier") String identifier) throws JsonProcessingException
    {
        Object carParkFloorObject = this.carParkService.getCarParkFloor(id, identifier);
        if (carParkFloorObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        CarParkFloor carParkFloor = (CarParkFloor) carParkFloorObject;
        
        return Response.ok(
            this.jsonMapper.writeValueAsString(
                carParkFloor.getParkingSpots().stream().map(
                    p -> {
                        return new ParkingSpotDTO((ParkingSpot)p);
                    }
                )
                .collect(Collectors.toList())
            )   
        ).build();
        
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, @PathParam("identifier") String identifier, String body) throws JsonProcessingException
    {
        
        Object existsParkingFloorObject = this.carParkService.getCarParkFloor(id, identifier);
        if (existsParkingFloorObject == null) {
             return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        try {
            ParkingSpotDTO request = this.jsonMapper.readValue(body, ParkingSpotDTO.class);
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (request.identifier == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (this.carParkService.parkingSpotExists(id, request.identifier)) {
                 return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.type != null) {
                if (request.type.id != null) {
                    Object carTypeExists = this.carParkService.getCarType(request.type.id);
                    if (carTypeExists == null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                } else { 
                    if (request.type.name == null) {
                     return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    Object carTypeExists = this.carParkService.getCarType(request.type.name);
                    if (carTypeExists != null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                }
            }
            
            Object createdSpotObject;
            if (request.type == null) {
                createdSpotObject = this.carParkService.createParkingSpot(id, identifier, request.identifier);
                
            } else {
                if (request.type.id != null) {
                    createdSpotObject = this.carParkService.createParkingSpot(id, identifier, request.identifier, request.type.id);
                } else {
                    Object carTypeExistsObject = this.carParkService.getCarType(request.type.name);
                    if (carTypeExistsObject == null) {
                        carTypeExistsObject = this.carParkService.createCarType(request.type.name);
                    }
                    
                    CarType carTypeExists = (CarType) carTypeExistsObject;
                    createdSpotObject = this.carParkService.createParkingSpot(id, identifier, request.identifier, carTypeExists.getId());
                }
               
            }
            if (createdSpotObject == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            ParkingSpot createdParkingSpot = (ParkingSpot) createdSpotObject;
            this.carParkService.evictCache();
            createdSpotObject = this.carParkService.getParkingSpot(createdParkingSpot.getId());
            createdParkingSpot = (ParkingSpot) createdSpotObject;
            //this.carParkService.evictCache();
            return Response.status(Response.Status.CREATED).entity(this.jsonMapper.writeValueAsString(new ParkingSpotDTO(createdParkingSpot))).build();
            
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
}
