package com.revature.util;

import java.io.InputStream;
import java.util.Properties;


import com.revature.models.Reimbursement;
import com.revature.models.User;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import org.hibernate.service.ServiceRegistry;

public class HibernateUtil {
    private static SessionFactory sessionFactory;

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            try {
                Properties props = new Properties();
                ClassLoader loader = Thread.currentThread().getContextClassLoader();
                InputStream propsInput = loader.getResourceAsStream("application.properties");
                props.load(propsInput);

                Properties settings = new Properties();
                Configuration configuration = new Configuration();
                settings.put(Environment.DRIVER, "org.postgresql.Driver");
                settings.put(Environment.URL, props.getProperty("url"));
                settings.put(Environment.USER, props.getProperty("username"));
                settings.put(Environment.PASS, props.getProperty("password"));
                settings.put(Environment.DIALECT, "org.hibernate.dialect.PostgreSQL95Dialect");
                settings.put(Environment.SHOW_SQL, "true");

                configuration.setProperties(settings);
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Reimbursement.class);

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties()).build();

                sessionFactory = configuration.buildSessionFactory(serviceRegistry);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return sessionFactory;
    }
}
