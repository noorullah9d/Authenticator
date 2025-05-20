package com.example.my.project.authenticator.ui.activities.iap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.billingclient.api.ProductDetails
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.analytics.IAP_DISMISS_SETTINGS
import com.example.my.project.authenticator.analytics.IAP_DISMISS_SPLASH
import com.example.my.project.authenticator.analytics.IAP_SPLASH
import com.example.my.project.authenticator.analytics.IAP_START_FREE_TRIAL
import com.example.my.project.authenticator.analytics.logScreen
import com.example.my.project.authenticator.analytics.postAnalytics
import com.example.my.project.authenticator.databinding.ActivityFreeTrialBinding
import com.example.my.project.authenticator.extensions.browse
import com.example.my.project.authenticator.extensions.formatFreeTrialFooter
import com.example.my.project.authenticator.extensions.isInternetAvailable
import com.example.my.project.authenticator.extensions.singleClick
import com.example.my.project.authenticator.extensions.startActivityWithAnimation
import com.example.my.project.authenticator.extensions.toast
import com.example.my.project.authenticator.ui.activities.BaseActivity
import com.example.my.project.authenticator.ui.activities.MainActivity
import com.example.my.project.authenticator.ui.activities.SelectLanguageActivity
import com.example.my.project.authenticator.ui.activities.SplashScreen
import com.example.my.project.authenticator.utils.PRIVACY_POLICY_URL
import com.example.my.project.authenticator.utils.PrefsHelper
import com.example.my.project.authenticator.utils.TERMS_CONDITIONS_URL
import com.example.my.project.authenticator.utils.WEEKLY
import com.example.my.project.authenticator.utils.YEARLY
import com.example.my.project.authenticator.utils.splashIAPExperiment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FreeTrialActivity : BaseActivity() {
    private val binding: ActivityFreeTrialBinding by lazy {
        ActivityFreeTrialBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModel: BillingViewModel

    private var selectedProduct: ProductDetails? = null
    private var yearlyProduct: ProductDetails? = null
    private var weeklyProduct: ProductDetails? = null
    private var shouldShowToast = false
    private var doesHaveCurrentPurchase = false
    private var isFromSplash = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        logScreen(IAP_SPLASH)

        isFromSplash = intent?.getBooleanExtra("isFromSplash", false) == true

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.purchases.collect { purchaseList ->
                    Log.d("IAP", "purchase list: $purchaseList")
                    PrefsHelper.isAdsRemoved = purchaseList?.isNotEmpty() == true
                }
            }
        }

        initViews()
        setupClickListeners()
        handleBackPress()
    }

    private fun handleBackPress() {
        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                postAnalytics(IAP_DISMISS_SPLASH)
                if (isFromSplash) {
                    if (!viewModel.isLanguageShown()) {
                        navigateToLanguageSelection()
                    } else {
                        navigateToMainActivity()
                    }
                } else finish()
            }
        })
    }

    private fun navigateToLanguageSelection() {
        startActivityWithAnimation<SelectLanguageActivity>()
        finish()
    }

    private fun navigateToMainActivity() {
        startActivityWithAnimation<MainActivity>()
        finish()
    }

    private fun initViews() {
        binding.apply {
            tvFooter.formatFreeTrialFooter(
                onPrivacyPolicyClicked = {
                    browse(PRIVACY_POLICY_URL)
                },
                onTermsAndConditionsClicked = {
                    browse(TERMS_CONDITIONS_URL)
                }
            )
        }

        lifecycleScope.launch {
            viewModel.getProductDetails().observe(this@FreeTrialActivity) {
                setPrices(it)
            }

            viewModel.hasActiveSubs.observe(this@FreeTrialActivity) {
                Log.d("IAP", "called! $doesHaveCurrentPurchase")
                if (!doesHaveCurrentPurchase && shouldShowToast) {
                    shouldShowToast = false
                    toast(getString(R.string.you_don_t_have_any_purchases_yet))
                }
            }

            viewModel.acknowledgeCallback = {
                val intent = Intent(binding.root.context, SplashScreen::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
            }

            viewModel.isBillingConnected().observe(this@FreeTrialActivity) {
                if (!it) {
                    toast(getString(R.string.failed_to_connect_to_billing_server))
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            icClose.setOnClickListener {
                postAnalytics(IAP_DISMISS_SPLASH)
                if (isFromSplash) {
                    if (!viewModel.isLanguageShown()) {
                        navigateToLanguageSelection()
                    } else {
                        navigateToMainActivity()
                    }
                } else finish()
            }

            btnBuy.singleClick {
                try {
                    handleSubscriptionPurchase(selectedProduct)
                    postAnalytics(IAP_START_FREE_TRIAL)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setPrices(products: Map<String, ProductDetails>) {
        var annualPostTrialPrice = ""
        yearlyProduct = products[BillingViewModel.BASE_PLAN_ANNUAL_SUBSCRIPTION]
        yearlyProduct?.subscriptionOfferDetails?.forEach { offerDetails ->
            offerDetails.pricingPhases.pricingPhaseList.let { phases ->
                for (pricingPhase in phases) {
                    if (pricingPhase.priceAmountMicros > 0) {
                        annualPostTrialPrice =
                            "${pricingPhase.priceCurrencyCode} ${(pricingPhase.priceAmountMicros / 1_000_000)}"
                        break
                    }
                }
            }
        }

        var weeklyPostTrialPrice = ""
        weeklyProduct = products[BillingViewModel.BASE_PLAN_WEEKLY_SUBSCRIPTION]
        weeklyProduct?.subscriptionOfferDetails?.forEach { offerDetails ->
            offerDetails.pricingPhases.pricingPhaseList.let { phases ->
                for (pricingPhase in phases) {
                    if (pricingPhase.priceAmountMicros > 0) {
                        weeklyPostTrialPrice =
                            "${pricingPhase.priceCurrencyCode} ${(pricingPhase.priceAmountMicros / 1_000_000)}"
                        break
                    }
                }
            }
        }

        binding.apply {
            when (splashIAPExperiment) {
                WEEKLY -> {
                    if (weeklyPostTrialPrice.trim().isNotEmpty()) {
                        binding.tvAmountYearly.text = getString(
                            R.string.free_trial_offer_then_amount_weekly,
                            weeklyPostTrialPrice
                        )
                    } else binding.tvAmountYearly.text =
                        getString(R.string.free_trial_offer_then_amount_weekly, "$7.99")


                    selectedProduct = weeklyProduct
                }

                YEARLY -> {
                    if (annualPostTrialPrice.trim().isNotEmpty()) {
                        binding.tvAmountYearly.text = getString(
                            R.string.free_trial_offer_then_amount_yearly,
                            annualPostTrialPrice
                        )
                    } else binding.tvAmountYearly.text =
                        getString(R.string.free_trial_offer_then_amount_yearly, "$23.99")

                    selectedProduct = yearlyProduct
                }
            }
        }
    }

    private fun handleSubscriptionPurchase(selected: ProductDetails?) {
        if (isInternetAvailable()) {
            if (viewModel.isBillingConnected().value == true) {
                if (!doesHaveCurrentPurchase) {
                    if (selected != null) {
                        Log.d(
                            "IAP",
                            "Selected Product: ${selected.productId}, Price: ${
                                selected.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(
                                    0
                                )?.priceAmountMicros
                            }"
                        )
                        viewModel.buySubscription(
                            productDetails = selected,
                            activity = this
                        )
                    } else {
                        resources?.getString(R.string.failed_to_connect)?.let {
                            toast("No offer selected!")
                        }
                    }
                } else {
                    resources?.getString(R.string.subscription_already_purchased)
                        ?.let {
                            toast(it)
                        }
                }
            } else {
                resources?.getString(R.string.failed_to_connect)?.let {
                    toast(it)
                }
            }
        } else {
            toast(getString(R.string.no_internet_connection))
        }
    }
}