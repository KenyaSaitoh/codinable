package pro.kensait.spring.employee.flux.service;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pro.kensait.spring.employee.flux.repository.Employee;
import pro.kensait.spring.employee.flux.repository.EmployeeReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/*
 * 社員に対するリアクティブなビジネスロジックを表すクラス
 */
// リアクティブプログラミングでは、サービスメソッドも値そのものではなく
// Mono（0〜1件）やFlux（0〜N件）という「非同期ストリーム」を返す
// 処理はmap/filter/flatMapなどのオペレーターを連結（パイプライン化）して記述し、
// 実際のデータは購読（subscribe）された時点で初めて流れ始める
// 注意: パイプラインの途中でblock()を呼ぶとイベントループスレッドが
// ブロックされ、ノンブロッキングの利点が失われるため、絶対に呼んではいけない
@Service
public class EmployeeFluxService {
    private static final Logger logger = LoggerFactory.getLogger(
            EmployeeFluxService.class);

    // インジェクションポイント
    @Autowired
    private EmployeeReactiveRepository employeeRepository;

    // コンストラクタ
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring owns the shared repository; copying it would split application data")
    public EmployeeFluxService(EmployeeReactiveRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // サービスメソッド：社員の取得
    public Mono<Employee> getEmployee(Integer employeeId) {
        logger.info("[ EmployeeFluxService#getEmployee ]");

        // 主キー検索を実行する（存在しない場合は空のMonoが返る）
        return employeeRepository.findById(employeeId);
    }

    // サービスメソッド：全社員の取得
    public Flux<Employee> getEmployeesAll() {
        logger.info("[ EmployeeFluxService#getEmployeesAll ]");
        return employeeRepository.findAll();
    }

    // サービスメソッド：社員を検索する（給与範囲をキーに）
    public Flux<Employee> getEmployeesBySalary(Integer lowerSalary,
            Integer upperSalary) {
        if (lowerSalary < 0 || lowerSalary > upperSalary) {
            throw new IllegalArgumentException("給与の下限は0以上、上限以下にしてください");
        }
        logger.info("[ EmployeeFluxService#getEmployeesBySalary ]");

        // filterオペレーターで、ストリームを流れる社員を給与範囲で絞り込む
        // （SQLのWHERE句に相当する処理を、オペレーターの連結で表現している）
        return employeeRepository.findAll()
                .filter(employee -> lowerSalary <= employee.salary()
                        && employee.salary() <= upperSalary);
    }

    // サービスメソッド：全社員の名前の取得
    public Flux<String> getEmployeeNamesAll() {
        logger.info("[ EmployeeFluxService#getEmployeeNamesAll ]");

        // mapオペレーターで、ストリームを流れる社員を社員名に変換する
        return employeeRepository.findAll()
                .map(Employee::employeeName);
    }

    // サービスメソッド：社員の追加
    public Mono<Employee> createEmployee(Employee employee) {
        logger.info("[ EmployeeFluxService#createEmployee ]");

        // IDをnullにした社員を保存し、新規採番されたIDを含む社員を発行する
        Employee target = new Employee(null, employee.employeeName(),
                employee.departmentId(), employee.departmentName(),
                employee.jobName(), employee.salary(), employee.entranceDate());
        return employeeRepository.save(target);
    }

    // サービスメソッド：社員の更新
    public Mono<Employee> updateEmployee(Integer employeeId, Employee employee) {
        logger.info("[ EmployeeFluxService#updateEmployee ]");

        // flatMapオペレーターで「主キー検索→保存」という非同期処理を連結する
        // （flatMapは、値から新たなMono/Fluxを生成して合流させるオペレーター）
        // 更新対象が存在しない場合は、空のMonoがそのまま後続に伝播する
        return employeeRepository.findById(employeeId)
                .flatMap(found -> employeeRepository.save(
                        new Employee(employeeId, employee.employeeName(),
                                employee.departmentId(), employee.departmentName(),
                                employee.jobName(), employee.salary(),
                                employee.entranceDate())));
    }

    // サービスメソッド：社員の削除
    public Mono<Employee> removeEmployee(Integer employeeId) {
        logger.info("[ EmployeeFluxService#removeEmployee ]");

        // 削除対象が存在する場合のみ削除を実行し、削除した社員を発行する
        // （thenReturnは、前段のMono<Void>の完了後に指定した値を発行するオペレーター）
        return employeeRepository.findById(employeeId)
                .flatMap(found -> employeeRepository.deleteById(employeeId)
                        .thenReturn(found));
    }

    // サービスメソッド：全社員を1秒間隔でストリーミング配信
    public Flux<Employee> streamEmployees() {
        logger.info("[ EmployeeFluxService#streamEmployees ]");

        // delayElementsオペレーターで、要素を1秒間隔で順次発行する
        // 待機中もスレッドはブロックされず、他のリクエストを処理できる
        // （これがリアクティブストリームによるノンブロッキング処理の見せ場）
        return employeeRepository.findAll()
                .delayElements(Duration.ofSeconds(1));
    }
}
