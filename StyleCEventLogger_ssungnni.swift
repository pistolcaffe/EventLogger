//
//  StyleCEventLogger.swift
//  StyleCiOS
//
//  Created by sumin jin on 2022/06/16.
//

import Foundation
import AppsFlyerLib
import FirebaseAnalytics


// MARK: - EventType

protocol EventType {

  var name: String { get }
  var parameters: [String: Any?]? { get } //제 경험상 이벤트 트래커 중에 파라미터 타입이 다른건 못봤던것 같긴해요 🤔
}

enum FirebaseEvent: EventType {

  case purchase
  case registration(str: String, model: Model) //enum case 에 associated type 을 넣어줘요!

  var name: String {
    switch self {
    case .purchase:
      return ""
    case let .registration(str, model):
      return "\(str), \(model.xxx)"
    }
  }

  var parameters: [String : Any?]? {
    switch self {
    case .purchase:
      return nil
    case let .registration(str, model):
      return ["key": str]
    }
  }
}

enum FirebaseEvent: EventType {

  case purchase
  case registration(str: String, model: Model)

  var name: String {
    switch self {
    case .purchase:
      return ""
    case let .registration(str, model):
      return "\(str), \(model.xxx)"
    }
  }

  var parameters: [String : Any?]? {
    switch self {
    case .purchase:
      return nil
    case let .registration(str, model):
      return ["key": str]
    }
  }
}


// MARK: - EventTracker
// EventType과 EventTracker 의 인터페이스를 분리해요

protocol EventTrackerProtocol {
  func log(_ event: EventType)
}

class FirebaseEventTracker: EventTrackerProtocol {
  
  func log(_ event: EventType) {
    Analytics.logEvent(event.name, parameters: event.parameters)
  }
}

class AppsFlyerEventTracker: EventTrackerProtocol {
  
  func log(_ event: EventType) {
    AppsFlyerLib.shared().logEvent(event.name, withValues: event.parameters)
  }
}
