package PMBPI.Face;

/**
 * Created by egor on 4/15/14.
 */
public class NativeComputations {
    static {
        System.loadLibrary("native");
    }

    public native void sayHello();

}
