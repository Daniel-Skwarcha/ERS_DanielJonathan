package com.revature.services;

import com.revature.models.Role;
import com.revature.models.User;
import com.revature.repositories.UserRepository;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Constitutes the SERVICE LAYER for users. concerned with validating all user
 * input before being sent to the database.
 */
public class UserService {
    private UserRepository userRepo = new UserRepository();
    /**
     * Gets all users from the DataBase
     * @return A list of Users
     */
    public List<User> getAllUsers(){
        List<User> users = userRepo.getAllusers();
        return users;
    }

    /**
     * Authentication method used by the authentication servlet
     * @param username username of the user
     * @param password password of the user
     * @return the object of the requested user
     */
    public User authenticate(String username, String password) throws IOException {
        if (username == null || username.trim().equals("") || password == null || password.trim().equals("")){
            return null;
        }

        Optional<User> authUser =  userRepo.getAUserByUsernameAndPassword(username,password);
        if (authUser.isPresent()) {
            return authUser.get();
        }
        else{
            return null;
        }
    }

    /**
     * Register a new user in the DB. validates all fields first
     * @param newUser completed user object
     */
    // TODO: encrypt all user passwords before persisting to data source
    public String register(User newUser) throws IOException {
        if (!isUserValid(newUser)) {
            return "Invalid user field values provided during registration!";

        }
        Optional<User> existingUser = userRepo.getAUserByUsername(newUser.getUsername());
        if (existingUser.isPresent()) {
            return "Username is already in use";

        }
        Optional<User> existingUserEmail = userRepo.getAUserByEmail(newUser.getEmail());
        if (existingUserEmail.isPresent()) {

            return "Email is already in use";
        }

        if(userRepo.addUser(newUser)) {
            return "New User Added";
        }
        return "No User Added";


    }

    /**
     * Update a user in the DB.
     * @param newUser user to update
     */
    public String update(User newUser) throws IOException {
        if (!isUserValid(newUser)) {
            return "Invalid user field values provided during registration!";
        }
        if (!userRepo.updateAUser(newUser)){
            return "There was a problem trying to update the user";
        }

        return "Success";
    }

    /**
     * Deletes a user by changing their role to 4
     * @param id id of user to delete
     * @return true if role was updated in db
     */
    public boolean deleteUserById(int id) {
        if (id <= 0){
            throw new RuntimeException("THE PROVIDED ID CANNOT BE LESS THAN OR EQUAL TO ZERO");
        }
        return userRepo.deleteAUserById(id);
    }

    /**
     * Method for simple checking of availability of username
     * @param username username to chek
     * @return true if available
     */
    public boolean isUsernameAvailable(String username) {
        User user = userRepo.getAUserByUsername(username).orElse(null);
        return user == null;
    }

    /**
     * Method for simple checking of availability of email
     * @param email
     * @return true if available
     */
    public boolean isEmailAvailable(String email) {
        User user = userRepo.getAUserByEmail(email).orElse(null);
        return user == null;
    }

    /**
     * Validates that the given user and its fields are valid (not null or empty strings). Does
     * not perform validation on id or role fields.
     *
     * @param user
     * @return true or false depending on if the user was valid or not
     */
    public boolean isUserValid(User user) {
        if (user == null) return false;
        if (user.getFirstname() == null || user.getFirstname().trim().equals("")) return false;
        if (user.getLastname() == null || user.getLastname().trim().equals("")) return false;
        if (user.getUsername() == null || user.getUsername().trim().equals("")) return false;
        if (user.getPassword() == null || user.getPassword().trim().equals("")) return false;
        return true;
    }
}
