package org.timestamp.lib.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AssetLinkTarget(
    val namespace: String,
    @SerialName("package_name")
    val packageName: String,
    @SerialName("sha256_cert_fingerprints")
    val fingerPrints: List<String>,
)

@Serializable
data class AssetLink(
    val relation: List<String>,
    val target: AssetLinkTarget
)
