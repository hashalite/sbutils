import org.gradle.util.internal.VersionNumber

plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("maven-publish")
}

version = "${property("mod_version")}+${sc.current.version}"
base.archivesName = property("mod_id") as String

val requiredJava = when {
    sc.current.parsed >= "1.20.6" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

repositories {
    /**
     * Restricts dependency search of the given [groups] to the [maven URL][url],
     * improving the setup speed.
     */
    fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
        forRepository { maven(url) { name = alias } }
        filter { groups.forEach(::includeGroup) }
    }
    strictMaven("https://maven.isxander.dev/releases", "isXander", "dev.isxander", "dev.isxander.yacl", "org.quiltmc.parsers")
    strictMaven("https://maven.terraformersmc.com/", "TerraformersMC", "com.terraformersmc")
    strictMaven("https://maven.parchmentmc.org/", "Parchment", "org.parchmentmc.data")
    strictMaven("https://maven.nucleoid.xyz/", "Nucleoid", "eu.pb4")
}

dependencies {
    /**
     * Fetches only the required Fabric API modules to not waste time downloading all of them for each version.
     * @see <a href="https://github.com/FabricMC/fabric">List of Fabric API modules</a>
     */
    fun fapi(vararg modules: String) {
        for (it in modules) modImplementation(fabricApi.module(it, property("fabric_version") as String))
    }

    /**
     * YACL versions between [2.5.0, 3.3.2] are under an alternate groupId/artifactId.
     * Returns true if the current project is configured to use one of these versions.
     */
    fun isYaclAltPath(): Boolean {
        val yaclBaseVersion = "\\d+\\.\\d+\\.\\d+".toRegex().find(property("yacl_version") as String)?.value
        val range = VersionNumber.parse("2.5.0")..VersionNumber.parse("3.3.2")
        return VersionNumber.parse(yaclBaseVersion) in range
    }

    minecraft("com.mojang:minecraft:${sc.current.version}")
    val parchmentAppendix = (property("parchment_version") as String).split("-")[0]
    val parchmentVersion = (property("parchment_version") as String).split("-")[1]
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-${parchmentAppendix}:${parchmentVersion}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    if (sc.compare(property("fabric_version") as String, "0.90.0") >= 0) {
        fapi("fabric-lifecycle-events-v1", "fabric-command-api-v2", "fabric-key-binding-api-v1")
    }  else {
        modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    }

    modImplementation ("com.terraformersmc:modmenu:${property("modmenu_version")}") {
        exclude(module = "fabric-api")
    }

    val yaclAltPath = isYaclAltPath()
    val yaclGroup = "dev.isxander" + if (yaclAltPath) ".yacl" else ""
    val yaclArtifact = "yet-another-config-lib" + if (yaclAltPath) "-fabric" else ""

    modApi("${yaclGroup}:${yaclArtifact}:${property("yacl_version")}") {
        when (sc.current.version) {
            "1.19.2" -> {
                exclude(module = "fabric-loader")
            }
            "1.19.4" -> {
                exclude(module = "imageio-core")
                exclude(module = "imageio-webp")
                exclude(module = "imageio-metadata")
                exclude(module = "common-lang")
                exclude(module = "common-io")
                exclude(module = "common-image")
            }
        }
    }
    include("${yaclGroup}:${yaclArtifact}:${property("yacl_version")}")

    modApi("net.objecthunter:exp4j:${property("exp4j_version")}")
    include("net.objecthunter:exp4j:${property("exp4j_version")}")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json") // Useful for interface injection
    accessWidenerPath = sc.process(rootProject.file("src/main/resources/sbutils.accesswidener"), "build/dev.accesswidener")

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava
}

tasks {
    processResources {
        val props = mapOf(
            "id" to project.property("mod_id"),
            "name" to project.property("mod_name"),
            "version" to project.property("mod_version"),
            "minecraft" to project.property("mc_version")
        )

        props.forEach { inputs.property(it.key, it.value) }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod_version")}"))
        dependsOn("build")
    }
}

// Publishes builds to a maven repository under `com.example:template:0.1.0+mc`
publishing {
    repositories {
        maven("https://maven.example.com/releases") {
            name = "myMaven"
            // To authenticate, create `myMavenUsername` and `myMavenPassword` properties in your Gradle home properties.
            // See https://stonecutter.kikugie.dev/wiki/tips/properties#defining-properties
            credentials(PasswordCredentials::class.java)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "${property("mod_group")}"
            artifactId = property("mod_id") as String
            version = project.version as String

            from(components["java"])
        }
    }
}