package buscador.plugins.impl;

import buscador.plugins.CancionDTO;
import java.util.ArrayList;
import java.util.List;
import buscador.plugins.IPluginSearch;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.mentaregex.Regex;

public class PluginGoear implements IPluginSearch {

    private final List<CancionDTO> listaCanciones = new ArrayList();
    private final Parser parser = new Parser();
    private List<Thread> listaHilos = new ArrayList<Thread>();
    private boolean cargandoCanciones = false;
    private int cantidadHilosCorriendo = 0;

    public String linkDescarga(String id) {
        return ("http://www.goear.com/action/sound/get/" + id);
    }

    public int cantidadPaginasResultado(String nombre) {
        int n = 0;
        try {
            parser.setResource("http://www.goear.com/search/" + nombre + "/");
            HasAttributeFilter filter = new HasAttributeFilter("id", "sounds_tab");
            NodeList list = parser.parse(filter);
            String[] rs = Regex.match(list.toString(), "/: (\\d+)/g");
            System.out.println("Plugin Goear: Encontradas "+rs[2]+" canciones.");
            n = Integer.parseInt(rs[2])/25;
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
        LinkRegexFilter filtro1 = new LinkRegexFilter("listen*", false);
        HasAttributeFilter filtro2 = new HasAttributeFilter("class", "stats length");
        HasAttributeFilter filtro3 = new HasAttributeFilter("title", "Kbps");
        for (int j = buscarDesde; j <= buscarHasta; j++) {
            p1.setResource("http://www.goear.com/search/" + buscar + "/" + j);
            p2.setResource("http://www.goear.com/search/" + buscar + "/" + j);
            p3.setResource("http://www.goear.com/search/" + buscar + "/" + j);
            NodeList listaDetalles = p1.extractAllNodesThatMatch(filtro1);
            NodeList listaDuracion = p2.extractAllNodesThatMatch(filtro2);
            NodeList listaCalidad = p3.extractAllNodesThatMatch(filtro3);
            for (int i = 0; i < listaCalidad.size(); i++) {
                Node node1 = listaDetalles.elementAt(i);
                Node node2 = listaDuracion.elementAt(i);
                Node node3 = listaCalidad.elementAt(i);
                String link = "";
                String titulo = "";
                if (node1 instanceof LinkTag) {
                    LinkTag meta = (LinkTag) node1;
                    link = linkDescarga(meta.getAttribute("href").substring(28, 35));
                    try {
                        titulo = meta.getAttribute("title").substring(9);
                    } catch (NullPointerException ex) {
                    }
                }
                if (node3.toPlainTextString() == null || titulo.equals("") || link == null || node2 == null) {
                } else {
                    listaResultados.add(new CancionDTO(titulo, link, node2.toPlainTextString(), node3.toPlainTextString()));
                }
            }
        }

        return listaResultados;
    }

    @Override
    public void loadCanciones(final String texto) {
        cargandoCanciones = true;
        Thread hiloPrincipal = new Thread() {
            @Override
            public void run() {
                
                int numeroHilos = cantidadPaginasResultado(texto);
                
                listaHilos = new ArrayList<Thread>(numeroHilos);
                for (int i = 0; i <= numeroHilos; i++) {
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
                this.interrupt();
            }
        };
        hiloPrincipal.start();
    }

    @Override

    public boolean terminado() {
        return !(cargandoCanciones || hilosCorriendo());
    }

    private boolean hilosCorriendo() {
        boolean aux = false;
        int i = 0;
        while (aux == false && i < listaHilos.size()) {
            if (listaHilos.get(i) != null) {
                if (listaHilos.get(i).isAlive()) {
                    aux = true;
                }
            }
            i++;
        }
        return aux;
    }

    @Override
    public List<CancionDTO> getCanciones() {
        return listaCanciones;
    }
}
