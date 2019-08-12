package ontologia;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: Pregunta
* @author ontology bean generator
* @version 2019/08/11, 20:51:26
*/
public class Pregunta implements Concept {

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
* Protege name: opcion2
   */
   private String opcion2;
   public void setOpcion2(String value) { 
    this.opcion2=value;
   }
   public String getOpcion2() {
     return this.opcion2;
   }

   /**
* Protege name: opcion3
   */
   private String opcion3;
   public void setOpcion3(String value) { 
    this.opcion3=value;
   }
   public String getOpcion3() {
     return this.opcion3;
   }

   /**
* Protege name: opcion1
   */
   private String opcion1;
   public void setOpcion1(String value) { 
    this.opcion1=value;
   }
   public String getOpcion1() {
     return this.opcion1;
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
* Protege name: respuestaCorrecta
   */
   private String respuestaCorrecta;
   public void setRespuestaCorrecta(String value) { 
    this.respuestaCorrecta=value;
   }
   public String getRespuestaCorrecta() {
     return this.respuestaCorrecta;
   }

   /**
* Protege name: opcion4
   */
   private String opcion4;
   public void setOpcion4(String value) { 
    this.opcion4=value;
   }
   public String getOpcion4() {
     return this.opcion4;
   }

   /**
* Protege name: enunciado
   */
   private String enunciado;
   public void setEnunciado(String value) { 
    this.enunciado=value;
   }
   public String getEnunciado() {
     return this.enunciado;
   }

}
