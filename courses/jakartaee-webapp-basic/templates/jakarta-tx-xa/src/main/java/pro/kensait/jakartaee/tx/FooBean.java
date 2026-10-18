package pro.kensait.jakartaee.tx;

import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.transaction.Transactional.TxType;

@ApplicationScoped
@Transactional(value = TxType.REQUIRED,
        rollbackOn = Exception.class)
public class FooBean {
    private static final Logger logger =
            Logger.getLogger(FooBean.class.getName());

    @Inject
    private BarBean barBean;

    @Inject
    private QuxBean quxBean;

    public void doBusiness(int param) {
        logger.info("[ FooBean#doBusiness ] Start");
        barBean.doBusiness(param);
        quxBean.doBusiness(param);
        logger.info("[ FooBean#doBusiness ] End");
    }
}
