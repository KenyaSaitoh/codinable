package pro.kensait.spring.tx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * fuga機能のコントローラー
 */
@Controller
public class FugaController {

    @Autowired
    private FooBean fooBean;
    /*
     Webブラウザのアドレスバーに以下のURLを直接入力し、このメソッドを呼び出す
     正常時 …http://localhost:8080/jta?param=100
     異常時 …http://localhost:8080/jta?param=-5
     異常時は、2つのデータソースへの更新が両方ともロールバックされる
    */
    // jtaの実行
    @RequestMapping(value = "/jta", method = RequestMethod.GET)
    @ResponseBody
    public String jta(@RequestParam(value = "param", required = true)
            Integer param) {

        // ビジネスメソッドを呼び出す
        try {
            fooBean.doBusiness(param);
        } catch(RuntimeException re) {
            return "ERROR OCCURED!!!";
        }
        return "NORMAL END";
    }
}
