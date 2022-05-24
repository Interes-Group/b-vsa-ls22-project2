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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.ParkingSpot;
import sk.stuba.fei.uim.vsa.pr2.dto.ParkingSpotDTO;

/**
 *
 * @author sheax
 */
 @Path("/parkingspots/{id}")
public class ParkingSpotResource3 {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public ParkingSpotResource3()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long spotId) throws JsonProcessingException
    {
        Object parkingSpotObject = this.carParkService.getParkingSpot(spotId);
        if (parkingSpotObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(new ParkingSpotDTO((ParkingSpot)parkingSpotObject))).build();
    }
    
    
    
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @PUT
    public Response update(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long stopId, String body) throws JsonProcessingException
    {
        Object parkingSpotExists = this.carParkService.getParkingSpot(stopId);
        if (parkingSpotExists == null) {
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
            
            
            
            Object parkingSpotObject = this.carParkService.getParkingSpot(stopId);
            if (parkingSpotObject == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            
            
            ParkingSpot parkingSpot = (ParkingSpot) parkingSpotObject;
            
            if (! request.identifier.equals(parkingSpot.getSpotIdentifier())) {
                if (this.carParkService.parkingSpotExists(parkingSpot.getCarParkFloor().getCarPark().getId(), request.identifier)) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            parkingSpot.setSpotIdentifier(request.identifier);
            this.carParkService.updateParkingSpot(parkingSpot);
            this.carParkService.evictCache();
            parkingSpotObject = this.carParkService.getParkingSpot(stopId);
            parkingSpot = (ParkingSpot) parkingSpotObject;
            
            return Response.ok(this.jsonMapper.writeValueAsString(new ParkingSpotDTO(parkingSpot))).build();
            
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long spotId)
    {
        Object parkingSpotObject  = this.carParkService.deleteParkingSpot(spotId);
        if (parkingSpotObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        this.carParkService.evictCache();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
