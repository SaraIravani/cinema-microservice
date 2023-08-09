pipeline {
    agent any

    tools {
        // Specify the NodeJS installation you configured in "Global Tool Configuration"
        nodejs 'NodeJS'
    }
    stages {
        stage('Checkout') {
            steps {
                // Checkout the source code from GitHub
                git 'https://github.com/SaraIravani/cinema-microservice.git'
                //checkout scm
            }
        }
        
        stage('Build') {
            steps {
                script {
                    dir('cinema-catalog-service') { 
                    // Build the microservices here, replace with your build commands
                        def nodeTool = tool name: 'NodeJS', type: 'jenkins.plugins.nodejs.tools.NodeJSInstallation'
                        sh 'node -v'
                        sh 'npm -v'
                        env.PATH = "${nodeTool}/bin:${env.PATH}"
                        sh 'npm install ' // Example for Node.js-based project
                        sh 'npm run build' // Example for Node.js-based project
                }
                }
            }
        }
        
        stage('Docker Build and Push') {
            environment {
                DOCKER_REGISTRY = '192.168.1.25' // Replace with your Docker registry
                IMAGE_NAME = 'cinema-microservice'
                IMAGE_TAG = 'latest'
            }
            steps {
                // Build the Docker image
                sh "docker build -t ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} ."

                // Log in to the Docker registry using credentials
                withCredentials([usernamePassword(credentialsId: 'docker-registry-credentials', passwordVariable: 'DOCKER_REGISTRY_PASSWORD', usernameVariable: 'DOCKER_REGISTRY_USERNAME')]) {
                    sh "docker login -u ${DOCKER_REGISTRY_USERNAME} -p ${DOCKER_REGISTRY_PASSWORD}"
                }

                // Push the Docker image to the registry
                sh "docker push ${DOCKER_REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}"
            }
        }

        stage('Deploy to Kubernetes') {
            environment {
                KUBE_CONFIG = credentials('config') // Jenkins credential for Kubernetes config
            }
            steps {
                // Apply Kubernetes manifests
                sh "kubectl --kubeconfig=$KUBE_CONFIG apply -f kubernetes-manifests/"
            }
        }
    }
}
