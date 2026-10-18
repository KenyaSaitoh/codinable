package pro.kensait.jakartaee.calc.service;

/** 2 つの数値の計算だけを受け持つ。状態を持たないので 1 つを共有してよい */
public class CalcService {

    public double add(double param1, double param2) {
        return param1 + param2;
    }
}
