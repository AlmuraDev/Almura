/*
 * This file is part of Almura.
 *
 * Copyright (c) AlmuraDev <https://github.com/AlmuraDev/>
 *
 * All Rights Reserved.
 */
package com.almuradev.almura.feature.bank;

import com.google.inject.Inject;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

final class BankCommands {

    @Inject private static ServerBankManager bankManager;

    /**
     * /bank: opens the bank window.
     */
    static CommandSpec createCommand() {
        return CommandSpec.builder()
            .description(Text.of("Open the bank"))
            .permission(ServerBankManager.PERMISSION_USE)
            .executor((src, args) -> {
                if (!(src instanceof Player)) {
                    src.sendMessage(Text.of("Only players can use the bank."));
                    return CommandResult.empty();
                }
                bankManager.openBank((Player) src);
                return CommandResult.success();
            })
            .build();
    }
}
