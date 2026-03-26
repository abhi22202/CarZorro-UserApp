package com.example.carzorrouserside.data.model.profile

import com.google.gson.annotations.SerializedName

data class WalletHistoryResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: WalletHistoryPaginationData?
)

data class WalletHistoryPaginationData(
    @SerializedName("current_page")
    val currentPage: Int,
    @SerializedName("last_page")
    val lastPage: Int,
    @SerializedName("per_page")
    val perPage: Int,
    @SerializedName("total")
    val total: Int,
    @SerializedName("data")
    val transactions: List<WalletTransaction>
)

data class WalletTransaction(
    @SerializedName("id")
    val id: Int,
    @SerializedName("razorpay_transaction_id")
    val razorpayTransactionId: String?,
    @SerializedName("payment_status")
    val paymentStatus: String?,
    @SerializedName("payable_amount")
    val payableAmount: Double?,
    @SerializedName("payment_date")
    val paymentDate: String?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("reason")
    val reason: String?,
    @SerializedName("user_payment_type")
    val userPaymentType: String?,
    @SerializedName("notes")
    val notes: String?
) {
    val isCredit: Boolean
        get() = userPaymentType?.equals("Credit", ignoreCase = true) == true
    
    val isDebit: Boolean
        get() = userPaymentType?.equals("Debit", ignoreCase = true) == true
}

