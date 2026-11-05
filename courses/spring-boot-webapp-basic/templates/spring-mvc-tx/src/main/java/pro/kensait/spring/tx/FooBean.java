package pro.kensait.spring.tx;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/*
 * 外側のトランザクションを開始し、BarBeanとQuxBeanを順に呼び出すクラス
 * SpringのTransactionalのrollbackFor属性により、チェック例外でもロールバックする
 */
@Service
@Transactional(propagation = Propagation.REQUIRED,
    rollbackFor = Exception.class)
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

        // BarBeanのビジネスメソッドを呼び出す
        barBean.doBusiness(param);

        // QuxBeanのビジネスメソッドを呼び出す
        quxBean.doBusiness(param);

        logger.info("[ FooBean#doBusiness ] End");
    }
}
