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
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.util.leap.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologia.*;

public class AgenteInteraccionConElUsuario extends Agent {

    private final Scanner entrada = new Scanner(System.in);
    BufferedReader buff = new BufferedReader(new InputStreamReader(System.in));
    private DFAgentDescription[] resultados;
    private final Codec codec = new SLCodec();
    private final Ontology ontologia = ElearnigOntology.getInstance();
    private boolean hacerSimulacro = false;
    private boolean creacionPreguntaSimulacro = false;
    private boolean creacionPreguntaEvaluacion = false;
    private boolean creacionPregunta = false;
    private boolean creacionEvaluacion = false;

    @Override
    protected void setup() {
        try {
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setType("InteradorConUsuario");
            sd.setName("Presentar simulacros");

            ServiceDescription sd1 = new ServiceDescription();
            sd1.setType("InteradorConUsuario");
            sd1.setName("Presentar evaluaciones");

            dfd.addServices(sd);
            dfd.addServices(sd1);

            DFService.register(this, dfd);
        } catch (FIPAException e) {
        }
        try {
            buscarServicio();
        } catch (FIPAException ex) {
            Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
        }

        getContentManager().registerLanguage(codec);
        getContentManager().registerOntology(ontologia);
        this.addBehaviour(new menu());
        this.addBehaviour(new ProtocoloGDS());
        this.addBehaviour(new protocoloUDC());
    }

    public void buscarServicio() throws FIPAException {
        DFAgentDescription descripcion = new DFAgentDescription();
        // Todas las descripciones que encajan con la plantilla proporcionada en el DF
        resultados = DFService.search(this, descripcion);
        if (resultados.length == 0) {
            System.out.println("Ningun agente ofrece el servicio deseado");
        } else {
            for (int i = 0; i < resultados.length; ++i) {
                System.out.println("El agente " + resultados[i].getName().getLocalName() + " ofrece los siguientes servicios:");
                Iterator servicios = resultados[i].getAllServices();
                int j = 1;
                while (servicios.hasNext()) {
                    ServiceDescription servicio = (ServiceDescription) servicios.next();
                    System.out.println(j + "- " + servicio.getName());
                    j++;
                }
            }
        }
    }

