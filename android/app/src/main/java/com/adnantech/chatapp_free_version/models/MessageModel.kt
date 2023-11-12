package com.secret.wc_call.models


data class MessageModel(
    var type: String,
    val name: String? = null,
    val target: String? = null,
    val data:Any?=null,
    val notification: String?=null
)