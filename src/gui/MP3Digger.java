package gui;

import buscador.plugins.CancionDTO;
import buscador.plugins.CargadorPlugins;
import buscador.plugins.IPluginSearch;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.basic.BasicSliderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerEvent;
import javazoom.jlgui.basicplayer.BasicPlayerException;
import javazoom.jlgui.basicplayer.BasicPlayerListener;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.jaudiotagger.tag.id3.ID3v1Tag;
import org.jaudiotagger.tag.id3.ID3v24Frames;
import org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer;

public class MP3Digger extends javax.swing.JFrame {

    private final Downloader downloader;
    private final List<Thread> listaHilos = new ArrayList<>();
    private final List<Timer> listaTimers = new ArrayList<>();
    private String busquedaActual;
    private TableRowSorter sorter;
    private final TableRowSorter sorter2;
    private int selectedLaf;
    private HashMap opciones = new HashMap();
    private final MarqueePanel marquesinaCancion = new MarqueePanel();
    private final MarqueePanel marquesinaCancion2 = new MarqueePanel();
    private static MP3Digger mp3Digger;
    private HashMap mapaTemas = new HashMap();
    private final HashMap mapaResultados = new HashMap();
    private final BasicPlayer basicPlayer = new BasicPlayer();
    private final BasicController controlador = (BasicController) basicPlayer;
    private final BasicPlayer basicPlayer2 = new BasicPlayer();
    private final BasicController controlador2 = (BasicController) basicPlayer2;
    private int filaCancionActual = -1;
    private BufferedImage imagen = null;
    private Thread hiloTiempo;
    private final List<CancionDTO> listaResultados = new ArrayList<>();
    private int cantidadHilosBuscando = 0;
    private boolean valorCambiado = false;
    private Integer nuevoValor = 0;

    private MP3Digger() {
        this.cargarLogo();
        this.directorioDescarga = new JTextField();
        this.downloader = new Downloader();
        initComponents();
        this.leerOpciones();
        this.setIcon();
        this.cargarIconosPestañas();
        this.texto.requestFocusInWindow();
        this.loading.setVisible(false);
        this.limpiarTabla();
        this.sorter = new TableRowSorter(this.jTable1.getModel());
        this.sorter2 = new TableRowSorter(this.jTable3.getModel());
        this.jTable1.setRowSorter(this.sorter);
        this.jTable3.setRowSorter(this.sorter2);
        this.agregarfiltroListener();
        this.agregarfiltro2Listener();
        this.setLocationRelativeTo(null);
        this.aparienciaListener();
        this.opciones = leerOpciones();
        this.revalidate();
        this.repaint();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.recolector();
        this.agregarReproductorListener();
        this.mp3Digger = this;
        this.deshabilitarLog();
    }

    public MP3Digger getInstancia() {
        if (mp3Digger == null) {
            mp3Digger = new MP3Digger();
        }
        return mp3Digger;
    }

