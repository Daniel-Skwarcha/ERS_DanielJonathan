package com.revature.servlets;

import com.revature.dtos.Credentials;
import com.revature.dtos.RbDTO;
import com.revature.dtos.RbNoDetailDTO;
import com.revature.models.Reimbursement;
import com.revature.models.User;
import com.revature.util.UserSession;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.services.ReimbursementService;
import com.revature.util.UserJWT;
import com.revature.dtos.Principal;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@WebServlet("/reimbursement")
public class ReimbursementServlet extends HttpServlet {
    private BufferedReader reader;
    private ReimbursementService reimbursementService = new ReimbursementService();
    protected static final Logger logger = LogManager.getLogger(ReimbursementServlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        reader = req.getReader();
        String header = req.getHeader("endPointValue");
        //UserJWT.checkToken(req);
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principal");

        if (principle == null) {
            resp.getWriter().write("Unauthorized: header is " + header);
            // 401 is unauthorized. The server doesn't know who they are because they are not logged in
            resp.setStatus(401);
            logger.error("Unauthorized entry at " + LocalDateTime.now().toString());
            return;
        }

        try {

            switch (header) {
                case "getReimbursementDetails":
                    if(!principle.getRole().equals("ADMIN")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;
                    }
                    getReimbursementDetails(req, resp, principle);
                    break;

                case "getReimbursementsByStatus":
                    if(!principle.getRole().equals("Finance Manager")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;
                    }
                    getReimbursementsByStatus(req, resp);
                    break;

                case "getReimbursementsByType":
                    if(!principle.getRole().equals("Finance Manager")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;
                    }
                    getReimbursementsByType(req, resp);
                    break;
                case "getReimbursementsByUserId":
                    if(!principle.getRole().equals("Employee")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;
                    }
                    getReimbursementsByUserId(req,resp, principle);
                    break;
                case "getAllReimbursements":
                    if (!principle.getRole().equals("Finance Manager")) {
                        resp.getWriter().write("You do not have permission to view this page");
                        resp.setStatus(403);
                        break;
                    }

                    getAllReimbursements(req, resp);
                    break;
                default:
                    resp.getWriter().write("Invalid endpoint");
                    resp.setStatus(400);
            }
        } catch (Exception e) {
            ObjectMapper mapper = new ObjectMapper();
            resp.getWriter().write(mapper.writeValueAsString(e.getStackTrace()));
        }
    }

    private void getReimbursementDetails(HttpServletRequest req, HttpServletResponse resp, Principal principal) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");
        //String sessionId = (String) req.getSession().getAttribute("id");
        //resp.getWriter().write("sessionid = " + sessionId);

