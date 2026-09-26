package org.study.ee.jpa.company.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.study.ee.jpa.company.test.base.JpaTestBase;
import org.study.ee.jpa.company.test.util.ResultUtil;

import pro.kensait.jpa.company.entity.Subsidiary;
import pro.kensait.jpa.company.entity.Employee;
import pro.kensait.jpa.company.entity.EmployeePK;

// 主キークラスのテスト（リレーションあり）
public class JpaIdClassRelationTest extends JpaTestBase {

    // 検索（主キーから）
    @Test
    public void test1() {
        System.out.println("[ test1 ] Start");
        Employee employee = em.find(Employee.class, new EmployeePK(2, 10002));
        assertEquals("Justin", employee.getEmployeeName());
        ResultUtil.showEmployee(employee);
        System.out.println("[ test1 ] End\n");
    }

    // 挿入
    @Test
    public void test2() {
        System.out.println("[ test2 ] Start");
        Subsidiary subsidiary = em.find(Subsidiary.class, 2);
        Employee employee = 
                new Employee(6, "なかがわ せいじ", subsidiary, 360000);
        em.persist(employee);
        commit();
        em.clear();
        assertEquals(360000, em.find(Employee.class, new EmployeePK(2, 6)).getSalary());
        ResultUtil.showEmployeeFromDatabase(2, 6);
        System.out.println("[ test2 ] End\n");
    }

    // 削除
    @Test
    public void test3() {
        System.out.println("[ test3 ] Start");
        Employee employee = em.find(Employee.class, new EmployeePK(2, 10002));
        em.remove(employee);
        commit();
        em.clear();
        assertNull(em.find(Employee.class, new EmployeePK(2, 10002)));
        ResultUtil.showEmployeeFromDatabase(2, 10002);
        System.out.println("[ test3 ] End\n");
    }

    // 更新
    @Test
    public void test4() {
        System.out.println("[ test4 ] Start");
        Employee employee = em.find(Employee.class, new EmployeePK(2, 10002));
        employee.setSalary(employee.getSalary() + 10000);
        commit();
        em.clear();
        assertEquals(470000, em.find(Employee.class, new EmployeePK(2, 10002)).getSalary());
        ResultUtil.showEmployeeFromDatabase(2, 10002);
        System.out.println("[ test4 ] End\n");
    }
}
