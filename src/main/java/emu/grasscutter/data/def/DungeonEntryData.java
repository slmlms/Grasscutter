package emu.grasscutter.data.def;

import emu.grasscutter.data.GameResource;

//@ResourceType(name = "DungeonEntryExcelConfigData.json")
public class DungeonEntryData extends GameResource {
    public int Id;
    public int DungeonEntryId;
    public String Type;
    public int RewardDataId;

    @Override
    public int getId() {
        return this.Id;
    }

    public int getDungeonEntryId() {
        return DungeonEntryId;
    }

    public String getType() {
        return Type;
    }

    public int getRewardDataId() {
        return RewardDataId;
    }


}
