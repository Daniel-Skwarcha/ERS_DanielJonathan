package com.revature.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.dtos.RbDTO;
import com.revature.dtos.RbNoDetailDTO;
import com.revature.models.Reimbursement;
import com.revature.models.ReimbursementStatus;
import com.revature.models.ReimbursementType;
import com.revature.models.User;
import com.revature.util.ConnectionFactory;
import com.revature.util.CriteriaBuilderFactory;
import com.revature.util.HibernateSessionFactory;
import com.revature.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;

/**
 * A class to interact with the database to CRUD reimbursement objects
 */
public class ReimbursementsRepository {
    //protected static final Logger logger = LogManager.getLogger(ReimbursementsRepository.class);
    //base query that combines the name and resolver names from one query
    private SessionFactory sessionFactory = HibernateSessionFactory.getInstance();
    private CriteriaBuilder criteriaBuilder = CriteriaBuilderFactory.getCriteriaBuilder();
    private String baseQuery = "SELECT er.id, er.amount, er.description, er.reimbursement_status_id, \n" +
            "er.reimbursement_type_id, er.resolved, er.submitted,  er.author_id , er.resolver_id,\n" +
            "author.first_name as author_first_name , author.last_name as author_last_name , \n" +
            "resolver.first_name as resolver_first_name, resolver.last_name as resolver_last_name\n" +
            "FROM project_1.ers_reimbursements er\n" +
            "left join project_1.ers_users author \n" +
            "on er.author_id = author.id\n" +
            "left join project_1.ers_users resolver \n" +
            "on er.resolver_id = resolver.id ";
    private String baseSelectNoDetail = "SELECT er.id, er.author_id, er.reimbursement_type_id, er.reimbursement_status_id FROM ers_reimbursements er ";
    private String baseInsert = "INSERT INTO project_1.ers_reimbursements ";
    private String baseUpdate = "UPDATE project_1.ers_reimbursements er ";

    public ReimbursementsRepository() {
        super();
    }

    //---------------------------------- CREATE -------------------------------------------- //
    /**
     * Adds a reimburement to the database, Does not handle Images!
     * @param reimbursement the reimbursement to be added to the DB
     * @throws SQLException e
     * @throws IOException e
     */
    // TODO add support to persist receipt images to data source
    public boolean addReimbursement(Reimbursement reimbursement) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        try {
            session.save(reimbursement);
            session.getTransaction().commit();
            session.close();
        } catch (Exception e) {
            session.getTransaction().rollback();
            session.close();
            return false;
        }

