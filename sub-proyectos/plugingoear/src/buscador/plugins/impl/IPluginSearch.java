package buscador.plugins;

import java.util.List;

public interface IPluginSearch {
    void loadCanciones(String texto);
    List<CancionDTO> getCanciones();
    boolean terminado();
}
