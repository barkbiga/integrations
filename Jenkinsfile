node {

 stage('Preparation') {
   env.PATH = "${tool 'Maven-3.5.0'}/bin:${env.PATH}"
 }

 stage('Checkout') {
    git 'https://github.com/barkbiga/integrations.git'
  }
  
  stage('Build Producer') {
       sh 'mvn clean install -f ./spring-batch-integration/pom.xml'
  }
  
  stage('Build Consumer') {
       sh 'mvn clean install -f ./spring-batch-integration-consumer/pom.xml'
  }
}