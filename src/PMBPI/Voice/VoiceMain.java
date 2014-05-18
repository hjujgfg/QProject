package PMBPI.Voice;

import PMBPI.Voice.feature.FeatureVector;
import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.*;
import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by egor on 5/6/14.
 */
public class VoiceMain {

    public static int samplesPerFrame = 16;
    public static int samplingRate = 16000;
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
        String[] testingFiles = {"audio_data/male_audio/M_1298636292/1298636292_4.wav",
                "audio_data/male_audio/M_1298636374/1298636374_4.wav",
                "audio_data/male_audio/M_1298636799/1298636799_4.wav",
                "audio_data/male_audio/M_1298636824/1298636824_4.wav",
                "audio_data/male_audio/M_1298637113/1298637113_4.wav",
                "audio_data/male_audio/M_1298638087/1298638087_4.wav",
                "audio_data/male_audio/M_1298638329/1298638329_4.wav",
                "audio_data/male_audio/M_1298639452/1298639452_4.wav",
                "audio_data/male_audio/M_1298639501/1298639501_4.wav",
                "audio_data/male_audio/M_1298639895/1298639895_4.wav",
                "audio_data/male_audio/M_1298639967/1298639967_4.wav",
                "audio_data/male_audio/M_1298640189/1298640189_4.wav",
                "audio_data/male_audio/M_1298640657/1298640657_4.wav",
                "audio_data/male_audio/M_1298641573/1298641573_4.wav",
                "audio_data/male_audio/M_1298642564/1298642564_4.wav",
                "audio_data/male_audio/M_1298643578/1298643578_4.wav",
                "audio_data/male_audio/M_1298646185/1298646185_4.wav",
                "audio_data/male_audio/M_1298646491/1298646491_4.wav",
                "audio_data/male_audio/M_1298647737/1298647737_4.wav"};

        /*double[][] res = identifySet(trainFiles, testingFiles, true, true, 15);
        for (double[] d : res) {
            for (double dd : d) {
                System.out.print(dd + " ");
            }
            System.out.println();
        }
        double[][] res2 = calcDistByMeanSet(res);
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
        System.out.println("Num errors = " + errorNumber);*/
        String [][] trainingFiles = new String[18][7];
        for (int i = 0; i < 18; i ++) {
            for (int j = 0; j < 7; j ++) {
                trainingFiles[i][j] = "audio_data/16khz_16bit/"+(i+1)+"/"+j+".wav";
            }
        }
        double[][] trainingStuff = new double[18][];
        for (int i = 0; i < 18; i ++) {
            trainingStuff[i] = trainOnSeveralFiles(trainingFiles[i], samplesPerFrame, samplingRate);
        }
        String [][] testingStrings = new String[24][3];
        for (int i = 0; i < 24; i ++) {
            for (int j = 7; j < 10; j ++) {
                testingStrings[i][j-7] = "audio_data/16khz_16bit/"+(i+1)+"/"+j+".wav";
            }
        }
        double[][] testingCentroids = new double[24][];
        for (int i = 0; i < 24; i ++) {
            testingCentroids[i] = trainOnSeveralFiles(testingStrings[i], samplesPerFrame, samplingRate);
        }
        double [][] res = new double[testingCentroids.length][];
        for (int i = 0; i < 24; i++) {
            res[i] = identify(testingCentroids[i], trainingStuff);
        }
        int i = 0;
        int errorNumber = 0;
        for (double[] d : res) {
            for (double dd : d) {
                System.out.print(dd + " ");
            }
            if (d[2] != i) errorNumber ++;
            i++;
            System.out.println();
        }
        System.out.println("Num errors = " + (errorNumber - 6));
        /*double[][] res2 = calcDistByMeanSet(res);
        System.out.println("results _____________");


        for (double[] d : res2) {
            System.out.print(i + " ");
            for (double dd : d) {
                System.out.print(dd + " ");
            }
            if (d[2] != i) errorNumber++;
            i++;
            System.out.println();
        }
        System.out.println("Num errors = " + errorNumber);*/

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

