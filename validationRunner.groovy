def call(Map params = [:]){
    def argv = ''
    def buildDir = "${env.WORKSPACE}".replace("\\", "/")

    def timestamp = sh(script: "date +%F-%H-%M-%S", returnStdout: true).trim()

    def zipArgument = buildDir+"/tests/resources/libraries/python/src/artifactory/ArtifactoryRunner.py" +
                       ' --path output/ --zip_file output/'+params.REPORT_ZIP+"-${timestamp}.zip" + ' create_zip'

    dir('tests'){
      def fetchTrialStudio = getRequest("${params.ARTIFACTORY_RELEASED}", "*TrialStudio-*.msi", 0)
      env.TS_MSI_FILE = "${fetchTrialStudio}"
      env.ARTIFACTORY_REPO = params.ARTIFACTORY_REPO
      env.ARTIFACTORY_PATH = params.ARTIFACTORY_PATH
      env.ZIP_FILE_NAME = params.REPORT_ZIP+"-${timestamp}.zip"

      
      if (params.INSTALLER_REPO){
        def fetchTrialInstaller = getRequest("${params.INSTALLER_REPO}", "*TrialInstaller-*.msi", 0)

        env.TI_MSI_FILE = "${fetchTrialInstaller}"

        argv += "--variable TI_MSI_INSTALLER_PATH:${buildDir}/tests/${fetchTrialInstaller}"
      }

      if (params.COLLECTOR_REPO){
        def fetchAndroidtc = getRequest("${params.COLLECTOR_REPO}", "${params.VERSION}-androidtc*", 0)
        def fetchEAndroidtc = getRequest("${params.COLLECTOR_REPO}", "${params.VERSION}-androidtc*", 1)
        env.ANDROID_ZIP = "${fetchAndroidtc}"

        params.VARIABLEFILES = "--variablefile ${buildDir}/tests/resources/common/AppiumLibrary/variables/device_reservation.py:${params.FILTER}"
        argv +=   "  --variable TC_ANDROIDTC_PACKAGE_PATH:${buildDir}/tests/${fetchEAndroidtc}"+
                  "  --variable SYS_VAR_TC_ANDROIDTC_UPDATED_PACKAGE_PATH:${buildDir}/tests/${fetchAndroidtc} "
      }

      argv += "  --nostatusrc  --outputdir output "+
               "--pythonpath ${buildDir}/tests/resources/page_objects:${buildDir}/tests/resources/libraries/python/src  ${params.VARIABLEFILES}"+
               " --variablefile ${buildDir}/tests/resources/common/variable_file/common_variables.py:${params.TEST_ENVIRONMENT}"+
               " ${params.VARIABLES}  --xunit xunit.xml  --include ${params.SELECT_TESTS_BASED_ON_TAGS}"+
               " --exclude ${params.EXCLUDE} --noncritical ${params.NONCRITICAL}  --loglevel TRACE "+
               " --variable TS_MSI_INSTALLER_PATH:${buildDir}/tests/${fetchTrialStudio} "+
               "--variable SYS_VAR_JACKRABBIT_CMS_CLUSTER_CHECK_TIMEOUT:600 --variable SYS_VAR_PAGE_TIMEOUT:35 "+
               "--variable SYSVAR_WUKS_TIMEOUT:45 --listener ${buildDir}/tests/resources/libraries/python/src/artifactory/ArtifactoryReport.py"+
               " ${buildDir}/tests/test-specification/${params.SUITE_FOLDER}"
      sh "py -3 -m robot.run $argv"
      sh "py -3 $zipArgument"
    }
}


def getRequest(repoName, fileName, index){
  def artifactoryUrl = "https://our.domain.com/artifactory"
  def artifactorySearchAPI= "${artifactoryUrl}/api/search/aql"
  def requestArg =  'items.find({"repo":"'+repoName+'"},'+'{"name":{"$match":"'+fileName+'"}}).include("repo","path","name","modified").sort({"$desc":["modified"]})'
  def response = httpRequest url: "${artifactorySearchAPI}",
                         contentType: 'APPLICATION_JSON',
                         httpMode: 'POST',
                         consoleLogResponseBody: false,
                         requestBody: "${requestArg}",
                         authentication: 'jenkins-artifactory-https-password-hki'
  def output = readJSON text: "${response.content}"
  def artifactsName = "${output.results[index].repo}/${output.results[index].path}/${output.results[index].name}"
  def downloadUrl = "${artifactoryUrl}/${artifactsName}"
  def fileToDownload = "${output.results[index].name}"

  withCredentials([usernamePassword(credentialsId: 'jenkins-artifactory-https-password-hki', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]){
    sh "curl -s -u username:${PASSWORD} $downloadUrl -o $fileToDownload"
  }

  return fileToDownload
}

def checkoutRepo(repoBranch){
  cleanWs()
  checkout scm
  git url: 'geturl.com',
      credentialsId: 'getcredentials',
      branch: "${repoBranch}"
}
