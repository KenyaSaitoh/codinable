package pro.kensait.jpa.company.main.crud;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import pro.kensait.jpa.company.entity.Department;
import pro.kensait.jpa.company.entity.Employee;

// フェッチのテスト
public class FetchStrategyMain {

    public static void main(String[] args) {
        // EntityManagerFactory → EntityManagerを取得する
        try (EntityManagerFactory entityManagerFactory =
                Persistence.createEntityManagerFactory("MyPersistenceUnit");
                EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            try {

                // Employee → Department（イーガーフェッチ）
                {
                    System.out.println("***** snippet_1 *****");
                    System.out.println("===== find()メソッド呼び出し開始 =====");
                    Employee employee = entityManager.find(Employee.class, 10011);
                    System.out.println("===== 関連するエンティティの" +
                            "永続フィールドにアクセス開始 =====");
                    String employeeName = employee.getEmployeeName();
                    System.out.println("employee.getEmployeeName() => " + employeeName);
                }
                {
                    // Department → Employee（レイジーフェッチ）
                    System.out.println("***** snippet_1 *****");
                    System.out.println("===== find()メソッド呼び出し開始 =====");
                    Department department = entityManager.find(Department.class, 1);
                    System.out.println("===== 関連するエンティティの" +
                            "永続フィールドにアクセス開始 =====");
                    Employee employee = department.getEmployees().get(0);
                    System.out.println("department.getEmployees().get(0) => "
                            + employee);
                }
                // レイジーフェッチにおける「N+1 SELECT」問題
                {
                    System.out.println("***** snippet_1 *****");

                    Query query = entityManager.createQuery("SELECT d FROM Department AS d");
                    List<Department> departmentList = (List<Department>) query.getResultList();
                    System.out.println("##### ループ開始 #####");
                    for (Department department : departmentList) {
                        List<Employee> employeeList = department.getEmployees();
                        for (Employee employee : employeeList) {
                            System.out.println(employee.getEmployeeName());
                        }
                    }
                    System.out.println("[ test3 ] End\n");
                }

                // test3における「N+1 SELECT」をフェッチジョインによって解決する
                {
                    System.out.println("***** snippet_1 *****");
                    Query query = entityManager.createQuery(
                            "SELECT DISTINCT d FROM Department AS d JOIN FETCH d.employees");
                    List<Department> departmentList = (List<Department>) query.getResultList();
                    // DICTINCTをつけない場合、結果が重複するので、
                    // Setに代入して重複を取り除くこともできる
                    // Set<Department> results = new HashSet<Department>(departmentList);
                    for (Department department : departmentList) {
                        System.out.println("### " + department.getDepartmentName() + " ###");
                        List<Employee> employeeList = department.getEmployees();
                        for (Employee employee : employeeList) {
                            System.out.println(employee.getEmployeeName());
                        }
                    }
                }
            } finally {
                if (entityManager.getTransaction().isActive()) {
                    entityManager.getTransaction().rollback();
                }
            }
        }
    }
}
