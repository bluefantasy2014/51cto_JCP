package interview.jcp.staticinnerclass.singleton;

public class Singleton {
    private Singleton() {
    }

    public static Singleton getInstance() {
        return SingletonHolder.INSTACE;
    }

    private static class SingletonHolder {
        private static Singleton INSTACE = new Singleton();
    }
}
