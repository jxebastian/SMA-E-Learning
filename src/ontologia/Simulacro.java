package ontologia;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Simulacro
* @author ontology bean generator
* @version 2019/08/12, 15:45:38
*/
public class Simulacro implements Concept {

   /**
* Protege name: tema
   */
   private String tema;
   public void setTema(String value) { 
    this.tema=value;
   }
   public String getTema() {
     return this.tema;
   }

   /**
* Protege name: analisis
   */
   private String analisis;
   public void setAnalisis(String value) { 
    this.analisis=value;
   }
   public String getAnalisis() {
     return this.analisis;
   }

   /**
* Protege name: nivelDificultad
   */
   private String nivelDificultad;
   public void setNivelDificultad(String value) { 
    this.nivelDificultad=value;
   }
   public String getNivelDificultad() {
     return this.nivelDificultad;
   }

   /**
* Protege name: listaDePreguntas
   */
   private List listaDePreguntas = new ArrayList();
   public void addListaDePreguntas(Pregunta elem) { 
     List oldList = this.listaDePreguntas;
     listaDePreguntas.add(elem);
   }
   public boolean removeListaDePreguntas(Pregunta elem) {
     List oldList = this.listaDePreguntas;
     boolean result = listaDePreguntas.remove(elem);
     return result;
   }
   public void clearAllListaDePreguntas() {
     List oldList = this.listaDePreguntas;
     listaDePreguntas.clear();
   }
   public Iterator getAllListaDePreguntas() {return listaDePreguntas.iterator(); }
   public List getListaDePreguntas() {return listaDePreguntas; }
   public void setListaDePreguntas(List l) {listaDePreguntas = l; }

   /**
* Protege name: calificacion
   */
   private int calificacion;
   public void setCalificacion(int value) { 
    this.calificacion=value;
   }
   public int getCalificacion() {
     return this.calificacion;
   }

}
