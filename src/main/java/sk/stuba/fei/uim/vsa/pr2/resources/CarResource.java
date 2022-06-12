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
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.Car;
import sk.stuba.fei.uim.vsa.pr1.domain.CarType;
import sk.stuba.fei.uim.vsa.pr1.domain.User;
import sk.stuba.fei.uim.vsa.pr2.dto.CarDTO;

/**
 *
 * @author sheax
 */
@Path("/cars")
public class CarResource {
    
    private final CarParkService carParkService;
    
    private final ObjectMapper jsonMapper;
    
    public CarResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @QueryParam("user") Long userId, @QueryParam("vrp") String vrp) throws JsonProcessingException
    {
        List<CarDTO> resultList = new ArrayList<>();
        if (userId != null) {
            if (vrp != null) {
                Object carObject = this.carParkService.getCar(vrp);
                if (carObject == null) {
                    return Response.ok(this.jsonMapper.writeValueAsString(resultList)).build();
                }
                Car car = (Car) carObject;
                if (car.getUser() == null || ! car.getUser().getId().equals(userId)) {
                    return Response.ok(this.jsonMapper.writeValueAsString(resultList)).build();
                } else {
                    resultList.add(new CarDTO(car));
                    return Response.ok(this.jsonMapper.writeValueAsString(resultList)).build();
                }
            } else {
                Object userObject = this.carParkService.getUser(userId);
                if (userObject == null) {
                    return Response.ok(this.jsonMapper.writeValueAsString(resultList)).build();
                }
                User user = (User) userObject;
                
                return Response.ok(this.jsonMapper.writeValueAsString(
                    user.getCars().stream().map(
                        c -> {
                                return new CarDTO(c);
                            }
                    )
                    .collect(Collectors.toList())
                )).build();
            }
        } else if (vrp != null) {
            Object carObject = this.carParkService.getCar(vrp);
            if (carObject == null) {
                return Response.ok(this.jsonMapper.writeValueAsString(resultList)).build();
            }
            resultList.add(new CarDTO((Car) carObject));
            return Response.ok(this.jsonMapper.writeValueAsString(resultList)).build();
        }
        
        List<Car> cars = this.carParkService.getAllCars();
        
        return Response.ok(this.jsonMapper.writeValueAsString(
            cars.stream().map(
                c -> {
                    return new CarDTO(c);
                }
            ).collect(Collectors.toList())
        )).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id) throws JsonProcessingException
    {
        Object carObject = this.carParkService.getCar(id);
        if (carObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(new CarDTO((Car)carObject))).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, String body)
    {
        try {
            Long carTypeId = null;
            Long userId = null;
            CarDTO request = this.jsonMapper.readValue(body, CarDTO.class);
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (request.brand == null || request.colour == null || request.model == null || request.vrp == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.owner == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Object carExists = this.carParkService.getCar(request.vrp);
            if (carExists != null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            
            if (request.owner.id != null) {
                Object userExistsObject = this.carParkService.getUser(request.owner.id);
                if (userExistsObject == null) {
                    request.owner.id = null;
                    if (request.owner.email == null || request.owner.firstName == null || request.owner.lastName == null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                } else {
                    userId = request.owner.id;
                }
                
            } else {
                if (request.owner.email == null || request.owner.firstName == null || request.owner.lastName == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                Object userExistsObject = this.carParkService.getUser(request.owner.email);
                if (userExistsObject != null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            
            
            if (request.type != null) {
                if (request.type.id != null) {
                    Object typeExistsObject = this.carParkService.getCarType(request.type.id);
                    if (typeExistsObject == null) {
                        request.type.id = null;
                        if (request.type.name == null) {
                            return Response.status(Response.Status.BAD_REQUEST).build();
                        }
                        typeExistsObject = this.carParkService.getCarType(request.type.name);
                        if (typeExistsObject != null) {
                            return Response.status(Response.Status.BAD_REQUEST).build();
                        }
                    }
                    carTypeId = request.type.id;
                } else {
                    if (request.type.name == null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    Object typeExistsObject = this.carParkService.getCarType(request.type.name);
                    if (typeExistsObject != null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                }
            }
            
            if (userId == null) {
                Object userObject = this.carParkService.createUser(request.owner.firstName, request.owner.lastName, request.owner.email);
                userId = ((User) userObject).getId();
            }
            
            if (carTypeId == null) {
                if (request.type != null) {
                    Object carTypeObject = this.carParkService.createCarType(request.type.name);
                    carTypeId = ((CarType) carTypeObject).getId();
                }
            }
            Object carObject = null;
            if (carTypeId != null) {
                carObject = this.carParkService.createCar(userId, request.brand, request.model, request.colour, request.vrp, carTypeId);
            } else {
                 carObject = this.carParkService.createCar(userId, request.brand, request.model, request.colour, request.vrp);
            }
            
            this.carParkService.evictCache();
            
            Long carId = ((Car) carObject).getId();
            
            return Response.status(Response.Status.CREATED).entity(
                    this.jsonMapper.writeValueAsString(
                            new CarDTO((Car)this.carParkService.getCar(carId))
                    )
                    
            ).build();
            
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, String body) throws JsonProcessingException
    {
        Object carExistsObject = this.carParkService.getCar(id);
        if (carExistsObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Car carExist = (Car) carExistsObject;
        
        try {
            CarDTO request = this.jsonMapper.readValue(body, CarDTO.class);
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (request.brand == null || request.colour == null || request.model == null || request.vrp == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (! carExist.getVrp().equals(request.vrp)) {
                Object carVrpObject = this.carParkService.getCar(request.vrp);
                if (carVrpObject != null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            
            carExist.setBrand(request.brand);
            carExist.setColour(request.colour);
            carExist.setModel(request.model);
            carExist.setVrp(request.vrp);
            
            this.carParkService.updateCar(carExist);
            
            this.carParkService.evictCache();
            
            carExistsObject = this.carParkService.getCar(id);
            carExist = (Car) carExistsObject;
            return Response.ok(this.jsonMapper.writeValueAsString(new CarDTO(carExist))).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id)
    {
        Object carExistObject = this.carParkService.getCar(id);
        if (carExistObject == null) {
             return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        this.carParkService.deleteCar(id);
        
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
