package com.secret.wc_call.utils

import com.secret.wc_call.models.MessageModel

interface NewMessageInterface {
    fun onNewMessage(message: MessageModel)
}