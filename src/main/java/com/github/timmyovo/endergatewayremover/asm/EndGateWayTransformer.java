package com.github.timmyovo.endergatewayremover.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.world.gen.feature.WorldGenEndGateway;
import net.minecraft.world.gen.feature.WorldGenEndPodium;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Optional;
import java.util.function.Supplier;

public class EndGateWayTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (transformedName.equals("com.github.timmyovo.endergatewayremover.asm.SimpleClassModifier")) {
            return basicClass;
        }
        Optional<SimpleClassModifier> transform = SimpleClassModifier.transform(transformedName, basicClass);
        return transform.isPresent() ? ((Supplier<byte[]>) () -> {
            SimpleClassModifier simpleClassModifier = transform.get();
            simpleClassModifier.findMethod("generate", "(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z").ifPresent(methodNode -> {
                methodNode.instructions.clear();
                methodNode.maxStack = 0;
                methodNode.maxLocals = 0;
                methodNode.instructions.add(new InsnNode(Opcodes.ICONST_1));
                methodNode.instructions.add(new InsnNode(Opcodes.IRETURN));
            });
            return simpleClassModifier.export();
        }).get() : basicClass;
    }
}
