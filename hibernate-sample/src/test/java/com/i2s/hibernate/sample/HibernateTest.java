package com.i2s.hibernate.sample;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.i2s.hibernate.sample.entities.Person;

public class HibernateTest {

    private SessionFactory sessionFactory;
    private List<Integer> primaryKeys = new ArrayList<Integer>();

    @Before
    public void setUp() throws Exception {
        sessionFactory = new Configuration().configure() // configures settings
                                                         // from
                                                         // hibernate.cfg.xml
                .buildSessionFactory();
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void test() {
        Session session = sessionFactory.openSession();
        session.beginTransaction();
        Person person = new Person("Our very first Person!");
        session.save(person);
        primaryKeys.add(person.getId());
        Person person2 = new Person("A follow up Person");
        session.save(person2);
        primaryKeys.add(person2.getId());
        session.getTransaction().commit();
        session.close();

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
        // now lets pull Persons from the database and list them
        Session session = sessionFactory.openSession();

        session.beginTransaction();
        @SuppressWarnings("unchecked")
        List<Person> result = session.createQuery("from Person").list();
        for (Person Person : (List<Person>) result) {
            System.out.println("Person (" + Person.getName() + ")");
        }
        session.getTransaction().commit();
        session.close();
    }

    private class ReadingRecordPessimistic implements Runnable {

        private int id = -1;

        public ReadingRecordPessimistic(int id) {
            this.id = id;
        }

        public void run() {
            try {
                System.out.println("Started thread: " + id);
                Session session = sessionFactory.openSession();
                session.beginTransaction();

                Person person = (Person) session.get(Person.class, primaryKeys
                        .get(0), new LockOptions(LockMode.PESSIMISTIC_WRITE));

                System.out.println(person);

                System.out.println("Sleeping thread: " + id);
                Thread.sleep(4000);
                System.out.println("Waking thread: " + id);

                session.getTransaction().commit();
                session.close();
                System.out.println("Finished thread: " + id);
            } catch (Throwable e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}
