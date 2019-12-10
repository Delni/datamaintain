package datamaintain.db.driver.mongo

import datamaintain.core.config.ConfigKey
import datamaintain.core.config.getNullableProperty
import datamaintain.core.config.getProperty
import datamaintain.core.db.driver.DatamaintainDriverConfig
import mu.KotlinLogging
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

private val logger = KotlinLogging.logger {}

data class MongoDriverConfig(val dbName: String? = null,
                             val mongoUri: String? = null,
                             val tmpFilePath: Path? = null
) : DatamaintainDriverConfig {
    companion object {
        fun buildConfig(props: Properties): MongoDriverConfig {
            return MongoDriverConfig(
                    props.getNullableProperty(MongoConfigKey.DB_MONGO_DBNAME),
                    props.getProperty(MongoConfigKey.DB_MONGO_URI),
                    props.getProperty(MongoConfigKey.DB_MONGO_TMP_PATH).let { Paths.get(it) })
        }
    }

    override fun toDriver() = MongoDriver(
            dbName ?: throw IllegalArgumentException("mongo db name is missing"),
            mongoUri ?: MongoConfigKey.DB_MONGO_URI.default!!,
            tmpFilePath ?: Paths.get(MongoConfigKey.DB_MONGO_TMP_PATH.default!!))

    override fun log() {
        logger.info { "mongo driver configuration: " }
        dbName?.let { logger.info { "- mongo database name -> $dbName" } }
        mongoUri?.let { logger.info { "- mongo uri -> $mongoUri" } }
        tmpFilePath?.let { logger.info { "- mongo tmp file -> $tmpFilePath" } }
        logger.info { "" }
    }
}

enum class MongoConfigKey(
        override val key: String,
        override val default: String? = null
) : ConfigKey {
    DB_MONGO_URI("db.mongo.uri", "localhost:27017"),
    DB_MONGO_DBNAME("db.mongo.dbname"),
    DB_MONGO_TMP_PATH("db.mongo.tmp.path", "/tmp/datamaintain.tmp"),
}
