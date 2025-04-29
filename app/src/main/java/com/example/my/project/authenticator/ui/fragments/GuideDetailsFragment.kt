package com.example.my.project.authenticator.ui.fragments

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.my.project.authenticator.databinding.FragmentGuideDetailsBinding
import java.io.File

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

    private fun downloadAndShowPdf(pdfUrl: String) {
        val fileName = extractFileName(pdfUrl) + ".pdf"
        val file = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (file.exists()) {
            openPdfFile(Uri.fromFile(file))
            return
        }

        val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        var downloadId: Long = -1 // 👉 Declare downloadId here (so it's visible inside onReceive)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: return
                if (id == downloadId) {
                    val uri = downloadManager.getUriForDownloadedFile(downloadId)
                    uri?.let {
                        openPdfFile(it) // ✅ pass DownloadManager's URI, not File URI
                    }
                    requireActivity().unregisterReceiver(this)
                }
            }
        }

        // ✅ Register receiver FIRST
        ContextCompat.registerReceiver(
            requireContext(),
            onComplete,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        // ✅ Then start the download
        val request = DownloadManager.Request(pdfUrl.toUri())
            .setTitle("Downloading PDF")
            .setDescription("Please wait...")
            .setDestinationUri(Uri.fromFile(file))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

        downloadId = downloadManager.enqueue(request) // now set downloadId AFTER receiver is ready
    }

    private fun openPdfFile(uri: Uri) {
        /*binding.pdfView.fromUri(uri)
            .defaultPage(0)
            .enableSwipe(true)
            .swipeHorizontal(false)
            .enableDoubletap(true)
            .load()*/
    }

    private fun extractFileName(url: String): String {
        val lastSegment = url.toUri().lastPathSegment ?: return "temp"
        return lastSegment.removeSuffix(".pdf")
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

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.postDelayed({
                        if (view.contentHeight == 0) {
                            view.reload()
                        }
                    }, 1000)
                }
            }

            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    linearProgressBar.show()
                    linearProgressBar.progress = newProgress
                    if (newProgress == 100) {
                        linearProgressBar.hide()
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