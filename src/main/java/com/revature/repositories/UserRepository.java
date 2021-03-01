package com.revature.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.models.User;
import com.revature.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class UserRepository {
    protected static final Logger logger = LogManager.getLogger(UserRepository.class);
    public UserRepository() {
        super();
    }

    //---------------------------------- CREATE -------------------------------------------- //

    /**
     * A method tho add a new user to the database, hashes passwords before inserting
     *
     * @param newUser the user to be added
     * @return returns true if one and only one row was inserted
     * @throws SQLException e
     */
    public boolean addUser(User newUser){
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            newUser.setPassword(PasswordEncryption.encryptString(newUser.getPassword()));
            session.save(newUser);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getStackTrace());

        } finally {
            session.close();
        }
        return false;
    }

    //---------------------------------- READ -------------------------------------------- //

    public List<User> getAllusers() {
        List<User> users = null;
        Transaction tx = null;
        String sql = "FROM User";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query query = session.createQuery(sql);
            users = query.getResultList();
            tx.commit();
            return users;
        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }
        return users;
    }

    /**
     * A method to get a single User by email
     *
     * @param email the email address to search the DB for
     * @return returns an Optional user
     * @throws SQLException e
     */
    public Optional<User> getAUserByEmail(String email) {
        Optional user = Optional.empty();
        Transaction tx = null;
        String sql = "FROM User u WHERE u.email = :email";

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query query = session.createQuery(sql);
            query.setParameter("email", email);
            user = query.stream().findFirst();
            tx.commit();
            return user;
        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }
        return user;
    }

    public Optional<User> getAUserByUsername(String userName) {

        Optional user = Optional.empty();
        Transaction tx = null;
        String sql = "FROM User u WHERE u.username = :username";

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query query = session.createQuery(sql);
            query.setParameter("username", userName);
            user = query.stream().findFirst();
            tx.commit();
            return user;
        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }
        return user;
    }

    /**
     * A method to get a single user by a given username and password
     *
     * @param userName the users usernamegit st
     * @param password the users password
     * @return returns an optional user
     * @throws SQLException e
     */
    public Optional<User> getAUserByUsernameAndPassword(String userName, String password){

        Optional user = Optional.empty();
        Transaction tx = null;
        String sqlUsername = "FROM User u WHERE u.username = :username";
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Query query = session.createQuery(sqlUsername);
            query.setParameter("username", userName);
            user = query.stream().findFirst();
            tx.commit();
            if (!user.isPresent()) {
                return user;
            }
            User tempUser = (User) user.get();
            String tablePassword = tempUser.getPassword();
            if (!PasswordEncryption.verifyPassword(password, tablePassword)) {
                Optional user1 = Optional.empty();
                return user1;
            }
            return user;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            return user;
        }
    }

    //---------------------------------- UPDATE -------------------------------------------- //

    public boolean updateAUser(User existingUser){

        Optional user = Optional.empty();
        String sqlID = "FROM User u WHERE u.id = :id";
        Transaction tx = null;
        Session session = HibernateUtil.getSessionFactory().openSession();
        try  {
            tx = session.beginTransaction();
            Query query = session.createQuery(sqlID);
            query.setParameter("id", existingUser.getUserId());
            user = query.stream().findFirst();
            tx.commit();
            session.close();
            if (!user.isPresent()) {
                return false;
            }
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            User tempUser = (User) user.get();
            String tablePassword = tempUser.getPassword();
            String newPassword = "";
            User user1 = (User) session.get(User.class, existingUser.getUserId());
            if (!PasswordEncryption.verifyPassword(existingUser.getPassword(), tablePassword)) {
                newPassword = PasswordEncryption.encryptString(existingUser.getPassword());
                user1.setPassword(newPassword);
            }
            if (!existingUser.getUsername().equals(tempUser.getUsername()))
            {
                user1.setUsername(existingUser.getUsername());
            }
            if (!existingUser.getFirstname().equals(tempUser.getFirstname()))
            {
                user1.setFirstname(existingUser.getFirstname());
            }
            if (!existingUser.getLastname().equals(tempUser.getLastname()))
            {
                user1.setLastname(existingUser.getLastname());
            }
            if (!existingUser.getUserRole().equals(tempUser.getUserRole()))
            {
                user1.setUserRole(existingUser.getUserRole());
            }
            if (!existingUser.getEmail().equals(tempUser.getEmail()))
            {
                user1.setEmail(existingUser.getEmail());
            }

            session.update(user1);
            tx.commit();
            session.close();

            return true;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            session.close();
            return false;
        }
    }

    //---------------------------------- DELETE -------------------------------------------- //

    /**
     * A method to delete a single User from the database
     *
     * @param userId the ID of the record to be deleted
     * @return returns true if one and only one record is updated
     * @throws SQLException
     */
    public boolean deleteAUserById(Integer userId) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            User user = (User) session.get(User.class, userId);
            user.setUserRole(4);
            session.update(user);
            tx.commit();
            return true;

        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            return false;
        }

    }
}
