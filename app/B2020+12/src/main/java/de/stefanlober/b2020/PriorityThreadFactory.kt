package de.stefanlober.b2020

import java.util.concurrent.ThreadFactory

class PriorityThreadFactory(val priority: Int) : ThreadFactory {
    override fun newThread(runnable: Runnable): Thread {
        var thread = Thread(runnable)
        thread.priority = priority
        return thread
    }
}