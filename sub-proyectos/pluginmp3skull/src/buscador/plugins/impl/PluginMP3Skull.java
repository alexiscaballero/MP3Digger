package buscador.plugins.impl;

import buscador.plugins.CancionDTO;
import java.util.ArrayList;
import java.util.List;
import buscador.plugins.IPluginSearch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.LinkRegexFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

public class PluginMP3Skull implements IPluginSearch {

    public List<CancionDTO> buscar(String buscar) {
        Parser p1 = new Parser();
        Parser p2 = new Parser();
        Parser p3 = new Parser();

        List<CancionDTO> listaResultados = new ArrayList();
        //Definimos cada filtro.
        LinkRegexFilter filtro1 = new LinkRegexFilter("mp3", false);
        HasAttributeFilter filtro2 = new HasAttributeFilter("style", "font-size:15px;");
        HasAttributeFilter filtro3 = new HasAttributeFilter("class", "left");

        try {
            String link = "http://mp3skull.com/mp3/" + buscar.replace(" ", "_") + ".html";
            p1.setResource(link);
            p2.setResource(link);
            p3.setResource(link);

            NodeList listaURLS = p1.extractAllNodesThatMatch(filtro1);
            NodeList listaNombres = p2.extractAllNodesThatMatch(filtro2);
            NodeList listaCalidadDuracionTamaño = p3.extractAllNodesThatMatch(filtro3);

            for (int i = 0; i < listaNombres.size(); i++) {
                Node node1 = listaURLS.elementAt(i + 1);
                Node node2 = listaNombres.elementAt(i);
                Node node3 = listaCalidadDuracionTamaño.elementAt(i);
                String url;
                String nombre;
                String calidad;
                String duracion;
                if (node1 instanceof LinkTag) {
                    LinkTag meta = (LinkTag) node1;
                    url = meta.getAttribute("href");
                    if (node2 instanceof Div) {
                        Div meta2 = (Div) node2;
                        nombre = meta2.getStringText().substring(3, meta2.getStringText().length() - 8);
                        if (node3 instanceof Div) {
                            Div meta3 = (Div) node3;
                            String temp = meta3.toPlainTextString().replace("\n", "").replace("\t", "");
                            List<String> numeros = new ArrayList();
                            Pattern p = Pattern.compile("\\d+");
                            Matcher m = p.matcher(temp);
                            while (m.find()) {
                                numeros.add(m.group());
                            }
                            if (numeros.size() == 4) {
                                calidad = numeros.get(0)+" kbps";
                                duracion = numeros.get(1)+":"+numeros.get(2).substring(0, 2);
                                listaResultados.add(new CancionDTO(nombre, url, duracion, calidad));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return listaResultados;
    }

    @Override
    public List getCanciones(final String texto) {
        return buscar(texto);
    }

}
