import ammonite.ops._

val jdkVersions = List("8u212", "11.0.3")
val millVersions = List("0.4.0")

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

  val dockerFileContent = generateDockerfile(jdkVersion, millVersion)
  val dockerFilePath = directoryPath / "Dockerfile"
  write(dockerFilePath, dockerFileContent)

  implicit val wd: Path = directoryPath
  val imageName =
    s"guilgaly/openjdk-mill-node-heroku:$jdkVersion-$millVersion-stretch"

  println()
  println(s"***** Building image $imageName *****")
  % docker ("build", s"-t=$imageName", ".")

  println()
  println(s"***** Pushing image $imageName *****")
  % docker ("push", imageName)

  println()
  println(s"***** Done with image $imageName! *****")
}

def generateDockerfile(jdkVersion: String, millVersion: String) =
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
