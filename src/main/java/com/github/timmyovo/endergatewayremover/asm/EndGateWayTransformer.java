package com.github.timmyovo.endergatewayremover.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Optional;
import java.util.function.Supplier;

public class EndGateWayTransformer implements IClassTransformer {
    static {
        SimpleClassModifier.class.getClass();
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        Optional<SimpleClassModifier> transform = SimpleClassModifier.transform(transformedName, basicClass);
        return transform.isPresent() ? ((Supplier<byte[]>) () -> {
            SimpleClassModifier simpleClassModifier = transform.get();
            simpleClassModifier.findMethod("generate", "(Lnet/minecraft/world/World;Ljava/util/Random;Lnet/minecraft/util/math/BlockPos;)Z").ifPresent(methodNode -> {
                methodNode.instructions.iterator().forEachRemaining(abstractInsnNode -> {
                    if (abstractInsnNode.getOpcode() == Opcodes.GETSTATIC) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) abstractInsnNode;
                        if (fieldInsnNode.name.equals("END_PORTAL") && fieldInsnNode.owner.equals("net/minecraft/init/Blocks")) {
                            fieldInsnNode.name = "AIR";
                            System.out.println("Injected to WorldGenEndPodium.java");
                        }
                    }
                });
            });
            return simpleClassModifier.export();
        }).get() : basicClass;
    }

    protected InsnList generatePrintln(String content) {
//        GETSTATIC java/lang/System.out : Ljava/io/PrintStream;
//        LDC "asda"
//        INVOKEVIRTUAL java/io/PrintStream.println (Ljava/lang/String;)V
        InsnList insnList = new InsnList();
        insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
        insnList.add(new LdcInsnNode(content));
        insnList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
        return insnList;
    }
}
