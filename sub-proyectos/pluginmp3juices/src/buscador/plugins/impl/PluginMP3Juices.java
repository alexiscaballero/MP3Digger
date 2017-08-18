package buscador.plugins.impl;

import buscador.plugins.CancionDTO;
import java.util.ArrayList;
import java.util.List;
import buscador.plugins.IPluginSearch;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.mentaregex.Regex;

public class PluginMP3Juices implements IPluginSearch {

    private final List<CancionDTO> listaCanciones = new ArrayList();
    private final Parser parser = new Parser();
    private List<Thread> listaHilos = new ArrayList<Thread>();
    private boolean cargandoCanciones = false;
    private int cantidadHilosCorriendo = 0;
    
    public int cantidadPaginasResultado(String nombre) {
        int n = 0;
        try {
            parser.setResource("http://mp3juices.to/mp3/" + nombre);
            LinkRegexFilter filtro = new LinkRegexFilter("http://mp3juices.to/search/" + nombre, false);
            NodeList list = parser.parse(filtro);
            n = list.size();
        } catch (Exception ex) {
        }
        return n;
    }

    public List<CancionDTO> buscar(String buscar, int buscarDesde, int buscarHasta) throws Exception {
        Parser p1 = new Parser();
        Parser p2 = new Parser();
        Parser p3 = new Parser();
        List<CancionDTO> listaResultados = new ArrayList<CancionDTO>();
        //Definimos cada filtro.
        HasAttributeFilter filtro1 = new HasAttributeFilter("class", "song_title");
        HasAttributeFilter filtro2 = new HasAttributeFilter("class", "details");
        LinkRegexFilter filtro3 = new LinkRegexFilter("mp3juices.to/download*", false);
            for (int j = buscarDesde; j <= buscarHasta; j++) {
                p1.setResource("http://mp3juices.to/mp3/" + buscar + "/" + j);
                p2.setResource("http://mp3juices.to/mp3/" + buscar + "/" + j);
                p3.setResource("http://mp3juices.to/mp3/" + buscar + "/" + j);

                NodeList listaTitulos = p1.extractAllNodesThatMatch(filtro1);
                NodeList listaDuracionTamañoCalidad = p2.extractAllNodesThatMatch(filtro2);
                NodeList listaLinks = p3.extractAllNodesThatMatch(filtro3);

                for (int i = 0; i < listaTitulos.size(); i++) {
                    Node node1 = listaTitulos.elementAt(i + 1);
                    Node node2 = listaDuracionTamañoCalidad.elementAt(i);
                    Node node3 = listaLinks.elementAt(i);
                    if (node1 == null || node2 == null || node3 == null) {
                    } else {
                        Tag meta1 = (Tag) node1;
                        Tag meta2 = (Tag) node2;
                        LinkTag meta3 = (LinkTag) node3;
                        String titulo = meta1.toPlainTextString();
                        String[] rs = Regex.match(meta2.toPlainTextString(), "/(\\d+)/g");
                        if (rs.length >= 4) {
                            String duracion = rs[0] + ":" + rs[1];
                            String calidad;
                            if (rs.length == 5) {
                                calidad = rs[4]+" kbps";
                            } else {
                                calidad = rs[3]+" kbps";
                            }
                            String link = meta3.getAttribute("href");
                            listaResultados.add(new CancionDTO(titulo, link, duracion, calidad));
                        }
                    }
                }
            }

        return listaResultados;
    }

    @Override
    public void loadCanciones(final String texto) {
        cargandoCanciones = true;
        int numeroHilos = cantidadPaginasResultado(texto);
        listaHilos = new ArrayList<Thread>(numeroHilos);
        for (int i = 0; i < numeroHilos; i++) {
            final int j = i;
            Thread hilo = new Thread() {
                @Override
                public void run() {
                    cantidadHilosCorriendo++;
                    try {
                        listaCanciones.addAll(buscar(texto, j, j));
                    } catch (Exception ex) {
                    } finally {
                        cantidadHilosCorriendo--;
                        if (cantidadHilosCorriendo == 0) {
                            cargandoCanciones = false;
                        }
                    }
                    this.interrupt();
                }
            };
            listaHilos.add(hilo);
            hilo.start();
        }
    }

    @Override
    public List<CancionDTO> getCanciones() {
        return listaCanciones;
    }

        @Override
    public boolean terminado() {
        return !(cargandoCanciones&&hilosCorriendo());
    }

    private boolean hilosCorriendo() {
        boolean aux = false;
        int i = 0;
            while (aux == false && i < listaHilos.size()) {
                if (listaHilos.get(i)!=null) {
                    if (listaHilos.get(i).isAlive()) {
                    aux = true;
                    }
                }
                i++;
            }
        return aux;
    }
    
}
