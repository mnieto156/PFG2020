#!/usr/bin/env groovy
import es.uned.lsi.*

def call(Map args){
  def tests_source = [:]
  def tests_ens = [:]
  def files_source
  def files_ens  
  def files_in
  def files_check
  def libFolder = args.grupo
  def curso = args.curso
  def libLocal  = args.local
  def sourceExtension = args.fileExt
  
  pipeline {
      agent any
      stages {
          stage('ant build compiler') {
        
            steps {
                echo 'Construyendo Compilador...'
               
                withAnt(installation: 'ant_latest') {               
                    dir('doc/config') {
                        sh "ant clear build"
                    }                 
                }
            }
                        
          }
          
          stage('ant build ens') {
          steps {
            echo 'Generar ensamblador de pruebas...'
            
            withAnt(installation: 'ant_latest') {
              dir(path: 'doc') {
                script{
                  if(!libLocal){
                    sh "find test/ -type f -name '*.${sourceExtension}' -delete"
                    sh (
                      script: "cp -r ${env.WORKSPACE}@libs/pipelineaslib/resources/PL2/${curso}/${libFolder}/* test/${libFolder}"
                    )
                  }
                  
                  files_source = findFiles(glob: "test/${libFolder}/*.${sourceExtension}")
                  files_source.each { f->
                  
                      tests_source[ f.name ] = {
                        sh "ant -f 'config/build.xml' jenkinsFinalTest -Dtest-file-name='${libFolder}/${f.name}'"
                      }
                  }
                    parallel tests_source
                    
                    files_ens = findFiles(glob: "test/${libFolder}/*.ens")
                }

              }
    
            }
    
          }
        }
        stage('ens run test') {
          when{
            equals expected:files_source.length, actual:files_ens.length
    
          }

            steps {
                echo 'Ejecutando pruebas en emulador...'
                echo "ficheros de codigo fuente: ${files_source.length}"
                echo "ficheros de ensamblador .ens: ${files_ens.length}"
                
                dir(path: "doc/test/${libFolder}"){
                  
                      timeout(time:3, unit: 'MINUTES') {
                          sh "cp ${env.WORKSPACE}@libs/pipelineaslib/resources/PL2/ens* ."
                          sh 'chmod +x ensShellScript.sh'
                          sh 'chmod +x ens2001'
                          script {
                            files_in = findFiles(glob: "*.in")
                            files_check = findFiles(glob: "*.check")

                              files_ens.each {
                                  f ->
                                  def baseName = f.name.take(f.name.lastIndexOf('.'))
                                  def file_in = files_in.find { it.name == "${baseName}.in"}
                                  def file_check = files_check.find { it.name == "${baseName}.check"}
                                  if (file_in && file_check){
                                    tests_ens[ baseName ] = {
                                      sleep(files_ens.length * Math.random())
                                      sh "timeout 1 ./ensShellScript.sh ${baseName}"
                                    }
                                  }
                                  else {println 'No encontrados los archivos de definicion de la prueba en emulador'}                                  
                              }
                              parallel tests_ens

                          }
                          
                  }
                
                }

            }

        }

      }
      post {
          always {
              echo 'Fin'
              echo "${env.BUILD_URL} - ${env.JOB_NAME} - ${env.BRANCH_NAME}"
              withCredentials([usernamePassword( credentialsId: 'postgresql_logsdb', 
                                      usernameVariable: 'dbUser', 
                                      passwordVariable: 'dbPassword')]) {
                script {
                    def commitAuthorEmail = sh (
                      script: "git --no-pager show -s --format=%ae",
                      returnStdout: true
                    ).trim()
                    def sl = new sendLogs()
                    sl.getJSONREST(commitAuthorEmail,curso,'PL2')
                }
              }
              archiveArtifacts artifacts: "doc/test/${libFolder}/*.out"
              deleteDir()
          }
          failure {
              echo "Fallo en la compilacion"
              
          }


      }


  }
}