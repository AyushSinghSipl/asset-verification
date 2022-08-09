package com.mahyco.assetsverification.asset_verification.asset_detail.asset_status.model

import com.google.gson.annotations.SerializedName

data class SaveAssetStatusParam(

	@field:SerializedName("Status")
	val status: String? = null,

	@field:SerializedName("CreatedBy")
	val createdBy: String? = null,

	@field:SerializedName("TechAssistantCode")
	val TechAssistantCode: String? = null,

	@field:SerializedName("TechAssistantName")
	val TechAssistantName: String? = null,

	@field:SerializedName("AssetQRId")
	val assetQRId: Int? = null,

	@field:SerializedName("Reason")
	val reason: String? = null,

	@field:SerializedName("FileName")
	val FileName: String? = null,

	@field:SerializedName("FileInBite")
	val FileInBite: String? = null,

	@field:SerializedName("NPAType")
	val npaType: String? = null
)
