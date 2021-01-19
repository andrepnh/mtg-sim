plugins {
    java
    id("me.champeau.gradle.jmh") version "0.5.2"
}

group = "andrepnh"
version = "1.0"

repositories {
    jcenter()
    gradlePluginPortal()
}

dependencies {
    implementation("io.magicthegathering:javasdk:0.0.18")
    implementation("io.projectreactor:reactor-core:3.4.1")
    implementation("io.projectreactor:reactor-tools:3.4.1")

    implementation("com.google.code.gson:gson:2.8.6")
    implementation("io.vavr:vavr:0.10.3")
    implementation("com.google.guava:guava:30.0-jre")

    implementation("org.slf4j:slf4j-api:1.7.30")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.0")
    implementation("com.lmax:disruptor:3.4.2") // async logging
    implementation("org.fusesource.jansi:jansi:1.18") // log colors for Windows; see log4j2 config XMLs

    implementation("org.projectlombok:lombok:1.18.16")
    annotationProcessor("org.projectlombok:lombok:1.18.16")

    testImplementation("org.assertj:assertj-core:3.18.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.mockito:mockito-junit-jupiter:3.7.7")
    testImplementation("io.projectreactor:reactor-test:3.4.2")
}

jmh {
    iterations = 1
    benchmarkMode = listOf("avgt") // [Throughput/thrpt, AverageTime/avgt, SampleTime/sample, SingleShotTime/ss, All/all]
    fork = 10 // How many times to forks a single benchmark. Use 0 to disable forking altogether
    failOnError = true
    warmupForks = 0 // How many warmup forks to make for a single benchmark. 0 to disable warmup forks.
    warmupIterations = 0 // Number of warmup iterations to do.
    duplicateClassesStrategy = DuplicatesStrategy.WARN
    timeUnit = "ms"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}