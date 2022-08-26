def call(Map params){
    pipeline{
      agent {
        node {
            label "${params.LABEL}"
        }
      }  
      options{
              disableConcurrentBuilds()
              skipDefaultCheckout(true)
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
           stage('Checkout system-test'){
              when{
                anyOf{
                    triggeredBy 'TimerTrigger';
                    triggeredBy cause: 'UserIdCause';
                    triggeredBy 'BuildUpstreamCause'
                }
              }
              steps{
                script {
                    validationRunner.checkoutRepo("${params.TEST_REPO_BRANCH}")
                }
              }
           }
           stage('Run System Test'){
              when {
                anyOf {
                    triggeredBy 'TimerTrigger';
                    triggeredBy cause: 'UserIdCause';
                    triggeredBy 'BuildUpstreamCause'
                }
              }
              steps{
                 script{
                     validationRunner(params)
                 }
              }
           }
           stage('Upload artifacts'){
               when {
                expression { params.PROJECT != null }
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
              archiveArtifacts artifacts: 'tests/output/*.zip, tests/output/*.xml, tests/output/*.png, tests/output/*.jpg, tests/output/*.log, tests/output/*.txt',
              allowEmptyArchive: true
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
}
