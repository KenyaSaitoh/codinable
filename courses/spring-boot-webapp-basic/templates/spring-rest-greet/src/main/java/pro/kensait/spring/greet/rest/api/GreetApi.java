package pro.kensait.spring.greet.rest.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
 * 挨拶機能のAPIを提供するクラス
 */
@RestController
@RequestMapping("/greet")
@CrossOrigin
public class GreetApi {

    // APIメソッド：GET + パスパラメータ
    // sayhelloの実行
    @GetMapping(path = "/hello/{name}")
    public ResponseEntity<String> sayHello(
            @PathVariable("name") String name) {
        String result = "Hello " + name + "!";
        return ResponseEntity.ok().body(result);
    }

    // APIメソッド：GET + パスパラメータ
    // saygoodbyeの実行
    @GetMapping(path = "/goodbye/{name}")
    public ResponseEntity<String> sayGoodbye(
            @PathVariable("name") String name) {
        String result = "Goodbye " + name + "!";
        return ResponseEntity.ok().body(result);
    }

    // APIメソッド：GET + クエリパラメータ
    // saygoodmorningの実行
    @GetMapping(path = "/morning")
    public ResponseEntity<String> sayGoodMorning(
            @RequestParam("name") String name) {
        String result = "Good Morning " + name + "!";
        return ResponseEntity.ok().body(result);
    }

    // APIメソッド：POST + フォーム
    // saygoodafternoonの実行
    @PostMapping(path = "/afternoon")
    public ResponseEntity<String> sayGoodAfternoon(
            @RequestParam("name") String name) {
        String result = "Good Afternoon " + name + "!";
        return ResponseEntity.ok().body(result);
    }

    // APIメソッド：POST + フォーム
    // saygoodeveningの実行
    @PostMapping(path = "/evening")
    public ResponseEntity<String> sayGoodEvening(
            @RequestBody GreetingRequest greetingRequest) {
        String result = "Good Evening " + greetingRequest.name() + "!";
        return ResponseEntity.ok().body(result);
    }

    // APIメソッド：GET + HTTPヘッダ
    // saygoodnightの実行
    @GetMapping(path = "/night")
    public ResponseEntity<String> sayGoodNight(
            @RequestHeader("name") String name) {
        String result = "Good Night " + name + "!";
        return ResponseEntity.ok().body(result);
    }
}
