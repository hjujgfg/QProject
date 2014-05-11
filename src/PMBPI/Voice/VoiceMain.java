package PMBPI.Voice;

import PMBPI.Voice.feature.FeatureVector;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.*;
import java.lang.reflect.Array;
import java.sql.ResultSet;

/**
 * Created by egor on 5/6/14.
 */
public class VoiceMain {

    public static void main(String[] args) {
        String[] trainFiles = {"audio_data/male_audio/M_1298636292/1298636292_1.wav",
                "audio_data/male_audio/M_1298636374/1298636374_1.wav",
                "audio_data/male_audio/M_1298636799/1298636799_1.wav",
                "audio_data/male_audio/M_1298636824/1298636824_1.wav",
                "audio_data/male_audio/M_1298637113/1298637113_1.wav",
                "audio_data/male_audio/M_1298638087/1298638087_1.wav",
                "audio_data/male_audio/M_1298638329/1298638329_1.wav",
                "audio_data/male_audio/M_1298639452/1298639452_1.wav",
                "audio_data/male_audio/M_1298639501/1298639501_1.wav",
                "audio_data/male_audio/M_1298639895/1298639895_1.wav",
                "audio_data/male_audio/M_1298639967/1298639967_1.wav",
                "audio_data/male_audio/M_1298640189/1298640189_1.wav",
                "audio_data/male_audio/M_1298640657/1298640657_1.wav",
                "audio_data/male_audio/M_1298641573/1298641573_1.wav",
                "audio_data/male_audio/M_1298642564/1298642564_1.wav"};
        String[] testingFiles = {"audio_data/male_audio/M_1298636292/1298636292_5.wav",
                "audio_data/male_audio/M_1298636374/1298636374_5.wav",
                "audio_data/male_audio/M_1298636799/1298636799_5.wav",
                "audio_data/male_audio/M_1298636824/1298636824_5.wav",
                "audio_data/male_audio/M_1298637113/1298637113_5.wav",
                "audio_data/male_audio/M_1298638087/1298638087_5.wav",
                "audio_data/male_audio/M_1298638329/1298638329_5.wav",
                "audio_data/male_audio/M_1298639452/1298639452_5.wav",
                "audio_data/male_audio/M_1298639501/1298639501_5.wav",
                "audio_data/male_audio/M_1298639895/1298639895_5.wav",
                "audio_data/male_audio/M_1298639967/1298639967_5.wav",
                "audio_data/male_audio/M_1298640189/1298640189_5.wav",
                "audio_data/male_audio/M_1298640657/1298640657_5.wav",
                "audio_data/male_audio/M_1298641573/1298641573_5.wav",
                "audio_data/male_audio/M_1298642564/1298642564_5.wav"};

        //String testFiles = "audio_data/male_audio/M_1298636824/1298636824_2.wav";
        /*double [][] trainigs = generateCentroids(trainFiles);
        double[] res = identify(testFiles, trainigs);
        for (double d : res) {
            System.out.println(d+" ");
        }*/

        double[][] res = identifySet(trainFiles, testingFiles, true, true, 15);
        for (double[] d : res) {
            for (double dd : d) {
                System.out.print(dd + " ");
            }
            System.out.println();
        }
        double[][] res2 = calcDistByMean(res);
        System.out.println("results _____________");
        int i = 0;
        int errorNumber = 0;
        for (double[] d : res2) {
            System.out.print(i + " ");
            for (double dd : d) {
                System.out.print(dd + " ");
            }
            if (d[2] != i) errorNumber++;
            i++;
            System.out.println();
        }
        System.out.println("Num errors = " + errorNumber);
    }

