package gui;

import buscador.plugins.CargadorPlugins;
import goear.BuscarGoear;
import goear.CancionDTO;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javazoom.jlgui.basicplayer.BasicController;
import javazoom.jlgui.basicplayer.BasicPlayer;
import javazoom.jlgui.basicplayer.BasicPlayerException;
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

/**
 *
 * @author Alexis
 */
public class GoearD extends javax.swing.JFrame {

    /**
     * Creates new form GoearD
     */
    private Downloader downloader;
    private List<Thread> listaHilos = new ArrayList<>();
    private String busquedaActual;
    private boolean buscando = false;
    private TableRowSorter sorter;
    private TableRowSorter sorter2;
    private int selectedLaf;
    private HashMap opciones = new HashMap();
    private MarqueePanel marquesinaCancion = new MarqueePanel();
    private MarqueePanel marquesinaCancion2 = new MarqueePanel();
    private GoearD goearD;
    private HashMap mapaTemas = new HashMap();
    private BasicPlayer basicPlayer = new BasicPlayer();
    private BasicController controlador = (BasicController) basicPlayer;
    private BasicPlayer basicPlayer2 = new BasicPlayer();
    private BasicController controlador2 = (BasicController) basicPlayer2;
    private int cancionActual = -1;
    private BufferedImage imagen = null;
    private Thread hiloTiempo;
    private List<CancionDTO> listaResultados = new ArrayList<>();
    private boolean pluginsCargados;
    
    
    public GoearD() {
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
        this.goearD = this;
        this.revalidate();
        this.repaint();
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        this.jTable3.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        this.jTable1.setDefaultRenderer(Object.class, new CustomTableCellRenderer());
        this.recolector();
        this.pluginsCargados = CargadorPlugins.cargarPlugins();
    }
    
    public GoearD getInstancia() {
        return this;
    }

    public void agregarResultados(List<CancionDTO> lista) {
        this.listaResultados.addAll(lista);
    }
    
