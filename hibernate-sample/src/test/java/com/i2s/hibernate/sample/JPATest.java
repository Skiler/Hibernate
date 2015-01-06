package com.i2s.hibernate.sample;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.LockModeType;
import javax.persistence.Persistence;

import junit.framework.TestCase;

import org.junit.Test;

import com.i2s.hibernate.sample.entities.Person;

public class JPATest extends TestCase {

    private EntityManagerFactory entityManagerFactory = null;
    private List<Integer> primaryKeys = new ArrayList<Integer>();

    @Override
    protected void setUp() throws Exception {
        // like discussed with regards to SessionFactory, an
        // EntityManagerFactory is set up once for an application
        // IMPORTANT: notice how the name here matches the name we gave the
        // persistence-unit in persistence.xml!
        entityManagerFactory = Persistence
                .createEntityManagerFactory("org.hibernate.tutorial.jpa");
    }

    @Override
    protected void tearDown() throws Exception {
        entityManagerFactory.close();
    }

    @Test
    public void test() {
        // create a couple of events...
        EntityManager entityManager = entityManagerFactory
                .createEntityManager();
        entityManager.getTransaction().begin();
        Person entity = new Person("Our very first person!");
        entityManager.persist(entity);
        primaryKeys.add(entity.getId());
        Person entity2 = new Person("A follow up person");
        entityManager.persist(entity2);
        primaryKeys.add(entity2.getId());
        entityManager.getTransaction().commit();
        entityManager.close();

        Thread pessimistic1 = new Thread(new ReadingRecordPessimistic(1));
        Thread pessimistic2 = new Thread(new ReadingRecordPessimistic(2));
        pessimistic1.start();
        pessimistic2.start();

        while (pessimistic1.isAlive() || pessimistic2.isAlive()) {
            // System.out.println("Waiting for threads");
        }
        // readAllTable();
    }

    private void readAllTable() {
        EntityManager entityManager;
        // now lets pull events from the database and list them
        entityManager = entityManagerFactory.createEntityManager();
        entityManager.getTransaction().begin();
        List<Person> result = entityManager.createQuery("from Person",
                Person.class).getResultList();
        for (Person event : result) {
            System.out.println("Person (" + event.getName() + ") ");
        }
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    private class ReadingRecordPessimistic implements Runnable {

        private int id = -1;

        public ReadingRecordPessimistic(int id) {
            this.id = id;
        }

        public void run() {
            try {
                System.out.println("Started thread: " + id);
                EntityManager entityManager = entityManagerFactory
                        .createEntityManager();
                entityManager.getTransaction().begin();
                Person person = entityManager.find(Person.class,
                        primaryKeys.get(0), LockModeType.PESSIMISTIC_WRITE);
                // Person person = entityManager.find(Person.class,
                // primaryKeys.get(0));
                System.out.println("Thread " + id + " " + person);

                System.out.println("Sleeping thread: " + id);
                Thread.sleep(4000);
                System.out.println("Waking thread: " + id);

                entityManager.getTransaction().commit();
                entityManager.close();
                System.out.println("Finished thread: " + id);
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

    }

}
