#!/usr/bin/env groovy
package es.uned.lsi

import groovy.sql.Sql
import java.util.Properties

class connectSql{

  //ToDo: use .properties file
  String dbUrl = "jdbc:postgresql://postgres/ProcLen_DB"
  String dbUser 
  String dbPassword 
  String dbDriver   = "org.postgresql.Driver"
  Map dbConnParams = [url: this.dbUrl, user: this.dbUser, password: this.dbPassword, driver: this.dbDriver]

  def selectUsers(String correo){    
    def rowSelect
    Sql sql
    try {
      println "Correo: ${correo}"
      sql = Sql.newInstance(dbConnParams) 
      //println "Conexion establecida"
      rowSelect = sql.rows("""select alumno_id, public.alumnos.nombre_completo 
        from public.alumnos
        where correo=${correo};""")
      
      return rowSelect      
      
    }
    catch (Exception ex){
      ex.printStackTrace()
      println ex.toString()
    }
    finally {
      sql.close()
    }
  }

  def selectCurso(String anio, String asignatura){
    def rowSelect
    Sql sql
    try {
      sql = Sql.newInstance(dbConnParams)
      rowSelect = sql.rows("""select curso_id
        from public.cursos
        where anio=${anio} and asignatura=${asignatura}""")
      return rowSelect
    }
    catch(Exception ex) {
      ex.printStackTrace()
      println ex.toString()
    }
    finally {
      sql.close()
    }
    
  }

  def insertCommit(alumno_id, curso_id, String commit_url, commit_num_errores, commit_num_correctos){
    def rowInsert
    Sql sql
    try {
      sql = Sql.newInstance(dbConnParams) 
      //println "Conexion establecida"
      rowInsert = sql.executeInsert("""INSERT INTO public.commits(
        alumno_id, curso_id, commit_url, commit_num_errores, commit_num_correctos)
        VALUES ( ${alumno_id}, ${curso_id}, ${commit_url}, ${commit_num_errores}, ${commit_num_correctos});""")
      
      return rowInsert
      
    }
    catch (Exception ex){
      ex.printStackTrace()
      println ex.toString()
    }
    finally {
      sql.close()
    }
  }

  def insertError(commit_id, String error_url, stage_name){
    def rowInsert
    Sql sql
    try {
      sql = Sql.newInstance(dbConnParams) 
      //println "Conexion establecida"
      rowInsert = sql.executeInsert("""INSERT INTO public.commits_errores(
        commit_id, error_url, error_stage_name)
        VALUES ( ${commit_id}, ${error_url}, ${stage_name});""")
      
      return rowInsert
      
    }
    catch (Exception ex){
      ex.printStackTrace()
      println ex.toString()
    }
    finally {
      sql.close()
    }
  }

}
