package net.vivin.ui;

import net.vivin.neural.NeuralNetwork;
import net.vivin.digit.DigitImage;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Paths;


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

    private NeuralNetwork net = null;
    private File chosenImageFile  = null;

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
                }

                if ( chosenImageFile == null )
                {
                    JOptionPane.showMessageDialog(null, "You have not selected image");
                }

                net.setInputs(DigitImage.preprocessImage(chosenImageFile));

                double[] output =  net.getOutput();

                fillTextAreaFromResults(output);
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
        for (int i = 0; i < 9; ++ i) {
            resultsString += i + " - " + String.valueOf(results[i]) + newline;
        }

        resultsTextArea.setText(resultsString);
    }
}


