pipeline {
    agent any
    tools {
        maven 'mvn'
        jdk 'jdk8'
    }
    stages {
        stage ('Initialize') {
            steps {
                sh '''
                    echo "PATH = ${PATH}"
                    echo "M2_HOME = ${M2_HOME}"
                '''
            }
        }

        stage ('test') {
            steps {
                sh 'mvn clean test'
            }
        }
        stage ('package') {
            steps {
                sh 'mvn package'
            }
        }
    }
}