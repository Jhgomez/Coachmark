plugins {
    alias(libs.plugins.android.library)
//    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.20"
    id("com.vanniktech.maven.publish") version "0.35.0"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

android {
    namespace = "okik.tech.coachmark"
    compileSdk = 36

    defaultConfig {
        minSdk = 18

        aarMetadata {
            minCompileSdk = 18
        }
    }

//    publishing {
//        singleVariant("release") {
//            withSourcesJar()
//            withJavadocJar()
//        }
//    }
}

//publishing {
//    publications {
//        create<MavenPublication>("release") {
//            groupId = "okik.tech.coachmark"
//            artifactId = "coachmark"
//            version = "1.0"
//
//            afterEvaluate {
//                from(components["release"])
//            }
//
//            pom {
//                name = "coachmark"
//                description = "Coach mark/tutorial overlay for Android"
//                url = "https://github.com/your/repo"
//
//                licenses {
//                    license {
//                        name = "Apache-2.0"
//                        url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
//                    }
//                }
//
//                scm {
//                    url = ("https://github.com/your/repo")
//                    connection = "scm:git:git://github.com/your/repo.git"
//                    developerConnection = "scm:git:ssh://github.com/your/repo.git"
//                }
//                developers {
//                    developer {
//                        id = "you"
//                        name = "Your Name"
//                    }
//                }
//            }
//        }
//    }

//    repositories {
//        maven {
//            name = "coachmark"
//            url = uri(layout.buildDirectory.dir("coachmarklib"))
//        }
//    }
//}


mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates("okik.tech", "coachmark", "1.0")

    pom {
        name = "coachmark"
        description = "Coach mark/tutorial overlay for Android"
        inceptionYear = "2020"
        url = "https://github.com/jhgomez/coachmark/"

        licenses {
            license {
                name = "he Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                id.set("username")
                name.set("User Name")
                url.set("https://github.com/username/")
            }
        }

        scm {
            url = "https://github.com/jhgomez/coachmark/"
            connection = "scm:git:git://github.com/jhgomez/repo.git"
            developerConnection = "scm:git:ssh://git@github.com/username/mylibrary.git"
        }
    }

}

dependencies {
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
}