    public void agregarResultados(final String busqueda, final List<CancionDTO> lista) {
        SwingWorker hilo = new SwingWorker<Void, Void>() {
            @Override
            public Void doInBackground() {
                try {
                    for (CancionDTO elementoLista : lista) {
                        final String[] vector = {elementoLista.getNombre(), elementoLista.getDuracion(), elementoLista.getCalidad(), elementoLista.getId()};
                        final DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
                        if (busqueda.equals(busquedaActual)) {
                            try {
                                modelo.addRow(vector);
                                int numeroR = Integer.parseInt(numeroResultados.getText());
                                numeroResultados.setText((numeroR + 1) + "");
                            } catch (Exception ex) {
                                JOptionPane.showMessageDialog(rootPane, ex.getMessage());
                            }
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(rootPane, ex.getMessage());
                }
                return null;
            }
        };
        hilo.execute();
    }

    private void agregarReproductorListener() {
        this.basicPlayer.addBasicPlayerListener(new BasicPlayerListener() {
            private long dt = 0;
            private long tv = 0;
            private long tn = 0;
            private double bytesLength;
            private int aux;

            @Override
            public void opened(Object o, Map map) {
                if (map.containsKey("audio.length.bytes")) {
                    bytesLength = Double.parseDouble(map.get("audio.length.bytes").toString());
                }
                aux = 1;
            }

            @Override
            public void progress(final int bytesread, final long l, byte[] bytes, final Map map) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        tn = System.currentTimeMillis();
                        dt = tn - tv;
                        if (dt > 420 || (bytesLength - bytesread < 1000)) {
                            float progressUpdate = (float) (bytesread * 1.0f / bytesLength * 1.0f);
                            int progressNow = (int) (bytesLength * progressUpdate);
                            // Descomentando la siguiente línea se mosrtaría el progreso
                            rellenar(progressNow, (int) bytesLength, new Long(map.get("mp3.position.microseconds").toString()));
                            tv = tn;
                        }
                        if (bytesLength == bytesread && aux == 1) {
                            aux--;
                            botonSiguiente.doClick();
                        }
                    }
                });
            }

            @Override
            public void stateUpdated(BasicPlayerEvent bpe) {
            }

            @Override
            public void setController(BasicController bc) {
            }
        });
    }

    private void cargarLogo() {
        try {
            this.imagen = ImageIO.read(this.getClass().getResource("iconos/fondoOscuro.png"));
        } catch (IOException ex) {
            Logger.getLogger(MP3Digger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private HashMap leerOpciones() {
        HashMap mapa = new HashMap();
        try {
            if (existeArchivoOpciones()) {
                //use buffering
                InputStream file = new FileInputStream("opciones.ops");
                InputStream buffer = new BufferedInputStream(file);
                ObjectInput input = new ObjectInputStream(buffer);
                try {
                    mapa = (HashMap) input.readObject();
                    if (mapa.containsKey("directorioDescarga")) {
                        this.directorioDescarga.setText(mapa.get("directorioDescarga").toString());
                    } else {
                        CodeSource codeSource = MP3Digger.class.getProtectionDomain().getCodeSource();
                        File jarFile = new File(codeSource.getLocation().toURI().getPath());
                        String jarDir = jarFile.getParentFile().getPath();
                        this.directorioDescarga.setText(jarDir);
                    }
                    if (mapa.containsKey("theme")) {
                        switch (mapa.get("theme").toString()) {
                            case "com.jtattoo.plaf.texture.TextureLookAndFeel":
                                com.jtattoo.plaf.texture.TextureLookAndFeel.setCurrentTheme(cargarPropiedadesTexture());
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                break;
                            case "com.jtattoo.plaf.hifi.HiFiLookAndFeel":
                                iconosBiblioteca("blanco");
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                break;
                            case "com.jtattoo.plaf.noire.NoireLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceGraphiteRedLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                            case "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel":
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                                iconosBiblioteca("blanco");
                                break;
                        }
                        if (UIManager.getLookAndFeel().getClass().getName().startsWith("com.jtattoo")) {
                            this.jTable3.setDefaultRenderer(String.class, new CustomTableCellRenderer());
                            this.jTable1.setDefaultRenderer(String.class, new CustomTableCellRenderer());
                        }
                        UIManager.setLookAndFeel(mapa.get("theme").toString());
                        SwingUtilities.updateComponentTreeUI(this);
                    } else {
                        UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteRedLookAndFeel");
                        iconosBiblioteca("blanco");
                        this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                        SwingUtilities.updateComponentTreeUI(this);
                        opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceGraphiteRedLookAndFeel");
                        this.escribirOpciones();
                    }
                } catch (Exception ex) {
                    //Si ocurre un error al leer archivo opciones se ignorara
                } finally {
                    input.close();
                }
            } else {
                File directory = new File(".");
                this.opciones.put("directorioDescarga", directory.getAbsolutePath());
                this.directorioDescarga.setText(directory.getAbsolutePath());
                this.escribirOpciones();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
        return mapa;
    }

    private void escribirOpciones() {
        try {
            //use buffering
            OutputStream file = new FileOutputStream("opciones.ops");
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput outPut = new ObjectOutputStream(buffer);
            try {
                outPut.writeObject(this.opciones);
            } finally {
                outPut.close();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex.getMessage());
        }
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

    private void limpiarTabla() {
        this.listaResultados.clear();
        jTable1.setRowSorter(null);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
                modelo.setRowCount(0);
                jTable1.getColumnModel().getColumn(1).setMaxWidth(80);
                jTable1.getColumnModel().getColumn(1).setMinWidth(80);
                jTable1.getColumnModel().getColumn(2).setMaxWidth(100);
                jTable1.getColumnModel().getColumn(2).setMinWidth(100);
                jTable1.getColumnModel().getColumn(3).setPreferredWidth(80);
                jTable1.getColumnModel().getColumn(3).setMinWidth(80);
                if (UIManager.getLookAndFeel().getClass().getName().startsWith("com.jtattoo")) {
                    CustomTableCellRenderer tcr = new CustomTableCellRenderer();
                    jTable1.setDefaultRenderer(String.class, tcr);
                    tcr.setHorizontalAlignment(SwingConstants.CENTER);
                    jTable1.getColumnModel().getColumn(2).setCellRenderer(tcr);
                    jTable1.getColumnModel().getColumn(1).setCellRenderer(tcr);
                    sorter = new TableRowSorter(jTable1.getModel());
                    jTable1.setRowSorter(sorter);
                } else {
                    SubstanceDefaultTableCellRenderer tcr = new SubstanceDefaultTableCellRenderer();
                    jTable1.setDefaultRenderer(String.class, tcr);
                    tcr.setHorizontalAlignment(SwingConstants.CENTER);
                    jTable1.getColumnModel().getColumn(2).setCellRenderer(tcr);
                    jTable1.getColumnModel().getColumn(1).setCellRenderer(tcr);
                    sorter = new TableRowSorter(jTable1.getModel());
                    jTable1.setRowSorter(sorter);
                }

            }
        });

    }

    public void limpiarTablaBiblioteca() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ((DefaultTableModel) jTable3.getModel()).setRowCount(0);
            }
        });
    }

    public List<File> getFiles(File f) {
        ArrayList<File> listaArchivos = new ArrayList();
        if (f.isFile()) {
            listaArchivos.add(f);
        } else {
            File files[] = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                getFiles(files[i]);
            }
        }
        return listaArchivos;
    }

    public void actualizarBiblioteca() {
        Thread hiloBiblioteca = new Thread() {
            @Override
            public void run() {
                mapaTemas.clear();
                mapaTemas = new HashMap();
                limpiarTablaBiblioteca();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jLabel12.setText(0 + "");
                    }
                });
                try {
                    Path path = FileSystems.getDefault().getPath(directorioDescarga.getText());
                    FileVisitor visitor = new FileVisitor();
                    /* List all files from a directory and its subdirectories */
                    Files.walkFileTree(path, visitor);
                } catch (InvalidPathException | IOException ex) {
                    JOptionPane.showMessageDialog(null, "Error Al Actualizar Biblioteca. ¿Existe Directorio?");
                }
                this.interrupt();
            }
        };
        hiloBiblioteca.start();
        //sorter2.modelStructureChanged();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane4 = new javax.swing.JScrollPane() {{
            setOpaque(false);
            getViewport().setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
            super.paintComponent(g);
        }
    };
    jTable2 = new javax.swing.JTable() {{
        setOpaque(false);
        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {{
            setOpaque(false);
        }});
    }};
    jTabbedPane1 = new javax.swing.JTabbedPane();
    jPanel1 = new javax.swing.JPanel();
    texto = new javax.swing.JTextField();
    jButton1 = new javax.swing.JButton();
    loading = new javax.swing.JLabel();
    jButton2 = new javax.swing.JButton();
    filtro = new javax.swing.JTextField();
    jLabel1 = new javax.swing.JLabel();
    jLabel2 = new javax.swing.JLabel();
    numeroResultados = new javax.swing.JLabel();
    jLabel4 = new javax.swing.JLabel();
    jLabel5 = new javax.swing.JLabel();
    jLabel6 = new javax.swing.JLabel();
    jLabel8 = new javax.swing.JLabel();
    botonPlay = new javax.swing.JButton();
    panelMarquesina = new javax.swing.JPanel();
    jScrollPane1 = new javax.swing.JScrollPane() {{
        setOpaque(false);
        getViewport().setOpaque(false);
    }
    @Override
    protected void paintComponent(Graphics g) {
        g.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
        super.paintComponent(g);
    }
    };
    jTable1 = new javax.swing.JTable();
    jSlider2 = new javax.swing.JSlider();
    jLabel13 = new javax.swing.JLabel();
    jLabel14 = new javax.swing.JLabel();
    tiempo = new javax.swing.JLabel();
    jLabel16 = new javax.swing.JLabel();
    jPanel2 = new javax.swing.JPanel();
    jScrollPane2 = new javax.swing.JScrollPane(this.downloader);
    jPanel4 = new javax.swing.JPanel();
    jScrollPane5 = new javax.swing.JScrollPane();
    jTable3 = new javax.swing.JTable();
    filtro1 = new javax.swing.JTextField();
    jLabel9 = new javax.swing.JLabel();
    botonAnterior = new javax.swing.JButton();
    botonSiguiente = new javax.swing.JButton();
    jToggleButton1 = new javax.swing.JToggleButton();
    botonPlayPausaBiblioteca = new javax.swing.JButton();
    jButton4 = new javax.swing.JButton();
    jSlider1 = new javax.swing.JSlider();
    jLabel10 = new javax.swing.JLabel();
    panelMarquesina1 = new javax.swing.JPanel();
    jLabel11 = new javax.swing.JLabel();
    jLabel12 = new javax.swing.JLabel();
    sliderProgreso = new javax.swing.JSlider();
    jPanel3 = new javax.swing.JPanel();
    jScrollPane3 = new javax.swing.JScrollPane();
    lafList = new javax.swing.JList();
    jLabel3 = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    directorioDescarga = new javax.swing.JTextField();
    jButton3 = new javax.swing.JButton();

    jScrollPane4.setBackground(new java.awt.Color(0, 0, 255));

    jTable2.setAutoCreateRowSorter(true);
    jTable2.setBackground(new java.awt.Color(0, 0, 0));
    jTable2.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Nombre", "Duración", "Calidad", "Id"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.String.class
        };
        boolean[] canEdit = new boolean [] {
            false, false, false, false
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    jTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    jTable2.setOpaque(false);
    jScrollPane4.setViewportView(jTable2);

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    setTitle("MP3Digger - Descarga Tu Música Favorita!");

    jTabbedPane1.setOpaque(true);

    texto.setToolTipText("Búsquedas de canciones específicas arrojarán resultados de mayor calidad");
    texto.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) {
            textoEnterPressed(evt);
        }
    });

    jButton1.setText("Buscar");
    jButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonBuscarActionPerformed(evt);
        }
    });

    loading.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/loading.gif"))); // NOI18N
    loading.setText("Cargando Resultados..");

    jButton2.setText("Cancelar");
    jButton2.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonCancelarActionPerformed(evt);
        }
    });

    jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel1.setText("Filtrar:");

    jLabel2.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel2.setText("Resultados:");

    numeroResultados.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    numeroResultados.setText("0");

    jLabel4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel4.setText("Alexis O. Caballero, Made In Argentina");

    jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel5.setText("Versión: 0.7");

    jLabel6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/argentina.png"))); // NOI18N

    jLabel8.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel8.setText("Reproduciendo:");

    botonPlay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/play.png"))); // NOI18N
    botonPlay.setEnabled(false);
    botonPlay.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonPlayActionPerformed(evt);
        }
    });

    panelMarquesina.setBackground(new java.awt.Color(0, 0, 0));

    javax.swing.GroupLayout panelMarquesinaLayout = new javax.swing.GroupLayout(panelMarquesina);
    panelMarquesina.setLayout(panelMarquesinaLayout);
    panelMarquesinaLayout.setHorizontalGroup(
        panelMarquesinaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 137, Short.MAX_VALUE)
    );
    panelMarquesinaLayout.setVerticalGroup(
        panelMarquesinaLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 0, Short.MAX_VALUE)
    );

    jScrollPane1.setBackground(new java.awt.Color(0, 0, 255));

    jTable1.setAutoCreateRowSorter(true);
    jTable1.setBackground(new java.awt.Color(0, 0, 0));
    jTable1.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Nombre", "Duración", "Calidad", "Link"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
        };
        boolean[] canEdit = new boolean [] {
            false, false, false, false
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    jTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    jTable1.setOpaque(false);
    jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            tablaBusquedaMouseClicked(evt);
        }
    });
    jScrollPane1.setViewportView(jTable1);

    jSlider2.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            jSlider2sliderChange(evt);
        }
    });

    jLabel13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/1362390121_sound_high.png"))); // NOI18N

    jLabel14.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel14.setText("Tiempo:");

    tiempo.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    tiempo.setText("0");
    tiempo.setAlignmentX(JTextField.RIGHT);

    jLabel16.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel16.setText("seg.");

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(2, 2, 2)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(jLabel8)
                    .addGap(18, 18, 18)
                    .addComponent(panelMarquesina, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(botonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(10, 10, 10)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jLabel4)
                    .addGap(14, 14, 14)
                    .addComponent(jLabel6))
                .addGroup(jPanel1Layout.createSequentialGroup()
                    .addComponent(texto, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton1)
                    .addGap(7, 7, 7)
                    .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(loading)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel14)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(tiempo, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel16)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel2)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(numeroResultados, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(filtro, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1133, Short.MAX_VALUE))
            .addGap(0, 0, 0))
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(31, 31, 31)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filtro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(numeroResultados)
                    .addComponent(jLabel14)
                    .addComponent(tiempo)
                    .addComponent(jLabel16))
                .addComponent(loading)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(texto, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 527, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(botonPlay, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(panelMarquesina, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.Alignment.TRAILING))
                .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap())
    );

    jTabbedPane1.addTab("Búsqueda", jPanel1);

    javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
    jPanel2.setLayout(jPanel2Layout);
    jPanel2Layout.setHorizontalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 1135, Short.MAX_VALUE)
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 628, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab("Descargas", jPanel2);

    jTable3.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Titulo", "Artista", "Duracion", "Calidad (kbps)", "Album", "Año", "Nombre Archivo"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
        };
        boolean[] canEdit = new boolean [] {
            false, false, false, false, false, false, false
        };

        public Class getColumnClass(int columnIndex) {
            return types [columnIndex];
        }

        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return canEdit [columnIndex];
        }
    });
    jTable3.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
    jTable3.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            tablaTemasMouseClicked(evt);
        }
    });
    jTable3.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) {
            tablaBibliotecaEnter(evt);
        }
    });
    jScrollPane5.setViewportView(jTable3);

    jLabel9.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel9.setText("Buscar:");

    botonAnterior.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/back_ff.png"))); // NOI18N
    botonAnterior.setToolTipText("Anterior Canción");
    botonAnterior.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonAnteriorActionPerformed(evt);
        }
    });

    botonSiguiente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/playback_ff.png"))); // NOI18N
    botonSiguiente.setToolTipText("Siguiente Canción");
    botonSiguiente.setPreferredSize(new java.awt.Dimension(48, 23));
    botonSiguiente.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            botonSiguienteMouseClicked(evt);
        }
    });
    botonSiguiente.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonSiguienteActionPerformed(evt);
        }
    });

    jToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/media-shuffle.png"))); // NOI18N
    jToggleButton1.setToolTipText("Aleatorio [ON/OFF]");
    jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jToggleButton1ActionPerformed(evt);
        }
    });

    botonPlayPausaBiblioteca.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/play_pause.png"))); // NOI18N
    botonPlayPausaBiblioteca.setToolTipText("Play/Pausa");
    botonPlayPausaBiblioteca.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonPlayPausaBibliotecaActionPerformed(evt);
        }
    });

    jButton4.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jButton4.setText("Actualizar Biblioteca");
    jButton4.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton4ActionPerformed(evt);
        }
    });

    jSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            sliderChange(evt);
        }
    });

    jLabel10.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel10.setText(" Volumen:");

    panelMarquesina1.setBackground(new java.awt.Color(0, 0, 0));
    panelMarquesina1.setForeground(new java.awt.Color(255, 255, 255));

    javax.swing.GroupLayout panelMarquesina1Layout = new javax.swing.GroupLayout(panelMarquesina1);
    panelMarquesina1.setLayout(panelMarquesina1Layout);
    panelMarquesina1Layout.setHorizontalGroup(
        panelMarquesina1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 213, Short.MAX_VALUE)
    );
    panelMarquesina1Layout.setVerticalGroup(
        panelMarquesina1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 0, Short.MAX_VALUE)
    );

    jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel11.setText("N. Canciones:");

    jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel12.setText("0");

    sliderProgreso.setForeground(new java.awt.Color(0, 51, 255));
    sliderProgreso.setValue(0);
    sliderProgreso.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    sliderProgreso.addChangeListener(new javax.swing.event.ChangeListener() {
        public void stateChanged(javax.swing.event.ChangeEvent evt) {
            sliderProgresoStateChanged(evt);
        }
    });
    sliderProgreso.addMouseListener(new java.awt.event.MouseAdapter() {
        public void mouseClicked(java.awt.event.MouseEvent evt) {
            sliderProgresoMouseClicked(evt);
        }
    });

    javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
    jPanel4.setLayout(jPanel4Layout);
    jPanel4Layout.setHorizontalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel4Layout.createSequentialGroup()
            .addContainerGap()
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane5)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addComponent(botonAnterior, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(botonPlayPausaBiblioteca, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(botonSiguiente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(26, 26, 26)
                    .addComponent(jLabel10)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jToggleButton1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(panelMarquesina1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(sliderProgreso, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addGap(29, 29, 29)
                    .addComponent(jLabel9)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(filtro1, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                    .addComponent(jLabel11)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jLabel12)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jButton4)))
            .addContainerGap())
    );
    jPanel4Layout.setVerticalGroup(
        jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
            .addGap(18, 18, 18)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                .addComponent(botonAnterior, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botonSiguiente, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(botonPlayPausaBiblioteca, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(panelMarquesina1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filtro1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jToggleButton1))
                .addComponent(sliderProgreso, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(18, 18, 18)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
            .addGap(8, 8, 8)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton4)
                .addComponent(jLabel11)
                .addComponent(jLabel12))
            .addGap(12, 12, 12))
    );

    jTabbedPane1.addTab("Biblioteca", jPanel4);

    lafList.setModel(new javax.swing.AbstractListModel() {
        String[] strings = { "Acryl", "Aero", "Aluminium", "Bernstein", "Fast", "Graphite", "HiFi", "Luna", "McWin", "Mint", "Noire", "Smart\"", "Magellan", "Autumn", "Business Black Steel", "Business Blue Steel", "Business", "Cerulean", "Challenger Deep", "Creme Coffee", "Creme", "Dust Coffee", "Dust", "Emerald Dusk", "Gemini", "Graphite Aqua", "Graphite Glass", "Mariner", "Mist Aqua", "Mist Silver", "Moderate", "Nebula Brick Wall", "Nebula", "Office Black 2007", "Office Blue 2007", "Office Silver 2007", "Raven", "Sahara", "Twilight", "Graphite Red" };
        public int getSize() { return strings.length; }
        public Object getElementAt(int i) { return strings[i]; }
    });
    lafList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
    jScrollPane3.setViewportView(lafList);

    jLabel3.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel3.setText("Apariencia:");

    jLabel7.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel7.setText("Directorio de Descarga:");

    directorioDescarga.setEditable(false);
    directorioDescarga.setText(" ");

    jButton3.setText("Elegir..");
    jButton3.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            jButton3ActionPerformed(evt);
        }
    });

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(55, 55, 55)
                    .addComponent(jLabel3))
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGap(25, 25, 25)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(18, 18, 18)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel7)
                    .addComponent(directorioDescarga, javax.swing.GroupLayout.PREFERRED_SIZE, 475, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addComponent(jButton3, javax.swing.GroupLayout.Alignment.TRAILING))
            .addContainerGap())
    );
    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addGap(51, 51, 51)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel3)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(directorioDescarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(18, 18, 18)
                    .addComponent(jButton3))
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 531, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addContainerGap(22, Short.MAX_VALUE))
    );

    jTabbedPane1.addTab("Opciones", jPanel3);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
    getContentPane().setLayout(layout);
    layout.setHorizontalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jTabbedPane1)
    );
    layout.setVerticalGroup(
        layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jTabbedPane1)
    );

    pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            JFileChooser fc = new JFileChooser();
            if (existeArchivoOpciones()) {
                fc.setCurrentDirectory(new File(this.opciones.get("directorioDescarga").toString()));
            }
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setMultiSelectionEnabled(false);
            fc.setForeground(Color.WHITE);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                this.directorioDescarga.setText(file.getAbsolutePath());
                this.opciones.put("directorioDescarga", file.getAbsolutePath());
                this.escribirOpciones();
            }
        } catch (NullPointerException ex) {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void renaudarReproduccionBusqueda() {
        try {
            marquesinaCancion.resumeScrolling();
            controlador2.resume();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void renaudarReproduccionBiblioteca() {
        try {
            marquesinaCancion2.resumeScrolling();
            controlador.resume();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void pausarReproduccionBusqueda() {
        try {
            marquesinaCancion.pauseScrolling();
            controlador2.pause();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void pausarReproduccionBiblioteca() {
        try {
            marquesinaCancion2.pauseScrolling();
            controlador.pause();
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private boolean bibliotecaReproduciendo() {
        boolean valor = false;
        if (basicPlayer.getStatus() == 0) {
            valor = true;
        }
        return valor;
    }

    private boolean busquedaReproduciendo() {
        boolean valor = false;
        if (basicPlayer2.getStatus() == 0) {
            valor = true;
        }
        return valor;
    }

    private void botonSiguienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonSiguienteActionPerformed
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (jToggleButton1.isSelected()) {
                        Random rn = new Random();
                        int nAzar = rn.nextInt(jTable3.getRowCount());
                        controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(nAzar, 6).toString()).toString()));
                        controlador.play();
                        leerVolumen();
                        filaCancionActual = nAzar;
                    } else {
                        if (filaCancionActual + 1 == jTable3.getRowCount()) {
                            filaCancionActual = 0;
                            controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(filaCancionActual, 6).toString()).toString()));
                            controlador.play();
                            leerVolumen();
                        } else {
                            filaCancionActual++;
                            controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(filaCancionActual, 6).toString()).toString()));
                            controlador.play();
                            leerVolumen();
                        }
                    }
                    jTable3.setRowSelectionInterval(filaCancionActual, filaCancionActual);
                    jTable3.scrollRectToVisible(jTable3.getCellRect(filaCancionActual, 0, false));
                    if ("".equals(jTable3.getModel().getValueAt(filaCancionActual, 0).toString())) {
                        pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(filaCancionActual, 6).toString(), jTable3.getModel().getValueAt(filaCancionActual, 2).toString(), jTable3.getModel().getValueAt(filaCancionActual, 3).toString());
                    } else {
                        pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(filaCancionActual, 0).toString() + " - " + jTable3.getModel().getValueAt(filaCancionActual, 1).toString(), jTable3.getModel().getValueAt(filaCancionActual, 2).toString(), jTable3.getModel().getValueAt(filaCancionActual, 3).toString());
                    }
                } catch (BasicPlayerException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                }
            }
        });
    }//GEN-LAST:event_botonSiguienteActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        actualizarBiblioteca();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void tablaTemasMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaTemasMouseClicked
        if (SwingUtilities.isLeftMouseButton(evt) & evt.getClickCount() == 2) {
            Thread hiloPlay = new Thread() {
                @Override
                public void run() {
                    ArrayList cancionSeleccionada = new ArrayList();
                    filaCancionActual = jTable3.getSelectedRow();
                    for (int i = 0; i < jTable3.getColumnCount(); i++) {
                        cancionSeleccionada.add(jTable3.getValueAt(filaCancionActual, i));
                    }
                    if (busquedaReproduciendo()) {
                        pausarReproduccionBusqueda();
                    }
                    try {
                        controlador.stop();
                        sliderProgreso.setValue(0);
                        controlador.open(new File(mapaTemas.get(cancionSeleccionada.get(6).toString()).toString()));
                        controlador.play();
                        leerVolumen();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                    if ("".equals(cancionSeleccionada.get(0).toString())) {
                        pintarMarquesinaBiblioteca(cancionSeleccionada.get(6).toString(), cancionSeleccionada.get(2).toString(), cancionSeleccionada.get(3).toString());
                    } else {
                        pintarMarquesinaBiblioteca(cancionSeleccionada.get(0).toString() + " - " + cancionSeleccionada.get(1).toString(), cancionSeleccionada.get(2).toString(), cancionSeleccionada.get(3).toString());
                    }
                    this.interrupt();
                }
            };
            hiloPlay.start();
        }
    }//GEN-LAST:event_tablaTemasMouseClicked

    private void leerVolumen() {
        double volumen = this.jSlider1.getValue() / 100.0;
        try {
            this.controlador.setGain(volumen);
        } catch (BasicPlayerException ex) {
            //JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void leerVolumenBusqueda() {
        double volumen = this.jSlider2.getValue() / 100.0;
        try {
            this.controlador2.setGain(volumen);
        } catch (BasicPlayerException ex) {
            //JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void correrTiempo() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                tiempo.setText(0 + "");
                hiloTiempo = new Thread() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                this.sleep(1000);
                                tiempo.setText((Integer.parseInt(tiempo.getText()) + 1) + "");
                            }
                        } catch (InterruptedException ex) {
                        }
                    }
                };
                hiloTiempo.start();
            }
        ;
    }

    );
    }

    @SuppressWarnings("empty-statement")
    private void detenerTiempo() {
        hiloTiempo.interrupt();
    }

    private void sliderChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderChange
        leerVolumen();
    }//GEN-LAST:event_sliderChange

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
    }//GEN-LAST:event_jToggleButton1ActionPerformed

    private void botonPlayPausaBibliotecaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonPlayPausaBibliotecaActionPerformed
        try {
            if (busquedaReproduciendo()) {
                pausarReproduccionBusqueda();
                renaudarReproduccionBiblioteca();
            } else {
                if (filaCancionActual == -1) {
                    controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(0, 6).toString()).toString()));
                    controlador.play();
                    leerVolumen();
                    filaCancionActual = 0;
                } else {
                    if (bibliotecaReproduciendo()) {
                        pausarReproduccionBiblioteca();
                    } else {
                        renaudarReproduccionBiblioteca();
                        leerVolumen();
                    }
                }
            }
        } catch (BasicPlayerException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_botonPlayPausaBibliotecaActionPerformed

    private void botonAnteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonAnteriorActionPerformed
        if (bibliotecaReproduciendo()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (jToggleButton1.isSelected()) {
                            Random rn = new Random();
                            int nAzar = rn.nextInt(jTable3.getRowCount());
                            controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(nAzar, 6).toString()).toString()));
                            controlador.play();
                            leerVolumen();
                            filaCancionActual = nAzar;
                        } else {
                            if (filaCancionActual == 0) {
                                filaCancionActual = jTable3.getRowCount() - 1;
                                controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(filaCancionActual, 6).toString()).toString()));
                                controlador.play();
                                leerVolumen();
                            } else {
                                filaCancionActual--;
                                controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(filaCancionActual, 6).toString()).toString()));
                                controlador.play();
                                leerVolumen();
                            }
                        }
                        jTable3.setRowSelectionInterval(filaCancionActual, filaCancionActual);
                        jTable3.scrollRectToVisible(jTable3.getCellRect(filaCancionActual, 0, false));
                        if ("".equals(jTable3.getModel().getValueAt(filaCancionActual, 0).toString())) {
                            pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(filaCancionActual, 6).toString(), jTable3.getModel().getValueAt(filaCancionActual, 2).toString(), jTable3.getModel().getValueAt(filaCancionActual, 3).toString());
                        } else {
                            pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(filaCancionActual, 0).toString() + " - " + jTable3.getModel().getValueAt(filaCancionActual, 1).toString(), jTable3.getModel().getValueAt(filaCancionActual, 2).toString(), jTable3.getModel().getValueAt(filaCancionActual, 3).toString());
                        }
                    } catch (BasicPlayerException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                }
            });
        }
    }//GEN-LAST:event_botonAnteriorActionPerformed

    private void tablaBibliotecaEnter(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tablaBibliotecaEnter
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            Thread hiloPlay = new Thread() {
                @Override
                public void run() {
                    ArrayList cancionSeleccionada = new ArrayList();
                    for (int i = 0; i < jTable3.getColumnCount(); i++) {
                        cancionSeleccionada.add(jTable3.getValueAt(jTable3.getSelectedRow() - 1, i));
                    }
                    jTable3.setRowSelectionInterval(jTable3.getSelectedRow() - 1, jTable3.getSelectedRow() - 1);
                    if (busquedaReproduciendo()) {
                        pausarReproduccionBusqueda();
                    }
                    try {
                        controlador.stop();
                        controlador.open(new File(mapaTemas.get(cancionSeleccionada.get(6).toString()).toString()));
                        controlador.play();
                        leerVolumen();
                        filaCancionActual = jTable3.getSelectedRow();
                    } catch (BasicPlayerException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    }
                    if ("".equals(cancionSeleccionada.get(0).toString())) {
                        pintarMarquesinaBiblioteca(cancionSeleccionada.get(6).toString(), cancionSeleccionada.get(2).toString(), cancionSeleccionada.get(3).toString());
                    } else {
                        pintarMarquesinaBiblioteca(cancionSeleccionada.get(0).toString() + " - " + cancionSeleccionada.get(1).toString(), cancionSeleccionada.get(2).toString(), cancionSeleccionada.get(3).toString());
                    }
                    this.interrupt();
                }
            };
            hiloPlay.start();
        }
    }//GEN-LAST:event_tablaBibliotecaEnter

    private void botonPlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonPlayActionPerformed
        if (bibliotecaReproduciendo()) {
            pausarReproduccionBiblioteca();
            renaudarReproduccionBusqueda();
        } else {
            if (!busquedaReproduciendo()) {
                renaudarReproduccionBusqueda();
            } else {
                pausarReproduccionBusqueda();
            }
        }
    }//GEN-LAST:event_botonPlayActionPerformed

    private void botonCancelarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonCancelarActionPerformed
        detenerTiempo();
        for (Thread thread : listaHilos) {
            thread.interrupt();
        }
        for (Timer timer : listaTimers) {
            timer.stop();
        }
        this.loading.setVisible(false);
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarActionPerformed
        try {
            if (hiloTiempo != null) {
                detenerTiempo();
            }
            mapaResultados.clear();
            for (Thread thread : listaHilos) {
                thread.interrupt();
            }
            for (Timer timer : listaTimers) {
                timer.stop();
            }
            this.numeroResultados.setText("0");
            System.out.println("Buscando: " + texto.getText());
            this.busquedaActual = texto.getText();
            if (texto.getText().equals("")) {
                JOptionPane.showMessageDialog(rootPane, "Ingrese Un Nombre o Título A Buscar");
            } else {
                this.limpiarTabla();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final String busqueda = texto.getText();
                            loading.setVisible(true);
                            correrTiempo();
                            CargadorPlugins.cargarPlugins();
                            IPluginSearch[] vectorPlugins = CargadorPlugins.getPlugins();
                            //Si existen plugins de busqueda realizamos la busqueda.
                            if (vectorPlugins.length > 0) {
                                for (final IPluginSearch a : vectorPlugins) {
                                    //por cada plugin ejecutamos un hilo donde se realizara la busqueda
                                    Thread hiloPluginDescarga = new Thread() {
                                        @Override
                                        public void run() {
                                            cantidadHilosBuscando++;
                                            a.loadCanciones(busqueda);
                                        }
                                    };
                                    listaHilos.add(hiloPluginDescarga);
                                    hiloPluginDescarga.start();
                                    Timer timer = new Timer(1000, new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent event) {
                                            final DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
                                            List<CancionDTO> listaAux = a.getCanciones();
                                            CancionDTO[] arrayCanciones = new CancionDTO[listaAux.size()];
                                            arrayCanciones = listaAux.toArray(arrayCanciones);
                                            for (CancionDTO elementoLista : arrayCanciones) {
                                                if (!mapaResultados.containsKey(elementoLista.getId())) {
                                                    final String[] vector = {elementoLista.getNombre(), elementoLista.getDuracion(), elementoLista.getCalidad(), elementoLista.getId()};
                                                    if (busqueda.equals(busquedaActual)) {
                                                        try {
                                                            modelo.addRow(vector);
                                                            int numeroR = Integer.parseInt(numeroResultados.getText());
                                                            numeroResultados.setText((numeroR + 1) + "");
                                                            mapaResultados.put(elementoLista.getId(), "");
                                                        } catch (Exception ex) {
                                                            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
                                                        }
                                                    }
                                                }
                                            }
                                            /**
                                            if (a.terminado()) {
                                                cantidadHilosBuscando--;
                                                if (cantidadHilosBuscando == 0) {
                                                    loading.setVisible(false);
                                                    detenerTiempo();
                                                }
                                                ((Timer) event.getSource()).stop();
                                            }
                                            **/
                                        }
                                    });
                                    listaTimers.add(timer);
                                    timer.start();
                                }
                            } else {
                                loading.setVisible(false);
                                detenerTiempo();
                                JOptionPane.showMessageDialog(rootPane, "No Existen Plugins Cargados.");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
                        }
                    }
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }//GEN-LAST:event_botonBuscarActionPerformed

    private void textoEnterPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textoEnterPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            for (Thread thread : listaHilos) {
                thread.interrupt();
            }
            this.loading.setVisible(false);
            this.jButton1.doClick();
        }
    }//GEN-LAST:event_textoEnterPressed

    private void tablaBusquedaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tablaBusquedaMouseClicked
        final ArrayList cancionSeleccionada = new ArrayList();
        jTable1.setRowSelectionInterval(jTable1.rowAtPoint(evt.getPoint()), jTable1.rowAtPoint(evt.getPoint()));
        for (int i = 0; i < jTable1.getColumnCount(); i++) {
            cancionSeleccionada.add(jTable1.getValueAt(jTable1.getSelectedRow(), i));
        }
        if (SwingUtilities.isLeftMouseButton(evt) & evt.getClickCount() == 2) {
            Thread hiloDescarga = new Thread() {
                @Override
                public void run() {
                    final String linkMP3 = cancionSeleccionada.get(3).toString();
                    if (linkMP3 != null) {
                        Thread hilo = new Thread() {
                            @Override
                            public void run() {
                                String url = linkMP3;
                                int codigo = 410;
                                try {
                                    URL link = new URL(linkMP3);
                                    while (codigo == 410) {
                                        HttpURLConnection connection = (HttpURLConnection) link.openConnection();
                                        connection.connect();
                                        if (connection.getResponseCode() == 410) {
                                            System.out.println("intento..");
                                            link = new URL(cancionSeleccionada.get(3).toString());
                                        } else {
                                            codigo = 200;
                                            url = link.toString();
                                        }
                                    }
                                    this.interrupt();
                                } catch (IOException ex) {
                                    JOptionPane.showMessageDialog(null, ex.getMessage());
                                }
                                downloader.actionAdd(cancionSeleccionada.get(0).toString(), url);
                            }
                        };
                        hilo.start();
                    }
                    this.interrupt();
                }
            };
            hiloDescarga.start();
        } else {
            if (SwingUtilities.isRightMouseButton(evt)) {
                jTable1.setRowSelectionInterval(jTable1.rowAtPoint(evt.getPoint()), jTable1.rowAtPoint(evt.getPoint()));
                JPopupMenu MenuOpciones = new JPopupMenu();
                JMenuItem itemDescargar = new JMenuItem("Descargar");
                JMenuItem itemReproducir = new JMenuItem("Reproducir");
                UIManager.getLookAndFeelDefaults().put("MenuItem.opaque", true);
                itemReproducir.setIcon(ImageHelper.loadImage("iconos/p.png"));
                itemDescargar.setIcon(ImageHelper.loadImage("iconos/Download.png"));
                ActionListener rListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    controlador2.stop();
                                    pausarReproduccionBiblioteca();
                                    marquesinaCancion.setSize(panelMarquesina.getSize());
                                    marquesinaCancion.setOpaque(false);
                                    marquesinaCancion.setBackground(Color.BLACK);
                                    marquesinaCancion.removeAll();
                                    JLabel label = new JLabel();
                                    label.setText("Cargando..");
                                    label.setForeground(Color.WHITE);
                                    marquesinaCancion.add(label);
                                    marquesinaCancion.setScrollFrequency(9);
                                    marquesinaCancion.startScrolling();
                                    panelMarquesina.add(marquesinaCancion);
                                    mp3Digger.revalidate();
                                    mp3Digger.repaint();
                                    URL linkMP3 = new URL(cancionSeleccionada.get(3).toString());
                                    int codigo = 410;
                                    while (codigo == 410) {
                                        HttpURLConnection connection = (HttpURLConnection) linkMP3.openConnection();
                                        connection.connect();
                                        if (connection.getResponseCode() == 410) {
                                            System.out.println("intento reproducir..");
                                            linkMP3 = new URL(cancionSeleccionada.get(3).toString());
                                        } else {
                                            codigo = 200;
                                        }
                                    }
                                    controlador2.open(linkMP3);
                                    controlador2.play();
                                    leerVolumenBusqueda();
                                    label.setText(cancionSeleccionada.get(0).toString());
                                    botonPlay.setEnabled(true);
                                } catch (IOException | BasicPlayerException ex) {
                                    JOptionPane.showMessageDialog(null, "Error Al Reproducir Archivo, Por Favor Intente Otro.");
                                }
                            }
                        });
                    }
                };
                ActionListener dListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        try {
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    final String linkMP3 = cancionSeleccionada.get(3).toString();
                                    Thread hilo = new Thread() {
                                        @Override
                                        public void run() {
                                            String url = linkMP3;
                                            int codigo = 410;
                                            try {
                                                URL link = new URL(linkMP3);
                                                while (codigo == 410) {
                                                    HttpURLConnection connection = (HttpURLConnection) link.openConnection();
                                                    connection.connect();
                                                    if (connection.getResponseCode() == 410) {
                                                        System.out.println("intento..");
                                                        link = new URL(cancionSeleccionada.get(3).toString());
                                                    } else {
                                                        codigo = 200;
                                                        url = link.toString();
                                                    }
                                                }
                                                this.interrupt();
                                            } catch (IOException ex) {
                                                JOptionPane.showMessageDialog(null, ex.getMessage());
                                            }
                                            final String urlAux = url; 
                                            SwingUtilities.invokeLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    downloader.actionAdd(cancionSeleccionada.get(0).toString(), urlAux);
                                                }
                                            });
                                        }
                                    };
                                    hilo.start();
                                }
                            });
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, ex.getMessage());
                        }
                    }
                };
                itemReproducir.addActionListener(rListener);
                itemDescargar.addActionListener(dListener);
                MenuOpciones.add(itemReproducir);
                MenuOpciones.add(itemDescargar);
                Component component = (Component) evt.getSource();
                MenuOpciones.show(component, evt.getX(), evt.getY());
            }
        }
    }//GEN-LAST:event_tablaBusquedaMouseClicked

    private void jSlider2sliderChange(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSlider2sliderChange
        leerVolumenBusqueda();
    }//GEN-LAST:event_jSlider2sliderChange

    private void sliderProgresoStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderProgresoStateChanged
        if (sliderProgreso.getValueIsAdjusting()) {
            valorCambiado = true;
            nuevoValor = sliderProgreso.getValue();
        } else {
            if (valorCambiado) {
                try {
                    valorCambiado = false;
                    controlador.seek(nuevoValor);
                    leerVolumen();
                } catch (BasicPlayerException ex) {
                    JOptionPane.showMessageDialog(this, ex.getMessage());
                }
            }
        }
    }//GEN-LAST:event_sliderProgresoStateChanged

    private void sliderProgresoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_sliderProgresoMouseClicked
        JSlider sourceSlider = (JSlider) evt.getSource();
        BasicSliderUI ui = (BasicSliderUI) sourceSlider.getUI();
        int value = ui.valueForXPosition(evt.getX());
        sliderProgreso.setValue(value);
    }//GEN-LAST:event_sliderProgresoMouseClicked

    private void botonSiguienteMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_botonSiguienteMouseClicked
    }//GEN-LAST:event_botonSiguienteMouseClicked

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MP3Digger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MP3Digger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MP3Digger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MP3Digger.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MP3Digger().getInstancia().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton botonAnterior;
    private javax.swing.JButton botonPlay;
    private javax.swing.JButton botonPlayPausaBiblioteca;
    private javax.swing.JButton botonSiguiente;
    private javax.swing.JTextField directorioDescarga;
    private javax.swing.JTextField filtro;
    private javax.swing.JTextField filtro1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JSlider jSlider2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JList lafList;
    private javax.swing.JLabel loading;
    private javax.swing.JLabel numeroResultados;
    private javax.swing.JPanel panelMarquesina;
    private javax.swing.JPanel panelMarquesina1;
    private static javax.swing.JSlider sliderProgreso;
    private javax.swing.JTextField texto;
    private javax.swing.JLabel tiempo;
    // End of variables declaration//GEN-END:variables

    private void setIcon() {
        BufferedImage image;
        try {
            image = ImageIO.read(this.getClass().getResource("iconos/MP3DiggerLogo.png"));
            this.setIconImage(image);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
        }
    }

    private void agregarfiltroListener() {
        this.filtro.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        newFilter();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        newFilter();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        newFilter();
                    }
                });
    }

    private void agregarfiltro2Listener() {
        this.filtro1.getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        newFilter2();
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        newFilter2();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        newFilter2();
                    }
                });
    }

    private void newFilter2() {
        RowFilter<TableModel, Object> rf = null;
        RowFilter<TableModel, Object> rf2 = null;
        RowFilter<TableModel, Object> rf3 = null;
        RowFilter<TableModel, Object> rf4 = null;
        RowFilter<TableModel, Object> rf5 = null;
        ArrayList<RowFilter<TableModel, Object>> orFilters = new ArrayList<RowFilter<TableModel, Object>>();
        //declare a row filter for your table model  
        try {
            rf = RowFilter.regexFilter("(?i).*" + this.filtro1.getText() + ".*(?i)", 0);
            rf2 = RowFilter.regexFilter("(?i).*" + this.filtro1.getText() + ".*(?i)", 1);
            rf3 = RowFilter.regexFilter("(?i).*" + this.filtro1.getText() + ".*(?i)", 4);
            rf4 = RowFilter.regexFilter("(?i).*" + this.filtro1.getText() + ".*(?i)", 5);
            rf5 = RowFilter.regexFilter("(?i).*" + this.filtro1.getText() + ".*(?i)", 6);
            orFilters.add(rf);
            orFilters.add(rf2);
            orFilters.add(rf3);
            orFilters.add(rf4);
            orFilters.add(rf5);
        } catch (java.util.regex.PatternSyntaxException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        sorter2.setRowFilter(RowFilter.orFilter(orFilters));
    }

    private void newFilter() {
        RowFilter<TableModel, Object> rf = null;
        RowFilter<TableModel, Object> rf2 = null;
        ArrayList<RowFilter<TableModel, Object>> orFilters = new ArrayList<RowFilter<TableModel, Object>>();
        //declare a row filter for your table model  
        try {
            rf = RowFilter.regexFilter("(?i).*" + this.filtro.getText() + ".*(?i)", 0);
            rf2 = RowFilter.regexFilter("(?i).*" + this.filtro.getText() + ".*(?i)", 2);
            orFilters.add(rf);
            orFilters.add(rf2);
        } catch (java.util.regex.PatternSyntaxException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
        sorter.setRowFilter(RowFilter.orFilter(orFilters));
    }

    private void aparienciaListener() {
        ListSelectionListener lafListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (lafList.getSelectedIndex() != -1) {
                        if (selectedLaf != lafList.getSelectedIndex()) {
                            selectedLaf = lafList.getSelectedIndex();
                            // We change the look and feel after all pending events are dispatched,
                            // otherwise there will be some serious redrawing problems.
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    setApariencia();
                                }
                            });
                        }
                    } else {
                        // We don't want the list to be unselected, so if user unselects the list
                        // we just select the last selected entry
                        lafList.setSelectedIndex(selectedLaf);
                    }
                }
            }
        };
        lafList.addListSelectionListener(lafListener);
    }

    private void setApariencia() {
        try {
            String theme = "Default";
            switch (selectedLaf) {
                case 0:
                    com.jtattoo.plaf.acryl.AcrylLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 1:
                    com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.aero.AeroLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 2:
                    com.jtattoo.plaf.aluminium.AluminiumLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 3:
                    com.jtattoo.plaf.bernstein.BernsteinLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 4:
                    com.jtattoo.plaf.fast.FastLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.fast.FastLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 5:
                    com.jtattoo.plaf.graphite.GraphiteLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 6:
                    com.jtattoo.plaf.hifi.HiFiLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.hifi.HiFiLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 7:
                    com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.luna.LunaLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.luna.LunaLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 8:
                    com.jtattoo.plaf.mcwin.McWinLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.mcwin.McWinLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 9:
                    com.jtattoo.plaf.mint.MintLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.mint.MintLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 10:
                    com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.noire.NoireLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 11:
                    com.jtattoo.plaf.smart.SmartLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.smart.SmartLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 12:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceMagellanLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 13:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceAutumnLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 14:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceBusinessBlackSteelLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 15:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceBusinessBlueSteelLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 16:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceBusinessLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 17:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceCeruleanLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceCeruleanLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 18:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceChallengerDeepLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 19:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceCremeCoffeeLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 20:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceCremeLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 21:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceDustCoffeeLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 22:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceDustLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 23:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceEmeraldDuskLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 24:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceGeminiLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 25:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceGraphiteAquaLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 26:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceGraphiteGlassLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 27:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceMarinerLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 28:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 29:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceMistSilverLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 30:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceModerateLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 31:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceNebulaBrickWallLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 32:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceNebulaLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 33:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceOfficeBlack2007LookAndFeel");
                    this.escribirOpciones();
                    break;
                case 34:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceOfficeBlue2007LookAndFeel");
                    this.escribirOpciones();
                    break;
                case 35:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceOfficeSilver2007LookAndFeel");
                    this.escribirOpciones();
                    break;
                case 36:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceRavenLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 37:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceSaharaLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 38:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceTwilightLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 39:
                    UIManager.setLookAndFeel("org.pushingpixels.substance.api.skin.SubstanceGraphiteRedLookAndFeel");
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "org.pushingpixels.substance.api.skin.SubstanceGraphiteRedLookAndFeel");
                    this.escribirOpciones();
                    break;
            }
            if (UIManager.getLookAndFeel().getClass().getName().startsWith("com.jtattoo")) {
                this.jTable3.setDefaultRenderer(String.class, new CustomTableCellRenderer());
                this.jTable1.setDefaultRenderer(String.class, new CustomTableCellRenderer());
            } else {
                this.jTable3.setDefaultRenderer(String.class, new org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer());
                this.jTable1.setDefaultRenderer(String.class, new org.pushingpixels.substance.api.renderers.SubstanceDefaultTableCellRenderer());
            }
            Window windows[] = Window.getWindows();
            for (int i = 0; i < windows.length; i++) {
                if (windows[i].isDisplayable()) {
                    SwingUtilities.updateComponentTreeUI(windows[i]);
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    } // end setLookAndFeel

    public class FileVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String nombreArchivo = file.getFileName().toString();
            if (nombreArchivo.toLowerCase().endsWith(".mp3")) {
                try {
                    MP3File cancion = (MP3File) AudioFileIO.read(file.toFile());
                    AudioHeader audioHeader = cancion.getAudioHeader();
                    ID3v1Tag v1tag = (ID3v1Tag) cancion.getID3v1Tag();
                    int duracionMin = audioHeader.getTrackLength() / 60;
                    float aux = (float) audioHeader.getTrackLength() / (float) 60;
                    float duracionSeg = (aux - duracionMin) * 60;
                    final String[] vector = {file.getFileName().toString(), "", duracionMin + ":" + Math.round(duracionSeg), audioHeader.getBitRate(), "", "", nombreArchivo};
                    if (cancion.hasID3v1Tag()) {
                        vector[0] = v1tag.getFirstTitle();
                        vector[1] = v1tag.getFirstArtist();
                        vector[4] = v1tag.getFirstAlbum();
                        vector[5] = v1tag.getFirstYear();
                        vector[6] = nombreArchivo;
                    } else {
                        if (cancion.hasID3v2Tag()) {
                            AbstractID3v2Tag v2Tag = cancion.getID3v2Tag();
                            vector[0] = v2Tag.getFirst(ID3v24Frames.FRAME_ID_TITLE);
                            vector[1] = v2Tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST);
                            vector[4] = v2Tag.getFirst(ID3v24Frames.FRAME_ID_ALBUM);
                            vector[5] = v2Tag.getFirst(ID3v24Frames.FRAME_ID_YEAR);
                            vector[6] = nombreArchivo;

                        }
                    }
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            ((DefaultTableModel) jTable3.getModel()).addRow(vector);
                        }
                    });
                    mapaTemas.put(file.getFileName().toString(), file.toRealPath());
                    int numeroCanciones = Integer.parseInt(jLabel12.getText());
                    jLabel12.setText((numeroCanciones + 1) + "");
                } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
                    //JOptionPane.showMessageDialog(null, "Error Al Leer MP3: " + nombreArchivo);
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private void pintarMarquesinaBiblioteca(final String nombre, final String duracion, final String calidad) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                marquesinaCancion2.setSize(panelMarquesina1.getSize());
                marquesinaCancion2.setOpaque(false);
                marquesinaCancion2.setBackground(Color.BLACK);
                marquesinaCancion2.removeAll();
                JLabel label = new JLabel();
                label.setText(nombre + " - " + duracion + " - " + calidad + " kbps");
                label.setForeground(Color.WHITE);
                marquesinaCancion2.add(label);
                marquesinaCancion2.setScrollFrequency(9);
                marquesinaCancion2.startScrolling();
                panelMarquesina1.removeAll();
                panelMarquesina1.add(marquesinaCancion2);
                mp3Digger.getInstancia().revalidate();
                mp3Digger.getInstancia().repaint();
            }
        });
    }

    private Properties cargarPropiedadesTexture() {
        Properties props = new Properties();
        props.put("textureSet", "Custom");
        Icon texture = ImageHelper.loadImage("iconos/WindowTexture.jpg");
        if (texture != null) {
            props.put("windowTexture", texture);
        }
        texture = ImageHelper.loadImage("iconos/WindowTexture.jpg");
        if (texture != null) {
            props.put("backgroundTexture", texture);
        }
        //Tab
        texture = ImageHelper.loadImage("iconos/blanco.png");
        if (texture != null) {
            props.put("alterBackgroundTexture", texture);
        }
        //TabMouseEntered
        texture = ImageHelper.loadImage("iconos/gris.png");
        if (texture != null) {
            props.put("rolloverTexture", texture);
        }
        texture = ImageHelper.loadImage("iconos/RolloverTexture.png");
        if (texture != null) {
            props.put("selectedTexture", texture);
        }
        texture = ImageHelper.loadImage("iconos/WindowTexture.jpg");
        if (texture != null) {
            props.put("pressedTexture", texture);
        }
        texture = ImageHelper.loadImage("iconos/WindowTexture.jpg");
        if (texture != null) {
            props.put("disabledTexture", texture);
        }
        texture = ImageHelper.loadImage("iconos/blanco.png");
        if (texture != null) {
            props.put("menubarTexture", texture);
        }
        props.setProperty("backgroundColor", "240 240 240");
        props.setProperty("backgroundColorLight", "220 220 220");
        props.setProperty("backgroundColorDark", "200 200 200");
        props.setProperty("alterBackgroundColor", "180 180 180");
        props.setProperty("frameColor", "164 164 164");
        props.setProperty("gridColor", "196 196 196");
        props.setProperty("disabledForegroundColor", "96 96 96");
        props.setProperty("disabledBackgroundColor", "240 240 240");
        props.setProperty("rolloverColor", "160 160 160");
        props.setProperty("rolloverColorLight", "230 230 230");
        props.setProperty("rolloverColorDark", "210 210 210");
        props.setProperty("controlBackgroundColor", "248 248 248");
        props.setProperty("controlShadowColor", "160 160 160");
        props.setProperty("controlDarkShadowColor", "110 110 110");
        props.setProperty("controlColorLight", "248 248 248");
        props.setProperty("controlColorDark", " 210 210 210");
        props.setProperty("tabAreaBackgroundColor", "255 255 255");
        props.setProperty("buttonColorLight", "255 255 255");
        props.setProperty("buttonColorDark", "230 230 230");
        props.setProperty("foregroundColor", "255 255 255");
        props.setProperty("menuBackgroundColor", "64 64 64");
        props.setProperty("menuColorLight", "255 255 255");
        props.setProperty("menuColorDark", "255 255 255");
        props.setProperty("menuSelectionBackgroundColor", "48 48 48");
        props.setProperty("toolbarBackgroundColor", "64 64 64");
        props.setProperty("toolbarColorLight", "96 96 96");
        props.setProperty("toolbarColorDark", "48 48 48");
        props.setProperty("desktopColor", "220 220 220");
        return props;
    }

    private void cargarIconosPestañas() {
        Icon icono1 = ImageHelper.loadImage("iconos/find.png");
        this.jTabbedPane1.setIconAt(0, icono1);
        Icon icono2 = ImageHelper.loadImage("iconos/folder_download.png");
        this.jTabbedPane1.setIconAt(1, icono2);
        Icon icono3 = ImageHelper.loadImage("iconos/folder_music.png");
        this.jTabbedPane1.setIconAt(2, icono3);
        Icon icono4 = ImageHelper.loadImage("iconos/settings.png");
        this.jTabbedPane1.setIconAt(3, icono4);
    }

    private void iconosBiblioteca(String color) {
        if (color.equals("blanco")) {
            botonAnterior.setIcon(ImageHelper.loadImage("iconos/back_ff2.png"));
            botonSiguiente.setIcon(ImageHelper.loadImage("iconos/playback_ff2.png"));
            botonPlayPausaBiblioteca.setIcon(ImageHelper.loadImage("iconos/play_pause2.png"));
            jToggleButton1.setIcon(ImageHelper.loadImage("iconos/media-shuffle2.png"));
            botonPlay.setIcon(ImageHelper.loadImage("iconos/play2.png"));
        } else {
            botonAnterior.setIcon(ImageHelper.loadImage("iconos/back_ff.png"));
            botonSiguiente.setIcon(ImageHelper.loadImage("iconos/playback_ff.png"));
            botonPlayPausaBiblioteca.setIcon(ImageHelper.loadImage("iconos/play_pause.png"));
            jToggleButton1.setIcon(ImageHelper.loadImage("iconos/media-shuffle.png"));
            botonPlay.setIcon(ImageHelper.loadImage("iconos/play.png"));
        }
    }

    private void recolector() {
        Thread recolector = new Thread() {
            @Override
            public void run() {
                try {
                    while (true) {
                        sleep(5000);
                        Runtime r = Runtime.getRuntime();
                        r.gc();
                    }
                } catch (InterruptedException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } finally {
                    this.interrupt();
                }
            }
        };
        recolector.start();
    }

    public void rellenar(int ahora, int total, Long aLong) {
        sliderProgreso.setMaximum(total);
        if (!sliderProgreso.getValueIsAdjusting()) {
            sliderProgreso.setValue(ahora);
        }
        if (ahora == total) {
            try {
                controlador.stop();
            } catch (BasicPlayerException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }
        this.repaint();
    }

    private void deshabilitarLog() {
        try {
            LogManager.getLogManager().readConfiguration(
                    new ByteArrayInputStream("org.jaudiotagger.level = SEVERE".getBytes()));
        } catch (Exception ex) {
            Logger.getLogger(MP3Digger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
