package PMBPI.Face;

/**
 * Created by egor on 4/19/14.
 */
public class Fisherface extends Eigenface {

    public static void localMain(int[] nums, int[] fs) {
        int classQuantity = nums.length;
        int classLength = fs.length;
        int [][] matr = readFaces(nums, fs);
        int [][] classMeans = new int[classQuantity][];
        //int [] generalMean = calcMean(matr);
        for (int i = 0; i < classQuantity; i ++) {
            //classMeans[i] = calcMean(getClass(matr, i, classLength));
        }

    }
    static int[][] getClass(int [][] arr, int classN, int classLength) {
        int [][] res = new int [classLength][];
        for (int i = 0; i < classLength; i++) {
            res[i] = arr[classN * classLength + i];
        }
        return res;
    }

    static int[][] clacBetweenClass(int[][] means, int[] mean) {
        int[][] res = new int[means[0].length][means[0].length];

        for (int i = 0; i < means.length; i ++) {

        }

        return res;
    }
}
