package Agentes;

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
//import java.util.List;
import jade.util.leap.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologia.*;

public class AgenteInteraccionConElUsuario extends Agent {

    Scanner entrada = new Scanner(System.in);
    private final Codec codec = new SLCodec();
    private final Ontology ontologia = ElearnigOntology.getInstance();
    private boolean hacerSimulacro = false;

    @Override
    protected void setup() {
        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontologia);
        this.addBehaviour(new menu());
    }

    private class respuestaCreacionPreguntaSimulacro extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeSimulacros");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchContent("creado"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                this.myAgent.addBehaviour(new menu());
            } else {
                block();
            }
        }
    }

    private class solicitarNombresUnidadConocimiento extends OneShotBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeUnidadesDeConocimiento");
            ACLMessage mensaje = new ACLMessage();
            mensaje.addReceiver(id);
            mensaje.setPerformative(ACLMessage.INFORM);
            mensaje.setContent("unidades");
            this.myAgent.send(mensaje);
            // this.myAgent.addBehaviour(new crearPreguntaSimulacro());
        }
    }

    private class crearPreguntaSimulacro extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeUnidadesDeConocimiento");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchOntology(ontologia.getName()));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    ContentElement ce = getContentManager().extractContent(msg);
                    if (ce instanceof UnidadesDeConocimientosCreada) {
                        UnidadesDeConocimientosCreada unidadesCreada = (UnidadesDeConocimientosCreada) ce;
                        UnidadesDeConocimientos unidades = unidadesCreada.getUnidades();
                        List unidadesDeConocimientos = unidades.getUnidadesDeConocimientos();
                        System.out.println("Para crear una pregunta de simulacro\n"
                                + "se debe seleccionar un tema para asociarlo a la pregunta");
                        System.out.println("Lista de tema que hay, seleccione una opcion");
                        for (int i = 0; i < unidadesDeConocimientos.size(); i++) {
                            UnidadDeConocimiento unidad = (UnidadDeConocimiento) unidadesDeConocimientos.get(i);
                            System.out.println(i + 1 + ". " + unidad.getTema());
                        }
                        int opcion = entrada.nextInt();
                        Pregunta pregunta = new Pregunta();
                        UnidadDeConocimiento unidad = (UnidadDeConocimiento) unidadesDeConocimientos.get(opcion - 1);
                        //datos para la creacion de la pregunta
                        System.out.println("Ingresar los datos para la pregunta");
                        System.out.println("Ingrese enunciado");
                        String next;
                        next = entrada.next();
                        pregunta.setEnunciado(next);
                        System.out.println("Ingrese opcion 1");
                        next = entrada.next();
                        pregunta.setOpcion1(next);
                        System.out.println("Ingrese opcion 2");
                        next = entrada.next();
                        pregunta.setOpcion2(next);
                        System.out.println("Ingrese opcion 3");
                        next = entrada.next();
                        pregunta.setOpcion3(next);
                        System.out.println("Ingrese opcion 4");
                        next = entrada.next();
                        pregunta.setOpcion4(next);
                        System.out.println("Ingrese la opcion correcta");
                        next = entrada.next();
                        pregunta.setRespuestaCorrecta(next);
                        System.out.println("Ingrese nivel de dificultad\n"
                                + "facil\n" + "medio\n" + "dificil");
                        next = entrada.next();
                        pregunta.setNivelDificultad(next);
                        System.out.println("--fin--");
                        pregunta.setTema(unidad.getTema());
                        PreguntaCreada preguntaCreada = new PreguntaCreada();
                        preguntaCreada.setPregunta(pregunta);

                        //enviar al agente simulacro
                        ACLMessage mensaje = new ACLMessage();
                        id = new AID();
                        id.setLocalName("AgenteGestionadorDeSimulacros");
                        mensaje.addReceiver(id);
                        mensaje.setLanguage(codec.getName());
                        mensaje.setOntology(ontologia.getName());
                        mensaje.setPerformative(ACLMessage.INFORM);
                        getContentManager().fillContent(mensaje, preguntaCreada);
                        this.myAgent.send(mensaje);
                        this.myAgent.addBehaviour(new respuestaCreacionPreguntaSimulacro());
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                block();
            }
        }
    }

    private class respuestaCreacionUDC extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeUnidadesDeConocimiento");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchContent("creado"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                this.myAgent.addBehaviour(new menu());
            } else {
                block();
            }
        }
    }

    private class crearUnidadConocimiento extends OneShotBehaviour {

        @Override
        public void action() {
            System.out.println("Ingresar nombre del tema");
            String tema = entrada.next();
            UnidadDeConocimiento unidadDeConocimiento = new UnidadDeConocimiento();
            unidadDeConocimiento.setTema(tema);
            UnidadDeConocimientoCreada unidadDeConocimientoCreada = new UnidadDeConocimientoCreada();
            unidadDeConocimientoCreada.setUnidadDeConocimiento(unidadDeConocimiento);
            ACLMessage mensaje = new ACLMessage();
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeUnidadesDeConocimiento");
            mensaje.addReceiver(id);
            mensaje.setLanguage(codec.getName());
            mensaje.setOntology(ontologia.getName());
            mensaje.setPerformative(ACLMessage.INFORM);
            try {
                getContentManager().fillContent(mensaje, unidadDeConocimientoCreada);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
            getAgent().send(mensaje);
            this.myAgent.addBehaviour(new respuestaCreacionUDC());
        }
    }

    private class presentarSimulacro extends CyclicBehaviour {

        @Override
        public void action() {                      
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeUnidadesDeConocimiento");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchOntology(ontologia.getName()));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    ContentElement ce = getContentManager().extractContent(msg);
                    if (ce instanceof UnidadesDeConocimientosCreada) {                        
                        UnidadesDeConocimientosCreada unidadesCreada = (UnidadesDeConocimientosCreada) ce;
                        UnidadesDeConocimientos unidades = unidadesCreada.getUnidades();
                        List unidadesDeConocimientos = unidades.getUnidadesDeConocimientos();
                        System.out.println("Escoger tema de simulacro");  
                        for (int i = 0; i < unidadesDeConocimientos.size(); i++) {
                            UnidadDeConocimiento unidad = (UnidadDeConocimiento) unidadesDeConocimientos.get(i);
                            System.out.println(i + 1 + ". " + unidad.getTema());
                        }
                        int opcion = entrada.nextInt();
                        UnidadDeConocimiento unidad = (UnidadDeConocimiento) unidadesDeConocimientos.get(opcion - 1);
                        UnidadDeConocimientoCreada unidadCreada = new UnidadDeConocimientoCreada();
                        unidadCreada.setUnidadDeConocimiento(unidad);

                        // enviar al agente simulacro
                        ACLMessage mensaje = new ACLMessage();
                        id = new AID();
                        id.setLocalName("AgenteGestionadorDeSimulacros");
                        mensaje.addReceiver(id);
                        mensaje.setLanguage(codec.getName());
                        mensaje.setOntology(ontologia.getName());
                        mensaje.setPerformative(ACLMessage.INFORM);
                        getContentManager().fillContent(mensaje, unidadCreada);
                        this.myAgent.send(mensaje);
                        //TODO don't forget Oct 3
                        // this.myAgent.addBehaviour(new respuestaCreacionPreguntaSimulacro());
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                block();
            }
        }
    }

    private class menu extends OneShotBehaviour {

        @Override
        public void action() {
            boolean bandera = false;
            do {                
                System.out.println("Menu");
                System.out.println("Opciones del profesor");
                System.out.println("1. Crear unidad de conocimiento");
                System.out.println("2. Crear pregunta de simulacro");
                System.out.println("3. Crear pregunta de evaluacion");
                System.out.println("4. Crear evalucion");
                System.out.println("Opciones del estudiante");
                System.out.println("5. Presentar un simulacro");
                System.out.println("6. Presentar una evaluacion");
                System.out.println("Ingrese una opcion");
                int opcion;
                opcion = entrada.nextInt();

                switch (opcion) {
                    case 1:
                        this.myAgent.addBehaviour(new crearUnidadConocimiento());
                        break;
                    case 2:
                        this.myAgent.addBehaviour(new solicitarNombresUnidadConocimiento());
                        this.myAgent.addBehaviour(new crearPreguntaSimulacro());
                        break;
                    case 5:
                        hacerSimulacro = true;
                        this.myAgent.addBehaviour(new solicitarNombresUnidadConocimiento());
                        this.myAgent.addBehaviour(new presentarSimulacro());
                        break;
                    default:
                        System.out.println("Ingrese un numero valido");
                        bandera = true;
                        break;
                }
            } while (bandera);          

        }
    }
}
