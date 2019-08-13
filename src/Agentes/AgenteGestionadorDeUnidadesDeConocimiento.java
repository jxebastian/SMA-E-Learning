package Agentes;

import bd.operaciones;
import jade.content.ContentElement;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.List;
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
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("GestionadorUDC");
            sd.setName("Crear unidades de conocimientos");

            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("GestionadorUDC");
            sd1.setName("Enviar unidades de conocimientos");

            dfd.addServices(sd);
            dfd.addServices(sd1);

            DFService.register(this, dfd);
        } catch (FIPAException e) {
        }

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
            if (msg != null) {
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
                        ACLMessage reply = new ACLMessage();
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
