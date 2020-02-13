package com.github.timmyovo.endergatewayremover.asm;

import com.google.common.collect.Lists;
import lombok.Getter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

@Getter
public class SimpleClassModifier {
    private static final boolean ENABLE_AUTO_REMAP_FIELD = true;
    private static final boolean ENABLE_AUTO_REMAP_METHODS = true;

    private static final List<String[]> FIELDS_MAP = Lists.newArrayList();
    private static final List<String[]> METHODS_MAP = Lists.newArrayList();

    private static final List<String> TRANFORM_LIST = Arrays.asList(
            "net.minecraft.world.gen.feature.WorldGenEndPodium"
    );

    static {

    }

    private byte[] target;
    private ClassReader classReader;
    private ClassNode classNode;

    public SimpleClassModifier() {
    }

    public SimpleClassModifier(byte[] target) {
        this.target = target;
        this.classReader = new ClassReader(target);
        this.classNode = new ClassNode();
        this.classReader.accept(classNode, 0);
        ClassLoader classLoader = getClass().getClassLoader();
        if (ENABLE_AUTO_REMAP_FIELD) {
            FIELDS_MAP.addAll(readCSVFile(classLoader.getResourceAsStream("fields.csv")));
        }
        if (ENABLE_AUTO_REMAP_METHODS) {
            METHODS_MAP.addAll(readCSVFile(classLoader.getResourceAsStream("methods.csv")));
        }
    }

    private static List<String[]> readCSVFile(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream)).lines()
                .skip(1)
                .map(line -> line.split(","))
                .map(strings -> Arrays.stream(strings).limit(2).toArray(String[]::new))
                .collect(Collectors.toList());
    }

    private static String remapName(String name) {
        return Stream.concat(FIELDS_MAP.stream(), METHODS_MAP.stream())
                .filter(pair -> name.equals(pair[1]))
                .map(strings -> strings[0])
                .findFirst()
                .orElse(name);
    }

    public static Optional<SimpleClassModifier> transform(String name, byte[] bytes) {
        return TRANFORM_LIST.stream()
                .filter(a -> a.equals(name))
                .map(n -> new SimpleClassModifier(bytes))
                .findFirst();
    }

    public Optional<MethodNode> findMethod(String methodName, String desc) {
        return classNode.methods.stream()
                .filter(methodNode -> methodName.equals(methodNode.name) || remapName(methodName).equals(methodNode.name))
                .filter(methodNode -> methodNode.desc.equals(desc))
                .findFirst();
    }

    public Optional<FieldNode> findField(String name) {
        return classNode.fields
                .stream()
                .filter(fieldNode -> name.equals(fieldNode.name) || remapName(name).equals(fieldNode.name))
                .findFirst();
    }

    public byte[] export() {
        ClassWriter classWriter = new ClassWriter(classReader, COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
