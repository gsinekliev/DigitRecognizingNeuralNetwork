package net.vivin.service;

import net.vivin.digit.DigitImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: vivin
 * Date: 11/11/11
 * Time: 10:07 AM
 */
public class DigitImageLoadingService {

    private String labelFileName;
    private String imageFileName;

    /** the following constants are defined as per the values described at http://yann.lecun.com/exdb/mnist/ **/

    private static final int MAGIC_OFFSET = 0;
    private static final int OFFSET_SIZE = 4; //in bytes

    private static final int LABEL_MAGIC = 2049;
    private static final int IMAGE_MAGIC = 2051;

    private static final int NUMBER_ITEMS_OFFSET = 4;
    private static final int ITEMS_SIZE          = 4;

    private static final int NUMBER_OF_ROWS_OFFSET = 8;
    private static final int ROWS_SIZE             = 4;
    public static final int ROWS                   = 28;

    private static final int NUMBER_OF_COLUMNS_OFFSET = 12;
    private static final int COLUMNS_SIZE = 4;
    public static final int COLUMNS = 28;

    private static final int IMAGE_OFFSET = 16;
    private static final int IMAGE_SIZE = ROWS * COLUMNS;


    public DigitImageLoadingService(String labelFileName, String imageFileName) {
        this.labelFileName = labelFileName;
        this.imageFileName = imageFileName;
    }

    public List<DigitImage> loadDigitImages() throws IOException {
        List<DigitImage> images = new ArrayList<DigitImage>();

        ByteArrayOutputStream labelBuffer = new ByteArrayOutputStream();
        ByteArrayOutputStream imageBuffer = new ByteArrayOutputStream();

        InputStream labelInputStream = this.getClass().getResourceAsStream(labelFileName);
        InputStream imageInputStream = this.getClass().getResourceAsStream(imageFileName);

        int read;
        byte[] buffer = new byte[16384];

        while((read = labelInputStream.read(buffer, 0, buffer.length)) != -1) {
           labelBuffer.write(buffer, 0, read);
        }

        labelBuffer.flush();

        while((read = imageInputStream.read(buffer, 0, buffer.length)) != -1) {
            imageBuffer.write(buffer, 0, read);
        }

        imageBuffer.flush();

        byte[] labelBytes = labelBuffer.toByteArray();
        byte[] imageBytes = imageBuffer.toByteArray();

        byte[] labelMagic = Arrays.copyOfRange(labelBytes, 0, OFFSET_SIZE);
        byte[] imageMagic = Arrays.copyOfRange(imageBytes, 0, OFFSET_SIZE);

        if(ByteBuffer.wrap(labelMagic).getInt() != LABEL_MAGIC)  {
            throw new IOException("Bad magic number in label file!");
        }

        if(ByteBuffer.wrap(imageMagic).getInt() != IMAGE_MAGIC) {
            throw new IOException("Bad magic number in image file!");
        }

        int numberOfLabels = ByteBuffer.wrap(Arrays.copyOfRange(labelBytes, NUMBER_ITEMS_OFFSET, NUMBER_ITEMS_OFFSET + ITEMS_SIZE)).getInt();
        int numberOfImages = ByteBuffer.wrap(Arrays.copyOfRange(imageBytes, NUMBER_ITEMS_OFFSET, NUMBER_ITEMS_OFFSET + ITEMS_SIZE)).getInt();

        if(numberOfImages != numberOfLabels) {
            throw new IOException("The number of labels and images do not match!");
        }

        int numRows = ByteBuffer.wrap(Arrays.copyOfRange(imageBytes, NUMBER_OF_ROWS_OFFSET, NUMBER_OF_ROWS_OFFSET + ROWS_SIZE)).getInt();
        int numCols = ByteBuffer.wrap(Arrays.copyOfRange(imageBytes, NUMBER_OF_COLUMNS_OFFSET, NUMBER_OF_COLUMNS_OFFSET + COLUMNS_SIZE)).getInt();

        if(numRows != ROWS && numRows != COLUMNS) {
            throw new IOException("Bad image. Rows and columns do not equal " + ROWS + "x" + COLUMNS);
        }

        for(int i = 0; i < numberOfLabels; i++) {
            int label = labelBytes[OFFSET_SIZE + ITEMS_SIZE + i];
            byte[] imageData = Arrays.copyOfRange(imageBytes, (i * IMAGE_SIZE) + IMAGE_OFFSET, (i * IMAGE_SIZE) + IMAGE_OFFSET + IMAGE_SIZE);

            images.add(new DigitImage(label, imageData));
        }

        return images;
    }

    public static void writeImagesToFiles( String outputFileDirectory )
    {
        DigitImageLoadingService trainingService = new DigitImageLoadingService("/train/train-labels-idx1-ubyte.dat", "/train/train-images-idx3-ubyte.dat");

        List<DigitImage> images = null;
        try {
            images = trainingService.loadDigitImages();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<Integer, List<DigitImage> > labelToDigitImageListMap = new HashMap<Integer, List<DigitImage>>();

        for (DigitImage digitImage: images) {

            if (labelToDigitImageListMap.get(digitImage.getLabel()) == null) {
                labelToDigitImageListMap.put(digitImage.getLabel(), new ArrayList<DigitImage>());
            }

            labelToDigitImageListMap.get(digitImage.getLabel()).add(digitImage);
        }

        for (Map.Entry<Integer, List<DigitImage>> entry : labelToDigitImageListMap.entrySet()) {
            List<DigitImage> values = entry.getValue();

            for (int i = 0; i < 2; /*values.size();*/ ++ i ) {
                DigitImage image = values.get(i);

                String path = String.valueOf( outputFileDirectory + "/" + image.getLabel()) + "_" + String.valueOf(i) + ".png";
                byte[] imageByteData = image.getOriginalData();
                try {
                    FileOutputStream baos = new FileOutputStream(path);
                    BufferedImage imageBuffer = new BufferedImage( ROWS, COLUMNS, BufferedImage.TYPE_BYTE_GRAY );

                    for ( int r = 0; r < ROWS; ++ r )
                    {
                        for ( int c = 0; c < COLUMNS; ++ c )
                        {
                            int pixelIntensityUnsigned = imageByteData[ r * ROWS + c ] & 0xFF; // from 0 to 255
                                                                                               // 0   means white
                                                                                               // 255 means black
                            int rgbCompliantIntensity = 255 - pixelIntensityUnsigned;
                            Color myWhite = new Color(rgbCompliantIntensity, rgbCompliantIntensity, rgbCompliantIntensity); // Color white
                            int rgb = myWhite.getRGB();

                            imageBuffer.setRGB( c, r, rgb );
                        }
                    }

                    ImageIO.write(imageBuffer, "png", baos);
                    baos.flush();
                    baos.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
