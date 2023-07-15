name := "scala_lenses_class"

version := "0.1"

scalaVersion := "2.12.17"

val monocleVersion = "2.0.0" // depends on cats 2.x

libraryDependencies ++= Seq(
  "com.github.julien-truffaut" %%  "monocle-core"  % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-macro" % monocleVersion,
  "com.github.julien-truffaut" %%  "monocle-law"   % monocleVersion % "test"
)

resolvers += Resolver.sonatypeRepo("releases")
addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0")

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.16" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-flatspec" % "3.2.16" % "test"