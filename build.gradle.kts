plugins {
	id ("org.springframework.boot") version "2.5.1"
	id ("io.spring.dependency-management") version "1.0.11.RELEASE"
	id ("java")
	id ("war")
	id ("jacoco")
}

group = "online.C-A-L"
version = "2.0.2.2"

subprojects {
    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

repositories {
	mavenCentral()
}

configurations.forEach { it.exclude("org.springframework.boot", "spring-boot-starter-tomcat") }

dependencies {
    implementation ("org.springframework.boot:spring-boot-starter-parent:2.4.5")
    implementation ("org.springframework.boot:spring-boot-starter-web")
    implementation ("org.springframework.boot:spring-boot-starter-jetty")
    implementation ("org.springframework.boot:spring-boot-starter-security")
    
    implementation ("org.springframework:spring-websocket")
    implementation ("org.springframework:spring-messaging")
    implementation ("org.springframework.security:spring-security-messaging")
    implementation ("com.auth0:java-jwt:3.3.0")
    implementation ("org.eclipse.jetty:jetty-http")
    implementation ("org.mongodb:mongo-java-driver:3.12.7")

	providedCompile( "org.springframework.boot:spring-boot-starter-jetty"){
		exclude (group = "org.eclipse.jetty", module="jetty-http")
		exclude (group = "org.eclipse.jetty", module="jetty-io")
		exclude (group = "org.eclipse.jetty", module="jetty-util")
	}

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation ("junit:junit")
    testRuntimeOnly ("org.junit.vintage:junit-vintage-engine")
}

tasks.processResources {
    filesMatching("application.properties") {
        expand (project.properties)
    }
}

tasks.bootWar {
	manifest {
		attributes("Implementation-Version" to archiveVersion)
	}
}

tasks.test{
      useJUnitPlatform()	
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}


tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
    }
    dependsOn(tasks.test) // tests are required to run before generating the report
}

tasks.jacocoTestReport {
  
}
