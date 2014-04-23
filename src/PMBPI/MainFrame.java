package PMBPI;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDevice;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.ds.buildin.WebcamDefaultDriver;
import org.apache.log4j.BasicConfigurator;

/**
 * Created by Егор on 23.04.2014.
 */
public class MainFrame extends JFrame{
    private JPanel rootPanel;
    private JButton openButton;
    private JPanel ImagePanel;
    private JLabel imageLabel;

    public MainFrame() {
        super("PMBPI Project");
        setContentPane(rootPanel);
        pack();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BasicConfigurator.configure();
        final Dimension size = WebcamResolution.QVGA.getSize();

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showConfirmDialog(MainFrame.this, "Opened");
                Webcam webcam = Webcam.getDefault();
                WebcamPanel panel = new WebcamPanel(webcam, size, true);
                ImagePanel.add(panel);
                //panel.start();
                //webcam.setViewSize(new Dimension(1024,768));

                //webcam.open(false);


                // get image
                BufferedImage image = webcam.getImage();

                // save image to PNG file
                try {
                    ImageIO.write(image, "PNG", new File("test.png"));
                    ImageIcon icon = new ImageIcon("test.png");
                    imageLabel.setIcon(icon);
                    webcam.close();

                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        });
        setVisible(true);

    }
    public static void main(String[] args){
        MainFrame m = new MainFrame();
    }
}
