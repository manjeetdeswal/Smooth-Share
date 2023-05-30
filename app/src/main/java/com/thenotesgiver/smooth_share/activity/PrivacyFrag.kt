package com.thenotesgiver.smooth_share.activity

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.thenotesgiver.smooth_share.R


class PrivacyFrag : Fragment() {


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_privacy, container, false)
        val web= view.findViewById<WebView>(R.id.web)
        web.settings.javaScriptEnabled = true

        web.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                view?.loadUrl(url!!)
                return true
            }
        }
        web.loadUrl("https://privacypolicythenotesgiver.blogspot.com/2023/05/smooth-share-privacy-policy.html")
        return view.rootView

    }


}