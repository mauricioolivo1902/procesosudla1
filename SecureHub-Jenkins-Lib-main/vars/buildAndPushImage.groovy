def call(Map config) {
    withCredentials([usernamePassword(
        credentialsId: 'dockerhub-creds',
        usernameVariable: 'DOCKER_USER',
        passwordVariable: 'DOCKER_PASS'
    )]) {
        script {
            def imageTag   = env.IMAGE_TAG
            def buildEnv   = env.BUILD_ENV
            def imageName  = config.imageName
            def dockerUser = env.DOCKER_USER
            env.DOCKER_USER_SAVED = dockerUser
            powershell """
                docker build `
                    --build-arg BUILD_ENV=${buildEnv} `
                    -t ${dockerUser}/${imageName}:${imageTag} `
                    -f Dockerfile .
            """
            bat "echo %DOCKER_PASS%| docker login -u %DOCKER_USER% --password-stdin"
            powershell """
                docker push ${dockerUser}/${imageName}:${imageTag}
            """
        }
    }
}