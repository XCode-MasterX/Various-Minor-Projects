import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;

public class Multithreading {
    public static void main(String args[]) {
        final Deque<Integer> XPoints = new ArrayDeque<>();
        final Deque<Integer> YPoints = new ArrayDeque<>();
        final String fileExtension = "jpg";

        Scanner in = new Scanner(System.in);
        System.out.println("Enter threads to use: ");
        int rows = in.nextInt() * 2;
        int cols = rows;

        in.nextLine();
        System.out.println("Kernel Size: " + rows + " X " + cols);
        
        try {
            System.out.println("Enter the file path: ");
            String s = in.nextLine();
            BufferedImage img;
            
            if(!s.startsWith("https:"))
                img = readImageFromFile(s);
            else
                img = ImageIO.read(new URL(s));
            
            if(img == null) {
                System.out.println("Image is null bro.");
                return;
            }

            System.out.println("Image dimensions: " + img.getWidth() + " x " + img.getHeight());
            System.out.println("Output format: " + fileExtension);

            double time = System.currentTimeMillis();
            Grayed grayed[] = new Grayed[rows * cols / 4];
            final int height = img.getHeight(), width = img.getWidth();
            final int cellHeight = height / rows, cellWidth = width / cols;

            for (int i = 0; i < rows * cols; i++) {
                int offsetY = cellHeight * (i / cols);
                int offsetX = cellWidth * (i % cols);
                XPoints.add(offsetX);
                YPoints.add(offsetY);
            }

            long threadsStart = System.nanoTime();
                for(int i = 0; i < grayed.length; i++)
                    grayed[i] = new Grayed(i % 4, img, XPoints, YPoints, cellWidth, cellHeight, i);

                for(Grayed g: grayed)
                    g.start();            

                for (Grayed g : grayed)
                    g.join();

            System.out.println("Threads time: " + (System.nanoTime() - threadsStart) / 1_000_000.0 + " ms");
            
            System.out.printf("Reading and Processing Time Taken: %f\n", (System.currentTimeMillis() - time));
            
            time = System.currentTimeMillis();
            File f = new File("C:/Users/Arpan/Desktop/output." + fileExtension);
            ImageIO.write(img, fileExtension, f);
            time = System.currentTimeMillis() - time;
            System.out.printf("Writing Time Taken: %f\n", time);
            //System.out.println("File written to: " + f.getAbsolutePath());
            
            float pickUp = 0, slack = 0;
            for(Grayed p : grayed) {
                System.out.printf("Thread %d: %f ms \t", p.n, p.timeTaken / 100.0f);
                System.out.printf("Thread %d total runtime: %f \t", p.n, p.totalRunTime);
                System.out.printf("Thread %d polled the queue %d times.\n", p.n, p.pollNum);
                
                int x = 4 - p.pollNum;
                if(x > 0) slack += x;
                else pickUp -= x;
            }
            System.out.println("Slack pick up: " + pickUp);
            System.out.println("Slacked: " + slack);
        }
        catch(IOException | InterruptedException e) {
            System.out.println(e.getMessage());
        }
        finally {
            in.close();
        }
    }

    private static BufferedImage readImageFromFile(String s) throws IOException{
        File f = new File(s);
        ImageInputStream iis = ImageIO.createImageInputStream(f);
                
        if(f.isDirectory()) {
            System.out.println("What you want to do?");
            return null;
        }
        
        return ImageIO.read(iis);
    }
}

class Grayed extends Thread {
    private final int[] pixels;
    private final WritableRaster raster;
    private final Deque<Integer> XPoints;
    private final Deque<Integer> YPoints;
    private final static Object lock = new Object();
    private final int cellWidth, cellHeight;
    private final int channel;
    private int startX, startY;
    
    public double timeTaken;
    public double totalRunTime;
    public int pollNum = 0;
    public int n;

    public Grayed(final int channel, final BufferedImage img, final Deque<Integer> XPoints, final Deque<Integer> YPoints, final int cellWidth, final int cellHeight, final int n) {
        this.channel = channel;
        this.XPoints = XPoints;
        this.YPoints = YPoints;
        this.cellWidth = cellWidth;
        this.cellHeight = cellHeight;
        this.n = n;
        
        pixels = new int[cellHeight * cellWidth * 3];
        raster = img.getRaster();
        totalRunTime = System.currentTimeMillis();
    }
    
    @Override
    public final void run() {
        timeTaken = System.currentTimeMillis();

        while(true) {
            synchronized (Grayed.lock) {                
                if(XPoints.isEmpty() || YPoints.isEmpty()) {
                    break;
                }
                startX = this.XPoints.poll();
                startY = this.YPoints.poll();
                pollNum++;
            }

            readPixels();
            writePixels();
        }
        
        timeTaken = System.currentTimeMillis() - timeTaken;
        totalRunTime = System.currentTimeMillis() - totalRunTime;
    }

    private void readPixels() {
        raster.getPixels(startX, startY, cellWidth, cellHeight, pixels);
        
        for(int i = 0; i < cellHeight; i++) {
            for(int j = 0; j < cellWidth; j++) {
                int index = (i * cellWidth + j) * 3;
                int r = pixels[index], g = pixels[index + 1], b = pixels[index + 2];
                r += g + b;
                r /= 3;
                //r = (int)(Math.sin(j) * r);

                switch(channel) {
                    case 0 -> {
                        pixels[index] = r;
                        pixels[index + 1] = r;
                        pixels[index + 2] = r;
                    }
                    case 1 -> {
                        pixels[index] = r;
                        pixels[index + 1] = 0;
                        pixels[index + 2] = 0;
                    }
                    case 2 -> {
                        pixels[index] = 0;
                        pixels[index + 1] = r;
                        pixels[index + 2] = 0;
                    }
                    case 3 -> {
                        pixels[index] = 0;
                        pixels[index + 1] = 0;
                        pixels[index + 2] = r;
                    }
                }
            }
        }
    }

    private void writePixels() {
        raster.setPixels(startX, startY, cellWidth, cellHeight, pixels);
    }

    public final int[] getPixels() { return pixels; }
}