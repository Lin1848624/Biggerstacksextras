package com.hermes.biggerstacksextras.mixin;

/** 只在装了 GTCEu Modern（modid: gtceu）时应用 GT 工具的补丁。 */
public final class GtMixinPlugin extends OptionalModMixinPlugin {

    @Override
    protected String modId() {
        return "gtceu";
    }
}
