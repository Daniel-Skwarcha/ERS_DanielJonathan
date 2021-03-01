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
    private UserService userService = new UserService();
    protected static final Logger logger = LogManager.getLogger(UserServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter respWriter = resp.getWriter();
        String header = req.getHeader("endPointValue");
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principal");
        try {

            switch (header) {
                case "getUsers":
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
                    getUsers(req, resp);
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

    private void getUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        try {

            List<User> users = userService.getAllUsers();

            if (users == null) {
                respWriter.write(mapper.writeValueAsString("Something went wrong. What that is, the server doesn't know."));
                resp.setStatus(500);
            }
            else {
                respWriter.write(String.valueOf(users));
                resp.setStatus(200);
            }

        }catch(Exception e) {
            respWriter.write(mapper.writeValueAsString("Error in processing your request."));
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        String header = req.getHeader("endPointValue");

        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principal");

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
            respWriter.write(mapper.writeValueAsString("The server was unable to process your request."));
            resp.setStatus(500);
        }
    }

    private void authenticateUser(HttpServletRequest req, HttpServletResponse resp) throws IOException  {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        try {
            Credentials creds = mapper.readValue(req.getInputStream(), Credentials.class);

            User authUser = userService.authenticate(creds.getUsername(), creds.getPassword());
            if (authUser == null) {
                respWriter.write("Your username or password was incorrect.");
                resp.setStatus(401);
                return;
            }
            UserSession.getUserSession().createSession(req, authUser);
            resp.setStatus(204);
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            respWriter.write(mapper.writeValueAsString("The server was unable to process the request."));
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
            respWriter.write(mapper.writeValueAsString("The server was unable to process the request."));
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        String header = req.getHeader("endPointValue");
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principal");
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
            respWriter.write(mapper.writeValueAsString("The server was unable to process the request."));
            resp.setStatus(500);
        }
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
            respWriter.write(mapper.writeValueAsString("The server was unable to process the request."));
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }

    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        String header = req.getHeader("endPointValue");
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principal");

        try {

            switch (header) {
                case "logout":
                    if (principle == null) {
                        resp.getWriter().write("Forbidden. Not Logged In.");
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
            respWriter.write(mapper.writeValueAsString("The server was unable to process the request."));
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
            respWriter.write(mapper.writeValueAsString("The server was unable to process the request."));
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }

    private void deleteUser(HttpServletRequest req, HttpServletResponse resp, Principal principal) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        try {
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
                resp.setStatus(204);
            }
            else
            {
                respWriter.write(mapper.writeValueAsString("User does not exist"));
                resp.setStatus(400);
            }

        } catch (Exception e) {
            respWriter.write(mapper.writeValueAsString("The server was unable to process the request."));
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }
}
