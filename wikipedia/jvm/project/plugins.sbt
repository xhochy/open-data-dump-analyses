// SBT Eclipse
resolvers += Classpaths.typesafeResolver

resolvers += "sonatype-public" at "https://oss.sonatype.org/content/groups/public"

addSbtPlugin("com.typesafe.sbteclipse" % "sbteclipse-plugin" % "3.0.0")

// sbt-assembly
resolvers += Resolver.url("artifactory", url("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")
