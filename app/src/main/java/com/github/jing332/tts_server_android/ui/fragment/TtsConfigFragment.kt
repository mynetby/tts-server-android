package com.github.jing332.tts_server_android.ui.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.jing332.tts_server_android.R
import com.github.jing332.tts_server_android.constant.ReadAloudTarget
import com.github.jing332.tts_server_android.data.SysTtsConfigItem
import com.github.jing332.tts_server_android.databinding.FragmentTtsConfigBinding
import com.github.jing332.tts_server_android.ui.custom.adapter.SysTtsConfigListItemAdapter
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity.Companion.KEY_DATA
import com.github.jing332.tts_server_android.ui.systts.TtsConfigEditActivity.Companion.KEY_POSITION


@Suppress("DEPRECATION")
class TtsConfigFragment : Fragment(), SysTtsConfigListItemAdapter.ClickListen,
    SysTtsConfigListItemAdapter.LongClickListen {
    companion object {
        const val TAG = "TtsConfigFragment"
        const val ACTION_ON_CONFIG_CHANGED = "on_config_changed"
    }

    private val viewModel: TtsConfigFragmentViewModel by activityViewModels()
    private val binding: FragmentTtsConfigBinding by lazy {
        FragmentTtsConfigBinding.inflate(
            layoutInflater
        )
    }

    private val recyclerAdapter: SysTtsConfigListItemAdapter by lazy {
        SysTtsConfigListItemAdapter(
            viewModel, arrayListOf()
        )
    }

    private val startForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            val data = result.data?.getSerializableExtra(KEY_DATA)
            val position = result.data?.getIntExtra(KEY_POSITION, -1) ?: -1
            data?.let {
                val cfgItem = data as SysTtsConfigItem
                viewModel.onEditActivityResult(cfgItem, position)
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recyclerAdapter.switchClick = this
        recyclerAdapter.editButtonClick = this
        recyclerAdapter.delButtonClick = this
        recyclerAdapter.itemLongClick = this

        val layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewList.apply {
            this.layoutManager = layoutManager
            this.adapter = recyclerAdapter
        }
        /* 悬浮加号按钮 */
        binding.fabAddConfig.setOnClickListener {
            val intent =
                Intent(requireContext(), TtsConfigEditActivity::class.java)
            startForResult.launch(intent)
        }
        /* 监听整个列表数据list */
        viewModel.ttsCfg.observe(this) {
            Log.d(TAG, "item list changed: $it")
            it.list.forEach { item ->
                recyclerAdapter.append(item, false)
            }
        }
        /* 添加列表数据 */
        viewModel.appendItemDataLiveData.observe(this) {
            Log.d(TAG, "add item: $it")
            recyclerAdapter.append(it, true)
        }
        /* 替换列表数据 */
        viewModel.replacedItemDataLiveData.observe(this) {
            Log.d(TAG, "replace item: $it")
            recyclerAdapter.update(it.sysTtsConfigItem, it.position, it.updateUi)
            viewModel.saveData()
            requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
        }
        /* 从本地加载数据 */
        viewModel.loadData()
    }

    override fun onPause() {
        super.onPause()
        /* 保存到本地 */
        viewModel.saveData()
    }

    override fun onClick(view: View, position: Int) {
        when (view.id) {
            R.id.checkBox_switch -> {
                val checkBox = view as CheckBox
                viewModel.onCheckBoxChanged(position, checkBox.isChecked)
                viewModel.saveData()
                requireContext().sendBroadcast(Intent(ACTION_ON_CONFIG_CHANGED))
            }
            R.id.btn_edit -> {
                val itemData = recyclerAdapter.itemList[position]
                val intent =
                    Intent(requireContext(), TtsConfigEditActivity::class.java)
                intent.putExtra(KEY_DATA, itemData)
                intent.putExtra(KEY_POSITION, position)
                startForResult.launch(intent)
            }
            R.id.btn_delete -> {
                AlertDialog.Builder(requireContext()).setTitle("确认删除？")
                    .setPositiveButton("删除") { _, _ ->
                        Log.e(TAG, "remove item: $position")
                        recyclerAdapter.remove(position)
                    }
                    .show()
            }
        }
    }

    override fun onLongClick(view: View, position: Int): Boolean {
        /* 列表item的长按菜单 */
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.menu_systts_list_item, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item ->
            val itemData = viewModel.ttsCfg.value?.list?.get(position)
            when (item.itemId) {
                R.id.menu_global -> { /* 全局 */
                    itemData?.readAloudTarget = ReadAloudTarget.DEFAULT
                }
                R.id.menu_setAsDialogue -> { /* 对话  */
                    itemData?.readAloudTarget = ReadAloudTarget.DIALOGUE
                }
                R.id.menu_setAsAside -> { /* 旁白 */
                    itemData?.readAloudTarget = ReadAloudTarget.ASIDE
                }
            }
            recyclerAdapter.update(itemData!!, position, true)
            false
        }
        popupMenu.show()

        return true
    }
}