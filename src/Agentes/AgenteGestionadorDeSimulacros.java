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
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologia.ElearnigOntology;
import ontologia.*;

public class AgenteGestionadorDeSimulacros extends Agent {

    private final operaciones baseDatos = new operaciones();
    private final Codec codec = new SLCodec();
    private final Ontology ontologia = ElearnigOntology.getInstance();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontologia);
        this.addBehaviour(new ProtocoloICU());
    }

    private class ProtocoloICU extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteInteraccionConElUsuario");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchOntology(ontologia.getName()));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    ContentElement ce = getContentManager().extractContent(msg);
                    if (ce instanceof PreguntaCreada) {
                        PreguntaCreada preguntaCreada = (PreguntaCreada) ce;
                        Pregunta pregunta = preguntaCreada.getPregunta();
                        baseDatos.guardarPreguntaSimulacro(pregunta);
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("creado");
                        this.myAgent.send(reply);
                    } else if(ce instanceof UnidadDeConocimientoCreada) {
                        UnidadDeConocimientoCreada unidadEscogida = (UnidadDeConocimientoCreada) ce;
                        UnidadDeConocimiento unidad = unidadEscogida.getUnidadDeConocimiento();
                        this.myAgent.addBehaviour(new CrearSimulacro(unidad));                        
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteGestionadorDeSimulacros.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                block();
            }
        }
    }

    private class CrearSimulacro extends OneShotBehaviour {

        private UnidadDeConocimiento unidad;
        public CrearSimulacro(UnidadDeConocimiento unidad) {
            this.unidad = unidad;
        }

        @Override
        public void action() {
            
            System.out.println("Daniel x2" + this.unidad.getTema());
        }
    }
}
