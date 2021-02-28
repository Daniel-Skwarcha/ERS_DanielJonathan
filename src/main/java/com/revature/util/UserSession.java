package com.revature.util;

import com.revature.dtos.Principal;
import com.revature.models.Role;
import com.revature.models.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

public class UserSession {

    private static final UserSession userSession = new UserSession();
    private ArrayList<HttpSession> httpSessionArrayList = new ArrayList<>();
    private UserSession() {

    }
    public static UserSession getUserSession(){
        return userSession;
    }

    public ArrayList<HttpSession> getHttpSessionArrayList() {
        return httpSessionArrayList;
    }

    private String mapEnumRole(Integer enumRole) {
        --enumRole; // we subtract one because we added one to the enum ordinal value before updating/inserting
        switch(enumRole) {
            case 0:
                return "ADMIN";
            case 1:
                return "FINANCE MANAGER";
            case 2:
                return "EMPLOYEE";
            default:
                return "DELETED";
        }
    }

    public void createSession(HttpServletRequest req , User user) {
        HttpSession httpSession = req.getSession();

        httpSession.setAttribute("ipAddress", req.getRemoteAddr());
        httpSession.setAttribute("userId", user.getUserId());
        httpSession.setAttribute("username", user.getUsername());
        httpSession.setAttribute("userRole", mapEnumRole(user.getUserRole()));
        httpSessionArrayList.add(httpSession);
    }

    public void checkForUser(HttpServletRequest req){


        // Get the HTTP cookie named Authorization
        HttpSession httpSession = null;
        Principal principal = null;
        Cookie[] cookies = req.getCookies();
        for (HttpSession httpSession1: httpSessionArrayList) {
            if(httpSession1.getAttribute("ipAddress").equals(req.getRemoteAddr())) {
                httpSession = httpSession1;
                break;
            }
        }

        if (httpSession != null)
        {
            principal = new Principal();
            principal.setId((Integer)httpSession.getAttribute("userId"));
            principal.setUsername((String)httpSession.getAttribute("username"));
            principal.setRole((String)httpSession.getAttribute("userRole"));
            req.setAttribute("principal", principal);
        }

//        try {
//
//
//
//            Principal principal = new Principal();
//            principal.setId(Integer.parseInt(httpSession.getAttribute("userId")));
//            principal.setUsername(claims.getSubject());
//            principal.setRole(claims.get("role", String.class));
//
//            req.setAttribute("principal", principal);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//
//        req.getRemoteAddr()
//
//        Principal principal = new Principal();
//        principal.setId(Integer.parseInt(claims.getId()));
//        principal.setUsername(claims.getSubject());
//        principal.setRole(claims.get("role", String.class));
//
//        req.setAttribute("principal", principal);

    }

    public void logoutUser(HttpServletRequest req) {
        for (HttpSession httpSession1: httpSessionArrayList) {
            if(((String)httpSession1.getAttribute("ipAddress")).equals(req.getRemoteAddr())) {
                httpSessionArrayList.remove(httpSession1);
                break;
            }
        }
        req.getSession().invalidate();

    }

    public void deleteUser(int id) {
        for (HttpSession httpSession1: httpSessionArrayList) {
            if(((Integer)httpSession1.getAttribute("userId")) == id) {
                httpSession1.invalidate();
                httpSessionArrayList.remove(httpSession1);
                break;
            }
        }
    }
}