        try {
            //int id = Integer.parseInt(req.getParameter("id"));
            if (principal.getRole().equals("Employee")) {
                respWriter.write(mapper.writeValueAsString("Unauthorized."));
                resp.setStatus(403);
                return;
            }

            List<RbDTO>  rbDTOList= reimbursementService.getReimbByUserId(principal.getId(), resp);
            if (rbDTOList != null && !rbDTOList.isEmpty()) {
                respWriter.write(mapper.writeValueAsString(rbDTOList));
                resp.setStatus(200);
                return;
            }

            resp.setStatus(400);
            logger.error("BAD REQUEST for getReimbursementDetails " + LocalDateTime.now().toString());

        } catch (Exception e) {
            resp.getWriter().write(mapper.writeValueAsString(e.getMessage() + Arrays.toString(e.getStackTrace())));
            logger.error(Arrays.toString(e.getStackTrace()));
            resp.setStatus(500);
        }
    }
    private void getReimbursementsByStatus(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            int statusId = Integer.parseInt(req.getParameter("id"));


            List<RbNoDetailDTO>  rbDTOList= reimbursementService.getReimbByStatus(statusId);
            if (rbDTOList != null && !rbDTOList.isEmpty()) {
                respWriter.write(mapper.writeValueAsString(rbDTOList));
                resp.setStatus(200);
                return;
            }

            resp.setStatus(400);
            logger.error("BAD REQUEST for getReimbursementDetails " + LocalDateTime.now().toString());

        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }
    private void getReimbursementsByType(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");


        try {
            int typeId = Integer.parseInt(req.getParameter("id"));


            List<RbNoDetailDTO>  rbDTOList= reimbursementService.getReimbByType(typeId);
            if (rbDTOList != null && !rbDTOList.isEmpty()) {
                respWriter.write(mapper.writeValueAsString(rbDTOList));
                resp.setStatus(200);
                return;
            }

            resp.setStatus(400);
            logger.error("BAD REQUEST for getReimbursementDetails " + LocalDateTime.now().toString());

        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }
    private void getReimbursementsByUserId(HttpServletRequest req, HttpServletResponse resp, Principal principal) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            int userId = principal.getId();


            List<RbNoDetailDTO>  rbNoDetailDTOList= reimbursementService.getUserByReimbursementsNoDetails(userId);
            if (rbNoDetailDTOList != null && !rbNoDetailDTOList.isEmpty()) {
                respWriter.write(mapper.writeValueAsString(rbNoDetailDTOList));
                resp.setStatus(200);
                return;
            }

            resp.setStatus(400);
            logger.error("BAD REQUEST for getReimbursementDetails " + LocalDateTime.now().toString());

        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }
    private void getAllReimbursements(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            List<RbNoDetailDTO>  rbNoDetailDTOList= reimbursementService.getAllReimbursementsNoDetails();
            if (rbNoDetailDTOList != null && !rbNoDetailDTOList.isEmpty()) {
                respWriter.write(mapper.writeValueAsString(rbNoDetailDTOList));
                resp.setStatus(200);
                return;
            }

            resp.setStatus(400);
            logger.error("BAD REQUEST for getReimbursementDetails " + LocalDateTime.now().toString());

        } catch (Exception e) {
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

        //UserJWT.checkToken(req);
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principle");

        if (principle == null) {
            resp.getWriter().write("Unauthorized");
            // 401 is unauthorized. The server doesn't know who they are because they are not logged in
            resp.setStatus(401);
            logger.error("Unauthorized entry at " + LocalDateTime.now().toString());
            return;
        }

        try {

            switch (header) {
                case "newReimbursement":
                    if(!principle.getRole().equals("Employee")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;
                    }

                    newReimbursement(req, resp, principle);
                    break;
                default:
                    respWriter.write(mapper.writeValueAsString("Invalid endpoint"));
                    resp.setStatus(400);
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }

    private void newReimbursement(HttpServletRequest req, HttpServletResponse resp, Principal principle) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            Reimbursement reimbursement = mapper.readValue(req.getInputStream(), Reimbursement.class);
            String result = reimbursementService.save(reimbursement);
            respWriter.write(mapper.writeValueAsString(result));
            if (result.contains("Successfully")) {
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
        resp.setContentType("application/json");
        String header = req.getHeader("endPointValue");

        //UserJWT.checkToken(req);
        UserSession.getUserSession().checkForUser(req);
        Principal principle = (Principal) req.getAttribute("principle");

        if (principle == null) {
            resp.getWriter().write("Unauthorized");
            // 401 is unauthorized. The server doesn't know who they are because they are not logged in
            resp.setStatus(401);
            logger.error("Unauthorized entry at " + LocalDateTime.now().toString());
            return;
        }

        try {

            switch (header) {
                case "Deny":
                    if(!principle.getRole().equals("Finance Manager")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;

                    }
                    denyReimbursement(req, resp, principle);
                    break;
                case "Approve":
                    if (!principle.getRole().equals("Finance Manager")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;
                    }
                    approveReimbursement(req, resp, principle);
                    break;

                case "Update":
                    if (!principle.getRole().equals("Employee")) {
                        resp.getWriter().write("Forbidden");
                        // 403 is forbidden, which means the server knows who they are, but the user does not have permission to complete this action
                        resp.setStatus(403);
                        logger.error("Forbidden entry at " + LocalDateTime.now().toString());
                        break;
                    }
                    updateReimbursement(req, resp, principle);
                    break;

                default:
                    respWriter.write(mapper.writeValueAsString("Invalid endpoint"));
                    resp.setStatus(400);
            }
        } catch (Exception e) {
            logger.error(Arrays.toString(e.getStackTrace()) + " " + LocalDateTime.now().toString());
            resp.setStatus(500);
        }
    }
    private void denyReimbursement(HttpServletRequest req, HttpServletResponse resp, Principal principal) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            int reimbId = Integer.parseInt(req.getParameter("id"));
            int resolverId = principal.getId();



            String result = reimbursementService.approve(resolverId, reimbId);
            respWriter.write(mapper.writeValueAsString(result));
            if (result.contains("Successfully")) {
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
    private void approveReimbursement(HttpServletRequest req, HttpServletResponse resp, Principal principal) throws IOException{

        ObjectMapper mapper = new ObjectMapper();

        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {
            int reimbId = Integer.parseInt(req.getParameter("id"));
            int resolverId = principal.getId();



            String result = reimbursementService.deny(resolverId, reimbId);
            respWriter.write(mapper.writeValueAsString(result));
            if (result.contains("Successfully")) {
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

    private void updateReimbursement(HttpServletRequest req, HttpServletResponse resp, Principal principal) throws IOException{
        ObjectMapper mapper = new ObjectMapper();

        PrintWriter respWriter = resp.getWriter();
        resp.setContentType("application/json");

        try {

            Reimbursement reimbursement = mapper.readValue(req.getInputStream(), Reimbursement.class);
            if (reimbursement.getAuthorId() != principal.getId()) {
                respWriter.write(mapper.writeValueAsString("Unauthorized. User cannot edit a reimbursement that they did not create!"));
                resp.setStatus(403);
                return;
            }
            String result = reimbursementService.updateEMP(reimbursement);
            respWriter.write(mapper.writeValueAsString(result));
            if (result.contains("Successfully")) {
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

    }
}
