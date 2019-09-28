import ammonite.ops._

val jdkVersions = List("8u222", "11.0.4", "12.0.2", "13")
val millVersions = List("0.5.1")

val tmpPath = pwd / "tmp"
rm ! tmpPath
mkdir ! tmpPath

for {
  jdkVersion <- jdkVersions
  millVersion <- millVersions
} {
  val directoryName = s"openjdk$jdkVersion-mill$millVersion-node-heroku"
  val directoryPath: Path = tmpPath / directoryName
  mkdir ! directoryPath

  val dockerFileContent =
    if (isLegacyVersion(jdkVersion))
      generateLegacyDockerfile(jdkVersion, millVersion)
    else
      generateNewDockerFile(jdkVersion, millVersion)
  val dockerFilePath = directoryPath / "Dockerfile"
  write(dockerFilePath, dockerFileContent)

  implicit val wd: Path = directoryPath
  val imageName =
    s"guilgaly/openjdk-mill-node-heroku:$jdkVersion-$millVersion"

  println()
  println(s"***** Building image $imageName *****")
  % docker ("build", s"-t=$imageName", ".")

  println()
  println(s"***** Pushing image $imageName *****")
  % docker ("push", imageName)

  println()
  println(s"***** Done with image $imageName! *****")
}

def generateLegacyDockerfile(jdkVersion: String, millVersion: String) =
  s"""FROM circleci/openjdk:$jdkVersion-jdk-stretch-node
     |
     |RUN curl https://cli-assets.heroku.com/install.sh | sh
     |RUN heroku plugins:install java
     |
     |RUN mkdir ~/bin
     |RUN curl -L -o ~/bin/mill "https://github.com/lihaoyi/mill/releases/download/$millVersion/$millVersion"
     |RUN chmod +x ~/bin/mill
     |ENV PATH "~/bin:$$PATH"
     |""".stripMargin

def generateNewDockerFile(jdkVersion: String, millVersion: String) =
  s"""FROM azul/zulu-openjdk-debian:$jdkVersion
     |
     |RUN apt-get -y install curl
     |RUN apt-get -y install git
     |
     |# Node
     |RUN curl -sL https://deb.nodesource.com/setup_10.x | bash
     |RUN curl -sL https://dl.yarnpkg.com/debian/pubkey.gpg | apt-key add -
     |RUN echo "deb https://dl.yarnpkg.com/debian/ stable main" | tee /etc/apt/sources.list.d/yarn.list
     |RUN apt-get update && apt-get -y install yarn
     |
     |# Heroku
     |RUN curl -sL https://cli-assets.heroku.com/install.sh | bash
     |RUN heroku plugins:install java
     |
     |# Mill
     |RUN mkdir ~/bin
     |RUN curl -L -o ~/bin/mill "https://github.com/lihaoyi/mill/releases/download/$millVersion/$millVersion"
     |RUN chmod +x ~/bin/mill
     |ENV PATH "~/bin:$$PATH"
     |""".stripMargin

def isLegacyVersion(version: String) =
  version.matches("""(8u.*|(9|10|11)\..*)""")
