package pro.kensait.jakartaee.calc.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * 計算履歴をメモリー上の Map に保存する DAO
 * @ApplicationScoped なので、履歴はすべての利用者で共有され、アプリケーションを停止すると失われる
 */
@ApplicationScoped
public class CalcDAO {
    private final Map<Integer, CalcResult> calcMap = new ConcurrentHashMap<>();

    public CalcResult find(int id) {
        return calcMap.get(id);
    }

    public List<CalcResult> findAll() {
        List<CalcResult> list = new ArrayList<>(calcMap.values());
        list.sort(Comparator.comparing(CalcResult::getId));
        return list;
    }

    // 最大の ID に 1 を足して採番し、保存する
    public synchronized int save(CalcResult calc) {
        int nextId = calcMap.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        calc.setId(nextId);
        calcMap.put(nextId, calc);
        return 1;
    }
}
