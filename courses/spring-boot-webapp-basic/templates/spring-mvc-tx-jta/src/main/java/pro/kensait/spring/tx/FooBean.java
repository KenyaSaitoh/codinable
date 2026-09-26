package pro.kensait.spring.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

/*
 * JTA標準アノテーション（jakarta.transaction.Transactional）を使用する
 * SpringのTransactionalと異なりrollbackFor属性はなく、
 * 非チェック例外（RuntimeException）で自動的にロールバックされる
 */
@Service
@Transactional(TxType.REQUIRED)
public class FooBean {
    private static final Logger logger = LoggerFactory.getLogger(
            FooBean.class);

    // インジェクションポイント（フィールドインジェクション）
    @Autowired
    private BarBean barBean;

    // インジェクションポイント（フィールドインジェクション）
    @Autowired
    private QuxBean quxBean;

    // ビジネスメソッド
    public void doBusiness(int param) {
        logger.info("[ FooBean#doBusiness ] Start");

        // セッションBean（BarBean）のビジネスメソッドを呼び出す
        barBean.doBusiness(param);

        // セッションBean（QuxBean）のビジネスメソッドを呼び出す
        quxBean.doBusiness(param);

        logger.info("[ FooBean#doBusiness ] End");
    }
}