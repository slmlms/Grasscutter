package emu.grasscutter.game.props;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public enum DungeonType {

    DUNGEON_ACTIVITY(0),
    DUNGEON_BLITZ_RUSH(1),
    DUNGEON_BOSS(2),
    DUNGEON_CHANNELLER_SLAB_LOOP(3),
    DUNGEON_CHANNELLER_SLAB_ONE_OFF(4),
    DUNGEON_CHESS(5),
    DUNGEON_DAILY_FIGHT(6),
    DUNGEON_DISCARDED(7),
    DUNGEON_EFFIGY(8),
    DUNGEON_ELEMENT_CHALLENGE(9),
    DUNGEON_FIGHT(10),
    DUNGEON_FLEUR_FAIR(11),
    DUNGEON_HACHI(12),
    DUNGEON_PLOT(13),
    DUNGEON_ROGUELIKE(14),
    DUNGEON_SUMO_COMBAT(15),
    DUNGEON_THEATRE_MECHANICUS(16),
    DUNGEON_TOWER(17);

    private final int value;
    private static final Int2ObjectMap<DungeonType> map = new Int2ObjectOpenHashMap<>();
    private static final Map<String, DungeonType> stringMap = new HashMap<>();

    static {
        Stream.of(values()).forEach(e -> {
            map.put(e.getValue(), e);
            stringMap.put(e.name(), e);
        });
    }

    DungeonType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DungeonType getTypeByValue(int value) {
        return map.getOrDefault(value, DUNGEON_DAILY_FIGHT);
    }

    public static DungeonType getTypeByName(String name) {
        return stringMap.getOrDefault(name, DUNGEON_DAILY_FIGHT);
    }
}
