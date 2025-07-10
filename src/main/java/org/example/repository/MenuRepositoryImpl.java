package org.example.repository;

import org.example.config.HibernateUtil;
import org.example.model.Menu;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class MenuRepositoryImpl implements MenuRepository {

    private static final Logger logger = LoggerFactory.getLogger(MenuRepositoryImpl.class);

    @Override
    public Menu save(Menu menu) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(menu);
            transaction.commit();
            logger.info("SUCCESS: Menu saved with ID: {}", menu.getId());
            return menu;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in save method for menu {}", menu.getTitle(), e);
            throw new RuntimeException("Could not save menu", e);
        }
    }

    @Override
    public Optional<Menu> findByRestaurantIdAndTitle(Long restaurantId, String title) {
        logger.debug("Finding menu with title '{}' for restaurant ID: {}", title, restaurantId);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Menu> query = session.createQuery(
                    "FROM Menu WHERE restaurant.id = :restaurantId AND title = :title", Menu.class);
            query.setParameter("restaurantId", restaurantId);
            query.setParameter("title", title);

            Optional<Menu> menuOptional = query.uniqueResultOptional();
            // This is crucial to prevent LazyInitializationException in the Action layer
            if (menuOptional.isPresent()) {
                Menu menu = menuOptional.get();
                Hibernate.initialize(menu.getRestaurant());
                Hibernate.initialize(menu.getRestaurant().getOwner());
                Hibernate.initialize(menu.getFoodItems());
            }
            return menuOptional;
        } catch (Exception e) {
            logger.error("CRITICAL ERROR in findByRestaurantIdAndTitle", e);
            return Optional.empty();
        }
    }

    @Override
    public Menu update(Menu menu) {
        logger.debug("Attempting to update menu with ID: {}", menu.getId());
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Menu updatedMenu = session.merge(menu);
            transaction.commit();
            logger.info("SUCCESS: Menu with ID {} updated.", updatedMenu.getId());
            return updatedMenu;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("CRITICAL ERROR in update method for menu ID {}", menu.getId(), e);
            throw new RuntimeException("Could not update menu", e);
        }
    }
}
