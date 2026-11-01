package pro.kensait.java.basic.lsn_19_1_5;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

public class Main_3 {
    public static void main(String[] args) {
        try {
            String param = ""; // Codinable では引数を渡せないので固定の値にしている
            if (param.equals("")) {
                throw new FileNotFoundException();
            }
            throw new MalformedURLException();
        } catch (FileNotFoundException | MalformedURLException ex) { //【1】
            System.out.println("例外発生, msg => " + ex.getMessage()); //【2】
        }
    }
}