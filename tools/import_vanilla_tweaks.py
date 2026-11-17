"""Imports Vanilla Tweaks textures and generates the item models Visible Ores switches to in Item mode.

Usage: python tools/import_vanilla_tweaks.py <VanillaTweaks.zip> [--force]

The zip must contain the packs SuspiciousSandGravelBorders, BuddingAmethystBorders, VisualInfestedStoneItems and
VisualWaxedCopperItems. Ore item models are generated from the textures already in textures/block/<variant>/.
Everything is written under src/main/resources/assets/visibleores; run it again after updating the pack.
Block and entity textures that already exist are kept, because they may have been edited by hand;
pass --force to replace them with the pack's versions too.
"""
import json
import pathlib
import re
import sys
import zipfile

ROOT = pathlib.Path(__file__).resolve().parent.parent / "src/main/resources/assets/visibleores"
NS = "visibleores"

ORES_CUBE = [
    "nether_quartz_ore", "nether_gold_ore", "gilded_blackstone",
    "redstone_ore", "lapis_ore", "iron_ore", "gold_ore", "emerald_ore", "diamond_ore", "copper_ore", "coal_ore",
    "deepslate_redstone_ore", "deepslate_lapis_ore", "deepslate_iron_ore", "deepslate_gold_ore",
    "deepslate_emerald_ore", "deepslate_diamond_ore", "deepslate_copper_ore", "deepslate_coal_ore",
]
VARIANTS = ["standard", "fancy", "blue_standard", "blue_fancy"]
FACES = ["down", "up", "north", "south", "west", "east"]

# Block textures of these packs are copied as they are.
VT_BLOCK_TEXTURES = ["suspicious_sand_0", "suspicious_sand_1", "suspicious_sand_2", "suspicious_sand_3",
                     "suspicious_gravel_0", "suspicious_gravel_1", "suspicious_gravel_2", "suspicious_gravel_3",
                     "budding_amethyst"]
# Item textures that are full block faces, so Block mode can use them too.
INFESTED_FACES = ["infested_stone", "infested_cobblestone", "infested_stone_bricks", "infested_mossy_stone_bricks",
                  "infested_cracked_stone_bricks", "infested_chiseled_stone_bricks", "infested_deepslate",
                  "infested_deepslate_top"]
WAXED_FACES = [f"waxed_{p}{n}" for p in ["", "exposed_", "weathered_", "oxidized_"]
               for n in (["copper_block"] if p == "" else ["copper"]) + ["cut_copper", "chiseled_copper", "copper_grate",
                                                                         "copper_bulb", "copper_trapdoor"]]


FORCE = False


def write_texture(path, data):
    """Writes a block or entity texture unless a (possibly hand-edited) copy is already there."""
    if path.exists() and not FORCE:
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(data)


