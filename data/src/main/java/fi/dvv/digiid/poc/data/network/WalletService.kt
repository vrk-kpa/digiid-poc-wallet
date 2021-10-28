package fi.dvv.digiid.poc.data.network

import fi.dvv.digiid.poc.vc.VerifiablePresentation
import retrofit2.http.GET
import retrofit2.http.Url

interface WalletService {
    @GET("api/0.1/coreid")
    suspend fun getCoreID(): VerifiablePresentation

    @GET
    suspend fun getVerificationMethod(@Url url: String): String
}