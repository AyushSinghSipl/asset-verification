package com.mahyco.assetsverification.asset_verification.asset_detail.asset_status.model

import com.google.gson.annotations.SerializedName

data class GetAppUserParam(

	@field:SerializedName("filterValue")
	val filterValue: String? = null,

	@field:SerializedName("FilterOption")
	val filterOption: String? = null
)
