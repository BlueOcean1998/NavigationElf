package base.foxizz.util

import android.os.Looper
import base.foxizz.mlh
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * 线程工具类
 */
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
    fun <T> submit(task: Callable<T>): Future<T> = mExecutorService.submit(task)
}

/**
 * 在子线程中执行代码
 *
 * @param delayMillis 延时
 * @param block 代码块
 */
fun runOnThread(delayMillis: Long = 0, block: () -> Unit) = ThreadUtil.execute {
    Thread.sleep(delayMillis)
    block.invoke()
}

/**
 * 在主线程中执行代码
 *
 * @param delayMillis 延时
 * @param block 代码块
 */
fun runOnUiThread(delayMillis: Long = 0, block: () -> Unit) =
    mlh.postDelayed(block, delayMillis)

/**
 * 判断当前线程是否是主线程
 */
fun isOnUiThread() = Looper.getMainLooper() == Looper.myLooper()
