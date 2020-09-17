#!/usr/bin/env groovy
import es.uned.lsi.*

def call(Map args){
  def tests_source = [:]  
  def files_source
  def libFolder = args.grupo
  def curso = args.curso
  def libLocal  = args.local
  def sourceExtension = args.fileExt
  
  pipeline {
      agent any
      stages {
        stage('find source files'){
          steps{
            dir(path: 'doc') {
                  script{
                    if(!libLocal){
                      sh "find test/ -type f -name '*.${sourceExtension}' -delete"
                      sh (
                        script: "cp -r ${env.WORKSPACE}@libs/pipelineaslib/resources/PL1/${curso}/${libFolder}/* test/${libFolder}"
                        )
                    }
                    files_source = findFiles(glob: "test/${libFolder}/*.${sourceExtension}")
                  }
                }
              }
        }
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
          
          stage('ant test flex') {
            steps {
              echo 'Prueba de analizador lexico'
              
              withAnt(installation: 'ant_latest') {
                dir(path: 'doc') {
                  script{
                    
                    files_source.each { f->
                    
                        tests_source[ f.name ] = {
                          sh "ant -f 'config/build.xml' jenkinsFlexTest -Dtest-file-name='${libFolder}/${f.name}'"
                          sh "grep -q 'LEXICAL ERROR' 'test/${libFolder}/${f.name}_flex.out' && exit 64 || exit 0"
                        }
                    }
                    parallel tests_source                    
                      
                  }

                }
      
              }
      
            }
          }
        stage('ant test cup') {
            steps {
              echo 'Prueba de analizador sintactico'
              
              withAnt(installation: 'ant_latest') {
                dir(path: 'doc') {
                  script{
                    
                    files_source.each { f->
                    
                        tests_source[ f.name ] = {
                          sh "ant -f 'config/build.xml' jenkinsCupTest -Dtest-file-name='${libFolder}/${f.name}'"
                        }
                    }
                    parallel tests_source                    
                      
                  }

                }
      
              }
      
            }
          }
        stage('ant test final') {
            steps {
              echo 'Prueba de analizador lexico y sintactico, semantico comentado'
              
              withAnt(installation: 'ant_latest') {
                dir(path: 'doc') {
                  script{
                    
                    files_source.each { f->
                    
                        tests_source[ f.name ] = {
                          sh "ant -f 'config/build.xml' jenkinsFinalTest -Dtest-file-name='${libFolder}/${f.name}'"
                        }
                    }
                    parallel tests_source                    
                      
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
                    sl.getJSONREST(commitAuthorEmail,curso,'PL1')
                }
              }
              archiveArtifacts artifacts: "doc/test/${libFolder}/*.out"
              deleteDir()
          }
          failure {
              echo "Fallo en la realizacion de las pruebas"
              
          }


      }


  }
}