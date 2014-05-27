package PMBPI;

import PMBPI.Face.Eigenface;
import PMBPI.Face.ImageProcessor;
import PMBPI.Face.PGMReaderAlt;
import PMBPI.Face.TrainingDataHolder;
import PMBPI.Support.DataHolder;
import PMBPI.Support.Person;
import PMBPI.Voice.Microphone;
import PMBPI.Voice.VoiceMain;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.sun.javafx.collections.annotations.ReturnsUnmodifiableCollection;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;
import org.apache.log4j.BasicConfigurator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


/**
 * Created by egor on 5/14/14.
 */
public class MainFrame extends JFrame{
    private JPanel topPanel;
    private JPanel MainPanel;
    private JTabbedPane tabbedPane1;
    private JPanel CameraPlace;
    private JButton selectFaceForIdentificationBtn;
    private JPanel IdentifiedFacePanel;
    private JPanel dataSetPanel;
    private JLabel voiceCaptureLabel;
    private JPanel testSignalPanel;
    private JPanel voiceStatusLabel;
    private JTextField personNameField;
    private JButton addPersonButton;
    private JButton chooseImageFile;
    private JButton captureImageButton;
    private JButton chooseVoiceFileButton;
    private JPanel newPersonImagesPanel;
    private JButton selectVoiceForIdentificationBtn;
    private JPanel newPersonVoicesPanel;
    private JPanel selectedDataPanel;
    private JButton startCustomIdentificationBtn;
    private JButton recordingBtn;
    static final int CAM_DIM_WIDTH = 176;
    static final int CAM_DIM_HEIGHT = 144;
    static final int PREFERRED_SIZE_W = 92;
    static final int PREFERRED_SIZE_H = 112;

    final Webcam webcam;
    DataHolder dataHolder;
    Thread realTimeFaceIdentificationThread;
    boolean realTimeIdentificationThreadIsRunning;
    Thread soundCaptureThread;