def write_json(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8", newline="\n")


def item_definition(variant, name):
    write_json(ROOT / f"items/{variant}/{name}.json",
               {"model": {"type": "minecraft:model", "model": f"{NS}:item/{variant}/{name}"}})


def cube_model(textures, emissive):
    """textures: face -> texture id; emissive: face -> overlay texture id (may be empty)."""
    keys = {}
    model_textures = {"particle": textures["north"]}
    for face in FACES:
        key = f"t_{face}"
        model_textures[key] = textures[face]
        keys[face] = key
        if face in emissive:
            model_textures[f"e_{face}"] = emissive[face]
    elements = [{"from": [0, 0, 0], "to": [16, 16, 16],
                 "faces": {f: {"texture": f"#{keys[f]}", "cullface": f} for f in FACES}}]
    if emissive:
        elements.append({"from": [0, 0, 0], "to": [16, 16, 16], "light_emission": 15,
                         "faces": {f: {"texture": f"#e_{f}", "cullface": f} for f in FACES if f in emissive}})
    return {"parent": "minecraft:block/block", "textures": model_textures, "elements": elements}


def generate_cube_item(variant, item, face_textures):
    """face_textures: face -> texture file name inside textures/block/<variant>/."""
    folder = ROOT / f"textures/block/{variant}"
    if not all((folder / f"{t}.png").exists() for t in face_textures.values()):
        return False
    textures = {f: f"{NS}:block/{variant}/{t}" for f, t in face_textures.items()}
    emissive = {f: f"{NS}:block/{variant}/{t}_e" for f, t in face_textures.items() if (folder / f"{t}_e.png").exists()}
    write_json(ROOT / f"models/item/{variant}/{item}.json", cube_model(textures, emissive))
    item_definition(variant, item)
    return True


def all_faces(texture):
    return {f: texture for f in FACES}


def generate_ore_items():
    count = 0
    for variant in VARIANTS:
        for ore in ORES_CUBE:
            count += generate_cube_item(variant, ore, all_faces(ore))
        column = {f: "ancient_debris_top" if f in ("up", "down") else "ancient_debris_side" for f in FACES}
        count += generate_cube_item(variant, "ancient_debris", column)
    return count


def retarget(value, item_textures, own_models):
    """Points texture and parent references of a Vanilla Tweaks item model to the copies in our namespace."""
    match = re.fullmatch(r"(?:minecraft:)?item/(.+)", value)
    if match and match.group(1) in item_textures:
        return f"{NS}:item/standard/{match.group(1)}"
    if match and match.group(1) in own_models:
        return f"{NS}:item/standard/{match.group(1)}"
    return value


def import_pack(zip_path):
    with zipfile.ZipFile(zip_path) as pack:
        names = pack.namelist()

        def read(name):
            return pack.read(name)

        for texture in VT_BLOCK_TEXTURES:
            write_texture(ROOT / f"textures/block/standard/{texture}.png", read(f"assets/minecraft/textures/block/{texture}.png"))

        item_textures = sorted(pathlib.PurePosixPath(n).stem for n in names
                               if n.startswith("assets/minecraft/textures/item/") and n.endswith(".png"))
        for texture in item_textures:
            data = read(f"assets/minecraft/textures/item/{texture}.png")
            target = ROOT / f"textures/item/standard/{texture}.png"
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(data)
            if texture in INFESTED_FACES or texture in WAXED_FACES:
                write_texture(ROOT / f"textures/block/standard/{texture}.png", data)
            meta = f"assets/minecraft/textures/item/{texture}.png.mcmeta"
            if meta in names:
                (ROOT / f"textures/item/standard/{texture}.png.mcmeta").write_bytes(read(meta))

        models = {pathlib.PurePosixPath(n).stem: n for n in names
                  if n.startswith("assets/minecraft/models/item/") and n.endswith(".json")}
        for name, path in models.items():
            model = json.loads(read(path).decode("utf-8"))
            if "parent" in model:
                model["parent"] = retarget(model["parent"], item_textures, models)
            for key, value in model.get("textures", {}).items():
                if not value.startswith("#"):
                    model["textures"][key] = retarget(value, item_textures, models)
            write_json(ROOT / f"models/item/standard/{name}.json", model)

        # Chest and golem statue item textures use the vanilla entity texture layout, so placed waxed chests and
        # statues can use them too. Double chest halves (_left/_right) have no Vanilla Tweaks texture and are drawn by hand.
        for oxidation in ["", "exposed_", "weathered_", "oxidized_"]:
            suffix = "_" + oxidation.rstrip("_") if oxidation else ""
            entity_textures = {
                f"waxed_{oxidation}copper_chest": f"textures/entity/chest/waxed_copper{suffix}.png",
                f"waxed_{oxidation}copper_golem_statue": f"textures/entity/copper_golem/waxed_copper_golem{suffix}.png",
            }
            for source, target in entity_textures.items():
                write_texture(ROOT / target, read(f"assets/minecraft/textures/item/{source}.png"))

        items = [pathlib.PurePosixPath(n).stem for n in names
                 if n.startswith("assets/minecraft/items/") and n.endswith(".json")]
        for item in items:
            item_definition("standard", item)

    # Block mode textures of these blocks come from the copied block textures, Item mode from these models.
    generated = 0
    for texture in ["suspicious_sand_0", "suspicious_gravel_0", "budding_amethyst"]:
        item = texture.removesuffix("_0")
        generated += generate_cube_item("standard", item, all_faces(texture))
    return len(item_textures), len(models), len(items), generated


def main():
    global FORCE
    args = [arg for arg in sys.argv[1:] if arg != "--force"]
    FORCE = len(args) != len(sys.argv) - 1
    if len(args) != 1:
        sys.exit(__doc__)
    textures, models, items, generated = import_pack(args[0])
    ores = generate_ore_items()
    print(f"Imported {textures} item textures, {models} item models and {items} item definitions; "
          f"generated {generated} block item models and {ores} ore item models.")


if __name__ == "__main__":
    main()
