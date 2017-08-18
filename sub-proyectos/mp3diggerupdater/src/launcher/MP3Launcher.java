package launcher;

import java.io.IOException;
import javax.swing.JOptionPane;
import launcher.splash.Splash;

/**
 *
 * @author Sara
 */
public class MP3Launcher {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Splash.getInstancia().animar();
            ProcessBuilder pb = new ProcessBuilder("java", "-jar", "MP3Digger.jar");
            Process p = pb.start();
            System.exit(0);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
    }
    
}
