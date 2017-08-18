package launcher.splash;

import java.awt.*;
import java.awt.SplashScreen;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import updater.UpdateLoader;

public final class Splash {

    //texto que se muestra a medida que se va cargando el screensplah
    final String[] texto = {"Buscando Actualizaciones", "Descargando Actualizaciones",
        "Instalando Actualizaciones", "Lanzando MP3Digger"};
    public int SEARCHING = 0;
    public int DOWNLOADING = 1;
    public int INSTALLING = 2;
    public int LAUNCHING = 3;
    private int estadoActualizacion;
    private final SplashScreen splash;
    private Graphics2D graph;
    private static Splash instancia;
    private BufferedImage image;
    private BufferedImage image2;
    Dimension splashSize;
    int x = 1;
    
    private Splash() {
//        ImageIcon ii = new ImageIcon(this.getClass().getResource("picture.png")); // 30x30px picture
        try {
            image = ImageIO.read(this.getClass().getResource("music.png"));
            image2 = ImageIO.read(this.getClass().getResource("pala.png"));
        } catch (IOException ex) {
            Logger.getLogger(Splash.class.getName()).log(Level.SEVERE, null, ex);
        }
        //image = ii.getImage();
        splash = SplashScreen.getSplashScreen();
        // compute base positions for text and progress bar
        splashSize = splash.getSize();
        graph = splash.createGraphics();
    }
    
    public static Splash getInstancia() {
        if (instancia==null) {
            instancia = new Splash();
        }
        return instancia;
    }

    public void setProgresoActualizacion(int i) {
        this.estadoActualizacion = i;
    }

    public void animar() {
        Thread hiloAnimacion = new Thread() {
            @Override
            public void run() {
                if (splash != null) {
                    while (estadoActualizacion != LAUNCHING) {
                        paint();
                        x=x+2;
                        //drawSplashProgress(texto[estadoActualizacion]);
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                        }
                    }
                    splash.close();
                    this.interrupt();
                }
            }
        };
        hiloAnimacion.start();
        //mientras se ejecuta la animacion ejecutamos el actualizador
        try {
            UpdateLoader.main();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

     public void paint() {
         
            
              // create the transform, note that the transformations happen
              // in reversed order (so check them backwards)
              AffineTransform at = new AffineTransform();
              // 4. translate it to the center of the component
              at.translate(splashSize.getWidth()/2, splashSize.getHeight()/2);
              // 3. do the actual rotation
              at.rotate(Math.toRadians(x));
              // 2. just a scale because this image is big
              //at.scale(0.5, 0.5);
              // 1. translate the object so that you rotate it around the 
              //    center (easier :))
              at.translate(-image.getWidth()/2, -image.getHeight()/2);
              graph = splash.createGraphics();
              graph.drawImage(image, at, null);
              graph.drawImage(image2, null,0,0);
              graph.dispose();
        Toolkit.getDefaultToolkit().sync();
        //graph.dispose();
        splash.update();
    }
    
}
