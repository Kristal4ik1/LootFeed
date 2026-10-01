package dev.lootfeed.core.tracker;

import dev.lootfeed.core.StackView;

import java.util.Collection;

public interface LootSink {

    void gained(StackView stack, int amount, boolean fromContainer);

    void dropped(StackView stack, int amount);

    void lost(StackView stack, int amount);

    void stored();

    void settled(Collection<StackView> inventory);
}
