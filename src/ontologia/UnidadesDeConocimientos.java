package ontologia;


import jade.content.*;
import jade.util.leap.*;
import jade.core.*;

/**
* Protege name: UnidadesDeConocimientos
* @author ontology bean generator
* @version 2019/08/11, 20:51:26
*/
public class UnidadesDeConocimientos implements Concept {

   /**
* Protege name: unidadesDeConocimientos
   */
   private List unidadesDeConocimientos = new ArrayList();
   public void addUnidadesDeConocimientos(UnidadDeConocimiento elem) { 
     List oldList = this.unidadesDeConocimientos;
     unidadesDeConocimientos.add(elem);
   }
   public boolean removeUnidadesDeConocimientos(UnidadDeConocimiento elem) {
     List oldList = this.unidadesDeConocimientos;
     boolean result = unidadesDeConocimientos.remove(elem);
     return result;
   }
   public void clearAllUnidadesDeConocimientos() {
     List oldList = this.unidadesDeConocimientos;
     unidadesDeConocimientos.clear();
   }
   public Iterator getAllUnidadesDeConocimientos() {return unidadesDeConocimientos.iterator(); }
   public List getUnidadesDeConocimientos() {return unidadesDeConocimientos; }
   public void setUnidadesDeConocimientos(List l) {unidadesDeConocimientos = l; }

}
