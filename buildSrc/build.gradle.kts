plugins {
    `kotlin-dsl`
}

val wrapperUser: String = System.getProperty("gradle.wrapperUser")
val wrapperPassword: String = System.getProperty("gradle.wrapperPassword")

repositories {
    mavenCentral()
    google()
}
