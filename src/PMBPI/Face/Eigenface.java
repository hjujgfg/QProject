package PMBPI.Face;

import com.sun.xml.internal.bind.marshaller.DataWriter;

import java.io.*;
import java.nio.channels.NonWritableChannelException;
import java.util.ArrayList;

import org.apache.commons.math3.geometry.Vector;
import org.apache.commons.math3.linear.*;

/**
 * Created by egor on 4/14/14.
 */
public class Eigenface {

    private int[][] sampleMatrix;

    public static int[][] readFaces(int[] nums, int[] fs) {
        int[][] matr = new int[nums.length * fs.length][];
        int k = 0;
        for (int i = 0; i < nums.length; i++) {
            for (int j = 0; j < fs.length; j++) {
                matr[k] = readFace("faces/s" + nums[i] + "/" + fs[j] + ".pgm");
                k++;
            }
        }
        return matr;
    }
    public static int[][] readFaces(File[] images) {
        int[][] matr = new int[images.length][];
        int l = 0;
        for (File f : images) {
            matr[l] = readFace(f.getPath());
            l ++;
        }
        return matr;
    }

    public static double[][] localMain(int[][] matr, int [][] I, int width, int height) {
        //int[] f = {1, 2};
        //int[][] matr = readFaces(nums, f);
        int[] mean = calcMean(matr);
        subtractMean(matr, mean);
        /*try {
            writeToPGM(mean, width, height, "faces/mean.pgm");
            int t = 0;
            for (int[] im : matr) {
                writeToPGM(im, width, height, "faces/wtmean" + t + ".pgm");
                t++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        int[][] C = matrixMult(matr, transpose(matr));
        RealMatrix cc = toRealMatrix(C);
        double[] eigenvalues = getEigenvalues(cc);
        RealVector[] eigenvectorsPre = getEigenvectors(cc);
        RealMatrix covarianceMatrix = MatrixUtils.createRealDiagonalMatrix(eigenvalues);
        double[][] eigenvectors = new double[matr.length][];

        RealMatrix rm = toRealMatrix(matr);
        for (int i = 0; i < matr.length; i++) {
            //eigenvectors[i] = multVectByScalar(rm.preMultiply(eigenvectorsPre[i].toArray()), (1 / Math.sqrt(eigenvalues[i])));
            eigenvectors[i] = rm.preMultiply(eigenvectorsPre[i].toArray());
        }

        for (int i = 0; i < eigenvectors.length; i ++) {
            int [] intv = new int[eigenvectors[i].length];
            for (int j = 0; j < intv.length; j++) {
                intv[j] = (int)eigenvectors[i][j];
            }
            try {
                writeToPGM(intv, width, height, "faces/eig"+i+".pgm");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        double[][] weights = new double[rm.getRowDimension()][matr.length];
        for (int i = 0; i < rm.getRowDimension(); i++) {
            for (int j = 0; j < matr.length; j++) {
                weights[j][i] = multVectors(eigenvectors[j], rm.getRow(i));
            }
        }
        weights = transpose(weights);

        //EigenDecomposition ed = new EigenDecomposition(rm);

        cc = new LUDecomposition(cc).getSolver().getInverse();
        //double[][] res = recognize(I, weights, eigenvectors, mean, cc);
        double[][] res = new double[I.length][];
        TrainingDataHolder tdh = new TrainingDataHolder(weights, eigenvectors, mean);
        int o = 0;
        for (int[] a : I) {
            res[o] = recognizeSingle(a, tdh);
            o ++;
        }
        System.out.print("\n Our Result \n");
        for (int i = 0; i < res.length; i++) {
            if (i % 3 == 0)
                System.out.print("\n");
            for (int j = 0; j < res[i].length; j++) {
                System.out.print(res[i][j] + " ");
            }

            System.out.print("\n");
        }
        return res;
    }

    public static TrainingDataHolder train(int[][] matr, int width, int height) {
        int[] mean = calcMean(matr);
        subtractMean(matr, mean);
        /*try {
            writeToPGM(mean, width, height, "faces/mean.pgm");
            int t = 0;
            for (int[] im : matr) {
                writeToPGM(im, width, height, "faces/wtmean" + t + ".pgm");
                t++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        int[][] C = matrixMult(matr, transpose(matr));
        RealMatrix cc = toRealMatrix(C);
        double[] eigenvalues = getEigenvalues(cc);
        RealVector[] eigenvectorsPre = getEigenvectors(cc);
        RealMatrix covarianceMatrix = MatrixUtils.createRealDiagonalMatrix(eigenvalues);
        double[][] eigenvectors = new double[matr.length][];

        RealMatrix rm = toRealMatrix(matr);
        for (int i = 0; i < matr.length; i++) {
            //eigenvectors[i] = multVectByScalar(rm.preMultiply(eigenvectorsPre[i].toArray()), (1 / Math.sqrt(eigenvalues[i])));
            eigenvectors[i] = rm.preMultiply(eigenvectorsPre[i].toArray());
        }

        for (int i = 0; i < eigenvectors.length; i ++) {
            int [] intv = new int[eigenvectors[i].length];
            for (int j = 0; j < intv.length; j++) {
                intv[j] = (int)eigenvectors[i][j];
            }
            try {
                writeToPGM(intv, width, height, "faces/eig"+i+".pgm");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        double[][] weights = new double[rm.getRowDimension()][matr.length];
        for (int i = 0; i < rm.getRowDimension(); i++) {
            for (int j = 0; j < matr.length; j++) {
                weights[j][i] = multVectors(eigenvectors[j], rm.getRow(i));
            }
        }
        weights = transpose(weights);

        //EigenDecomposition ed = new EigenDecomposition(rm);

        cc = new LUDecomposition(cc).getSolver().getInverse();
        TrainingDataHolder res = new TrainingDataHolder(weights, eigenvectors, mean);
        return res;
    }

    public static double[] recognizeSingle(int [] test, TrainingDataHolder holder) {
        int[] testVec = subtractVector(test, holder.mean);
        double [] res = new double[2];
        double[] FI = new double[testVec.length];
        for (int j = 0; j < testVec.length; j++)
            FI[j] = testVec[j];
        double[] w = new double[holder.weights.length];
        for (int j = 0; j < holder.weights.length; j++)
            w[j] = multVectors(holder.eigenvectors[j], FI);
        double[] d = new double[w.length];

        for (int j = 0; j < w.length; j++) {
            RealVector r1 = new ArrayRealVector(w);
            RealVector r2 = new ArrayRealVector(holder.weights[j]);

            d[j] = r1.getDistance(r2);
                /*double [] subVect = new double[w.length];
                for (int k = 0; k < subVect.length; k ++)
                    subVect[k] = w[k] - W[j][k];
                double[] first = covarianceMatr.preMultiply(subVect);
                d[j] = Math.sqrt(Math.sqrt(multVectors(first, subVect)));*/
        }
        System.out.println("Eigenface distances");
        for (double dd : d) {
            System.out.print(dd + " ");
        }
        System.out.println();
        RealVector r = new ArrayRealVector(d);

