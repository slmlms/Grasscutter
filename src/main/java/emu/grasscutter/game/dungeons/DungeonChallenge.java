package emu.grasscutter.game.dungeons;

import emu.grasscutter.Grasscutter;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.common.ItemParamData;
import emu.grasscutter.data.def.DungeonData;
import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.data.def.RewardPreviewData;
import emu.grasscutter.game.entity.EntityMonster;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ActionReason;
import emu.grasscutter.game.world.Scene;
import emu.grasscutter.scripts.constants.EventType;
import emu.grasscutter.scripts.data.SceneGroup;
import emu.grasscutter.scripts.data.ScriptArgs;
import emu.grasscutter.server.packet.send.PacketChallengeDataNotify;
import emu.grasscutter.server.packet.send.PacketDungeonChallengeBeginNotify;
import emu.grasscutter.server.packet.send.PacketDungeonChallengeFinishNotify;
import emu.grasscutter.server.packet.send.PacketGadgetAutoPickDropInfoNotify;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class DungeonChallenge {
    private final Scene scene;
    private final SceneGroup group;

    private int challengeIndex;
    private int challengeId;
    private boolean success;
    private boolean progress;

    private int score;
    private int objective = 0;
    private IntSet rewardedPlayers;
    private final Random random = new Random();

    public DungeonChallenge(Scene scene, SceneGroup group) {
        this.scene = scene;
        this.group = group;
        this.setRewardedPlayers(new IntOpenHashSet());
    }

    public Scene getScene() {
        return scene;
    }

    public SceneGroup getGroup() {
        return group;
    }

    public int getChallengeIndex() {
        return challengeIndex;
    }

    public void setChallengeIndex(int challengeIndex) {
        this.challengeIndex = challengeIndex;
    }

    public int getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(int challengeId) {
        this.challengeId = challengeId;
    }

    public int getObjective() {
        return objective;
    }

    public void setObjective(int objective) {
        this.objective = objective;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean isSuccess) {
        this.success = isSuccess;
    }

    public boolean inProgress() {
        return progress;
    }

    public int getScore() {
        return score;
    }

    public int getTimeLimit() {
        return 600;
    }

    public IntSet getRewardedPlayers() {
        return rewardedPlayers;
    }

    public void setRewardedPlayers(IntSet rewardedPlayers) {
        this.rewardedPlayers = rewardedPlayers;
    }

    public void start() {
        this.progress = true;
        getScene().broadcastPacket(new PacketDungeonChallengeBeginNotify(this));
    }

    public void finish() {
        this.progress = false;

        getScene().broadcastPacket(new PacketDungeonChallengeFinishNotify(this));

        if (this.isSuccess()) {
            // Call success script event
            this.getScene().getScriptManager().callEvent(EventType.EVENT_CHALLENGE_SUCCESS, null);

            // Settle
            settle();
        } else {
            this.getScene().getScriptManager().callEvent(EventType.EVENT_CHALLENGE_FAIL, null);
        }
    }

    private void settle() {
        getScene().getDungeonSettleObservers().forEach(o -> o.onDungeonSettle(getScene()));

        getScene().getScriptManager().callEvent(EventType.EVENT_DUNGEON_SETTLE, new ScriptArgs(this.isSuccess() ? 1 : 0));
    }

    public void onMonsterDie(EntityMonster entity) {
        score = getScore() + 1;

        getScene().broadcastPacket(new PacketChallengeDataNotify(this, 1, getScore()));

        if (getScore() >= getObjective() && this.progress) {
            this.setSuccess(true);
            finish();
        }
    }

    public void getStatueDrops(Player player) {
        DungeonData dungeonData = getScene().getDungeonData();
        if (!isSuccess() || dungeonData == null || dungeonData.getRewardPreview() == null || dungeonData.getRewardPreview().getPreviewItems().length == 0) {
            return;
        }

        // Already rewarded
        if (getRewardedPlayers().contains(player.getUid())) {
            return;
        }

        RewardPreviewData rewardPreview = getScene().getDungeonData().getRewardPreview();
        List<GameItem> rewards = getGameItems(rewardPreview, getScene().getDungeonData().getShowLevel());
        //TODO 后续添加周本、深渊等
        if (Objects.isNull(rewards)) return;
        player.getInventory().addItems(rewards, ActionReason.DungeonStatueDrop);
        player.sendPacket(new PacketGadgetAutoPickDropInfoNotify(rewards));

        getRewardedPlayers().add(player.getUid());
    }

    @Nullable
    private List<GameItem> getGameItems(RewardPreviewData rewardPreview, int level) {
        ItemParamData[] previewItems = rewardPreview.getPreviewItems();
        String desc = rewardPreview.getDesc();
        if (desc.contains("圣遗物地城")) {
            return artDungeonList(previewItems, level);
        } else if (desc.contains("角色技能地城")) {
            return talentDungeonList(previewItems, level);
        } else if (desc.contains("武器突破地城")) {
            return weaponDungeonList(previewItems, level);
        }
        return null;
    }


    /* 配置文件（RewardPreviewExcelConfigData.json）格式如下：
    * Id：为DungeonExcelConfigData.json中的PassRewardPreviewID
    *
    * PreviewItems： {
        "Id": 724, 72代表圣遗物套装，4代表四星
        "Count": "4" 这个数量代表单次可以生成的最大数量
      }
      *
      *
    * */

    private List<GameItem> artDungeonList(ItemParamData[] artList, int level) {

        List<GameItem> artifacts = new ArrayList<>();
        for (ItemParamData data : artList) {
            if (Objects.isNull(data)) continue;
            if (data.getId() < 100 || data.getId() > 999) continue;
            //经验、摩拉
            if (data.getId() == 102 || data.getId() == 105 || data.getId() == 202) {
                artifacts.add(new GameItem(data.getId(), Math.max(data.getCount(), 1)));
                continue;
            }
            //获取圣遗物套装ID，例如724（四星平雷套），结果为72
            int type = Math.floorDiv(data.getId(), 10);
            //获取圣遗物星级，结果为4
            int star = data.getId() - (type * 10);
            if (type > 51 && type < 99) {
                for (int i = 1; i <= random.nextInt(data.getCount()) + 1; i++) {
                    //生成随机圣遗物ID
                    int itemID = type * 1000
                            + star * 100
                            + (random.nextInt(4) + 1) * 10
                            + (star - (random.nextInt(1) + 1));
                    //添加到列表里面
                    ItemData itemData = GameData.getItemDataMap().get(itemID);
                    if (Objects.isNull(itemData)) continue;
                    Grasscutter.getLogger().info("scene id:" + getScene().getId() + "\tart id" + itemID);
                    artifacts.add(new GameItem(itemData, i));
                }
            }
        }
        return artifacts;
    }

    private List<GameItem> weaponDungeonList(ItemParamData[] wapList, int level) {
        List<GameItem> weaponList = new ArrayList<>();
        for (ItemParamData itemParamData : wapList) {
            if (itemParamData.getId() == 102 || itemParamData.getId() == 105 || itemParamData.getId() == 202) {
                weaponList.add(new GameItem(itemParamData.getId(), Math.max(itemParamData.getCount(), 1)));
            }
            weaponList.add(new GameItem(itemParamData.getId(), new Random().nextInt(itemParamData.getCount()) + 1));
        }
        return weaponList;
    }

    private List<GameItem> talentDungeonList(ItemParamData[] talList, int level) {
        //TODO
        return null;
    }


}
