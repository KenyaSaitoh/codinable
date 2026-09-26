package pro.kensait.spring.employee.flux.api;

import java.net.URI;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.spring.employee.flux.repository.Employee;
import pro.kensait.spring.employee.flux.service.EmployeeFluxService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * 社員管理システムのリアクティブREST APIを表すクラス
 */
// Spring MVC（命令型）ではリクエストごとにスレッドを割り当て、処理が終わるまで
// そのスレッドを占有する（ブロッキング）一方WebFluxでは、少数のイベントループ
// スレッドがノンブロッキングI/Oで多数のリクエストを捌くため、
// 大量の同時接続に対して少ないリソースでスケールする
// APIメソッドはMono（0〜1件）やFlux（0〜N件）を返すだけでよく、
// 購読（subscribe）はフレームワーク側が行う
// また、リアクティブストリームには「バックプレッシャー」という仕組みがあり、
// 消費側（クライアント）が処理できる量だけを生産側に要求することで、
// 速度差によるデータの溢れを防ぐことができる
@RestController
@RequestMapping("/employees")
public class EmployeeFluxApi {
    private static final Logger logger = LoggerFactory.getLogger(
            EmployeeFluxApi.class);

    // インジェクションポイント
    @Autowired
    private EmployeeFluxService employeeService;

    // APIメソッド：主キー検索によるEmployee取得
    @GetMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<Employee>> get(
            @PathVariable("employeeId") Integer employeeId) {
        logger.info("[ EmployeeFluxApi#get ]");

        // ビジネスロジックを呼び出し、社員が存在すればステータス200で応答する
        // 存在しない場合は空のMonoが返るため、switchIfEmptyオペレーターで
        // ステータス404のResponseEntityに切り替える
        return employeeService.getEmployee(employeeId)
                .map(employee -> ResponseEntity.ok().body(employee))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // APIメソッド：全Employeeリスト取得
    @GetMapping
    public Flux<Employee> getAll() {
        logger.info("[ EmployeeFluxApi#getAll ]");

        // ビジネスロジックを呼び出し、Fluxをそのまま返す
        // （購読と応答への書き出しはWebFluxフレームワークが行う）
        return employeeService.getEmployeesAll();
    }

    // APIメソッド：条件検索（給与範囲をキーに）によるEmployeeリスト取得
    @GetMapping(path = "/query_by_salary")
    public Flux<Employee> queryBySalary(
            @RequestParam("lowerSalary") Integer lowerSalary,
            @RequestParam("upperSalary") Integer upperSalary) {
        logger.info("[ EmployeeFluxApi#queryBySalary ]");

        // ビジネスロジックを呼び出し、給与範囲で絞り込んだFluxを返す
        return employeeService.getEmployeesBySalary(lowerSalary, upperSalary);
    }

    // APIメソッド：Employeeの新規作成
    @PostMapping
    public Mono<ResponseEntity<Employee>> create(
            @Valid @RequestBody Employee employee) {
        logger.info("[ EmployeeFluxApi#create ]");

        // ビジネスロジックを呼び出してEmployeeを新規作成し、
        // ステータス201とLocationヘッダーを持つResponseEntityに変換して返す
        return employeeService.createEmployee(employee)
                .map(created -> ResponseEntity
                        .created(URI.create("/employees/" + created.employeeId()))
                        .body(created));
    }

    // APIメソッド：Employeeの更新
    @PutMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<Employee>> update(
            @PathVariable("employeeId") Integer employeeId,
            @Valid @RequestBody Employee employee) {
        logger.info("[ EmployeeFluxApi#update ]");

        // ビジネスロジックを呼び出してEmployeeを更新し、ステータス200で応答する
        // 更新対象が存在しない場合はステータス404で応答する
        return employeeService.updateEmployee(employeeId, employee)
                .map(updated -> ResponseEntity.ok().body(updated))
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // APIメソッド：Employeeの削除
    @DeleteMapping(path = "/{employeeId}")
    public Mono<ResponseEntity<Void>> delete(
            @PathVariable("employeeId") Integer employeeId) {
        logger.info("[ EmployeeFluxApi#delete ]");

        // ビジネスロジックを呼び出してEmployeeを削除し、ステータス204で応答する
        // 削除対象が存在しない場合はステータス404で応答する
        return employeeService.removeEmployee(employeeId)
                .map(removed -> ResponseEntity.noContent().<Void> build())
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    // APIメソッド：全EmployeeのSSEストリーミング配信
    // producesにTEXT_EVENT_STREAM_VALUEを指定すると、Server-Sent Events（SSE）
    // として1件ずつクライアントに配信される（全件揃うのを待たない）
    // 命令型MVCのList返却では「全件まとめて1回で応答」だが、リアクティブでは
    // 「準備できた要素から順次ストリーミング」できるのが大きな違い
    @GetMapping(path = "/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<org.springframework.http.codec.ServerSentEvent<Object>> stream() {
        logger.info("[ EmployeeFluxApi#stream ]");

        // ビジネスロジックを呼び出し、1秒間隔のストリーミングFluxを返す
        return employeeService.streamEmployees()
                .map(employee -> org.springframework.http.codec.ServerSentEvent.<Object>builder(employee).build())
                .concatWithValues(org.springframework.http.codec.ServerSentEvent.<Object>builder()
                        .event("complete").data(java.util.Map.of("complete", true)).build());
    }
}
