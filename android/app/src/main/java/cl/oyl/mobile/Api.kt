package cl.oyl.mobile

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface OylApi {
    @GET("health") suspend fun health(): Map<String, String>
    @POST("auth/register") suspend fun register(@Body data: RegisterData): Account
    @POST("auth/login") suspend fun login(@Body data: Credentials): Account
    @PUT("users/{id}") suspend fun updateProfile(@Path("id") id: Long, @Body data: ProfileUpdate): Account
    @GET("products") suspend fun products(): List<Product>
    @POST("products") suspend fun createProduct(@Query("adminId") adminId: Long, @Body product: Product): Product
    @PUT("products/{id}") suspend fun updateProduct(@Path("id") id: Long, @Query("adminId") adminId: Long, @Body product: Product): Product
    @POST("products/{id}/stock") suspend fun changeStock(@Path("id") id: Long, @Query("adminId") adminId: Long, @Body change: StockChange): Product
    @POST("orders") suspend fun createOrder(@Body order: OrderRequest): PurchaseOrder
    @GET("orders") suspend fun orders(@Query("userId") userId: Long): List<PurchaseOrder>
    @PATCH("orders/{id}/status") suspend fun status(@Path("id") id: Long, @Query("adminId") adminId: Long, @Body change: StatusChange): PurchaseOrder
    @GET("locations") suspend fun locations(): List<PickupLocation>
}

object Remote {
    // Dirección del computador anfitrión vista desde el emulador Android.
    const val BASE_URL = "http://10.0.2.2:8080/api/"
    val api: OylApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OylApi::class.java)
    }
}
