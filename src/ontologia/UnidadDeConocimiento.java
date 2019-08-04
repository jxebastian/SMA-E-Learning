package ontologia;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UnidadDeConocimiento
* @author ontology bean generator
* @version 2019/08/4, 16:13:27
*/
public class UnidadDeConocimiento implements Concept {

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

}
