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
import jade.util.leap.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologia.*;

public class AgenteGestionadorDeEvaluaciones extends Agent {

    private final operaciones baseDatos = new operaciones();
    private final Codec codec = new SLCodec();
    private final Ontology ontologia = ElearnigOntology.getInstance();

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontologia);
        this.addBehaviour(new ProtocoloICU());
    }

    private class CrearEvaluacion extends OneShotBehaviour {
        private final UnidadDeConocimiento unidadDeConocimiento;
        public CrearEvaluacion(UnidadDeConocimiento unidadDeConocimiento) {
            this.unidadDeConocimiento = unidadDeConocimiento;
        }

        @Override
        public void action() {
            Evaluacion evaluacion = new Evaluacion();
            evaluacion.setTema(this.unidadDeConocimiento.getTema());
            List preguntas = baseDatos.obtenerPreguntasEvaluacion(this.unidadDeConocimiento.getTema());
            ACLMessage mensaje = new ACLMessage();
            AID id = new AID();
            id.setLocalName("AgenteInteraccionConElUsuario");
            mensaje.addReceiver(id);
            if (preguntas.size() == 5){
                evaluacion.setListaDePreguntas(preguntas);
                baseDatos.guardarEvaluacion(evaluacion);
                mensaje.setContent("evaluacion creada");
            }else {
                mensaje.setContent("no se puede crear");
                System.out.println("no se puede");
            }
            this.myAgent.send(mensaje);
        }
    }

    private class CrearPregunta extends OneShotBehaviour {
        private final Pregunta pregunta;
        private CrearPregunta(Pregunta pregunta) {
            this.pregunta = pregunta;
        }

        @Override
        public void action() {
            baseDatos.guardarPreguntaEvaluacion(pregunta);
            ACLMessage mensaje = new ACLMessage();
            mensaje.setPerformative(ACLMessage.INFORM);
            AID id = new AID();
            id.setLocalName("AgenteInteraccionConElUsuario");
            mensaje.addReceiver(id);
            mensaje.setContent("creado");
            this.myAgent.send(mensaje);
        }

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
                        this.myAgent.addBehaviour(new CrearPregunta(pregunta));
                    } else if (ce instanceof UnidadDeConocimientoCreada) {
                        UnidadDeConocimientoCreada unidadDeConocimientoCreada = (UnidadDeConocimientoCreada) ce;
                        UnidadDeConocimiento unidadDeConocimiento = unidadDeConocimientoCreada.getUnidadDeConocimiento();
                        this.myAgent.addBehaviour(new CrearEvaluacion(unidadDeConocimiento));
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteGestionadorDeSimulacros.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                block();
            }
        }
    }
}
