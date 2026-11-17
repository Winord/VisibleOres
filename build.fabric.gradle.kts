plugins {
    id("dev.kikugie.loom-back-compat")
}

version = "${sc.properties["mod.version"]}+${sc.properties.getOrNull("mod.mc_label") ?: sc.current.version}"
base.archivesName = "${sc.properties["mod.id"]}-fabric"

val requiredJava = JavaVersion.VERSION_25

repositories {
    exclusiveContent {
        forRepository { maven("https://api.modrinth.com/maven") { name = "Modrinth" } }
        filter { includeGroup("maven.modrinth") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    loomx.applyMojangMappings()

    modImplementation("net.fabricmc:fabric-loader:${sc.properties["deps.fabric_loader"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${sc.properties["deps.fabric_api"]}")

    modCompileOnly("maven.modrinth:modmenu:${sc.properties["deps.modmenu"]}") { isTransitive = false }
    modRuntimeOnly("maven.modrinth:modmenu:${sc.properties["deps.modmenu"]}") { isTransitive = false }
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run")
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("version", project.version.toString())
            register("minecraft", sc.properties["mod.mc_compat"])
            register("fabric_loader", sc.properties["deps.fabric_loader"])
            register("java", requiredJava.majorVersion)
        }

        filesMatching("fabric.mod.json") { expand(props) }
        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("META-INF/neoforge.mods.toml", "pack.mcmeta", "icon.png")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to build/libs/{mod version}/"

        inputs.property("version", sc.properties["mod.version"])
        from(loomx.modJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${sc.properties["mod.version"]}"))
    }
}
