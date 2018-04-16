package com.learning.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.concurrent.*;

public class PessimisticLockExample {
    static final Logger log = LoggerFactory.getLogger(PessimisticLockExample.class);
    static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("test");
//    static EntityManager em;

    public static void main(String[] args) throws Exception {

//        em = emf.createEntityManager();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            persistArticle();
            executor.execute(() -> {
                updateArticle();
            });
            executor.execute(() -> {
                //simulating other user by using different thread
                readArticle();
            });
            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } finally {
//            em.close();
            emf.close();
        }
    }

    public static void persistArticle() {
        log.info("persisting article");
        EntityManager em = emf.createEntityManager();

        Article article = new Article("test article");
        em.getTransaction().begin();
        em.persist(article);
        em.getTransaction().commit();
        em.close();

        log.info("Article persisted", article);
    }

    private static void readArticle() {
        try {//some delay before reading
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        log.info("[READ]: before acquiring read lock");
        Article article = em.find(Article.class, 1L, LockModeType.PESSIMISTIC_READ);
        log.info("[READ]: After acquiring read lock", article);
        em.getTransaction().commit();
        em.close();

        log.info("[READ]: Article after read commit: {}", article);
    }

    private static void updateArticle() {
        log.info("[UPDATE]: updating Article entity");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        Article article = em.find(Article.class, 1L, LockModeType.PESSIMISTIC_WRITE);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        article.setContent("updated content .. ");
        log.info("[UPDATE]: committing in write thread.");
        em.getTransaction().commit();
        em.close();

        log.info("[UPDATE]: Article updated", article);
    }
}
