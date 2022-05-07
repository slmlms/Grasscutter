package emu.grasscutter.command.commands;

import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.def.AvatarData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.player.Player;

import java.util.List;
import java.util.Objects;

@Command(label = "giveallchar", usage = "giveallchar <playerId> <StellaFortuna level> [level]",
        description = "Gives the player all character", aliases = {"giveallc"}, permission = "player.giveallchar")
public final class GiveAllCharCommand implements CommandHandler {


    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        int stellaFortuna = 0;
        int level = 1;
        switch (args.size()) {

            case 2://playerId ,StellaFortuna
                try {
                    stellaFortuna = reviseStellaFortuna(Integer.parseInt(args.get(1)));
                } catch (NumberFormatException ignored) {
                    return;
                }
                break;
            case 3://playerId ,StellaFortuna ,level
                try {
                    stellaFortuna = reviseStellaFortuna(Integer.parseInt(args.get(1)));
                    level = Integer.parseInt(args.get(2));
                } catch (NumberFormatException ignored) {
                    return;
                }
                break;
            default:
                if (!Objects.isNull(sender)) {
                    break;
                } else
                return;

        }


        for (AvatarData avatarData : GameData.getAvatarDataMap().values()) {

            //Exclude test avatar
            if (isTestAvatar(avatarData.getId())) continue;
            Avatar avatar;
            //如果角色存在，则直接修改角色的数据
            if (targetPlayer.getAvatars().hasAvatar(avatarData.getId())) {
                avatar = targetPlayer.getAvatars().getAvatarById(avatarData.getId());
                avatar.setLevel(level);
                avatar.setPromoteLevel(getPromteLevel(level));
                avatar.setCoreProudSkillLevel(stellaFortuna);
                avatar.update();
            } else {
                avatar = new Avatar(avatarData);
                for (int i = 1; i <= 6; ++i) {
                    avatar.getTalentIdList().add((avatar.getAvatarId() - 10000000) * 10 + i);
                }
                // This will handle stats and talents
                avatar.recalcStats();
                avatar.setLevel(level);
                avatar.setPromoteLevel(getPromteLevel(level));
                avatar.setCoreProudSkillLevel(stellaFortuna);
                boolean result = targetPlayer.getAvatars().addAvatar(avatar);
                if (!result) {
                    CommandHandler.sendMessage(sender, "Failed to add avatar " + avatar.getAvatarId());
                    continue;
                }
            }
            CommandHandler.sendMessage(sender, "success to add avatar " + avatar.getAvatarId());

        }
        CommandHandler.sendMessage(sender, "success");
    }


    private Integer getPromteLevel(int level) {
        if (level <= 20) return 0;
        if (level <= 40) return 1;
        if (level <= 50) return 2;
        if (level <= 60) return 3;
        if (level <= 70) return 4;
        if (level <= 80) return 5;
        return 6;
    }

    public Integer reviseStellaFortuna(int stellaFortuna) {
        return Math.min(stellaFortuna, 6);
    }

    public boolean isTestAvatar(int avatarId) {
        return avatarId < 10000002 || avatarId >= 11000000;
    }
}