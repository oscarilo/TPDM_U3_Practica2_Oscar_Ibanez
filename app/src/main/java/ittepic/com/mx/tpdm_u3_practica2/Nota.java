package ittepic.com.mx.tpdm_u3_practica2;

public class Nota {

    String materia,descripcion,fecha;

    public Nota(String materia, String descripcion, String fecha){
        this.materia = materia;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }// constr

    public Nota(){}

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}// class
