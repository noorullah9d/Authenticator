package com.example.my.project.authenticator.ui.activities.iap

import android.app.Activity
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.my.project.authenticator.utils.PrefsHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

class BillingViewModel(
    context: Context
) : ViewModel(), PurchasesUpdatedListener, ProductDetailsResponseListener {

    private val _purchases = MutableStateFlow<List<Purchase>?>(null)
    val purchases: Flow<List<Purchase>?> get() = _purchases.asStateFlow()

    private val _productWithProductDetails = MutableLiveData<Map<String, ProductDetails>>()
    private val _isBillingConnected = MutableLiveData<Boolean>()
    val hasActiveSubs: MutableLiveData<Boolean> = MutableLiveData()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    var acknowledgeCallback: ((Boolean) -> Unit)? = null
    private var retriesRemaining = 2

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK
            && !purchases.isNullOrEmpty()
        ) {
            _purchases.value = purchases
            for (purchase in purchases) {
                acknowledgePurchases(purchase)
            }
        }
    }

    override fun onProductDetailsResponse(
        billingResult: BillingResult,
        productDetailsList: List<ProductDetails>
    ) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        println(debugMessage)
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                var newMap = emptyMap<String, ProductDetails>()
                if (productDetailsList.isNotEmpty()) {
                    newMap = productDetailsList.associateBy { it.productId }
                }
                _productWithProductDetails.postValue(newMap)
            }
        }
    }

    suspend fun isAdsRemoved(): Boolean {
        return purchases
            .firstOrNull { it != null } // wait for at least one emission
            ?.isNotEmpty() == true
    }


    private fun acknowledgePurchases(purchase: Purchase?) {
        purchase?.let {
            if (!it.isAcknowledged) {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(it.purchaseToken)
                    .build()

                billingClient.acknowledgePurchase(
                    params
                ) { billingResult ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                        it.purchaseState == Purchase.PurchaseState.PURCHASED
                    ) {
//                        MyPostAnalytics.myPostAnalytic("premium_purchase_successful")
                        acknowledgeCallback?.invoke(true)
                    }
                }
            }
        }
    }

    // Establish a connection to Google Play.
    fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases and product details here
                    queryPurchases()
                    queryProductDetails()
                    _isBillingConnected.postValue(true)
                }
            }

            override fun onBillingServiceDisconnected() {
                if (retriesRemaining > 0) {
                    retriesRemaining--
                }
                _isBillingConnected.postValue(false)
            }
        })
    }

    fun buySubscription(
        productDetails: ProductDetails,
        activity: Activity?
    ) {
        try {
            val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
            val billingParams = offerToken?.let {
                billingFlowParamsBuilder(
                    productDetails = productDetails,
                    offerToken = it
                )
            }

            if (billingParams != null) {
                launchBillingFlow(
                    activity,
                    billingParams.build()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun billingFlowParamsBuilder(
        productDetails: ProductDetails,
        offerToken: String
    ): BillingFlowParams.Builder {
        return BillingFlowParams.newBuilder().setProductDetailsParamsList(
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(productDetails)
                    .setOfferToken(offerToken)
                    .build()
            )
        )
    }

    fun getProductDetails(): LiveData<Map<String, ProductDetails>> = _productWithProductDetails

    fun isBillingConnected(): LiveData<Boolean> = _isBillingConnected

    // Query Google Play Billing for existing purchases.
    // New purchases will be provided to PurchasesUpdatedListener.onPurchasesUpdated().
    fun queryPurchases() {
        if (billingClient.isReady) {
            // Query for existing subscription products that have been purchased.
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS)
                    .build()
            ) { billingResult, mPurchaseList ->

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchaseList: MutableList<Purchase> = arrayListOf()

                    if (mPurchaseList.isNotEmpty()) {
                        mPurchaseList.forEach {
                            when (it.purchaseState) {
                                Purchase.PurchaseState.PURCHASED -> {
                                    purchaseList.add(it)
                                    if (!it.isAcknowledged) {
                                        acknowledgePurchases(it)
                                    }
                                }

                                Purchase.PurchaseState.PENDING -> {}
                            }
                        }
                        _purchases.value = purchaseList
                        hasActiveSubs.postValue(purchaseList.isNotEmpty())
                    } else {
                        _purchases.value = emptyList()
                        hasActiveSubs.postValue(false)
                    }
                }
            }
        }
    }

    // Query Google Play Billing for products available to sell and present them in the UI
    fun queryProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
        val productList = mutableListOf<QueryProductDetailsParams.Product>()

        // Generate query product list
        packageIds.forEach { packageId ->
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(packageId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }


        for (product in packageIds) {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )
        }

        params.setProductList(productList).let { productDetailsParams ->
            billingClient.queryProductDetailsAsync(productDetailsParams.build(), this)
        }
    }

    fun isLanguageShown(): Boolean {
        return PrefsHelper.isLanguageShown
    }

    private fun launchBillingFlow(activity: Activity?, params: BillingFlowParams) {
        activity?.let {
            if (billingClient.isReady) {
                billingClient.launchBillingFlow(it, params)
            }
        }
    }

    private fun terminateBillingConnection() {
        billingClient.endConnection()
    }

    override fun onCleared() {
        terminateBillingConnection()
        super.onCleared()
    }

    companion object {
        // Package IDs
//        const val PACKAGE_SPLASH_YEARLY = "dec24_splash_yearly"
//        const val PACKAGE_PREMIUM_BUTTON_CLICK = "dec24_premium_button_click"

        // Base Plan IDs
        const val BASE_PLAN_WEEKLY_SUBSCRIPTION = "weekly_sub"
        const val BASE_PLAN_MONTHLY_SUBSCRIPTION = "monthly_sub"
        const val BASE_PLAN_ANNUAL_SUBSCRIPTION = "yearly_sub"

        // Offer IDs
        const val OFFER_3DAY_FREE_TRIAL = "3day-free-trial"

        // Define packages (Base plans and Offers will be handled later during purchase)
        val packageIds = listOf(
            BASE_PLAN_WEEKLY_SUBSCRIPTION,
            BASE_PLAN_MONTHLY_SUBSCRIPTION,
            BASE_PLAN_ANNUAL_SUBSCRIPTION
        )
    }
}