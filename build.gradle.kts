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
val camelVersion = "3.19.0"
val grpcVersion = "1.48.1"
val protobufVersion = "3.21.8"


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
	implementation("org.apache.camel.springboot:camel-spring-boot-starter:$camelVersion")
	implementation("org.apache.camel.springboot:camel-spring-rabbitmq-starter:$camelVersion")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	implementation("com.google.protobuf:protobuf-java:$protobufVersion")
	implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
	implementation("io.grpc:grpc-stub:$grpcVersion")
	implementation("io.grpc:grpc-protobuf:$grpcVersion")
	implementation("org.apache.camel:camel-minio:$camelVersion")
	implementation("org.apache.camel.springboot:camel-minio-starter:$camelVersion")
	implementation("org.apache.camel.springboot:camel-file-starter:$camelVersion")
	implementation("org.apache.camel.springboot:camel-kubernetes-starter:$camelVersion")
	implementation("org.apache.camel:camel-jackson:$camelVersion")
	implementation("org.apache.camel.springboot:camel-jackson-starter:$camelVersion")
	implementation("org.apache.camel.springboot:camel-rest-starter:$camelVersion")
	implementation("org.apache.camel.springboot:camel-servlet-starter:$camelVersion")
	implementation("org.apache.camel.springboot:camel-grpc-starter:$camelVersion")
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

