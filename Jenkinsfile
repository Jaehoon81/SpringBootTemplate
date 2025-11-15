pipeline {
    agent any  // 파이프라인을 실행할 Jenkins 에이전트

    // environment 블록의 변수 값들을 설정
    environment {
        // --- 1. Git 저장소 정보 ---
        GIT_REPO_URL = 'https://github.com/Jaehoon81/SpringBootTemplate.git'  // 실제 Git 저장소 URL
//         GIT_CREDENTIALS_ID = 'my-git-credentials-id'  // Jenkins에 등록된 GIT 키 자격증명 ID
        GIT_BRANCH = 'master'  // 빌드할 브랜치

        // --- 2. Docker 이미지 정보 ---
        DOCKER_IMAGE_NAME = 'springboot-template'  // 이미지 이름 (docker-compose.yml의 web 서비스 image 이름과 동일)
//         DOCKER_IMAGE_TAG = 'latest'  // 항상 latest 태그 사용
        DOCKER_IMAGE_TAG = "${env.BUILD_NUMBER}"  // Jenkins 빌드 번호를 태그로 사용 (불변성 확보)

        // --- 3. AWS EC2 배포 정보 ---
//         EC2_SSH_CREDENTIALS = 'my-ec2-ssh-credentials'  // Jenkins에 등록된 EC2 SSH 키 자격증명 ID
        EC2_USER = 'ubuntu'  // EC2 인스턴스의 사용자 이름
        EC2_HOST = 'ec2-52-79-127-32.ap-northeast-2.compute.amazonaws.com'  // EC2 인스턴스 퍼블릭 IP 또는 도메인
        EC2_DEPLOY_PATH = '/home/ubuntu/springboottemplate'  // docker-compose.yml이 있는 EC2 경로

        // --- 4. 서비스명 및 파일 이름 ---
        WEB_SERVICE_NAME = 'web'  // docker-compose.yml의 web 서비스 이름
        DOCKER_IMAGE_FILE = "${DOCKER_IMAGE_NAME}_${DOCKER_IMAGE_TAG}.tar"  // Jenkins에서 생성하고 EC2로 전송할 이미지 파일 이름 (태그 포함)
    }

    // Jenkins 파이프라인에서 이미지를 빌드하고 이 호스트로 'Save and Load' 방식을 사용합니다.
    stages {
        // Jenkins는 'Pipeline script from SCM' 방식으로 파이프라인을 실행할 때,
        // 파이프라인을 시작하기 전에 이미 해당 SCM(Git) 저장소의 내용을 워크스페이스에 클론해 놓습니다.
        // 따라서 Jenkinsfile 내부에서 다시 git 명령을 사용하여 소스 코드를 가져오는 것은 중복이므로 방법 1)과 방법 2) 모두 사용 X
        // 방법 1)
//         stage('Checkout Source Code') {
//             steps {
//                 echo "Checking out source code from ${GIT_REPO_URL} branch ${GIT_BRANCH}..."
//                 git branch: GIT_BRANCH, credentialsId: GIT_CREDENTIALS_ID, url: GIT_REPO_URL
//             }
//         }
        // 방법 2)
//         stage('Checkout Source Code') {
//             steps {
//                 echo "Checking out source code from ${GIT_REPO_URL} branch ${GIT_BRANCH}..."
//                 // credentials 스텝을 사용하여 자격 증명을 런타임에 로드
//                 credentials {
//                     // GitHub HTTPS Credential의 경우, username/password를 사용 (GitHub Personal Access Token도 가능)
//                     // usernamePassword credentials를 통해 GIT_USERNAME, GIT_PASSWORD 변수에 접근
//                     usernamePassword(credentialsId: 'my-git-credentials-id', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD') {
//                         // Git Clone 명령은 GIT_USERNAME과 GIT_PASSWORD 환경 변수를 자동으로 활용
//                         sh "git clone --single-branch -b ${GIT_BRANCH} https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/Jaehoon81/SpringBootTemplate.git ."
//                     }
//                 }
//             }
//         }

        // .env 파일을 Jenkins 워크스페이스에 수동으로 생성:
        // Jenkins에서 파이프라인을 실행하기 전에 Jenkins 마스터/에이전트의 /var/jenkins_home/workspace/springboottemplate-ci-cd/ 경로에
        // .env 파일을 SSH 등으로 직접 만들고, EC2_HOST 정보를 입력하는 수동 작업이 필요하므로 사용 X
//         stage('Load Environment Variables') {
//             steps {
//                 script {
//                     def envFileContent = readFile("${env.WORKSPACE}/.env")  // Jenkins 워크스페이스 루트의 .env 파일을 읽음
//                     def config = [:]
//                     envFileContent.eachLine { line ->
//                         if (line.trim() && !line.startsWith('#')) {
//                             def parts = line.split('=', 2)
//                             if (parts.size() == 2) {
//                                 config[parts[0].trim()] = parts[1].trim()
//                             }
//                         }
//                     }
//                     // 로드된 변수를 Jenkins 환경 변수로 주입
//                     env.EC2_HOST = config.EC2_HOST ?: ""  // EC2_HOST를 환경 변수에 설정
//                     echo "EC2_HOST loaded from .env: ${env.EC2_HOST}..."
//                 }
//             }
//         }

        stage('Build Docker Image') {
            steps {
                echo "Building Docker image ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} using Dockerfile..."
                // Jenkins 워크스페이스의 Dockerfile을 사용하여 Docker 이미지 빌드
                // Dockerfile은 프로젝트 루트에 있어야 합니다.(명령어 맨 마지막의 점(.)이 루트 디렉토리를 의미함)
//                 sh "docker build --platform linux/amd64 -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
                sh "docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} ."
            }
        }

        stage('Save Docker Image') {
            steps {
                echo "Saving Docker image to ${DOCKER_IMAGE_FILE}..."
                // 빌드된 Docker 이미지를 tar 파일로 저장
                sh "docker save -o ${DOCKER_IMAGE_FILE} ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}"
            }
        }

        stage('Deploy to AWS EC2') {
            steps {
                echo "Deploying ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} to EC2 instance: ${EC2_HOST}..."
                script {
                    // 'ec2-ssh-credentials'는 Jenkins에 등록한 Secret Text Credentials의 ID
                    // Secret Text Credentials의 값은 EC2_SSH_CREDENTIALS 값입니다.
                    withCredentials([string(credentialsId: 'ec2-ssh-credentials', variable: 'EC2_SSH_CREDENTIALS_FROM_JENKINS')]) {
                        env.EC2_SSH_CREDENTIALS = EC2_SSH_CREDENTIALS_FROM_JENKINS
                        echo "EC2_SSH_CREDENTIALS loaded from Jenkins: ${env.EC2_SSH_CREDENTIALS}..."

                        sshagent(credentials: [env.EC2_SSH_CREDENTIALS]) {
                            // 1. 이미지 파일을 Jenkins 워크스페이스에서 EC2 서버로 전송
                            echo "Transferring ${DOCKER_IMAGE_FILE} to ${EC2_HOST}:${EC2_DEPLOY_PATH}/"
                            // StrictHostKeyChecking=no는 Man-in-the-Middle 공격에 대해 보안상 취약하므로
                            // 추후에는 .ssh/known_hosts 파일에 EC2 인스턴스의 SSH 호스트 키를 미리 등록하여 직접 관리하는 것을 고려
                            sh "scp -o StrictHostKeyChecking=no ${DOCKER_IMAGE_FILE} ${EC2_USER}@${EC2_HOST}:${EC2_DEPLOY_PATH}/"

                            // 2. EC2 서버에서 Docker 이미지 로드 및 배포
                            echo "Loading and deploying image on ${EC2_HOST}..."
                            sh """
                                ssh -o StrictHostKeyChecking=no ${EC2_USER}@${EC2_HOST} << 'EOF'
                                    # env 파일로 환경 변수를 가상(임시)으로 넘겨서 Docker 이미지 이름과 태그가 docker-compose.yml에서 사용됨 (방법 2-1)
                                    #export DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME}"
                                    #export DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG}"

                                    # EC2 서버에서 docker-compose.yml이 있는 디렉토리로 이동
                                    cd ${EC2_DEPLOY_PATH}

                                    # 전송받은 tar 파일로부터 Docker 이미지 로드
                                    docker load -i ${DOCKER_IMAGE_FILE}

                                    # Docker Compose를 사용하여 웹 서비스 컨테이너 업데이트 (재시작)
                                    # 방법 1)
                                    # docker-compose.yml에 image: springboot-template:latest로 설정되어 있어야 합니다.(-> 사용 X)
                                    # /usr/bin/docker-compose up -d --no-deps ${WEB_SERVICE_NAME}
                                    #docker compose up -d --no-deps ${WEB_SERVICE_NAME}
                                    # 방법 2-1)
                                    # docker-compose.yml에 정의된 변수를 EC2 환경에서 사용
                                    # 또는 docker-compose.yml의 environment 섹션을 바로 이용하면 --env-file 옵션은 필요없음
                                    # docker-compose.yml에 image: springboot-template:${env.BUILD_NUMBER}로 설정되어 있어야 합니다.
                                    #docker compose --env-file ./.env up -d --no-deps ${WEB_SERVICE_NAME}
                                    # 방법 2-2)
                                    # 여기서는 shell 변수를 사용하여 바로 docker compose 명령에 태그를 전달
                                    # (Docker compose version 1.28.0+부터 'docker-compose config'에서 ENV_FILE 지원)
                                    # (Docker compose plugin(v2)에서는 CLI 환경 변수를 직접 주입 가능)
                                    DOCKER_IMAGE_NAME="${DOCKER_IMAGE_NAME}" DOCKER_IMAGE_TAG="${DOCKER_IMAGE_TAG}" docker compose up -d --no-deps ${WEB_SERVICE_NAME}

                                    # 전송받은 이미지 파일 삭제 (공간 확보를 위한 선택 사항)
                                    rm ${DOCKER_IMAGE_FILE}

                                    echo "Deployment of ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG} complete."
EOF
"""
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished.'
        }
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed. Check logs for details.'
        }
    }
}
