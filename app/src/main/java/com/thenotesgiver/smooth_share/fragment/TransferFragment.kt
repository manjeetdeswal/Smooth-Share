package com.thenotesgiver.smooth_share.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thenotesgiver.smooth_share.R
import com.thenotesgiver.smooth_share.database.model.WebTransfer
import com.thenotesgiver.smooth_share.databinding.LayoutEmptyContentBinding
import com.thenotesgiver.smooth_share.databinding.ListTransferHistoryBinding
import com.thenotesgiver.smooth_share.fragment.dialog.WebTransferContentViewModel
import com.thenotesgiver.smooth_share.viewmodel.EmptyContentViewModel
import com.thenotesgiver.smooth_share.viewmodel.TransfersViewModel
import dagger.hilt.android.AndroidEntryPoint
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.thenotesgiver.smooth_share.activity.NativeTemplateStyle
import com.thenotesgiver.smooth_share.activity.TemplateView
import com.thenotesgiver.smooth_share.app.Activity
import com.thenotesgiver.smooth_share.app.Activity.Companion.mInterstitialAd
import java.util.*

@AndroidEntryPoint
class TransferFragment : Fragment(R.layout.layout_transfer_fragment), MenuProvider {
    private val viewModel: TransfersViewModel by viewModels()
    private val deleteData = ArrayList<WebTransfer>()
    private lateinit var template: TemplateView
    private  var nativeAd : NativeAd? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
        template = view.findViewById(R.id.my_template)
        MobileAds.initialize(
            requireContext()
        ) { refreshAd() }
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val emptyView = LayoutEmptyContentBinding.bind(view.findViewById(R.id.emptyView))

        val adapter = TransferHistoryAdapter {
            when (it) {
                is TransferHistoryAdapter.Click.Web -> findNavController().navigate(
                    TransferFragmentDirections.actionTransferFragmentToWebTransferDetailsFragment(it.transfer)
                )
            }
        }

        val emptyContentViewModel = EmptyContentViewModel()

        emptyView.viewModel = emptyContentViewModel
        emptyView.emptyText.setText(R.string.transfer_history)
        emptyView.emptyImage.setImageResource(R.drawable.ic_compare_arrows_white_24dp)
        emptyView.executePendingBindings()
        adapter.setHasStableIds(true)
        recyclerView.adapter = adapter

        viewModel.transfersHistory.observe(viewLifecycleOwner) {
            adapter.submitList(it)
            emptyContentViewModel.with(recyclerView, it.isNotEmpty())
            //delete all transfer history
            deleteData.clear()
            deleteData.addAll(it)
        }

        view.findViewById<View>(R.id.sendButton).setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(requireActivity())
            }
            findNavController().navigate(
                TransferFragmentDirections.actionTransferFragmentToSharingFragment()
            )
        }
        view.findViewById<View>(R.id.receiveButton).setOnClickListener {
            if (mInterstitialAd != null) {
                mInterstitialAd!!.show(requireActivity())
            }
            findNavController().navigate(
                TransferFragmentDirections.actionTransferFragmentToWebShareLauncherFragment2()
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAd?.destroy()
    }
    private fun refreshAd() {
        val adLoader = AdLoader.Builder(requireContext(), getString(R.string.NativeDownload))
            .forNativeAd { nativeAd: NativeAd ->
                //     Log.d(TAG, "Native Ad Loaded");
                this.nativeAd = nativeAd

                val styles: NativeTemplateStyle = NativeTemplateStyle.Builder().build()
                template.setStyles(styles)
                template.visibility = View.VISIBLE
                template.setNativeAd(nativeAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // Handle the failure by logging, altering the UI, and so on.
                    //   Log.d(TAG, "Native Ad Failed To Load");
                    template.visibility = View.GONE
                }
            })
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .build()
            )
            .build()
        adLoader.loadAd(AdRequest.Builder().build())
    }
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        // Add menu items here
        menuInflater.inflate(R.menu.transfer_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        // Handle the menu selection
        when (menuItem.itemId) {
            R.id.delete_transfer_history -> {
                AlertDialog.Builder(requireContext())
                    .setMessage(R.string.delete_transfer_history)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete_all) { _, _ ->
                        try {
                            for (data in deleteData) {
                                viewModel.deleteTransferHistory(data)
                            }
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), R.string.unknown_failure, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .show()

            }
            R.id.privacy ->{

                findNavController().navigate(R.id.action_transferFragment_to_privacyFrag)

            }
            R.id.btn_moreApps -> {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://developer?id=" + "The Notes Giver")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/developer?id=" + "The Notes Giver")
                        )
                    )
                }
                return true
            }

        }

        return false
    }
}

class TransferHistoryCallback : DiffUtil.ItemCallback<WebTransfer>() {
    override fun areItemsTheSame(oldItem: WebTransfer, newItem: WebTransfer): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: WebTransfer, newItem: WebTransfer): Boolean {
        return oldItem == newItem
    }
}

class TransferHistoryAdapter(private val clickListener: (click: Click) -> Unit) : ListAdapter<WebTransfer,
        TransferHistoryHolder>(TransferHistoryCallback()) {
    private val webClick: (WebTransfer) -> Unit = {
        clickListener(Click.Web(it))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransferHistoryHolder {
        return TransferHistoryHolder(
            ListTransferHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            webClick)
    }

    override fun onBindViewHolder(holder: TransferHistoryHolder, position: Int) {
        holder.bind(getItem(position))
    }

    sealed class Click {
        class Web(val transfer: WebTransfer) : Click()
    }
}

class TransferHistoryHolder(
    private val binding: ListTransferHistoryBinding,
    private val clickListener: (WebTransfer) -> Unit,) : RecyclerView.ViewHolder(binding.root) {
    fun bind(transfer: WebTransfer) {
        binding.viewModel = WebTransferContentViewModel(transfer)
        binding.container.setOnClickListener { clickListener(transfer) }
        binding.executePendingBindings()
    }
}