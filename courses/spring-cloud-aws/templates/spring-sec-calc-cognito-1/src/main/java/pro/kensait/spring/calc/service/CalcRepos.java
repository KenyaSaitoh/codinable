package pro.kensait.spring.calc.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

/*
 * 計算結果を保存するリポジトリ
 */
@Service
public class CalcRepos {
    private Map<Integer, CalcResult> calcMap = new HashMap<>();

    // 主キー検索
    public CalcResult find(int id) {
        return calcMap.get(id);
    }

    // 全件検索
    public List<CalcResult> findAll() {
        return new ArrayList<CalcResult>(calcMap.values());
    }

    // 挿入
    public int save(CalcResult calcResult) {
        int maxId = 0;
        for (int id : calcMap.keySet()) {
            if (maxId <= id) maxId = id;
        }
        int nextId = maxId + 1;
        calcResult.setId(nextId);
        calcMap.put(nextId, calcResult);
        return 1;
    }
}
