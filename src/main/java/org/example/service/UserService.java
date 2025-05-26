package org.example.service;


import  org.example.dto.RegisterRequest;
import org.example.factory.UserFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.example.model.User;

public class UserService {

    private final EntityManagerFactory emf = Persistence.createEntityManagerFactory("default");

    public void register(RegisterRequest request) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();

        User user = UserFactory.createUser(request);
        em.persist(user);

        em.getTransaction().commit();
        em.close();
    }

}
