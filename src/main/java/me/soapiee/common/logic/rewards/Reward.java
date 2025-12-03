package me.soapiee.common.logic.rewards;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.soapiee.common.manager.MessageManager;

@AllArgsConstructor
public abstract class Reward implements RewardInterface {

    @Getter protected final RewardType type;
    @Getter private final boolean isTemporary;
    @Getter protected final MessageManager messageManager;
}
