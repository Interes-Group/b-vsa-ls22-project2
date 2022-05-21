/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.CarPark;
import sk.stuba.fei.uim.vsa.pr2.dto.CarParkDTO;

/**
 *
 * @author sheax
 */
@Path("/carPark")
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
}
