package com.learning.jpa;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

/**
 * https://www.dynatrace.com/news/blog/week-38-transactions-in-a-jpa-world/
 */
public class LocksTest {
    private static final Logger log = LoggerFactory.getLogger(LocksTest.class);

    EntityManagerFactory emf;

    @Before
    public void setUp() throws Exception {
        emf = Persistence.createEntityManagerFactory("test");
    }

    @Test
    public void testLock() {
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        Long entityId = null;
        em1.getTransaction().begin();
        Book book = new Book();
        book.setName("book-01");
        book.setYear(1994);
        em1.persist(book);
        entityId = book.getId();
        log.debug("entity id = {}", entityId);
        em1.getTransaction().commit();


        em1.getTransaction().begin();
        Book book1 = em1.find(Book.class, entityId, LockModeType.PESSIMISTIC_WRITE);
        book1.setName("tx1");

        em2.getTransaction().begin();
        Book book2 = em2.find(Book.class, entityId);
        book2.setName("tx2");

        em2.getTransaction().commit();
        em1.getTransaction().commit(); // javax.persistence.OptimisticLockException

        em1.close();
        em2.close();
    }

    @Test
    public void testPessimisticWrite() throws Exception {
//        Map<String,Object> timeoutProperties = new HashMap<>();
//        timeoutProperties.put("javax.persistence.lock.timeout", 0);

        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        em1.getTransaction().begin();
        Book book = new Book();
        book.setName("book-01");
        book.setYear(1994);
        em1.persist(book);
        Long entityId = book.getId();
        log.debug("entity id = {}", entityId);
        em1.getTransaction().commit();


        Thread t1 = new Thread(() -> {
            em1.getTransaction().begin();
            log.debug("TX1 before lock");
            Book book1 = em1.find(Book.class, entityId, LockModeType.PESSIMISTIC_WRITE);
            log.debug("TX1 after lock");
            book1.setName("tx1");
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("TX1 commit");
            em1.getTransaction().commit();
        });
        t1.start();


        Thread t2 = new Thread(() -> {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            em2.getTransaction().begin();
            log.debug("TX2 before get");
            Book book2 = em2.find(Book.class, entityId, LockModeType.PESSIMISTIC_WRITE);
            log.debug("TX2 after get");
            book2.setName("tx2");
            log.debug("TX2 commit");
            em2.getTransaction().commit();
        });
        t2.start();

        t1.join();
        t2.join();

        em1.clear();
        em1.getTransaction().begin();
        log.info("entityName: {}", em1.find(Book.class, entityId).getName());
        em1.getTransaction().commit();

        em1.close();
        em2.close();
    }
}
