package dev.jwall64.mdproxy;

import java.util.List;
import java.util.Map;

import dev.jwall64.mdcfg.ConfigList;
import dev.jwall64.mdcfg.ConfigObject;
import dev.jwall64.mdcfg.ConfigSection;
import dev.jwall64.mdcfg.registry.Identifier;
import dev.jwall64.mdproxy.RegistryData.TagList;

public final class DynamicRegistries {

    private static final String DUMMY_SOUND_EVENT = "minecraft:entity.generic.eat";

    private static final ConfigSection DUMMY_CAT_SOUND_VARIANT = new ConfigSection()
                .with("ambient_sound", DUMMY_SOUND_EVENT)
                .with("stray_ambient_sound", DUMMY_SOUND_EVENT)
                .with("hiss_sound", DUMMY_SOUND_EVENT)
                .with("hurt_sound", DUMMY_SOUND_EVENT)
                .with("death_sound", DUMMY_SOUND_EVENT)
                .with("eat_sound", DUMMY_SOUND_EVENT)
                .with("beg_for_food_sound", DUMMY_SOUND_EVENT)
                .with("purr_sound", DUMMY_SOUND_EVENT)
                .with("purreow_sound", DUMMY_SOUND_EVENT);

    private static final ConfigSection DUMMY_WOLF_SOUND_VARIANT = new ConfigSection()
                .with("ambient_sound", DUMMY_SOUND_EVENT)
                .with("death_sound", DUMMY_SOUND_EVENT)
                .with("growl_sound", DUMMY_SOUND_EVENT)
                .with("hurt_sound", DUMMY_SOUND_EVENT)
                .with("pant_sound", DUMMY_SOUND_EVENT)
                .with("whine_sound", DUMMY_SOUND_EVENT)
                .with("step_sound", DUMMY_SOUND_EVENT);

    public static final ConfigSection DUMMY_SOUND_VARIANT = new ConfigSection()
                .with("ambient_sound", DUMMY_SOUND_EVENT)
                .with("hurt_sound", DUMMY_SOUND_EVENT)
                .with("death_sound", DUMMY_SOUND_EVENT)
                .with("step_sound", DUMMY_SOUND_EVENT)
                .with("eat_sound", DUMMY_SOUND_EVENT);

    public static final ConfigSection DUMMY_TRIM_MATERIAL = new ConfigSection()
                .with("asset_name", "amethyst")
                .with("palette_id", "minecraft:trim/amethyst")
                .with("description", "dummy");

    public static final ConfigSection DUMMY_JUKEBOX_SONG = new ConfigSection()
                .with("comparator_output", 1)
                .with("description", "dummy")
                .with("length_in_seconds", 1.0)
                .with("sound_event", DUMMY_SOUND_EVENT);

    public static final ConfigSection DUMMY_INSTRUMENT = new ConfigSection()
                .with("description", "dummy")
                .with("sound_event", DUMMY_SOUND_EVENT)
                .with("range", 1.0)
                .with("use_duration", 1.0);

    public static final ConfigObject DUMMY_BLOCK_TRANSFORMER = new ConfigList()
        .append(new ConfigSection()
                .with("block_state_provider", new ConfigSection()
                    .with("type", "simple")
                    .with("state", new ConfigSection()
                        .with("id", "minecraft:dirt"))
                    )
                );
    
    public static final ConfigObject DUMMY_POT_PATTERN = new ConfigSection()
        .with("asset_id", "minecraft:angler_pottery_pattern");

