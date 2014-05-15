package PMBPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import PMBPI.Face.Eigenface;
import PMBPI.Face.PGMReaderAlt;
import com.github.sarxos.webcam.WebcamResolution;
import org.apache.log4j.BasicConfigurator;

/**
 * Created by Егор on 23.04.2014.
 */
public class FaceTestMainFrame extends JFrame{
    private JPanel rootPanel;
    private JButton openButton;
    private JPanel ImagePanel;
    private JPanel PrevMainPanel;
    private JButton addTrainigBtn;
    private JButton identifyBtn;
    private JButton addTestBtn;
    private JPanel testPanel;
    private JLabel trainingName;
    private JLabel testingName;
    private JLabel identName;
    private JButton clearBtn;
    private JTextField thresholdField;
    private JTextPane textPane1;
    private JTable ImageTable;
    WebCamFrame webcam;
    ArrayList<PreviewPanel> training;
    ArrayList<PreviewPanel> testing;
    int threshold = 80;
    public FaceTestMainFrame() {
        super("PMBPI Project");
        setContentPane(rootPanel);
        pack();

        training = new ArrayList<PreviewPanel>();
        testing = new ArrayList<PreviewPanel>();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        BasicConfigurator.configure();
        final Dimension size = WebcamResolution.QVGA.getSize();

        //panel.start();
        //PrevMainPanel.setLayout(new BoxLayout(PrevMainPanel, BoxLayout.PAGE_AXIS));
        //PrevMainPanel.add(Box.createRigidArea(new Dimension(2, 0)));
        PrevMainPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        //PrevMainPanel.add(ImageTable);
        PrevMainPanel.setLayout(new GridBagLayout());
        testPanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        testPanel.setLayout(new GridBagLayout());
        ImagePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ImagePanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.NONE;
        c.weightx = 1;

        //PrevMainPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        setVisible(true);

        //WebCamFrame webCamFrame = new WebCamFrame();

        addTrainigBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser("faces/");
                int ret = fileopen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    PreviewPanel p = new PreviewPanel(file);
                    training.add(p);
                    PrevMainPanel.add(p);
                    PrevMainPanel.revalidate();
                    PrevMainPanel.repaint();
                }
            }
        });
        addTestBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileopen = new JFileChooser("faces/");
                int ret = fileopen.showDialog(null, "Открыть файл");
                if (ret == JFileChooser.APPROVE_OPTION) {
                    File file = fileopen.getSelectedFile();
                    PreviewPanel p = new PreviewPanel(file);
                    testing.add(p);
                    testPanel.add(p);
                    testPanel.revalidate();
                    testPanel.repaint();
                }
            }
        });
        identifyBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (thresholdField.getText() != null || thresholdField.getText() != "" ) {
                    threshold = Integer.parseInt(thresholdField.getText());
                }
                File[] testFiles = new File[testing.size()];
                int  i = 0;
                for (PreviewPanel p : testing) {
                    testFiles[i] = p.pathToImage;
                    i++;
                }
                File[] trainingFiles = new File[training.size()];
                i = 0;
                for (PreviewPanel p : training) {
                    trainingFiles[i] = p.pathToImage;
                    i ++;
                }
                int[][] trainingMatrix = Eigenface.readFaces(trainingFiles);
                int[][] testingMatrix = Eigenface.readFaces(testFiles);
                double [][] result = Eigenface.localMain(trainingMatrix, testingMatrix, 92, 112);
                for (i = 0; i < result.length; i ++) {
                    GridBagConstraints cons = new GridBagConstraints();
                    cons.gridx = i;
                    cons.gridy = i;
                    ImagePanel.add(testing.get(i), cons);
                    ImagePanel.add(training.get((int)result[i][1]));

                }
                i = 0;
                for (PreviewPanel p : training) {
                    GridBagConstraints cons = new GridBagConstraints();
                    cons.gridx = 0;
                    cons.gridy = i;
                    ImagePanel.add(p, cons);
                    int k = 1;
                    for (int j = 0; j < result.length; j ++) {
                        cons.gridx = k;
                        cons.gridy = i;
                        if ((int)result[j][1] == i) {
                            if (result[j][0] > threshold * 100000)
                            {
                                testing.get(j).setBorder(BorderFactory.createLineBorder(Color.RED));
                            }
                            ImagePanel.add(testing.get(j), cons);

                            k ++;
                        }
                    }
                    i++;
                }
                ImagePanel.revalidate();
                ImagePanel.repaint();
                /*StringBuilder sb = new StringBuilder();
                for (i = 0; i < result.length; i++) {
                    if (i % 4 == 0)
                        sb.append("\n");
                    for (int j = 0; j < result[i].length; j++) {
                        sb.append(result[i][j] + " ");
                    }

                    sb.append("\n");
                }*/
                //textPane1.setText(sb.toString());
                //ImageTable.invalidate();
            }
        });
        clearBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                training.clear();
                testing.clear();
                PrevMainPanel.removeAll();
                testPanel.removeAll();
                ImagePanel.removeAll();
            }
        });
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

    @Override
    public void repaint() {
        super.repaint();
        for (PreviewPanel p : training) {
            PrevMainPanel.add(p);
        }
        for (PreviewPanel p : testing) {
            testPanel.add(p);
        }
    }



    public static void main(String[] args){
        FaceTestMainFrame m = new FaceTestMainFrame();
    }

    private class PreviewPanel extends JPanel{

        private BufferedImage image;
        File pathToImage;
        public File getPathToImage() {
            return pathToImage;
        }

        public PreviewPanel(int i) {
            try {
                pathToImage = new File("faces/s1/"+ i +".pgm");
                PGMReaderAlt readerAlt = new PGMReaderAlt(pathToImage);
                image = readerAlt.read();
                this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            } catch (IOException ex) {
                ex.printStackTrace();
                // handle exception...
            }
        }
        public PreviewPanel(File f) {
            try {
                PGMReaderAlt readerAlt = new PGMReaderAlt(f);
                image = readerAlt.read();
                this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
                pathToImage = f;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.drawImage(image, 0, 0, null); // see javadoc for more info on the parameters
        }

    }

}
