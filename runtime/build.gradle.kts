description = "Implementation of runtime method replacement"

dependencies {
    implementation(project(":api"))
}

tasks.jar {
    manifest {
        attributes(
                "Premain-Class" to "io.pality.runtime.RuntimeAgent",
                "Can-Redefine-Classes" to "true",
                "Can-Retransform-Classes" to "true"
        )
    }
}
