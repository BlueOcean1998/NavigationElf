package base.foxizz

import android.os.Bundle
import android.view.View
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

/**
 * 基础碎片
 *
 * @param contentLayoutId 内容布局id
 */
abstract class BaseFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    val baseActivity: BaseActivity
        get() {
            return requireActivity() as? BaseActivity
                ?: throw IllegalStateException("Activity which Fragment $this depend on is not BaseActivity.")
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    open fun initView() {}
}