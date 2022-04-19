def call(Map params = [:]){
    def artifactoryUrl = "https://devel.crfhealth.com/artifactory"
    def artifactorySearchAPI= "${artifactoryUrl}/api/search/aql"
    def buildDir = "${env.WORKSPACE}".replace("\\", "/")
    def response = httpRequest url: "${artifactorySearchAPI}",
                   contentType: 'APPLICATION_JSON',
                   httpMode: 'POST',
                   consoleLogResponseBody: false,
                   requestBody: 'items.find({"repo":"crf-test-local"},{"name":{"$match":"*TrialStudio-*.msi"}}).include("repo","path","name","modified").sort({"$desc":["modified"]})',
                   authentication: 'jenkins-artifactory-https-password-hki'
    def output = readJSON text: "${response.content}"
    def artifactsName = "${output.results[0].repo}"+"/"+"${output.results[0].path}"+"/"+"${output.results[0].name}"
    def downloadUrl = "${artifactoryUrl}"+"/"+"${artifactsName}"
    def tempOutput = "${output.results[0].name}"
    def timestamp = sh(script: "date +%F-%H-%M-%S", returnStdout: true).trim()
    def zipArgument = buildDir+"/tests/resources/libraries/python/src/artifactory/ArtifactoryRunner.py" +
                       ' --path ' +'output/ '+ '--zip_file output/'+params.REPORT_ZIP+'-'+"${timestamp}"+'.zip' + ' create_zip'
    dir('tests'){
      sh "curl -n -s $downloadUrl -o $tempOutput"
      def argv = '--nostatusrc' + ' ' +
               '--outputdir output' + ' ' +
               '--pythonpath ' +buildDir+'/tests/resources/page_objects:'+buildDir+'/tests/resources/libraries/python/src'+' ' +
               params.VARIABLEFILES + ' ' +
               '--variablefile '  +buildDir+'/tests/resources/common/variable_file/common_variables.py:'+params.TEST_ENVIRONMENT + ' ' +
               params.VARIABLES + ' ' +
               '--xunit xunit.xml' + ' ' +
               '--include ' + params.SELECT_TESTS_BASED_ON_TAGS+' ' +
               '--exclude ' + params.EXCLUDE + ' ' +
               '--noncritical ' +params.NONCRITICAL+' ' +
               '--loglevel TRACE' + ' ' +
               '--variable TS_MSI_INSTALLER_PATH:'+buildDir+'/tests/'+tempOutput+' ' +
               '--variable SYS_VAR_JACKRABBIT_CMS_CLUSTER_CHECK_TIMEOUT:600' + ' ' +
               '--variable SYS_VAR_PAGE_TIMEOUT:35' + ' ' +
               '--variable SYSVAR_WUKS_TIMEOUT:45' + ' ' +
               '--listener ' +buildDir+'/tests/resources/libraries/python/src/artifactory/ArtifactoryReport.py' + ' ' +
               buildDir+'/tests/test-specification/'+params.SUITE_FOLDER
      sh "py -3 -m robot.run $argv"
      sh "py -3 $zipArgument"
    }
}

