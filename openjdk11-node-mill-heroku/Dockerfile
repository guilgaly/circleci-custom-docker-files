FROM circleci/openjdk:11.0.2-jdk-stretch-node

RUN curl https://cli-assets.heroku.com/install.sh | sh
RUN heroku plugins:install java

RUN mkdir ~/bin
RUN curl -L -o ~/bin/mill "https://github.com/lihaoyi/mill/releases/download/0.3.6/0.3.6"
RUN chmod +x ~/bin/mill
ENV PATH "~/bin:$PATH"
