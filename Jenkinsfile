pipeline{

    parameters {
        string(name: 'ARTIFACTORY_REPO', defaultValue: 'crf-test-local', description: '')
        string(name: 'ARTIFACTORY_PATH', defaultValue: '', description: '')
        string(name: 'SUITE_FOLDER', defaultValue: 'server', description: '')
        string(name: 'SELECT_TESTS_BASED_ON_TAGS', defaultValue: '', description: '')
        string(name: 'EXCLUDE', defaultValue: '', description: '')
        string(name: 'NONCRITCAL', defaultValue: '', description: '')
        string(name: 'TEST_ENVIRONMENT', defaultValue: 'replica', description: '')
        string(name: 'TEST_REPO_BRANCH', defaultValue: '', description: '')
        string(name: 'UPSTREAM_ID', defaultValue: '', description: '')
        string(name: 'VARIABLES', defaultValue: '', description: '')
        string(name: 'VARIABLEFILES', defaultValue: '', description: '')
        string(name: 'REPORT_ZIP', defaultValue: '', description: '')
    }
      agent{
          node{
              label 'ta-win10-13'
          }
      }
      options{
              disableConcurrentBuilds()
              skipDefaultCheckout(true)
      }
      libraries{
             lib('validation-pipeline-libraries@master')
      }
      environment{
          PATH = "${env.PATH}" + ";C:\\Apps\\jq-6;C:\\Program Files\\Git\\bin;C:\\Program Files\\Git\\usr\\bin;C:\\Users\\Administrator\\AppData\\Local\\Programs\\Python\\Python37\\Scripts;C:\\Users\\Administrator\\AppData\\Local\\Programs\\Python\\Python37;c:\\Windows\\System32"
          JAVA_HOME="/c/Java/jdk1.8.0_60"
          ANDROID_HOME = "/c/Android/Android-SDK-ADB-1.0.39"
          CHROME_DRIVER="/c/Apps/chrome_driver"
          ORACLE_HOME="/c/Apps/instantclient_12_1"
          LPATH="/c/Apps/jq-6:$CHROME_DRIVER:$ORACLE_HOME:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools:$JAVA_HOME/bin:$PATH"
      }
      stages{
           stage('Checkout'){
              when {
                  triggeredBy cause: 'UserIdCause'
              }
              steps{
                  cleanWs()
                  checkout scm
                  git url: 'ssh://aaaaa.cccccc.com:29418/system-test',
                      credentialsId: 'jenkins-git-ssh-sshkey-hki',
                      branch: 'master'
              }
           }
           stage('Test'){
              when {
                  triggeredBy cause: 'UserIdCause'
              }
              steps{
                 script{
                     validationRunner(params)
                 }
              }
           }
           stage('Upload artifacts'){
               when {
                   triggeredBy cause: 'UserIdCause'
               }
               steps{
                   script{
                       publishResults(params)
                   }
               }
           }
      }
      post {
          always {
              catchError(buildResult: 'SUCCESS',
                         message: 'Error publishing test results. No results?')
              {
                   step([
                         $class: 'RobotPublisher',
                         outputPath: 'tests/output/',
                         outputFileName : "output.xml",
                         disableArchiveOutput : false,
                         passThreshold : 100,
                         unstableThreshold : 90.0,
                         onlyCritical: true,
                         otherFiles : "**/*.png, *.png",
                   ])
              }  
          }
      }
}



