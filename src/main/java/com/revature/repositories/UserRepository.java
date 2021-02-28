package com.revature.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.models.User;
import com.revature.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.html.Option;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class UserRepository {
    private String baseQuery = "SELECT * FROM ers_users";
    private String baseInsert = "INSERT INTO project_1.ers_users ";
    private String baseUpdate = "UPDATE project_1.ers_users eu ";
    protected static final Logger logger = LogManager.getLogger(UserRepository.class);
    private SessionFactory sessionFactory = HibernateSessionFactory.getInstance();
    private CriteriaBuilder criteriaBuilder = CriteriaBuilderFactory.getCriteriaBuilder();

    public UserRepository(){
        super();
    }

    //---------------------------------- CREATE -------------------------------------------- //

    /**
     * A method tho add a new user to the database, hashes passwords before inserting
     * @param newUser the user to be added
     * @return returns true if one and only one row was inserted
     * @throws SQLException e
     */
    public boolean addUser(User newUser) {

        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            session.save(newUser);
            session.getTransaction().commit();
            session.close();
            return true;
        } catch(Exception e) {
            logger.error(e.getStackTrace());
        }
        /*try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseInsert +
                         "(username, password, first_name, last_name, email, user_role_id)\n" +
                         "VALUES(?, project_1.crypt(?, project_1.gen_salt('bf', 10)), ?, ?, ?, ?);\n";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1,newUser.getUsername());
            ps.setString(2,newUser.getPassword());
            ps.setString(3,newUser.getFirstname());
            ps.setString(4,newUser.getLastname());
            ps.setString(5,newUser.getEmail());
            ps.setInt(6,newUser.getUserRole());
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        } catch (SQLException e) {
            logger.error(e.getStackTrace());
        }*/
        return false;
    }

    //---------------------------------- READ -------------------------------------------- //

    public List<User> getAllusers() {
        Session session = sessionFactory.openSession();
        List<User> users = null;

        try {
            users = session.createQuery("from ers_users", User.class).getResultList();

        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }
        /*List<User> users = new ArrayList<>();
        try (Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + " order by eu.id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            users = mapResultSet(rs);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        session.close();
        return users;
    }

    /**
     * A method to get a single User by email
     * @param email the email address to search the DB for
     * @return returns an Optional user
     * @throws SQLException e
     */
    public Optional<User> getAUserByEmail(String email) {
        Optional<User> user = Optional.empty();
        Session session = sessionFactory.openSession();

        try {

            CriteriaQuery<User> userCQ = criteriaBuilder.createQuery(User.class);
            Root<User> rootUser = userCQ.from(User.class);
            userCQ.select(rootUser)
                    .where(criteriaBuilder.equal(rootUser.get("email"), email));

            Query<User> userQuery = session.createQuery(userCQ);
            user = Optional.ofNullable(userQuery.getSingleResult());

        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }
        /*Optional<User> user = Optional.empty();
        try (Connection conn = ConnectionFactory.getInstance().getConnection()){
            String sql = baseQuery + "WHERE email =? ";
            PreparedStatement psmt = conn.prepareStatement(sql);
            psmt.setString(1,email);
            ResultSet rs = psmt.executeQuery();
            user = mapResultSet(rs).stream().findFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
        session.close();
        return user;
    }

    public Optional<User> getAUserByUsername(String userName) {
        Optional<User> user = Optional.empty();
        Session session = sessionFactory.openSession();
        try {

            CriteriaQuery<User> userCQ = criteriaBuilder.createQuery(User.class);
            Root<User> rootUser = userCQ.from(User.class);
            userCQ.select(rootUser)
                    .where(criteriaBuilder.equal(rootUser.get("username"), userName));

            Query<User> userQuery = session.createQuery(userCQ);
            user = Optional.ofNullable(userQuery.getSingleResult());

        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }
        /*Optional<User> user = Optional.empty();
        try (Connection conn = ConnectionFactory.getInstance().getConnection()){
            String sql = baseQuery + "WHERE username = ?";
            PreparedStatement psmt = conn.prepareStatement(sql);
            psmt.setString(1,userName);
            ResultSet rs = psmt.executeQuery();
            user = mapResultSet(rs).stream().findFirst();
        } catch (SQLException sqle) {
            logger.error(sqle.getStackTrace());
        }
        System.out.println(user);*/
        session.close();
        return user;
    }

    /**
     * A method to get a single user by a given username and password
     * @param userName the users usernamegit st
     * @param password the users password
     * @return returns an optional user
     * @throws SQLException e
     */
    public Optional<User> getAUserByUsernameAndPassword(String userName, String password, HttpServletResponse resp) throws IOException {

       // Session session = HibernateUtil.getSessionFactory().openSession();
       // session.beginTransaction();

        // Check database version
        //String sql = "select version()";

        //String result = (String) session.createNativeQuery(sql).getSingleResult();
        //System.out.println(result);

        //session.getTransaction().commit();
        //session.close();

       // HibernateUtil.shutdown();





        Optional user = Optional.empty();
        //Session session = null;
        Transaction tx = null;
        String sql = "FROM User u WHERE u.username = :username AND u.password = :password";

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            //session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            //NativeQuery nativeQuery = session.createNativeQuery(sql);
            Query query = session.createQuery(sql);
            query.setParameter("username", userName);
            query.setParameter("password", password);
            user = query.stream().findFirst();
            tx.commit();


            return user;
//            session = sessionFactory.openSession();
//            session.beginTransaction();

            /*CriteriaQuery<User> userCQ = criteriaBuilder.createQuery(User.class);
            Root<User> rootUser = userCQ.from(User.class);
            Predicate equalsUsername = criteriaBuilder.equal(rootUser.get("username"), userName);
            userCQ.select(rootUser)
                    .where(equalsUsername);*/

//            NativeQuery userQuery = session.createNativeQuery(baseQuery);
//            sql = userQuery.getQueryString();
//            List<Object[]> users = userQuery.getResultList();
//            user = (User) users.get(0)[0];
//            session.getTransaction().commit();

            //String currentPassword = user.getPassword();
            //boolean passswordsMatched = PasswordEncryption.verifyPassword(password, currentPassword);

            //if (!passswordsMatched) {
            //    session.close();
            //    return null;
            //}

//            return user;
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            //logger.error(e.getStackTrace());
            ObjectMapper mapper = new ObjectMapper();
            resp.getWriter().write(mapper.writeValueAsString(" The exception is " + e.getMessage() + " The username and passowrd are " + userName + " " + " " + password));
            //resp.getWriter().write(mapper.writeValueAsString("query: " + sql + "\nWhat's wrong: " + e.getStackTrace()));
            //session.close();
            return user;
        }
        /*Optional<User> user = Optional.empty();
        try (Connection conn = ConnectionFactory.getInstance().getConnection()){
            String sql = baseQuery + "WHERE username = ?";
            PreparedStatement psmt = conn.prepareStatement(sql);
            psmt.setString(1,userName);
            ResultSet rs = psmt.executeQuery();
            user = mapResultSet(rs).stream().findFirst();
        } catch (SQLException sqle) {
            logger.error(sqle.getStackTrace());
        }
        System.out.println(user);*/
    }

    //---------------------------------- UPDATE -------------------------------------------- //

    public boolean updateAUser(User newUser) {
        User user = null;
        Session session = sessionFactory.openSession();
        try {


            CriteriaQuery<User> userCQ = criteriaBuilder.createQuery(User.class);
            Root<User> rootUser = userCQ.from(User.class);
            Predicate equalsUserId = criteriaBuilder.equal(rootUser.get("userId"), newUser.getUserId());
            userCQ.select(rootUser)
                    .where(equalsUserId);

            Query<User> userQuery = session.createQuery(userCQ);
            user = userQuery.getSingleResult();

            String currentPassword = user.getPassword();
            boolean passswordsMatched = PasswordEncryption.verifyPassword(newUser.getPassword(), currentPassword);
            session.close();
            session = sessionFactory.openSession();
            CriteriaUpdate<User> userCU = criteriaBuilder.createCriteriaUpdate(User.class);
            rootUser = userCU.from(User.class);
            equalsUserId = criteriaBuilder.equal(rootUser.get("userId"), newUser.getUserId());


            userCU.set("first_name", newUser.getFirstname());
            userCU.set("last_name", newUser.getLastname());
            userCU.set("email", newUser.getEmail());
            userCU.set("user_role_id", newUser.getUserRole());
            userCU.set("username", newUser.getUsername());




            if (!passswordsMatched) {

                String newPassword = PasswordEncryption.encryptString(newUser.getPassword());

                userCU.set("password", newPassword);
            }



            userCU.where(equalsUserId);

           userQuery = session.createQuery(userCU);
            int rowCount = userQuery.executeUpdate();
            session.close();
            return rowCount > 1;
        } catch (Exception e){
            logger.error(e.getStackTrace());
        }
//        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
//            String sql = baseUpdate +
//                    "SET first_name=?, last_name=?, email=?, user_role_id=?, username=?, password= project_1.crypt(?, project_1.gen_salt('bf', 10))\n" +
//                    "WHERE id=?";
//            PreparedStatement ps = conn.prepareStatement(sql);
//            ps.setString(1,newUser.getFirstname());
//            ps.setString(2,newUser.getLastname());
//            ps.setString(3,newUser.getEmail());
//            ps.setInt(4,newUser.getUserRole());
//            ps.setString(5,newUser.getUsername());
//            ps.setString(6, newUser.getPassword());
//            ps.setInt(7,newUser.getUserId());
//            int rowsInserted = ps.executeUpdate();
//            return rowsInserted != 0;
//        } catch (SQLException e) {
//            logger.error(e.getStackTrace());
//        }
        session.close();
        return false;
    }

    //---------------------------------- DELETE -------------------------------------------- //

    /**
     * A method to delete a single User from the database
     * @param userId the ID of the record to be deleted
     * @return returns true if one and only one record is updated
     * @throws SQLException
     */
    public boolean deleteAUserById(Integer userId) {
        Session session = sessionFactory.openSession();
        try {

            CriteriaUpdate<User> userUpdate = criteriaBuilder.createCriteriaUpdate(User.class);
            Root<User> root = userUpdate.from(User.class);
            userUpdate.set("user_role_id", 4);
            userUpdate.where(criteriaBuilder.equal(root.get("id"), userId));

            Query<User> userQuery = session.createQuery(userUpdate);
            int rowCount = userQuery.executeUpdate();
            session.close();
            return rowCount > 1;
        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }

        /*try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseUpdate +
                         "SET user_role_id=4\n" +
                         "WHERE id=? ";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        } catch (SQLException e) {
            logger.error(e.getStackTrace());
        }*/
        session.close();
        return false;
    }
}
