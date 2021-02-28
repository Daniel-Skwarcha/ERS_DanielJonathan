package com.revature.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javax.persistence.criteria.CriteriaBuilder;

public class CriteriaBuilderFactory {
    private static final Logger logger = LogManager.getLogger(CriteriaBuilderFactory.class);
    private static CriteriaBuilder cb = buildCriteriaFactory();

    private static CriteriaBuilder buildCriteriaFactory()
    {
        try {
            if (cb == null) {
                cb = HibernateSessionFactory.getSessionFactory().getCriteriaBuilder();
            }
        } catch (Exception e) {
            logger.error(e.getStackTrace());
        }

        return cb;
    }

    public static CriteriaBuilder getCriteriaBuilder() {
        return cb;
    }
}