    static double[][] train(String[] trainFiles) {
        double[][] res = generateCentroids(trainFiles);
        try {
            FileOutputStream fos = new FileOutputStream("serializedTrainingData.ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(res);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

    static double[][] calcDistByMean(double[][] set) {
        double[][] res = new double[set.length][3];
        int i = 0;
        for (double[] vec : set) {
            res[i][0] = calcMean(vec);
            double[] temp = findFurtherest(res[i][0], vec);
            res[i][1] = temp[0];
            res[i][2] = temp[1];
            i++;
        }

        return res;
    }

    static double[] findFurtherest(double mean, double[] vec) {
        double[] res = new double[2];
        double max = 0;
        int maxInd = 0;

        /*for (double d : vec) {
            double dist = Math.abs(d-mean);
            dist *= dist;
            if (dist > max) {
                max = dist;
                maxInd = i;
            }
            i ++;
        }
        res[0] = max;
        res[1] = maxInd;*/
        double[] mins = new double[vec.length];
        for (int i = 0; i < vec.length; i++) {
            mins[i] = mean - vec[i];
        }
        int minindex = findMax(mins);
        res[0] = vec[minindex];
        res[1] = minindex;
        return res;
    }

    static int findMax(double[] vec) {
        double max = 0;
        int ind = 0;
        for (int i = 0; i < vec.length; i++) {
            if (vec[i] > max) {
                max = vec[i];
                ind = i;
            }
        }
        return ind;
    }

    static double calcMean(double[] vec) {
        double d = 0;
        for (double dd : vec) {
            d += dd;
        }
        d /= vec.length;
        return d;
    }

    static double[][] generateMFCCS(String path) {
        WaveData wd = new WaveData();
        File testFile = new File(path);
        float[] amplitude = wd.extractAmplitudeFromFile(testFile);
        PreProcess pp = new PreProcess(amplitude, 16, 8000);
        //float[] tt = new float[]
        FeatureExtract fe = new FeatureExtract(pp.framedSignal, 8000, 16);
        fe.makeMfccFeatureVector();
        FeatureVector fv = fe.getFeatureVector();
        double[][] mfccs = fv.getMfccFeature();
        for (int i = 0; i < mfccs.length; i++) {
            mfccs[i] = removeUnneccessaryMFCCS(mfccs[i]);
        }
       /* for (double[] d : mfccs) {
            for (double dd : d) {
                System.out.print(dd + " ");
            }
            System.out.println();
        }*/
        return mfccs;
    }

    static double[] removeUnneccessaryMFCCS(double[] vec) {
        double[] res = new double[vec.length - 3];
        for (int i = 0; i < vec.length - 3; i++) {
            res[i] = vec[i + 1];
        }
        return res;
    }

    static double[] calcCentroid(double[][] cluster) {
        double[] mean = new double[cluster[0].length];

        for (int i = 0; i < cluster[0].length; i++) {
            double m = 0;
            for (int j = 0; j < cluster.length; j++) {
                m += cluster[j][i];
            }
            mean[i] = m / cluster.length;
        }
        return mean;
    }

    static double[][] generateCentroids(String[] trainingFiles) {
        double[][] centroids = new double[trainingFiles.length][];
        int i = 0;
        for (String s : trainingFiles) {
            centroids[i] = calcCentroid(generateMFCCS(s));
            i++;
        }
        return centroids;
    }

    static double[][] prepareTestFromFile(String s, double[][] centroids, int ind) {
        double[][] test = generateMFCCS(s);
        try {
            FileOutputStream fos = new FileOutputStream("serializedTestinggData" + ind + ".ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(test);
            oos.close();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return test;
    }

    static double[] identify(double[][] testMFCCS, double[][] centroids) {
        double[] res = new double[centroids.length];
        double[][] test = testMFCCS;
        double[] testCentr = calcCentroid(test);
        RealVector testVec = new ArrayRealVector(testCentr);
        for (int i = 0; i < centroids.length; i++) {
            RealVector current = new ArrayRealVector(centroids[i]);
            res[i] = testVec.getDistance(current);
            res[i] = res[i] * res[i];
        }
        return res;
    }

    static double[][] identifySet(String[] training, String[] testing, boolean needsTraining, boolean needgenTests, int numtests) {
        double[][] res = new double[testing.length][training.length];
        double[][] trainingCentroids = null;
        if (needsTraining) {
            trainingCentroids = train(training);
        } else {
            try {
                FileInputStream fis = new FileInputStream("serializedTrainingData.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                trainingCentroids = (double[][]) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!needgenTests) {
            for (int i = 0; i < numtests; i ++) {
                try {
                    FileInputStream fis = new FileInputStream("serializedTestinggData" + i + ".ser");
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    double[][] currentTest = (double[][]) ois.readObject();
                    res[i] = identify(currentTest, trainingCentroids);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            int i = 0;
            for (String s : testing) {
                double[][] tmp = prepareTestFromFile(s, trainingCentroids, i);
                res[i] = identify(tmp, trainingCentroids);
                i++;
            }
        }
        return res;
    }

}
