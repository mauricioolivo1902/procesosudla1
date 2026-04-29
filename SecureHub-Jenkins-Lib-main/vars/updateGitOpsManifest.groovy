def call(Map config) {
    withCredentials([usernamePassword(
        credentialsId: 'github-creds',
        usernameVariable: 'GIT_USER',
        passwordVariable: 'GIT_TOKEN'
    )]) {
        script {
            def namespace    = env.KUBE_NAMESPACE
            def imageTag     = env.IMAGE_TAG
            def manifestPath = env.MANIFEST_PATH
            def imageName    = config.imageName
            def dockerUser   = env.DOCKER_USER_SAVED ?: 'daviduyaguarij'
            def gitopsBranch = 'main'
            bat """
                if exist gitops-repo rmdir /s /q gitops-repo
                git clone https://%GIT_USER%:%GIT_TOKEN%@github.com/DavidUyaguariJ/SecureHub-GitOps.git gitops-repo
                cd gitops-repo && git checkout ${gitopsBranch}
            """
            def template   = readFile('deployment-template.yaml')
            def deployment = template
                .replace('${NAMESPACE}',               namespace)
                .replace('${IMAGE_TAG}',               imageTag)
                .replace('${DOCKERHUB_USER}',          dockerUser)
                .replace('${IMAGE_NAME}',              imageName)
                .replace('${ASPNETCORE_ENVIRONMENT}',  env.BUILD_ENV)
            powershell """
                New-Item -ItemType Directory -Force -Path "gitops-repo/${manifestPath}"
            """
            writeFile(
                file: "gitops-repo/${manifestPath}/deployment.yaml",
                text: deployment
            )
            echo "Manifiesto escrito en: gitops-repo/${manifestPath}/deployment.yaml"
            def status = bat(
                script: 'cd gitops-repo && git status --porcelain',
                returnStdout: true
            ).trim()
            if (status) {
                bat """
                    cd gitops-repo
                    git config user.name "Jenkins CI"
                    git config user.email "jenkins@securehub.local"
                    git add ${manifestPath}/deployment.yaml
                    git commit -m "Update ${imageName}:${imageTag} for ${namespace} [skip ci]"
                    git push https://%GIT_USER%:%GIT_TOKEN%@github.com/DavidUyaguariJ/SecureHub-GitOps.git ${gitopsBranch}
                """
                echo "Cambios pusheados al repositorio GitOps"
            } else {
                echo "Sin cambios, nada que commitear"
            }

            echo "ArgoCD detectará y sincronizará automáticamente"
        }
    }
}