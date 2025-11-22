package lt.viko.eif.mtrimaitis.Slingo.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import lt.viko.eif.mtrimaitis.Slingo.data.dao.FavoriteDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.FriendRequestDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.FriendshipDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.PlaylistDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.SharedPlaylistDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.SongDao
import lt.viko.eif.mtrimaitis.Slingo.data.dao.UserDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.FriendRequest
import lt.viko.eif.mtrimaitis.Slingo.data.models.Friendship
import lt.viko.eif.mtrimaitis.Slingo.data.models.Playlist
import lt.viko.eif.mtrimaitis.Slingo.data.models.PlaylistSong
import lt.viko.eif.mtrimaitis.Slingo.data.models.SharedPlaylist
import lt.viko.eif.mtrimaitis.Slingo.data.models.Song
import lt.viko.eif.mtrimaitis.Slingo.data.models.User

@Database(
    entities = [
        User::class, 
        Song::class, 
        Playlist::class, 
        PlaylistSong::class, 
        lt.viko.eif.mtrimaitis.Slingo.data.models.FavoriteSong::class,
        FriendRequest::class,
        Friendship::class,
        SharedPlaylist::class
    ],
    version = 4, // Updated to support friends and shared playlists
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun friendRequestDao(): FriendRequestDao
    abstract fun friendshipDao(): FriendshipDao
    abstract fun sharedPlaylistDao(): SharedPlaylistDao
}

