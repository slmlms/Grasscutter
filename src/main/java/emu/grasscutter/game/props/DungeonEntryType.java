package emu.grasscutter.game.props;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum DungeonEntryType {

    DUNGEN_ENTRY_TYPE_RELIQUARY(0),
    DUNGEN_ENTRY_TYPE_WEAPON_PROMOTE(1),
    DUNGEON_ENTRY_TYPE_NORMAL(2),
    DUNGEN_ENTRY_TYPE_AVATAR_TALENT(3),
    DUNGEON_ENTRY_TYPE_OBSCURAE(5),
    DUNGEON_ENTRY_TYPE_TRIAL(6),
    DUNGEON_ENTRY_TYPE_EFFIGY(7),
    DUNGEON_ENTRY_TYPE_FLEUR_FAIR(8),
    DUNGEON_ENTRY_TYPE_CHANNELLER_SLAB_ONE_OFF(9),
    DUNGEON_ENTRY_TYPE_CHANNELLER_SLAB_LOOP(10),
    DUNGEON_ENTRY_TYPE_BLITZ_RUSH(11),
    DUNGEON_ENTRY_TYPE_SUMO(12),
    DUNGEON_ENTRY_TYPE_ACTIVITY(13),
    DUNGEON_ENTRY_TYPE_HACHI(14);

    private final int value;
    private static final Int2ObjectMap<DungeonEntryType> map = new Int2ObjectOpenHashMap<>();
    private static final Map<String, DungeonEntryType> stringMap = new HashMap<>();

    static {
        Stream.of(values()).forEach(e -> {
            map.put(e.getValue(), e);
            stringMap.put(e.name(), e);
        });
    }

    DungeonEntryType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DungeonEntryType getTypeByValue(int value) {
        return map.getOrDefault(value, DUNGEON_ENTRY_TYPE_NORMAL);
    }

    public static DungeonEntryType getTypeByName(String name) {
        return stringMap.getOrDefault(name, DUNGEON_ENTRY_TYPE_NORMAL);
    }
}
