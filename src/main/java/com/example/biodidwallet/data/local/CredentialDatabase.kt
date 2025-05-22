package com.example.biodidwallet.data.local

import android.content.Context
import androidx.room.*
import java.util.*

@Entity(tableName = "verifiable_credentials")
data class VerifiableCredential(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String,
    val issuer: String,
    val issuanceDate: Long,
    val expirationDate: Long?,
    val ownerDid: String,
    val subject: String,
    val credentialData: String, // JSON格式的凭证数据
    val proof: String // 签名数据
)

@Dao
interface CredentialDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(credential: VerifiableCredential)

    @Query("SELECT * FROM verifiable_credentials WHERE ownerDid = :didId")
    suspend fun getByDID(didId: String): List<VerifiableCredential>

    @Query("SELECT * FROM verifiable_credentials WHERE id = :id")
    suspend fun getById(id: String): VerifiableCredential?

    @Query("DELETE FROM verifiable_credentials WHERE id = :id")
    suspend fun delete(id: String)
    
    @Query("SELECT * FROM verifiable_credentials WHERE expirationDate > :currentTime OR expirationDate IS NULL")
    suspend fun getValidCredentials(currentTime: Long = System.currentTimeMillis()): List<VerifiableCredential>
}

@Database(entities = [VerifiableCredential::class], version = 1)
abstract class CredentialDatabase : RoomDatabase() {
    abstract fun credentialDao(): CredentialDao

    companion object {
        @Volatile
        private var INSTANCE: CredentialDatabase? = null

        fun getInstance(context: Context): CredentialDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CredentialDatabase::class.java,
                    "credential_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 