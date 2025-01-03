fun properties(key: String) = project.findProperty(key).toString()
fun rootProperties(key: String) = rootProject.findProperty(key).toString()

group = rootProperties("group")
version = rootProperties("version")

// Run server
runServer {
    version.set(rootProperties("spigot.version"))
    javaVersion.set(JavaVersion.VERSION_21)
}

// Fairy configuration
fairy {
    name.set(properties("name"))
    mainPackage.set(properties("package"))
    fairyPackage.set("io.fairyproject")

    bukkitProperties().depends.add("fairy-lib-plugin")

    bukkitProperties().foliaSupported = true
    bukkitProperties().bukkitApi = rootProperties("spigot.version")
}

// Dependencies
dependencies {
    // Annotation module
    compileOnly(project(":annotation"))

    // Cache module
    compileOnly(project(":cache"))

    // Mongodb module
    compileOnly(project(":mongodb"))
}