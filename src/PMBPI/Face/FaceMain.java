package PMBPI.Face;

/**
 * Created by egor on 4/14/14.
 */
public class FaceMain {
    public static void main(String[] args) {
        int[] nums = {22, 32, 30, 7, 12};
        int[] fs = {1, 2};
        int [][] training = Eigenface.readFaces(nums, fs);
        int[] n1 = {22, 32, 30, 7, 16, 35};
        int[] fs1 = {3, 4, 5, 6};
        int[][] I = Eigenface.readFaces(n1, fs1);
        Eigenface.localMain(training, I);


    }
}
