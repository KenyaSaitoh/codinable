package org.study.ee.jpa.company.test.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.course.SampleDatabase;

/*
 * JPAtestbaseの機能を提供するクラス
 */
public class JpaTestBase {
    public EntityManagerFactory entityManagerFactory;
    public EntityManager em;
    public EntityTransaction et;

    // テストメソッド呼び出し前処理
    @BeforeEach
    public void beforeTest() throws Exception {
        SampleDatabase.reset();
        entityManagerFactory = Persistence.createEntityManagerFactory("MyPersistenceUnit");
        em = entityManagerFactory.createEntityManager();
        et = em.getTransaction();
        et.begin();
    }

    // テストメソッド呼び出し後処理
    @AfterEach
    public void afterTest() {
        try {
            if (em != null) {
                if (et != null && et.isActive()) {
                    et.rollback();
                }
                em.close();
            }
            if (entityManagerFactory != null) {
                entityManagerFactory.close();
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // コミット
    public void commit() {
        try {
            et.commit();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
