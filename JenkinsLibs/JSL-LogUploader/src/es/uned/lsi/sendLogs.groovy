#!/usr/bin/env groovy
package es.uned.lsi

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.7.1' )

import static groovyx.net.http.ContentType.JSON

import groovyx.net.http.RESTClient

def getJSONREST(String commitAuthorEmail, String cursoAnio, String cursoAsignatura){
  // ToDo:get this from .properties file
  def server = "localhost:8080"
  def jenkinsHost = "http://${server}/"
  
  println "Job: ${env.JOB_NAME} - ${env.BUILD_NUMBER}"  
  
  def projectName = "${env.JOB_NAME}"
  def buildNumber = "${env.BUILD_NUMBER}"
  
  def replacedProjectName = (projectName.replaceAll(/([^\/]+)\/([^\/]+)\/([^\/]+)/,
    { it[1] +"/pipelines/"+ it[2] + "/branches/" +it[3] }
    ))
  //println("$replacedProjectName")
  def username = "logreader"
  //def password = "password"
  def apiToken = ""
  Map<String,String> headersMap = new HashMap<String, String>(['Authorization': "Basic ${"$username:$apiToken".bytes.encodeBase64()}"])
  def jobLogREST = new RESTClient("${jenkinsHost}blue/rest/".toString() )
  //jobLogREST.auth.basic username, password
  //RESTClient.setHeaders(headersMap)
  try {
    // def response = jobLogREST.get( path : "api/json",
     //                             headers : headersMap)
    def response = jobLogREST.get( path : "organizations/jenkins/pipelines/$replacedProjectName/runs/$buildNumber/nodes",
                                  headers : headersMap)

    //println(response.getData().toString())

    def jobJSON = response.getData()
    def aciertos = 0
    def errores = 0
    def nodos_id_URL = [:]
    for (def node : jobJSON){
      if (node.result != "SUCCESS") {
        nodos_id_URL[node.displayName]=node._links.self.href
        errores += 1
      }
      else {
        aciertos += 1
      }
    }

    println "Errores: $errores"
    println "Correctos: $aciertos"
    
    def connectSql = new connectSql()
    connectSql.dbConnParams.user = env.dbUser
    connectSql.dbConnParams.password = env.dbPassword

    /* Datos del alumno */
    println "Correo: $commitAuthorEmail"
    def alumn = connectSql.selectUsers(commitAuthorEmail)
    assert alumn.size() > 0:'Error al obtener alumno de la base de datos'
    def alumnDatos = alumn[0]
    assert alumnDatos.alumno_id > 0:'Alumno no valido'

    /* Datos del curso */
    println "Curso: $cursoAnio - $cursoAsignatura"
    def cursos = connectSql.selectCurso(cursoAnio, cursoAsignatura)
    assert cursos.size() > 0:'Curso no encontrado'
    def curso = cursos[0]
    assert curso.curso_id > 0:'Curso no valido'

    /* Insertar commit y errores */
    def commitInsertd = connectSql.insertCommit(alumnDatos.alumno_id, curso.curso_id, "${env.BUILD_URL}", errores, aciertos)
    println commitInsertd
    nodos_id_URL.each { k, v ->
       println "${k}:${v}"
       connectSql.insertError(commitInsertd[0][0], "${v}", "${k}")
    }
  }
  catch (groovyx.net.http.HttpResponseException ex) {
    ex.printStackTrace()
    println ex.toString()
  }
 
}

return this