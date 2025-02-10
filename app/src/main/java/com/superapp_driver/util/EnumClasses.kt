package com.superapp_driver.util

enum class TripStatus(val type: Int){
    REQUESTED(0),
    ACCEPTED(1),
    STARTED(2),
    ENDED(3),
    REJECTED_BY_DRIVER(4),
    CANCELLED_BY_CUSTOMER(5),
    TIMEOUT(6),
    ACCEPTED_BY_OTHER_DRIVER(7),
    ACCEPTED_THEN_REJECTED(8),
    CANCELLED_ACCEPTED_REQUEST(10),
    RIDE_CANCELLED_BY_CUSTOMER(13),
    ARRIVED(14),
    NOT_RATED_BY_DRIVER(21)
}


enum class DriverDocumentStatusForApp(val type: String){
    PENDING("PENDING"),
    APPROVED("APPROVED"),
    REJECTED("REJECTED")
}


enum class DocumentApprovalDetail(val type: String){
    NOT_UPLOADED("NOT_UPLOADED"),
    REJECTED("REJECTED"),
    APPROVED("APPROVED"),
    UPLOADED("UPLOADED"),
    EXPIRED("EXPIRED")
}