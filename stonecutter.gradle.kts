plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom-remap") version "1.14-SNAPSHOT" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "1.21.11"

/*
// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}
 */

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod_version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    dependencies["fapi"] = node.project.property("fabric_version") as String
    dependencies["yacl"] = node.project.property("yacl_version") as String

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
        string(current.parsed >= "1.20") {
            replace("import com.mojang.blaze3d.vertex.PoseStack;", "import net.minecraft.client.gui.GuiGraphics;")
            replace("PoseStack", "GuiGraphics")
        }
        val yaclVersion = node.project.property("yacl_version") as String
        string(sc.compare(yaclVersion, "3.2.0") >= 0) {
            replace("config.ConfigEntry;", "config.v2.api.SerialEntry;")
            replace("@ConfigEntry", "@SerialEntry")
            replace("config.ConfigInstance;", "config.v2.api.ConfigClassHandler;")
            replace("ConfigInstance", "ConfigClassHandler")
        }
        string(sc.compare(yaclVersion, "3.0.0") >= 0) {
            replace("import dev.isxander.yacl.", "import dev.isxander.yacl3.")
        }
    }
}
