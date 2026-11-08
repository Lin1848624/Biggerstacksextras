package com.hermes.biggerstacksextras.mixin;

import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * 可选模组兼容的开关。
 *
 * 我们的兼容补丁会在“装了目标模组”时才应用；没装时整段跳过，
 * 不会去加载对方的类，也不会因为对方不在而报错。
 *
 * 检查放在 shouldApplyMixin 里做：这时模组列表一定已经加载完成。
 */
public abstract class OptionalModMixinPlugin implements IMixinConfigPlugin {

    private Boolean present;

    protected abstract String modId();

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (present == null) {
            LoadingModList list = LoadingModList.get();
            present = list != null && list.getModFileById(modId()) != null;
        }
        return present;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
