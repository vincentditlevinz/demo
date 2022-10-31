import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.google.protobuf.gradle.*

plugins {
	id("org.springframework.boot") version "2.7.5"
	id("io.spring.dependency-management") version "1.0.15.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	id("idea")
	id("java")
	id("com.google.protobuf") version "0.9.1"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

val nexusSaagieUrl: String by project
val nexusSaagieUsername: String by project
val nexusSaagiePassword: String by project

repositories {
	mavenCentral()
	maven {
		name = "SaagieRepository"
		url = uri("$nexusSaagieUrl/repository/saagie-repository/")
		credentials {
			username = nexusSaagieUsername
			password = nexusSaagiePassword
		}
	}
	maven {
		name = "SaagieStagingRepository"
		url = uri("$nexusSaagieUrl/repository/saagie-staging")
		credentials {
			username = nexusSaagieUsername
			password = nexusSaagiePassword
		}
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.apache.camel.springboot:camel-spring-boot-starter:3.19.0")
	implementation("org.apache.camel.springboot:camel-spring-rabbitmq-starter:3.19.0")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.google.protobuf:protobuf-java:3.21.8")
	implementation("com.google.protobuf:protobuf-java-util:3.21.8")
	implementation("io.grpc:grpc-stub:1.50.2")
	implementation("io.grpc:grpc-protobuf:1.50.2")
	implementation("org.apache.camel:camel-minio:3.19.0")
	implementation("org.apache.camel.springboot:camel-minio-starter:3.19.0")
	implementation("org.apache.camel.springboot:camel-file-starter:3.19.0")
	implementation("org.apache.camel.springboot:camel-kubernetes-starter:3.19.0")
	implementation("org.apache.camel:camel-jackson:3.19.0")
	implementation("org.apache.camel.springboot:camel-jackson-starter:3.19.0")
	implementation("org.apache.camel.springboot:camel-rest-starter:3.19.0")
	implementation("org.apache.camel.springboot:camel-servlet-starter:3.19.0")
	implementation("io.saagie.projects-and-jobs:argo-model:1.19.2209301445-3683.110")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
	dependsOn("generateProto")
}

tasks.withType<Test> {
	useJUnitPlatform()
	dependsOn("generateProto")
}

protobuf {
	protoc {
		// The artifact spec for the Protobuf Compiler
		artifact = "com.google.protobuf:protoc:3.21.8"
	}
	plugins {
		// Optional: an artifact spec for a protoc plugin, with "grpc" as
		// the identifier, which can be referred to in the "plugins"
		// container of the "generateProtoTasks" closure.
		id("grpc") {
			artifact = "io.grpc:protoc-gen-grpc-java:1.50.2"
		}
	}
	generateProtoTasks {
		ofSourceSet("main").forEach {
			it.plugins {
				// Apply the "grpc" plugin whose spec is defined above, without
				// options. Note the braces cannot be omitted, otherwise the
				// plugin will not be added. This is because of the implicit way
				// NamedDomainObjectContainer binds the methods.
				id("grpc") { }
			}
		}
	}
}

