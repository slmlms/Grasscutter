package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.def.ItemData;
import emu.grasscutter.game.inventory.GameItem;
import emu.grasscutter.game.inventory.ItemType;
import emu.grasscutter.game.inventory.MaterialType;
import emu.grasscutter.game.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.IntStream;

import static emu.grasscutter.utils.Language.translate;

@Command(label = "giveallmaterial", usage = "giveallmaterial  <amount>",
        description = "Gives the player all character", aliases = {"giveallmat"}, permission = "player.giveallmaterial")
public final class GiveAllMaterialCommand implements CommandHandler {

    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        int amount = 2000;
        if (args.size() == 1) {//amount
            try {
                amount = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                CommandHandler.sendMessage(sender, translate("commands.generic.invalid.amount"));
            }
        }
        giveAllMaterial(targetPlayer, amount);
    }


    private void giveAllMaterial(Player player, Integer amount) {
        CommandHandler.sendMessage(player, "now is giving");

        List<GameItem> itemList = new ArrayList<>();
        for (ItemData itemdata : GameData.getItemDataMap().values()) {
            if (!itemdata.getItemType().equals(ItemType.ITEM_MATERIAL)) continue;

            boolean iSCharacterMaterial = itemdata.getMaterialType() == MaterialType.MATERIAL_AVATAR_MATERIAL;
            boolean isGatherMaterial = itemdata.getMaterialType() == MaterialType.MATERIAL_EXCHANGE;
            boolean isExpMaterial = itemdata.getMaterialType() == MaterialType.MATERIAL_EXP_FRUIT;
            boolean isWaponExpMaterial = itemdata.getMaterialType() == MaterialType.MATERIAL_WEAPON_EXP_STONE;
            if (iSCharacterMaterial || isGatherMaterial || isExpMaterial || isWaponExpMaterial) {
                GameItem item = new GameItem(itemdata);
                item.setCount(amount);
                itemList.add(item);
            }
        }

        player.getInventory().addItems(itemList);
        CommandHandler.sendMessage(player, "success");
    }

    public boolean isGatherMaterials(int itemId) {
        IntStream[] intStreams = {
                IntStream.range(100011, 100095),
                IntStream.range(101101, 101110),
                IntStream.range(101201, 101212)
        };
        for (IntStream intStream : intStreams) {
            return intStream.anyMatch(i -> i == itemId);
        }
        return false;
    }


    public boolean isTestItem(int itemId) {
        for (GiveAllCommand.Range range : testItemRanges) {
            if (range.check(itemId)) {
                return true;
            }
        }

        return testItemsList.contains(itemId);
    }


    private static final GiveAllCommand.Range[] testItemRanges = new GiveAllCommand.Range[]{
            new GiveAllCommand.Range(106, 139),
            new GiveAllCommand.Range(1000, 1099),
            new GiveAllCommand.Range(2001, 3022),
            new GiveAllCommand.Range(1102, 1165),
            new GiveAllCommand.Range(23300, 23340),
            new GiveAllCommand.Range(23383, 23385),
            new GiveAllCommand.Range(78310, 78554),
            new GiveAllCommand.Range(99310, 99554),
            new GiveAllCommand.Range(100001, 100187),
            new GiveAllCommand.Range(100210, 100214),
            new GiveAllCommand.Range(100303, 100398),
            new GiveAllCommand.Range(100414, 100425),
            new GiveAllCommand.Range(100454, 103008),
            new GiveAllCommand.Range(109000, 109492),
            new GiveAllCommand.Range(115001, 118004),
            new GiveAllCommand.Range(141001, 141072),
            new GiveAllCommand.Range(220050, 221016),
    };
    private static final Integer[] testItemsIds = new Integer[]{
            210, 211, 314, 315, 317, 1005, 1007, 1105, 1107, 1201, 1202, 10366,
            101212, 11411, 11506, 11507, 11508, 12505, 12506, 12508, 12509, 13503,
            13506, 14411, 14503, 14505, 14508, 15411, 15504, 15505, 15506, 15508,
            20001, 10002, 10003, 10004, 10005, 10006, 10008, 100231, 100232, 100431,
            101689, 105001, 105004, 106000, 106001, 108000, 110000
    };

    private static final Collection<Integer> testItemsList = Arrays.asList(testItemsIds);
}
