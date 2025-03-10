pipeline {
    agent any

    environment {
        DOCKER_HUB_USERNAME = 'ayoubajdour'
        DOCKER_IMAGE_NAME = 'sampleprojet'
        SONARQUBE_IMAGE = 'ayoubajdour/sonarqube'
        SONAR_PROJECT_KEY = 'SampleProjet'
        SONAR_HOST_URL = 'http://192.168.189.129:9000'
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "nexus:8081"
        NEXUS_REPOSITORY = "sampleprojet"
        NEXUS_CREDENTIAL_ID = "nexustoken"
        ARGOCD_SERVER = '172.16.60.135:31749'
        ARGOCD_USERNAME = 'admin'
        ARGOCD_PASSWORD = 'ad6zKKFuV7Qv6mr7'
        API_URL = "http://172.16.60.135:8082/api/samples"
    }

    stages {
        stage('🔄 Install Git & Checkout Code') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'github-token', variable: 'GITHUB_TOKEN')]) {
                        sh 'echo $GITHUB_TOKEN'
                        sh 'git config --global user.name "Ayoub Ajdour"'
                        sh 'git config --global user.email "ayoubajdour20@gmail.com"'
                        
                        // Remove the directory if it already exists
                        sh 'rm -rf SampleProject'
                        
                        // Clone the repository
                        sh 'git clone https://github.com/Ayoub-Ajdour/SampleProject.git'
                    }
                }
            }
        }

        stage('🐳 Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest SampleProject/"
                }
            }
        }

        stage('🔍 Trivy Security Scan') {
            steps {
                script {
                    sh "docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy image ${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest"
                }
            }
        }

        stage('🔐 Login to Docker Hub') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'dockertoken', variable: 'DOCKER_TOKEN')]) {
                        sh "echo ${DOCKER_TOKEN} | docker login -u ${DOCKER_HUB_USERNAME} --password-stdin"
                    }
                }
            }
        }

        stage('☁️ Push Docker Image to Docker Hub') {
            steps {
                script {
                    sh "docker push ${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest"
                }
            }
        }

        stage('📦 Build Artifact') {
            steps {
                script {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Check Artifact Existence') {
            steps {
                script {
                    // Check if the artifact exists
                    sh 'ls -alh target/'
                }
            }
        }

        stage('📤 Publish to Nexus') {
            steps {
                script {
                    def pom = readMavenPom file: "pom.xml"
                    def filesByGlob = findFiles(glob: "target/*.${pom.packaging}")
                    
                    // Ensure the artifact is found before proceeding
                    if (filesByGlob.length == 0) {
                        error "❌ No artifact found to publish!"
                    }

                    def artifactPath = filesByGlob[0].path
                    echo "*** Uploading ${artifactPath} to Nexus ***"

                    nexusArtifactUploader(
    nexusVersion: NEXUS_VERSION,
    protocol: NEXUS_PROTOCOL,
    nexusUrl: NEXUS_URL,
    groupId: "${pom.groupId}",
    version: "${pom.version}",
    repository: NEXUS_REPOSITORY,
    credentialsId: NEXUS_CREDENTIAL_ID,
    artifacts: [
        [
            artifactId: "${pom.artifactId}",
            classifier: '',
            file: 'target/SampleProject-0.0.1-SNAPSHOT.jar',  // Use the actual path to your artifact
            type: "${pom.packaging}"
        ]
    ]
)

                }
            }
        }

        stage('🔍 SonarQube Analysis') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
                        sh """
                        docker run --rm -v ${WORKSPACE}:/usr/src --network=host sonarsource/sonar-scanner-cli \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.sources=. \
                            -Dsonar.java.binaries=target/classes \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }

        stage('🚀 Deploy with ArgoCD') {
            steps {
                script {
                    sh "argocd login ${ARGOCD_SERVER} --username '${ARGOCD_USERNAME}' --password '${ARGOCD_PASSWORD}' --insecure"
                    sh 'argocd app sync sampleprojet'
                }
            }
        }

        stage('🔧 API Test After Deployment') {
            steps {
                script {
                    sleep 30  // Wait for the deployment to stabilize
                    def response = sh(script: "curl -s -o /dev/null -w '%{http_code}' ${API_URL}", returnStdout: true).trim()

                    if (response == "200") {
                        echo "✅ API is up and running at ${API_URL}"
                    } else {
                        error "❌ API test failed! Received status code: ${response}"
                    }
                }
            }
        }

        stage('✅ Cleanup') {
            steps {
                script {
                    sh 'docker logout'
                    sh 'docker rmi ${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest || true'
                }
            }
        }
    }

    post {
        always {
            echo "Cleaning up resources..."
            // Add any additional cleanup tasks you may need here
        }
        failure {
            echo "❌ Pipeline failed. Check logs for details."
        }
        success {
            echo "✅ Pipeline executed successfully!"
        }
    }
}
