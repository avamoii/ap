package org.example.config;


import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

    public class HibernateUtil {
        private static final SessionFactory sessionFactory = buildSessionFactory();

        private static SessionFactory buildSessionFactory() {
            try {
                return new Configuration().configure().buildSessionFactory();
            } catch (Exception e) {
                throw new RuntimeException("Hibernate session factory ساخت نشد!", e);
            }
        }

        public static SessionFactory getSessionFactory() {
            return sessionFactory;
        }

        public static void shutdown() {
            getSessionFactory().close();
        }
    }