    private void cargarLogo() {
        try {
            this.imagen = ImageIO.read(this.getClass().getResource("iconos/fondoOscuro.png"));
        } catch (IOException ex) {
            Logger.getLogger(GoearD.class.getName()).log(Level.SEVERE, null, ex);
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
                    this.directorioDescarga.setText(mapa.get("directorioDescarga").toString());
                    if (mapa.containsKey("theme")) {
                        if ("com.jtattoo.plaf.texture.TextureLookAndFeel".equals(mapa.get("theme").toString())) {
                            com.jtattoo.plaf.texture.TextureLookAndFeel.setCurrentTheme(cargarPropiedadesTexture());
                            this.logo.setIcon(ImageHelper.loadImage("iconos/goeard2.png"));
                            this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                        } else {
                            if ("com.jtattoo.plaf.hifi.HiFiLookAndFeel".equals(mapa.get("theme").toString())) {
                                iconosBiblioteca("blanco");
                                this.logo.setIcon(ImageHelper.loadImage("iconos/goeard2.png"));
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                            }
                            if ("com.jtattoo.plaf.noire.NoireLookAndFeel".equals(mapa.get("theme").toString())) {
                                this.logo.setIcon(ImageHelper.loadImage("iconos/goeard2.png"));
                                this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                            }
                        }
                        UIManager.setLookAndFeel(mapa.get("theme").toString());
                        SwingUtilities.updateComponentTreeUI(this);
                    } else {
                        UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
                        SwingUtilities.updateComponentTreeUI(this);
                        opciones.put("theme", "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
                        this.escribirOpciones();
                    }
                } catch (ClassNotFoundException | IOException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage());
                } finally {
                    input.close();
                }
            } else {
                File directory = new File(".");
                this.opciones.put("directorioDescarga", directory.getAbsolutePath());
                this.directorioDescarga.setText(directory.getAbsolutePath());
                this.escribirOpciones();
            }
        } catch (IOException ex) {
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
        DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
        modelo.setRowCount(0);
        this.jTable1.getColumnModel().getColumn(1).setMaxWidth(80);
        this.jTable1.getColumnModel().getColumn(1).setMinWidth(80);
        this.jTable1.getColumnModel().getColumn(2).setMaxWidth(100);
        this.jTable1.getColumnModel().getColumn(2).setMinWidth(100);
        this.jTable1.getColumnModel().getColumn(3).setPreferredWidth(80);
        this.jTable1.getColumnModel().getColumn(3).setMinWidth(80);
        CustomTableCellRenderer tcr = new CustomTableCellRenderer();
        tcr.setHorizontalAlignment(SwingConstants.CENTER);
        this.jTable1.getColumnModel().getColumn(2).setCellRenderer(tcr);
        this.jTable1.getColumnModel().getColumn(1).setCellRenderer(tcr);
        Runtime r = Runtime.getRuntime();
        r.gc();
    }

    public void limpiarTablaBiblioteca() {
        ((DefaultTableModel) jTable3.getModel()).setRowCount(0);
        Runtime r = Runtime.getRuntime();
        r.gc();
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
                mapaTemas = null;
                mapaTemas = new HashMap();
                Runtime r = Runtime.getRuntime();
                r.gc();
                limpiarTablaBiblioteca();
                jLabel12.setText(0 + "");
                Path path = FileSystems.getDefault().getPath(directorioDescarga.getText());
                FileVisitor visitor = new FileVisitor();
                try {
                    /* List all files from a directory and its subdirectories */
                    Files.walkFileTree(path, visitor);
                } catch (IOException ex) {
                    Logger.getLogger(GoearD.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.interrupt();
            }
        };
        hiloBiblioteca.start();
        sorter2.modelStructureChanged();
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
    jPanel3 = new javax.swing.JPanel();
    jScrollPane3 = new javax.swing.JScrollPane();
    lafList = new javax.swing.JList();
    jLabel3 = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    directorioDescarga = new javax.swing.JTextField();
    jButton3 = new javax.swing.JButton();
    logo = new javax.swing.JLabel();

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
    setTitle("GoearD - Descarga Tu Música Favorita! 100% Libre De Virus");

    jTabbedPane1.setOpaque(true);

    texto.setToolTipText("Búsquedas de canciones específicas arrojarán resultados de mayor calidad");
    texto.setSelectionColor(new java.awt.Color(0, 0, 153));
    texto.addKeyListener(new java.awt.event.KeyAdapter() {
        public void keyPressed(java.awt.event.KeyEvent evt) {
            textoEnterPressed(evt);
        }
    });

    jButton1.setText("Buscar");
    jButton1.setOpaque(false);
    jButton1.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonBuscarActionPerformed(evt);
        }
    });

    loading.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    loading.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/loading.gif"))); // NOI18N
    loading.setText("Cargando Resultados..");

    jButton2.setText("Cancelar");
    jButton2.setOpaque(false);
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
    jLabel4.setText("Alexis O. Caballero, Gualeguaychú, Entre Ríos, Argentina");

    jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel5.setText("Versión: 0.6");

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
            "Nombre", "Duración", "Calidad (kbps)", "Id"
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
                    .addComponent(jSlider2, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(jScrollPane1))
            .addGap(0, 0, 0))
    );
    jPanel1Layout.setVerticalGroup(
        jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(20, 20, 20)
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
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE)
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
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 948, Short.MAX_VALUE)
    );
    jPanel2Layout.setVerticalGroup(
        jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)
    );

    jTabbedPane1.addTab("Descargas", jPanel2);

    jScrollPane5.setOpaque(false);

    jTable3.setModel(new javax.swing.table.DefaultTableModel(
        new Object [][] {

        },
        new String [] {
            "Titulo", "Artista", "Duracion", "Calidad (kbps)", "Album", "Año", "Nombre Archivo"
        }
    ) {
        Class[] types = new Class [] {
            java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class
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
    botonAnterior.setOpaque(false);
    botonAnterior.addActionListener(new java.awt.event.ActionListener() {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            botonAnteriorActionPerformed(evt);
        }
    });

    botonSiguiente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/playback_ff.png"))); // NOI18N
    botonSiguiente.setToolTipText("Siguiente Canción");
    botonSiguiente.setOpaque(false);
    botonSiguiente.setPreferredSize(new java.awt.Dimension(48, 23));
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
    botonPlayPausaBiblioteca.setOpaque(false);
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
        .addGap(0, 220, Short.MAX_VALUE)
    );
    panelMarquesina1Layout.setVerticalGroup(
        panelMarquesina1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGap(0, 0, Short.MAX_VALUE)
    );

    jLabel11.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel11.setText("N. Canciones:");

    jLabel12.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
    jLabel12.setText("0");

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
                    .addComponent(panelMarquesina1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGap(18, 79, Short.MAX_VALUE)
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
                .addComponent(botonAnterior, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                .addComponent(botonSiguiente, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                .addComponent(botonPlayPausaBiblioteca, javax.swing.GroupLayout.DEFAULT_SIZE, 31, Short.MAX_VALUE)
                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filtro1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9)
                    .addComponent(jToggleButton1))
                .addComponent(panelMarquesina1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(jSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGap(18, 18, 18)
            .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 513, Short.MAX_VALUE)
            .addGap(8, 8, 8)
            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jButton4)
                .addComponent(jLabel11)
                .addComponent(jLabel12))
            .addGap(1, 1, 1))
    );

    jTabbedPane1.addTab("Biblioteca", jPanel4);

    lafList.setModel(new javax.swing.AbstractListModel() {
        String[] strings = { "S.O.", "Acryl", "Aero", "Aluminium", "Bernstein", "Fast", "Graphite", "HiFi", "Luna", "McWin", "Mint", "Noire", "Smart\"", "Texture" };
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

    logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/gui/iconos/goeard.png"))); // NOI18N

    javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
    jPanel3.setLayout(jPanel3Layout);
    jPanel3Layout.setHorizontalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                .addComponent(jButton3)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(55, 55, 55)
                            .addComponent(jLabel3))
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGap(25, 25, 25)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGap(45, 45, 45)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jLabel7)
                        .addComponent(directorioDescarga, javax.swing.GroupLayout.PREFERRED_SIZE, 475, javax.swing.GroupLayout.PREFERRED_SIZE))))
            .addContainerGap(274, Short.MAX_VALUE))
        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
            .addGap(0, 0, Short.MAX_VALUE)
            .addComponent(logo))
    );
    jPanel3Layout.setVerticalGroup(
        jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
        .addGroup(jPanel3Layout.createSequentialGroup()
            .addGap(40, 40, 40)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jLabel3)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel3Layout.createSequentialGroup()
                    .addComponent(directorioDescarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jButton3))
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 228, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 133, Short.MAX_VALUE)
            .addComponent(logo, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE))
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
        if (bibliotecaReproduciendo()) {
            Thread hiloSig = new Thread() {
                @Override
                public void run() {
                    try {
                        int fila = 0;
                        if (jToggleButton1.isSelected()) {
                            Random rn = new Random();
                            int nAzar = rn.nextInt(jTable3.getRowCount());
                            fila = nAzar;
                            controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(nAzar, 6).toString()).toString()));
                            controlador.play();
                            leerVolumen();
                            cancionActual = nAzar;
                        } else {
                            if (cancionActual + 1 == jTable3.getRowCount()) {
                                fila = 0;
                                controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(0, 6).toString()).toString()));
                                controlador.play();
                                leerVolumen();
                                cancionActual = 0;
                            } else {
                                fila = cancionActual + 1;
                                controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(cancionActual + 1, 6).toString()).toString()));
                                controlador.play();
                                leerVolumen();
                                cancionActual++;
                            }
                        }
                        jTable3.setRowSelectionInterval(fila, fila);
                        jTable3.scrollRectToVisible(jTable3.getCellRect(fila, 0, false));
                        if ("".equals(jTable3.getModel().getValueAt(fila, 0).toString())) {
                            pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(fila, 6).toString(), jTable3.getModel().getValueAt(fila, 2).toString(), jTable3.getModel().getValueAt(fila, 3).toString());
                        } else {
                            pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(fila, 0).toString() + " - " + jTable3.getModel().getValueAt(fila, 1).toString(), jTable3.getModel().getValueAt(fila, 2).toString(), jTable3.getModel().getValueAt(fila, 3).toString());
                        }
                    } catch (BasicPlayerException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    } finally {
                        this.interrupt();
                    }
                }
            };
            hiloSig.start();
        }
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
                    for (int i = 0; i < jTable3.getColumnCount(); i++) {
                        cancionSeleccionada.add(jTable3.getValueAt(jTable3.getSelectedRow(), i));
                    }
                    if (busquedaReproduciendo()) {
                        pausarReproduccionBusqueda();
                    }
                    try {
                        controlador.stop();
                        controlador.open(new File(mapaTemas.get(cancionSeleccionada.get(6).toString()).toString()));
                        controlador.play();
                        leerVolumen();
                        cancionActual = jTable3.getSelectedRow();
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
                if (cancionActual == -1) {
                    controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(0, 6).toString()).toString()));
                    controlador.play();
                    leerVolumen();
                    cancionActual = 0;
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
            Thread hiloAnt = new Thread() {
                @Override
                public void run() {
                    try {
                        int fila = 0;
                        if (jToggleButton1.isSelected()) {
                            Random rn = new Random();
                            int nAzar = rn.nextInt(jTable3.getRowCount());
                            fila = nAzar;
                            controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(nAzar, 6).toString()).toString()));
                            controlador.play();
                            leerVolumen();
                            cancionActual = nAzar;
                        } else {
                            if (cancionActual == 0) {
                                fila = jTable3.getRowCount() - 1;
                                controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(jTable3.getRowCount() - 1, 6).toString()).toString()));
                                controlador.play();
                                leerVolumen();
                                cancionActual = jTable3.getRowCount() - 1;
                            } else {
                                fila = cancionActual - 1;
                                controlador.open(new File(mapaTemas.get(jTable3.getModel().getValueAt(cancionActual - 1, 6).toString()).toString()));
                                controlador.play();
                                leerVolumen();
                                cancionActual--;
                            }
                        }
                        jTable3.setRowSelectionInterval(fila, fila);
                        jTable3.scrollRectToVisible(jTable3.getCellRect(fila, 0, false));
                        if ("".equals(jTable3.getModel().getValueAt(fila, 0).toString())) {
                            pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(fila, 6).toString(), jTable3.getModel().getValueAt(fila, 2).toString(), jTable3.getModel().getValueAt(fila, 3).toString());
                        } else {
                            pintarMarquesinaBiblioteca(jTable3.getModel().getValueAt(fila, 0).toString() + " - " + jTable3.getModel().getValueAt(fila, 1).toString(), jTable3.getModel().getValueAt(fila, 2).toString(), jTable3.getModel().getValueAt(fila, 3).toString());
                        }
                    } catch (BasicPlayerException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage());
                    } finally {
                        this.interrupt();
                    }
                }
            };
            hiloAnt.start();
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
                        cancionActual = jTable3.getSelectedRow();
                    } catch (BasicPlayerException ex) {
                        ex.printStackTrace();
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
        this.loading.setVisible(false);
        this.buscando = false;
    }//GEN-LAST:event_botonCancelarActionPerformed

    private void botonBuscarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_botonBuscarActionPerformed
        try {
            if (hiloTiempo != null) {
                detenerTiempo();
            }
            for (Thread thread : listaHilos) {
                thread.interrupt();
            }
            this.numeroResultados.setText("0");
            buscando = true;
            System.out.println("comenzo la busqueda de: " + texto.getText());
            this.busquedaActual = texto.getText();
            if (texto.getText().equals("")) {
                JOptionPane.showMessageDialog(rootPane, "Ingrese Un Nombre o Título A Buscar");
            } else {
                this.limpiarTabla();
                Thread hilo = new Thread() {
                    @Override
                    public void run() {
                        try {
                            final String busquedaHilo = texto.getText();
                            loading.setVisible(true);
                            correrTiempo();
                            //System.out.println(BuscarGoear.getInstancia().cantidadPaginasResultado(texto.getText().replace(" ", "-")));
                            for (int i = 0; i <= BuscarGoear.getInstancia().cantidadPaginasResultado(texto.getText().replace(" ", "-")); i++) {
                                if (buscando) {
                                    final int j = i;
                                    System.out.println("numero de paginas resultados encontradas: " + j);
                                    Thread hiloPaginaResultado = new Thread() {
                                        @Override
                                        public void run() {
                                            System.out.println("corriendo hilo de busqueda..");
                                            List<CancionDTO> lista = BuscarGoear.getInstancia().buscar(texto.getText().replace(" ", "-"), j, j);
                                            System.out.println("hilo termino de buscar, recolectando info.");
                                            for (int j = 0; j < lista.size(); j++) {
                                                final String[] vector = {lista.get(j).getNombre(), lista.get(j).getDuracion(), lista.get(j).getCalidad(), lista.get(j).getId()};
                                                Thread hiloBusqueda = new Thread() {
                                                    @Override
                                                    public void run() {
                                                        final DefaultTableModel modelo = (DefaultTableModel) jTable1.getModel();
                                                        if (busquedaHilo.equals(busquedaActual)) {
                                                            try {
                                                                SwingWorker worker = new SwingWorker<Void, Integer>() {
                                                                    @Override
                                                                    protected Void doInBackground() throws Exception {
                                                                        if (busquedaHilo.equals(busquedaActual)) {
                                                                            modelo.addRow(vector);
                                                                        } else {
                                                                            System.out.println("se cancelo");
                                                                        }
                                                                        return null;
                                                                    }
                                                                };
                                                                worker.execute();
                                                            } catch (Exception ex) {
                                                                System.out.println("error por aca");
                                                                JOptionPane.showMessageDialog(rootPane, ex.getMessage());
                                                            }
                                                            //sorter.modelStructureChanged();
                                                            int numeroR = Integer.parseInt(numeroResultados.getText());
                                                            numeroResultados.setText((numeroR + 1) + "");
                                                            this.interrupt();
                                                        }
                                                    }
                                                };
                                                hiloBusqueda.start();
                                            }
                                            this.interrupt();
                                        }
                                    };
                                    hiloPaginaResultado.start();
                                }
                            }
                            loading.setVisible(false);
                            detenerTiempo();
                            buscando = false;
                        } catch (Exception ex) {
                            System.out.println("tire errorrrrrrrrrrrrrrrr al buscar");
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(rootPane, ex.getMessage());
                        } finally {
                            this.interrupt();
                        }
                    }
                };
                this.listaHilos.add(hilo);
                hilo.start();
            }

        } catch (Exception ex) {
            System.out.println("tire error al buscarrrrrr.................");
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
                    final String linkMP3 = BuscarGoear.getInstancia().linkDescarga(cancionSeleccionada.get(3).toString());
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
                                            link = new URL(BuscarGoear.getInstancia().linkDescarga(cancionSeleccionada.get(3).toString()));
                                        } else {
                                            codigo = 200;
                                            url = link.toString();
                                        }
                                    }
                                    this.interrupt();
                                } catch (IOException ex) {
                                    ex.printStackTrace();
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
                JMenuItem itemReproducir = new JMenuItem("Reproducir");
                UIManager.getLookAndFeelDefaults().put("MenuItem.foreground", Color.BLACK);
                UIManager.getLookAndFeelDefaults().put("MenuItem.opaque", true);
                JMenuItem itemDescargar = new JMenuItem("Descargar");
                itemReproducir.setIcon(ImageHelper.loadImage("iconos/p.png"));
                itemDescargar.setIcon(ImageHelper.loadImage("iconos/Download.png"));
                ActionListener rListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        Thread hiloPlay = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    controlador2.stop();
                                    pausarReproduccionBiblioteca();
                                    marquesinaCancion.setSize(panelMarquesina.getSize());
                                    marquesinaCancion.setOpaque(false);
                                    marquesinaCancion.removeAll();
                                    JLabel label = new JLabel();
                                    label.setText("Cargando..");
                                    label.setForeground(Color.WHITE);
                                    marquesinaCancion.add(label);
                                    marquesinaCancion.setScrollFrequency(9);
                                    marquesinaCancion.startScrolling();
                                    panelMarquesina.add(marquesinaCancion);
                                    goearD.revalidate();
                                    goearD.repaint();
                                    URL linkMP3 = new URL(BuscarGoear.getInstancia().linkDescarga(cancionSeleccionada.get(3).toString()));
                                    int codigo = 410;
                                    while (codigo == 410) {
                                        HttpURLConnection connection = (HttpURLConnection) linkMP3.openConnection();
                                        connection.connect();
                                        if (connection.getResponseCode() == 410) {
                                            System.out.println("intento reproducir..");
                                            linkMP3 = new URL(BuscarGoear.getInstancia().linkDescarga(cancionSeleccionada.get(3).toString()));
                                        } else {
                                            codigo = 200;
                                        }
                                    }
                                    controlador2.open(linkMP3);
                                    leerVolumenBusqueda();
                                    controlador2.play();
                                    label.setText(cancionSeleccionada.get(0).toString());
                                    botonPlay.setEnabled(true);
                                    this.interrupt();
                                } catch (IOException | BasicPlayerException ex) {
                                    ex.printStackTrace();
                                    System.out.println("error por aca3");
                                    JOptionPane.showMessageDialog(null, ex.getMessage());
                                } finally {
                                    this.interrupt();
                                }
                            }
                        };
                        hiloPlay.start();
                    }
                };
                ActionListener dListener = new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent event) {
                        try {
                            Thread hiloDescarga = new Thread() {
                                @Override
                                public void run() {
                                    final String linkMP3 = BuscarGoear.getInstancia().linkDescarga(cancionSeleccionada.get(3).toString());

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
                                                        link = new URL(BuscarGoear.getInstancia().linkDescarga(cancionSeleccionada.get(3).toString()));
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

                                    this.interrupt();
                                }
                            };
                            hiloDescarga.start();
                        } catch (Exception ex) {
                            System.out.println("error por aca4");
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
            java.util.logging.Logger.getLogger(GoearD.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(GoearD.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(GoearD.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(GoearD.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GoearD().setVisible(true);
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
    private javax.swing.JLabel logo;
    private javax.swing.JLabel numeroResultados;
    private javax.swing.JPanel panelMarquesina;
    private javax.swing.JPanel panelMarquesina1;
    private javax.swing.JTextField texto;
    private javax.swing.JLabel tiempo;
    // End of variables declaration//GEN-END:variables

    private void setIcon() {
        BufferedImage image;
        try {
            image = ImageIO.read(this.getClass().getResource("iconos/goearlogo.png"));
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
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", UIManager.getSystemLookAndFeelClassName());
                    this.escribirOpciones();
                    break;
                case 1:
                    com.jtattoo.plaf.acryl.AcrylLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.acryl.AcrylLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.acryl.AcrylLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 2:
                    com.jtattoo.plaf.aero.AeroLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.aero.AeroLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.aero.AeroLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 3:
                    com.jtattoo.plaf.aluminium.AluminiumLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.aluminium.AluminiumLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 4:
                    com.jtattoo.plaf.bernstein.BernsteinLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.bernstein.BernsteinLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 5:
                    com.jtattoo.plaf.fast.FastLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.fast.FastLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.fast.FastLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 6:
                    com.jtattoo.plaf.graphite.GraphiteLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.graphite.GraphiteLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 7:
                    com.jtattoo.plaf.hifi.HiFiLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard2.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("blanco");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.hifi.HiFiLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 8:
                    com.jtattoo.plaf.luna.LunaLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.luna.LunaLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.luna.LunaLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 9:
                    com.jtattoo.plaf.mcwin.McWinLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.mcwin.McWinLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.mcwin.McWinLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 10:
                    com.jtattoo.plaf.mint.MintLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.mint.MintLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.mint.MintLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 11:
                    com.jtattoo.plaf.noire.NoireLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.noire.NoireLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard2.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.noire.NoireLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 12:
                    com.jtattoo.plaf.smart.SmartLookAndFeel.setTheme(theme);
                    UIManager.setLookAndFeel("com.jtattoo.plaf.smart.SmartLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/1362390121_sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.smart.SmartLookAndFeel");
                    this.escribirOpciones();
                    break;
                case 13:
                    com.jtattoo.plaf.texture.TextureLookAndFeel.setCurrentTheme(cargarPropiedadesTexture());
                    UIManager.setLookAndFeel("com.jtattoo.plaf.texture.TextureLookAndFeel");
                    this.logo.setIcon(ImageHelper.loadImage("iconos/goeard2.png"));
                    this.jLabel13.setIcon(ImageHelper.loadImage("iconos/sound_high.png"));
                    iconosBiblioteca("");
                    SwingUtilities.updateComponentTreeUI(this);
                    opciones.put("theme", "com.jtattoo.plaf.texture.TextureLookAndFeel");
                    this.escribirOpciones();
                    break;
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
                    //sorter2.modelStructureChanged();
                    mapaTemas.put(file.getFileName().toString(), file.toRealPath());
                    int numeroCanciones = Integer.parseInt(jLabel12.getText());
                    jLabel12.setText((numeroCanciones + 1) + "");
                } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException ex) {
                    JOptionPane.showMessageDialog(null, "Error Al Leer MP3: " + nombreArchivo);
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }

    private void pintarMarquesinaBiblioteca(String nombre, String duracion, String calidad) {
        marquesinaCancion2.setSize(panelMarquesina1.getSize());
        marquesinaCancion2.setOpaque(false);
        marquesinaCancion2.removeAll();
        JLabel label = new JLabel();
        label.setText(nombre + " - " + duracion + " - " + calidad + " kbps");
        label.setForeground(Color.WHITE);
        marquesinaCancion2.add(label);
        marquesinaCancion2.setScrollFrequency(9);
        marquesinaCancion2.startScrolling();
        panelMarquesina1.removeAll();
        panelMarquesina1.add(marquesinaCancion2);
        goearD.revalidate();
        goearD.repaint();
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
}
