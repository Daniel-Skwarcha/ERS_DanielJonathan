package com.revature.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revature.models.Reimbursement;
import com.revature.models.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

public class HibernateSessionFactory {
    private static SessionFactory sessionFactory = null;
    private static final Logger logger = LogManager.getLogger(HibernateSessionFactory.class);
    private static String errorMessage;

    /*private static SessionFactory buildSessionFactory()
    {
        try
        {
            if (sessionFactory == null)
            {
                Properties props = ConnectionFactory.getInstance().getProperties();
                Configuration config = new Configuration();
                config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL82Dialect");
                config.setProperty("hibernate.connection.url", props.getProperty("url"));
                config.setProperty("hibernate.connection.username", props.getProperty("username"));
                config.setProperty("hibernate.connection.password", props.getProperty("password"));
                config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");

                config.addAnnotatedClass(User.class);
                config.addAnnotatedClass(Reimbursement.class);

                sessionFactory = config.buildSessionFactory();
            }
        } catch (Exception e) {
            e.printStackTrace();
            //assert logger != null;
            //logger.error(e.getStackTrace());
        }

        return sessionFactory;
    }*/

    public static SessionFactory getInstance() {
        try
        {
            if (sessionFactory == null)
            {
                Properties props = ConnectionFactory.getInstance().getProperties();
                Configuration config = new Configuration();
                config.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL82Dialect");
                config.setProperty("hibernate.connection.url", props.getProperty("url"));
                config.setProperty("hibernate.connection.username", props.getProperty("username"));
                config.setProperty("hibernate.connection.password", props.getProperty("password"));
                config.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");

                config.addAnnotatedClass(User.class);
                config.addAnnotatedClass(Reimbursement.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(config.getProperties()).build();

                sessionFactory = config.buildSessionFactory(serviceRegistry);
            }
        } catch (Exception e) {

            //assert logger != null;
            errorMessage = e.getMessage();
            logger.error(e.getStackTrace());
        }

        return sessionFactory;
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static String getErrorMessage() { return errorMessage; }

    public static void shutdown() {
        getSessionFactory().close();
    }
}