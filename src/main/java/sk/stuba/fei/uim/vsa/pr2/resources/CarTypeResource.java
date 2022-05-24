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
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.CarType;
import sk.stuba.fei.uim.vsa.pr2.dto.CarTypeDTO;

/**
 *
 * @author sheax
 */
@Path("/cartypes")
public class CarTypeResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public CarTypeResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @QueryParam("name") String name) throws JsonProcessingException
    {
        List<Object> carTypes = new ArrayList<>();
        if (name != null) {
            Object carTypeObject = this.carParkService.getCarType(name);
            if (carTypeObject != null) {
                carTypes.add(carTypeObject);
            }
        } else {
            carTypes = this.carParkService.getCarTypes();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(
            carTypes.stream().map(
                c -> {
                    return new CarTypeDTO((CarType) c);
                }
            ).collect(Collectors.toList())
        )).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id) throws JsonProcessingException
    {
        Object carExistObject = this.carParkService.getCarType(id);
        if (carExistObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(new CarTypeDTO((CarType) carExistObject))).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, String body)
    {
        try {
            CarTypeDTO request = this.jsonMapper.readValue(body, CarTypeDTO.class);
            
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.name == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Object carTypeExistsObject = this.carParkService.getCarType(request.name);
            
            if (carTypeExistsObject != null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            carTypeExistsObject = this.carParkService.createCarType(request.name);
            CarType carType = (CarType) carTypeExistsObject;
            
            this.carParkService.evictCache();
            
            return Response.status(Response.Status.CREATED).entity(new CarTypeDTO(carType)).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id)
    {
        Object deletedCarTypeObject = this.carParkService.deleteCarType(id);
        
        if (deletedCarTypeObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
