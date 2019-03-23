/* USAGE:
 * $ javac ImageHelper.java
 * $ java ImageHelper 0 bool_filter_filename original_img_filename filtered_img_filename
 * ...OR...
 * $ java ImageHelper 1 test_config_filename
 *
 * The first applies a filter/foreground generated from Main.java
 * to an image. The second creates a testcase using a config file
 * that contains information in the same order as TestConfig.
 * Lots of arguments in first option because we don't
 * want to force students to create config file. */
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

/* Nice rgba wrapper for raw pixels */
class Pixel {
    int alpha, r, g, b;
}

/* Configuration to produce a test case */
class TestConfig {
    String imageFilename; // image to generate the test case from
    String seedFilename; // holds background/foreground seed coordinates
        // first line of seedFile should be number of seeds, k
        // then k coordinate pairs, separated by a space, for foreground
        // then k coordinate pairs, separated by a space, for background
    // purpose of blocks is to map large image into smaller one
    // so that we can actually perform segmentation within reasonable time
    int blockHeight; // set so that height/blockHeight <= 30
                     // MUST be factor of image height
    int blockWidth; // set so that width/blockWidth <= 30
                    // MUST be factor of image width
    String testFilename; // output file that will hold testcase input
}

/* Test produced by the TestConfig. Used as input to Main.java */
class TestData {
    int height;
    int width;
    int blockHeight;
    int blockWidth;
    int[][] fReward;
    int[][] bReward;
    int[][] pBtwCols;
    int[][] pBtwRows;
}

/* Primary class */
class ImageHelper {
    int mode;   // 0 ==> apply filter to image
                // 1 ==> generate test case from config

    String file1; // filterfile or testconfigfile
    String file2; // imgfile (or null if mode 1)
    String file3; // filtered_imgfile (or null if mode 1)
    BufferedImage img; // always load this
    int height, width; // of img
    Pixel[][] pixels; // clean version of img
    int[][] filter; // load iff in mode 0

    TestConfig config; // load this if in mode 1
    TestData data; // load this if in mode 1
    int numSeeds;
    Pixel[] bSeedPixels;    // O(1) seed pixels to learn background distribution
                            // load this if in mode 1
    Pixel[] fSeedPixels;    // O(1) seed pixels to learn foreground distribution
                            // load this if in mode 1
    final int MAX_SIMILARITY = 255*4/100;   // max similarity between two pixels
                                            // all rgba values same; normalize and floor

    /* Wrap raw pixel in nice rgba structure; both modes */
    Pixel wrapPixel(int p) {
        Pixel pixel = new Pixel();
        pixel.alpha = (p>>24) & 0xff;
        pixel.r = (p>>16) & 0xff;
        pixel.g = (p>>8) & 0xff;
        pixel.b = p & 0xff;
        return pixel;
    }

    /* Unwrap nice rgba structure into raw pixel. */
    int unwrapPixel(Pixel pixel) {
        return (pixel.alpha<<24) | (pixel.r<<16) | (pixel.g<<8) | pixel.b;
    }

    /* for MODES 0 and 1: load in command line arguments */
    void input(String[] args) throws IOException {
        mode = Integer.parseInt(args[0]);
        file1 = args[1];
        if (mode == 0) {
            file2 = args[2];
            file3 = args[3];
        }
    }

    /* for MODE 1: load in test configuration from file1. */
    void loadConfig() {
        config = new TestConfig();
        try {
            FileReader fin = new FileReader(file1); // must be in mode 0!
            BufferedReader bin = new BufferedReader(fin);
            config.imageFilename = bin.readLine();
            config.seedFilename = bin.readLine();
            config.blockHeight = Integer.parseInt(bin.readLine());
            config.blockWidth = Integer.parseInt(bin.readLine());
            config.testFilename = bin.readLine();
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* for MODE 1: load in background and foreground seeds from
     * seed file specified in test configuration file */
    void loadSeeds() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(config.seedFilename));
            numSeeds = Integer.parseInt(in.readLine());
            bSeedPixels = new Pixel[numSeeds];
            fSeedPixels = new Pixel[numSeeds];
            for (int i = 0; i < numSeeds; i++) {
                String[] xy = in.readLine().split("\\s+");
                int x = Integer.parseInt(xy[0]);
                int y = Integer.parseInt(xy[1]);
                fSeedPixels[i] = wrapPixel(img.getRGB(y,x));
            }
            for (int i = 0; i < numSeeds; i++) {
                String[] xy = in.readLine().split("\\s+");
                int x = Integer.parseInt(xy[0]);
                int y = Integer.parseInt(xy[1]);
                bSeedPixels[i] = wrapPixel(img.getRGB(y,x));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /* for MODE 1: create all data needed for test case */
    void populateTestData() {
        data = new TestData();
        data.height = height;
        data.width = width;
        data.blockHeight = config.blockHeight;
        data.blockWidth = config.blockWidth;
        populateSeparationPenalties();
        populateRewards();
    }

    /* for MODE 1: create separation penalty data needed for test case.
     * Uses similarity between two pixels (penalize more for similar pixels) */
    void populateSeparationPenalties() {
        data.pBtwCols = new int[height][width-1];
        data.pBtwRows = new int[height-1][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width-1; j++) {
                // separation penalty between pixels (i,j), (i,j+1)
                data.pBtwCols[i][j] = similarity(pixels[i][j],pixels[i][j+1])/5;
            }
        }
        for (int i = 0; i < height-1; i++) {
            for (int j = 0; j < width; j++) {
                // separation penalty between pixels (i,j), (i+1,j)
                data.pBtwRows[i][j] = similarity(pixels[i][j],pixels[i+1][j])/5;
            }
        }
    }

    /* for MODE 1: create foreground/background reward data needed for test case.
     * Uses similarity of each pixel to foreground and background seed pixels. */
    void populateRewards() {
        data.fReward = new int[height][width];
        data.bReward = new int[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int sim = 0;
                int newSim;
                for (int k = 0; k < numSeeds; k++) { // should be k=O(1)
                    Pixel px = pixels[i][j];
                    Pixel seed = fSeedPixels[k];
                    newSim = similarity(px,seed);
                    sim = newSim > sim ? newSim : sim;
                }
                data.fReward[i][j] = sim;

                sim = 0;
                for (int k = 0; k < numSeeds; k++) { // should be k=O(1)
                    Pixel px = pixels[i][j];
                    Pixel seed = bSeedPixels[k];
                    newSim = similarity(px,seed);
                    sim = newSim > sim ? newSim : sim;
                }
                data.bReward[i][j] = sim;
            }
        }
    }

