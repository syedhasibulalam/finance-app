package com.achievemeaalk.freedjf.util

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Comprehensive input validation utilities for the Finance Tracker app
 */
object InputValidation {
    
    // Constants for validation limits
    private const val MAX_AMOUNT = 999999999.99
    private const val MIN_AMOUNT = 0.01
    private const val MAX_DESCRIPTION_LENGTH = 500
    private const val MAX_ACCOUNT_NAME_LENGTH = 50
    private const val MAX_CATEGORY_NAME_LENGTH = 30
    
    /**
     * Validation result class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    /**
     * Validates monetary amounts
     */
    fun validateAmount(amountString: String): ValidationResult {
        if (amountString.isBlank()) {
            return ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_amount_empty))
        }
        
        // Remove any non-numeric characters except decimal point
        val cleanAmount = amountString.replace(Regex("[^0-9.]"), "")
        
        if (cleanAmount.isEmpty()) {
            return ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_amount_invalid))
        }
        
        // Check for multiple decimal points
        if (cleanAmount.count { it == '.' } > 1) {
            return ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_amount_format))
        }
        
        val amount = try {
            BigDecimal(cleanAmount).setScale(2, RoundingMode.HALF_UP)
        } catch (e: NumberFormatException) {
            return ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_number_invalid))
        }
        
        return when {
            amount.toDouble() < MIN_AMOUNT -> ValidationResult(
                false,
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(
                    com.achievemeaalk.freedjf.R.string.error_amount_min,
                    String.format("%.2f", MIN_AMOUNT)
                )
            )
            amount.toDouble() > MAX_AMOUNT -> ValidationResult(
                false, 
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(
                    com.achievemeaalk.freedjf.R.string.error_amount_max,
                    String.format("%.0f", MAX_AMOUNT)
                )
            )
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Safely converts amount string to double with validation
     */
    fun parseAmount(amountString: String): Double? {
        val validation = validateAmount(amountString)
        if (!validation.isValid) return null
        
        val cleanAmount = amountString.replace(Regex("[^0-9.]"), "")
        return try {
            BigDecimal(cleanAmount).setScale(2, RoundingMode.HALF_UP).toDouble()
        } catch (e: NumberFormatException) {
            null
        }
    }
    

    
    fun validateNotes(notes: String): ValidationResult {
        return when {
            notes.length > MAX_DESCRIPTION_LENGTH -> ValidationResult(
                false,
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(
                    com.achievemeaalk.freedjf.R.string.error_notes_max,
                    MAX_DESCRIPTION_LENGTH
                )
            )
            else -> ValidationResult(true)
        }
    }

    /**
     * Validates account names
     */
    fun validateAccountName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_account_name_empty))
            name.length > MAX_ACCOUNT_NAME_LENGTH -> ValidationResult(
                false,
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(
                    com.achievemeaalk.freedjf.R.string.error_account_name_max,
                    MAX_ACCOUNT_NAME_LENGTH
                )
            )
            name.contains(Regex("[<>\"'&]")) -> ValidationResult(
                false,
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_account_name_invalid)
            )
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates category names
     */
    fun validateCategoryName(name: String): ValidationResult {
        return when {
            name.isBlank() -> ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_category_name_empty))
            name.length > MAX_CATEGORY_NAME_LENGTH -> ValidationResult(
                false,
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(
                    com.achievemeaalk.freedjf.R.string.error_category_name_max,
                    MAX_CATEGORY_NAME_LENGTH
                )
            )
            name.contains(Regex("[<>\"'&]")) -> ValidationResult(
                false,
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_category_name_invalid)
            )
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates that transfer accounts are different
     */
    fun validateTransferAccounts(fromAccountId: Int?, toAccountId: Int?): ValidationResult {
        return when {
            fromAccountId == null -> ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_select_source_account))
            toAccountId == null -> ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_select_destination_account))
            fromAccountId == toAccountId -> ValidationResult(
                false,
                com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_accounts_same)
            )
            else -> ValidationResult(true)
        }
    }
    
    /**
     * Validates complete transaction data
     */
    fun validateTransaction(
        amount: String,
        accountId: Int?,
        categoryId: Int?,
        destinationAccountId: Int?,
        isTransfer: Boolean
    ): ValidationResult {
        // Validate amount
        val amountValidation = validateAmount(amount)
        if (!amountValidation.isValid) return amountValidation

        // Validate account selection
        if (accountId == null) {
            return ValidationResult(false, "Please select an account")
        }

        // Validate transfer-specific requirements
        if (isTransfer) {
            return validateTransferAccounts(accountId, destinationAccountId)
        } else {
            // Validate category for non-transfer transactions
            if (categoryId == null) {
                return ValidationResult(false, com.achievemeaalk.freedjf.MonefyApplication.appContext.getString(com.achievemeaalk.freedjf.R.string.error_select_category))
            }
        }

        return ValidationResult(true)
    }
} 