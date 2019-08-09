package ontologia;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: PreguntaCreada
* @author ontology bean generator
* @version 2019/08/9, 13:02:56
*/
public class PreguntaCreada implements Predicate {

   /**
* Protege name: pregunta
   */
   private Pregunta pregunta;
   public void setPregunta(Pregunta value) { 
    this.pregunta=value;
   }
   public Pregunta getPregunta() {
     return this.pregunta;
   }

}
