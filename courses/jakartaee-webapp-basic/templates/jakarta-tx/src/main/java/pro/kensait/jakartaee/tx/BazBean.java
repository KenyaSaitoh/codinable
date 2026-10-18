package pro.kensait.jakartaee.tx;

import java.util.List;

import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

/** UserTransaction で 1 件ずつ独立したトランザクションにする（@Transactional は付けない） */
@ApplicationScoped
public class BazBean {
    @Resource
    private UserTransaction userTransaction;

    @Inject
    private QuxBean quxBean;

    public int doEach(List<Integer> params) throws Exception {
        int committed = 0;
        for (int param : params) {
            userTransaction.begin();
            try {
                quxBean.doBusiness(param);
                userTransaction.commit();
                committed++;
            } catch (RuntimeException e) {
                userTransaction.rollback();
            }
        }
        return committed;
    }
}
