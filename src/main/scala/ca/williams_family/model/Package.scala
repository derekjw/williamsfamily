package ca.williams_family
package object model {

  import net.fyrie.redis.RedisClient
  import net.fyrie.redis.{Commands => cmd}
  import net.fyrie.redis.serialization.{Parse, Format}
  import Parse.Implicits._
  
  import net.liftweb.json._
  import net.liftweb.http.S

  import org.joda.time.{LocalDateTime, DateTimeZone}

  implicit def redisClient: RedisClient = GlobalRedisClient //if (S.inStatefulScope_?) SessionRedisClient else GlobalRedisClient

  implicit val jsonFormat = DefaultFormats + RatioSerializer + LocalDateTimeSerializer

  implicit val parsePhoto = Parse[Photo](x => Serialization.read[Photo](new String(x, "UTF-8")))

  implicit val redisFormat = Format{
    case x: Photo => Serialization.write(x)
  }

  def uri[A](in: A)(implicit u: URI[A]) = u(in)

  def dateToLong(in: LocalDateTime): Long = in.toDateTime(DateTimeZone.UTC).getMillis

}
