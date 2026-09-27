package pro.kensait.spring.employee.flux.repository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * 社員を保持するインメモリのリアクティブリポジトリを表すクラス
 */
// リアクティブアプリケーションでは、リポジトリもMono/Fluxを返す非同期APIにする必要がある
// 本来はSpring Data R2DBC（リアクティブ対応のDBアクセス）を使うところだが、
// 本講座で使用しているHSQLDBにはR2DBCドライバが存在しないため、
// このクラスではConcurrentHashMapによるインメモリ実装で代替している
// 実運用ではR2DBC対応DB（PostgreSQL、MySQL等）とspring-boot-starter-data-r2dbcを使う
@Repository
public class EmployeeReactiveRepository {

    // 社員を保持するマップ（キーは社員ID）
    private final Map<Integer, Employee> employeeMap = new ConcurrentHashMap<>();

    // 社員IDの採番用シーケンス（初期データが10001〜10016、新規は10017から）
    private final AtomicInteger sequence = new AtomicInteger(10001);

    // 初期化メソッド：初期データとして社員16件の登録
    @PostConstruct
    public void init() {
        saveInternal(new Employee(null, "Alice", 3, "SALES", "MANAGER",
                500000, LocalDate.of(2012, 4, 1)));
        saveInternal(new Employee(null, "Bob", 1, "PLANNING", "MANAGER",
                450000, LocalDate.of(2012, 4, 1)));
        saveInternal(new Employee(null, "Carol", 2, "HR", "CHIEF",
                350000, LocalDate.of(2012, 4, 1)));
        saveInternal(new Employee(null, "Dave", 3, "SALES", "LEADER",
                400000, LocalDate.of(2012, 4, 1)));
        saveInternal(new Employee(null, "Ellen", 3, "SALES", "CHIEF",
                300000, LocalDate.of(2013, 4, 1)));
        saveInternal(new Employee(null, "Frank", 1, "PLANNING", "ASSOCIATE",
                250000, LocalDate.of(2013, 10, 1)));
        saveInternal(new Employee(null, "Ivan", 4, "PRODUCT", "MANAGER",
                480000, LocalDate.of(2014, 1, 1)));
        saveInternal(new Employee(null, "Justin", 2, "HR", "MANAGER",
                460000, LocalDate.of(2014, 4, 1)));
        saveInternal(new Employee(null, "Mallory", 4, "PRODUCT", "LEADER",
                420000, LocalDate.of(2014, 7, 1)));
        saveInternal(new Employee(null, "Matilda", 3, "SALES", "ASSOCIATE",
                280000, LocalDate.of(2015, 8, 1)));
        saveInternal(new Employee(null, "Oscar", 4, "PRODUCT", "CHIEF",
                320000, LocalDate.of(2015, 11, 1)));
        saveInternal(new Employee(null, "Pat", 4, "PRODUCT", "ASSOCIATE",
                240000, LocalDate.of(2016, 4, 1)));
        saveInternal(new Employee(null, "Peggy", 3, "SALES", "ASSOCIATE",
                270000, LocalDate.of(2016, 10, 1)));
        saveInternal(new Employee(null, "Victor", null, null, "ASSOCIATE",
                220000, LocalDate.of(2017, 4, 1)));
        saveInternal(new Employee(null, "Steve", 1, "PLANNING", "LEADER",
                380000, LocalDate.of(2017, 10, 1)));
        saveInternal(new Employee(null, "Trent", 4, "PRODUCT", "CHIEF",
                310000, LocalDate.of(2017, 10, 1)));
    }

    // リポジトリメソッド：主キー検索によって社員の取得
    public Mono<Employee> findById(Integer employeeId) {
        // Mono.justOrEmptyは、値が存在すればその値を発行し、
        // nullであれば空のMono（onCompleteのみ）を返す
        return Mono.defer(() -> Mono.justOrEmpty(employeeMap.get(employeeId)));
    }

    // リポジトリメソッド：全社員の取得
    public Flux<Employee> findAll() {
        // Flux.deferで購読時点のマップ内容からFluxを生成する
        // （購読されるまでデータは評価されない＝遅延評価）
        return Flux.defer(() -> Flux.fromIterable(employeeMap.values()))
                .sort(Comparator.comparing(Employee::employeeId));
    }

    // リポジトリメソッド：社員を保存する（IDがnullの場合は新規採番する）
    public Mono<Employee> save(Employee employee) {
        // Mono.fromSupplierで保存処理を遅延実行し、保存後の社員を発行する
        return Mono.fromSupplier(() -> saveInternal(employee));
    }

    // リポジトリメソッド：主キー指定によって社員の削除
    public Mono<Void> deleteById(Integer employeeId) {
        // Mono.fromRunnableで削除処理を遅延実行し、完了のみを通知する
        return Mono.fromRunnable(() -> employeeMap.remove(employeeId));
    }

    // 社員をマップに保存する（IDがnullの場合はシーケンスから新規採番する）
    private Employee saveInternal(Employee employee) {
        Integer employeeId = employee.employeeId() == null
                ? Integer.valueOf(sequence.getAndIncrement())
                : employee.employeeId();
        Employee saved = new Employee(employeeId, employee.employeeName(),
                employee.departmentId(), employee.departmentName(),
                employee.jobName(), employee.salary(), employee.entranceDate());
        employeeMap.put(employeeId, saved);
        return saved;
    }
}
