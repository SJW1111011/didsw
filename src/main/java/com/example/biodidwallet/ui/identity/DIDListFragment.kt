package com.example.biodidwallet.ui.identity

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biodidwallet.R
import com.example.biodidwallet.data.repository.DIDInfo
import com.example.biodidwallet.ui.auth.DIDAuthActivity
import com.example.biodidwallet.ui.main.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class DIDListFragment : Fragment() {
    
    private lateinit var viewModel: DIDViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: DIDAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_did_list, container, false)
        
        // 初始化RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_did_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = DIDAdapter(
            onItemClick = { didInfo ->
                // 选择此DID
                mainViewModel.selectDID(didInfo.id)
                Toast.makeText(requireContext(), "已选择: ${didInfo.name}", Toast.LENGTH_SHORT).show()
            },
            onAuthClick = { didInfo -> 
                // 启动生物验证活动
                val intent = Intent(requireContext(), DIDAuthActivity::class.java).apply {
                    putExtra(DIDAuthActivity.EXTRA_KEY_ALIAS, didInfo.keyAlias)
                    putExtra(DIDAuthActivity.EXTRA_AUTH_PURPOSE, DIDAuthActivity.PURPOSE_AUTHENTICATE)
                }
                startActivity(intent)
            },
            onDeleteClick = { didInfo ->
                // 确认删除
                AlertDialog.Builder(requireContext())
                    .setTitle("删除身份")
                    .setMessage("确定要删除身份 ${didInfo.name} 吗？此操作不可撤销。")
                    .setPositiveButton("删除") { _, _ ->
                        viewModel.deleteDID(didInfo.id)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        )
        recyclerView.adapter = adapter
        
        // 添加DID按钮
        val fabAddDID = view.findViewById<FloatingActionButton>(R.id.fab_add_did)
        fabAddDID.setOnClickListener {
            showCreateDIDDialog()
        }
        
        return view
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(DIDViewModel::class.java)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        
        // 观察DID列表变化
        viewModel.didList.observe(viewLifecycleOwner) { didList ->
            adapter.updateDIDs(didList)
        }
        
        // 加载DID列表
        viewModel.loadDIDs()
    }
    
    private fun showCreateDIDDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_create_did, null)
        
        val editTextName = dialogView.findViewById<EditText>(R.id.edit_did_name)
        
        AlertDialog.Builder(requireContext())
            .setTitle("创建新身份")
            .setView(dialogView)
            .setPositiveButton("创建") { _, _ ->
                val name = editTextName.text.toString()
                if (name.isNotEmpty()) {
                    viewModel.createDID(name)
                } else {
                    Toast.makeText(requireContext(), "请输入身份名称", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private class DIDAdapter(
        private val onItemClick: (DIDInfo) -> Unit,
        private val onAuthClick: (DIDInfo) -> Unit,
        private val onDeleteClick: (DIDInfo) -> Unit
    ) : RecyclerView.Adapter<DIDAdapter.DIDViewHolder>() {
        
        private var didList: List<DIDInfo> = emptyList()
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        
        fun updateDIDs(newList: List<DIDInfo>) {
            didList = newList
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DIDViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_did, parent, false)
            return DIDViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: DIDViewHolder, position: Int) {
            val didInfo = didList[position]
            holder.bind(didInfo)
        }
        
        override fun getItemCount() = didList.size
        
        inner class DIDViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textName: TextView = itemView.findViewById(R.id.text_did_name)
            private val textId: TextView = itemView.findViewById(R.id.text_did_id)
            private val textDate: TextView = itemView.findViewById(R.id.text_did_date)
            private val buttonAuth: Button = itemView.findViewById(R.id.button_did_auth)
            private val buttonDelete: Button = itemView.findViewById(R.id.button_did_delete)
            
            fun bind(didInfo: DIDInfo) {
                // 绑定数据到视图
                textName.text = didInfo.name
                textId.text = didInfo.id
                textDate.text = dateFormat.format(Date(didInfo.createdTimestamp))
                
                // 设置点击事件
                itemView.setOnClickListener { onItemClick(didInfo) }
                buttonAuth.setOnClickListener { onAuthClick(didInfo) }
                buttonDelete.setOnClickListener { onDeleteClick(didInfo) }
            }
        }
    }
} 