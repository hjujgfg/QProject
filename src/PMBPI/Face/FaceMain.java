package PMBPI.Face;

/**
 * Created by egor on 4/14/14.
 */
public class FaceMain {
    public static void main(String[] args) {
        /*int[] nums = {22, 32, 30, 7, 12, 13, 14, 15};
        int[] fs = {1, 2};*/
        //int[] nums = {1, 2, 3, 4, 5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30};
        int[] nums = new int[40];
        int[] fs = {1, 2, 3, 4, 9};


        /*int[] n1 = {22, 32, 30, 7, 12, 13, 14, 15, 16, 35, 36, 37, 38, 39, 40};
        int[] fs1 = {3, 4, 5, 6};*/
        int[] n1 = new int[40];
        for (int i = 1; i < 41; i ++) {
            n1[i-1] = i;
            nums[i-1] = i;
        }
        int [][] training = Eigenface.readFaces(nums, fs);
        int[] fs1 = {10, 5, 6};
        int[][] I = Eigenface.readFaces(n1, fs1);
        Eigenface.localMain(training, I, 92, 112);


    }
}
