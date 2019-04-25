node {
  def app

  stage('Clone repository') {
    checkout scm
  }

  stage('Build image') {
    app = docker.build("ec-components/auth-proxy")
  }

  stage('Push image') {
    docker.withRegistry('http://registry:5000') {
      app.push("${env.BUILD_NUMBER}")
      app.push("latest")
    }
  }
}
