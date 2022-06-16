//
//  StyleCEventLogger.swift
//  StyleCiOS
//
//  Created by sumin jin on 2022/06/16.
//

import Foundation
import AppsFlyerLib
import FirebaseAnalytics

protocol EventLogger {
    associatedtype T
    
    func getEventName() -> String
    
    // 분석 도구마다 params 타입이 다를 수 있음
    func getEventParams(data: Any) -> T?
    func logEvent(data: Any)
}

enum Firebase : String, EventLogger {
    
    typealias T = Dictionary<String, Any>
    
    func getEventName() -> String {
        switch self {
        case .purchase:
            return AnalyticsEventPurchase
        }
    }
    
    // TODO: Analytics Event Param 구성
    func getEventParams(data: Any) -> Dictionary<String, Any>? {
        switch self {
        case.purchase:
            return [:]
        }
    }
    
    case purchase
    
    func logEvent(data: Any) {
        Analytics.logEvent(getEventName(), parameters: getEventParams(data: data))
    }
}

enum AppsFlyer : String, EventLogger {
    
    typealias T = Dictionary<String, Any>
    
    func getEventName() -> String {
        switch self {
        case .registration:
            return AFEventCompleteRegistration
        case .firstPurchase:
            return Constants.AFEventFirstPurchase
        }
    }
    
    // TODO: Appsflyer Event Param 구성
    func getEventParams(data: Any) -> Dictionary<String, Any>? {
        switch self {
        case .registration:
            return [:]
        case .firstPurchase:
            return [:]
        }
    }
    
    case registration
    case firstPurchase
    
    func logEvent(data: Any) {
        AppsFlyerLib.shared().logEvent(getEventName(), withValues: getEventParams(data: data))
    }
}
