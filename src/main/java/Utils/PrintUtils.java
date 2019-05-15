package Utils;

public class PrintUtils {
    public static void print(String msg){
        System.out.println("[Current Thread:" + Thread.currentThread().getName() + "]: " + msg);
    }
}
