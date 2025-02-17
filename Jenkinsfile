pipeline {
    agent any

    environment {
        DOCKER_HUB_USERNAME = 'ayoubajdour'
        DOCKER_IMAGE_NAME = 'sampleprojet'
        SONARQUBE_IMAGE = 'ayoubajdour/sonarqube'
        SONAR_PROJECT_KEY = 'SampleProjet'
        SONAR_HOST_URL = 'http://192.168.189.128:9000'
        NEXUS_VERSION = "nexus3"
        NEXUS_PROTOCOL = "http"
        NEXUS_URL = "192.168.189.128:8081"
        NEXUS_REPOSITORY = "sampleprojet"
        NEXUS_CREDENTIAL_ID = "nexustoken"
        ARGOCD_SERVER = '172.16.60.135:31749' // Update this to include the server and port without 'https://'
        ARGOCD_USERNAME = 'admin'
        ARGOCD_PASSWORD = 'ad6zKKFuV7Qv6mr7'
    }

    stages {

        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/Ayoub-Ajdour/SampleProject.git', credentialsId: 'githubtoken'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    sh "docker build -t ${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest ."
                }
            }
        }
        stage('Trivy Scan') {
    steps {
        script {
            // Run Trivy scan on the built Docker image without using unsupported flags
            sh 'docker run --rm -v /var/run/docker.sock:/var/run/docker.sock aquasec/trivy:latest image ${DOCKER_HUB_USERNAME}/${DOCKER_IMAGE_NAME}:latest'
        }
    }
}


        stage('Login to Docker Hub') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'dockertoken', variable: 'DOCKER_TOKEN')]) {
                        sh """
                        echo ${DOCKER_TOKEN} | docker login -u ${DOCKER_HUB_USERNAME} --password-stdin
                        """
                    }
                }
            }
        }

        stage('Build Artifact') {
            steps {
                script {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage("Publish to Nexus") {
            steps {
                script {
                    sh 'mvn clean package -Dmaven.test.skip=true' // Ensure tests are skipped
                    pom = readMavenPom file: "pom.xml"
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}")
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                    artifactPath = filesByGlob[0].path
                    artifactExists = fileExists artifactPath

                    if (artifactExists) {
                        echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version}"

                        nexusArtifactUploader(
                            nexusVersion: NEXUS_VERSION,
                            protocol: NEXUS_PROTOCOL,
                            nexusUrl: NEXUS_URL,
                            groupId: pom.groupId,
                            version: '1',
                            repository: NEXUS_REPOSITORY,
                            credentialsId: NEXUS_CREDENTIAL_ID,
                            artifacts: [
                                [artifactId: pom.artifactId,
                                 classifier: '',
                                 file: artifactPath,
                                 type: pom.packaging]
                            ]
                        )

                    } else {
                        error "*** File: ${artifactPath}, could not be found"
                    }
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    withCredentials([string(credentialsId: 'sonartoken', variable: 'SONAR_TOKEN')]) {
                        sh """
                        docker run --rm \
                            --network=host \
                            -v \$(pwd):/usr/src \
                            sonarsource/sonar-scanner-cli \
                            -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                            -Dsonar.sources=. \
                            -Dsonar.host.url=${SONAR_HOST_URL} \
                            -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }
        // stage('Deploy with ArgoCD') {
        //     steps {
        //         script {
        //             sh """
        //                 argocd login ${ARGOCD_SERVER} --username "${ARGOCD_USERNAME}" --password "${ARGOCD_PASSWORD}" --insecure
        //             """

        //             sh 'argocd app sync sampleprojet'
        //         }
        //     }
        // }

        stage('Post Actions') {
            steps {
                sh 'docker logout'
                sh 'docker rmi ayoubajdour/sampleprojet:latest'
            }
        }
    }

    post {
        failure {
            echo "Pipeline failed. Please check the logs for more details."
        }
        success {
            echo "Pipeline succeeded!"
        }
    }
}
