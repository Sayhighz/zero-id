package com.zero.id.app.model

/**
 * Represents a request for user data from a third party, parsed from the QR code.
 */
data class DataRequest(
    // A field to distinguish this from a VerificationRequest, e.g., "DATA_REQUEST"
    val type: String,
    // The name of the entity requesting the data, e.g., "MyBank"
    val requester: String,
    // The reason for the data request, e.g., "To open a new bank account"
    val purpose: String,
    // A list of keys for the data being requested, e.g., ["fullName", "idNumber"]
    val claims: List<String>
)
