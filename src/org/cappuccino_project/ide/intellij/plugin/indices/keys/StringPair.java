package org.cappuccino_project.ide.intellij.plugin.indices.keys;

import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class StringPair {
    private String val1;
    private String val2;

    public StringPair(@NotNull final String className, @NotNull final String val2) {
        this.val1 = className;
        this.val2 = val2;
    }

    @NotNull
    public String getVal1() {
        return val1;
    }

    @NotNull
    public String getVal2() {
        return val2;
    }

    @Override
    public boolean equals(Object ob) {
        if (ob == this) {
            return true;
        }
        if (ob instanceof StringPair) {
            final StringPair classMethodPair = (StringPair)ob;
            return val1.equals(classMethodPair.val1) && val2.equals(classMethodPair.val2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(val1, val2);
    }

    public static final KeyDescriptor<StringPair> KEY_DESCRIPTOR = new KeyDescriptor<StringPair>() {
        @Override
        public int getHashCode(StringPair classMethodPair) {
            return classMethodPair.hashCode();
        }

        @Override
        public boolean isEqual(StringPair classMethodPair, StringPair t1) {
            return classMethodPair.equals(t1);
        }

        @Override
        public void save(
                @NotNull
                        DataOutput dataOutput, StringPair classMethodPair) throws IOException {
            dataOutput.writeUTF(classMethodPair.getVal1());
            dataOutput.writeUTF(classMethodPair.getVal2());
        }

        @Override
        public StringPair read(
                @NotNull
                        DataInput dataInput) throws IOException {
            final String val1 = dataInput.readUTF();
            final String val2 = dataInput.readUTF();
            return new StringPair(val1, val2);
        }
    };
}