    private class RespuestaCreacionEvaluacion extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeEvaluaciones");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchContent("evaluacion creada"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println(msg.getContent());
                this.myAgent.addBehaviour(new menu());
            } else {
                block();
            }
        }
    }

    private class crearEvaluacion extends OneShotBehaviour {

        private final UnidadesDeConocimientos unidades;

        private crearEvaluacion(UnidadesDeConocimientos unidades) {
            this.unidades = unidades;
        }

        @Override
        public void action() {
            List unidadesDeConocimientos = this.unidades.getUnidadesDeConocimientos();
            System.out.println("Para crear una evaluacion\n"
                    + "se debe seleccionar un tema");
            System.out.println("Lista de tema que hay, seleccione una opcion");
            for (int i = 0; i < unidadesDeConocimientos.size(); i++) {
                UnidadDeConocimiento unidad = (UnidadDeConocimiento) unidadesDeConocimientos.get(i);
                System.out.println(i + 1 + ". " + unidad.getTema());
            }
            int opcion = entrada.nextInt();
            UnidadDeConocimiento unidad = (UnidadDeConocimiento) unidadesDeConocimientos.get(opcion - 1);
            UnidadDeConocimientoCreada unidadDeConocimientoCreada = new UnidadDeConocimientoCreada();
            unidadDeConocimientoCreada.setUnidadDeConocimiento(unidad);
            ACLMessage mensaje = new ACLMessage();
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeEvaluaciones");
            mensaje.addReceiver(id);
            mensaje.setLanguage(codec.getName());
            mensaje.setOntology(ontologia.getName());
            mensaje.setPerformative(ACLMessage.INFORM);
            try {
                getContentManager().fillContent(mensaje, unidadDeConocimientoCreada);
                this.myAgent.send(mensaje);
                this.myAgent.addBehaviour(new RespuestaCreacionEvaluacion());
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private class PresentarSimulacro extends OneShotBehaviour {

        private final Simulacro simulacro;

        public PresentarSimulacro(Simulacro simulacro) {
            this.simulacro = simulacro;
        }

        @Override
        public void action() {
            System.out.println("Enunciado");
            List preguntas = simulacro.getListaDePreguntas();
            int nota = 0;
            for (int i = 0; i < preguntas.size(); i++) {
                Pregunta pregunta = (Pregunta) preguntas.get(i);
                System.out.println("pregunta #" + (i + 1));
                System.out.println(pregunta.getEnunciado());
                System.out.println("1. " + pregunta.getOpcion1());
                System.out.println("2. " + pregunta.getOpcion2());
                System.out.println("3. " + pregunta.getOpcion3());
                System.out.println("4. " + pregunta.getOpcion4());
                System.out.println("Ingrese su respuesta:");
                int opcion = entrada.nextInt();
                String respuesta = "opcion" + opcion;
                if (pregunta.getRespuestaCorrecta().equals(respuesta)) {
                    nota++;
                }
            }
            simulacro.setCalificacion(nota);
            SimulacroPresentado simulacroPresentado = new SimulacroPresentado();
            simulacroPresentado.setSimulacro(simulacro);
            ACLMessage mensaje = new ACLMessage();
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeSimulacros");
            mensaje.addReceiver(id);
            mensaje.setLanguage(codec.getName());
            mensaje.setOntology(ontologia.getName());
            mensaje.setPerformative(ACLMessage.INFORM);
            try {
                getContentManager().fillContent(mensaje, simulacroPresentado);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
            getAgent().send(mensaje);
        }
    }

    private class ProtocoloGDS extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeSimulacros");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchOntology(ontologia.getName()));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    ContentElement ce = getContentManager().extractContent(msg);
                    if (ce instanceof SimulacroCreado) {
                        SimulacroCreado simulacroCreado = (SimulacroCreado) ce;
                        Simulacro simulacro = simulacroCreado.getSimulacro();
                        this.myAgent.addBehaviour(new PresentarSimulacro(simulacro));
                    } else if (ce instanceof SimulacroCalificado) {
                        SimulacroCalificado simulacroCalificado = (SimulacroCalificado) ce;
                        Simulacro simulacro = simulacroCalificado.getSimulacro();
                        System.out.println("Tu nota fue de: " + simulacro.getCalificacion());
                        System.out.println("Recomendación: " + simulacro.getAnalisis());
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                block();
            }

        }
    }

    private class protocoloUDC extends CyclicBehaviour {

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
                        if (creacionPregunta) {
                            this.myAgent.addBehaviour(new crearPregunta(unidades));
                        } else if (creacionEvaluacion) {
                            this.myAgent.addBehaviour(new crearEvaluacion(unidades));
                        } else if (hacerSimulacro) {
                            this.myAgent.addBehaviour(new ObtenerSimulacro(unidades));
                        }
                    }
                } catch (Codec.CodecException | OntologyException ex) {
                    Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                block();
            }
        }

    }

    private class respuestaCreacionPreguntaEvaluacion extends CyclicBehaviour {

        @Override
        public void action() {
            AID id = new AID();
            id.setLocalName("AgenteGestionadorDeEvaluaciones");
            MessageTemplate mt = MessageTemplate.and(
                    MessageTemplate.MatchSender(id),
                    MessageTemplate.MatchContent("creado"));
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                System.out.println("Pregunta creada");
                this.myAgent.addBehaviour(new menu());
            } else {
                block();
            }
        }
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
        }
    }

    private class crearPregunta extends OneShotBehaviour {

        private final UnidadesDeConocimientos unidades;

        private crearPregunta(UnidadesDeConocimientos unidades) {
            this.unidades = unidades;
        }

        @Override
        public void action() {
            try {
                List unidadesDeConocimientos = this.unidades.getUnidadesDeConocimientos();
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
                String input = buff.readLine();
                pregunta.setEnunciado(input);
                System.out.println("Ingrese opcion 1");
                input = buff.readLine();
                pregunta.setOpcion1(input);
                System.out.println("Ingrese opcion 2");
                input = buff.readLine();
                pregunta.setOpcion2(input);
                System.out.println("Ingrese opcion 3");
                input = buff.readLine();
                pregunta.setOpcion3(input);
                System.out.println("Ingrese opcion 4");
                input = buff.readLine();
                pregunta.setOpcion4(input);
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
                ACLMessage mensaje = new ACLMessage();
                AID id = new AID();
                if (creacionPreguntaSimulacro) {
                    //enviar al agente simulacro
                    id.setLocalName("AgenteGestionadorDeSimulacros");
                    creacionPreguntaSimulacro = false;
                } else if (creacionPreguntaEvaluacion) {
                    id.setLocalName("AgenteGestionadorDeEvaluaciones");
                    creacionPreguntaEvaluacion = false;
                }
                creacionPregunta = false;
                mensaje.addReceiver(id);
                mensaje.setLanguage(codec.getName());
                mensaje.setOntology(ontologia.getName());
                mensaje.setPerformative(ACLMessage.INFORM);
                getContentManager().fillContent(mensaje, preguntaCreada);
                this.myAgent.send(mensaje);
                this.myAgent.addBehaviour(new respuestaCreacionPreguntaSimulacro());
                this.myAgent.addBehaviour(new respuestaCreacionPreguntaEvaluacion());

            } catch (Codec.CodecException | OntologyException | IOException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
            String tema = "";
            try {
                tema = buff.readLine();
            } catch (IOException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    private class ObtenerSimulacro extends OneShotBehaviour {

        private final UnidadesDeConocimientos unidades;

        public ObtenerSimulacro(UnidadesDeConocimientos unidades) {
            this.unidades = unidades;
        }

        @Override
        public void action() {
            try {
                List unidadesDeConocimientos = this.unidades.getUnidadesDeConocimientos();
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
                AID id = new AID();
                id.setLocalName("AgenteGestionadorDeSimulacros");
                mensaje.addReceiver(id);
                mensaje.setLanguage(codec.getName());
                mensaje.setOntology(ontologia.getName());
                mensaje.setPerformative(ACLMessage.INFORM);
                getContentManager().fillContent(mensaje, unidadCreada);
                this.myAgent.send(mensaje);
            } catch (Codec.CodecException | OntologyException ex) {
                Logger.getLogger(AgenteInteraccionConElUsuario.class.getName()).log(Level.SEVERE, null, ex);
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
                        creacionPregunta = true;
                        creacionPreguntaSimulacro = true;
                        this.myAgent.addBehaviour(new solicitarNombresUnidadConocimiento());
                        break;
                    case 3:
                        creacionPregunta = true;
                        creacionPreguntaEvaluacion = true;
                        this.myAgent.addBehaviour(new solicitarNombresUnidadConocimiento());
                        break;
                    case 4:
                        creacionEvaluacion = true;
                        this.myAgent.addBehaviour(new solicitarNombresUnidadConocimiento());
                        break;
                    case 5:
                        hacerSimulacro = true;
                        this.myAgent.addBehaviour(new solicitarNombresUnidadConocimiento());
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
