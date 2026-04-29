def call(Map config) {
    def branch = env.BRANCH_NAME?.trim()
    def isPR   = env.CHANGE_ID != null
    if (isPR || !(branch == 'develop' || branch == 'master' || branch?.startsWith('stage'))) {
        currentBuild.result      = 'NOT_BUILT'
        currentBuild.description = "Branch '${branch}' ignorada"
        return
    }
    pipeline {
        agent any
        stages {
            stage('Set Environment') {
                steps {
                    script {
                        setEnvironment(config)
                    }
                }
            }
            stage('Checkout') {
                steps {
                    checkout scm
                }
            }

            stage('Build & Push Image') {
                steps {
                    script {
                        buildAndPushImage(config)
                    }
                }
            }
            stage('Tag Git') {
                steps {
                    script {
                        if (env.BRANCH_NAME == 'master') {
                            tagGitRelease(config)
                        } else {
                            echo "Branch '${env.BRANCH_NAME}' — se omite tag de Git"
                        }
                    }
                }
            }
            stage('Update GitOps Manifests') {
                steps {
                    script {
                        updateGitOpsManifest(config)
                    }
                }
            }

        }
        post {
            success {
                script {
                    echo """
                    ✔ Pipeline completado
                    Servicio:   ${config.service}
                    Imagen:     ${config.imageName}:${env.IMAGE_TAG}
                    Ambiente:   ${env.BUILD_ENV}
                    Namespace:  ${env.KUBE_NAMESPACE}
                    Manifiesto: ${env.MANIFEST_PATH}/deployment.yaml
                    """
                }
            }
            failure {
                echo "✘ Pipeline falló — revisa los logs."
            }
            always {
                bat 'if exist gitops-repo rmdir /s /q gitops-repo'
            }
        }
    }
}