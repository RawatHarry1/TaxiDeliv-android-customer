package com.venus_customer.model.dataClass

import androidx.annotation.Keep

@Keep

data class CouponAndPromos(
    val autosCoupons: List<AutosCoupon>,
    val autosPromotions: List<Any>,
    val commonCoupons: List<Any>,
    val commonOromotions: List<Any>,
    val coupons: List<Coupon>,
    val freshCoupons: List<Any>,
    val freshPromotions: List<Any>,
    val inviteMessage: String,
    val payCoupons: List<Any>,
    val payPromotions: List<Any>,
    val promoCodes: List<PromoCode>,
    val promotions: List<Any>,
    val prosCoupons: List<Any>,
    val prosPromotions: List<Any>,
    val suryaCoupons: List<Any>,
    val suryaPromotions: List<Any>
)

data class AutosCoupon(
    val account_id: Int,
    val allowed_vehicles: List<Int>,
    val autos: Int,
    val benefit_type: Int,
    val cashback_percentage: Int,
    val coupon_card_type: Int,
    val coupon_id: Int,
    val coupon_type: Int,
    val delivery_customer: Int,
    val description: String,
    val discount: Int,
    val discount_maximum: Int,
    val discount_percentage: Int,
    val drop_latitude: Int,
    val drop_longitude: Int,
    val drop_radius: Int,
    val end_time: String,
    val expiry_date: String,
    val fresh: Int,
    val grocery: Int,
    val image: String,
    val is_scratched: Int,
    val is_selected: Int,
    val maximum: Int,
    val meals: Int,
    val menus: Int,
    val operator_id: Int,
    val redeemed_on: String,
    val start_time: String,
    val status: Int,
    val subtitle: String,
    val title: String,
    val type: Int
)

data class Coupon(
    val account_id: Int,
    val allowed_vehicles: List<Int>,
    val autos: Int,
    val benefit_type: Int,
    val cashback_percentage: Int,
    val coupon_card_type: Int,
    val coupon_id: Int,
    val coupon_type: Int,
    val delivery_customer: Int,
    val description: String,
    val discount: Int,
    val discount_maximum: Int,
    val discount_percentage: Int,
    val drop_latitude: Int,
    val drop_longitude: Int,
    val drop_radius: Int,
    val end_time: String,
    val expiry_date: String,
    val fresh: Int,
    val grocery: Int,
    val image: String,
    val is_scratched: Int,
    val is_selected: Int,
    val maximum: Int,
    val meals: Int,
    val menus: Int,
    val operator_id: Int,
    val redeemed_on: String,
    val start_time: String,
    val status: Int,
    val subtitle: String,
    val title: String,
    val type: Int
)

data class PromoCode(
    val bonus_type: Int,
    val city_id: String,
    val coupon_id_autos: Int,
    val coupons_validity_autos: Int,
    val end_date: String,
    val is_active: Int,
    val max_number: Int,
    val money_to_add: Int,
    val num_redeemed: Int,
    val promo_code: String,
    val promo_id: Int,
    val promo_owner_client_id: String,
    val promo_type: Int,
    val start_date: String,
    val user_type: Int
)