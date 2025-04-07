package com.example.my.project.authenticator.ui.activities.iap

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.billingclient.api.ProductDetails
import com.example.my.project.authenticator.R
import com.example.my.project.authenticator.databinding.ActivityPremiumBinding
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
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PremiumActivity : BaseActivity() {
    private val binding: ActivityPremiumBinding by lazy {
        ActivityPremiumBinding.inflate(layoutInflater)
    }

    @Inject
    lateinit var viewModel: BillingViewModel

    private var selectedProduct: ProductDetails? = null
    private var monthlyProduct: ProductDetails? = null
    private var yearlyProduct: ProductDetails? = null
    private var shouldShowToast = false
    private var doesHaveCurrentPurchase = false
    private var selectedOfferType: String = ""
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
            yearlyCheck.setImageResource(R.drawable.ic_radio_check)
            monthlyCheck.setImageResource(R.drawable.ic_radio_uncheck)
            selectedOfferType = BillingViewModel.BASE_PLAN_ANNUAL_SUBSCRIPTION

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
            viewModel.getProductDetails().observe(this@PremiumActivity) {
                setPrices(it)
            }

            viewModel.hasActiveSubs.observe(this@PremiumActivity) {
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

            viewModel.isBillingConnected().observe(this@PremiumActivity) {
                if (!it) {
                    toast(getString(R.string.failed_to_connect_to_billing_server))
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.apply {
            icClose.setOnClickListener {
                if (isFromSplash) {
                    if (!viewModel.isLanguageShown()) {
                        navigateToLanguageSelection()
                    } else {
                        navigateToMainActivity()
                    }
                } else finish()
            }

            monthlyLayout.singleClick {
                yearlyCheck.setImageResource(R.drawable.ic_radio_uncheck)
                monthlyCheck.setImageResource(R.drawable.ic_radio_check)
                btnBuy.text = getString(R.string.n_continue)
                selectedProduct = monthlyProduct
                selectedOfferType = BillingViewModel.BASE_PLAN_MONTHLY_SUBSCRIPTION

                selectSubscriptionLayout(
                    selectedLayout = binding.monthlyLayout,
                    unselectedLayout = binding.yearlyLayout
                )
            }

            yearlyLayout.singleClick {
                yearlyCheck.setImageResource(R.drawable.ic_radio_check)
                monthlyCheck.setImageResource(R.drawable.ic_radio_uncheck)
                btnBuy.text = getString(R.string.start_a_3_day_free_trial)
                selectedProduct = yearlyProduct
                selectedOfferType = BillingViewModel.BASE_PLAN_ANNUAL_SUBSCRIPTION

                selectSubscriptionLayout(
                    selectedLayout = binding.yearlyLayout,
                    unselectedLayout = binding.monthlyLayout
                )
            }

            btnBuy.singleClick {
                try {
                    handleSubscriptionPurchase(selectedProduct)
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
        selectedProduct = yearlyProduct

        var monthlyPostTrialPrice = ""
        monthlyProduct = products[BillingViewModel.BASE_PLAN_MONTHLY_SUBSCRIPTION]
        monthlyProduct?.subscriptionOfferDetails?.forEach { offerDetails ->
            offerDetails.pricingPhases.pricingPhaseList.let { phases ->
                for (pricingPhase in phases) {
                    if (pricingPhase.priceAmountMicros > 0) {
                        monthlyPostTrialPrice =
                            "${pricingPhase.priceCurrencyCode} ${(pricingPhase.priceAmountMicros / 1_000_000)}"
                        break
                    }
                }
            }
        }

        binding.apply {
            if (monthlyPostTrialPrice.trim().isNotEmpty()) {
                tvAmountMonthly.text = monthlyPostTrialPrice
            } else tvAmountMonthly.text = "$3.99"

            if (annualPostTrialPrice.trim().isNotEmpty()) {
                binding.tvAmountYearly.text = annualPostTrialPrice
            } else binding.tvAmountYearly.text = "$23.99"
        }
    }

    private fun selectSubscriptionLayout(
        selectedLayout: MaterialCardView?,
        unselectedLayout: MaterialCardView?
    ) {
        selectedLayout?.apply {
            strokeColor = ResourcesCompat.getColor(
                resources,
                android.R.color.white,
                theme
            )
        }

        unselectedLayout?.apply {
            strokeColor = ResourcesCompat.getColor(
                resources,
                android.R.color.transparent,
                theme
            )
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
                            toast(it)
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