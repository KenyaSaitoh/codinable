package pro.kensait.java.advanced.lsn_3_2_5.invokeall;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import pro.kensait.java.advanced.lsn_3_2_4.callable.CallableTask;

public class Main {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newCachedThreadPool();
        List<CallableTask> taskList = new ArrayList<CallableTask>();
        taskList.add(new CallableTask("foo", 8)); //【1】
        taskList.add(new CallableTask("bar", 15));
        taskList.add(new CallableTask("baz", 5));

        try {
            System.out.println("[ Main ] starting all tasks...");
            List<Future<Integer>> futureList = executor.invokeAll(taskList); //【2】
            for (Future<Integer> future : futureList) { //【3】
                System.out.println("result => " + future.get());
            }
            System.out.println("[ Main ] finish");
        } catch(InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }

        executor.shutdown();
    }
}