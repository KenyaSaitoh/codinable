package pro.kensait.java.basic.lsn_11_1_2.case_3;

public class Bar extends Foo {
    public int base = 200; // 同じ名前のフィールドを再宣言
    public int y; // 追加
    public Bar(int x, int y) {
        super(x);
        this.y = y;
    }
	public int subtract() { // 追加
		return base - getX() - y;
	}
	@Override
	public int add() {
		System.out.println(super.base + "#" + this.base); // 100#200

		return base + getX() + y; // 200 + 30 + 10 = 240
		// return super.base + x + y; // 140
	}
	public int add(int z) { // これは上書きではない
		return base + getX() + y + z;
	}
}