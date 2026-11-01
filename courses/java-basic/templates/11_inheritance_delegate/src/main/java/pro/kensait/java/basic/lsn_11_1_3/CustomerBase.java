package pro.kensait.java.basic.lsn_11_1_3;

public abstract class CustomerBase {
    protected int id;
    protected String name;
    protected int point;

    public CustomerBase(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    // 【1】購入金額の上限をチェックする（共通実装）
    public boolean overTotalPrice(int totalPrice) { //【1】
        if (1_000_000 < totalPrice) {
            return true;
        }
        return false;
    }

    // 【2】ポイントを加算する（抽象メソッド）
    public abstract void addPoint(int value); //【2】
}