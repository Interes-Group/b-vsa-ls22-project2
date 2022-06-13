/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sk.stuba.fei.uim.vsa.pr2;

import java.util.Base64;
import sk.stuba.fei.uim.vsa.pr1.CarParkService;
import sk.stuba.fei.uim.vsa.pr1.domain.User;

/**
 *
 * @author sheax
 */
public class Helpers {
    
    
    
    public static User getAuthorizedUser(CarParkService carParkSerive, String authRequest)
    {
        if (! authRequest.startsWith("Basic ")) {
            return null;
        }
        String s = authRequest.substring(6);
        byte[] decodedBytes = Base64.getDecoder().decode(s);
        String decodedString = new String(decodedBytes);
        String[] split = decodedString.split(":");
        String userEmail = split[0];
        String userId = split[1];
        Object u = carParkSerive.getUser(userEmail);
        if (u == null) {
            return null;
        }
        User user = (User) u;
        Long userIdLong = Long.parseLong(userId);
        if (user.getId().equals(userIdLong)) {
            return user;
        }
        return null;
    }
}
