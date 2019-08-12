/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bd;

import jade.util.leap.ArrayList;
import jade.util.leap.List;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import ontologia.*;

/**
 *
 * @author johan
 */
public class operaciones {

    private final conexion conexion = new conexion();

    public boolean guardarPreguntaSimulacro(Pregunta pregunta) {
        conexion.conectar();
        String sql = "insert into preguntaSimulacro values(NULL,'" + pregunta.getEnunciado()
                + "','" + pregunta.getOpcion1()
                + "','" + pregunta.getOpcion2()
                + "','" + pregunta.getOpcion3()
                + "','" + pregunta.getOpcion4()
                + "','" + pregunta.getRespuestaCorrecta()
                + "','" + pregunta.getNivelDificultad()
                + "','" + pregunta.getTema() + "')";
        try {
            conexion.consulta.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(operaciones.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                conexion.consulta.close();
                conexion.conexion.close();
            } catch (SQLException e) {
            }
        }
        return true;
    }

    boolean guardarPreguntaEvaluacion(Pregunta pregunta, String tema) {
        conexion.conectar();
        String sql = "insert into preguntaEvaluacion values('" + pregunta.getEnunciado()
                + "','" + pregunta.getOpcion1()
                + "','" + pregunta.getOpcion2()
                + "','" + pregunta.getOpcion3()
                + "','" + pregunta.getOpcion4()
                + "','" + pregunta.getRespuestaCorrecta()
                + "','" + pregunta.getNivelDificultad()
                + "','" + tema + "')";
        try {
            conexion.consulta.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(operaciones.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                conexion.consulta.close();
                conexion.conexion.close();
            } catch (SQLException e) {
            }
        }
        return true;
    }

    public boolean guardarUnidadDeConocimiento(String tema) {
        conexion.conectar();
        String sql = "insert into unidadDeConocimiento values('" + tema + "')";
        try {
            conexion.consulta.execute(sql);
        } catch (SQLException ex) {
            Logger.getLogger(operaciones.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } finally {
            try {
                conexion.consulta.close();
                conexion.conexion.close();
            } catch (SQLException e) {
            }
        }
        return true;
    }

    boolean guardarEvaluacion(Evaluacion evaluacion, String tema) {
        String sql = "insert into preguntaSimulacro values(" + evaluacion.getCalificacion()
                + ",'" + tema + "')";
        try {
            conexion.consulta.execute(sql);
            for (int i=0; i< evaluacion.getListaDePreguntas().size(); i++){
                // sql = "insert into preguntaXevaluacion values(" + ;
            }
        } catch (SQLException ex) {
            Logger.getLogger(operaciones.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    List obtenerPreguntasSimulacro(String nivelDificultad) {
        List preguntas = new ArrayList();
        conexion.conectar();
        String sql = "select * from preguntaSimulacro";
        try {
            ResultSet resultado = conexion.consulta.executeQuery(sql);
            if (resultado != null) {
                int numeroColumna = resultado.getMetaData().getColumnCount();
                while (resultado.next()) {
                    ///
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(operaciones.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                conexion.consulta.close();
                conexion.conexion.close();
            } catch (SQLException e) {
            }
        }
        return preguntas;
    }
    
    public List obtenerUnidadesDeConocimientos(){
        List unidades = new ArrayList();
        conexion.conectar();
        String sql = "Select * from unidadDeConocimiento";
        try {
            ResultSet resultado = conexion.consulta.executeQuery(sql);
            while(resultado.next()){
                UnidadDeConocimiento unidad = new UnidadDeConocimiento();
                unidad.setTema(resultado.getString("tema"));
                unidades.add(unidad);
            }
        } catch (SQLException ex) {
            Logger.getLogger(operaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return unidades;
    }

    public Simulacro obtenerSimulacro(String unidad) {
        conexion.conectar();
        Simulacro simulacro = null;
        String sql = "Select * from simulacro WHERE tema=" + unidad;
        try {
            ResultSet resultado = conexion.consulta.executeQuery(sql);
            while(resultado.last()){
                simulacro = new Simulacro();
                simulacro.setTema(resultado.getString("tema"));
                simulacro.setAnalisis(resultado.getString("analisis"));
                simulacro.setCalificacion(resultado.getInt("calificacion"));
                simulacro.setNivelDificultad(resultado.getString("nivelDificultad"));
            }
        } catch (SQLException ex) {
            Logger.getLogger(operaciones.class.getName()).log(Level.SEVERE, null, ex);
        }
        return simulacro;
    }
}
