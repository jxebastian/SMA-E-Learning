package Agentes;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import java.util.Scanner;

public class AgenteInteraccionConElUsuario extends Agent{
    @Override
    protected void setup() {
        this.addBehaviour(new menu());
    }

    private class menu extends OneShotBehaviour {
        @Override
        public void action() {
            Scanner entrada = new Scanner(System.in);
            System.out.println("Menu");
            System.out.println("1. Presentar un simulacro");
            System.out.println("2. Presentar una evaluacion");
            System.out.println("Ingrese una opcion");
            int opcion;
            opcion = entrada.nextInt();
            
            switch (opcion) {
                case 1:
                    //pedir preguntas
                    break;
                case 2:
                    //pedir preguntas
                    break;
                default:
                    System.out.println("Ingrese un numero valido");
                    break;
            }
            
        }
    }
}
