package com.example.my.project.authenticator.ui.fragments

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.databinding.FragmentGuideDetailsBinding

class GuideDetailsFragment : Fragment() {
    private lateinit var binding: FragmentGuideDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentGuideDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.let { GuideDetailsFragmentArgs.fromBundle(it).url }
        Log.d("GuideDetails", "onViewCreated: url= $url")

        loadPdf(url)
        setupClickListeners()
        handleBackPress()
    }

    private fun setupClickListeners() {
        binding.icBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun handleBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().popBackStack()
                }
            })
    }

    private fun loadPdf(url: String?) {
        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$url"

        binding.apply {
            webView.settings.javaScriptEnabled = true
            webView.settings.cacheMode = WebSettings.LOAD_DEFAULT

            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }
            }
            webView.webChromeClient = WebChromeClient()

            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    linearProgressBar.visibility = View.VISIBLE
                    linearProgressBar.setProgress(newProgress)
                    if (newProgress == 100) {
                        linearProgressBar.visibility = View.GONE
                    }
                }
            }

            try {
                webView.loadUrl(googleDocsUrl)
            } catch (e: Resources.NotFoundException) {
                Log.w("WebView", "WebView resource error: ${e.message}")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.webView.destroy()
    }
}