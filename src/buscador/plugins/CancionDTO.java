/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package buscador.plugins;

/**
 *
 * @author Alexis
 */
public class CancionDTO {

    private String nombre;
    private String id;
    private String duracion;
    private String calidad;
    
    public CancionDTO(String nombre, String id, String duracion, String calidad) {
        this.nombre=nombre;
        this.id=id;
        this.duracion=duracion;
        this.calidad=calidad;
    }

    public String getNombre() {
        return nombre;
    }

    public String getId() {
        return id;
    }

    public String getDuracion() {
        return duracion;
    }

    public String getCalidad() {
        return calidad;
    }

    
}
