package ca.williams_family.model

import net.liftweb.json._
import JsonAST._

import net.fyrie.ratio._

import org.joda.time.{LocalDateTime, DateTimeZone}

object LocalDateTimeSerializer extends Serializer[LocalDateTime] {
  private val LocalDateTimeClass = classOf[LocalDateTime]

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), LocalDateTime] = {
    case (TypeInfo(LocalDateTimeClass, _), json) => json match {
      case JInt(millis) => new LocalDateTime(millis.toLong, DateTimeZone.UTC)
      case JArray(List(JInt(ye), JInt(mo), JInt(da), JInt(ho), JInt(mi), JInt(se), JInt(ss))) =>
        new LocalDateTime(ye.toInt,mo.toInt,da.toInt,ho.toInt,mi.toInt,se.toInt,ss.toInt)
      case x => throw new MappingException("Can't convert "+x+" to LocalDateTime")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: LocalDateTime => JInt(dateToLong(x))
  }
}

object RatioSerializer extends Serializer[Ratio] {
  private val RatioClass = classOf[Ratio]
  private val RatioRegex = """^\s*(\d+)\s*/\s*(\d+)\s*$""".r
  private val IntRegex = """^\s*(\d+)\s*$""".r

  def deserialize(implicit format: Formats): PartialFunction[(TypeInfo, JValue), Ratio] = {
    case (TypeInfo(RatioClass, _), json) => json match {
      case JString(RatioRegex(n,d)) => Ratio(BigInt(n), BigInt(d))
      case JString(IntRegex(n)) => Ratio(BigInt(n))
      case JObject(JField("n", JInt(n)) :: JField("d", JInt(d)) :: Nil) => Ratio(n, d)
      case x => throw new MappingException("Can't convert "+x+" to Ratio")
    }
  }

  def serialize(implicit format: Formats): PartialFunction[Any, JValue] = {
    case x: Ratio => JString(x.toString)
  }
}
