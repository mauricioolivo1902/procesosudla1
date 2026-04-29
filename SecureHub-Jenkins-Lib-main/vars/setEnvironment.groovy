def call(Map config) {
    def branch = env.BRANCH_NAME?.trim()

    if (branch == 'develop') {
        env.BUILD_ENV      = config.devEnv      ?: 'development'
        env.KUBE_NAMESPACE = 'dev'
        env.IMAGE_TAG      = "dev-${env.BUILD_NUMBER}"
        env.MANIFEST_PATH  = "kubernetes/dev/${config.service}"

    } else if (branch?.startsWith('stage')) {
        env.BUILD_ENV      = config.stageEnv    ?: 'pre'
        env.KUBE_NAMESPACE = 'stage'
        env.IMAGE_TAG      = "stage-${env.BUILD_NUMBER}"
        env.MANIFEST_PATH  = "kubernetes/stage/${config.service}"

    } else if (branch == 'master') {
        env.BUILD_ENV      = config.prodEnv     ?: 'production'
        env.KUBE_NAMESPACE = 'prod'
        env.MANIFEST_PATH  = "kubernetes/prod/${config.service}"
        env.IMAGE_TAG      = config.getVersionCmd.call()
    }

    echo "======================================="
    echo "SERVICE:        ${config.service}"
    echo "BUILD_ENV:      ${env.BUILD_ENV}"
    echo "KUBE_NAMESPACE: ${env.KUBE_NAMESPACE}"
    echo "IMAGE_TAG:      ${env.IMAGE_TAG}"
    echo "MANIFEST_PATH:  ${env.MANIFEST_PATH}"
    echo "======================================="
}