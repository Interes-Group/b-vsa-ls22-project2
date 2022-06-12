/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.*;
import sk.stuba.fei.uim.vsa.pr2.dto.CarParkFloorDTO;
import sk.stuba.fei.uim.vsa.pr2.dto.CarTypeDTO;
import sk.stuba.fei.uim.vsa.pr2.dto.ParkingSpotDTO;

/**
 *
 * @author sheax
 */
@Path("/carparks/{id}/floors")
public class CarParkFloorResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public CarParkFloorResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id) throws JsonProcessingException
    {
        Object carParkObject = this.carParkService.getCarPark(id);
        if (carParkObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        CarPark carPark = (CarPark) carParkObject;
        return Response.ok(this.jsonMapper.writeValueAsString(
                carPark.getCarParkFloorList().stream().map(
                        f -> {
                            return new CarParkFloorDTO(f);
                        }
                ).collect(Collectors.toList())
            )
        ).build();
    }
    
    @GET
    @Path("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, @PathParam("identifier") String identifier) throws JsonProcessingException
    {
        Object carParkFloorObject = this.carParkService.getCarParkFloor(id, identifier);
        if (carParkFloorObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        CarParkFloor carParkFloor = (CarParkFloor) carParkFloorObject;
        
        return Response.ok(this.jsonMapper.writeValueAsString(new CarParkFloorDTO(carParkFloor))).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, String body) throws JsonProcessingException {
        
        Object existsCarPark = this.carParkService.getCarPark(id);
        if (existsCarPark == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        try {
            CarParkFloorDTO request = this.jsonMapper.readValue(body, CarParkFloorDTO.class);
            if (request == null) {
                 return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.identifier == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            
            
            if (request.spots != null) {
                Set<String> newSpotIdentifiers = new HashSet<>();
                for (ParkingSpotDTO spot: request.spots) {
                    if (spot.identifier == null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    if (newSpotIdentifiers.contains(spot.identifier)) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    newSpotIdentifiers.add(spot.identifier);
                    
                    if (this.carParkService.parkingSpotExists(id, spot.identifier)) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    
                    if (spot.type != null) {
                        if (spot.type.id != null) {
                            Object existTypeObject = this.carParkService.getCarType(spot.type.id);
                            if (existTypeObject == null) {
                                spot.type.id = null;
                                if (spot.type.name == null) {
                                    return Response.status(Response.Status.BAD_REQUEST).build();
                                }
                                existTypeObject = this.carParkService.getCarType(spot.type.name);
                                if (existTypeObject != null) {
                                    return Response.status(Response.Status.BAD_REQUEST).build();
                                }
                            }
                        } else {
                            if (spot.type.name == null) {
                                return Response.status(Response.Status.BAD_REQUEST).build();
                            }
                            Object existTypeObject = this.carParkService.getCarType(spot.type.name);
                            if (existTypeObject != null) {
                                return Response.status(Response.Status.BAD_REQUEST).build();
                            }
                        }
                    }
                }
            }
            
            Object createdCarParkFloorObject = this.carParkService.createCarParkFloor(id, request.identifier);
            if (createdCarParkFloorObject == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            CarParkFloor createdCarParkFloor = (CarParkFloor) createdCarParkFloorObject;
            
            if (request.spots != null) {
                for (ParkingSpotDTO spot: request.spots) {
                    if (spot.type != null) {                    
                        CarTypeDTO carType = spot.type;
                        if (carType.id != null) {
                            this.carParkService.createParkingSpot(id, createdCarParkFloor.getEmbeddedId().getIdentifier(), spot.identifier, spot.type.id);
                        } else {
                            Object carTypeExistObject = this.carParkService.getCarType(carType.name);
                            if (carTypeExistObject == null) {
                                carTypeExistObject = this.carParkService.createCarType(carType.name);
                            }
                            CarType carTypeExist = (CarType) carTypeExistObject;
                            this.carParkService.createParkingSpot(id, createdCarParkFloor.getEmbeddedId().getIdentifier(), spot.identifier, carTypeExist.getId());
                        }

                    } else {
                        this.carParkService.createParkingSpot(id, createdCarParkFloor.getEmbeddedId().getIdentifier(), spot.identifier);
                    }
                }
            }
            this.carParkService.evictCache();
            createdCarParkFloorObject = this.carParkService.getCarParkFloor(id, request.identifier);
            createdCarParkFloor = (CarParkFloor) createdCarParkFloorObject;
            
            
            return Response.status(Response.Status.CREATED).entity(this.jsonMapper.writeValueAsString(new CarParkFloorDTO(createdCarParkFloor))).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @DELETE
    @Path("/{identifier}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, @PathParam("identifier") String identifier) {
        Object carParkFloorObject = this.carParkService.getCarParkFloor(id, identifier);
        if (carParkFloorObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.carParkService.deleteCarParkFloor(id, identifier);
        this.carParkService.evictCache();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
