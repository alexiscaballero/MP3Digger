/**
 * Written by Periklis Master_ex Ntanasis <pntanasis@gmail.com>
 * http://masterex.github.com/
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package updater.jupar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import updater.jupar.objects.Modes;
import updater.jupar.parsers.DownloaderXMLParser;

/**
 *
 * @author Periklis Ntanasis
 */
public class Downloader {

    public void download(String filesxml, String destinationdir, Modes mode) throws Exception,
            FileNotFoundException, IOException, InterruptedException {

        DownloaderXMLParser parser = new DownloaderXMLParser();
        Iterator iterator = parser.parse(filesxml, mode).iterator();
        java.net.URL url;

        File dir = new File(destinationdir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        String urlDir = FilenameUtils.getPath(filesxml);
        while (iterator.hasNext()) {
            url = new java.net.URL(urlDir + ((String) iterator.next()));
            wget(url, destinationdir + File.separator + new File(url.getFile()).getName());
        }

    }

    private void wget(java.net.URL url, String destination) throws MalformedURLException, IOException, URISyntaxException {
        //Chequeamos si existe la url a descargar
        if (urlIsAvailable(url)) {
            //Chequeamos si es un archivo o una carpeta
            String extensionArchivoOCarpeta = FilenameUtils.getExtension(url.toString());
            if (extensionArchivoOCarpeta.equals("")) {
                File folder = new File(destination);
                //Creamos la carpeta
                if (!folder.exists()) {
                    folder.mkdirs();
                    System.out.println(folder.getAbsolutePath());
                }
                //Obtenemos los archivos/carpetas dentro de la misma
                Document doc = Jsoup.connect(url.toString()).timeout(10000).get();
                //Descargamos cada archivo/carpeta encontrado 
                for (Element file : doc.getAllElements()) {
                    String link = file.attr("href");
                    //System.out.println(link);
                    //System.out.println(FilenameUtils.getPath(url.toString()));
                    if (!link.equals("")) {
                        String urlNueva = url + "/" + FilenameUtils.getName(file.attr("href"));
                        String urlAux = urlNueva.substring(0, urlNueva.length()-1);
                        if (!urlAux.equals(url.toString())) {
                            try {
                                wget(new java.net.URL(urlNueva), folder.getPath() + "/" + FilenameUtils.getName(file.attr("href")));
                            } catch (Exception ex) {
                                System.out.println("No se pudo obtener: " + urlNueva);
                            }
                        }
                    }
                }
                //Si es un archivo    
            } else {
                //Solo bajamos archivos con las siguientes extensiones
                if (extensionArchivoOCarpeta.equals("jar")
                        || extensionArchivoOCarpeta.equals("xml")
                        || extensionArchivoOCarpeta.equals("txt")
                        || extensionArchivoOCarpeta.equals("png")) {
                    //Obtenemos el archivo y lo descargamos en la ubicacion
                    java.net.URLConnection conn = url.openConnection();
                    java.io.InputStream in = conn.getInputStream();
                    File dstfile = new File(destination);
                    System.out.println(dstfile.getAbsolutePath());
                    OutputStream out = new FileOutputStream(dstfile);
                    byte[] buffer = new byte[512];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        out.write(buffer, 0, length);
                    }
                    in.close();
                    out.close();
                }
            }
        }
    }

    private boolean urlIsAvailable(URL url) {
        boolean estado = false;
        //System.out.println("Chequeando Estado URL: "+url.toString());
        try {
            HttpURLConnection huc = (HttpURLConnection) url.openConnection();
            huc.setRequestMethod("HEAD");  //OR  huc.setRequestMethod ("HEAD");
            huc.connect();
            int code = huc.getResponseCode();
            if (code == 200) {
                estado = true;
            }
        } catch (Exception ex) {
        }
        return estado;
    }
}
