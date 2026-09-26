package pro.kensait.jpa.company.main.crud;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.type.JobType;

// カスケードのテスト
public class CascadeStrategyMain {

    public static void main(String[] args) {
        // persist操作（INSERT）、Many側からカスケード
        {
            System.out.println("***** snippet_1 *****");
            // EntityManagerFactory → EntityManagerを取得する
            try (EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("MyPersistenceUnit");
                    EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                try {

                    // EntityTransactionを開始する
                    EntityTransaction entityTransaction = entityManager.getTransaction();
                    entityTransaction.begin();

                    List<Employee> employees = new ArrayList<Employee>();
                    Department department = new Department(5, "総務部", "TOKYO HQ", employees, null);
                    Employee employee = new Employee(10051, "Steve", department, LocalDate.now(),
                            JobType.LEADER, 380000, null);
                    department.getEmployees().add(employee);
                    entityManager.persist(employee);
                    entityTransaction.commit();
                    System.out.println("");
                } finally {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            }
        }

        // persist操作（INSERT）、One側からカスケード
        {
            System.out.println("***** snippet_2 *****");
            // EntityManagerFactory → EntityManagerを取得する
            try (EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("MyPersistenceUnit");
                    EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                try {

                    // EntityTransactionを開始する
                    EntityTransaction entityTransaction = entityManager.getTransaction();
                    entityTransaction.begin();

                    List<Employee> employees = new ArrayList<Employee>();
                    Department department = new Department(6, "管理部", "TOKYO HQ", employees, null);
                    Employee employee = new Employee(10052, "Trent", department, LocalDate.now(),
                            JobType.CHIEF, 310000, null);
                    department.getEmployees().add(employee);
                    entityManager.persist(department);
                    entityTransaction.commit();
                    System.out.println("[ test2 ] End\n");
                } finally {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            }
        }

        // remove操作（DELETE）
        {
            System.out.println("***** snippet_3 *****");
            // EntityManagerFactory → EntityManagerを取得する
            try (EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("MyPersistenceUnit");
                    EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                try {

                    // EntityTransactionを開始する
                    EntityTransaction entityTransaction = entityManager.getTransaction();
                    entityTransaction.begin();

                    Department department = entityManager.find(Department.class, 5);
                    entityManager.remove(department);
                    entityTransaction.commit();
                    System.out.println("");
                } finally {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            }
        }

        // remove操作（DELETE）
        {
            System.out.println("***** snippet_4 *****");
            // EntityManagerFactory → EntityManagerを取得する
            try (EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("MyPersistenceUnit");
                    EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                try {

                    // EntityTransactionを開始する
                    EntityTransaction entityTransaction = entityManager.getTransaction();
                    entityTransaction.begin();

                    Employee employee = entityManager.find(Employee.class, 10001);
                    entityManager.remove(employee);
                    entityTransaction.commit();
                } finally {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            }
        }

        // merge操作（UPDATE）
        {
            System.out.println("***** snippet_5 *****");
            // EntityManagerFactory → EntityManagerを取得する
            try (EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("MyPersistenceUnit");
                    EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                try {

                    // EntityTransactionを開始する
                    EntityTransaction entityTransaction = entityManager.getTransaction();
                    entityTransaction.begin();

                    Employee employee = entityManager.find(Employee.class, 10012);
                    Department department = employee.getDepartment();
                    entityManager.clear(); // mergeのテストのために、取得したエンティティを意図的にDETACHED状態にする
                    employee.setSalary(500000);
                    department.setLocation("品川支社"); // もともとはTOKYO HQ
                    employee.setDepartment(department);
                    entityManager.merge(employee);
                    entityTransaction.commit();
                } finally {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            }
        }

        // refresh操作
        {
            System.out.println("***** snippet_6 *****");
            // EntityManagerFactory → EntityManagerを取得する
            try (EntityManagerFactory entityManagerFactory =
                    Persistence.createEntityManagerFactory("MyPersistenceUnit");
                    EntityManager entityManager = entityManagerFactory.createEntityManager()) {
                try {

                    // EntityTransactionを開始する
                    EntityTransaction entityTransaction = entityManager.getTransaction();
                    entityTransaction.begin();

                    Employee employee = entityManager.find(Employee.class, 10002);
                    Department department = employee.getDepartment();
                    employee.setSalary(999999);
                    department.setDepartmentName("経営PLANNING"); // もともとはPLANNING
                    entityManager.refresh(employee);
                    entityTransaction.commit();
                    System.out.println("[ test6 ] End\n");
                } finally {
                    if (entityManager.getTransaction().isActive()) {
                        entityManager.getTransaction().rollback();
                    }
                }
            }
        }
    }
}
