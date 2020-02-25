import ammonite.ops._

val jdkVersions = List(/*"8.0.242", */"11.0.6", "12.0.2", "13.0.2")
val millVersions = List("0.6.0")

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

  val dockerFileContent = generateDockerFile(jdkVersion, millVersion)
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

def generateDockerFile(jdkVersion: String, millVersion: String) =
  s"""FROM cimg/openjdk:$jdkVersion
     |
     |# Node, Heroku, Mill
     |RUN curl -sL https://deb.nodesource.com/setup_10.x | sudo -E bash - && \\
     |    curl -sL https://dl.yarnpkg.com/debian/pubkey.gpg | sudo -E apt-key add - && \\
     |    echo "deb https://dl.yarnpkg.com/debian/ stable main" | sudo -E tee /etc/apt/sources.list.d/yarn.list && \\
     |    sudo -E apt-get update && \\
     |    sudo -E apt-get -y install nodejs && \\
     |    sudo -E apt-get -y install yarn && \\
     |    curl https://cli-assets.heroku.com/install.sh | sh && \\
     |    heroku plugins:install java && \\
     |    mkdir ~/bin && \\
     |    curl -L -o ~/bin/mill "https://github.com/lihaoyi/mill/releases/download/$millVersion/$millVersion" && \\
     |    chmod +x ~/bin/mill
     |ENV PATH "~/bin:$$PATH"
     |""".stripMargin
