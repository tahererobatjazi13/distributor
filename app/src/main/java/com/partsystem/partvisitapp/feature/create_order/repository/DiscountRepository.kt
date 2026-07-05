package com.partsystem.partvisitapp.feature.create_order.repository

import android.util.Log
import com.partsystem.partvisitapp.core.database.AppDatabase
import com.partsystem.partvisitapp.core.database.dao.ActDao
import com.partsystem.partvisitapp.core.database.dao.DiscountDao
import com.partsystem.partvisitapp.core.database.dao.FactorDao
import com.partsystem.partvisitapp.core.database.dao.MojoodiDao
import com.partsystem.partvisitapp.core.database.dao.PatternDao
import com.partsystem.partvisitapp.core.database.dao.ProductDao
import com.partsystem.partvisitapp.core.database.dao.ProductPackingDao
import com.partsystem.partvisitapp.core.database.entity.DiscountEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountGiftsEntity
import com.partsystem.partvisitapp.core.database.entity.DiscountStairsEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDetailEntity
import com.partsystem.partvisitapp.core.database.entity.FactorDiscountEntity
import com.partsystem.partvisitapp.core.database.entity.FactorGiftInfoEntity
import com.partsystem.partvisitapp.core.database.entity.FactorHeaderEntity
import com.partsystem.partvisitapp.core.database.entity.PatternDetailEntity
import com.partsystem.partvisitapp.core.database.entity.ProductEntity
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity
import com.partsystem.partvisitapp.core.utils.ActKind
import com.partsystem.partvisitapp.core.utils.CalculateUnit2Type
import com.partsystem.partvisitapp.core.utils.DiscountApplyKind
import com.partsystem.partvisitapp.core.utils.DiscountCalculationKind
import com.partsystem.partvisitapp.core.utils.DiscountExecuteKind
import com.partsystem.partvisitapp.core.utils.DiscountInclusionKind
import com.partsystem.partvisitapp.core.utils.DiscountKind
import com.partsystem.partvisitapp.core.utils.DiscountPaymentKind
import com.partsystem.partvisitapp.core.utils.DiscountPriceKind
import com.partsystem.partvisitapp.core.utils.DiscountUnitKind
import com.partsystem.partvisitapp.core.utils.PatternInclusionKind
import com.partsystem.partvisitapp.core.utils.UnitKind
import com.partsystem.partvisitapp.feature.create_order.model.DiscountEshantyunResult
import com.partsystem.partvisitapp.feature.create_order.model.ProductModel
import com.partsystem.partvisitapp.feature.create_order.model.VwFactorDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.floor

