package PMBPI.Face;

import com.sun.xml.internal.bind.marshaller.DataWriter;

import java.io.*;
import java.nio.channels.NonWritableChannelException;
import java.util.ArrayList;

import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 * Created by egor on 4/14/14.
 */
public class Eigenface {

    private int[][] sampleMatrix;

    public static int[][] readFaces(int[] nums) {
        int[][] matr = new int[nums.length][];
        for (int i = 0; i < nums.length; i++) {
            matr[i] = readFace("faces/s" + nums[i] + "/1.pgm");
        }
        return matr;
    }
    public static void localMain(int[] nums) {
        int [][] matr = readFaces(nums);
        int[] mean = calcMean(matr);
        subtractMean(matr, mean);
        try {
            writeToPGM(mean, 92, 112, "faces/mean.pgm");
            int t = 0;
            for (int[] im : matr) {
                writeToPGM(im, 92, 112, "faces/wtmean" + t + ".pgm");
                t++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        long [][] C = matrixMult(matr, transpose(matr));

        RealVector[] eigenvectors = getEigenvectors(C);


    }

    public static RealVector[] getEigenvectors(long[][] matr) {
        double[] vect;
        RealVector[] vects = new RealVector[matr.length];
        double[][] matrix = new double[matr.length][matr[0].length];
        for (int i = 0; i < matr.length; i++) {
            for (int j = 0; j < matr[0].length; j++) {
                matrix[i][j] = matr[i][j];
            }
        }
        RealMatrix rm = MatrixUtils.createRealMatrix(matrix);

        EigenDecomposition f = new EigenDecomposition(rm);
        double [] vals = new double[matr.length];
        for (int i = 0; i < matr.length; i ++) {
            vects[i] = f.getEigenvector(i);
            vals[i] = f.getRealEigenvalue(i);
        }
        return vects;
    }

    private static int[] calcMean(int[][] arr) {
        int[] avg = new int[arr[0].length];
        for (int i = 0; i < arr[0].length; i++) {
            avg[i] = 0;
            for (int j = 0; j < arr.length; j++) {
                avg[i] += arr[j][i];
            }
            avg[i] /= arr.length;
            System.out.print(arr.length + "");
        }
        return avg;
    }

    private static void subtractMean(int[][] matr, int[] mean) {
        for (int i = 0; i < matr.length; i++) {
            for (int j = 0; j < mean.length; j++) {
                matr[i][j] -= mean[j];
            }
        }
    }

    public static long[][] matrixMult(int[][] a, int[][] b) {
        int m = a.length;
        int n = b.length;
        int l = b[0].length;
        long[][] res = new long[m][m];
        int tmp = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < l; j++) {
                tmp = 0;
                for (int k = 0; k < n; k++) {
                    tmp += a[i][k] * b[k][j];
                }
                res[i][j] = tmp;
            }
        }
        return res;
    }

    public static int[][] transpose(int[][] a) {
        int[][] T = new int[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                T[j][i] = a[i][j];
            }
        }
        return T;
    }

    private static int[] readFace(String face) {
        int[] image = new int[0];
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(face));
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            DataInputStream dis = new DataInputStream(is);
            int x = 0, y = 0;

            int counter = 3;
            boolean bb = false;

            String xx = "";
            String yy = "";
            while (counter != 0) {
                char c = (char) dis.readUnsignedByte();
                if (counter == 2) {
                    if (!bb) {
                        if (c != ' ')
                            xx += c;
                        else
                            bb = !bb;
                    } else {
                        if (c != '\n')
                            yy += c;
                    }
                }
                if (c == '\n')
                    counter--;
            }
            x = Integer.parseInt(xx);
            y = Integer.parseInt(yy);
            System.out.print(x + " " + y + "\n");

            image = new int[x * y];
            for (int i = 0; i < x * y; i++)
                image[i] = dis.readUnsignedByte();

            for (int i : image) {
                System.out.print(i + " ");
            }

            br.close();
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    private static void writeToPGM(int[] image, int x, int y, String dest) throws IOException {
        OutputStream osw = new BufferedOutputStream(new FileOutputStream(dest));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(osw));
        DataOutputStream dos = new DataOutputStream(osw);//
        dos.writeBytes("P5\n92 112\n255\n");
//        for (int i = 0; i < x; i ++) {
//            for (int j = 0; j < y; j ++) {
//                //bw.write(test[i][j]);
//                dos.writeByte(image[x*i + j]);
//            }
//        }
        for (int i : image) {
            dos.writeByte(i);
        }
        dos.close();
        bw.close();
        osw.close();
    }
}