    static double[][] calcDistByMeanSet(double[][] set) {
        double[][] res = new double[set.length][4];
        int i = 0;
        for (double[] vec : set) {
            res[i][0] = calcMean(vec);
            double[] temp = findFurtherest(res[i][0], vec);
            res[i][1] = temp[0];
            res[i][2] = temp[1];
            res[i][3] = temp[2];
            i++;
        }

        return res;
    }

    static double[] calcDistBymean(double[] distances) {
        double mean = calcMean(distances);
        return findFurtherest(mean, distances);
    }

    static double[] findFurtherest(double mean, double[] vec) {
        double[] res = new double[3];
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
        res[2] = calcVariance(vec);
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

    static double calcVariance(double [] vec) {
        double meanSquare = 0;
        double squareMean = 0;
        for (double d : vec) {
            squareMean += d*d;
            meanSquare += d;
        }
        meanSquare *= meanSquare;
        return (squareMean - meanSquare/vec.length)/(vec.length-1);
    }

    static double[][] generateMFCCS(String path, int samplePerFrame, int samplingRate) {
        WaveData wd = new WaveData();
        File testFile = new File(path);
        float[] amplitude = wd.extractAmplitudeFromFile(testFile);
        PreProcess pp = new PreProcess(amplitude, samplePerFrame, samplingRate);
        //float[] tt = new float[]
        FeatureExtract fe = new FeatureExtract(pp.framedSignal, samplingRate, samplePerFrame);
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

    static double[] trainOnSeveralFiles(String[] paths, int samplesPerFrame, int samplingRate) {
        ArrayList<double[]> totalCluster = new ArrayList<double[]>();
        for (String s : paths) {
            totalCluster.add(calcCentroid(generateMFCCS(s, samplesPerFrame, samplingRate)));
        }
        double [][] res = new double[totalCluster.size()][];
        int i = 0;
        for (double [] d : totalCluster) {
            res[i] = d;
            i ++;
        }
        return calcCentroid(res);
    }

    static double[] removeUnneccessaryMFCCS(double[] vec) {
        double[] res = new double[vec.length - 3];
        for (int i = 1; i < vec.length - 2; i++) {
            res[i-1] = vec[i + 1];
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

    public static double[] getCentroidOfRecording(String path, int samplesPerFrame, int samplingRate) {
        try {
            double[][] r = generateMFCCS(path, samplesPerFrame, samplingRate);
            double [] centroid = calcCentroid(r);
            return centroid;
        } catch (Exception e) {
            return null;
        }
    }

    static double[][] generateCentroids(String[] trainingFiles) {
        double[][] centroids = new double[trainingFiles.length][];
        int i = 0;
        for (String s : trainingFiles) {
            centroids[i] = calcCentroid(generateMFCCS(s, samplesPerFrame, samplingRate));
            i++;
        }
        return centroids;
    }

    static double[][] prepareTestFromFile(String s, double[][] centroids, int ind) {
        double[][] test = generateMFCCS(s, samplesPerFrame, samplingRate);
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
        //testCentr = removeUnneccessaryMFCCS(testCentr);
        RealVector testVec = new ArrayRealVector(testCentr);
        for (int i = 0; i < centroids.length; i++) {
            RealVector current = new ArrayRealVector(centroids[i]);
            res[i] = testVec.getDistance(current);
            res[i] = res[i] * res[i];
        }
        return res;
    }

    public static double[] identify(double[] testCentroid, double[][] trainingCentroids) {
        double[] res = new double[trainingCentroids.length];
        RealVector testVec = new ArrayRealVector(testCentroid);
        for (int i = 0; i < trainingCentroids.length; i++) {
            RealVector current = new ArrayRealVector(trainingCentroids[i]);
            res[i] = testVec.getDistance(current);
            res[i] = res[i] * res[i];
        }
        return calcDistBymean(res);
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
