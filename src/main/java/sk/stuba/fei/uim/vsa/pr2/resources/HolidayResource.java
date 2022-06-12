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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.Holiday;
import sk.stuba.fei.uim.vsa.pr2.dto.HolidayDTO;

/**
 *
 * @author sheax
 */
@Path("/holidays")
public class HolidayResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public HolidayResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @QueryParam("date") String date) throws JsonProcessingException
    {
        List<Object> holidayObjects = new ArrayList<>();
        if (date != null) {
            try {
                Date d=new SimpleDateFormat("yyyy-MM-dd").parse(date);
                
                if (d == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                
                Object holiday = this.carParkService.getHoliday(d);
                if (holiday != null) {
                    holidayObjects.add(holiday);
                }
                
            } catch (ParseException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            holidayObjects = this.carParkService.getHolidays();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(
            holidayObjects.stream().map(
                h -> {
                    return new HolidayDTO((Holiday) h);
                }
            ).collect(Collectors.toList())
        )).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, String body)
    {
        try {
            HolidayDTO request = this.jsonMapper.readValue(body, HolidayDTO.class);
            
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.name == null || request.date == null ) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Date d=new SimpleDateFormat("yyyy-MM-dd").parse(request.date);
            
            if (d == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Object holidayExists = this.carParkService.getHoliday(d);
            if (holidayExists != null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            holidayExists = this.carParkService.createHoliday(request.name, d);
            
            this.carParkService.evictCache();
            
            return Response.status(Response.Status.CREATED).entity(new HolidayDTO((Holiday) holidayExists)).build();
            
        } catch (JsonProcessingException| ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @Path("/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id)
    {
        Object deletedHoliday = this.carParkService.deleteHoliday(id);
        if (deletedHoliday == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
