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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.Reservation;
import sk.stuba.fei.uim.vsa.pr1.domain.User;
import sk.stuba.fei.uim.vsa.pr2.Helpers;
import sk.stuba.fei.uim.vsa.pr2.dto.ReservationDTO;

/**
 *
 * @author sheax
 */
@Path("/reservations")
public class ReservationResource {
    
    private final CarParkService carParkService;
    private final ObjectMapper jsonMapper;
    
    public ReservationResource()
    {
        this.carParkService = new CarParkService();
        this.jsonMapper = new ObjectMapper();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @QueryParam("user") Long userId, @QueryParam("spot") Long spotId, @QueryParam("date") String date)
    {
        try {
            List<Object> reservations;
            if (spotId != null) {
                if (date == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                Date d=new SimpleDateFormat("yyyy-MM-dd").parse(date);
                
                reservations = this.carParkService.getReservations(spotId, d);
                if (userId != null) {
                    reservations = reservations.stream().filter(
                        r -> {
                            Reservation res = (Reservation) r;
                            return res.getCar().getUser().getId().equals(userId);
                        }
                    ).collect(Collectors.toList());
                }
                
            } else {
                if (date != null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
                if (userId != null) {
                    reservations = this.carParkService.getMyReservations(userId);
                } else {
                    reservations = this.carParkService.getAllReservations();
                }
            }
            
            return Response.ok(this.jsonMapper.writeValueAsString(
                reservations.stream().map(
                    r -> {
                         return new ReservationDTO((Reservation) r);
                    }
                ).collect(Collectors.toList())
            )).build();
            
        } catch (JsonProcessingException | ParseException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
    }
    
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response read(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id) throws JsonProcessingException
    {
        Reservation reservation = this.carParkService.getReservation(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        
        return Response.ok(this.jsonMapper.writeValueAsString(new ReservationDTO(reservation))).build();
    }
    
    @Path("/{id}/end")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response endReservation(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, @QueryParam("coupon") Long couponId) throws JsonProcessingException
    {
        User u = Helpers.getAuthorizedUser(carParkService, authorizationHeader);
        if (u == null) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        this.carParkService.evictCache();
        Reservation reservation = this.carParkService.getReservation(id);
        if (reservation == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (reservation.getEndsAt() != null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        if (couponId != null) {
            Object couponObject = this.carParkService.getCoupon(couponId);
            if (couponObject == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }
        if (! reservation.getCar().getUser().getId().equals(u.getId())) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
        this.carParkService.evictCache();
        Object endedReservationObject =null;
        if (couponId != null) {
            endedReservationObject = this.carParkService.endReservation(id, couponId);
        } else{
            endedReservationObject = this.carParkService.endReservation(id);
        }
        
        this.carParkService.evictCache();
        endedReservationObject = this.carParkService.getReservation(((Reservation) endedReservationObject).getId());
        
        
        return Response.ok(this.jsonMapper.writeValueAsString(new ReservationDTO((Reservation) endedReservationObject))).build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, String body)
    {
        try {
            ReservationDTO request = this.jsonMapper.readValue(body, ReservationDTO.class);
            
            if (request == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            if (request.car == null || request.spot == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            if (request.car.id == null || request.spot.id == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Object reservationObject = this.carParkService.createReservation(request.spot.id, request.car.id);
           
            if (reservationObject == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            this.carParkService.evictCache();
            Reservation r = (Reservation) reservationObject;
            r = this.carParkService.getReservation(r.getId());
            
            return Response.status(Response.Status.CREATED).entity(this.jsonMapper.writeValueAsString(new ReservationDTO(r))).build();
            
        } catch (JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(@HeaderParam(HttpHeaders.AUTHORIZATION) String authorizationHeader, @PathParam("id") Long id, String body)
    {
        Object reservationObject = this.carParkService.getReservation(id);
        if (reservationObject == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Reservation reservation = (Reservation) reservationObject;
        
        try {
            ReservationDTO request = this.jsonMapper.readValue(body, ReservationDTO.class);
            if (request == null) {
               return Response.status(Response.Status.BAD_REQUEST).build(); 
            }
            
            if (request.start == null) {
                return Response.status(Response.Status.BAD_REQUEST).build(); 
            }
            
            Date startDate = null;
            
            try {
                startDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(request.start);
            } catch (ParseException e) {
                
            }
            if (startDate == null) {
                try {
                    startDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(request.start);
                } catch (ParseException e) {
                    
                }
            }
            
            if (startDate == null) {
                try {
                    startDate = new SimpleDateFormat("yyyy-MM-dd").parse(request.start);
                } catch (ParseException e) {
                    
                }
            }
            
            if (startDate == null) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Date endDate = null;
            
            if (request.end != null) {
 
                try {
                    endDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(request.end);
                } catch (ParseException e) {

                }
                if (endDate == null) {
                    try {
                        endDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(request.end);
                    } catch (ParseException e) {

                    }
                }

                if (endDate == null) {
                    try {
                        endDate = new SimpleDateFormat("yyyy-MM-dd").parse(request.end);
                    } catch (ParseException e) {

                    }
                }

                if (endDate == null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            } else {
                if (reservation.getEndsAt() != null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            
            if (request.prices == null) {
                if (reservation.getEndsAt() != null) {
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            }
            
            reservation.setPrice(request.prices);
            reservation.setStartsAt(startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            if (endDate != null) {
                reservation.setEndsAt(endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            } else {
                reservation.setEndsAt(null);
            }
            
            Object reservationModifiedObject = this.carParkService.updateReservation(reservation);
            
            return Response.status(Response.Status.OK).entity(new ReservationDTO((Reservation) reservationModifiedObject)).build();
            
            
        } catch(JsonProcessingException e) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
}
