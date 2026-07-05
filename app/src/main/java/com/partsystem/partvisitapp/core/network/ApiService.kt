package com.partsystem.partvisitapp.core.network


import com.partsystem.partvisitapp.feature.create_order.model.ApiResponse
import com.partsystem.partvisitapp.feature.create_order.model.FinalFactorRequestDto
import com.partsystem.partvisitapp.feature.main.home.model.GroupProductDto
import com.partsystem.partvisitapp.feature.login.model.LoginResponse
import com.partsystem.partvisitapp.feature.main.home.model.ActDto
import com.partsystem.partvisitapp.feature.main.home.model.ApplicationSettingDto
import com.partsystem.partvisitapp.feature.main.home.model.AssignDirectionCustomerDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDirectionDto
import com.partsystem.partvisitapp.feature.main.home.model.CustomerDto
import com.partsystem.partvisitapp.feature.main.home.model.DiscountDto
import com.partsystem.partvisitapp.feature.main.home.model.InvoiceCategoryDto
import com.partsystem.partvisitapp.feature.create_order.model.MojoodiDto
import com.partsystem.partvisitapp.feature.create_order.model.ValidateCredit
import com.partsystem.partvisitapp.feature.main.home.model.PatternDetailDto
import com.partsystem.partvisitapp.feature.main.home.model.PatternDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductImageDto
import com.partsystem.partvisitapp.feature.main.home.model.ProductPackingDto
import com.partsystem.partvisitapp.feature.report_factor.online.model.ReportFactorDto
import com.partsystem.partvisitapp.feature.main.home.model.SaleCenterDto
import com.partsystem.partvisitapp.feature.main.home.model.VatDto
import com.partsystem.partvisitapp.feature.main.home.model.VisitScheduleDto
import com.partsystem.partvisitapp.feature.login.model.VisitorDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiService {

    @POST("LoginUser")
    suspend fun loginUser(
        @Query("UserName") userName: String,
        @Query("Password") password: String
    ): Response<LoginResponse>

    @GET("ApplicationSetting")
    suspend fun getApplicationSetting(): Response<List<ApplicationSettingDto>>

    @GET("Visitor")
    suspend fun getVisitors(@Query("visitorId") visitorId: Int): Response<List<VisitorDto>>

    @GET("VisitSchedule")
    suspend fun getVisitSchedule(@Query("visitorId") visitorId: Int): Response<List<VisitScheduleDto>>

    @GET("GroupProduct")
    suspend fun getGroupProducts(): Response<List<GroupProductDto>>

    @GET("Product")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("ProductImage")
    suspend fun getProductImages(): Response<List<ProductImageDto>>

    @GET("ProductPacking")
    suspend fun getProductPacking(): Response<List<ProductPackingDto>>

    @GET("Customer")
    suspend fun getCustomers(@Query("visitorId") visitorId: Int): Response<List<CustomerDto>>

    @GET("CustomerDirection")
    suspend fun getCustomerDirections(@Query("visitorId") visitorId: Int): Response<List<CustomerDirectionDto>>

    @GET("AssignDirectionCustomer")
    suspend fun getAssignDirectionCustomer(@Query("visitorId") visitorId: Int): Response<List<AssignDirectionCustomerDto>>

    @GET("InvoiceCategory")
    suspend fun getInvoiceCategories(): Response<List<InvoiceCategoryDto>>

    @GET("Pattern")
    suspend fun getPattern(@Query("visitorId") visitorId: Int): Response<List<PatternDto>>

    @GET("PatternDetail")
    suspend fun getPatternDetails(@Query("visitorId") visitorId: Int): Response<List<PatternDetailDto>>

    @GET("Act")
    suspend fun getAct(): Response<List<ActDto>>

    @GET("Vat")
    suspend fun getVat(): Response<List<VatDto>>

    @GET("SaleCenter")
    suspend fun getSaleCenters(): Response<List<SaleCenterDto>>

    @GET("Discount")
    suspend fun getDiscounts(): Response<List<DiscountDto>>

    @GET("ReportFactor")
    suspend fun getReportFactorVisitor(
        @Query("type") type: Int,
        @Query("VisitorId") visitorId: Int,
        @Query("Condition") condition: String,
        @Query("PageNumber") pageNumber: Int,
        @Query("PageSize") pageSize: Int
    ): Response<List<ReportFactorDto>>

    @GET("ReportFactor")
    suspend fun getReportFactorDetail(
        @Query("type") type: Int,
        @Query("FactorId") factorId: Int,
        @Query("PageNumber") pageNumber: Int,
        @Query("PageSize") pageSize: Int
    ): Response<List<ReportFactorDto>>


    @GET("ReportFactor")
    suspend fun getReportFactorCustomer(
        @Query("type") type: Int,
        @Query("CustomerId") customerId: Int,
        @Query("PageNumber") pageNumber: Int,
        @Query("PageSize") pageSize: Int
    ): Response<List<ReportFactorDto>>


    @POST("InsertFactor")
    suspend fun sendFactorToServer(
        @Body factors: List<FinalFactorRequestDto>
    ): Response<ApiResponse>

    @GET("Mojoodi")
    suspend fun checkMojoodi(
        @Query("anbarId") anbarId: Int,
        @Query("productId") productId: Int,
        @Query("persianDate") persianDate: String
    ): Response<List<MojoodiDto>>

    @GET("ValidateCredit")
    suspend fun validateCreditCustomer(
        @Query("customerId") customerId: Int,
        @Query("persianDate") persianDate: String,
        @Query("Kind") kind: Int
    ): Response<List<ValidateCredit>>
}