    public static final List<RegistryData> REQUIRED_REGISTRIES = List.of(
        // Needs some tags for components
        RegistryData.builder("damage_type")
            .add("generic", new ConfigSection()
                .with("exhaustion", 0.0)
                .with("message_id", "generic")
                .with("scaling", "when_caused_by_living_non_player"))
            .add("spear", new ConfigSection()
                .with("exhaustion", 0.1)
                .with("message_id", "spear")
                .with("scaling", "when_caused_by_living_non_player"))
        .build(),

        RegistryData.builder("banner_pattern")
            .add("base", new ConfigSection()
                .with("asset_id", "minecraft:base")
                .with("translation_key", "block.minecraft.banner.base"))
        .build(),

        // Must not be empty
        RegistryData.builder("cat_sound_variant")
            .add("classic", new ConfigSection()
                .with("adult_sounds", DUMMY_CAT_SOUND_VARIANT)
                .with("baby_sounds", DUMMY_CAT_SOUND_VARIANT))
        .build(),

        RegistryData.builder("cat_variant")
            .add("tabby", new ConfigSection()
                .with("asset_id", "entity/cat/cat_jellie")
                .with("baby_asset_id", "entity/cat/cat_jellie_baby")
                .with("spawn_conditions", new ConfigList()))
        .build(),

        RegistryData.builder("chicken_sound_variant")
            .add("classic", new ConfigSection()
                .with("adult_sounds", DUMMY_SOUND_VARIANT)
                .with("baby_sounds", DUMMY_SOUND_VARIANT))
        .build(),

        RegistryData.builder("chicken_variant")
            .add("temperate", new ConfigSection()
                .with("asset_id", "entity/chicken/chicken_temperate")
                .with("baby_asset_id", "entity/chicken/chicken_temperate_baby")
                .with("spawn_conditions", new ConfigList()))
            .add("cold", new ConfigSection()
                .with("asset_id", "entity/chicken/chicken_cold")
                .with("baby_asset_id", "entity/chicken/chicken_cold_baby")
                .with("spawn_conditions", new ConfigList()))
            .add("warm", new ConfigSection()
                .with("asset_id", "entity/chicken/chicken_warm")
                .with("baby_asset_id", "entity/chicken/chicken_warm_baby")
                .with("spawn_conditions", new ConfigList()))
        .build(),

        RegistryData.builder("cow_sound_variant")
            .add("classic", DUMMY_SOUND_VARIANT)
        .build(),

        RegistryData.builder("cow_variant")
            .add("temperate", new ConfigSection()
                .with("asset_id", "entity/cow/cow_temperate")
                .with("baby_asset_id", "entity/cow/cow_temperate_baby")
                .with("spawn_conditions", new ConfigList())
        ).build(),

        RegistryData.builder("painting_variant")
            .add("dummy", new ConfigSection()
                .with("width", 1)
                .with("height", 1)
                .with("asset_id", "kebab")
                .with("title", "Dummy")
                .with("author", "MidnightProxy")
        ).build(),

        RegistryData.builder("frog_variant")
            .add("temperate", new ConfigSection()
                .with("asset_id", "entity/frog/frog_temperate")
                .with("spawn_conditions", new ConfigList())
        ).build(),

        RegistryData.builder("pig_sound_variant")
            .add("classic", new ConfigSection()
                .with("adult_sounds", DUMMY_SOUND_VARIANT)
                .with("baby_sounds", DUMMY_SOUND_VARIANT)
        ).build(),

        RegistryData.builder("pig_variant")
            .add("temperate", new ConfigSection()
                .with("asset_id", "entity/pig/pig_temperate")
                .with("baby_asset_id", "entity/pig/pig_temperate_baby")
                .with("spawn_conditions", new ConfigList())
        ).build(),

        RegistryData.builder("wolf_sound_variant")
            .add("classic", new ConfigSection()
                .with("adult_sounds", DUMMY_WOLF_SOUND_VARIANT)
                .with("baby_sounds", DUMMY_WOLF_SOUND_VARIANT)
        ).build(),

        RegistryData.builder("wolf_variant")
            .add("pale", new ConfigSection()
                .with("assets", new ConfigSection()
                    .with("angry", "entity/wolf/wolf_angry")
                    .with("wild", "entity/wolf/wolf")
                    .with("tame", "entity/wolf/wolf_tame")
                )
                .with("baby_assets", new ConfigSection()
                    .with("angry", "entity/wolf/wolf_angry_baby")
                    .with("wild", "entity/wolf/wolf_baby")
                    .with("tame", "entity/wolf/wolf_tame_baby")
                )
                .with("spawn_conditions", new ConfigList())
        ).build(),

        RegistryData.builder("zombie_nautilus_variant")
            .add("temperate", new ConfigSection()
                .with("asset_id", "zombie_nautilus")
                .with("model", "normal")
                .with("spawn_conditions", new ConfigList())
        ).build(),

        // Must have all entries
        RegistryData.builder("jukebox_song")
            .add("11", DUMMY_JUKEBOX_SONG)
            .add("13", DUMMY_JUKEBOX_SONG)
            .add("5", DUMMY_JUKEBOX_SONG)
            .add("blocks", DUMMY_JUKEBOX_SONG)
            .add("cat", DUMMY_JUKEBOX_SONG)
            .add("chirp", DUMMY_JUKEBOX_SONG)
            .add("creator", DUMMY_JUKEBOX_SONG)
            .add("creator_music_box", DUMMY_JUKEBOX_SONG)
            .add("far", DUMMY_JUKEBOX_SONG)
            .add("lava_chicken", DUMMY_JUKEBOX_SONG)
            .add("mall", DUMMY_JUKEBOX_SONG)
            .add("mellohi", DUMMY_JUKEBOX_SONG)
            .add("otherside", DUMMY_JUKEBOX_SONG)
            .add("pigstep", DUMMY_JUKEBOX_SONG)
            .add("precipice", DUMMY_JUKEBOX_SONG)
            .add("relic", DUMMY_JUKEBOX_SONG)
            .add("stal", DUMMY_JUKEBOX_SONG)
            .add("strad", DUMMY_JUKEBOX_SONG)
            .add("tears", DUMMY_JUKEBOX_SONG)
            .add("wait", DUMMY_JUKEBOX_SONG)
            .add("ward", DUMMY_JUKEBOX_SONG)
            .add("bounce", DUMMY_JUKEBOX_SONG)
        .build(),

        RegistryData.builder("trim_material")
            .add("amethyst", DUMMY_TRIM_MATERIAL)
            .add("copper", DUMMY_TRIM_MATERIAL)
            .add("diamond", DUMMY_TRIM_MATERIAL)
            .add("emerald", DUMMY_TRIM_MATERIAL)
            .add("gold", DUMMY_TRIM_MATERIAL)
            .add("iron", DUMMY_TRIM_MATERIAL)
            .add("lapis", DUMMY_TRIM_MATERIAL)
            .add("netherite", DUMMY_TRIM_MATERIAL)
            .add("quartz", DUMMY_TRIM_MATERIAL)
            .add("redstone", DUMMY_TRIM_MATERIAL)
            .add("resin", DUMMY_TRIM_MATERIAL)
            .build(),

        RegistryData.builder("instrument")
            .add("admire_goat_horn", DUMMY_INSTRUMENT)
            .add("call_goat_horn", DUMMY_INSTRUMENT)
            .add("dream_goat_horn", DUMMY_INSTRUMENT)
            .add("feel_goat_horn", DUMMY_INSTRUMENT)
            .add("ponder_goat_horn", DUMMY_INSTRUMENT)
            .add("seek_goat_horn", DUMMY_INSTRUMENT)
            .add("sing_goat_horn", DUMMY_INSTRUMENT)
            .add("yearn_goat_horn", DUMMY_INSTRUMENT)
            .build()
    );

