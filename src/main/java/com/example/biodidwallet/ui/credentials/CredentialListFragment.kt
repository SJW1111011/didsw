package com.example.biodidwallet.ui.credentials

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.biodidwallet.R
import com.example.biodidwallet.data.local.VerifiableCredential
import com.example.biodidwallet.ui.auth.DIDAuthActivity
import com.example.biodidwallet.ui.main.MainViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class CredentialListFragment : Fragment() {
    
    private lateinit var viewModel: CredentialViewModel
    private lateinit var mainViewModel: MainViewModel
    private lateinit var adapter: CredentialAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_credential_list, container, false)
        
        // 初始化RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_credential_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = CredentialAdapter(
            onItemClick = { credential ->
                // 查看凭证详情
                showCredentialDetails(credential)
            },
            onShareClick = { credential ->
                // 分享凭证
                shareCredential(credential)
            }
        )
        recyclerView.adapter = adapter
        
        // 添加凭证按钮
        val fabAddCredential = view.findViewById<FloatingActionButton>(R.id.fab_add_credential)
        fabAddCredential.setOnClickListener {
            addDemoCredential()
        }
        
        return view
    }
    
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(CredentialViewModel::class.java)
        mainViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
        
        // 观察凭证列表变化
        viewModel.credentials.observe(viewLifecycleOwner) { credentials ->
            adapter.updateCredentials(credentials)
        }
        
        // 观察活动DID变化
        mainViewModel.selectedDID.observe(viewLifecycleOwner) { didInfo ->
            if (didInfo != null) {
                // 加载选中DID的凭证
                viewModel.loadCredentialsForDID(didInfo.id)
            } else {
                // 没有选中DID，清空列表
                adapter.updateCredentials(emptyList())
            }
        }
    }
    
    private fun showCredentialDetails(credential: VerifiableCredential) {
        // TODO: 打开凭证详情页面
        Toast.makeText(requireContext(), "查看凭证详情: ${credential.subject}", Toast.LENGTH_SHORT).show()
    }
    
    private fun shareCredential(credential: VerifiableCredential) {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "凭证: ${credential.subject}\n发行方: ${credential.issuer}")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "分享凭证"))
    }
    
    private fun addDemoCredential() {
        val currentDID = mainViewModel.selectedDID.value
        if (currentDID != null) {
            // 首先需要验证身份
            val intent = Intent(requireContext(), DIDAuthActivity::class.java).apply {
                putExtra(DIDAuthActivity.EXTRA_KEY_ALIAS, currentDID.keyAlias)
                putExtra(DIDAuthActivity.EXTRA_AUTH_PURPOSE, DIDAuthActivity.PURPOSE_SIGN_CREDENTIAL)
            }
            startActivity(intent)
            
            // 身份验证成功后，创建演示凭证
            viewModel.createDemoCredential(currentDID.id)
        } else {
            Toast.makeText(requireContext(), "请先选择一个身份", Toast.LENGTH_SHORT).show()
        }
    }
    
    private class CredentialAdapter(
        private val onItemClick: (VerifiableCredential) -> Unit,
        private val onShareClick: (VerifiableCredential) -> Unit
    ) : RecyclerView.Adapter<CredentialAdapter.CredentialViewHolder>() {
        
        private var credentialList: List<VerifiableCredential> = emptyList()
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        fun updateCredentials(newList: List<VerifiableCredential>) {
            credentialList = newList
            notifyDataSetChanged()
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CredentialViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_credential, parent, false)
            return CredentialViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: CredentialViewHolder, position: Int) {
            val credential = credentialList[position]
            holder.bind(credential)
        }
        
        override fun getItemCount() = credentialList.size
        
        inner class CredentialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textSubject: TextView = itemView.findViewById(R.id.text_credential_subject)
            private val textIssuer: TextView = itemView.findViewById(R.id.text_credential_issuer)
            private val textDate: TextView = itemView.findViewById(R.id.text_credential_date)
            private val buttonShare: Button = itemView.findViewById(R.id.button_credential_share)
            
            fun bind(credential: VerifiableCredential) {
                // 绑定数据到视图
                textSubject.text = credential.subject
                textIssuer.text = credential.issuer
                textDate.text = dateFormat.format(Date(credential.issuanceDate))
                
                // 设置点击事件
                itemView.setOnClickListener { onItemClick(credential) }
                buttonShare.setOnClickListener { onShareClick(credential) }
            }
        }
    }
} 