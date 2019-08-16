package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.utils.now
import cappuccino.ide.intellij.plugin.utils.orElse

object ObjJInsertionTracker {

    private const val pointsForRecent = 5
    private val insertions:MutableMap<String, InsertionData> = mutableMapOf()

    fun hit(text:String) {
        val data = insertions.getOrDefault(text, InsertionData(text,0, 0))
        data.lastInsertionTime = now
        data.timesHit += 1
        insertions[text] = data
    }

    fun getPoints(text:String, defaultPriority:Double = 0.0) : Double {
        return defaultPriority + (insertions.get(text)?.timesHit ?: 0)
    }

    fun getPoints():Map<String, Int> {
        var mostRecent:InsertionData? = null
        val out:MutableMap<String, Int> = mutableMapOf()
        insertions.map { (key, data) ->
            if (mostRecent?.lastInsertionTime.orElse(0) < data.lastInsertionTime)
                mostRecent = data
            out[key] = data.timesHit
        }
        val mostRecentKey = mostRecent?.text ?: return out
        if (out[mostRecentKey] != null)
            out[mostRecentKey] = out[mostRecentKey]!! + pointsForRecent
        return out
    }
}

private data class InsertionData(internal val text:String, internal var lastInsertionTime:Long, internal var timesHit:Int)