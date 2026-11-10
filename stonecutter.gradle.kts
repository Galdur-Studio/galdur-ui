plugins {
    id("dev.kikugie.stonecutter")
}

// The version src/ is currently written for. Change it with the "Set active project to ..." tasks
// (group "stonecutter"), never by hand.
stonecutter active "1.21.1-neoforge"

stonecutter parameters {
    val loader = current.project.substringAfterLast('-')

    // stonecutter.properties.toml: the [<minecraft>] and [<loader>."<minecraft>"] tables apply.
    properties {
        tags(current.version, loader)
    }

    // Lets the code test the loader: //? if fabric { ... //?}
    constants {
        match(loader, "neoforge", "fabric", "forge")
    }

    // Names Mojang changed, replaced in the whole source (both ways when switching versions).
    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }
        // Minecraft is no longer obfuscated: the access widener becomes a class tweaker.
        string(current.parsed >= "26.1") {
            replace("accessWidener v2 named", "classTweaker v2 official")
        }
        // 26.1: the GUI no longer draws directly, it "extracts" what to draw (GuiGraphicsExtractor),
        // and the render methods are renamed accordingly. Whole words only, so each rename can be
        // reversed when switching back to an older version.
        regex(current.parsed >= "26.1") {
            fun rename(old: String, new: String) = replace("""\b$old\b""" to new, """\b$new\b""" to old)
            rename("GuiGraphics", "GuiGraphicsExtractor")
            rename("renderWidget", "extractWidgetRenderState")
            rename("renderBackground", "extractBackground")
            rename("renderTransparentBackground", "extractTransparentBackground")
            rename("renderTooltip", "extractTooltip")
            rename("renderLabels", "extractLabels")
            // render(GuiGraphics ...) of Renderable and Screen, not the other render(...) methods.
            replace("""\brender\((?=(?:GuiGraphics\w*|\w*[gG]raphics)\b)""" to "extractRenderState(",
                """\bextractRenderState\(""" to "render(")
        }
    }
}
