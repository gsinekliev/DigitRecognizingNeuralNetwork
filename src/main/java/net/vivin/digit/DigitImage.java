package net.vivin.digit;

import net.vivin.neural.activators.SigmoidActivationStrategy;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: vivin
 * Date: 11/11/11
 * Time: 10:05 AM
 */
public class DigitImage {

    private int label;
    private double[] data;
    private byte[] rawData;


    public DigitImage(int label, byte[] data) {
        this.label = label;
        this.rawData = data;
        this.data = DigitImage.preprocessImage(this.rawData);
    }


    public byte[] getOriginalData(){
        return rawData;
    }

    public static double[] preprocessImage(File inputFile){
        try {
            BufferedImage image = ImageIO.read(inputFile);

            return preprocessImage(image);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return new double[0];
    }

    public static double[] preprocessImage( BufferedImage image ){
        byte[] grayLevels = new byte[ image.getWidth() * image.getHeight() ];

        for ( int row = 0; row < image.getHeight(); ++ row ) {
            for ( int column = 0; column < image.getWidth(); ++ column ) {
                int rgb = image.getRGB(column, row);

                Color rgbColor = new Color(rgb);

                int r = rgbColor.getRed();
                int g = rgbColor.getGreen();
                int b = rgbColor.getBlue();

                int grayscaleColorLevel = ( r + g + b ) / 3;
                int transformedColorLevel = 255 - grayscaleColorLevel; // so that 255 can be black and 0 - white
                // ( our neural network is trained that way;
                byte toByte = (byte)transformedColorLevel;

                grayLevels[ row * image.getHeight() + column ] = toByte;
            }
        }

        return preprocessImage(grayLevels);
    }

    /**
     * Preprocesses the image
     * 1) Converting byte data to unsigned grayscale intesity values
     * 2) otsu
     */
    public static double[] preprocessImage( byte[] imageData ){
        double[] numericData = new double[imageData.length];

        for(int i = 0; i < imageData.length; i++) {
            numericData[i] = imageData[i] & 0xFF; //convert to unsigned
        }

        numericData = otsu(numericData);

        return numericData;
    }


    //Uses Otsu's Threshold algorithm to convert from grayscale to black and white
    private static double[] otsu(double[] originalData) {
        int[] histogram = new int[256];

        for(double datum : originalData) {
            histogram[(int) datum]++;
        }

        double sum = 0;
        for(int i = 0; i < histogram.length; i++) {
            sum += i * histogram[i];
        }

        double sumB = 0;
        int wB = 0;
        int wF = 0;

        double maxVariance = 0;
        int threshold = 0;

        int i = 0;
        boolean found = false;

        while(i < histogram.length && !found) {
            wB += histogram[i];

            if(wB != 0) {
                wF = originalData.length - wB;

                if(wF != 0) {
                    sumB += (i * histogram[i]);

                    double mB = sumB / wB;
                    double mF = (sum - sumB) / wF;

                    double varianceBetween = wB * Math.pow((mB - mF), 2);

                    if(varianceBetween > maxVariance) {
                        maxVariance = varianceBetween;
                        threshold = i;
                    }
                }

                else {
                    found = true;
                }
            }

            i++;
        }

/*        System.out.println(label + ": threshold is " + threshold);

        for(i = 0; i < data.length; i++) {
            if(i % 28 == 0) {
                System.out.println("<br />");
            }

            System.out.print("<span style='color:rgb(" + (int) (255 - data[i]) + ", " + (int) (255 - data[i]) + ", " + (int) (255 - data[i]) + ")'>&#9608;</span>");
        } */

        for(i = 0; i < originalData.length; i++) {
            originalData[i] = originalData[i] <= threshold ? 0 : 1;
        }
/*
        if(label == 7 || label == 9) {
            for(i = 0; i < data.length; i++) {
                if(i % 28 == 0) {
                    System.out.println("");
                }

                System.out.print(data[i] == 1 ? "#" : " ");
            }
        }*/

        return originalData;
    }

    public int getLabel() {
        return label;
    }

    public double[] getData() {
        return data;
    }
}
