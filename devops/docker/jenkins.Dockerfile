FROM jenkins/jenkins:lts

USER root

# 필수 패키지 설치 (Docker, Git, Curl 등)
RUN apt-get update && apt-get install -y \
    sudo \
    curl \
    git \
    openjdk-17-jdk \
    docker.io

# Jenkins 유저를 Docker 그룹에 추가
RUN usermod -aG docker jenkins

# Jenkins에서 sudo 사용 가능하게 설정
RUN echo "jenkins ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

USER jenkins
