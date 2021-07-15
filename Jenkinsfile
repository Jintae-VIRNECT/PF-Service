pipeline {
    agent any

    environment {
        GIT_TAG = sh(returnStdout: true, script: 'git for-each-ref refs/tags --sort=-creatordate --format="%(refname)" --count=1 | cut -d/  -f3').trim()
        REPO_NAME = sh(returnStdout: true, script: 'git config --get remote.origin.url | sed "s/.*:\\/\\/github.com\\///;s/.git$//"').trim()
    }

    stages {
        stage('Build') {
            parallel {
                stage('Develop Branch') {
                    when {
                        branch 'develop'
                    }
                    steps {
                        sh 'docker build -t pf-license -f docker/Dockerfile .'
                    }
                }

                stage('Staging Branch') {
                    when {
                        branch 'staging'
                    }
                    steps {
                        sh 'git checkout ${GIT_TAG}'
                        sh 'docker build -t pf-license:${GIT_TAG} -f docker/Dockerfile .'
                    }
                }
            }
        }

        stage('Test') {
            steps {
                echo 'Test Stage'
            }
        }

        stage('Deploy') {
            parallel {

                stage('Develop Branch') {
                    when {
                        branch 'develop'
                    }
                    steps {
                        sh 'count=`docker ps -a | grep pf-license | grep -v batch | wc -l`; if [ ${count} -gt 0 ]; then echo "Running STOP&DELETE"; docker stop pf-license && docker rm pf-license; else echo "Not Running STOP&DELETE"; fi;'
                        sh 'docker run -p 8632:8632 -e "CONFIG_SERVER=http://192.168.6.3:6383" -e "VIRNECT_ENV=develop" -d --restart=always --name=pf-license pf-license'
                        sh 'count=`docker ps -a | grep pf-license-onpremise | grep -v batch | wc -l`; if [ ${count} -gt 0 ]; then echo "Running STOP&DELETE"; docker stop pf-license-onpremise && docker rm pf-license-onpremise; else echo "Not Running STOP&DELETE"; fi;'
                        sh 'docker run -p 18632:8632 -e "CONFIG_SERVER=http://192.168.6.3:6383" -e "VIRNECT_ENV=onpremise" -d --restart=always --name=pf-license-onpremise pf-license'
                        catchError() {
                             sh "if [ `docker images | grep pf-license |  grep -v batch | grep -v 103505534696 | wc -l` -gt 2 ]; then docker rmi  -f \$(docker images | grep \"pf-license\" | grep -v batch | grep -v \\${GIT_TAG} | grep -v \"latest\" | awk \'{print \$3}\'); else echo \"Just One Images...\"; fi;"
                        }
                    }
                }

                stage('Staging Branch') {
                    when {
                        branch 'staging'
                    }
                    steps {
                        script {
                            docker.withRegistry("https://$aws_ecr_address", 'ecr:ap-northeast-2:aws-ecr-credentials') {
                                docker.image("pf-license:${GIT_TAG}").push("${GIT_TAG}")
                                docker.image("pf-license:${GIT_TAG}").push("latest")
                            }
                        }

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
                                                execCommand: "docker pull $aws_ecr_address/pf-license:\\${GIT_TAG}"
                                            ),
                                            sshTransfer(
                                                execCommand: 'count=`docker ps -a | grep pf-license | grep -v batch | wc -l`; if [ ${count} -gt 0 ]; then echo "Running STOP&DELETE"; docker stop pf-license && docker rm pf-license; else echo "Not Running STOP&DELETE"; fi;'
                                            ),
                                            sshTransfer(
                                                execCommand: "docker run -p 8632:8632 --restart=always -e 'CONFIG_SERVER=https://stgconfig.virnect.com' -e 'VIRNECT_ENV=staging' -e eureka.instance.ip-address=`hostname -I | awk  \'{print \$1}\'` -d --name=pf-license $aws_ecr_address/pf-license:\\${GIT_TAG}"
                                            ),
                                            sshTransfer(
                                                execCommand: "if [ `docker images | grep pf-license | grep -v batch | wc -l` -ne 1 ]; then docker rmi  -f \$(docker images | grep \"pf-license\" | grep -v batch | grep -v \\${GIT_TAG} | awk \'{print \$3}\'); else echo \"Just One Images...\"; fi;"
                                            )
                                        ]
                                    )
                                ]
                            )
                        }
                        script {
                            sshPublisher(
                                continueOnError: false, failOnError: true,
                                publishers: [
                                    sshPublisherDesc(
                                        configName: 'aws-onpremise-qa',
                                        verbose: true,
                                        transfers: [
                                            sshTransfer(
                                                execCommand: 'aws ecr get-login --region ap-northeast-2 --no-include-email | bash'
                                            ),
                                            sshTransfer(
                                                execCommand: "docker pull $aws_ecr_address/pf-license:\\${GIT_TAG}"
                                            ),
                                            sshTransfer(
                                                execCommand: 'count=`docker ps -a | grep pf-license | grep -v batch | wc -l`; if [ ${count} -gt 0 ]; then echo "Running STOP&DELETE"; docker stop pf-license && docker rm pf-license; else echo "Not Running STOP&DELETE"; fi;'
                                            ),
                                            sshTransfer(
                                                execCommand: "docker run -p 8632:8632 --restart=always -e 'CONFIG_SERVER=http://3.35.50.181:6383' -e 'VIRNECT_ENV=onpremise' -e eureka.instance.ip-address=`hostname -I | awk  \'{print \$1}\'` -d --name=pf-license $aws_ecr_address/pf-license:\\${GIT_TAG}"
                                            ),
                                            sshTransfer(
                                                execCommand: "if [ `docker images | grep pf-license | grep -v batch | wc -l` -ne 1 ]; then docker rmi  -f \$(docker images | grep \"pf-license\" | grep -v batch | grep -v \\${GIT_TAG} | awk \'{print \$3}\'); else echo \"Just One Images...\"; fi;"
                                            )
                                        ]
                                    )
                                ]
                            )
                        }
                        script {
                            def GIT_TAG_CONTENT = sh(returnStdout: true, script: 'git for-each-ref refs/tags/$GIT_TAG --format=\'%(contents)\' | sed -z \'s/\\\n/\\\\n/g\'')
                            def payload = """
                            {"tag_name": "$GIT_TAG", "name": "$GIT_TAG", "body": "$GIT_TAG_CONTENT", "target_commitish": "master", "draft": false, "prerelease": true}
                            """                             

                            sh "curl -d '$payload' -X POST 'https://api.github.com/repos/$REPO_NAME/releases?access_token=$securitykey'"
                            def GIT_TAG_RELEASE = sh(returnStdout: true, script: 'git for-each-ref refs/tags/$GIT_TAG --format=\'%(contents)\' | sed -z \'s/\\\n/\\\n\\\n/g\'')
                            sh "curl -H \"Content-Type: application/json\" --data '{\"summary\": \"GITHUB Release note\",\"sections\" : [{ \"facts\": [{\"name\": \"REPO_NAME\",\"value\": \"\'\"$REPO_NAME\"\'\"},{\"name\": \"TAG_VERSION\",\"value\": \"\'\"$GIT_TAG\"\'\"},{\"NAME\": \"Branch\",\"value\": \"Staging\"},{\"name\": \"Information\",\"value\": \"\'\"$GIT_TAG_RELEASE\"\'\"}],\"markdown\": true}]}' -X POST 'https://virtualconnect.webhook.office.com/webhookb2/41e17451-4a57-4a25-b280-60d2d81e3dc9@d70d3a32-a4b8-4ac8-93aa-8f353de411ef/IncomingWebhook/642d8eba6da64076aba68a6bb6cc3e96/d0ac2f62-c503-4802-8bf9-f6368d7f39f8'"

                        }
                    }
                }

                stage('Master Branch') {
                    when {
                        branch 'master'
                    }
                    steps {
                        script {
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
                                                execCommand: "docker pull $aws_ecr_address/pf-license:\\${GIT_TAG}"
                                            ),
                                            sshTransfer(
                                                execCommand: 'count=`docker ps -a | grep pf-license | grep -v batch | wc -l`; if [ ${count} -gt 0 ]; then echo "Running STOP&DELETE"; docker stop pf-license && docker rm pf-license; else echo "Not Running STOP&DELETE"; fi;'
                                            ),
                                            sshTransfer(
                                                execCommand: "docker run -p 8632:8632 --restart=always -e 'CONFIG_SERVER=https://config.virnect.com' -e 'VIRNECT_ENV=production' -e eureka.instance.ip-address=`hostname -I | awk  \'{print \$1}\'` -d --name=pf-license $aws_ecr_address/pf-license:\\${GIT_TAG}"
                                            ),
                                            sshTransfer(
                                                execCommand: "if [ `docker images | grep pf-license | grep -v batch | wc -l` -ne 1 ]; then docker rmi  -f \$(docker images | grep \"pf-license\" | grep -v batch | grep -v \\${GIT_TAG} | awk \'{print \$3}\'); else echo \"Just One Images...\"; fi;"
                                            )
                                        ]
                                    )
                                ]
                            )
                        }

                        script {
                            def GIT_RELEASE_INFO = sh(returnStdout: true, script: 'curl -X GET https:/api.github.com/repos/$REPO_NAME/releases/tags/$GIT_TAG?access_token=$securitykey')
                            def RELEASE = readJSON text: "$GIT_RELEASE_INFO"
                            def RELEASE_ID = RELEASE.id
                            def payload = """
                            {"prerelease": false}
                            """

                            sh "echo '$RELEASE'"

                            sh "curl -d '$payload' -X PATCH 'https://api.github.com/repos/$REPO_NAME/releases/$RELEASE_ID?access_token=$securitykey'"
                            def GIT_TAG_RELEASE = sh(returnStdout: true, script: 'git for-each-ref refs/tags/$GIT_TAG --format=\'%(contents)\' | sed -z \'s/\\\n/\\\n\\\n/g\'')
                            sh "curl -H \"Content-Type: application/json\" --data '{\"summary\": \"GITHUB Release note\",\"sections\" : [{ \"facts\": [{\"name\": \"REPO_NAME\",\"value\": \"\'\"$REPO_NAME\"\'\"},{\"name\": \"TAG_VERSION\",\"value\": \"\'\"$GIT_TAG\"\'\"},{\"NAME\": \"Branch\",\"value\": \"Staging\"},{\"name\": \"Information\",\"value\": \"\'\"$GIT_TAG_RELEASE\"\'\"}],\"markdown\": true}]}' -X POST 'https://virtualconnect.webhook.office.com/webhookb2/41e17451-4a57-4a25-b280-60d2d81e3dc9@d70d3a32-a4b8-4ac8-93aa-8f353de411ef/IncomingWebhook/5433af0a21da48a799418f2c7a046d3d/d0ac2f62-c503-4802-8bf9-f6368d7f39f8'"

                        }
                    }
                }
            }
        }
    }

    post {
        always {
            office365ConnectorSend 'https://outlook.office.com/webhook/41e17451-4a57-4a25-b280-60d2d81e3dc9@d70d3a32-a4b8-4ac8-93aa-8f353de411ef/JenkinsCI/e79d56c16a7944329557e6cb29184b32/d0ac2f62-c503-4802-8bf9-f6368d7f39f8'
        }
    }
}
