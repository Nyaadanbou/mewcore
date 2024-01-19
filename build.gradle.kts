import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("cc.mewcraft.repo-conventions")
    id("cc.mewcraft.java-conventions")
    id("cc.mewcraft.deploy-conventions")
    id("cc.mewcraft.publishing-conventions")
    alias(libs.plugins.pluginyml.paper)
}

project.ext.set("name", "MewCore")

group = "cc.mewcraft.mewcore"
version = "5.17.4"
description = "Common code of all Mewcraft plugins."

// Reference: https://youtrack.jetbrains.com/issue/IDEA-276365
/*configurations {
    compileOnly {
        isTransitive = false
    }
}*/

dependencies {
    // Dependencies at [compile path]
    // These dependencies are essential for plugin development,
    // so we just expose them to the consumers at compile path.
    // REMOVED

    // Dependencies at [runtime path]
    // These dependencies will be shaded in the final jar.
    // Consumers are expected to declare these dependencies
    // at [compile path] if they need these dependencies.
    // REMOVED

    // The Minecraft server API
    compileOnly(libs.server.paper)

    // Plugin libs - these will present as other plugins
    compileOnly(libs.helper)

    // Libs to be shaded
    implementation(libs.configurate.yaml)

    // Testing dependencies
    testImplementation(libs.junit4)
    testImplementation(libs.helper)
    testImplementation(libs.server.paper)
}

paper {
    main = "cc.mewcraft.mewcore.MewCorePlugin"
    name = project.ext.get("name") as String
    version = "${project.version}"
    description = project.description
    apiVersion = "1.19"
    author = "Nailm"
    serverDependencies {
        register("helper") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}