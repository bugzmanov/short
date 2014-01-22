organization  := "bugzmanov"

version       := "0.01"

scalaVersion  := "2.10.2"

resolvers ++= Seq(
  "spray" at "http://repo.spray.io/"
)

libraryDependencies ++= Seq(
  "io.spray"          %   "spray-can"                 % "1.2-M8",
  "io.spray"          %   "spray-routing"             % "1.2-M8",
  "io.spray"          %   "spray-testkit"             % "1.2-M8" % "test",
  "com.typesafe.akka" %%  "akka-actor"                % "2.2.0-RC1",
  "org.specs2"        %%  "specs2"                    % "1.14" % "test",
  "org.scalikejdbc"   %%  "scalikejdbc"               % "[1.7,)",
  "org.scalikejdbc"   %%  "scalikejdbc-interpolation" % "[1.7,)",
  "com.h2database"    %   "h2"                        % "[1.3,)",
  "commons-codec"     %   "commons-codec"             % "1.8"
)