    public static final RegistryData BLOCK_TRANSFORMERS = RegistryData.builder("block_transformer")
        .add("axe", DUMMY_BLOCK_TRANSFORMER)
        .add("hoe", DUMMY_BLOCK_TRANSFORMER)
        .add("shovel", DUMMY_BLOCK_TRANSFORMER)
        .build();

    public static final RegistryData POT_PATTERNS = RegistryData.builder("decorated_pot_pattern")
        .add("angler", DUMMY_POT_PATTERN)
        .add("archer", DUMMY_POT_PATTERN)
        .add("arms_up", DUMMY_POT_PATTERN)
        .add("blade", DUMMY_POT_PATTERN)
        .add("brewer", DUMMY_POT_PATTERN)
        .add("burn", DUMMY_POT_PATTERN)
        .add("danger", DUMMY_POT_PATTERN)
        .add("explorer", DUMMY_POT_PATTERN)
        .add("flow", DUMMY_POT_PATTERN)
        .add("friend", DUMMY_POT_PATTERN)
        .add("guster", DUMMY_POT_PATTERN)
        .add("heart", DUMMY_POT_PATTERN)
        .add("heartbreak", DUMMY_POT_PATTERN)
        .add("howl", DUMMY_POT_PATTERN)
        .add("miner", DUMMY_POT_PATTERN)
        .add("mourner", DUMMY_POT_PATTERN)
        .add("plenty", DUMMY_POT_PATTERN)
        .add("prize", DUMMY_POT_PATTERN)
        .add("scrape", DUMMY_POT_PATTERN)
        .add("sheaf", DUMMY_POT_PATTERN)
        .add("shelter", DUMMY_POT_PATTERN)
        .add("skull", DUMMY_POT_PATTERN)
        .add("snort", DUMMY_POT_PATTERN)
        .build();
 
 
    public static final Map<Identifier, List<TagList>> REQUIRED_TAGS = Map.of(
        new Identifier("minecraft", "damage_type"), List.of(
            TagList.builder("is_fire").build(),
            TagList.builder("is_explosion").build(),
            TagList.builder("bypasses_shield").build(),
            TagList.builder("bypasses_armor").build()
        ),
        new Identifier("minecraft", "banner_pattern"), List.of(
            TagList.builder("no_item_required").build(),
            TagList.builder("pattern_item/field_masoned").build(),
            TagList.builder("pattern_item/flower").build(),
            TagList.builder("pattern_item/flow").build(),
            TagList.builder("pattern_item/creeper").build(),
            TagList.builder("pattern_item/globe").build(),
            TagList.builder("pattern_item/guster").build(),
            TagList.builder("pattern_item/bordure_indented").build(),
            TagList.builder("pattern_item/mojang").build(),
            TagList.builder("pattern_item/skull").build(),
            TagList.builder("pattern_item/piglin").build()
        )
    );

}