    File selectedTestingImage;
    File selectedTestingVoice;
    double [] selectedVoiceCepstras;
    PreviewPanel selectedFaceForIdentification;
    CepstraPreviewPanel selectedVoiceForIdentification;
    ArrayList<double[]> newPersonCepstras;
    int newPersonVoiceIndex;
    int newPersonImageIndex;
    boolean recording;
    Microphone microphone;
    int downCounter;
    Thread countDown;
    public MainFrame() {
        super("PMBPI Project");
        BasicConfigurator.configure();
        setContentPane(MainPanel);
        pack();
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                webcam.close();
                try {
                    realTimeFaceIdentificationThread.interrupt();
                    dataHolder.save();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        newPersonImageIndex = 0;
        newPersonVoiceIndex = 0;
        newPersonVoicesPanel.setLayout(new GridBagLayout());
        recording = false;
        //recordingBtn.setEnabled(false);

        voiceCaptureLabel.requestFocus();
        voiceCaptureLabel.addKeyListener(new KeyListener() {



            @Override
            public void keyTyped(KeyEvent e) {
                if (!recording) {
                    if (e.getKeyChar() == ' ') {
                        voiceCaptureLabel.setText("Идет запись");
                        recording = true;
                        if (tabbedPane1.getSelectedIndex() == 0){
                            microphone = new Microphone("data/tempSample.wav");
                            downCounter = 0;
                            soundCaptureThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    microphone.start();
                                    //while (recording) {}
                                    //microphone.stop();
                                }
                            });
                            //microphone.start();
                            soundCaptureThread.start();

                        } else if (tabbedPane1.getSelectedIndex() == 1) {
                            microphone = new Microphone("data/tmp" + newPersonVoiceIndex + ".wav");
                            soundCaptureThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    microphone.start();
                                }
                            });
                            soundCaptureThread.start();
                            microphone.stop();
                        }
                    }
                } else {
                    if (e.getKeyChar() == ' ') {
                        voiceCaptureLabel.setText("Нажмите пробел для начала записи");
                        recording = false;

                        microphone.stop();

                        //soundCaptureThread.interrupt();
                        /*try {
                            Thread.sleep(500);
                            soundCaptureThread.isAlive();
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }*/

                        if (tabbedPane1.getSelectedIndex() == 0) {
                            //while (soundCaptureThread.isAlive()) {}
                            double[] d;
                            try {
                                d = VoiceMain.getCentroidOfRecording("data/tempSample.wav", 16, 16000);
                                if (d == null) {
                                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
                                    return;
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
                                return;
                            }
                            CepstraPreviewPanel pnl = new CepstraPreviewPanel(d, 200, 100);
                            testSignalPanel.removeAll();
                            testSignalPanel.add(pnl);
                            testSignalPanel.revalidate();
                            testSignalPanel.repaint();
                            realTimeIdentificationThreadIsRunning = false;
                            BufferedImage image = webcam.getImage();
                            ImageProcessor processor = new ImageProcessor();
                            image = processor.rescaleImageBrightness(image, 1.35f, 15);
                            BufferedImage gray = processor.convertToGrayScale(image);
                            int [] face = processor.extractInnerImage(gray, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT);
                            try {
                                Eigenface.writeToPGM(face, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT, "data/identImageTmp.pgm");
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата изображения");
                                return;
                            }
                            Person p = dataHolder.identify("data/identImageTmp.pgm", d);
                            if (p == null) {
                                JOptionPane.showMessageDialog(MainFrame.this, "Человека с такими параметрами нет в выборке");
                                return;
                            }
                            IdentifiedFacePanel.removeAll();
                            try {
                                IdentifiedFacePanel.add(new PreviewPanel(p.getUserPic()));
                                IdentifiedFacePanel.add(new CepstraPreviewPanel(p.getVoiceCentroids()[0], 200, 100));
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                        } else if (tabbedPane1.getSelectedIndex() == 1) {
                            double[] d = null;
                            try {
                                d = VoiceMain.getCentroidOfRecording("data/tmp" + newPersonVoiceIndex + ".wav", DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
                                if (d == null) {
                                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
                                }
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
                            }
                            newPersonCepstras.add(d);
                            CepstraPreviewPanel p = new CepstraPreviewPanel(d, 200, 100);
                            p.indexForNewPerson = newPersonVoiceIndex;
                            newPersonVoicesPanel.add(p);
                            newPersonVoiceIndex ++;
                            newPersonVoicesPanel.revalidate();
                            newPersonVoicesPanel.repaint();
                        }
                        System.gc();
                    }
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        newPersonCepstras = new ArrayList<double[]>();
        dataHolder = DataHolder.restore();
        if (dataHolder == null) {
            dataHolder = new DataHolder();
            trainDataHolder(dataHolder);
        }
        dataHolder.setIMGDimensions(92, 112);
        dataHolder.setSoundCapureParams(16, 16000);
        //mic = new Microphone("data/~tmp.wav");
        webcam = Webcam.getDefault();

        webcam.setViewSize(new Dimension(176, 144));
        CameraPlace.setLayout(new GridBagLayout());
        final WebcamPanel webcamPanel = new WebcamPanel(webcam, new Dimension(176, 144), true);
        MyWebCamPainter painter = new MyWebCamPainter();
        webcamPanel.setPainter(painter);
        CameraPlace.add(webcamPanel);

        setVisible(true);

        IdentifiedFacePanel.setLayout(new GridBagLayout());
        realTimeIdentificationThreadIsRunning = true;
        realTimeFaceIdentificationThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (realTimeIdentificationThreadIsRunning){
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        BufferedImage image = webcam.getImage();
                        ImageProcessor processor = new ImageProcessor();
                        image = processor.rescaleImageBrightness(image, 1.3f, 15);
                        BufferedImage gray = processor.convertToGrayScale(image);
                        Person p = dataHolder.identifyByImage(gray);
                        IdentifiedFacePanel.removeAll();
                        IdentifiedFacePanel.add(new PreviewPanel(p.getUserPic()));
                        IdentifiedFacePanel.add(new CepstraPreviewPanel(p.getVoiceCentroids()[0], 200, 100));
                        IdentifiedFacePanel.revalidate();
                        IdentifiedFacePanel.repaint();
                    }
                } catch (Exception e ) {
                    e.printStackTrace();
                }
            }
        });

        realTimeFaceIdentificationThread.start();

        /*dataSetPanel.setLayout(new GridBagLayout());
        for (Person p : dataHolder.getPeople()) {
            dataSetPanel.add(new PreviewPanel(p.getUserPic()));
        }*/
        testSignalPanel.setLayout(new GridBagLayout());

        newPersonImagesPanel.setLayout(new GridBagLayout());

        chooseImageFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();

                FileFilter filter = new FileNameExtensionFilter(
                        "Face image files (*.pgm), (*.png)", "pgm", "png");
                chooser.setFileFilter(filter);
                chooser.setMultiSelectionEnabled(true);
                File currentdir = new File(System.getProperty("user.dir"));
                chooser.setCurrentDirectory(currentdir);
                int returnVal = chooser.showOpenDialog(MainFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles();
                    for (File f : files) {
                        try {
                            newPersonImagesPanel.add(new PreviewPanel(f));
                            newPersonImagesPanel.revalidate();
                            newPersonImagesPanel.repaint();
                            newPersonImageIndex ++;
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(newPersonImagesPanel, "Ошибка чтения файла: " + f.getPath());
                        }
                    }
                }
            }
        });
        chooseVoiceFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();

                FileFilter filter = new FileNameExtensionFilter(
                        "Voice audio file, (*.wav)", "wav");
                chooser.setFileFilter(filter);
                File currentdir = new File(System.getProperty("user.dir"));
                chooser.setCurrentDirectory(currentdir);
                chooser.setMultiSelectionEnabled(true);
                int returnVal = chooser.showOpenDialog(MainFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles();

                    for (File f : files ) {
                        double[] temp;
                        try {
                            temp = VoiceMain.getCentroidOfRecording(f.getPath(), DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
                            newPersonCepstras.add(temp);
                            CepstraPreviewPanel p = new CepstraPreviewPanel(temp, 200, 100);
                            p.indexForNewPerson = newPersonVoiceIndex;
                            newPersonVoicesPanel.add(p);
                            newPersonVoiceIndex ++;
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения файла: " + f.getPath());
                        }
                    }
                }
            }
        });
        captureImageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    BufferedImage image = webcam.getImage();
                    ImageProcessor processor = new ImageProcessor();
                    image = processor.rescaleImageBrightness(image, 1.35f, 15);
                    image = processor.convertToGrayScale(image);
                    int [] res = processor.extractInnerImage(image, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT);
                    Eigenface.writeToPGM(res, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT, "data/temp" + newPersonImageIndex+".pgm");
                    newPersonImagesPanel.add(new PreviewPanel(new File("data/temp" + newPersonImageIndex+".pgm")));
                    newPersonImageIndex ++;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        selectedDataPanel.setLayout(new GridBagLayout());
        selectFaceForIdentificationBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                JFileChooser chooser = new JFileChooser();

                FileFilter filter = new FileNameExtensionFilter(
                        "Face image files (*.pgm), (*.png)", "pgm", "png");
                chooser.setFileFilter(filter);
                File currentdir = new File(System.getProperty("user.dir"));
                chooser.setCurrentDirectory(currentdir);
                int returnVal = chooser.showOpenDialog(MainFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File f = chooser.getSelectedFile();
                    try {
                        try {
                            selectedDataPanel.remove(selectedFaceForIdentification);
                        } catch (Exception ex) {}

                        selectedFaceForIdentification = new PreviewPanel(f);
                        selectedDataPanel.add(selectedFaceForIdentification);
                        selectedTestingImage = f;
                        selectedDataPanel.revalidate();
                        selectedDataPanel.repaint();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(newPersonImagesPanel, "Ошибка чтения файла: " + f.getPath());
                    }
                }
            }
        });
        selectVoiceForIdentificationBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();

                FileFilter filter = new FileNameExtensionFilter(
                        "Voice audio file, (*.wav)", "wav");
                chooser.setFileFilter(filter);
                File currentdir = new File(System.getProperty("user.dir"));
                chooser.setCurrentDirectory(currentdir);
                chooser.setMultiSelectionEnabled(true);
                int returnVal = chooser.showOpenDialog(MainFrame.this);

                if (returnVal == JFileChooser.APPROVE_OPTION) {

                    try {
                        try {
                            selectedDataPanel.remove(selectedVoiceForIdentification);
                        } catch (Exception ex) {}
                        File[] files = chooser.getSelectedFiles();
                        String [] strings = new String[files.length];
                        int i = 0;
                        for (File f : files) {
                            strings[i] = f.getPath();
                            i ++;
                        }
                        //selectedVoiceCepstras = VoiceMain.getCentroidOfRecording(f.getPath(), DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
                        selectedVoiceCepstras = VoiceMain.trainOnSeveralFiles(strings, DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
                        selectedVoiceForIdentification = new CepstraPreviewPanel(selectedVoiceCepstras, 200, 100);
                        selectedDataPanel.add(selectedVoiceForIdentification);
                        //selectedTestingVoice = f;
                        selectedDataPanel.revalidate();
                        selectedDataPanel.repaint();
                    }catch (Exception ex) {
                        JOptionPane.showMessageDialog(newPersonImagesPanel, "Ошибка чтения файла: ");// + f.getPath());
                    }
                }
            }
        });
        startCustomIdentificationBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedTestingImage == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Выберите корректный файл изображения лица");
                    return;
                }
                if (selectedVoiceCepstras == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Выберите корректную запись голоса");
                    return;
                }
                realTimeIdentificationThreadIsRunning = false;
                Person p = doIdentification();
                if (p == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Такого человека нет в выборке");
                } else {
                    IdentifiedFacePanel.removeAll();
                    try {
                        IdentifiedFacePanel.add(new PreviewPanel(p.getUserPic()));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    IdentifiedFacePanel.add(new CepstraPreviewPanel(p.getVoiceCentroids()[0], 200, 100));
                    IdentifiedFacePanel.revalidate();
                    IdentifiedFacePanel.repaint();
                }
            }
        });

        addPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = personNameField.getText();
                if (name.equals("")) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Введите имя");
                    return;
                }
                if (newPersonCepstras == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Добавьте записи голоса");
                    return;
                }
                if (newPersonCepstras.size() == 0) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Добавьте записи голоса");
                    return;
                }
                if (newPersonImageIndex <= 1) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Добавьте изображения");
                    return;
                }
                String[] newImages = new String[newPersonImageIndex];
                /*for (int i = 0; i < newPersonImageIndex; i ++) {
                    PreviewPanel tmp = (PreviewPanel)newPersonImagesPanel.getComponent(i);
                    tmp.getPathToImage().renameTo(new File("data/" + name + "" +i+".pgm"));
                    newImages[i] = "data/" + name + "" +i+".pgm";
                }*/
                int j = 0;
                for (int i = 0; i < newPersonImagesPanel.getComponentCount(); i ++) {
                    try {
                        PreviewPanel tmp = (PreviewPanel)newPersonImagesPanel.getComponent(i);
                        tmp.getPathToImage().renameTo(new File("data/" + name + "" +j+".pgm"));
                        newImages[j] = "data/" + name + "" +j+".pgm";
                        j ++;
                    } catch(Exception ex) {

                    }
                }
                dataHolder.addPerson(name, newImages, newPersonCepstras);
                newPersonImagesPanel.removeAll();
                newPersonVoicesPanel.revalidate();
                newPersonImagesPanel.repaint();
                newPersonVoicesPanel.removeAll();
                newPersonVoicesPanel.revalidate();
                newPersonImageIndex = 0;
                newPersonVoiceIndex = 0;
                newPersonCepstras.clear();
                personNameField.setText("");
            }
        });
        recordingBtn.addActionListener(new ActionListener() {
            Microphone microphone;
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!recording) {
                    voiceCaptureLabel.setText("Идет запись");
                    recording = true;
                    if (tabbedPane1.getSelectedIndex() == 0){
                        microphone = new Microphone("data/tempSample.wav");
                        downCounter = 0;
                        soundCaptureThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                microphone.start();
                                //while (recording) {}
                                //microphone.stop();
                            }
                        });
                        //microphone.start();
                        soundCaptureThread.start();

                    } else if (tabbedPane1.getSelectedIndex() == 1) {
                        microphone = new Microphone("data/tmp" + newPersonVoiceIndex + ".wav");
                        soundCaptureThread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                microphone.start();
                            }
                        });
                        soundCaptureThread.start();
                    }
                    countDown = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            downCounter = 5;
                            while (downCounter > 0) {
                                downCounter --;
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                voiceCaptureLabel.setText("Цифра " + downCounter);
                            }
                            finishRecording(microphone);
                        }
                    });
                    countDown.start();
                }
            }
        });
    }
    private void finishRecording(Microphone microphone) {
        voiceCaptureLabel.setText("Нажмите пробел для начала записи");
        recording = false;

        microphone.stop();

        //soundCaptureThread.interrupt();
                    /*try {
                        Thread.sleep(500);
                        soundCaptureThread.isAlive();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }*/

        if (tabbedPane1.getSelectedIndex() == 0) {
            //while (soundCaptureThread.isAlive()) {}
            double[] d;
            try {
                d = VoiceMain.getCentroidOfRecording("data/tempSample.wav", 16, 16000);
                if (d == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
                    return;
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
                return;
            }
            CepstraPreviewPanel pnl = new CepstraPreviewPanel(d, 200, 100);
            testSignalPanel.removeAll();
            testSignalPanel.add(pnl);
            testSignalPanel.revalidate();
            testSignalPanel.repaint();
            realTimeIdentificationThreadIsRunning = false;
            BufferedImage image = webcam.getImage();
            ImageProcessor processor = new ImageProcessor();
            image = processor.rescaleImageBrightness(image, 1.35f, 15);
            BufferedImage gray = processor.convertToGrayScale(image);
            int [] face = processor.extractInnerImage(gray, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT);
            try {
                Eigenface.writeToPGM(face, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT, "data/identImageTmp.pgm");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата изображения");
                return;
            }
            Person p = dataHolder.identify("data/identImageTmp.pgm", d);
            if (p == null) {
                JOptionPane.showMessageDialog(MainFrame.this, "Человека с такими параметрами нет в выборке");
                return;
            }
            IdentifiedFacePanel.removeAll();
            try {
                IdentifiedFacePanel.add(new PreviewPanel(p.getUserPic()));
                IdentifiedFacePanel.add(new CepstraPreviewPanel(p.getVoiceCentroids()[0], 200, 100));
            } catch (IOException e1) {
                e1.printStackTrace();
            }

        } else if (tabbedPane1.getSelectedIndex() == 1) {
            double[] d = null;
            try {
                d = VoiceMain.getCentroidOfRecording("data/tmp" + newPersonVoiceIndex + ".wav", DataHolder.VOICE_SAMPLE_PER_FRAME, DataHolder.VOICE_SAMPLING_RATE);
                if (d == null) {
                    JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(MainFrame.this, "Ошибка захвата звука, попробуйте повторно");
            }
            newPersonCepstras.add(d);
            CepstraPreviewPanel p = new CepstraPreviewPanel(d, 200, 100);
            p.indexForNewPerson = newPersonVoiceIndex;
            newPersonVoicesPanel.add(p);
            newPersonVoiceIndex ++;
            newPersonVoicesPanel.revalidate();
            newPersonVoicesPanel.repaint();
        }
        System.gc();
    }
    private Person doIdentification() {
        if (selectedVoiceCepstras == null || selectedTestingImage == null) return null;
        return dataHolder.identify(selectedTestingImage.getPath(), selectedVoiceCepstras);
    }
    private void trainDataHolder(DataHolder dh) {
        dh.setIMGDimensions(92, 112);
        dh.setSoundCapureParams(16, 16000);
        int train = 20;
        String[][] perFaces = new String[train][2];
        for (int i = 0; i < train; i ++) {
            for (int j = 0; j < 2; j ++) {
                perFaces[i][j] = "faces/s" + (i+1) + "/" + (j+1) + ".pgm";
            }
        }
        String[][] perVoices = new String[train][7];
        for (int i = 0; i < train; i ++) {
            for (int j = 0; j < 7; j ++) {
                perVoices[i][j] = "audio_data/16khz_16bit/"+(i+1)+"/"+j+".wav";
            }
        }
        for (int i = 0; i < train; i ++) {
            dh.addPerson("pers#" + (i+1), perFaces[i], perVoices[i]);
        }
        int[][] matr = dh.collectFaceMatrix();
        TrainingDataHolder tdh = Eigenface.train(matr, DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT);
        tdh.save();
    }
    private class MyWebCamPainter implements WebcamPanel.Painter {
        @Override
        public void paintPanel(WebcamPanel webcamPanel, Graphics2D graphics2D) {
            webcamPanel.getDefaultPainter().paintPanel(webcamPanel, graphics2D);
            int x = CAM_DIM_WIDTH - PREFERRED_SIZE_W;
            x /= 2;
            int y = CAM_DIM_HEIGHT - PREFERRED_SIZE_H;
            y /= 2;
            graphics2D.drawRect(x, y, PREFERRED_SIZE_W, PREFERRED_SIZE_H);
        }

        @Override
        public void paintImage(WebcamPanel webcamPanel, BufferedImage image, Graphics2D graphics2D) {
            webcamPanel.getDefaultPainter().paintImage(webcamPanel, image, graphics2D);
            int x = CAM_DIM_WIDTH - PREFERRED_SIZE_W;
            x /= 2;
            int y = CAM_DIM_HEIGHT - PREFERRED_SIZE_H;
            y /= 2;
            graphics2D.drawRect(x, y, PREFERRED_SIZE_W, PREFERRED_SIZE_H);
            graphics2D.drawLine(x, y + PREFERRED_SIZE_H / 2, x + PREFERRED_SIZE_W, y + PREFERRED_SIZE_H / 2);
        }
    }
    public static void main(String[] args) {
        new MainFrame();
    }
    private class PreviewPanel extends JPanel{

        private BufferedImage image;
        File pathToImage;
        public File getPathToImage() {
            return pathToImage;
        }

        public PreviewPanel(File f) throws IOException {
            if (f.getPath().endsWith("pgm")) {
                PGMReaderAlt readerAlt = new PGMReaderAlt(f);
                image = readerAlt.read();
                this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
                this.setBorder(new EmptyBorder(1, 2, 1, 2));
                pathToImage = f;
            } else if (f.getPath().endsWith("png")) {
                ImageProcessor processor = new ImageProcessor();
                BufferedImage tmp = processor.robustReadPNG(f.getPath(), DataHolder.IMG_WIDTH, DataHolder.IMG_HEIGHT);
                image = processor.convertToGrayScale(tmp);
                this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
                pathToImage = f;
            }

            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (tabbedPane1.getSelectedIndex() == 0) {

                        try {
                            selectedDataPanel.remove(selectedFaceForIdentification);
                            selectedFaceForIdentification = null;
                            selectedTestingImage = null;
                            selectedDataPanel.revalidate();
                            selectedDataPanel.repaint();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    } else if (tabbedPane1.getSelectedIndex() == 1) {
                        try {
                            newPersonImagesPanel.remove(PreviewPanel.this);
                            newPersonImagesPanel.revalidate();
                            newPersonImagesPanel.repaint();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
        }

    }

    private class CepstraPreviewPanel extends JPanel{
        private double[] coefs;
        double[] connectedCepstras;
        int width;
        int height;
        public int indexForNewPerson;
        public CepstraPreviewPanel(double [] coefficients, int width, int height) {
            connectedCepstras = coefficients;
            coefs = coefficients.clone();
            this.width = width;
            this.height = height;
            this.setPreferredSize(new Dimension(width, height));
            addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (tabbedPane1.getSelectedIndex() == 0) {
                        selectedDataPanel.remove(selectedVoiceForIdentification);
                        selectedVoiceForIdentification = null;
                        selectedVoiceCepstras = null;
                        selectedTestingVoice = null;
                        selectedDataPanel.revalidate();
                        selectedDataPanel.repaint();
                    } else if (tabbedPane1.getSelectedIndex() == 1) {
                        try {
                            newPersonVoicesPanel.remove(CepstraPreviewPanel.this);
                            newPersonCepstras.remove(connectedCepstras);
                            newPersonVoiceIndex --;
                            newPersonVoicesPanel.revalidate();
                            newPersonVoicesPanel.repaint();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка удаления записи");
                        }
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {

                }

                @Override
                public void mouseReleased(MouseEvent e) {

                }

                @Override
                public void mouseEntered(MouseEvent e) {

                }

                @Override
                public void mouseExited(MouseEvent e) {

                }
            });
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (coefs == null) return;

            RealVector rv = new ArrayRealVector(coefs);
            rv = rv.mapMultiply(10);
            int min = (int) rv.getMinValue();
            min --;
            rv = rv.mapAdd(-min);
            int max = (int)rv.getMaxValue();
            int interval = (width - 5) / coefs.length;
            for (int i = 0; i < coefs.length; i ++) {
                g.drawRect((i+1) * interval, (int) (rv.getEntry(i) * (height - 10) / max), 2, 2);
            }
            g.drawLine(1, height-1, 1, 1);
            g.drawLine(1, height-1, width, height-1);
        }
    }
}
