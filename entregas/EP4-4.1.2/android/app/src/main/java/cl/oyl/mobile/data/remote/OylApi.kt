package cl.oyl.mobile.data.remote

import cl.oyl.mobile.model.auth.Account
import cl.oyl.mobile.model.auth.Credentials
import cl.oyl.mobile.model.auth.RegisterData
import cl.oyl.mobile.model.checkout.OrderRequest
import cl.oyl.mobile.model.mapa.PickupLocation
import cl.oyl.mobile.model.pedidos.PurchaseOrder
import cl.oyl.mobile.model.pedidos.StatusChange
import cl.oyl.mobile.model.perfil.ProfileUpdate
import cl.oyl.mobile.model.productos.Product
import android.content.Context
import okhttp3.OkHttpClient
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
    @GET("health") suspend fun health(): Map<String, String> // Comprueba el Gateway.
    @GET("health/{service}") suspend fun serviceHealth(@Path("service") service: String): Map<String, String>
    @POST("auth/register") suspend fun register(@Body data: RegisterData): Account // Registra usuarios.
    @POST("auth/login") suspend fun login(@Body data: Credentials): Account // Inicia sesión y recibe token.
    @PUT("users/{id}") suspend fun updateProfile(@Path("id") id: Long, @Body data: ProfileUpdate): Account // Actualiza perfil.
    @GET("products") suspend fun products(): List<Product> // Consulta el catálogo remoto.
    @POST("products") suspend fun createProduct(@Query("adminId") adminId: Long, @Body product: Product): Product // Crea producto.
    @PUT("products/{id}") suspend fun updateProduct(@Path("id") id: Long, @Query("adminId") adminId: Long, @Body product: Product): Product // Edita producto.
    @retrofit2.http.DELETE("products/{id}") suspend fun deactivateProduct(@Path("id") id: Long, @Query("adminId") adminId: Long): Product
    @POST("products/{id}/stock") suspend fun changeStock(@Path("id") id: Long, @Query("adminId") adminId: Long, @Body change: StockChange): Product // Ajusta inventario.
    @POST("orders") suspend fun createOrder(@Body order: OrderRequest): PurchaseOrder // Envía compra.
    @GET("orders") suspend fun orders(@Query("userId") userId: Long): List<PurchaseOrder> // Consulta pedidos.
    @PATCH("orders/{id}/status") suspend fun changeOrderStatus(@Path("id") id: Long, @Query("adminId") adminId: Long, @Body change: StatusChange): PurchaseOrder // Cambia estado.
    @GET("locations") suspend fun locations(): List<PickupLocation> // Obtiene puntos de retiro.
}

object RemoteApis {
    // La prueba de una dirección nueva nunca envía el token de la tienda activa.
    fun publicProbe(url: String): OylApi = Retrofit.Builder().baseUrl(url)
        .client(OkHttpClient.Builder().callTimeout(6, java.util.concurrent.TimeUnit.SECONDS).build())
        .addConverterFactory(GsonConverterFactory.create()).build().create(OylApi::class.java)

    fun oyl(context: Context, url: String = GatewayAddress.current(context)): OylApi {
        // Lee el token guardado después de iniciar sesión.
        val preferences = GatewayAddress.auth(context, url)
        // Agrega el token en las llamadas que lo necesitan.
        val client = OkHttpClient.Builder().addInterceptor { chain ->
            val request = chain.request().newBuilder()
            preferences.getString("jwt", null)?.takeIf { it.isNotBlank() }?.let { request.header("Authorization", "Bearer $it") }
            chain.proceed(request.build())
        }.build()
        // Crea Retrofit para el API Gateway propio.
        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OylApi::class.java)
    }
}

data class StockChange(val delta: Int)
