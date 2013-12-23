package testData.ClassFile;

public class ClassWithStaticInit {
    private static int a;
    static {
        a = 5;
    }
    private static int b;
    static {
        b = a;
    }
}
