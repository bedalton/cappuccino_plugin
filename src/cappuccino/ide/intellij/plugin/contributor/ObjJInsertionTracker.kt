package cappuccino.ide.intellij.plugin.contributor

import cappuccino.ide.intellij.plugin.psi.utils.LOGGER
import cappuccino.ide.intellij.plugin.utils.now
import cappuccino.ide.intellij.plugin.utils.orElse
import kotlin.math.max

object ObjJInsertionTracker {

    private const val pointsForRecent = 500
    private const val pointsForHit = 50
    private val insertions: MutableMap<String, InsertionData> = mutableMapOf()

    fun hit(text: String) {
        val data = insertions.getOrDefault(text, InsertionData(text, 0, 0))
        val newData = data
                .copy(
                        lastInsertionTime = now,
                        timesHit = data.timesHit + 1
                )
        insertions[text] = newData
        reduce()
    }

    fun getPoints(text: String, defaultPriority: Double = 0.0): Double {
        val data = insertions[text] ?: return defaultPriority
        var points = defaultPriority
        points += data.timesHit * pointsForHit
        val recentIfGreaterThan = insertions.map { it.value.lastInsertionTime }.max()?.minus(6000) ?: 0
        if (data.lastInsertionTime > recentIfGreaterThan)
            points += pointsForRecent
        return points
    }

    private fun reduce() {
        val now = now;
        for ((text, data) in insertions) {
            if (data.lastInsertionTime < now - 60000 && data.timesHit > 2) {
                insertions[text] = data.copy(timesHit = max(data.timesHit - 1, 2))
            }
        }
    }

    fun getPoints(): Map<String, Int> {
        var mostRecent: InsertionData? = null
        val out: MutableMap<String, Int> = mutableMapOf()
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

private data class InsertionData(internal val text: String, internal val lastInsertionTime: Long, internal val timesHit: Int)