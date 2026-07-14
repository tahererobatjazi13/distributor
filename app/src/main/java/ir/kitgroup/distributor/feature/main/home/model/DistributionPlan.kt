package ir.kitgroup.distributor.feature.main.home.model


data class DistributionPlan(
    val PlanGuid: String,
    val NoPlan: Int,
    val Name: String,
    val CountOrder: Int,
    val distributedQuantity: Int,
    val UndistributedQuantity: Int,
    val DateDisp: String
)
