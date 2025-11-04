package lt.viko.eif.mtrimaitis.Slingo.data

import kotlinx.coroutines.flow.Flow
import lt.viko.eif.mtrimaitis.Slingo.data.dao.UserDao
import lt.viko.eif.mtrimaitis.Slingo.data.models.User
import java.security.MessageDigest

class LoginRepository(private val userDao: UserDao) {
    suspend fun registerUser(username: String, email: String, password: String): Result<User> {
        return try {
            // Check if user already exists
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("User with this email already exists"))
            }

            // Hash password (simple hash for demo - in production use proper hashing)
            val hashedPassword = hashPassword(password)
            
            val user = User(
                username = username,
                email = email,
                password = hashedPassword
            )
            
            val userId = userDao.insertUser(user)
            val newUser = user.copy(id = userId)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val hashedPassword = hashPassword(password)
            val user = userDao.getUserByEmailAndPassword(email, hashedPassword)
            
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserById(id: Long): Flow<User?> = userDao.getUserById(id)

    private fun hashPassword(password: String): String {
        val bytes = password.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }
}
