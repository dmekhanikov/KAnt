package testData.ClassFile;

public class ClassWithOverrides {
    public ClassWithOverrides() {}
    public ClassWithOverrides(int a) {}
    public ClassWithOverrides(String a) {}

    public void method1() {}

    public int method1(int a) {
        return a;
    }

    public String method1(String s) {
        return s;
    }
}
