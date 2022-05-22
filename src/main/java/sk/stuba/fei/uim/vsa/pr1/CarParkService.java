package sk.stuba.fei.uim.vsa.pr1;


import sk.stuba.fei.uim.vsa.pr1.domain.*;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CarParkService extends AbstractCarParkService {

    public static boolean useHoliday = true;

    public CarParkService() {
        super();
    }

    @Override
    public Object createCarPark(String name, String address, Integer pricePerHour) {

        EntityManager em = emf.createEntityManager();
        TypedQuery<CarPark> q = em.createQuery("SELECT c FROM CarPark c WHERE c.name = :name", CarPark.class);
        q.setParameter("name", name);
        List<CarPark> carParks = q.getResultList();
        if (!carParks.isEmpty()) {
            em.close();
            return null;
        }
        em.getTransaction().begin();
        CarPark c = new CarPark();
        c.setName(name);
        c.setAddress(address);
        c.setPricePerHour(pricePerHour);
        em.persist(c);
        em.getTransaction().commit();
        em.close();
        return c;
    }

    @Override
    public Object getCarPark(Long carParkId) {

        EntityManager em = emf.createEntityManager();
        CarPark p = em.find(CarPark.class, carParkId);
        em.close();
        return p;

    }

    @Override
    public Object getCarPark(String carParkName) {

        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> q = em.createQuery("SELECT c FROM CarPark c WHERE c.name=:name", Object.class);
        q.setParameter("name", carParkName);
        Object p = null;
        try {
            p = q.getSingleResult();
        } catch (Exception e) {
        }
        em.close();
        return p;

    }

    @Override
    public List<Object> getCarParks() {

        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> q = em.createQuery("SELECT c FROM CarPark c", Object.class);
        List<Object> carParks = q.getResultList();
        em.close();
        return carParks;

    }

    @Override
    public Object updateCarPark(Object carPark) {

        CarPark c = (CarPark) carPark;
        if (c == null) return null;
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        CarPark entityCarPark = em.find(CarPark.class, c.getId());
        if (entityCarPark == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        try {
            entityCarPark.setAddress(c.getAddress());
            entityCarPark.setName(c.getName());
            entityCarPark.setPricePerHour(c.getPricePerHour());
            em.getTransaction().commit();
            em.close();
            return entityCarPark;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            em.close();
            return null;
        }


    }

    @Override
    public Object deleteCarPark(Long carParkId) {

        EntityManager em = emf.createEntityManager();
        CarPark c = em.find(CarPark.class, carParkId);


        em.getTransaction().begin();

        TypedQuery<Reservation> resQuery = em.createQuery("SELECT r from Reservation r WHERE r.parkingSpot.carParkFloor.carPark = :carPark", Reservation.class);
        resQuery.setParameter("carPark", c);
        List<Reservation> reservations = resQuery.getResultList();
        for (Reservation res : reservations) {
            if (res.getEndsAt() != null) {
                if (CarParkService.useHoliday) {
                    LocalDateTime now = LocalDateTime.now();
                    res.endReservation(this.getHolidayMinutes(res.getStartsAt(), now, em));
                } else {
                    res.endReservation();
                }


            }
            res.setParkingSpot(null);
            em.merge(res);
        }

        if (c != null) {
            for (CarParkFloor floor : c.getCarParkFloorList()) {
                for (ParkingSpot spot : floor.getParkingSpots()) {
                    em.remove(spot);
                }
                em.remove(floor);
            }
            em.remove(c);
        }
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
        em.close();
        return c;

    }

    @Override
    public Object createCarParkFloor(Long carParkId, String floorIdentifier) {

        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        CarPark carPark = em.find(CarPark.class, carParkId);
        if (carPark == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        CarParkFloor c = new CarParkFloor();
        CarParkEmbeddedId emb = new CarParkEmbeddedId();
        emb.setIdentifier(floorIdentifier);
        emb.setCarParkId(carParkId);
        c.setEmbeddedId(emb);

        carPark.addCarParkFloor(c);
        try {
            em.persist(c);
            em.getTransaction().commit();
            em.close();
            return c;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            return null;
        }

    }

    @Override
    public Object getCarParkFloor(Long carParkId, String floorIdentifier) {

        EntityManager em = emf.createEntityManager();
        CarParkEmbeddedId emb = new CarParkEmbeddedId();
        emb.setCarParkId(carParkId);
        emb.setIdentifier(floorIdentifier);
        CarParkFloor f = em.find(CarParkFloor.class, emb);
        em.close();
        return f;

    }

    @Override
    public Object getCarParkFloor(Long carParkFloorId) {
        return null;
    }

    @Override
    public List<Object> getCarParkFloors(Long carParkId) {

        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> q = em.createQuery("SELECT c FROM CarParkFloor c WHERE c.embeddedId.carParkId = :carParkId", Object.class);
        q.setParameter("carParkId", carParkId);
        List<Object> result = q.getResultList();
        em.close();
        return result;

    }

    @Override
    public Object updateCarParkFloor(Object carParkFloor) {
        return null;
    }

    @Override
    public Object deleteCarParkFloor(Long carParkId, String floorIdentifier) {

        EntityManager em = emf.createEntityManager();
        CarParkEmbeddedId emb = new CarParkEmbeddedId();
        emb.setCarParkId(carParkId);
        emb.setIdentifier(floorIdentifier);

        CarParkFloor floor = em.find(CarParkFloor.class, emb);

        if (floor != null) {
            em.getTransaction().begin();

            TypedQuery<Reservation> reservationsQuery = em.createQuery("SELECT r from Reservation r where r.parkingSpot.carParkFloor = :carParkFloor", Reservation.class);
            reservationsQuery.setParameter("carParkFloor", floor);
            List<Reservation> reservations = reservationsQuery.getResultList();
            for (Reservation r : reservations) {
                //r.setEndsAt(now);
                if (r.getEndsAt() == null) {
                    if (CarParkService.useHoliday) {
                        LocalDateTime now = LocalDateTime.now();
                        r.endReservation(this.getHolidayMinutes(r.getStartsAt(), now, em));
                    } else {
                        r.endReservation();
                    }
                }
                r.setParkingSpot(null);
                em.merge(r);
            }

            for (ParkingSpot s : floor.getParkingSpots()) {
                em.remove(s);
            }

            em.remove(floor);

            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
            return floor;
        }
        em.close();
        return null;


    }

    @Override
    public Object deleteCarParkFloor(Long carParkFloorId) {

        return null;
    }

    @Override
    public Object createParkingSpot(Long carParkId, String floorIdentifier, String spotIdentifier) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<ParkingSpot> existsQuery = em.createQuery("SELECT p FROM ParkingSpot p where p.carParkFloor.carPark.id = :carParkId AND p.spotIdentifier = :identifier", ParkingSpot.class);
        existsQuery.setParameter("carParkId", carParkId);
        existsQuery.setParameter("identifier", spotIdentifier);
        List<ParkingSpot> existsList = existsQuery.getResultList();
        if (!existsList.isEmpty()) {
            em.close();
            return null;
        }
        CarParkEmbeddedId emb = new CarParkEmbeddedId();
        emb.setCarParkId(carParkId);
        emb.setIdentifier(floorIdentifier);
        em.getTransaction().begin();
        CarParkFloor f = em.find(CarParkFloor.class, emb);

        if (f != null) {
            ParkingSpot spot = new ParkingSpot();
            spot.setSpotIdentifier(spotIdentifier);
            em.persist(spot);
            f.addParkingSpot(spot);
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
            return spot;
        }
        em.close();
        return null;
    }

    @Override
    public Object getParkingSpot(Long parkingSpotId) {

        EntityManager em = emf.createEntityManager();
        ParkingSpot sp = em.find(ParkingSpot.class, parkingSpotId);
        em.close();
        return sp;
    }

    @Override
    public List<Object> getParkingSpots(Long carParkId, String floorIdentifier) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> parkingSpots = em.createQuery("SELECT s FROM ParkingSpot s WHERE s.carParkFloor.embeddedId.carParkId = :carParkId AND s.carParkFloor.embeddedId.identifier = :identifier", Object.class);
        parkingSpots.setParameter("carParkId", carParkId);
        parkingSpots.setParameter("identifier", floorIdentifier);
        List<Object> retList = parkingSpots.getResultList();
        em.close();
        return retList;
    }

    @Override
    public Map<String, List<Object>> getParkingSpots(Long carParkId) {
        EntityManager em = emf.createEntityManager();
        CarPark c = em.find(CarPark.class, carParkId);
        if (c == null) {
            em.close();
            return null;
        }
        TypedQuery<Object> parkingSpotQuery = em.createQuery("SELECT s FROM ParkingSpot s WHERE s.carParkFloor.carPark = :carPark AND s.carParkFloor.embeddedId.identifier = :identifier", Object.class);
        Map<String, List<Object>> retMap = new HashMap();
        parkingSpotQuery.setParameter("carPark", c);
        for (CarParkFloor f : c.getCarParkFloorList()) {

            parkingSpotQuery.setParameter("identifier", f.getEmbeddedId().getIdentifier());
            List<Object> l = parkingSpotQuery.getResultList();
            retMap.put(f.getEmbeddedId().getIdentifier(), l);
        }
        em.close();
        return retMap;
    }

    @Override
    public Map<String, List<Object>> getAvailableParkingSpots(String carParkName) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<CarPark> q = em.createQuery("SELECT c FROM CarPark c WHERE c.name = :name", CarPark.class);
        q.setParameter("name", carParkName);
        CarPark c = q.getSingleResult();
        if (c == null) {
            em.close();
            return null;
        }
        Map<String, List<Object>> retMap = new HashMap();
        TypedQuery<Long> activeReservationsQuery = em.createQuery("SELECT r.parkingSpot.id FROM Reservation r WHERE r.parkingSpot.carParkFloor.carPark.name = :name AND (r.endsAt IS NULL OR r.endsAt < :now)", Long.class);
        TypedQuery<Object> spotNotInQuery = em.createQuery("SELECT p from ParkingSpot p WHERE p.carParkFloor = :floor AND p.id NOT IN :occupiedIds", Object.class);
        TypedQuery<Object> spotQuery = em.createQuery("SELECT p from ParkingSpot p WHERE p.carParkFloor = :floor", Object.class);
        LocalDateTime now = LocalDateTime.now();
        activeReservationsQuery.setParameter("now", now);
        for (CarParkFloor f : c.getCarParkFloorList()) {
            activeReservationsQuery.setParameter("name", f.getCarPark().getName());
            List<Long> occupiedIds = activeReservationsQuery.getResultList();
            if (occupiedIds.isEmpty()) {
                spotQuery.setParameter("floor", f);
                retMap.put(f.getEmbeddedId().getIdentifier(), spotQuery.getResultList());
            } else {
                spotNotInQuery.setParameter("floor", f);
                spotNotInQuery.setParameter("occupiedIds", occupiedIds);
                retMap.put(f.getEmbeddedId().getIdentifier(), spotNotInQuery.getResultList());
            }

        }
        em.close();
        return retMap;
    }

    @Override
    public Map<String, List<Object>> getOccupiedParkingSpots(String carParkName) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<CarPark> q = em.createQuery("SELECT c FROM CarPark c WHERE c.name = :name", CarPark.class);
        q.setParameter("name", carParkName);
        CarPark c = q.getSingleResult();
        if (c == null) {
            em.close();
            return null;
        }
        Map<String, List<Object>> retMap = new HashMap();
        TypedQuery<Object> spotQuery = em.createQuery("SELECT r.parkingSpot from Reservation r WHERE r.parkingSpot.carParkFloor = :floor AND r.endsAt IS NULL", Object.class);
        for (CarParkFloor f : c.getCarParkFloorList()) {
            spotQuery.setParameter("floor", f);
            retMap.put(f.getEmbeddedId().getIdentifier(), spotQuery.getResultList());
        }
        em.close();
        return retMap;
    }

    @Override
    public Object updateParkingSpot(Object parkingSpot) {
        if (parkingSpot == null) {
            return null;
        }
        ParkingSpot sp = (ParkingSpot) parkingSpot;
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ParkingSpot databaseSpot = em.find(ParkingSpot.class, sp.getId());
        if (databaseSpot == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        try {
            databaseSpot.setSpotIdentifier(sp.getSpotIdentifier());
            em.merge(databaseSpot);
            em.getTransaction().commit();
            em.close();
            return databaseSpot;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            return null;
        }
    }

    @Override
    public Object deleteParkingSpot(Long parkingSpotId) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        ParkingSpot spot = em.find(ParkingSpot.class, parkingSpotId);
        if (spot == null) {
            em.getTransaction().rollback();
            return null;
        }

        TypedQuery<Reservation> resQuery = em.createQuery("SELECT r FROM Reservation r WHERE r.parkingSpot = :parkingSpot", Reservation.class);
        resQuery.setParameter("parkingSpot", spot);
        List<Reservation> list = resQuery.getResultList();
        for (Reservation r : list) {
            if (r.getEndsAt() == null) {
                if (CarParkService.useHoliday) {
                    LocalDateTime now = LocalDateTime.now();
                    r.endReservation(this.getHolidayMinutes(r.getStartsAt(), now, em));
                } else {
                    r.endReservation();
                }
            }
            //r.endReservation();
            r.setParkingSpot(null);
            em.merge(r);
        }

        em.remove(spot);
        em.getTransaction().commit();
        em.close();
        return spot;
    }

    @Override
    public Object createCar(Long userId, String brand, String model, String colour, String vehicleRegistrationPlate) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, userId);
        if (user == null)
            return null;
        Car car = new Car(brand, model, colour, vehicleRegistrationPlate);
        em.getTransaction().begin();
        em.persist(car);
        user.addCar(car);
        em.getTransaction().commit();
        em.close();
        return car;
    }

    @Override
    public Object getCar(Long carId) {
        if (carId == null) return null;
        EntityManager em = emf.createEntityManager();
        Car car = em.find(Car.class, carId);
        em.close();
        return car;
    }

    @Override
    public Object getCar(String vehicleRegistrationPlate) {
        if (vehicleRegistrationPlate == null) return null;
        EntityManager em = emf.createEntityManager();
        Car car = null;
        try {
            TypedQuery<Car> query = em.createQuery("select c from Car c where c.vrp = :vrp", Car.class);
            query.setParameter("vrp", vehicleRegistrationPlate);
            car = query.getSingleResult();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        em.close();
        return car;
    }

    @Override
    public List<Object> getCars(Long userId) {
        if (userId == null) return new ArrayList<>();
        EntityManager em = emf.createEntityManager();
        List<Object> cars = null;
        try {
            User user = em.find(User.class, userId);

            TypedQuery<Object> q = em.createQuery("SELECT c FROM Car c where c.user = :user", Object.class);
            q.setParameter("user", user);

            cars = q.getResultList();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        em.close();
        return cars;
    }

    @Override
    public Object updateCar(Object car) {
        if (car == null) return null;
        Car c = (Car) car;
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Car databaseCar = em.find(Car.class, c.getId());
        if (databaseCar == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        try {
            databaseCar.setBrand(c.getBrand());
            databaseCar.setColour(c.getColour());
            databaseCar.setModel(c.getModel());
            databaseCar.setVrp(c.getVrp());
            em.getTransaction().commit();
            em.close();
            return databaseCar;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            return null;
        }
    }

    @Override
    public Object deleteCar(Long carId) {
        if (carId == null) return null;
        EntityManager em = emf.createEntityManager();
        Car c = em.find(Car.class, carId);
        if (c == null) {
            em.close();
            return null;
        }
        em.getTransaction().begin();

        TypedQuery<Reservation> resQuery = em.createQuery("SELECT r FROM Reservation r WHERE r.car = :car", Reservation.class);
        resQuery.setParameter("car", c);
        List<Reservation> res = resQuery.getResultList();
        for (Reservation r : res) {
            if (r.getEndsAt() == null) {
                if (CarParkService.useHoliday) {
                    LocalDateTime now = LocalDateTime.now();
                    r.endReservation(this.getHolidayMinutes(r.getStartsAt(), now, em));
                } else {
                    r.endReservation();
                }
            }
            //r.endReservation();
            r.setCar(null);
            em.merge(r);
        }

        em.remove(c);
        em.getTransaction().commit();
        em.close();
        return c;
    }

    @Override
    public Object createUser(String firstname, String lastname, String email) {
        EntityManager em = emf.createEntityManager();
        User u = new User();
        u.setEmail(email);
        u.setFirstName(firstname);
        u.setLastName(lastname);
        em.getTransaction().begin();
        try {
            em.persist(u);
            em.getTransaction().commit();
            em.close();
            return u;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            return null;
        }
    }

    @Override
    public Object getUser(Long userId) {
        EntityManager em = emf.createEntityManager();
        User user = em.find(User.class, userId);
        em.close();
        return user;
    }

    @Override
    public Object getUser(String email) {
        if (email == null) return null;
        return getObject("select u from User u where u.email = :mail", User.class, Collections.singletonMap("mail", email));
    }

    @Override
    public List<Object> getUsers() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> query = em.createQuery("select u from User u", Object.class);
        List<Object> users = query.getResultList();
        em.close();
        return users;
    }

    @Override
    public Object updateUser(Object user) {
        if (user == null) return null;
        User u = (User) user;
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        User databaseUser = em.find(User.class, u.getId());
        if (databaseUser == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        try {
            databaseUser.setEmail(u.getEmail());
            databaseUser.setFirstName(u.getFirstName());
            databaseUser.setLastName(u.getLastName());
            em.merge(databaseUser);
            em.getTransaction().commit();
            em.close();
            return databaseUser;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            return null;
        }
    }

    @Override
    public Object deleteUser(Long userId) {
        if (userId == null) return null;
        EntityManager em = emf.createEntityManager();
        User u = em.find(User.class, userId);
        if (u == null) {
            em.close();
            return null;
        }
        em.getTransaction().begin();
        TypedQuery<Reservation> q = em.createQuery("SELECT r FROM Reservation r WHERE r.car.user = :user", Reservation.class);
        q.setParameter("user", u);
        if (u.getCars().isEmpty()) {
            em.remove(u);
            em.getTransaction().commit();
            em.close();
            return u;
        }
        List<Reservation> res = q.getResultList();
        for (Reservation r : res) {
            if (r.getEndsAt() == null) {
                if (CarParkService.useHoliday) {
                    LocalDateTime now = LocalDateTime.now();
                    r.endReservation(this.getHolidayMinutes(r.getStartsAt(), now, em));
                } else {
                    r.endReservation();
                }
            }
            r.setCar(null);
            em.merge(r);
        }
        for (Car c : u.getCars()) {
            em.remove(c);
        }
        em.remove(u);
        em.getTransaction().commit();
        em.close();
        return u;
    }

    @Override
    public Object createReservation(Long parkingSpotId, Long cardId) {
        EntityManager em = emf.createEntityManager();
        ParkingSpot p = em.find(ParkingSpot.class, parkingSpotId);
        Car c = em.find(Car.class, cardId);
        if (c == null || p == null) {
            em.close();
            return null;
        }
        if (p.getType() != null && !Objects.equals(p.getType().getId(), c.getType().getId())) {
            em.close();
            return null;
        }

        TypedQuery<Reservation> activeRes = em.createQuery("SELECT r FROM Reservation r WHERE (r.car = :car OR r.parkingSpot = :spot) AND r.endsAt IS NULL", Reservation.class);
        activeRes.setParameter("car", c);
        activeRes.setParameter("spot", p);
        List<Reservation> res = activeRes.getResultList();

        if (!res.isEmpty()) {
            em.close();
            return null;
        }
        em.getTransaction().begin();
        LocalDateTime now = LocalDateTime.now();
        Reservation r = new Reservation();
        r.setCar(c);
        r.setParkingSpot(p);
        r.setStartsAt(now);
        r.setEndsAt(null);
        em.persist(r);
        em.flush();
        em.getTransaction().commit();
        em.close();
        return r;
    }

    @Override
    public Object endReservation(Long reservationId) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Reservation r = em.find(Reservation.class, reservationId);
        if (r == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        if (r.getEndsAt() != null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        if (CarParkService.useHoliday) {
            LocalDateTime now = LocalDateTime.now();
            r.endReservation(this.getHolidayMinutes(r.getStartsAt(), now, em));
        } else {
            r.endReservation();
        }
        em.merge(r);
        em.getTransaction().commit();
        em.close();
        return r;
    }

    @Override
    public List<Object> getReservations(Long parkingSpotId, Date date) {
        EntityManager em = emf.createEntityManager();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        LocalDateTime dayStart = localDate.atStartOfDay();
        LocalDateTime dayEnd = localDate.atTime(LocalTime.MAX);

        TypedQuery<Object> res = em.createQuery("SELECT r FROM Reservation r WHERE r.startsAt >= :dayStart AND r.startsAt <= :dayEnd AND r.parkingSpot.id = :parkingSpotId", Object.class);
        res.setParameter("dayStart", dayStart);
        res.setParameter("dayEnd", dayEnd);
        res.setParameter("parkingSpotId", parkingSpotId);
        List<Object> retList = res.getResultList();
        em.close();
        return retList;
    }

    @Override
    public List<Object> getMyReservations(Long userId) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> resQuery = em.createQuery("SELECT r FROM Reservation r WHERE r.car.user.id = :userId", Object.class);
        resQuery.setParameter("userId", userId);
        List<Object> r = resQuery.getResultList();
        em.close();
        return r;
    }

    @Override
    public Object updateReservation(Object reservation) {
        if (reservation == null) {
            return null;
        }
        Reservation r = (Reservation) reservation;
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        Reservation databaseReservation = em.find(Reservation.class, r.getId());
        if (databaseReservation == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        try {
            databaseReservation.setEndsAt(r.getEndsAt());
            databaseReservation.setPrice(r.getPrice());
            databaseReservation.setStartsAt(r.getStartsAt());
            em.merge(databaseReservation);
            em.getTransaction().commit();
            em.close();
            return databaseReservation;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            return null;
        }
    }

    @Override
    public Object createDiscountCoupon(String name, Integer discount) {
        if (name == null || discount == null) return null;
        return runTransaction(em -> {
            Coupon coupon = new Coupon(name, discount);
            em.persist(coupon);
            return coupon;
        });
    }

    @Override
    public void giveCouponToUser(Long couponId, Long userId) {
        if (couponId == null || userId == null)
            throw new IllegalArgumentException("Provided arguments are null");
        Coupon coupon = (Coupon) getCoupon(couponId);
        if (coupon == null)
            throw new IllegalArgumentException("Cannot found coupon with id " + couponId);
        User user = (User) getUser(userId);
        if (user == null)
            throw new IllegalArgumentException("Cannot found user with id " + userId);
        runTransaction(em -> {
            UserCoupon uc = new UserCoupon();
            uc.setUser(user);
            uc.setCoupon(coupon);
            em.persist(uc);
            return uc;
        });
    }

    @Override
    public Object getCoupon(Long couponId) {
        if (couponId == null) return null;
        EntityManager em = emf.createEntityManager();
        Coupon coupon = em.find(Coupon.class, couponId);
        em.close();
        return coupon;
    }

    @Override
    public List<Object> getCoupons(Long userId) {
        List<Object> coupons = new ArrayList<>();
        if (userId == null) return coupons;
        User user = (User) getUser(userId);
        if (user == null)
            throw new IllegalArgumentException("Cannot found user with id " + userId);
        List<UserCoupon> uc = getObjects("select uc from UserCoupon uc where uc.user = :user",
                UserCoupon.class, Collections.singletonMap("user", user));
        coupons = uc.stream().map(UserCoupon::getCoupon).collect(Collectors.toList());
        return coupons;
    }

    @Override
    public Object deleteCoupon(Long couponId) {
        if (couponId == null) return null;
        return runTransaction(em -> {
            Coupon coupon = em.find(Coupon.class, couponId);
            if (coupon == null)
                throw new IllegalStateException("Coupon with id " + couponId + " does not exist!");
            TypedQuery<UserCoupon> ucQuery = em.createQuery("select uc from UserCoupon uc where uc.coupon = :coupon", UserCoupon.class);
            ucQuery.setParameter("coupon", coupon);
            List<UserCoupon> ucs = ucQuery.getResultList();
            if (ucs != null && !ucs.isEmpty()) {
                ucs.forEach(em::remove);
            }
            em.remove(coupon);
            return coupon;
        });
    }

    @Override
    public Object endReservation(Long reservationId, Long couponId) {
        if (reservationId == null || couponId == null) return null;
        return runTransaction(em -> {
            Reservation r = em.find(Reservation.class, reservationId);
            if (r == null)
                throw new IllegalStateException("Reservation with id " + reservationId + " does not exist!");
            if (r.getEndsAt() != null)
                throw new IllegalStateException("Reservation with id " + reservationId + " has already ended with end time " + r.getEndsAt().toString());
            Coupon c = em.find(Coupon.class, couponId);
            if (c == null)
                throw new IllegalStateException("Coupon with id " + couponId + " does not exist!");
            r.endReservation(c.getDiscount());
            em.merge(r);
            return r;
        });
    }

    @Override
    public Object createCarType(String name) {
        if (name == null || name.isEmpty())
            return null;
        return runTransaction(em -> {
            CarType type = new CarType(name);
            em.persist(type);
            return type;
        });
    }

    @Override
    public List<Object> getCarTypes() {
        return Arrays.asList(getObjects("select t from CarType t", CarType.class, null).toArray());
    }

    @Override
    public Object getCarType(Long carTypeId) {
        if (carTypeId == null) return null;
        EntityManager em = emf.createEntityManager();
        CarType type = em.find(CarType.class, carTypeId);
        em.close();
        return type;
    }

    @Override
    public Object getCarType(String name) {
        if (name == null || name.isEmpty()) return null;
        return getObject("select t from CarType t where t.name = :typeName", CarType.class, Collections.singletonMap("typeName", name));
    }

    @Override
    public Object deleteCarType(Long carTypeId) {
        if (carTypeId == null) return null;
        return runTransaction(em -> {
            CarType type = em.find(CarType.class, carTypeId);
            if (type == null)
                throw new IllegalStateException("Car type with id " + carTypeId + " does not exist!");
            em.remove(type);
            return type;
        });
    }

    @Override
    public Object createCar(Long userId, String brand, String model, String colour, String vehicleRegistrationPlate, Long carTypeId) {
        if (carTypeId == null) return null;
        CarType type = (CarType) getCarType(carTypeId);
        if (type == null) return null;
        Car car = (Car) createCar(userId, brand, model, colour, vehicleRegistrationPlate);
        if (car == null) return null;
        return runTransaction(em -> {
            Car c = em.find(Car.class, car.getId());
            c.setType(type);
            em.merge(c);
            return c;
        });
    }

    @Override
    public Object createParkingSpot(Long carParkId, String floorIdentifier, String spotIdentifier, Long carTypeId) {
        if (carTypeId == null) return null;
        CarType type = (CarType) getCarType(carTypeId);
        if (type == null) return null;
        ParkingSpot spot = (ParkingSpot) createParkingSpot(carParkId, floorIdentifier, spotIdentifier);
        if (spot == null) return null;
        return runTransaction(em -> {
            ParkingSpot s = em.find(ParkingSpot.class, spot.getId());
            s.setType(type);
            em.merge(s);
            return s;
        });
    }

    @Override
    public Object createHoliday(String name, Date date) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        try {
            Holiday h = new Holiday();
            h.setName(name);
            h.setDay(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withYear(1));
            em.persist(h);
            em.getTransaction().commit();
            em.close();
            return h;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            em.close();
            return null;
        }
    }

    @Override
    public Object getHoliday(Date date) {
        if (date == null) {
            return null;
        }
        LocalDate d = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().withYear(1);
        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> q = em.createQuery("SELECT h FROM Holiday h WHERE h.day = :selectedDate", Object.class);
        q.setParameter("selectedDate", d);
        List<Object> holidays = q.getResultList();
        em.close();
        if (holidays.isEmpty()) {
            return null;
        }
        return holidays.get(0);
    }

    @Override
    public List<Object> getHolidays() {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> q = em.createQuery("SELECT h FROM Holiday h", Object.class);
        List<Object> holidays = q.getResultList();
        em.close();
        return holidays;
    }

    @Override
    public Object deleteHoliday(Long holidayId) {
        if (holidayId == null) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        Holiday h = em.find(Holiday.class, holidayId);
        if (h == null) {
            em.getTransaction().rollback();
            em.close();
            return null;
        }
        em.remove(h);
        em.getTransaction().commit();
        em.close();
        return h;
    }

    private Object runTransaction(Function<EntityManager, Object> operation) {
        EntityManager em = emf.createEntityManager();
        Object obj = null;
        try {
            em.getTransaction().begin();
            obj = operation.apply(em);
            em.getTransaction().commit();
        } catch (Exception ex) {
            ex.printStackTrace();
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
        }
        em.close();
        return obj;
    }

    private <T> List<T> getObjects(String query, Class<T> clazz, Map<String, Object> parameters) {
        EntityManager em = emf.createEntityManager();
        TypedQuery<T> q = em.createQuery(query, clazz);
        if (parameters != null) {
            parameters.forEach(q::setParameter);
        }
        List<T> list = q.getResultList();
        em.close();
        return list;
    }

    private <T> T getObject(String query, Class<T> clazz, Map<String, Object> parameters) {
        List<T> list = getObjects(query, clazz, parameters);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private long getHolidayMinutes(LocalDateTime start, LocalDateTime end, EntityManager em) {
        LocalDate startDate = start.toLocalDate();
        LocalDate endDate = end.toLocalDate();
        if (endDate.isBefore(startDate)) {
            return 0;
        }
        boolean useFullYear = false;
        List<LocalDate> inList = new ArrayList();
        if (start.getYear() == end.getYear()) {
            inList.add(startDate.withYear(1));
            if (start.getDayOfYear() != end.getDayOfYear()) {
                for (LocalDate d = startDate.plusDays(1); d.isBefore(endDate); d = d.plusDays(1)) {
                    inList.add(d.withYear(1));
                }
                inList.add(endDate.withYear(1));
            }
        } else {
            if (end.getYear() - start.getYear() == 1) {
                LocalDate startWithoutYear = startDate.withYear(1);
                LocalDate endWithoutYear = endDate.withYear(1);
                if (endWithoutYear.isAfter(startWithoutYear)) {
                    useFullYear = true;
                } else {
                    LocalDate endYear = endWithoutYear.withMonth(12).withDayOfMonth(31);
                    for (LocalDate d = startWithoutYear; d.isBefore(endYear); d = d.plusDays(1)) {
                        inList.add(d);
                    }
                    inList.add(endYear);
                    LocalDate startYear = startWithoutYear.withDayOfMonth(1).withMonth(1);
                    for (LocalDate d = startYear; d.isBefore(endWithoutYear); d = d.plusDays(1)) {
                        inList.add(d);
                    }
                    inList.add(endWithoutYear);
                }
            }
        }
        List<LocalDate> holidays = null;
        if (useFullYear) {
            TypedQuery<LocalDate> q = em.createQuery("SELECT h.day from Holiday h", LocalDate.class);
            holidays = q.getResultList();
        } else {
            TypedQuery<LocalDate> q = em.createQuery("SELECT h.day from Holiday h WHERE h.day IN :inDates", LocalDate.class);
            q.setParameter("inDates", inList);
            holidays = q.getResultList();
        }

        long holidayMinutes = 0;

        if (startDate.equals(endDate)) {
            if (holidays.contains(startDate.withYear(1))) {
                holidayMinutes = ChronoUnit.MINUTES.between(start, end);
            }
        } else {
            LocalDateTime dayAfter = start.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
            if (holidays.contains(startDate.withYear(1))) {
                holidayMinutes = ChronoUnit.MINUTES.between(start, dayAfter);
            }
            LocalDateTime endDayStart = end.withHour(0).withMinute(0).withSecond(0).withNano(0);
            for (LocalDateTime d = dayAfter; d.isBefore(endDayStart); d = d.plusDays(1)) {
                if (holidays.contains(d.withYear(1).toLocalDate())) {
                    holidayMinutes += 24 * 60;
                }
            }
            if (holidays.contains(end.withYear(1).toLocalDate())) {
                holidayMinutes += ChronoUnit.MINUTES.between(endDayStart, end);
            }
        }

        long holidayHours = holidayMinutes / 60;
        if (holidayMinutes % 60 > 0) {
            holidayHours++;
        }
        return holidayHours;


    }
    
    public List<Reservation> getReservationForCar(Long carId)
    {
         EntityManager em = emf.createEntityManager();
         TypedQuery<Reservation> q = em.createQuery("SELECT r FROM Reservation r WHERE r.car.id = :carId", Reservation.class);
         List<Reservation> retList = q.getResultList();
         em.close();
         return retList;
    }
    
    public Boolean isParkingSpotAvailable(ParkingSpot spot)
    {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Reservation> activeRes = em.createQuery("SELECT r FROM Reservation r WHERE r.parkingSpot = :spot AND r.endsAt IS NULL", Reservation.class);
        activeRes.setParameter("spot", spot);
        List<Reservation> res = activeRes.getResultList();
        em.close();
        if (res != null) {
            if (res.isEmpty()) {
                return true;
            }
            return false;
        }
        return true;
    }
    
    public void evictCache()
    {
        emf.getCache().evictAll();
    }
    
    public void evictCache(Class c) {
        emf.getCache().evict(c);
    }
    
    public Boolean parkingSpotExists(Long carParkId, String parkingSpotIdentifier)
    {
        EntityManager em = emf.createEntityManager();
        TypedQuery<ParkingSpot> q = em.createQuery("SELECT p FROM ParkingSpot p WHERE p.carParkFloor.embeddedId.carParkId = :carParkId AND p.spotIdentifier = :spotIdentifier", ParkingSpot.class);
        q.setParameter("carParkId", carParkId);
        q.setParameter("spotIdentifier", parkingSpotIdentifier);
        List<ParkingSpot> res = q.getResultList();
        em.close();
        if (res == null) {
            return false;
        }
        if (res.isEmpty()) {
            return false;
        }
        return true;
    }
    
    public List<Car> getAllCars()
    {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Car> q = em.createQuery("SELECT c FROM Car c", Car.class);
        List<Car>  cars = q.getResultList();
        em.close();
        return cars;
    }
    
    public List<Object> getAllCoupons()
    {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> q = em.createQuery("SELECT c FROM Coupon c", Object.class);
        List<Object>  coupons = q.getResultList();
        em.close();
        return coupons;
    }
    
    public List<Object> getAllReservations()
    {
        EntityManager em = emf.createEntityManager();
        TypedQuery<Object> q = em.createQuery("SELECT r FROM Reservation r", Object.class);
        List<Object>  reservations = q.getResultList();
        em.close();
        return reservations;
    }
    
    public Reservation getReservation(Long id)
    {
        if (id == null) {
            return null;
        }
        EntityManager em = emf.createEntityManager();
        Reservation r = em.find(Reservation.class, id);
        em.close();
        return r;
    }
}
