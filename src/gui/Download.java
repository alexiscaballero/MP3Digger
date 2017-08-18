/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.JOptionPane;

// This class downloads a file from a URL.
class Download extends Observable implements Runnable {

    // Max size of download buffer.
    private static final int MAX_BUFFER_SIZE = 1024;
    // These are the status names.
    public static final String STATUSES[] = {"Downloading",
        "Paused", "Complete", "Cancelled", "Error"};
    // These are the status codes.
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int COMPLETE = 2;
    public static final int CANCELLED = 3;
    public static final int ERROR = 4;
    private String nombre;
    private URL url; // download URL
    private int size; // size of download in bytes
    private int downloaded; // number of bytes downloaded
    private int status; // current status of download
    private RandomAccessFile file = null;

    // Constructor for Download.
    public Download(String nombre, URL url) {
        this.nombre = nombre;
        this.url = url;
        size = -1;
        downloaded = 0;
        status = DOWNLOADING;

        // Begin the download.
        download();
    }

    // Get this download's URL.
    public String getUrl() {
        return url.toString();
    }

    public String getNombre() {
        return nombre;
    }

    // Get this download's size.
    public int getSize() {
        return size;
    }

    // Get this download's progress.
    public float getProgress() {
        return ((float) downloaded / size) * 100;
    }

    // Get this download's status.
    public int getStatus() {
        return status;
    }

    // Pause this download.
    public void pause() {
        status = PAUSED;
        stateChanged();
    }

    // Resume this download.
    public void resume() {
        status = DOWNLOADING;
        stateChanged();
        download();
    }

    // Cancel this download.
    public void cancel() {
        if (status!=COMPLETE) {
        try {
            this.file.close();
            Path path = Paths.get(rutaDirectorioDescarga() + "/" + nombre + ".temp");
            Files.delete(path);
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        status = CANCELLED;
        stateChanged();}
    }

    // Mark this download as having an error.
    private void error() {
        status = ERROR;
        stateChanged();
    }

    // Start or resume downloading.
    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    // Get file name portion of URL.
    private String getFileName(URL url) {
        String fileName = url.getFile();
        return fileName.substring(fileName.lastIndexOf('/') + 1);
    }

    // Download file.
    @Override
    public void run() {
        InputStream stream = null;
        try {
            // Open connection to URL.
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // Specify what portion of file to download.
            connection.setRequestProperty("Range",
                    "bytes=" + downloaded + "-");
            // Connect to server.
            connection.connect();
            // Make sure response code is in the 200 range.
//            if (connection.getResponseCode() == 410) {
//                url = BuscarGoear.getInstancia().linkDescarga(nombre);
//                HttpURLConnection conection= (HttpURLConnection) url.openConnection();
//                 Specify what portion of file to download.
//                conection.setRequestProperty("Range",
//                        "bytes=" + downloaded + "-");
//                 Connect to server.
//                conection.connect();
//                System.out.println(conection.getResponseCode());
//            }
            if (connection.getResponseCode() / 100 != 2) {
                System.out.println(connection.getResponseCode());
                error();
            }
            // Check for valid content length.
            int contentLength = connection.getContentLength();
            if (contentLength < 1) {
                error();
            }
            /* Set the size for this download if it
             hasn't been already set. */
            if (size == -1) {
                size = contentLength;
                stateChanged();
            }
            // Open file and seek to the end of it.
            file = new RandomAccessFile(rutaDirectorioDescarga() + "/" + nombre + ".temp", "rw");
            file.seek(downloaded);
            stream = connection.getInputStream();
            while (status == DOWNLOADING) {
                /* Size buffer according to how much of the
                 file is left to download. */
                byte buffer[];
                if (size - downloaded > MAX_BUFFER_SIZE) {
                    buffer = new byte[MAX_BUFFER_SIZE];
                } else {
                    buffer = new byte[size - downloaded];
                }
                // Read from server into buffer.
                int read = stream.read(buffer);
                if (read == -1) {
                    break;
                }
                // Write buffer to file.
                if (status != CANCELLED) {
                    try {
                    file.write(buffer, 0, read);
                    } catch (IOException ex) {
                        if (ex.getMessage().equals("Stream Closed")) {}
                        else {throw ex;}
                    }
                };
                downloaded += read;
                stateChanged();
            }
            /* Change status to complete if this point was
             reached because downloading has finished. */
            if (status == DOWNLOADING) {
                status = COMPLETE;
                stateChanged();
                file.close();
                File f1 = new File(rutaDirectorioDescarga() + "/" + nombre + ".temp");
                File f2 = new File(rutaDirectorioDescarga() + "/" + nombre + ".mp3");
                f1.renameTo(f2);
            }
            if (status == CANCELLED) {
                connection.disconnect();
                stream.close();
                file.close();
                status = CANCELLED;
                stateChanged();
            }
        } catch (FileNotFoundException ex){
            JOptionPane.showMessageDialog(null, "Error Al Descargar "+nombre+" Inténtelo De Nuevo");
            error();
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, ex.getMessage());
            error();
        } finally {
            // Close file.
            if (file != null) {
                try {
                    file.close();
                } catch (Exception e) {
                }
            }

            // Close connection to server.
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {
                }
            }
        }
    }

    // Notify observers that this download's status has changed.
    private void stateChanged() {
        setChanged();
        notifyObservers();
    }

    private String rutaDirectorioDescarga() {
        HashMap mapa = new HashMap();
        String ruta = "";
        try {
            if (existeArchivoOpciones()) {
                //use buffering
                InputStream file = new FileInputStream("opciones.ops");
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer);
                try {
                    mapa = (HashMap) input.readObject();
                    ruta = (mapa.get("directorioDescarga").toString());
                } finally {
                    input.close();
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            System.out.println("error downloader10");
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return ruta;
    }

    private boolean existeArchivoOpciones() {
        String sFichero = "opciones.ops";
        File fichero = new File(sFichero);
        if (fichero.exists()) {
            return true;
        } else {
            return false;
        }
    }
}