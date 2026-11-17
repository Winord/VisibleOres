stonecutter active "26.3-fabric"

stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    properties {
        tags(version, loader)
    }

    constants {
        match(loader, "fabric", "neoforge")
    }

    files.disable(loader != "fabric", "winord/visibleores/fabric/")
    files.disable(loader != "neoforge", "winord/visibleores/neoforge/")

    replacements {
        string(current.parsed < "26.3") {
            replace("InputConstants.Type.KEYBOARD", "InputConstants.Type.KEYSYM")
            replace("InputConstants.isKeyDown(key.getValue())", "InputConstants.isKeyDown(minecraft.getWindow(), key.getValue())")
            replace("getVanillaPackResources().fullResources()", "getVanillaPackResources()")
        }

        string(current.parsed < "26.2") {
            replace("minecraft.gui.screen()", "minecraft.screen")
            replace("minecraft.gui.setScreen(", "minecraft.setScreen(")
            replace("gui.hud.setOverlayMessage(", "gui.setOverlayMessage(")
            replace("minecraft.levelExtractor.", "minecraft.levelRenderer.")
        }
    }
}
