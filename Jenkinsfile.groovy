pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                // Checkout the source code from GitHub
                git 'https://github.com/SaraIravani/cinema-microservice.git'
            }
        }

        stage('Build') {
            steps {
                // Build the microservices here, replace with your build commands
                sh 'npm install' // Example for Node.js-based project
                sh 'npm run build' // Example for Node.js-based project
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
                sh "docker login -u sara -p ${DOCKER_REGISTRY_PASSWORD}"

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
