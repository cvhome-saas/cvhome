package com.asrevo.cvhome.aot;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.asm.ClassReader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * Which classes a class file names, read from its constant pool without loading anything. For the registrars that
 * have to find reflection by what the bytecode refers to — a Stripe param our code builds, a library class that hands
 * Spring a method to invoke by name — rather than by an annotation.
 */
final class BytecodeReferences {

    private static final int CONSTANT_CLASS = 7;

    private BytecodeReferences() {
    }

    /**
     * Every class file matching {@code pattern}, by binary class name, with the internal names of the classes each one
     * refers to ({@code com/stripe/param/ProductCreateParams}).
     */
    static Map<String, Set<String>> scan(ClassLoader classLoader, String pattern) {
        Map<String, Set<String>> references = new LinkedHashMap<>();
        for (Resource resource : resources(classLoader, pattern)) {
            try (InputStream in = resource.getInputStream()) {
                ClassReader reader = new ClassReader(in);
                references.put(reader.getClassName().replace('/', '.'), referencedClasses(reader));
            } catch (IOException e) {
                throw new UncheckedIOException(String.format("Could not read %s", resource), e);
            }
        }
        return references;
    }

    private static Set<String> referencedClasses(ClassReader reader) {
        Set<String> names = new LinkedHashSet<>();
        char[] buffer = new char[reader.getMaxStringLength()];
        for (int i = 1; i < reader.getItemCount(); i++) {
            int offset = reader.getItem(i);
            if (offset > 0 && reader.readByte(offset - 1) == CONSTANT_CLASS) {
                String name = reader.readUTF8(offset, buffer);
                if (name != null) {
                    names.add(name);
                }
            }
        }
        return names;
    }

    private static Resource[] resources(ClassLoader classLoader, String pattern) {
        try {
            return new PathMatchingResourcePatternResolver(classLoader).getResources(pattern);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not scan %s", pattern), e);
        }
    }

}
