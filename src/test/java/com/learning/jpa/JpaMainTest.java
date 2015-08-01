package com.learning.jpa;

import org.junit.Assert;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMainTest {

    @Test
    public void testJpaBootstrap() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
        EntityManager em = emf.createEntityManager();
        Assert.assertNotNull(em);
    }

    @Test
    public void testSaveEntity() throws Exception {
        Book book = new Book();
        book.setName("book-01");
        book.setYear(1994);

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        em.persist(book);

        tx.commit();
        em.close();
        emf.close();
    }
}
