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
import java.util.List;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.*;
import sk.stuba.fei.uim.vsa.pr1.domain.CarPark;
import sk.stuba.fei.uim.vsa.pr2.dto.CarParkDTO;
import sk.stuba.fei.uim.vsa.pr2.dto.CarParkFloorDTO;
import sk.stuba.fei.uim.vsa.pr2.dto.ParkingSpotDTO;

/**
 *
 * @author sheax
 */
@Path("/carparks")
public class CarParkResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public CarParkResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index( @HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @QueryParam("name") String name ) throws JsonProcessingException
    {
        List<CarParkDTO> result = new ArrayList<>();
        if (name != null) {
            Object carPark = carParkService.getCarPark(name);
            if (carPark != null) {
                result.add(new CarParkDTO((CarPark) carPark));
            }
        } else {
            List<Object> carParks = carParkService.getCarParks();
            for (Object carPark : carParks) {
                 result.add(new CarParkDTO((CarPark) carPark));
            }
        }
        return Response.ok(this.jsonMapper.writeValueAsString(result)).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id) throws JsonProcessingException
    {
        Object carPark = this.carParkService.getCarPark(id);
        if (carPark != null) {
            CarParkDTO result = new CarParkDTO((CarPark) carPark);
            return Response.ok(this.jsonMapper.writeValueAsString(result)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, String body)
    {
        try {
            CarParkDTO request = this.jsonMapper.readValue(body, CarParkDTO.class);
        
        
            if (request != null) {
                Object created = this.carParkService.createCarPark(request.name, request.address, request.prices);
                if (created != null) {
                    CarPark createdCarPark = (CarPark) created;

                    if (request.floors != null) {
                        for (CarParkFloorDTO carParkFloor : request.floors) {
                            Object createdFloor = this.carParkService.createCarParkFloor(createdCarPark.getId(), carParkFloor.identifier);
                            if (createdFloor != null) {
                                CarParkFloor createdCarParkFloor = (CarParkFloor) createdFloor;

                                if (carParkFloor.spots != null) {
                                    for (ParkingSpotDTO spot: carParkFloor.spots) {
                                        if (spot.type != null) {
                                            this.carParkService.createParkingSpot(createdCarPark.getId(), createdCarParkFloor.getEmbeddedId().getIdentifier(), spot.identifier, spot.type.id);
                                        } else {
                                            this.carParkService.createParkingSpot(createdCarPark.getId(), createdCarParkFloor.getEmbeddedId().getIdentifier(), spot.identifier);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    created = this.carParkService.getCarPark(createdCarPark.getId());
                    CarParkDTO result = new CarParkDTO((CarPark) created);

                    return Response.status(Response.Status.CREATED).entity(this.jsonMapper.writeValueAsString(result)).build();
                }

            }
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            Object c = e;
        }
        
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

        
    }
    
    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, String body)
    {
        try {
            CarParkDTO carParkBody = this.jsonMapper.readValue(body, CarParkDTO.class);
            Object storedCarParkObject = this.carParkService.getCarPark(id);
            if (storedCarParkObject == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            CarPark storedCarPark = (CarPark) storedCarParkObject;
            
            storedCarPark.setAddress(carParkBody.address);
            storedCarPark.setName(carParkBody.name);
            storedCarPark.setPricePerHour(carParkBody.prices);
            
            this.carParkService.updateCarPark(storedCarParkObject);
            
            storedCarParkObject = this.carParkService.getCarPark(id);
            storedCarPark = (CarPark) storedCarParkObject;
            this.carParkService.evictCache();
            return Response.ok(new CarParkDTO(storedCarPark)).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @DELETE
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id)
    {
        Object deleted = this.carParkService.deleteCarPark(id);
        this.carParkService.evictCache();
        if (deleted != null) {
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    
}
