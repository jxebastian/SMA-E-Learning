package ontologia;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: RecomendacionDada
* @author ontology bean generator
* @version 2019/08/9, 13:02:57
*/
public class RecomendacionDada implements Predicate {

   /**
* Protege name: recomendacionDeResultados
   */
   private RecomendacionDeSimulacro recomendacionDeResultados;
   public void setRecomendacionDeResultados(RecomendacionDeSimulacro value) { 
    this.recomendacionDeResultados=value;
   }
   public RecomendacionDeSimulacro getRecomendacionDeResultados() {
     return this.recomendacionDeResultados;
   }

}