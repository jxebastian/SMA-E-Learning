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
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("GestionadorSimulacros");
            sd.setName("Crear simulacros");

            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("GestionadorSimulacros");
            sd1.setName("Enviar simulacros");
            
            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType("GestionadorSimulacros");
            sd2.setName("Calificar simulacros");

            ServiceDescription sd3 = new ServiceDescription();
            sd3.setType("GestionadorSimulacros");
            sd3.setName("Dar recomendaciones para los simulacros");
            
            ServiceDescription sd4 = new ServiceDescription();
            sd4.setType("GestionadorSimulacros");
            sd4.setName("Guardar simulacros");
            
            dfd.addServices(sd);
            dfd.addServices(sd1);
            dfd.addServices(sd2);
            dfd.addServices(sd3);
            dfd.addServices(sd4);

            DFService.register(this, dfd);
        } catch (FIPAException e) {
        }
        
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontologia);
        this.addBehaviour(new crearPregunta());
    }

    private class crearPregunta extends CyclicBehaviour {

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
                        ACLMessage reply = new ACLMessage();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.addReceiver(id);
                        reply.setContent("creado");
                        this.myAgent.send(reply);
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteGestionadorDeSimulacros.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                block();
            }
        }
    }
}
