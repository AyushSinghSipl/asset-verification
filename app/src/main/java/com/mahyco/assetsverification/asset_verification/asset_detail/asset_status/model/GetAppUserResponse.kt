package com.mahyco.assetsverification.asset_verification.asset_detail.asset_status.model

import com.google.gson.annotations.SerializedName

data class GetAppUserResponse(

	@field:SerializedName("GetAppUserResponse")
	val getAppUserResponse: java.util.ArrayList<GetAppUserResponseItem>? = null
)

data class GetAppUserResponseItem(

	@field:SerializedName("Role")
	val role: String? = null,

	@field:SerializedName("TechAssistantCode")
	val techAssistantCode: String? = null,

	@field:SerializedName("LoginId")
	val loginId: String? = null,

	@field:SerializedName("IsActive")
	val isActive: String? = null,

	@field:SerializedName("TechAssistantName")
	val techAssistantName: String? = null,

	@field:SerializedName("RoleId")
	val roleId: String? = null
){
	override fun toString(): String {
		return techAssistantName.toString()
	}
}
