pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = 'git.janpeterdhalle.com'
        IMAGE_NAME = 'jp-transfer-service'
        GIT_USERNAME = 'jp'
    }

    tools {
        jdk 'java21'
        maven 'maven3'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn -B clean package'
            }
        }

        stage('Docker Build & Push') {
            steps {
                script {
                    // Default tag
                    def imageTag = "latest"

                    // If current branch is 'develop', append _dev
                    if (env.BRANCH_NAME == "develop") {
                        imageTag = "latest_dev"
                    }

                    def fullImage = "${DOCKER_REGISTRY}/${GIT_USERNAME}/${IMAGE_NAME}:${imageTag}"

                    withCredentials([usernamePassword(credentialsId: 'forgejo', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "\$DOCKER_PASS" | docker login \$DOCKER_REGISTRY -u "\$DOCKER_USER" --password-stdin
                            docker build -t ${fullImage} .
                            docker push ${fullImage}
                        """
                    }
                }
            }
        }
    }
}
