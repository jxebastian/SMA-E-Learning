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
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
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
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("GestionadorEvaluaciones");
            sd.setName("Crear evaluaciones");

            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("GestionadorEvaluaciones");
            sd1.setName("Enviar evaluaciones");
            
            ServiceDescription sd2 = new ServiceDescription();
            sd2.setType("GestionadorEvaluaciones");
            sd2.setName("Calificar evaluaciones");
            
            dfd.addServices(sd);
            dfd.addServices(sd1);
            dfd.addServices(sd2);

            DFService.register(this, dfd);
        } catch (FIPAException e) {
        }
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontologia);
        this.addBehaviour(new ProtocoloICU());
    }
    private class CalificarEvaluacion extends OneShotBehaviour {
        private final Evaluacion evaluacion;

        public CalificarEvaluacion(Evaluacion evaluacion) {
            this.evaluacion = evaluacion;
        }

        @Override
        public void action() {
           String dificultad = this.evaluacion.getNivelDificultad();
           int nota = this.evaluacion.getCalificacion();
           String analisis;
           
            if (nota < 3) {
                analisis = "Debes esforzarte más, intenta repasar " + this.evaluacion.getTema();
            }else if (nota < 5) {
                analisis = "Vas bien, solo falta un poco";
            }else{
                analisis = "Excelente!!! sigue asi!!";
            }
            this.evaluacion.setAnalisis(analisis);
            baseDatos.guardarEvaluacion(evaluacion);
            EvaluacionCalificada evaluacionCalificada = new EvaluacionCalificada();
            evaluacionCalificada.setEvaluacion(evaluacion);
            ACLMessage mensaje = new ACLMessage();
            AID id = new AID();
            id.setLocalName("AgenteInteraccionConElUsuario");
            mensaje.addReceiver(id);
            mensaje.setLanguage(codec.getName());
            mensaje.setOntology(ontologia.getName());
            mensaje.setPerformative(ACLMessage.INFORM);
            try {
                getContentManager().fillContent(mensaje,evaluacionCalificada);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
            getAgent().send(mensaje);  
        }
    }

    private class PresentarEvaluacion extends OneShotBehaviour {
        private final String tema;
        public PresentarEvaluacion(String tema) {
            this.tema=tema;
        }

        @Override
        public void action() {
            Evaluacion evaluacion = (Evaluacion) baseDatos.obtenerEvaluacion(this.tema);
            
            if (evaluacion != null) {
                EvaluacionCreada evaluacionCreado = new EvaluacionCreada();
                evaluacionCreado.setEvaluacion((Evaluacion) evaluacion);
                ACLMessage mensaje = new ACLMessage();
                AID id = new AID();
                id.setLocalName("AgenteInteraccionConElUsuario");
                mensaje.addReceiver(id);
                mensaje.setLanguage(codec.getName());
                mensaje.setOntology(ontologia.getName());
                mensaje.setPerformative(ACLMessage.INFORM);
                try {
                    getContentManager().fillContent(mensaje, evaluacionCreado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
                }
                getAgent().send(mensaje);
            }   
        }
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
                    }else if (ce instanceof UnidadesDeConocimientosCreada) {
                        UnidadesDeConocimientosCreada unidadDeConocimientoCreada = (UnidadesDeConocimientosCreada) ce;
                        UnidadesDeConocimientos unidadesDeConocimiento = (UnidadesDeConocimientos) unidadDeConocimientoCreada.getUnidades();
                        UnidadDeConocimiento unidadDeConocimiento = (UnidadDeConocimiento) unidadesDeConocimiento.getUnidadesDeConocimientos().get(0);                        
                        this.myAgent.addBehaviour(new PresentarEvaluacion(unidadDeConocimiento.getTema()));
                    } else if (ce instanceof EvaluacionPresantada) {
                        EvaluacionPresantada evaluacionPresentada = (EvaluacionPresantada) ce;
                        this.myAgent.addBehaviour(new CalificarEvaluacion(evaluacionPresentada.getEvaluacion()));
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