        res[0] = r.getMinValue();
        res[1] = r.getMinIndex();
        return res;
    }


    public static double[][] recognize(int[][] I, double[][] W, double[][] U, int[] M, RealMatrix covarianceMatr) {
        //int[][] I = readFaces(nums, fs);
        subtractMean(I, M);
        double[][] ret = new double[I.length][2];
        for (int i = 0; i < I.length; i++) {
            double[] FI = new double[I[0].length];
            for (int j = 0; j < I[i].length; j++)
                FI[j] = I[i][j];
            double[] w = new double[W.length];
            for (int j = 0; j < W.length; j++)
                w[j] = multVectors(U[j], FI);
            double[] d = new double[w.length];

            for (int j = 0; j < w.length; j++) {
                RealVector r1 = new ArrayRealVector(w);
                RealVector r2 = new ArrayRealVector(W[j]);

                d[j] = r1.getDistance(r2);
                /*double [] subVect = new double[w.length];
                for (int k = 0; k < subVect.length; k ++)
                    subVect[k] = w[k] - W[j][k];
                double[] first = covarianceMatr.preMultiply(subVect);
                d[j] = Math.sqrt(Math.sqrt(multVectors(first, subVect)));*/
            }
            RealVector r = new ArrayRealVector(d);

            ret[i][0] = r.getMinValue();
            ret[i][1] = r.getMinIndex();
        }
        return ret;
    }
    static double [] multVectByScalar(double[] vec, double scalar) {
        double [] res = new double[vec.length];
        for (int i = 0; i < vec.length; i ++) {
            res [i] = vec[i]*scalar;
        }
        return res;
    }
    static RealVector[] getEigenvectors(RealMatrix rm) {

        RealVector[] vects = new RealVector[rm.getRowDimension()];

        EigenDecomposition f = new EigenDecomposition(rm);
        double[] vals = new double[rm.getRowDimension()];
        for (int i = 0; i < rm.getColumnDimension(); i++) {
            vects[i] = f.getEigenvector(i);
            vals[i] = f.getRealEigenvalue(i);
        }
        return vects;
    }
    static double[] getEigenvalues(RealMatrix rm) {

        EigenDecomposition f = new EigenDecomposition(rm);
        double[] vals = new double[rm.getRowDimension()];
        for (int i = 0; i < rm.getColumnDimension(); i++) {
            vals[i] = f.getRealEigenvalue(i);
        }
        return vals;
    }

    static double multVectors(double[] a, double[] b) {
        double res = 0;
        for (int i = 0; i < a.length; i++) {
            res += a[i] * b[i];
        }
        return res;
    }

    static RealMatrix toRealMatrix(int[][] m) {
        RealMatrix r;
        double d[][] = new double[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                d[i][j] = m[i][j];
            }
        }
        r = MatrixUtils.createRealMatrix(d);
        return r;
    }


    static int[] calcMean(int[][] arr) {
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

    static void subtractMean(int[][] matr, int[] mean) {
        for (int i = 0; i < matr.length; i++) {
            for (int j = 0; j < mean.length; j++) {
                matr[i][j] -= mean[j];
            }
        }
    }

    static int[] subtractVector(int[] v1, int[] v2) {
        int []res = new int[v1.length];
        for (int i = 0; i < v1.length; i ++) {
            res[i] = v1[i] - v2[i];
        }
        return res;
    }


    static int[][] matrixMult(int[][] a, int[][] b) {
        int m = a.length;
        int n = b.length;
        int l = b[0].length;
        int[][] res = new int[m][m];
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

//    private static double[] multiplyByRow()

    static int[][] transpose(int[][] a) {
        int[][] T = new int[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                T[j][i] = a[i][j];
            }
        }
        return T;
    }

    static double[][] transpose(double[][] a) {
        double[][] T = new double[a[0].length][a.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < a[i].length; j++) {
                T[j][i] = a[i][j];
            }
        }
        return T;
    }

    public static int[] readFace(String face) {
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

    public static void writeToPGM(int[] image, int x, int y, String dest) throws IOException {
        OutputStream osw = new BufferedOutputStream(new FileOutputStream(dest));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(osw));
        DataOutputStream dos = new DataOutputStream(osw);//
        dos.writeBytes("P5\n"+x+" "+ y+"\n255\n");
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
