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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.CarType;
import sk.stuba.fei.uim.vsa.pr1.domain.User;
import sk.stuba.fei.uim.vsa.pr2.dto.CarDownFromUserDTO;
import sk.stuba.fei.uim.vsa.pr2.dto.UserDTO;

/**
 *
 * @author sheax
 */
@Path("/users")
public class UserResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public UserResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @QueryParam("email") String email) throws JsonProcessingException
    {
        if (email != null) {
            List<UserDTO> resultList = new ArrayList<>();
            Object userObject = this.carParkService.getUser(email);
            if (userObject != null) {
                resultList.add(new UserDTO((User)userObject));
               
            }
            return Response.ok(this.jsonMapper.writeValueAsString(resultList)).build();  
        }
        
        List<Object> userList = this.carParkService.getUsers();
        
        return Response.ok(this.jsonMapper.writeValueAsString(
            userList.stream().map(
                u -> {
                    return new UserDTO((User)u);
                }
            ).collect(Collectors.toList())
        )).build();
    }
    
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id) throws JsonProcessingException
    {
        Object userObject = this.carParkService.getUser(id);
        if (userObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(new UserDTO((User) userObject))).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, String body) throws JsonProcessingException
    {
        try {
            //Long typeId = null;
            UserDTO request = this.jsonMapper.readValue(body, UserDTO.class);
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.email == null || request.firstName == null || request.lastName == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Object userExistsObject = this.carParkService.getUser(request.email);
            if (userExistsObject != null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.cars != null) {
                Set<String> carVRPList = new HashSet<>();
                for (CarDownFromUserDTO car: request.cars) {
                    if (car.brand == null || car.colour == null || car.model == null || car.vrp == null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    if (carVRPList.contains(car.vrp)) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    carVRPList.add(car.vrp);
                    
                    Object carExistsObject = this.carParkService.getCar(car.vrp);
                    if (carExistsObject != null) {
                        return Response.status(Response.Status.BAD_REQUEST).build();
                    }
                    
                    if (car.type != null) {
                        if (car.type.id != null) {
                            Object typeExistsObject = this.carParkService.getCarType(car.type.id);
                            if (typeExistsObject == null) {
                                car.type.id = null;
                                if (car.type.name == null) {
                                    return Response.status(Response.Status.BAD_REQUEST).build();
                                }
                                typeExistsObject = this.carParkService.getCarType(car.type.name);
                                if (typeExistsObject != null) {
                                    return Response.status(Response.Status.BAD_REQUEST).build();
                                }
                            }
                        } else {
                            if (car.type.name == null) {
                                return Response.status(Response.Status.BAD_REQUEST).build();
                            }
                            Object typeExistsObject = this.carParkService.getCarType(car.type.name);
                            if (typeExistsObject != null) {
                                return Response.status(Response.Status.BAD_REQUEST).build();
                            }
                        }
                    }
                }
            }
            
            Object userCreatedObject = this.carParkService.createUser(request.firstName, request.lastName, request.email);
            User userCreated = (User) userCreatedObject;
            
            if (request.cars != null) {
                for (CarDownFromUserDTO car : request.cars) {
                    if (car.type != null) {
                        if (car.type.id != null) {
                            this.carParkService.createCar(userCreated.getId(), car.brand, car.model, car.colour, car.vrp, car.type.id);
                        } else {
                            Object carTypeObject = this.carParkService.getCarType(car.type.name);
                            if (carTypeObject == null) {
                                carTypeObject = this.carParkService.createCarType(car.type.name);
                                this.carParkService.evictCache();
                            }
                            CarType carType = (CarType) carTypeObject;
                            this.carParkService.createCar(userCreated.getId(), car.brand, car.model, car.colour, car.vrp, carType.getId());
                        }
                    } else {
                       this.carParkService.createCar(userCreated.getId(), car.brand, car.model, car.colour, car.vrp); 
                    }
                }
            }
            
            this.carParkService.evictCache();
            
            userCreatedObject = this.carParkService.getUser(userCreated.getId());
            userCreated = (User) userCreatedObject;
            
            return Response.status(Response.Status.CREATED).entity(this.jsonMapper.writeValueAsString(new UserDTO(userCreated))).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("/{id}")
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, String body)
    {
        Object userExistsObject = this.carParkService.getUser(id);
        if (userExistsObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        User userExists = (User) userExistsObject;
        
        try {
            UserDTO request = this.jsonMapper.readValue(body, UserDTO.class);
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (request.email == null || request.firstName == null || request.lastName == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (! userExists.getEmail().equals(request.email)) {
                Object anotherUserExists = this.carParkService.getUser(request.email);
                if (anotherUserExists != null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            
            userExists.setEmail(request.email);
            userExists.setFirstName(request.firstName);
            userExists.setLastName(request.lastName);
            
            this.carParkService.updateUser(userExists);
            this.carParkService.evictCache();
            
            userExistsObject = this.carParkService.getUser(userExists.getId());
            
            return Response.ok(this.jsonMapper.writeValueAsString(new UserDTO((User) userExistsObject))).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id)
    {
        Object userDeletedObject = this.carParkService.deleteUser(id);
        if (userDeletedObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
