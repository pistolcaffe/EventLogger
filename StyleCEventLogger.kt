package com.webview.stylec.event

import android.content.Context
import android.os.Bundle
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.JsonParser

sealed class EventLogger<T> {
    sealed class AppsFlyer : EventLogger<Map<String, Any>>() {

        object Registration : AppsFlyer() {
            override val eventName: String
                get() = AFInAppEventType.COMPLETE_REGISTRATION
        }

        object ContentView : AppsFlyer() {
            override val eventName: String
                get() = AFInAppEventType.CONTENT_VIEW

            override fun getEventParams(data: String): Map<String, Any> {
                val jsonObject = JsonParser().parse(data).asJsonObject
                return HashMap<String, Any>().apply {
                    put(AFInAppEventParameterName.CURRENCY, jsonObject.get(AFInAppEventParameterName.CURRENCY).asString)
                    put(AFInAppEventParameterName.CONTENT_ID, jsonObject.get(AFInAppEventParameterName.CONTENT_ID).asString)
                    put(AFInAppEventParameterName.CONTENT_TYPE, jsonObject.get(AFInAppEventParameterName.CONTENT_TYPE).asString)
                    put(AFInAppEventParameterName.PRICE, jsonObject.get(AFInAppEventParameterName.PRICE).asString)
                }
            }
        }

        object FirstPurchase : AppsFlyer() {
            override val eventName: String
                get() = "first_purchase"

            override fun getEventParams(data: String): Map<String, Any> {
                return getPurchaseEventParams(data)
            }
        }

        object Purchase : AppsFlyer() {
            override val eventName: String
                get() = AFInAppEventType.PURCHASE

            override fun getEventParams(data: String): Map<String, Any> {
                return getPurchaseEventParams(data)
            }
        }

        override fun logEvent(context: Context, data: String?) {
            AppsFlyerLib.getInstance().logEvent(context, eventName, data?.run {
                getEventParams(this)
            })
        }

        protected fun getPurchaseEventParams(data: String): Map<String, Any> {
            val purchasedJsonObject = JsonParser().parse(data).asJsonObject
            return HashMap<String, Any>().apply {
                put(AFInAppEventParameterName.CURRENCY, purchasedJsonObject.get(AFInAppEventParameterName.CURRENCY).asString)
                put(AFInAppEventParameterName.CONTENT_ID, purchasedJsonObject.get(AFInAppEventParameterName.CONTENT_ID).asString)
                put(AFInAppEventParameterName.REVENUE, purchasedJsonObject.get(AFInAppEventParameterName.REVENUE).asInt)
                put(AFInAppEventParameterName.QUANTITY, purchasedJsonObject.get(AFInAppEventParameterName.QUANTITY).asString)
                put(AFInAppEventParameterName.VALIDATED, purchasedJsonObject.get(AFInAppEventParameterName.VALIDATED).asBoolean)
                put(AFInAppEventParameterName.CONTENT_TYPE, purchasedJsonObject.get(AFInAppEventParameterName.CONTENT_TYPE).asString)
                put(AFInAppEventParameterName.ORDER_ID, purchasedJsonObject.get(AFInAppEventParameterName.ORDER_ID).asString)
                put(AFInAppEventParameterName.RECEIPT_ID, purchasedJsonObject.get(AFInAppEventParameterName.RECEIPT_ID).asString)
            }
        }
    }

    sealed class Firebase : EventLogger<Bundle>() {

        object Purchase : Firebase() {
            override val eventName: String
                get() = FirebaseAnalytics.Event.PURCHASE

            override fun getEventParams(data: String): Bundle {
                val purchasedJsonObject = JsonParser().parse(data).asJsonObject
                return purchasedJsonObject.let {
                    val items = mutableListOf<Bundle>()
                    val contentIds = it[AFInAppEventParameterName.CONTENT_ID].asString.split(",")
                    val contentNames = it["af_content_name"].asString.split(",")

                    for (i in contentIds.indices) {
                        val item = Bundle().apply {
                            putString(FirebaseAnalytics.Param.ITEM_ID, contentIds[i])
                            putString(FirebaseAnalytics.Param.ITEM_NAME, contentNames[i])
                        }
                        items.add(item)
                    }

                    Bundle().apply {
                        putString(FirebaseAnalytics.Param.TRANSACTION_ID, it[AFInAppEventParameterName.ORDER_ID].asString)
                        putString(FirebaseAnalytics.Param.CURRENCY, it[AFInAppEventParameterName.CURRENCY].asString)
                        putLong(FirebaseAnalytics.Param.VALUE, it[AFInAppEventParameterName.REVENUE].asLong)
                        putParcelableArray(FirebaseAnalytics.Param.ITEMS, items.toTypedArray())
                    }
                }
            }
        }

        override fun logEvent(context: Context, data: String?) {
            FirebaseAnalytics.getInstance(context).logEvent(eventName, data?.run {
                getEventParams(this)
            })
        }
    }

    open fun getEventParams(data: String): T? {
        return null
    }

    abstract val eventName: String
    abstract fun logEvent(context: Context, data: String? = null)
}