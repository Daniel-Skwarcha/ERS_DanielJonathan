package com.revature.services;

import com.revature.dtos.RbDTO;
import com.revature.dtos.RbNoDetailDTO;
import com.revature.models.Reimbursement;
import com.revature.repositories.ReimbursementsRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for validating reimbursements before sending to or from the Database
 */
public class ReimbursementService {
    private final ReimbursementsRepository reimbRepo = new ReimbursementsRepository();

    public ReimbursementService() {
    }

    /**
     * Gets all Reimbursements from the DataBase
     * @return A list of RbDTO objects
     */
    public List<RbDTO> getAllReimb(){
        List<RbDTO> reimbursements = reimbRepo.getAllReimbursements();
        if (reimbursements.isEmpty()){
            throw new RuntimeException();
        }
        return reimbursements;
    }

    /**
     * Gets all reimbursements for a usre given their Id
     * @param userId user id requested
     * @return A list of RbDTO objects
     */
    public List<RbDTO> getReimbByUserId(Integer userId) throws SQLException {
        if (userId <= 0){
            return null;
            //throw new RuntimeException("THE PROVIDED USER ID CANNOT BE LESS THAN OR EQUAL TO ZERO");
        }
        return reimbRepo.getAllReimbSetByAuthorId(userId);

    }

    public List<RbNoDetailDTO> getUserByReimbursementsNoDetails(Integer userId) throws SQLException {
        if(userId <= 0) {
            return null;
        }

        return reimbRepo.getAllReimbursementsNByUserIdoDetails(userId);
    }

    public List<RbNoDetailDTO> getAllReimbursementsNoDetails() throws SQLException {

        return reimbRepo.getAllReimbursementsNByoDetails();
    }


    /**
     * Gets all reimbursements by a specified type
     * @param typeId ordinal number of the type requested, between 1-4
     * @return A list of RbDTO objects
     */
    public List<RbNoDetailDTO> getReimbByType(Integer typeId) throws SQLException {
        if (typeId <= 0 || typeId >=5){
            return null;
            //throw new RuntimeException("THE PROVIDED USER ID CANNOT BE LESS THAN OR EQUAL TO ZERO");
        }
        return reimbRepo.getAllReimbSetByType(typeId);
    }

    /**
     * Gets all reimbursements by a specified status
     * @param statusId ordinal number of the type requested, between 1-3
     * @return A list of RbDTO objects
     */
    public List<RbNoDetailDTO> getReimbByStatus(Integer statusId) throws SQLException {
        if (statusId <= 0 || statusId >= 4){
            return null;
            //throw new RuntimeException("THE PROVIDED USER ID CANNOT BE LESS THAN OR EQUAL TO ZERO");
        }
        return reimbRepo.getAllReimbSetByStatus(statusId);
    }

    /**
     * Saves a reimbursement after validation
     * @param reimb the completed reimbursement object
     */
    public String save(Reimbursement reimb){
        if (!isReimbursementValid(reimb)){
            return "Invalid user field values provided!";
        }
        if(!reimbRepo.addReimbursement(reimb)){
            return "Something went wrong trying to save this reimbursement";
        }
        return "Successfully added a new reimbursement";
    }

    /**
     * Update a reimbursement
     * @param reimb the completed reimbursement object
     */
    public String updateEMP(Reimbursement reimb) {
        if (!isReimbursementValid(reimb)){
            return "Invalid user field values provided!";

        }
        if(!reimbRepo.updateEMP(reimb)){
            return "Something went wrong trying to save this reimbursement";
        }
        System.out.println(reimb);
        return "Successfully updated the reimbursement";
    }

    /**
     * Approve a Reimb.
     * @param resolverId the Id of the fin manager resolving the reimb.
     * @param reimbId id of the Reimb. to approve or disapprove.
     */
    public String approve(Integer resolverId, Integer reimbId) {
        if (reimbId <= 0 || resolverId <=0){
            return "Invalid user field values provided!";
        }
        if(!reimbRepo.updateFIN(resolverId, 2, reimbId)){
            return "Something went wrong trying to approve this reimbursement";
        }
        return "Successfully approved the reimbursement";
    }

    /**
     * Deny a reimb.
     * @param resolverId the Id of the fin manager resolving the reimb.
     * @param reimbId id of the Reimb. to approve or disapprove.
     */
    public String deny(Integer resolverId, Integer reimbId) {
        if (reimbId <= 0){
            return "Invalid user field values provided!";
        }
        if(!reimbRepo.updateFIN(resolverId, 3, reimbId)){
            return "Something went wrong trying to deny this reimbursement";
        }
        return "Successfully denied the reimbursement";
    }

    /**
     * Validates feilds of a reimbursement
     * @param reimb reimb. to be validated
     * @return true or false based on fields
     */
    public boolean isReimbursementValid(Reimbursement reimb){
        if (reimb == null) return false;
        if (reimb.getAmount() == null || reimb.getAmount() <= 0 ) return false;
        if (reimb.getDescription() == null || reimb.getDescription().trim().equals("")) return false;
        if (reimb.getAuthor().getUserId() <= 0 ) return false;
        if (reimb.getReimbursementType() == null ) return false;
        return true;
    }


}
