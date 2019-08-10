package Agentes;

import bd.operaciones;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.List;
//import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologia.ElearnigOntology;
import ontologia.UnidadDeConocimiento;
import ontologia.UnidadDeConocimientoCreada;
import ontologia.UnidadesDeConocimientos;
import ontologia.UnidadesDeConocimientosCreada;

public class AgenteGestionadorDeUnidadesDeConocimiento extends Agent {

    private final operaciones baseDatos = new operaciones();
    private final Codec codec = new SLCodec();
    private final Ontology ontologia = ElearnigOntology.getInstance();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontologia);
        this.addBehaviour(new crearUnidad());
        this.addBehaviour(new enviarUnidadesDeConocimientos());
    }

    private class enviarUnidadesDeConocimientos extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteInteraccionConElUsuario");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchContent("unidades"));
            ACLMessage msg = myAgent.receive(mt);
            if(msg != null){
                List unidades = baseDatos.obtenerUnidadesDeConocimientos();
                UnidadesDeConocimientos unidadesDeConocimientos = new UnidadesDeConocimientos();
                unidadesDeConocimientos.setUnidadesDeConocimientos(unidades);
                UnidadesDeConocimientosCreada unidadesDeConocimientosCreada = new UnidadesDeConocimientosCreada();
                unidadesDeConocimientosCreada.setUnidades(unidadesDeConocimientos);
                ACLMessage reply = msg.createReply();
                reply.setLanguage(codec.getName());
                reply.setOntology(ontologia.getName());
                reply.setPerformative(ACLMessage.INFORM);
                try {
                    getContentManager().fillContent(reply, unidadesDeConocimientosCreada);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteGestionadorDeUnidadesDeConocimiento.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.myAgent.send(reply);
            }
        }

    }

    private class crearUnidad extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteInteraccionConElUsuario");
            //MessageTemplate MatchContent = MessageTemplate.MatchContent("crear");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchOntology(ontologia.getName()));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    ContentElement ce = getContentManager().extractContent(msg);
                    if (ce instanceof UnidadDeConocimientoCreada) {
                        UnidadDeConocimientoCreada unidadDeConocimientoCreada = (UnidadDeConocimientoCreada) ce;
                        UnidadDeConocimiento unidadDeConocimiento = unidadDeConocimientoCreada.getUnidadDeConocimiento();
                        baseDatos.guardarUnidadDeConocimiento(unidadDeConocimiento.getTema());
                        System.out.println("Unidad de conocimiento creada");
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.addReceiver(id);
                        reply.setContent("creado");
                        this.myAgent.send(reply);
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteGestionadorDeUnidadesDeConocimiento.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                block();
            }
        }
    }
}
