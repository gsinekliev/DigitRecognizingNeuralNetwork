package net.vivin.ui;

import javafx.scene.paint.Color;
import net.vivin.DigitRecognizingNeuralNetwork;
import net.vivin.neural.NeuralNetwork;
import net.vivin.digit.DigitImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.nio.file.Paths;
import java.util.Random;


/**
 * Created by Georgi Sinekliev on 7/1/2015.
 */
public class MainWindow {
    private JTextArea resultsTextArea;
    private JButton loadFileButton;
    private JButton loadNeuralNetworkButton;
    private JButton classifyButton;
    private JButton blurButton;
    private JPanel mainPanel;
    private JLabel imageLabel;
    private JLabel imagePathLabel;
    private JButton applyNoiseButton;
    private JButton testNetworkButton;

    private NeuralNetwork net = null;
    private File chosenImageFile  = null;
    private BufferedImage chosenImage = null;

    public MainWindow() {
        loadFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println( "We clicked on the load file");

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory( new File(Paths.get(".").toAbsolutePath().normalize().toString()));

                FileFilter imageFilter = new FileNameExtensionFilter(
                        "Image files", ImageIO.getReaderFileSuffixes());
                fileChooser.addChoosableFileFilter(imageFilter);
                if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                    chosenImageFile = fileChooser.getSelectedFile();

                    try {
                        BufferedImage img = ImageIO.read(chosenImageFile);
                        chosenImage = img;
                        ImageIcon icon = new ImageIcon(img);
                        imagePathLabel.setText( chosenImageFile.getAbsolutePath() );
                        imageLabel.setIcon(icon);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        loadNeuralNetworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory( new File(Paths.get(".").toAbsolutePath().normalize().toString()));
                FileFilter netFilter = new FileNameExtensionFilter("Neural nets(.net)", ".net");
                fileChooser.addChoosableFileFilter(netFilter); // to do : this filter doesn't work
                if (fileChooser.showOpenDialog(mainPanel) == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();

                    ObjectInputStream objectInputStream = null;

                    try {
                        objectInputStream = new ObjectInputStream(new FileInputStream(file));
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }

                    try {
                        net = (NeuralNetwork) objectInputStream.readObject();
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        classifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // boring checks
                if ( net == null )
                {
                    JOptionPane.showMessageDialog(null, "You have not selected neural network");
                    return;
                }

                if ( chosenImage == null )
                {
                    JOptionPane.showMessageDialog(null, "You have not selected image");
                    return;
                }

                net.setInputs(DigitImage.preprocessImage(chosenImage));

                double[] output =  net.getOutput();

                fillTextAreaFromResults(output);
            }
        });
        blurButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                float[] matrix = {
                        0.111f, 0.111f, 0.111f,
                        0.111f, 0.111f, 0.111f,
                        0.111f, 0.111f, 0.111f,
                };

                BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, matrix));

                chosenImage = op.filter(chosenImage, null);

                ImageIcon icon = new ImageIcon(chosenImage);
                imageLabel.setIcon(icon);
            }
        });
        applyNoiseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // this should add some random black pixels in image
                int height = chosenImage.getHeight();
                int width = chosenImage.getWidth();

                Random random = new Random();
                for ( int i = 0; i < 20; ++ i )
                {
                    int x = random.nextInt( width );
                    int y = random.nextInt( height );

                    chosenImage.setRGB( x, y, 0 ); // black;
                }

                ImageIcon icon = new ImageIcon(chosenImage);
                imageLabel.setIcon(icon);
            }
        });
        testNetworkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( net == null )
                {
                    JOptionPane.showMessageDialog(null, "You have not selected neural network");
                    return;
                }

                String results = DigitRecognizingNeuralNetwork.testNeuralNetwork( net, 10 );
                resultsTextArea.setText(results);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MainWindow");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void fillTextAreaFromResults(double[] results)
    {
        String newline = System.getProperty("line.separator");
        String resultsString = new String();
        double max = results[0];
        int j = 0; // position of max element
        for (int i = 0; i < 10; ++ i) {
            resultsString += i + " - " + String.valueOf(results[i]) + newline;
            if ( results[i] > max ){
                j = i;
                max = results[i];
            }
        }

        resultsString = "Maximum probability for: " + String.valueOf(j) + newline + resultsString;
        resultsTextArea.setText(resultsString);
    }
}