        return true;
    }

    //---------------------------------- READ -------------------------------------------- //

    public List<RbDTO> getAllReimbursements() {
        List<RbDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + " order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            //reimbursements = mapResultSetDTO(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reimbursements;
    }

    public List<RbNoDetailDTO> getAllReimbSetByStatus(Integer statusId) throws SQLException {
        Session session = sessionFactory.openSession();
        CriteriaQuery<Reimbursement> reimbursementCQ = criteriaBuilder.createQuery(Reimbursement.class);
        session.beginTransaction();

        Root<Reimbursement> rootReimbursement = reimbursementCQ.from(Reimbursement.class);
        Join<Reimbursement, Reimbursement> authorJoin = rootReimbursement.join("author_id", JoinType.LEFT);
        Join<Reimbursement, Reimbursement> resolverJoin = authorJoin.join("resolver_id", JoinType.LEFT);

        reimbursementCQ.select(rootReimbursement)
                .where(criteriaBuilder.equal(rootReimbursement.get("reimbursement_status_id"), statusId));

        Query<Reimbursement> query = session.createQuery(reimbursementCQ);
        List<Reimbursement> list = query.list();
        List<RbNoDetailDTO> reimbursements = mapResultSetNoDetailDTO(list);
        session.getTransaction().commit();
        session.close();

        return reimbursements;

        /*
        List<RbNoDetailDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.reimbursement_status_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,statusId);
            ResultSet rs = ps.executeQuery();
            reimbursements = mapResultSetNoDetailDTO(rs);
        } catch (SQLException e) {
            logger.error(e.getStackTrace());
        }
        return reimbursements;
        */
    }

    /**
     * A method to get Reimbursements by the id of the reimbursement itself
     * @param reimbId The ID of the reimbursement in the database that is requested
     * @return returns an Option Reimbursement object
     * @throws SQLException e
     */
    public Optional<Reimbursement> getAReimbByReimbId(Integer reimbId) throws SQLException {
        Optional<Reimbursement> reimbursement = Optional.empty();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,reimbId);

            ResultSet rs = ps.executeQuery();

            reimbursement = mapResultSet(rs).stream().findFirst();
        }
        return reimbursement;
    }

    /**
     * A method to get all of the records for an author given their id
     * @param authorId the ID of the author of the reimbursement
     * @return a set of reimbursements mapped by the MapResultSet method
     * @throws SQLException e
     */
    public List<RbDTO> getAllReimbSetByAuthorId(Integer authorId, HttpServletResponse resp) throws Exception, IOException {
        List<Reimbursement> reimbursements = null;
        List<RbDTO> reimbs = null;

        Session session = sessionFactory.openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            Query<Reimbursement> query = session.createQuery("FROM Reimbursement WHERE author_id = :id")
                    .setParameter("id", authorId);
            reimbursements = query.getResultList();
            transaction.commit();
            reimbs = mapResultSetDTO(reimbursements);
        } catch(Exception e) {
            ObjectMapper mapper = new ObjectMapper();
            resp.getWriter().write(mapper.writeValueAsString("error in author id fetch " + e.getMessage()));
            if(transaction != null){
                transaction.rollback();
            }
        }
        session.close();
        return reimbs;
    }

    /**
     * A method to get all of the records for an author given their id and filter by status
     * @param authorId the ID of the author of the reimbursement
     * @param reStat the status that the reimbursement is to be set to
     * @return a set of reimbursements mapped by the MapResultSet method
     * @throws SQLException e
     */
    public List<RbDTO> getAllReimbSetByAuthorIdAndStatus(Integer authorId, ReimbursementStatus reStat) throws SQLException {
        List<RbDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.author_id=? AND er.reimbursement_status_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,authorId);
            ps.setInt(2,reStat.ordinal() + 1);
            ResultSet rs = ps.executeQuery();
            //reimbursements = mapResultSetDTO(rs);
        }
        return reimbursements;
    }

    /**
     * A method to get all of the records for an author given their id and filter by type
     * @param authorId ID of the Author User
     * @param reType the Type to update the record to
     * @return a set of reimbursements mapped by the MapResultSet method
     * @throws SQLException e
     */
    public List<RbDTO> getAllReimbSetByAuthorIdAndType(Integer authorId, ReimbursementType reType) throws SQLException {
        List<RbDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.author_id=? AND er.reimbursement_type_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,authorId);
            ps.setInt(2,reType.ordinal() + 1);
            ResultSet rs = ps.executeQuery();
            //reimbursements = mapResultSetDTO(rs);
        }
        return reimbursements;
    }

    public List<RbNoDetailDTO> getAllReimbursementsNByUserIdoDetails(Integer userId) throws SQLException {
        Session session = sessionFactory.openSession();
        CriteriaQuery<Reimbursement> reimbursementCQ = criteriaBuilder.createQuery(Reimbursement.class);
        session.beginTransaction();

        Root<Reimbursement> rootReimbursement = reimbursementCQ.from(Reimbursement.class);
        Join<Reimbursement, Reimbursement> authorJoin = rootReimbursement.join("author_id", JoinType.LEFT);
        Join<Reimbursement, Reimbursement> resolverJoin = authorJoin.join("resolver_id", JoinType.LEFT);

        reimbursementCQ.select(rootReimbursement)
                .where(criteriaBuilder.equal(rootReimbursement.get("id"), userId));

        Query<Reimbursement> query = session.createQuery(reimbursementCQ);
        List<Reimbursement> list = query.list();
        List<RbNoDetailDTO> reimbursements = mapResultSetNoDetailDTO(list);
        session.getTransaction().commit();
        session.close();

        return reimbursements;
        /*List<RbNoDetailDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseSelectNoDetail + "WHERE er.author_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,userId);
            ResultSet rs = ps.executeQuery();
        } catch (SQLException e) {
            //logger.error(e.getStackTrace());
        }
        return reimbursements;*/
    }

    public List<RbNoDetailDTO> getAllReimbursementsNByoDetails() throws SQLException {
        Session session = sessionFactory.openSession();
        session.beginTransaction();

        Query<Reimbursement> query = session.createNativeQuery(baseQuery);
        List<Reimbursement> list = query.list();
        List<RbNoDetailDTO> reimbursements = mapResultSetNoDetailDTO(list);
        session.getTransaction().commit();
        session.close();

        return reimbursements;
        /*List<RbNoDetailDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseSelectNoDetail + "order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            reimbursements = mapResultSetNoDetailDTO(rs);
        } catch (SQLException e) {
            //logger.error(e.getStackTrace());
        }
        return reimbursements;*/

    }



    public List<RbNoDetailDTO> getAllReimbSetByType(Integer typeId) throws SQLException {
        Session session = sessionFactory.openSession();
        CriteriaQuery<Reimbursement> reimbursementCQ = criteriaBuilder.createQuery(Reimbursement.class);
        session.beginTransaction();

        Root<Reimbursement> rootReimbursement = reimbursementCQ.from(Reimbursement.class);
        Join<Reimbursement, Reimbursement> authorJoin = rootReimbursement.join("author_id", JoinType.LEFT);
        Join<Reimbursement, Reimbursement> resolverJoin = authorJoin.join("resolver_id", JoinType.LEFT);

        reimbursementCQ.select(rootReimbursement)
                .where(criteriaBuilder.equal(rootReimbursement.get("reimbursement_type_id"), typeId));

        Query<Reimbursement> query = session.createQuery(reimbursementCQ);
        List<Reimbursement> list = query.list();
        List<RbNoDetailDTO> reimbursements = mapResultSetNoDetailDTO(list);
        session.getTransaction().commit();
        session.close();

        return reimbursements;
        /*List<RbNoDetailDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.reimbursement_type_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,typeId);
            ResultSet rs = ps.executeQuery();
            reimbursements = mapResultSetNoDetailDTO(rs);
        } catch (SQLException e) {
            logger.error(e.getStackTrace());
        }
        return reimbursements;*/
    }

    /**
     * A method to get all of the records for a resolver given their id
     * @param resolverId ID of the Resolver User
     * @return a set of reimbursements mapped by the MapResultSet method
     * @throws SQLException e
     */
    public List<RbDTO> getAllReimbSetByResolverId(Integer resolverId) throws SQLException {
        List<RbDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.resolver_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,resolverId);

            ResultSet rs = ps.executeQuery();

            //reimbursements = mapResultSetDTO(rs);
        }
        return reimbursements;
    }

    /**
     * A method to get all of the records for a resolver given their id and filter by status
     * @param resolverId  ID of the Resolver User
     * @param reStat the status to update the record to
     * @return a set of reimbursements mapped by the MapResultSet method
     * @throws SQLException e
     */
    public List<RbDTO> getAllReimbSetByResolverIdAndStatus(Integer resolverId, ReimbursementStatus reStat) throws SQLException {
        List<RbDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.resolver_id=? AND er.reimbursement_status_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,resolverId);
            ps.setInt(2,reStat.ordinal() + 1);
            ResultSet rs = ps.executeQuery();
            //reimbursements = mapResultSetDTO(rs);
        }
        return reimbursements;
    }

    /**
     * A  method to get all of the records for a resolver given their id and filter by type
     * @param resolverId ID of the Resolver User
     * @param reType type of Reimbursements to select by
     * @return a set of reimbursements mapped by the MapResultSet method
     * @throws SQLException e
     */
    public List<RbDTO> getAllReimbSetByResolverIdAndType(Integer resolverId, ReimbursementType reType) throws SQLException {
        List<RbDTO> reimbursements = new ArrayList<>();
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseQuery + "WHERE er.resolver_id=? AND er.reimbursement_type_id=? order by er.id";
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,resolverId);
            ps.setInt(2,reType.ordinal() + 1);
            ResultSet rs = ps.executeQuery();
            //reimbursements = mapResultSetDTO(rs);
        }
        return reimbursements;
    }

    //---------------------------------- UPDATE -------------------------------------------- //
    public boolean updateEMP(Reimbursement reimb) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        CriteriaUpdate<Reimbursement> criteriaUpdateCQ = criteriaBuilder.createCriteriaUpdate(Reimbursement.class);
        Root<Reimbursement> rootReimb = criteriaUpdateCQ.from(Reimbursement.class);

        criteriaUpdateCQ.set("amount", reimb.getAmount());
        criteriaUpdateCQ.set("description", reimb.getDescription());
        criteriaUpdateCQ.set("reimbursement_type_id", reimb.getReimbursementType().ordinal() + 1);
        criteriaUpdateCQ.where(criteriaBuilder.equal(rootReimb.get("id"), reimb.getId()));

        Query<Reimbursement> reimbQuery = session.createQuery(criteriaUpdateCQ);
        int rowsUpdated = reimbQuery.executeUpdate();

        session.getTransaction().commit();
        session.close();

        return rowsUpdated > 0;
        /*try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseUpdate +
                    "SET amount=?, description=?, reimbursement_type_id=?\n" +
                    "WHERE id=?\n";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, reimb.getAmount());
            ps.setString(2, reimb.getDescription());
            ps.setInt(3,reimb.getReimbursementType().ordinal() + 1);
            ps.setInt(4,reimb.getId());
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        } catch (SQLException e) {
            //logger.error(e.getStackTrace());
        }
        return false;*/
    }

    public boolean updateFIN(Integer resolverId, Integer statusId, Integer reimbId) {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        CriteriaUpdate<Reimbursement> criteriaUpdateCQ = criteriaBuilder.createCriteriaUpdate(Reimbursement.class);
        Root<Reimbursement> rootReimb = criteriaUpdateCQ.from(Reimbursement.class);

        criteriaUpdateCQ.set("resolver_id", resolverId);
        criteriaUpdateCQ.set("reimbursement_status_id", statusId);
        criteriaUpdateCQ.set("resolved", Instant.now().toEpochMilli());
        criteriaUpdateCQ.where(criteriaBuilder.equal(rootReimb.get("id"), reimbId));

        Query<Reimbursement> reimbQuery = session.createQuery(criteriaUpdateCQ);
        int rowsUpdated = reimbQuery.executeUpdate();

        session.getTransaction().commit();
        session.close();

        return rowsUpdated > 0;
        /*try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseUpdate +
                    "SET resolver_id=?, reimbursement_status_id=?, resolved=CURRENT_TIMESTAMP\n" +
                    "WHERE id=?\n";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, resolverId);
            ps.setInt(2, statusId);
            ps.setInt(3,reimbId);

            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        } catch (SQLException e) {
            //logger.error(e.getStackTrace());
        }
        return false;*/
    }

    /**
     * A method to update only the resolved timestamp by the id of the reimbursement
     * @param reimbId The ID of the reimbursement in the database that is requested
     * @param timestamp an SQL timestamp object to set the time resolved to
     * @return returns true if one and only one record was updated
     * @throws SQLException e
     */
    public boolean updateResolvedTimeStampByReimbId(Integer reimbId, Timestamp timestamp) throws SQLException {

        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseUpdate +
                         "SET resolved=?\n" +
                         "WHERE id=?\n";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setTimestamp(1,timestamp);
            ps.setInt(2,reimbId);
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        }
    }

    /**
     * A method to update only the resolver ID by the id of the reimbursement
     * @param reimbId The ID of the reimbursement in the database that is requested
     * @param resolverId the ID of the user that resolves the record to update the record to
     * @return returns true if one and only one record was updated
     * @throws SQLException e
     */
    public boolean updateResolverIdByReimbId(Integer reimbId, Integer resolverId) throws SQLException {

        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseUpdate +
                    "SET resolver_id=?\n" +
                    "WHERE id=?\n";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,resolverId);
            ps.setInt(2,reimbId);
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        }
    }

    /**
     * A method to update only the Reimb. TYPE by the id of the Reimbursement
     * @param reimbId The ID of the reimbursement in the database that is requested
     * @param reimbursementType the type to update the record to
     * @return returns true if one and only one record was updated
     * @throws SQLException e
     */
    public boolean updateReimbursementTypeByReimbId(Integer reimbId, ReimbursementType reimbursementType) throws SQLException {
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseUpdate +
                    "SET reimbursement_type_id=? " +
                    "WHERE er.id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,reimbursementType.ordinal() + 1);
            ps.setInt(2,reimbId);
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        }
    }

    /**
     * A method to update the status of a reimbursement in the database
     * @param reimbId The ID of the reimbursement in the database that is requested
     * @param newReimbStatus the status to update the record to
     * @return returns true if one and only one record was updated
     * @throws SQLException e
     */
    public boolean updateReimbursementStatusByReimbId(Integer reimbId, ReimbursementStatus newReimbStatus) throws SQLException {
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = baseUpdate +
                         "SET reimbursement_status_id=? " +
                         "WHERE er.id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,newReimbStatus.ordinal() + 1);
            ps.setInt(2,reimbId);
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        }
    }


    //---------------------------------- DELETE -------------------------------------------- //

    /**
     * A method to delete a single Reimbursement from the database
     * @param reimbId the ID of the record to be deleted
     * @return returns true if one and only one record is updated
     * @throws SQLException e
     */
    public boolean delete(Integer reimbId) throws SQLException {
        try(Connection conn = ConnectionFactory.getInstance().getConnection()) {
            String sql = "DELETE FROM project_1.ers_reimbursements\n" +
                         "WHERE id=? ";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1,reimbId);
            //get the number of affected rows
            int rowsInserted = ps.executeUpdate();
            return rowsInserted != 0;
        }
    }

    //---------------------------------- UTIL -------------------------------------------- //
    /**
     * A method to map the result sets from the reimbursement queries
     * @param rs a resultset
     * @return a set of reimbursements
     * @throws SQLException e
     */
    private Set<Reimbursement> mapResultSet(ResultSet rs) throws SQLException {
        Set<Reimbursement> reimbursements = new HashSet<>();
        while (rs.next()){
            Reimbursement temp = new Reimbursement();
            temp.setId(rs.getInt("id"));
            temp.setAmount(rs.getDouble("amount"));
            temp.setSubmitted(rs.getTimestamp("submitted"));
            temp.setResolved(rs.getTimestamp("resolved"));
            temp.setDescription(rs.getString("description"));
            //temp.setAuthorId(rs.getInt("author_id"));
            //temp.setResolverId(rs.getInt("resolver_id"));
            temp.setReimbursementStatus(ReimbursementStatus.getByNumber(rs.getInt("reimbursement_status_id")));
            temp.setReimbursementType(ReimbursementType.getByNumber(rs.getInt("reimbursement_type_id")));

            reimbursements.add(temp);
        }
        return reimbursements;
    }

    private List<RbDTO> mapResultSetDTO(List<Reimbursement> reimbursements) throws SQLException {
        List<RbDTO> reimbsList = new ArrayList<>();

        reimbursements.stream().forEach(reimbursement -> {
            RbDTO temp = new RbDTO();
            temp.setId(reimbursement.getId());
            temp.setAmount(reimbursement.getAmount());
            temp.setSubmitted(reimbursement.getSubmitted().toString().substring(0,19));
            temp.setDescription(reimbursement.getDescription());
            //temp.setAuthorName(reimbursement.getAuthor().getFirstname() + " " + reimbursement.getAuthor().getLastname());
            temp.setStatus(ReimbursementStatus.getByNumber(reimbursement.getReimbursementStatus().ordinal() + 1).toString());
            temp.setType(ReimbursementType.getByNumber(reimbursement.getReimbursementType().ordinal() + 1).toString());

            try {
                temp.setResolved(reimbursement.getResolved().toString().substring(0,19));
                //temp.setResolverName(reimbursement.getResolver().getFirstname() + " " +
                //        reimbursement.getResolver().getLastname());
            } catch (NullPointerException e){
                //If Reimb. has not been resolved DB will return null for these values:
                temp.setResolved("");
                temp.setResolverName("");
            }

            reimbsList.add(temp);
        });

        return reimbsList;
    }

    private List<RbNoDetailDTO> mapResultSetNoDetailDTO(List<Reimbursement> reimbursements) throws SQLException {
        List<RbNoDetailDTO> reimbsList = new ArrayList<>();
        reimbursements.stream().forEach(reimbursement -> {
            RbNoDetailDTO temp = new RbNoDetailDTO();
            temp.setId(reimbursement.getId());
            temp.setAuthorId(reimbursement.getAuthorId());
            temp.setStatus(ReimbursementStatus.getByNumber(reimbursement.getReimbursementStatus().ordinal() + 1).toString());
            temp.setType(ReimbursementType.getByNumber(reimbursement.getReimbursementType().ordinal() + 1).toString());

            reimbsList.add(temp);
        });

        return reimbsList;
    }
}