class DiscountRepository @Inject constructor(
    private val factorDao: FactorDao,
    private val discountDao: DiscountDao,
    private val patternDao: PatternDao,
    private val productDao: ProductDao,
    private val productPackingDao: ProductPackingDao,
    private val mojoodiDao: MojoodiDao,
    private val actDao: ActDao,
    private val database: AppDatabase

) {
    suspend fun calculateDiscountInsert(
        applyKind: Int,
        factorHeader: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?,
        hasTaxConnection: Boolean?
    ) {
        if (factorHeader.patternId == null) return

        val hasGift = false
        val insertCount: Byte = 0
        val editCount: Byte = 0 //مشخص می کند به تخفیفات/اضافات سطر اضافه شده یا خیر
        val factorSortCode = 0
        val productSortCode = 0

        // حذف تمام تخفیف‌های قبلی برای این ردیف یا فاکتور
        when (applyKind) {
            DiscountApplyKind.ProductLevel.ordinal -> {
                if (factorDetail != null) {
                    factorDao.deleteDiscountsByFactorDetailId(factorDetail.id)
                }
            }

            DiscountApplyKind.FactorLevel.ordinal -> {
                factorDao.deleteFactorLevelDiscounts(factorHeader.id)
            }
        }
        //region Init

        var discounts =
            getDiscounts(applyKind, factorHeader.createDate!!, factorHeader.persianDate!!, true)

        val pattern = patternDao.getPattern(factorHeader.patternId!!) ?: return
        val discountInclusionKind: Int = pattern.discountInclusionKind!!

        // Remove already applied discounts
        val usedDiscountIds = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
            factorDetail?.getDiscountIds(applyKind, null) ?: emptyList()
        } else {
            factorDetail?.getDiscountIds(applyKind, factorDetail.id) ?: emptyList()
        }
        //  usedDiscountIds=[]
        discounts = discounts.filter { !usedDiscountIds.contains(it.id) }

        // ---------- فیلتر مودیان - فقط برای سطح فاکتور ----------
        // اگر HasTaxConnection درست باشد و تخفیف در سطح فاکتور باشد
        // و نوعش تخفیف و اضافات باشد discountkind=0||1 (کسورات نباشد)
        // و DiscountCalculationKind!=Eshantyun اشانتیون نباشد
        // تخفیف اعمال نشود

        // ---------- فیلتر مودیان -  برای سطح ردیف ----------
        // اگر HasTaxConnection درست باشد و تخفیف در سطح ردیف باشد
        // و نوعش  اضافات باشد discountkind=1
        // تخفیف اعمال نشود

        /* if (hasTaxConnection == true) {

             discounts = discounts.filterNot { discount ->

                 when (applyKind) {

                     // سطح فاکتور
                     DiscountApplyKind.FactorLevel.ordinal -> {
                         (discount.kind == DiscountKind.Discount.ordinal ||
                                 discount.kind == DiscountKind.Addition.ordinal) &&
                                 discount.calculationKind != DiscountCalculationKind.Eshantyun.ordinal
                     }

                     // سطح ردیف
                     DiscountApplyKind.ProductLevel.ordinal -> {
                         discount.kind == DiscountKind.Addition.ordinal
                     }

                     else -> false
                 }
             }
         }*/
        // Apply pattern inclusion filter
        if (discountInclusionKind == PatternInclusionKind.List.ordinal) {
            val listPatternDetail = getPatternDetailById(pattern.id)
            val result = java.util.ArrayList<Int>()

            if (listPatternDetail != null) {
                for (patternDetail in listPatternDetail) {
                    if (patternDetail.discountId != null) result.add(patternDetail.discountId)
                }
            }

            discounts = if (result != null && result.size > 0) {
                discounts.filter { it.id in result }.toMutableList()

            } else {
                mutableListOf()
            }
        }

        var lastSortCode = 1
        var hasGiftEshantyun = false

        for (discount in discounts) {

            if (discount.calculationKind == DiscountCalculationKind.Eshantyun.ordinal ||
                discount.calculationKind == DiscountCalculationKind.Gift.ordinal
            ) {
                hasGiftEshantyun = true
                //lastSortCode = factorDetail.size + 1
                break
            }
        }

        processDiscounts(
            discounts = discounts,
            factor = factorHeader,
            factorDetail = factorDetail,
            applyKind = applyKind,
            repository = this@DiscountRepository
        )
    }

    suspend fun processDiscounts(
        discounts: List<DiscountEntity>,
        factor: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?,
        applyKind: Int, // 0 = FactorLevel, 1 = ProductLevel
        repository: DiscountRepository
    ) {

        var factorSortCode = 0
        var productSortCode =
            0 // Note: در کد اصلی به اشتباه از factorSortCode استفاده شده در بخش else!
        var insertCount = 0
        var hasGift = false

        for (discount in discounts) {

            val (productOfDiscount, productIds) = checkProductInDiscount(
                discount,
                factor,
                factorDetail,
                applyKind
            )

            if (productOfDiscount) {
                // همیشه رکورد جدید ایجاد کن (چون تخفیف‌های قبلی حذف شده‌اند)
                val maxFactorDiscountId = getMaxFactorDiscountId()
                val factorDiscount = FactorDiscountEntity(
                    id = maxFactorDiscountId + 1,
                    factorId = factor.id,
                    discountId = discount.id,
                    productId = if (applyKind == DiscountApplyKind.ProductLevel.ordinal) factorDetail?.productId else null,
                    factorDetailId = if (applyKind == DiscountApplyKind.ProductLevel.ordinal) factorDetail?.id else null,
                    sortCode = 0,
                    price = 0.0,
                    discountPercent = 0.0,
                    discountKind = discount.kind,
                )

                // Set SortCode
                if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    if (factorSortCode == 0) {
                        val existingCount = discountDao.getFactorDiscountCountByFactorId(factor.id)
                        factorSortCode = existingCount + 1
                    }
                    factorDiscount.sortCode = factorSortCode++
                    factorDiscount.productId = null
                    factorDiscount.factorDetailId = null
                } else {
                    if (productSortCode == 0) {
                        val existingCount = discountDao.getFactorDiscountCountByFactorDetailId(
                            factorDetail?.id ?: -1
                        )
                        productSortCode = existingCount + 1
                    }
                    factorDiscount.sortCode = productSortCode++
                    factorDiscount.productId = factorDetail!!.productId
                    factorDiscount.factorDetailId = factorDetail.id
                }

                // Calculate discount amount
                calculate(
                    discount,
                    factorDiscount,
                    productIds,
                    applyKind,
                    factor,
                    factorDetail,
                    repository
                )

                insertCount++

                // Insert into DB
                if (discount.calculationKind != DiscountCalculationKind.Eshantyun.ordinal &&
                    discount.calculationKind != DiscountCalculationKind.Gift.ordinal
                ) {
                    if (factorDiscount.price > 0) {
                        factorDao.insertFactorDiscount(factorDiscount)
                    }

                    // Handle FactorGiftInfo for Factor-Level discounts
                    if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                        val allValue = getSumUnit1ValueByProductIds(factor.id, productIds)
                        if (allValue > 0) {
                            var maxId = repository.getMaxFactorGiftInfoId()
                            for (productId in productIds) {
                                val unitValue =
                                    repository.getSumUnit1ValueByProduct(factor.id, productId)
                                val giftInfo = FactorGiftInfoEntity(
                                    id = ++maxId,
                                    factorId = factor.id,
                                    discountId = discount.id,
                                    price = kotlin.math.round((factorDiscount.price * unitValue) / allValue),
                                    productId = productId
                                )
                                factorDao.insertFactorGift(giftInfo)
                            }
                        }
                    }
                } else {
                    hasGift = true
                }
            }
        }
    }


    private suspend fun checkProductInDiscount(
        discount: DiscountEntity,
        factor: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?,
        applyKind: Int
    ): Pair<Boolean, List<Int>> {
        var productIds = java.util.ArrayList<Int?>()

        val inclusionKind = discount.inclusionKind
        return when (inclusionKind) {
            DiscountInclusionKind.All.ordinal -> {
                Pair(true, emptyList())
            }

            DiscountInclusionKind.List.ordinal -> {
                val factorProductIds =
                    if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                        // Fetch all product IDs in factor (non-gift)
                        getFactorProductIds(factor.id)
                    } else {
                        listOf(factorDetail?.productId ?: -1)
                    }

                val discountProductIds =
                    discount.productIds // Assume this is available or fetched via DAO
                val intersection = factorProductIds.intersect(discountProductIds.toSet())

                if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    Pair(intersection.isNotEmpty(), intersection.toList())
                } else {
                    Pair(intersection.isNotEmpty(), emptyList())
                }
            }

            DiscountInclusionKind.Group.ordinal -> {
                if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    // برای سطح فاکتور، باید تمام محصولات فاکتور را بررسی کنیم
                    val productIds = getFactorProductIds(factor.id)
                    val matched =
                        discountDao.getProductMatchDiscountGroup(discount.id, ArrayList(productIds))
                    Pair(matched.isNotEmpty(), emptyList())
                } else {
                    // برای سطح محصول
                    val productId = factorDetail?.productId ?: return Pair(false, emptyList())
                    val matched = discountDao.getProductMatchDiscountGroup(
                        discount.id,
                        arrayListOf(productId)
                    )
                    Pair(matched.isNotEmpty(), emptyList())
                }
            }

            // Handle Group, ProductKind similarly with repository calls
            else -> {
                // For simplicity, return false — implement based on your DB structure
                Pair(true, emptyList())
            }
        }
    }

    private suspend fun calculate(
        discount: DiscountEntity,
        factorDiscount: FactorDiscountEntity,
        productIds: List<Int>,
        applyKind: Int, // 0 = FactorLevel, 1 = ProductLevel
        factor: FactorHeaderEntity,
        factorDetail: FactorDetailEntity?,
        repository: DiscountRepository
    ) {
        var discountPrice = 0.0
        var price = 0.0
        val lastSortCode: Int = getMaxSortCode(factor.id)

        // --- تعیین پایه‌ی محاسبه قیمت ---
        when (discount.priceKind) {

            DiscountPriceKind.SalePrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    getSumPrice(factor.id, productIds)
                } else {
                    factorDetail?.price ?: 0.0
                }
            }

            DiscountPriceKind.DiscountedPrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    getSumPriceAfterDiscountByProductIds(factor.id, productIds)
                } else {
                    factorDetail?.getPriceAfterDiscount() ?: 0.0
                }
            }

            DiscountPriceKind.PurePrice.ordinal -> {
                price = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    getSumPriceAfterVatByProductIds(factor.id, productIds)
                } else {
                    factorDetail?.getPriceAfterVat() ?: 0.0
                }
            }

            DiscountPriceKind.FinalPrice.ordinal -> {
                if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                    price = factor.finalPrice
                }
            }
        }

        // --- محاسبه بر اساس نوع تخفیف ---
        when (discount.calculationKind) {
            DiscountCalculationKind.Simple.ordinal -> {
                when (discount.paymentKind) {
                    DiscountPaymentKind.Percent.ordinal -> {
                        discountPrice = (discount.priceAmount / 100.0) * price
                    }

                    DiscountPaymentKind.Price.ordinal -> {
                        discountPrice = discount.priceAmount
                    }

                    DiscountPaymentKind.UnitPrice.ordinal -> {
                        val unitValue = if (applyKind == DiscountApplyKind.FactorLevel.ordinal) {
                            getSumUnit1ValueByFactorId(factor.id)
                        } else {
                            factorDetail?.unit1Value ?: 0.0
                        }
                        discountPrice = discount.priceAmount * unitValue
                    }
                }
            }

            DiscountCalculationKind.Stair.ordinal -> {
                discountPrice = discount.calculateStairDiscount(price)
            }

            DiscountCalculationKind.StairByValue.ordinal -> {

                discountPrice += calculateDiscountStairByValue(
                    factorId = factor.id,
                    discountId = discount.id,
                    productIds = productIds,
                    factorDetailId = factorDetail?.id,
                    price = price
                )
            }

            DiscountCalculationKind.Eshantyun.ordinal -> {

                val patternId = factor.patternId ?: return
                var actId = factorDao.getPatternDetailActId(patternId, ActKind.Product.ordinal)
                if (actId == null) {
                    actId = factorDao.getPatternDetailActId(patternId, ActKind.Service.ordinal)
                }
                if (actId == null) return

                val eshantyun = calculateDiscountEshantyun(
                    factorId = factor.id,
                    discountId = discount.id,
                    productIds = productIds
                ) ?: return

                val lastSortCode = getMaxSortCode(factor.id)
                val productId = eshantyun.productId
                val packingId = if (eshantyun.packingId == 0) {
                    productPackingDao.getPackingIdByProductId(eshantyun.productId) ?: 0
                } else {
                    eshantyun.packingId
                }
                val anbarId = eshantyun.anbarId
                val maxId = repository.getMaxFactorDetailId()

                /*  val productWithRate =
                      productDao.getProductWithRateAct(productId, factor.actId!!).map { it.rate }
                          .firstOrNull()*/
                val productWithRate =
                    productDao.getProductRate(productId, actId).firstOrNull() ?: 0.0

                val productRate = productWithRate


                // 1. ساخت detail هدیه
                val detail = FactorDetailEntity(
                    id = maxId + 1,
                    factorId = factor.id,
                    productId = productId,
                    actId = factor.actId,
                    sortCode = lastSortCode + 1,
                    anbarId = anbarId,
                    isGift = 1,
                    unit1Value = 0.0,
                    unit2Value = 0.0,
                    packingValue = 0.0,
                    packingId = packingId,
                    price = 0.0,
                    unit1Rate = productRate
                )

                // تنظیم مقادیر بر اساس نوع واحد هدیه
                when (eshantyun.unitKind) {
                    DiscountUnitKind.Unit1.ordinal -> {
                        detail.packingId  = if (eshantyun.packingId == 0) {
                            productPackingDao.getPackingIdByProductId(eshantyun.productId) ?: 0
                        } else {
                            eshantyun.packingId
                        }
                        detail.unit1Value = eshantyun.value
                        val values = fillProductValues(
                            anbarId = anbarId,
                            productId = productId,
                            factorDetailId = detail.id,
                            packingId = detail.packingId,
                            unit1ValueInput = detail.unit1Value,
                            unit2ValueInput = null,
                            packingValueInput = null,
                            isInput = false
                        )
                        detail.unit2Value = values["Unit2Value"] ?: 0.0
                        detail.packingValue = values["PackingValue"] ?: 0.0
                    }

                    DiscountUnitKind.Unit2.ordinal -> {
                        detail.packingId  = if (eshantyun.packingId == 0) {
                            productPackingDao.getPackingIdByProductId(eshantyun.productId) ?: 0
                        } else {
                            eshantyun.packingId
                        }
                        detail.unit2Value = eshantyun.value
                        val values = fillProductValues(
                            anbarId = anbarId,
                            productId = productId,
                            factorDetailId = detail.id,
                            packingId = detail.packingId,
                            unit1ValueInput = null,
                            unit2ValueInput = detail.unit2Value,
                            packingValueInput = null,
                            isInput = false
                        )
                        detail.unit1Value = values["Unit1Value"] ?: 0.0
                        detail.packingValue = values["PackingValue"] ?: 0.0
                    }

                    DiscountUnitKind.Packing.ordinal -> {
                        detail.packingId  = if (eshantyun.packingId == 0) {
                            productPackingDao.getPackingIdByProductId(eshantyun.productId) ?: 0
                        } else {
                            eshantyun.packingId
                        }
                        detail.packingValue = eshantyun.value
                        val values = fillProductValues(
                            anbarId = anbarId,
                            productId = productId,
                            factorDetailId = detail.id,
                            packingId = detail.packingId,
                            unit1ValueInput = null,
                            unit2ValueInput = null,
                            packingValueInput = detail.packingValue,
                            isInput = false
                        )
                        detail.unit1Value = values["Unit1Value"] ?: 0.0
                        detail.unit2Value = values["Unit2Value"] ?: 0.0
                    }

                    else -> return // واحد ناشناخته
                }
                // 3. محاسبه قیمت نهایی (fillByAct)
                fillByAct(detail) // فرض: این تابع detail.price را ست می‌کند

                factorDao.insertFactorDetail(detail)

                // 4. ساخت تخفیف مرتبط با هدیه
                val maxFactorDiscountId = getMaxFactorDiscountId()
                val detailDiscount = FactorDiscountEntity(
                    id = maxFactorDiscountId + 1,
                    factorId = factor.id,
                    discountId = discount.id,
                    productId = detail.productId,
                    factorDetailId = detail.id,
                    sortCode = 1,
                    price = detail.price,
                    discountPercent = 0.0,
                    discountKind = discount.kind
                )

                factorDao.insertFactorDiscount(detailDiscount)

                // مرحله 4: ایجاد FactorGiftInfo برای توزیع هدیه بین محصولات اصلی
                // 5. ساخت لیست FactorGiftInfo
                val gifts = mutableListOf<FactorGiftInfoEntity>()

                val allValue = getSumUnit1Value(factor.id, productId, productIds)

                // if (allValue > 0) {
                val allDetails = getFactorDetailProductIds(factor.id, detail.productId, productIds)
                var maxFactorGiftInfoId = getMaxFactorGiftInfoId()
                for (itemDetail in allDetails) {
                    val giftInfo = FactorGiftInfoEntity(
                        id = ++maxFactorGiftInfoId,
                        factorId = factor.id,
                        discountId = discount.id,
                        productId = itemDetail.productId,
                        price = kotlin.math.round(detail.price * itemDetail.unit1Value / allValue)
                    )
                    factorDao.insertFactorGift(giftInfo)
                }

                //  }

                //  factorDao.insertFactorWithDiscountAndGifts(detail, detailDiscount, gifts)

                // پس از ایجاد هدیه، تخفیف اصلی مبلغی ندارد
                // factorDiscount.price = 0.0
                return
            }

            DiscountCalculationKind.SettlementDate.ordinal -> {
                // calculateDiscountPrice(factor, discount, price)
            }

            DiscountCalculationKind.ProductKind.ordinal -> {
                discountPrice = getDiscountByProductKind(discount.id, factor.id)
            }

            DiscountCalculationKind.Round.ordinal -> {
                val output = factor.finalPrice
                discountPrice =
                    output - (kotlin.math.round(output / discount.priceAmount) * discount.priceAmount)
            }

            DiscountCalculationKind.Gift.ordinal -> {
                if (factor.patternId != null) {
                    var actId =
                        factorDao.getPatternDetailActId(factor.patternId!!, ActKind.Product.ordinal)
                    if (actId == null) {
                        actId = factorDao.getPatternDetailActId(
                            factor.patternId!!,
                            ActKind.Service.ordinal
                        )
                    }
                    if (actId != null) {
                        var allPrice = 0.0
                        when (discount.priceKind) {
                            DiscountPriceKind.SalePrice.ordinal -> {
                                allPrice = repository.getSumPriceByProductIds(factor.id, productIds)
                            }

                            DiscountPriceKind.DiscountedPrice.ordinal -> {
                                allPrice = repository.getSumPriceAfterDiscountByProductIds(
                                    factor.id,
                                    productIds
                                )
                            }

                            DiscountPriceKind.PurePrice.ordinal,
                            DiscountPriceKind.FinalPrice.ordinal -> {
                                allPrice = repository.getSumPriceAfterVatByProductIds(
                                    factor.id,
                                    productIds
                                )
                            }
                        }

                        val gift = getDiscountGift(discount.id, allPrice)
                        if (gift != null) {

                            val detail = FactorDetailEntity(
                                id = 0,
                                factorId = factor.id,
                                productId = gift.productId,
                                sortCode = lastSortCode,
                                anbarId = gift.anbarId,
                                isGift = 1,
                                unit1Value = 0.0,
                                unit2Value = 0.0,
                                packingValue = 0.0,
                                packingId = null,
                                price = 0.0
                            )

                            when (gift.unitKind) {
                                DiscountUnitKind.Unit1.ordinal -> {
                                    detail.unit1Value = gift.value

                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        null,
                                        detail.unit1Value,
                                        null,
                                        null,
                                        false
                                    )
                                    detail.unit2Value = values["Unit2Value"] ?: 0.0
                                    detail.packingValue = values["PackingValue"] ?: 0.0
                                }

                                DiscountUnitKind.Unit2.ordinal -> {
                                    detail.unit2Value = gift.value

                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        null,
                                        null,
                                        detail.unit2Value,
                                        null,
                                        false
                                    )

                                    detail.unit1Value = values["Unit1Value"] ?: 0.0
                                    detail.packingValue = values["PackingValue"] ?: 0.0
                                }

                                DiscountUnitKind.Packing.ordinal -> {
                                    detail.packingId = gift.packingId
                                    detail.packingValue = gift.value
                                    val values = fillProductValues(
                                        detail.anbarId,
                                        detail.productId,
                                        detail.id,
                                        detail.packingId,
                                        null,
                                        null,
                                        detail.packingValue,
                                        false
                                    )


                                    detail.unit1Value = values["Unit1Value"] ?: 0.0
                                    detail.unit2Value = values["Unit2Value"] ?: 0.0
                                }
                            }

                            factorDao.insertFactorDetail(detail)
                            fillByAct(detail)

                            val detailDiscount = FactorDiscountEntity(
                                id = 0,
                                factorId = factor.id,
                                discountId = discount.id,
                                productId = detail.productId,
                                factorDetailId = detail.id,
                                sortCode = 1,
                                price = detail.price,
                                discountPercent = 0.0,
                                discountKind = discount.kind,
                            )
                            factorDao.insertFactorDiscount(detailDiscount)

                            factorDiscount.price = 0.0
                            return
                        }
                    }
                }
            }

            DiscountCalculationKind.DiscountByValue.ordinal -> {
                discountPrice += calculateDiscountByValue(
                    factorId = factor.id,
                    discountId = discount.id,
                    productIds = productIds,
                    factorDetailId = factorDetail?.id,
                    price = price
                )
            }

            DiscountCalculationKind.DiscountByPrice.ordinal -> {
                val item = getDiscountStairByPrice(discount.id, price)
                if (item != null) {
                    val stairPrice =
                        if (discount.executeKind == DiscountExecuteKind.Simple.ordinal) {
                            if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                price * item.price / 100.0
                            } else {
                                item.price
                            }
                        } else {
                            val multiplier = floor(price / item.ratio)
                            if (discount.paymentKind == DiscountPaymentKind.Percent.ordinal) {
                                price * (item.price * multiplier) / 100.0
                            } else {
                                item.price * multiplier
                            }
                        }
                    discountPrice += stairPrice
                }
            }
        }
        // --- نهایی‌سازی ---
        factorDiscount.price = kotlin.math.round(discountPrice)
    }

    // DiscountExtensions.kt
    fun DiscountEntity.calculateStairDiscount(price: Double): Double {
        var discountPrice = 0.0

        // discountStair ممکن است null باشد (چون در Room optional است)
        this.discountStairs?.forEach { item ->
            // اگر item null باشد (مثلاً به دلیل left join یا اشتباه دیتابیس)، نادیده بگیر
            item ?: return@forEach

            val fromPrice = item.fromPrice
            val toPrice = item.toPrice
            val itemPrice = item.price
            val ratio = item.ratio
            val executeKind = this.executeKind
            val paymentKind = this.paymentKind

            when {
                price >= fromPrice && price >= toPrice -> {
                    val currentPrice = toPrice - fromPrice
                    discountPrice += if (executeKind == DiscountExecuteKind.Simple.ordinal) {
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
                            currentPrice * itemPrice / 100.0
                        } else {
                            itemPrice
                        }
                    } else {
                        val multiplier = if (ratio != 0.0) floor(currentPrice / ratio) else 0.0
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
                            currentPrice * (itemPrice * multiplier) / 100.0
                        } else {
                            itemPrice * multiplier
                        }
                    }
                }

                price >= fromPrice && price < toPrice -> {
                    val currentPrice = price - fromPrice
                    discountPrice += if (executeKind == DiscountExecuteKind.Simple.ordinal) {
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
                            currentPrice * itemPrice / 100.0
                        } else {
                            itemPrice
                        }
                    } else {
                        val multiplier = if (ratio != 0.0) floor(currentPrice / ratio) else 0.0
                        if (paymentKind == DiscountPaymentKind.Percent.ordinal) {
                            currentPrice * (itemPrice * multiplier) / 100.0
                        } else {
                            itemPrice * multiplier
                        }
                    }
                }
            }
        }

        return discountPrice
    }

    /**
     * محاسبه تخفیف پله‌ای بر اساس فاکتور، تخفیف، محصولات و قیمت مرجع
     *
     * معادل کامل:
     * getCalculateDiscountStairByValue(factorId, discountId, productIds, factorDetailId, price)
     */
    suspend fun calculateDiscountStairByValue(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>? = null,
        factorDetailId: Int? = null,
        price: Double
    ): Double = withContext(Dispatchers.IO) {
        // دریافت تخفیف + پله‌ها
        val discount = factorDao.getDiscountWithStairs(discountId) ?: return@withContext 0.0

        // دریافت جزئیات فاکتور مطابق فیلتر
        val factorDetails = when {
            factorDetailId != null -> {
                val detail = factorDao.getDetailById(factorId, factorDetailId)
                listOfNotNull(detail)
            }

            !productIds.isNullOrEmpty() -> {
                factorDao.getDetailsByFactorAndProducts(factorId, productIds)
            }

            else -> {
                factorDao.getDetailsByFactorId(factorId)
            }
        }

        if (factorDetails.isEmpty()) return@withContext 0.0

        // محاسبه مجموع مقادیر واحد
        val totalUnit1 = factorDetails.sumOf { it.unit1Value }
        val totalUnit2 = factorDetails.sumOf { it.unit2Value }
        val totalPacking = factorDetails.sumOf { it.packingValue }

        // محاسبه تخفیف بر اساس هر پله
        var totalDiscount = 0.0

        for (stair in discount.discountStairs.orEmpty()) {
            if (stair == null) continue

            // تعیین Val بر اساس UnitKind (0=Unit1, 1=Unit2, 2=Packing)
            val valAmount = when (stair.unitKind) {
                0 -> totalUnit1
                1 -> totalUnit2
                2 -> totalPacking
                else -> 0.0
            }

            if (valAmount == 0.0) continue

            // تشخیص بازه مؤثر
            val effectiveRange = when {
                valAmount >= stair.toPrice -> stair.toPrice - stair.fromPrice
                valAmount >= stair.fromPrice -> valAmount - stair.fromPrice
                else -> 0.0
            }

            if (effectiveRange <= 0) continue

            // محاسبه مبلغ تخفیف
            val discountValue = if (discount.paymentKind == 1) {
                // PaymentKind = 1 → Amount (مبلغ ثابت)
                if (discount.executeKind == 0) {
                    // Simple
                    stair.price
                } else {
                    // Complex
                    val multiplier =
                        if (stair.ratio != 0.0) floor(effectiveRange / stair.ratio) else 0.0
                    stair.price * multiplier
                }
            } else {
                // PaymentKind = 0 → Percent
                val baseValue = price / valAmount // معادل (SumPrice / Val)
                val multiplier = if (discount.executeKind == 0) {
                    effectiveRange
                } else {
                    if (stair.ratio != 0.0) floor(effectiveRange / stair.ratio) else 0.0
                }
                baseValue * multiplier * stair.price / 100.0
            }

            totalDiscount += discountValue
        }

        totalDiscount
    }

    private suspend fun calculateDiscountEshantyun(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>?
    ): DiscountEshantyunResult? {

        val ids = productIds ?: emptyList()

        return discountDao.getCalculateDiscountEshantyun(
            factorId = factorId,
            discountId = discountId,
            productIds = ids,
            productIdsSize = ids.size
        )
    }


    suspend fun getFactorDiscountCount2(factorDetailId: Int): Int {
        return discountDao.getFactorDiscountCountByFactorDetailId(factorDetailId)
    }

    suspend fun insertFactorDiscount(discount: FactorDiscountEntity) {
        discountDao.insertFactorDiscount(discount)
    }

    suspend fun getSumUnit1ValueByProductIds(
        factorId: Int,
        productIds: List<Int>
    ): Double {
        if (productIds.isEmpty()) return 0.0
        return discountDao
            .getSumUnit1ValueByProductIds(factorId, productIds)
            ?: 0.0
    }

    suspend fun getSumUnit1Value(
        factorId: Int,
        productId: Int,
        productIds: List<Int>? = null
    ): Double {
        val ids = productIds ?: emptyList()
        val filterFlag = if (ids.isEmpty()) 0 else 1
        return discountDao.sumUnit1Value(factorId, productId, ids, filterFlag) ?: 0.0
    }

    suspend fun getSumUnit1ValueByProduct(factorId: Int, productId: Int): Double {
        return discountDao.getSumUnit1ValueByProduct(factorId, productId) ?: 0.0
    }

    suspend fun getMaxFactorGiftInfoId(): Int {
        return discountDao.getMaxFactorGiftInfoId() ?: 0
    }

    suspend fun getMaxFactorDiscountId(): Int {
        return discountDao.getMaxFactorDiscountId() ?: 0
    }

    suspend fun getMaxFactorDetailId(): Int {
        return discountDao.getMaxFactorDetailId() ?: 0
    }

    suspend fun insertFactorGiftInfo(info: FactorGiftInfoEntity) {
        discountDao.insertFactorGiftInfo(info)
    }

    suspend fun getFactorProductIds(factorId: Int): List<Int> {
        return discountDao.getFactorProductIds(factorId)
    }

    suspend fun getAppliedDiscountIds(factorId: Int, factorDetailId: Int): List<Int> {
        return discountDao.getAppliedDiscountIds(factorId, factorDetailId)
    }


    suspend fun fillProductValues(
        anbarId: Int?,
        productId: Int,
        factorDetailId: Int?,
        packingId: Int?,
        unit1ValueInput: Double?,
        unit2ValueInput: Double?,
        packingValueInput: Double?,
        isInput: Boolean
    ): Map<String, Double?> {

        val product = productDao.getProductByProductId(productId)
        val packing = packingId?.let {
            productPackingDao.getPackingByPackingIdAndProductId(it, productId).firstOrNull()
        }

        return fillProductValuesInternal(
            anbarId,
            product,
            packing,
            unit1ValueInput,
            unit2ValueInput,
            packingValueInput,
            isInput
        )
    }

    private suspend fun fillProductValuesInternal(
        anbarId: Int?,
        product: ProductEntity?,
        packing: ProductPackingEntity?,
        unit1Input: Double?,
        unit2Input: Double?,
        packingInput: Double?,
        isInput: Boolean
    ): Map<String, Double?> {

        var unit1Value = unit1Input
        var unit2Value = unit2Input
        var packingValue = packingInput

        val calculateUnit2Type = product?.calculateUnit2Type ?: 255
        val convertRatio = product?.convertRatio ?: 0.0
        val hasUnit2 = product?.unit2Id != null

        val packingUnit1 = packing?.unit1Value ?: 0.0
        val packingUnit2 = packing?.unit2Value ?: 0.0

        if (packingValue != null && packingValue != 0.0) {
            if (packingUnit1 != 0.0)
                unit1Value = packingUnit1 * packingValue
            else if (packingUnit2 != 0.0)
                unit2Value = packingUnit2 * packingValue
        }

        if (hasUnit2) {

            // AverageUnits
            if (calculateUnit2Type == CalculateUnit2Type.AverageUnits.ordinal &&
                !isInput && anbarId != null
            ) {
                val mojoodi = mojoodiDao.getMojoodi(anbarId, product!!.id)

                val unit1Amount = mojoodi?.remainValue ?: 1.0
                val unit2Amount = mojoodi?.remainValue2 ?: 1.0

                if (unit1Value != null && unit1Value != 0.0 && unit1Amount != 0.0) {
                    unit2Value = unit2Amount * unit1Value / unit1Amount
                } else if (unit2Value != null && unit2Value != 0.0 && unit2Amount != 0.0) {
                    unit1Value = unit1Amount * unit2Value / unit2Amount
                }
            }

            // StandardFormula
            else if (calculateUnit2Type == CalculateUnit2Type.StandardFormula.ordinal) {
                if (unit1Value != null && unit1Value != 0.0 && convertRatio != 0.0)
                    unit2Value = unit1Value * convertRatio
                else if (unit2Value != null && unit2Value != 0.0 && convertRatio != 0.0)
                    unit1Value = unit2Value / convertRatio
            }
        }

        if (packingValue == null || packingValue == 0.0) {
            if (packingUnit1 != 0.0 && unit1Value != null)
                packingValue = unit1Value / packingUnit1
            else if (packingUnit2 != 0.0 && unit2Value != null)
                packingValue = unit2Value / packingUnit2
        }

        return mapOf(
            "Unit1Value" to unit1Value,
            "Unit2Value" to unit2Value,
            "PackingValue" to packingValue
        )
    }

    private suspend fun fillByAct(insItem: FactorDetailEntity) {
        val actId = insItem.actId ?: return
        val productId = insItem.productId

        val item = actDao.getActDetail(actId, productId) ?: return

        if (item.unitKind == UnitKind.Unit1.ordinal) {
            insItem.price = kotlin.math.round(insItem.unit1Value * item.rate)
        } else {
            insItem.price = kotlin.math.round(insItem.unit2Value * item.rate)
        }
    }

    suspend fun getFactorDetailProductIds(
        factorId: Int,
        productId: Int,
        productIds: List<Int>?
    ): List<VwFactorDetail> {
        return if (productIds.isNullOrEmpty()) {
            factorDao.getFactorDetail(factorId, productId)
        } else {
            factorDao.getFactorDetailByProductIds(factorId, productIds)
        }
    }


    suspend fun getDiscountByProductKind(discountId: Int, factorId: Int): Double {
        return discountDao.getDiscountByProductKind(discountId, factorId)
    }

    suspend fun getDiscountGift(discountId: Int, allPrice: Double): DiscountGiftsEntity? {
        return discountDao.getDiscountGift(discountId, allPrice)
    }

    suspend fun getMaxSortCode(factorId: Int): Int {
        return factorDao.getMaxSortCode(factorId)
    }

    /*suspend fun calculateDiscountByValue(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>?,
        factorDetailId: Int?,
        price: Double
    ): Double {
        return discountDao.getCalculateDiscountByValue(
            factorId = factorId,
            discountId = discountId,
            productIds = productIds,
            factorDetailId = factorDetailId,
            price = price
        ) ?: 0.0
    }*/
    suspend fun calculateDiscountByValue(
        factorId: Int,
        discountId: Int,
        productIds: List<Int>?,
        factorDetailId: Int?,
        price: Double
    ): Double {
        return if (productIds.isNullOrEmpty()) {
            discountDao.getCalculateDiscountByValueWithoutProducts(
                factorId = factorId,
                discountId = discountId,
                factorDetailId = factorDetailId,
                price = price
            )
        } else {
            discountDao.getCalculateDiscountByValueWithProducts(
                factorId = factorId,
                discountId = discountId,
                productIds = productIds,
                factorDetailId = factorDetailId,
                price = price
            )
        } ?: 0.0
    }

    suspend fun getDiscountStairByPrice(discountId: Int, price: Double): DiscountStairsEntity? {
        return discountDao.getDiscountStairByPrice(discountId, price)
    }

    suspend fun getSumPriceByProductIds(factorId: Int, productIds: List<Int>): Double {
        // اگر لیست خالی بود، نتیجه 0 است
        if (productIds.isEmpty()) return 0.0

        return factorDao.getSumPriceByProductIds(factorId, productIds) ?: 0.0
    }

    suspend fun getSumPrice(factorId: Int, productIds: List<Int> = emptyList()): Double {
        return if (productIds.isEmpty()) {
            // اگر لیست خالی است، همه ردیف‌ها را جمع کن
            factorDao.getSumPrice(factorId) ?: 0.0
        } else {
            factorDao.getSumPriceByProductIds(factorId, productIds) ?: 0.0
        }
    }


    private suspend fun getSumByField(
        factorId: Int,
        productIds: List<Int>,
        fieldSelector: (FactorDetailEntity) -> Double
    ): Double {

        // اگر لیست محصولات خالی باشد، نتیجه 0 است
        if (productIds.isEmpty()) return 0.0
        val details = factorDao.getNonGiftFactorDetailsByProductIds(factorId, productIds)
        return details.sumOf(fieldSelector)
    }

    suspend fun getSumPriceAfterVatByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterVat() }

    suspend fun getSumPriceAfterDiscountByProductIds(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.getPriceAfterDiscount() }

    suspend fun getSumUnit1ValueByProductIds2(factorId: Int, productIds: List<Int>) =
        getSumByField(factorId, productIds) { it.unit1Value }


    suspend fun getSumUnit1ValueByFactorId(factorId: Int): Double {
        return factorDao.getSumUnit1ValueByFactorId(factorId)
    }


    suspend fun getDiscounts(
        applyKind: Int,
        toDate: String,
        persianBeginDate: String,
        includeDetail: Boolean
    ): List<DiscountEntity> = withContext(Dispatchers.IO) {

        if (includeDetail) {
            val discountsWithDetails = discountDao.getDiscountsWithDetails(
                applyKind = applyKind,
                toDate = toDate,
                persianBeginDate = persianBeginDate
            )
            discountsWithDetails.map { item ->
                DiscountEntity(
                    id = item.discount.id,
                    formType = item.discount.formType,
                    code = item.discount.code,
                    name = item.discount.name,
                    kind = item.discount.kind,
                    isAutoCalculate = item.discount.isAutoCalculate,
                    applyKind = item.discount.applyKind,
                    calculationKind = item.discount.calculationKind,
                    inclusionKind = item.discount.inclusionKind,
                    paymentKind = item.discount.paymentKind,
                    priceAmount = item.discount.priceAmount,
                    priceKind = item.discount.priceKind,
                    sabt = item.discount.sabt,
                    beginDate = item.discount.beginDate,
                    persianBeginDate = item.discount.persianBeginDate,
                    hasCash = item.discount.hasCash,
                    hasMaturityCash = item.discount.hasMaturityCash,
                    hasSanad = item.discount.hasSanad,
                    hasSanadAndCash = item.discount.hasSanadAndCash,
                    hasUseToolsPrice = item.discount.hasUseToolsPrice,
                    hasCredit = item.discount.hasCredit,
                    dayCount = item.discount.dayCount,
                    toDate = item.discount.toDate,
                    isSystem = item.discount.isSystem,
                    hasUseToolsPercnet = item.discount.hasUseToolsPercnet,
                    unitKind = item.discount.unitKind,
                    hasLastControl = item.discount.hasLastControl,
                    executeKind = item.discount.executeKind,
                    maxPrice = item.discount.maxPrice,
                    customerFilterKind = item.discount.customerFilterKind,
                    toPersianDate = item.discount.toPersianDate
                ).apply {
                    discountEshantyuns = discountDao.getDiscountEshantyun(item.discount.id)
                    discountGifts = discountDao.getDiscountGift(item.discount.id)
                    discountGroups = discountDao.getDiscountGroup(item.discount.id)
                    discountProducts = discountDao.getDiscountProducts(item.discount.id)
                    discountProductKinds = discountDao.getDiscountProductKind(item.discount.id)
                    discountProductKindInclusions =
                        discountDao.getDiscountProductKindInclusion(item.discount.id)
                    discountStairs = discountDao.getDiscountStair(item.discount.id)
                    discountUsers = discountDao.getDiscountUser(item.discount.id)
                }
            }
        } else {
            discountDao.getDiscounts(
                applyKind = applyKind,
                toDate = toDate,
                persianBeginDate = persianBeginDate
            )
        }
    }

    suspend fun getPatternDetailById(patternId: Int): List<PatternDetailEntity>? {
        return patternDao.getPatternDetailById(patternId)
    }

    suspend fun removeGiftsAndAutoDiscounts(factorId: Int): Any =
        withContext(Dispatchers.IO) {
            try {
                database.runInTransaction {
                    // STEP 1: Delete gift details (CASCADE automatically deletes their discounts)
                    val deletedGiftCount = discountDao.deleteGiftDetails(factorId)

                    // STEP 2: Delete auto-calculated HEADER discounts (factorDetailId = NULL)
                    val deletedHeaderDiscounts =
                        discountDao.deleteAutoCalculatedHeaderDiscounts(factorId)

                    // STEP 3: Delete all gift info records for this factor
                    discountDao.deleteByFactorId(factorId)

                    // STEP 4: Reset factor status and refresh hasDetail flag
                    discountDao.resetSabtAndRefreshHasDetail(factorId)

                    // Optional: Safety fallback if cascade delete failed (defensive)
                    if (deletedGiftCount > 0) {
                        val giftDetailIds = discountDao.getGiftDetailIds(factorId)
                        if (giftDetailIds.isNotEmpty()) {
                            discountDao.deleteDiscountsByDetailIds(giftDetailIds)
                            // Log warning: "Cascade delete may have failed for detail discounts"
                        }
                    }

                    true
                }
            } catch (e: Exception) {
                Log.e(
                    "FactorRepository",
                    "Failed to remove gifts/discounts for factor $factorId",
                    e
                )
                false
            }
        }


    suspend fun calculatePriceForDiscount(
        applyKind: Int,
        factorId: Int,
        factorDetail: FactorDetailEntity? = null,
        productIds: List<Int>? = null
    ): Double {
        return when (applyKind) {
            DiscountApplyKind.FactorLevel.ordinal -> {
                // جمع قیمت کل فاکتور (با فیلتر محصولات اختیاری)
                factorDao.getSumPrice(
                    factorId = factorId,
                    productIds = productIds ?: emptyList()
                ) ?: 0.0
            }

            DiscountApplyKind.ProductLevel.ordinal -> {
                // قیمت از خود ردیف مربوطه گرفته می‌شود
                factorDetail?.price ?: 0.0
            }

            else -> 0.0
        }
    }

}
