//package com.procurement.access.model.dto.lots
//
//import com.procurement.access.utils.compare
//import org.junit.jupiter.api.DisplayName
//import org.junit.jupiter.api.Test
//
//internal class LotsDtoTest {
//
//    @Test
//    @DisplayName("lotsRequestDto")
//    fun lotsRequestDto() {
//        compare(UpdateLotsRq::class.java, "/json/lots_req_dto.json")
//    }
//
//    @Test
//    @DisplayName("lotsResponseDto")
//    fun lotsResponseDto() {
//        compare(GetLotsRs::class.java, "/json/lots_resp_dto.json")
//    }
//
//    @Test
//    @DisplayName("lotsUpdateResponseDto")
//    fun lotsUpdateResponseDto() {
//        compare(UpdateLotsRs::class.java, "/json/lots_upd_resp_dto.json")
//    }
//
//    @Test
//    @DisplayName("lotUpdateResponseDto")
//    fun lotUpdateResponseDto() {
//        compare(UpdateLotByBidRs::class.java, "/json/lot_upd_resp_dto.json")
//    }
//}