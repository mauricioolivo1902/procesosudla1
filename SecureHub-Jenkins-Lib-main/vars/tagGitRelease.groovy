def call(Map config) {
    withCredentials([usernamePassword(
        credentialsId: 'github-creds',
        usernameVariable: 'GIT_USER',
        passwordVariable: 'GIT_TOKEN'
    )]) {
        script {
            def tag      = env.IMAGE_TAG
            def repoUrl  = config.repoUrl
            def gitUser  = env.GIT_USER
            def gitToken = env.GIT_TOKEN
            powershell """
                git config user.name "jenkins"
                git config user.email "jenkins@local"
                git tag "v${tag}"
                git push https://${gitUser}:${gitToken}@${repoUrl} "v${tag}"
            """
        }
    }
}