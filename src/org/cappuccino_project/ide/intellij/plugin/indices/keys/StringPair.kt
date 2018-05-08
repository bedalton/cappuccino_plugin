package org.cappuccino_project.ide.intellij.plugin.indices.keys

import com.intellij.util.io.KeyDescriptor

import java.io.DataInput
import java.io.DataOutput
import java.io.IOException
import java.util.Objects

class StringPair(val val1: String, val val2: String) {

    override fun equals(ob: Any?): Boolean {
        if (ob === this) {
            return true
        }
        if (ob is StringPair) {
            val classMethodPair = ob as StringPair?
            return val1 == classMethodPair!!.val1 && val2 == classMethodPair.val2
        }
        return false
    }

    override fun hashCode(): Int {
        return Objects.hash(val1, val2)
    }

    companion object {

        val KEY_DESCRIPTOR: KeyDescriptor<StringPair> = object : KeyDescriptor<StringPair> {
            override fun getHashCode(classMethodPair: StringPair): Int {
                return classMethodPair.hashCode()
            }

            override fun isEqual(classMethodPair: StringPair, t1: StringPair): Boolean {
                return classMethodPair == t1
            }

            @Throws(IOException::class)
            override fun save(
                    dataOutput: DataOutput, classMethodPair: StringPair) {
                dataOutput.writeUTF(classMethodPair.val1)
                dataOutput.writeUTF(classMethodPair.val2)
            }

            @Throws(IOException::class)
            override fun read(
                    dataInput: DataInput): StringPair {
                val val1 = dataInput.readUTF()
                val val2 = dataInput.readUTF()
                return StringPair(val1, val2)
            }
        }
    }
}
