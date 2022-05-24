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
import sk.stuba.fei.uim.vsa.pr1.domain.Coupon;
import sk.stuba.fei.uim.vsa.pr2.dto.CouponDTO;

/**
 *
 * @author sheax
 */
@Path("/coupons")
public class CouponResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public CouponResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @QueryParam("user") Long userId) throws JsonProcessingException
    {
        List<Object> couponObjects = null;
        if (userId != null) {
            couponObjects = this.carParkService.getCoupons(userId);
        } else {
            couponObjects = this.carParkService.getAllCoupons();
        }
        
        if (couponObjects == null) {
            couponObjects = new ArrayList<>();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(
            couponObjects.stream().map(
                c -> {
                    return new CouponDTO((Coupon) c);
                }
            ).collect(Collectors.toList())
        )).build();
    }
    
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id) throws JsonProcessingException
    {
        Object couponObject = this.carParkService.getCoupon(id);
        if (couponObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(new CouponDTO((Coupon) couponObject))).build();
    }
    
    @GET
    @Path("/{id}/give/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response giveToUser(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, @PathParam("userId") Long userId) throws JsonProcessingException
    {
        Object couponObject = this.carParkService.getCoupon(id);
        if (couponObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        Object userObject = this.carParkService.getUser(userId);
        if (userObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        this.carParkService.giveCouponToUser(id, userId);
        this.carParkService.evictCache();
        
        return Response.ok(this.jsonMapper.writeValueAsString(new CouponDTO((Coupon) couponObject))).build();
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, String body)
    {
        try {
            CouponDTO request = this.jsonMapper.readValue(body, CouponDTO.class);
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (request.discount == null || request.name == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Object couponObject = this.carParkService.createDiscountCoupon(request.name, request.discount);
            this.carParkService.evictCache();
            
            return Response.status(Response.Status.CREATED).entity(this.jsonMapper.writeValueAsString(new CouponDTO((Coupon) couponObject))).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response delete(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id)
    {
        Object couponExistsObject = this.carParkService.deleteCoupon(id);
        if (couponExistsObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
