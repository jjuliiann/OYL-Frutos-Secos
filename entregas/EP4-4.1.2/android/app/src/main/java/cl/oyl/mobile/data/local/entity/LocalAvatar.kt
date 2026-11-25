package cl.oyl.mobile.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_avatars")
data class LocalAvatar(@PrimaryKey val userId: Long, val dataUri: String)
