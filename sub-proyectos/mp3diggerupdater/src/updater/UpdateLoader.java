package updater;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import launcher.splash.Splash;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import updater.jupar.Downloader;
import updater.jupar.Updater;
import updater.jupar.objects.Modes;
import updater.jupar.objects.Release;
import updater.jupar.parsers.ReleaseXMLParser;
import org.xml.sax.SAXException;

public class UpdateLoader {

    /**
     * @param args the command line arguments
     */
    private static final ReleaseXMLParser parser = new ReleaseXMLParser();
    private static String lastVersion;
    private static String lastVersionFiles;

    public UpdateLoader() {}
    
    public static void main() {
        Splash.getInstancia().setProgresoActualizacion(Splash.getInstancia().SEARCHING);
        System.out.println("Chequeando Por Actualizaciones..");
        int answer = -1;
        if (loadLastVersion()) {
            Splash.getInstancia().setProgresoActualizacion(Splash.getInstancia().DOWNLOADING);
            System.out.println("Se Encontro Una Actualizacion..");
            try {
                Release current = parser.parse(lastVersion, Modes.URL);
                if (current.compareTo(loadVersion()) > 0) {
                    answer = 0;
                    lastVersionFiles = lastVersionFiles.concat(current.getpkgver()+"."+current.getPkgrel()+"/files.xml");
                    Downloader dl = new Downloader();
                    dl.download(lastVersionFiles, "tmp", Modes.URL);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            /**
             * Start the updating procedure
             */
            if (answer == 0) {
                Splash.getInstancia().setProgresoActualizacion(Splash.getInstancia().INSTALLING);
                try {
                    Updater update = new Updater();
                    update.update("update.xml", "tmp", Modes.FILE);
                    System.out.println("Actualizacion Exitosa");
                } catch (SAXException | InterruptedException ex) {
                    Logger.getLogger(UpdateLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UpdateLoader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UpdateLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            /**
             * Delete tmp directory
             */
            File tmp = new File("tmp");
            if (tmp.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(tmp);
                } catch (IOException ex) {
                    Logger.getLogger(UpdateLoader.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            Splash.getInstancia().setProgresoActualizacion(Splash.getInstancia().LAUNCHING);
        } else {
            System.out.println("No Se Encontraron Actualizaciones");
        }
    }

    private static Release loadVersion() {
        Release version;
        try {
            version = parser.parse("version.xml", Modes.FILE);
            return version;
        } catch (Exception ex) {
            version = new Release();
            version.setpkgver("0.0");
            version.setPkgrel("1");
        }
        return version;
    }

    private static boolean loadLastVersion() {
        boolean result = false;
        try {
            File file = new File("sources.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(file);
            lastVersion = document.getElementsByTagName("lastversion").item(0).getTextContent();
            lastVersionFiles = document.getElementsByTagName("files").item(0).getTextContent();
            result = true;
        } catch (Exception ex) {
        }
        return result;
    }
}