    /* for MODE 1: Similarity metric between pixels. */
    int similarity(Pixel p1, Pixel p2) {
        int alpha_diff = Math.abs(p1.alpha-p2.alpha);
        int r_diff = Math.abs(p1.r-p2.r);
        int g_diff = Math.abs(p1.g-p2.g);
        int b_diff = Math.abs(p1.b-p2.b);
        int diff_normalized = (alpha_diff + r_diff + g_diff + b_diff)/100;
        int similarity = MAX_SIMILARITY - diff_normalized;
        return similarity > 0 ? similarity : 0; // sanity check for nonnegative
    }

    /* Represent array as string */
    String arrToString(int[][] arr) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                builder.append(arr[i][j]);
                if (j < arr[i].length-1) {
                    builder.append(" ");
                }
            }
            builder.append("\n");
        }
        return builder.toString();
    }

    /* for MODE 1: Write all test data to file specified by test config file. */
    void writeTestData() {
        try {
            FileWriter fout = new FileWriter(config.testFilename);
            BufferedWriter bout = new BufferedWriter(fout);
            bout.write(data.height + " " + data.width);
            bout.newLine();
            bout.write(data.blockHeight + " " + data.blockWidth);
            bout.newLine();
            bout.write(arrToString(data.fReward));
            bout.write(arrToString(data.bReward));
            bout.write(arrToString(data.pBtwCols));
            bout.write(arrToString(data.pBtwRows));
            bout.flush();
            bout.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* for MODES 0 and 1: load image from specified file */
    void loadImage() throws IOException {
        String imgfile = null;
        if (mode == 0) {
            imgfile = file2;
        } else {
            imgfile = config.imageFilename;
        }
        img = null;
        try {
            File f = new File(imgfile);
            img = ImageIO.read(f);
            height = img.getHeight();
            width = img.getWidth();
            System.out.println(height);
            System.out.println(width);
            pixels = new Pixel[height][width];
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    pixels[i][j] = wrapPixel(img.getRGB(j,i));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* for MODE 0: load foreground filter from file. */
    void loadFilter() throws FileNotFoundException {
        filter = new int[height][width];
        try {
            FileReader fin = new FileReader(file1); // must be in mode 0!
            BufferedReader bin = new BufferedReader(fin);
            String line;
            String[] parts;
            int i = 0;
            while ((line = bin.readLine()) != null) {
                parts = line.split("\\s+"); // length should be == width
                for (int j = 0; j < width; j++) {
                    filter[i][j] = Integer.parseInt(parts[j]);
                }
                i++;
            }
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* for MODE 0: apply filter to image */
    void applyFilter() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                Pixel pixel = pixels[i][j];
                // Change the below behavior to
                // whatever is desired
                if (filter[i][j] == 1) {
                    // Change this filtering function as desired
                    // probably will want to do += or -= on some of these
                    //pixel.alpha = 0;
                    // blend with hot pink
                    pixel.r = (pixel.r/2) + (255/2);
                    pixel.g = (pixel.g/2) + (20/2);
                    pixel.b = (pixel.b/2) + (147/2);
                } else {
                    // blend with black
                    pixel.r = (pixel.r/7);
                    pixel.g = (pixel.g/7);
                    pixel.b = (pixel.b/7);
                }
                img.setRGB(j,i,unwrapPixel(pixel));
            }
        }
    }

    /* for MODE 0: write filtered image to specified file */
    void writeFilteredImage() throws IOException {
        String outputFilename = file3;
        try {
            File f = new File(outputFilename);
            ImageIO.write(img, "png", f);
            System.out.println(img.getWidth());
            System.out.println(img.getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Run everything (mode specified by command line argument)
    public ImageHelper(String[] args) throws IOException {
        input(args);
        if (mode == 0) { // filter
            loadImage();
            loadFilter();
            applyFilter();
            writeFilteredImage();
        } else { // mode == 1; make test
            loadConfig();
            loadImage();
            loadSeeds();
            populateTestData();
            writeTestData();
        }
    }

    public static void main(String[] args) throws IOException {
        new ImageHelper(args);
    }
}
