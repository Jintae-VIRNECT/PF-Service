/* SpringBoot API Server Docker Based Jenkinsfile */
pipeline {
    agent any

    options {
        timeout(time: 2, unit: 'HOURS')
    }

    environment {
        REPO_URL = sh(returnStdout: true, script: 'git config --get remote.origin.url').trim()
        REPO_NAME = sh(returnStdout: true, script: 'git config --get remote.origin.url | rev | cut -f 1 -d "/" | rev | sed "s/.git//gi";sed "/^ *$/d"').toLowerCase().trim() 
        PORT = sh(returnStdout: true, script: 'cat docker/Dockerfile | egrep EXPOSE | awk \'{print $2}\'').trim()
        BRANCH_NAME = "${BRANCH_NAME.toLowerCase().trim()}"
        APP = ' '
        PREVIOUS_VERSION = sh(returnStdout: true, script: 'git semver get || git semver minor').trim()
        NEXT_VERSION = getNextSemanticVersion(to: [type: 'REF', value: 'HEAD'], patchPattern: '^[Ff]ix.*').toString()
        SLACK_CHANNEL = "${SLACK_ALERT_CHANNEL}"
        AUTHOR = sh(returnStdout: true, script : 'git --no-pager show -s --pretty="format: %an"')
        ONPRE_URL = 'root@192.168.0.2'
        ONPRE_STG_URL = 'vntuser@192.168.0.212'
        ONPRE_DIR = '/data/onpre_dev_tar'
        ONPRE_ED = '1'//(0:disable , 1:enable)
    }

    stages {
        stage ('start') {
            steps {
                slackSend (channel: env.SLACK_CHANNEL, color: '#FFFF00', message: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})(${AUTHOR})")
                script{
                    if("${ADD_NOTION}"=='1'){
                        sh "node ${ADD_NOTION_PATH} $BUILD_TIMESTAMP ${env.JOB_NAME} ${env.BUILD_NUMBER} ${env.BUILD_URL} ${AUTHOR}"
                    }
                }
            }
        }

        stage ('compatibility check') {
            when { anyOf { branch 'master'; branch 'staging'; branch 'develop'} }
            environment {
                IS_REBASE_MERGE_FROM_MASTER = sh(script: "git branch --contains ${PREVIOUS_VERSION} | grep ${BRANCH_NAME}", returnStatus: true)
            }
            steps {
                script {
                    echo """
                    LATEST RELEASE VERSION: ${PREVIOUS_VERSION} \n
                    NEXT VERSION: ${NEXT_VERSION} \n
                    """
                    if (env.IS_REBASE_MERGE_FROM_MASTER != '0') {
                        echo """버전 호환이 맞지 않습니다. 아래 명령어를 통해 Rebase Merge 후 다시 시도해 주세요. \n
                            git rabase origin/master \n
                            git push -f origin ${BRANCH_NAME} \n
                        """
                        
                        deleteDir()
                        currentBuild.getRawBuild().getExecutor().interrupt(Result.ABORTED)
                        sleep(1)
                    }
                }
            }
        }

        stage ('version update commit check') {
            when {
                branch 'master'
            }
            environment {
                IS_UPDATE_COMMIT = sh(script: "git log -1 | grep 'chore: SOFTWARE VERSION UPDATED'", returnStatus: true)
            }
            steps {
                script {
                    if (env.IS_UPDATE_COMMIT == '0') {
                        echo "version update commit, not running..."
                        echo "clean up current directory"
                        deleteDir()
                        currentBuild.getRawBuild().getExecutor().interrupt(Result.SUCCESS)
                        sleep(1)
                    }
                }
            }
        }

        /*stage ('jacoco coverage analysis') {
            when {
                branch 'develop'
            }
            steps {
                sh '''
                    chmod +x ./gradlew && ./gradlew jacocoTestReport || IS_FAIL=true
                    if [[ $IS_FAIL = "true" ]]; then
                        echo "JaCoCo Report Failure"
                        exit 1
                    else
                        echo "JaCoCo Report Success"
                    fi
                    '''
            }
        }

        stage ('sonarqube code analysis') {
            when {
                branch 'develop'
            }
            environment {
                scannerHome = tool 'sonarqube-scanner'
            }
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh "${scannerHome}/bin/sonar-scanner"
                }                
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }*/

        stage ('edit gradle version') {
            steps {
                script {
                    if ("${BRANCH_NAME}" == 'master') {
                        sh '''
                            sed -i "/version =/ c\\version = \'${NEXT_VERSION}\'" build.gradle
                        '''
                    } else {
                        sh '''
                            sed -i "/version =/ c\\version = \'${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}\'" build.gradle
                        '''
                    }
                }
            }
        }

        stage ('build docker image') {
            steps {
                script {
                    sh '''
                        docker images --quiet --filter=dangling=true | xargs --no-run-if-empty docker rmi -f
                    '''
                    APP = docker.build("""${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}""", "-f ./docker/Dockerfile .")
                }
            }
            post {
                always {
                    echo "jiraSendBuildInfo"
                    //jiraSendBuildInfo site: "${JIRA_URL}"
                }
            }
        }
        
        stage ('save onpremise tar | send remote server') {
            when { anyOf { branch 'staging'; branch 'develop'} }
            environment {
                //DIR_EXIST = sh(script: "find ${ONPRE_DIR}/${REPO_NAME}", returnStatus: true)
                ONPRE_ADDR = "root@192.168.0.2,vntuser@192.168.0.212"
            }
            steps {
                script {
                    if ("${ONPRE_ED}"=="1"){
                        env.ONPRE_ADDR.tokenize(",").each { addr ->
                            echo "Server is $addr"
                        }
                        if ("${BRANCH_NAME}"=="develop"){
                            withCredentials([usernamePassword(credentialsId: 'onpre_dev', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                                def remote = [:]
                                remote.name = "onpre_dev"
                                remote.host = "${ONPRE_URL.substring(5)}"
                                remote.allowAnyHosts = true
                                remote.user = USERNAME
                                remote.password = PASSWORD
                                remote.failOnError = true
                                sshCommand remote: remote, command: """
                                cd ${ONPRE_DIR}/
                                test -d ${REPO_NAME} && echo ${REPO_NAME} || sudo mkdir ${REPO_NAME}
                                cd ${ONPRE_DIR}/${REPO_NAME}
                                touch ${REPO_NAME}-${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.txt
                                """
                            }
                            sh '''
                            cd ${ONPRE_DIR}/
                            test -d ${REPO_NAME} && echo ${REPO_NAME} || sudo mkdir ${REPO_NAME}
                            cd ${ONPRE_DIR}/${REPO_NAME}
                            sudo docker save -o ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}
                            sudo chown -R jenkins:jenkins ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar
                            scp -P 22 ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar ${ONPRE_URL}:${ONPRE_DIR}/${REPO_NAME}
                            '''
                        } else {
                            withCredentials([usernamePassword(credentialsId: 'onpre_stg', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                                def remote = [:]
                                remote.name = "onpre_stg"
                                remote.host = "${ONPRE_STG_URL.substring(8)}"
                                remote.allowAnyHosts = true
                                remote.user = USERNAME
                                remote.password = PASSWORD
                                remote.failOnError = true
                                sshCommand remote: remote, command: """
                                cd ${ONPRE_DIR}/
                                test -d ${REPO_NAME} && echo ${REPO_NAME} || sudo mkdir ${REPO_NAME}
                                cd ${ONPRE_DIR}/${REPO_NAME}
                                touch ${REPO_NAME}-${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.txt
                                """
                            }
                            sh '''
                            cd ${ONPRE_DIR}/
                            test -d ${REPO_NAME} && echo ${REPO_NAME} || sudo mkdir ${REPO_NAME}
                            cd ${ONPRE_DIR}/${REPO_NAME}
                            sudo docker save -o ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}
                            sudo chown -R jenkins:jenkins ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar
                            scp -P 22 ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar ${ONPRE_STG_URL}:${ONPRE_DIR}/${REPO_NAME}
                            '''
                        }
                    }
                }
            }
        }
        
        stage ('save image to nexus') {
            steps {
                script {
                    docker.withRegistry("""https://${NEXUS_REGISTRY}""", "jenkins_to_nexus") {
                        APP.push("${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}")

                        if ("${BRANCH_NAME}" == 'master') {
                            APP.push("${NEXT_VERSION}")
                            APP.push("latest")
                        }
                    }
                }
            }
        }

        stage ('save image to ecr') {
            when { anyOf { branch 'master'; branch 'staging' } }
            steps {
                script {
                    docker.withRegistry("https://$aws_ecr_address", 'ecr:ap-northeast-2:aws-ecr-credentials') {
                        APP.push("${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}")

                        if ("${BRANCH_NAME}" == 'master') {
                            APP.push("${NEXT_VERSION}")
                            APP.push("latest")
                        }
                    }
                }
            }
        }

/*
        stage ('image scanning') {
            when {
                branch 'develop'
            }
            steps {
                writeFile file: 'anchore_images', text: "${NEXUS_REGISTRY}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}"
                anchore name: 'anchore_images'
            }
        }
 */

        stage ('deploy to development') {
            when {
                branch 'develop'
            }
                
            steps {
                // develop
                script {
                    withCredentials([usernamePassword(credentialsId: 'server_credentials', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                        def remote = [:]
                        remote.name = "${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}" 
                        remote.host = "${DEV_SERVER}"
                        remote.allowAnyHosts = true 
                        remote.user = USERNAME 
                        remote.password = PASSWORD
                        remote.failOnError = true

                        sshCommand remote: remote, command: """
                            docker login ${NEXUS_REGISTRY}
                            docker pull ${NEXUS_REGISTRY}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}
                            docker stop ${REPO_NAME} && docker rm ${REPO_NAME} || true
                            docker run --restart=on-failure:10 \
                                -d \
                                -e VIRNECT_ENV=develop \
                                -e CONFIG_SERVER=${DEV_CONFIG_SERVER} \
                                -p ${PORT}:${PORT} \
                                --name=${REPO_NAME} ${NEXUS_REGISTRY}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}
                        """
                    }
                }

                // onpremise develop
                script {
                    if ("${ONPRE_ED}"=="1"){
                        withCredentials([usernamePassword(credentialsId: 'onpre_dev', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                        def remote = [:]
                        remote.name = "onpre_dev"
                        remote.host = "${ONPRE_URL.substring(5)}"
                        remote.allowAnyHosts = true
                        remote.user = USERNAME
                        remote.password = PASSWORD
                        remote.failOnError = true
                        sshCommand remote: remote, command: """
                        cd ${ONPRE_DIR}/                            
                        test -d ${REPO_NAME} && echo ${REPO_NAME} || sudo mkdir ${REPO_NAME}
                        docker stop ${REPO_NAME} && docker rm ${REPO_NAME} || true
                        cd ${ONPRE_DIR}/${REPO_NAME}
                        pwd
                        docker load -i ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar
                        docker image tag ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER} ${REPO_NAME}:latest
                        sudo docker save -o ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}.tar ${REPO_NAME}:latest
                        cd ..
                        docker-compose up -d
                        cd virnectDownload
                        sed -i 's/tg-2lax\\">${REPO_NAME}.*/tg-2lax\\"\\>${REPO_NAME}:$BUILD_TIMESTAMP\\<\\/th\\>/g' index.html
                        sed -i 's/tg-1lax\\">${REPO_NAME}.*/tg-1lax\\"\\>${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar\\<\\/th\\>/g' index.html 
                        docker rmi -f ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}
                        docker images --quiet --filter=dangling=true | xargs --no-run-if-empty docker rmi
                        rm -rf ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar
                        cd ../../
                        test -f RemoteOnpre.tar && sudo rm -rf RemoteOnpre.tar || echo notarimage
                        tar -cf RemoteOnpre.tar onpre_dev_tar
                        """
                        }
                    }
                }
            }

            

            post {
                always {
                    echo "jiraSendBuildInfo"
                    //jiraSendDeploymentInfo site: "${JIRA_URL}", environmentId: 'harington-development', environmentName: 'harington-development', environmentType: 'development'
                    //jiraSendDeploymentInfo site: "${JIRA_URL}", environmentId: 'harington-development-onpremise', environmentName: 'harington-development-onpremise', environmentType: 'development'
                }
            }
        }

        stage ('deploy to staging') {
            when {
                branch 'staging'
            }
                
            steps {
                script {
                    sshPublisher(
                        continueOnError: false, failOnError: true,
                        publishers: [
                            sshPublisherDesc(
                                configName: 'aws-bastion-deploy-qa',
                                verbose: true,
                                transfers: [
                                    sshTransfer(
                                        execCommand: 'aws ecr get-login --region ap-northeast-2 --no-include-email | bash'
                                    ),
                                    sshTransfer(
                                        execCommand: "docker pull ${aws_ecr_address}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}"
                                    ),
                                    sshTransfer(
                                        execCommand: """
                                            echo '${REPO_NAME} Container stop and delete'
                                            docker stop ${REPO_NAME} && docker rm ${REPO_NAME} 

                                            echo '${REPO_NAME} New Container start'
                                            docker run --restart=on-failure:10 \
                                                    -d \
                                                    -e VIRNECT_ENV=staging \
                                                    -e CONFIG_SERVER=${STG_CONFIG_SERVER} \
                                                    -e WRITE_YOUR=ENVIRONMENT_VARIABLE_HERE \
                                                    -e eureka.instance.ip-address=`hostname -I | awk  \'{print \$1}\'` \
                                                    -p ${PORT}:${PORT} \
                                                    --name=${REPO_NAME} ${aws_ecr_address}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}
                                        """
                                    )
                                ]
                            )
                        ]
                    )
                }
                
                // onpremise staging
                script {
                    if ("${ONPRE_ED}"=="1"){
                        withCredentials([usernamePassword(credentialsId: 'onpre_stg', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
                        def remote = [:]
                        remote.name = "onpre_stg"
                        remote.host = "${ONPRE_STG_URL.substring(8)}"
                        remote.allowAnyHosts = true
                        remote.user = USERNAME
                        remote.password = PASSWORD
                        remote.failOnError = true
                        sshCommand remote: remote, command: """
                        cd ${ONPRE_DIR}/                            
                        test -d ${REPO_NAME} && echo ${REPO_NAME} || mkdir ${REPO_NAME}
                        docker stop ${REPO_NAME} && docker rm ${REPO_NAME} || true
                        cd ${ONPRE_DIR}/${REPO_NAME}
                        pwd
                        docker load -i ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar
                        docker image tag ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER} ${REPO_NAME}:latest
                        docker save -o ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}.tar ${REPO_NAME}:latest
                        cd ..
                        docker-compose up -d
                        cd virnectDownload
                        sed -i 's/tg-2lax\\">${REPO_NAME}.*/tg-2lax\\"\\>${REPO_NAME}:$BUILD_TIMESTAMP\\<\\/th\\>/g' index.html
                        sed -i 's/tg-1lax\\">${REPO_NAME}.*/tg-1lax\\"\\>${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar\\<\\/th\\>/g' index.html 
                        docker rmi -f ${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}
                        docker images --quiet --filter=dangling=true | xargs --no-run-if-empty docker rmi
                        rm -rf ${ONPRE_DIR}/${REPO_NAME}/${REPO_NAME}:${NEXT_VERSION}-${BRANCH_NAME}-${BUILD_NUMBER}.tar
                        cd ../../
                        test -f RemoteOnpre.tar && sudo rm -rf RemoteOnpre.tar || echo notarimage
                        sudo tar -cf RemoteOnpre.tar onpre_dev_tar
                        """
                        }
                    }
                }
            }
            
            post {
                always {
                    echo "jiraSendBuildInfo"
                    //jiraSendDeploymentInfo site: "${JIRA_URL}", environmentId: 'aws-stging', environmentName: 'aws-stging', environmentType: 'staging'
                    //jiraSendDeploymentInfo site: "${JIRA_URL}", environmentId: 'aws-stging-onpremise', environmentName: 'aws-stging-onpremise', environmentType: 'staging'
                }
            }
        }

        stage ('git push and create release on github') {
            when { branch 'master'; expression { env.PREVIOUS_VERSION != env.NEXT_VERSION } }
            steps {
                script {
                    withCredentials([string(credentialsId: 'github_api_access_token', variable: 'TOKEN')]) {
                        sh '''
                            git add build.gradle
                            git commit -m "chore: SOFTWARE VERSION UPDATED"
                            git push https://$TOKEN@github.com/virnect-corp/$REPO_NAME.git +$BRANCH_NAME
                        '''

                        env.CHANGE_LOG = gitChangelog returnType: 'STRING', 
                            from: [type: 'REF', value: "${PREVIOUS_VERSION}"],
                            to: [type: 'REF', value: 'master'],
                            template: "{{#tags}}{{#ifContainsBreaking commits}}### Breaking Changes \\r\\n {{#commits}}{{#ifCommitBreaking .}}{{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{{commitDescription .}}}([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}})) \\r\\n {{/ifCommitBreaking}}{{/commits}}{{/ifContainsBreaking}} {{#ifContainsType commits type='feat'}} ### Features \\r\\n {{#commits}}{{#ifCommitType . type='feat'}}{{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{{commitDescription .}}}([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}})) \\r\\n {{/ifCommitType}}{{/commits}}{{/ifContainsType}} {{#ifContainsType commits type='fix'}}### Bug Fixes \\r\\n {{#commits}}{{#ifCommitType . type='fix'}}{{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{{commitDescription .}}}([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}})) \\r\\n {{/ifCommitType}}{{/commits}}{{/ifContainsType}} \\r\\n Copyright (C) 2020, VIRNECT CO., LTD. - All Rights Reserved \\r\\n {{/tags}}"
                                                
                        sh '''
                            curl \
                                -X POST \
                                -H "Accept: application/vnd.github.manifold-preview" \
                                -H "Authorization: token $TOKEN" \
                                -H "Content-Type: application/json" \
                                https://api.github.com/repos/virnect-corp/$REPO_NAME/releases \
                                -d '{"tag_name": "'"${NEXT_VERSION}"'", "target_commitish": "master", "name": "'"$NEXT_VERSION"'", "draft": false, "prerelease": false, "body": "'"$CHANGE_LOG"'"}'
                        '''
                    }
                }
            }
        }

        stage ('deploy to production') {
            when {
                branch 'master'
            }
                
            steps {
                script {
                    echo "deploy production"

                    // pull and run container
                    sshPublisher(
                        continueOnError: false, failOnError: true,
                        publishers: [
                            sshPublisherDesc(
                                configName: 'aws-bastion-deploy-prod',
                                verbose: true,
                                transfers: [
                                    sshTransfer(
                                        execCommand: 'aws ecr get-login --region ap-northeast-2 --no-include-email | bash'
                                    ),
                                    sshTransfer(
                                        execCommand: "docker pull ${aws_ecr_address}/${REPO_NAME}:\\${NEXT_VERSION}"
                                    ),
                                    sshTransfer(
                                        execCommand: """
                                            echo '${REPO_NAME} Container stop and delete'
                                            docker stop ${REPO_NAME} && docker rm ${REPO_NAME} 

                                            echo '${REPO_NAME} New Container start'
                                            docker run --restart=on-failure:10 \
                                                -d \
                                                -e VIRNECT_ENV=production \
                                                -e CONFIG_SERVER=${PROD_CONFIG_SERVER} \
                                                -e WRITE_YOUR=ENVIRONMENT_VARIABLE_HERE \
                                                -p ${PORT}:${PORT} \
                                                --name=${REPO_NAME} ${aws_ecr_address}/${REPO_NAME}:${NEXT_VERSION}
                                        """
                                    )
                                ]
                            )
                        ]
                    )
                }
            }                

            post {
                always {
                    echo "jiraSendBuildInfo"
                    //jiraSendDeploymentInfo site: "${JIRA_URL}", environmentId: 'aws-production', environmentName: 'aws-production', environmentType: 'production'
                }
            }
        }
        
        stage ('deploy to production-us') {
            when {
                branch 'master'
            }
                
            steps {
                script {
                    echo "deploy production-us"

                    // pull and run container
                    sshPublisher(
                        continueOnError: false, failOnError: true,
                        publishers: [
                            sshPublisherDesc(
                                configName: 'aws-bastion-deploy-prod-us',
                                verbose: true,
                                transfers: [
                                    sshTransfer(
                                        execCommand: 'aws ecr get-login --region ap-northeast-2 --no-include-email | bash'
                                    ),
                                    sshTransfer(
                                        execCommand: "docker pull ${aws_ecr_address}/${REPO_NAME}:\\${NEXT_VERSION}"
                                    ),
                                    sshTransfer(
                                        execCommand: """
                                            echo '${REPO_NAME} Container stop and delete'
                                            docker stop ${REPO_NAME} && docker rm ${REPO_NAME} 

                                            echo '${REPO_NAME} New Container start'
                                            docker run --restart=on-failure:10 \
                                                -d \
                                                -e VIRNECT_ENV=production \
                                                -e CONFIG_SERVER=${PROD_US_CONFIG_SERVER} \
                                                -e WRITE_YOUR=ENVIRONMENT_VARIABLE_HERE \
                                                -p ${PORT}:${PORT} \
                                                --name=${REPO_NAME} ${aws_ecr_address}/${REPO_NAME}:${NEXT_VERSION}
                                        """
                                    )
                                ]
                            )
                        ]
                    )
                }
            }                

            post {
                always {
                    echo "jiraSendBuildInfo"
                    //jiraSendDeploymentInfo site: "${JIRA_URL}", environmentId: 'aws-production-us', environmentName: 'aws-production-us', environmentType: 'production'
                }
            }
        }
    }

    post {
        success {
            slackSend (channel: env.SLACK_CHANNEL, color: '#00FF00', message: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
        failure {
            slackSend (channel: env.SLACK_CHANNEL, color: '#FF0000', message: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }
        aborted {
            slackSend (channel: env.SLACK_CHANNEL, color: '#808080', message: "ABORTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]' (${env.BUILD_URL})")
        }

        cleanup {
            echo 'clean up current directory'
            deleteDir()
        }
    }
}
