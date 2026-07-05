package com.partsystem.partvisitapp.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.partsystem.partvisitapp.core.database.entity.ProductPackingEntity

@Dao
interface ProductPackingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packings: List<ProductPackingEntity>)

    @Query("DELETE FROM ProductPacking")
    suspend fun clearProductPacking()

    @Query(
        """
        SELECT * FROM ProductPacking
        WHERE packingId = :packingId
        AND productId = :productId
    """
    )
    suspend fun getPackingByPackingIdAndProductId(
        packingId: Int,
        productId: Int
    ): List<ProductPackingEntity>

    @Query(
        """
    SELECT packingId 
    FROM ProductPacking
    WHERE productId = :productId
    LIMIT 1
    """
    )
    suspend fun getPackingIdByProductId(productId: Int): Int?

}

