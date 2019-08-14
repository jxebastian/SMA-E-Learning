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
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.List;
import java.util.Random;
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
        this.addBehaviour(new ProtocoloICU());
    }

    private class AnalizarSimulacro extends OneShotBehaviour {
        private final Simulacro simulacro;

        public AnalizarSimulacro(Simulacro simulacro) {
            this.simulacro = simulacro;
        }

        @Override
        public void action() {
           String dificultad = this.simulacro.getNivelDificultad();
           int nota = this.simulacro.getCalificacion();
           String analisis;
           
            if (nota < 3) {
                analisis = "Debes esforzarte más, intenta repasar " + this.simulacro.getTema();
            }else if (nota < 5) {
                analisis = "Vas bien, solo falta un poco";
            }else{
                analisis = "Excelente!!! sigue asi!!";
            }
            this.simulacro.setAnalisis(analisis);
            baseDatos.guardarSimulacro(simulacro);
            SimulacroCalificado simulacroCalificado = new SimulacroCalificado();
            simulacroCalificado.setSimulacro(simulacro);
            ACLMessage mensaje = new ACLMessage();
            AID id = new AID();
            id.setLocalName("AgenteInteraccionConElUsuario");
            mensaje.addReceiver(id);
            mensaje.setLanguage(codec.getName());
            mensaje.setOntology(ontologia.getName());
            mensaje.setPerformative(ACLMessage.INFORM);
            try {
                getContentManager().fillContent(mensaje,simulacroCalificado);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
            getAgent().send(mensaje);  
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
                        baseDatos.guardarPreguntaSimulacro(pregunta);
                        ACLMessage reply = new ACLMessage();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.addReceiver(id);
                        reply.setContent("creado");
                        this.myAgent.send(reply);
                    } else if(ce instanceof UnidadDeConocimientoCreada) {
                        UnidadDeConocimientoCreada unidadEscogida = (UnidadDeConocimientoCreada) ce;
                        UnidadDeConocimiento unidad = unidadEscogida.getUnidadDeConocimiento();
                        this.myAgent.addBehaviour(new CrearSimulacro(unidad));                        
                    }else if(ce instanceof SimulacroPresentado) {
                        SimulacroPresentado simulacroPresentado = (SimulacroPresentado) ce;
                        Simulacro simulacro = simulacroPresentado.getSimulacro();
                        this.myAgent.addBehaviour(new AnalizarSimulacro(simulacro));                        
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
        private Object obj = null;
        public CrearSimulacro(UnidadDeConocimiento unidad) {
            this.unidad = unidad;
        }

        @Override
        public void action() {
            Simulacro simulacro = (Simulacro) baseDatos.obtenerSimulacro(this.unidad.getTema());
            
            if (simulacro != null) {
                if (simulacro.getCalificacion() >= 3) {
                    if (simulacro.getNivelDificultad().equals("facil")) {
                        obj = crearSimulacro("medio", this.unidad.getTema());
                    }else {
                        obj = crearSimulacro("dificil", this.unidad.getTema());
                    }
                }else{
                    obj = crearSimulacro(simulacro.getNivelDificultad(), this.unidad.getTema()); 
                }
            } else {
                obj = crearSimulacro("facil", this.unidad.getTema());
                //this.myAgent.addBehaviour(new CrearSimulacro(unidad)); 
            }
            
            if (obj != null) {
                SimulacroCreado simulacroCreado = new SimulacroCreado();
                simulacroCreado.setSimulacro((Simulacro) obj);
                ACLMessage mensaje = new ACLMessage();
                AID id = new AID();
                id.setLocalName("AgenteInteraccionConElUsuario");
                mensaje.addReceiver(id);
                mensaje.setLanguage(codec.getName());
                mensaje.setOntology(ontologia.getName());
                mensaje.setPerformative(ACLMessage.INFORM);
                try {
                    getContentManager().fillContent(mensaje, simulacroCreado);
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
                }
                getAgent().send(mensaje);
            }   
        }
        
        private Object crearSimulacro(String dificultad, String tema) {
            List preguntas = baseDatos.obtenerPreguntasParaSimulacro(dificultad, tema);
            Random rd =new Random();
            Simulacro simulacro = new Simulacro();
            int contador = 5 ;
            if (preguntas.size() >= 5) {
                while(contador > 0){
                    int indice = rd.nextInt(preguntas.size());
                    simulacro.addListaDePreguntas((Pregunta) preguntas.get(indice));
                    preguntas.remove(indice);
                    contador --;
                }
                simulacro.setNivelDificultad(dificultad);
                simulacro.setTema(tema);
            }else {
                System.out.println("No hay preguntas suficientes");
                return null;
            }
            return simulacro;
        }
    }
}
