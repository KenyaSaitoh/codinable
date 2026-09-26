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

    // 社員IDの採番用シーケンス
    private final AtomicInteger sequence = new AtomicInteger(1);

    // 初期化メソッド：初期データとして社員6件の登録
    @PostConstruct
    public void init() {
        saveInternal(new Employee(null, "Alice", 10, "営業部", "Sales",
                300000, LocalDate.of(2018, 4, 1)));
        saveInternal(new Employee(null, "Bob", 20, "開発部", "Engineer",
                350000, LocalDate.of(2015, 4, 1)));
        saveInternal(new Employee(null, "Carol", 20, "開発部", "Engineer",
                400000, LocalDate.of(2012, 4, 1)));
        saveInternal(new Employee(null, "Dave", 10, "営業部", "Manager",
                450000, LocalDate.of(2008, 4, 1)));
        saveInternal(new Employee(null, "Ellen", 30, "総務部", "Clerk",
                250000, LocalDate.of(2020, 4, 1)));
        saveInternal(new Employee(null, "Frank", 30, "総務部", "Manager",
                420000, LocalDate.of(2010, 4, 1)));
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
