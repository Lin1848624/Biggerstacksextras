package com.hermes.biggerstacksextras.mixin;

/** 只在装了 Butchery（modid: butchery）时应用感染血液瓶的补丁。 */
public final class ButcheryMixinPlugin extends OptionalModMixinPlugin {

    @Override
    protected String modId() {
        return "butchery";
    }
}
