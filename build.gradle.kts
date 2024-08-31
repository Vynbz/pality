plugins {
    id("java")
}

allprojects {
    group = "io.pality"
    version = "1.0.0"
}

subprojects {
    apply(plugin = "java")

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.jar {
        manifest {
            attributes(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version
            )
        }
    }

}

tasks.jar {
    enabled = false
}
