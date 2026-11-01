import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Codinable の実行ボタンはキーボードの入力を渡せないので、stdin/ の入力ファイルを標準入力の代わりに渡す。
 * 教材のクラスは Scanner を入力のたびに new するので、1 回の読み取りで 1 行だけ返し、
 * 読んだ行は入力したときと同じように画面にも出す。教材のコードではない。
 */
public class StdinRunner {
    public static void main(String[] args) throws Exception {
        byte[] input = Files.readAllBytes(Path.of(System.getProperty("stdin.file")));
        System.setIn(new LineInput(input));
        var main = Class.forName(args[0]).getMethod("main", String[].class);
        try {
            main.invoke(null, (Object) new String[0]);
        } catch (InvocationTargetException e) {
            // このクラスの呼び出しの行をスタックトレースから除く（教材の説明と同じ表示にする）
            Throwable t = e.getCause();
            trim(t, args[0]);
            System.err.print("Exception in thread \"main\" ");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void trim(Throwable t, String mainClass) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            var s = c.getStackTrace();
            int end = s.length;
            for (int i = 0; i < s.length; i++) {
                if (s[i].getClassName().equals(mainClass) && s[i].getMethodName().equals("main")) {
                    end = i + 1;
                    break;
                }
            }
            c.setStackTrace(Arrays.copyOf(s, end));
        }
    }

    private static final class LineInput extends InputStream {
        private final ByteArrayInputStream in;

        LineInput(byte[] bytes) {
            in = new ByteArrayInputStream(bytes);
        }

        @Override public int read() {
            return in.read();
        }

        @Override public int read(byte[] b, int off, int len) throws IOException {
            int n = 0;
            while (n < len) {
                int c = in.read();
                if (c < 0) break;
                b[off + n++] = (byte) c;
                if (c == 10) break; // 改行まで
            }
            if (n == 0) return -1;
            System.out.write(b, off, n);
            System.out.flush();
            return n;
        }

        // 「続きがある」と答えると、最初の Scanner が次の行まで読み込んでしまう
        @Override public int available() {
            return 0;
        }
    }
}
