package pro.kensait.spring.calc.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pro.kensait.spring.calc.config.ConfigHolder;
import pro.kensait.spring.calc.service.exception.LimitOverException;
import pro.kensait.spring.calc.service.exception.ZeroDivideException;

/*
 * 計算機能のビジネスロジックを表すクラス
 */
@Service
public class CalcService {
    private static final Logger logger = LoggerFactory.getLogger(
            CalcService.class);

    // インジェクションポイント
    @Autowired
    private ConfigHolder config;

    // インジェクションポイント
    @Autowired
    private CalcRepos repos;

    // サービスメソッド：足し算の実行
    public int add(BigDecimal param1, BigDecimal param2, String username) {
        logger.info("[ CalcService#add ]");

        // 足し算を実行し、四捨五入してスケールを合わせる
        BigDecimal result = param1.add(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);

        // テーブルに計算結果を保存する
        CalcResult calcResult = new CalcResult(username, param1, param2,
                CalcType.ADD, result, LocalDateTime.now());
        repos.save(calcResult);

        return calcResult.getId();
    }

    // サービスメソッド：引き算の実行
    public int subtract(BigDecimal param1, BigDecimal param2, String username) {
        logger.info("[ CalcService#subtract ]");

        // 引き算を実行し、四捨五入してスケールを合わせる
        BigDecimal result = param1.subtract(param2)
                .setScale(config.getCalcResultScale(), RoundingMode.HALF_UP);

        // テーブルに計算結果を保存する
        CalcResult calcResult = new CalcResult(username, param1, param2,
                CalcType.SUBTRACT, result, LocalDateTime.now());
        repos.save(calcResult);

        return calcResult.getId();
    }

    // サービスメソッド：掛け算の実行
    public int multiply(BigDecimal param1, BigDecimal param2, String username)
            throws LimitOverException {
        logger.info("[ CalcService#multiply ]");

        // 掛け算を実行し、四捨五入してスケールを合わせる
        BigDecimal tmpResult = param1.multiply(param2);
        BigDecimal result = tmpResult.setScale(config.getCalcResultScale(),
                RoundingMode.HALF_UP);

        // テーブルに計算結果を保存する
        CalcResult calcResult = new CalcResult(username, param1, param2,
                CalcType.MULTIPLY, result, LocalDateTime.now());
        repos.save(calcResult);

        // 計算結果が極度オーバーしていないかチェックし、発生した場合は例外をスローする
        BigDecimal limit = new BigDecimal(config.getCalcResultLimit());
        if (0 < calcResult.getResult().compareTo(limit)) {
            throw new LimitOverException();
        }

        return calcResult.getId();
    }

    // サービスメソッド：割り算の実行
    public int divide(BigDecimal param1, BigDecimal param2, String username)
            throws ZeroDivideException {
        logger.info("[ CalcService#divide ]");

        // ゼロ割が発生するかチェックし、発生した場合は例外をスローする
        if (param2.compareTo(BigDecimal.ZERO) == 0) {
            throw new ZeroDivideException();
        }

        // 割り算を実行し、四捨五入してスケールを合わせる
        BigDecimal result = param1.divide(param2, config.getCalcResultScale(),
                RoundingMode.HALF_UP);

        // テーブルに計算結果を保存する
        CalcResult calcResult = new CalcResult(username, param1, param2,
                CalcType.DIVIDE, result, LocalDateTime.now());
        repos.save(calcResult);

        return calcResult.getId();
    }

    // サービスメソッド：計算結果の取得
    public CalcResult getCalcResult(int id) {
        return repos.find(id);
    }

    // サービスメソッド：すべての計算結果の取得
    public List<CalcResult> getCalcResults() {
        return repos.findAll();
    }
}