package base.foxizz.util

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

object ThreadUtil {
    //线程池，最大容量
    private val mExecutorService = Executors.newCachedThreadPool()

    /**
     * 执行线程
     *
     * @param runnable Runnable接口
     */
    fun execute(runnable: Runnable) = mExecutorService.execute(runnable)

    /**
     * 执行线程
     *
     * @param task Callable接口
     */
    fun submit(task: Callable<*>): Future<*> = mExecutorService.submit(task)
}