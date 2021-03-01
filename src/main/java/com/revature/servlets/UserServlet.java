package com.revature.servlets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.revature.dtos.Credentials;
import com.revature.dtos.Principal;
import com.revature.models.User;
import com.revature.services.UserService;
import com.revature.util.UserJWT;
import com.revature.util.UserSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@WebServlet("/user")
public class UserServlet extends HttpServlet {
    private BufferedReader reader;
    private UserService userService = new UserService();

    protected static final Logger logger = LogManager.getLogger(UserServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.getWriter().write("I am in get! and the header is " + req.getHeader("endPointValue"));
        resp.setStatus(200);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        String header = req.getHeader("endPointValue");

        //UserJWT.checkToken(req);
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principal");
        if(principle == null)
        {
            respWriter.write("The principle is null the list size is " + UserSession.getUserSession().getHttpSessionArrayList().size());
        }
        else if (principle != null) {
            respWriter.write("The principle is not null the list size is " + UserSession.getUserSession().getHttpSessionArrayList().size());
        }


        try {

            switch (header) {
                case "login":
                    if (principle != null) {
                        respWriter.write(mapper.writeValueAsString("You are already logged in. Please logout if you would like to log in as another user."));
                        resp.setStatus(400);
                        logger.error("Unauthorized entry at " + LocalDateTime.now().toString());
                        break;
                    }
                    authenticateUser(req, resp);
                    break;

                case "addNewUser":
                    if(principle == null) {
                        respWriter.write(mapper.writeValueAsString("Unauthorized"));
                        resp.setStatus(401);
                        break;
                    }
                    if (!principle.getRole().equals("Admin")) {
                        respWriter.write(mapper.writeValueAsString("Forbidden"));
                        resp.setStatus(403);
                        break;
                    }
                    addNewUser(req, resp);
                    break;
                default:
                    respWriter.write(mapper.writeValueAsString("Invalid endpoint"));
                    resp.setStatus(400);
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            respWriter.write(mapper.writeValueAsString("The header is " + header + " and the exception was " + Arrays.toString(e.getStackTrace())));
            resp.setStatus(500);
        }
    }

    private void authenticateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException  {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            Credentials creds = mapper.readValue(req.getInputStream(), Credentials.class);

            User authUser = userService.authenticate(creds.getUsername(), creds.getPassword(), resp);
            if (authUser == null) {
                respWriter.write("Bad credentials: " + creds.toString());
                resp.setStatus(401);
                return;
            }

            respWriter.write(mapper.writeValueAsString("Successfully logged in"));
            UserSession.getUserSession().createSession(req, authUser);
            resp.setStatus(200);
//            httpSession = req.getSession();
//
//            httpSession.setAttribute("ipAddress", req.getRemoteAddr());
//            httpSession.setAttribute("userid", authUser.getUserId());
//            UserSession.getUserSession().getHttpSessionArrayList().add(httpSession);
            //String token = UserJWT.createJwt(authUser);
            //Cookie tokenCookie = new Cookie("my-token", token);
            //tokenCookie.setHttpOnly(true);
           // resp.addCookie(tokenCookie);

        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            respWriter.write(mapper.writeValueAsString(e.getStackTrace()));
            resp.setStatus(500);
        }

    }

    private void addNewUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            User user = mapper.readValue(req.getInputStream(), User.class);
            String result = userService.register(user);
            respWriter.write(mapper.writeValueAsString(result));
            if (result.contains("New")) {
                resp.setStatus(201);

            }
            else {
                resp.setStatus(400);
            }
        }catch(Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }


    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();

        String header = req.getHeader("endPointValue");

        //UserJWT.checkToken(req);
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principle");



        try {

            switch (header) {
                case "editUser":
                    if (principle == null) {
                        respWriter.write("Unauthorized");
                        resp.setStatus(401);
                        break;
                    }
                    if (!principle.getRole().equals("Admin") ) {
                        respWriter.write("Forbidden");
                        resp.setStatus(403);
                        break;
                    }
                    editUser(req, resp);
                    break;
                default:
                    resp.getWriter().write("Invalid endpoint");
                    resp.setStatus(400);
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }



        reader = req.getReader();
    }

    private void editUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            User user = mapper.readValue(req.getInputStream(), User.class);
            String result = userService.update(user);
            respWriter.write(mapper.writeValueAsString(result));
            if (result.contains("Success")) {
                resp.setStatus(200);

            }
            else {
                resp.setStatus(400);
            }
        }catch(Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String header = req.getHeader("endPointValue");
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principal");

        try {

            switch (header) {
                case "logout":
                    if (principle == null) {
                        resp.getWriter().write("Forbidden");
                        resp.setStatus(403);
                        logger.error("Attempt to log out user not logged in " + LocalDateTime.now().toString());
                        break;
                    }
                    logOut(req, resp);
                    break;
                case "deleteUser":
                    if (principle == null) {
                        resp.getWriter().write("Unauthorized");
                        resp.setStatus(401);
                        logger.error("Unauthorized access" + LocalDateTime.now().toString());
                        break;
                    }
                    if (!principle.getRole().equals("Admin")) {
                        resp.getWriter().write("Forbidden");
                        resp.setStatus(403);
                        logger.error("Unauthorized access" + LocalDateTime.now().toString());
                        break;
                    }

                    deleteUser(req, resp, principle);
                    break;
                default:
                    resp.getWriter().write("Invalid endpoint");
                    resp.setStatus(400);
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }

    }

    private void logOut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            respWriter.write(mapper.writeValueAsString("Logged out"));
            UserSession.getUserSession().logoutUser(req);
            resp.setStatus(200);

        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp, Principal principal) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        try {

            //boolean userDeleted = userService.deleteUserById(principal.getId());
                // USE
               int id = Integer.parseInt(req.getParameter("id"));

               if (id == principal.getId()) {
                   respWriter.write(mapper.writeValueAsString("Unauthorized. User is not allowed to delete their own account."));
                   resp.setStatus(403);
                   return;
               }

            boolean userDeleted = userService.deleteUserById(id);

            if(userDeleted)
            {
                UserSession.getUserSession().deleteUser(id);

                respWriter.write(mapper.writeValueAsString("User successfully deleted"));

                resp.setStatus(200);
            }
            else
            {
                respWriter.write(mapper.writeValueAsString("User does not exist"));
                resp.setStatus(400);
            }

        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }
}
