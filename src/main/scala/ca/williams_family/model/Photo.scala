package ca.williams_family
package model

import net.fyrie.ratio.Ratio
import net.fyrie.redis.RedisClient
import net.fyrie.redis.{Commands => cmd}

import org.joda.time.{LocalDateTime}

case class Photo(id: String,
                 createDate: LocalDateTime,
                 exposure: Option[Ratio],
                 aperature: Option[Ratio],
                 iso: Option[Ratio],
                 focalLength: Option[Ratio],
                 width: Int,
                 height: Int,
                 images: Map[String,Image])

object Photo extends RedisMeta[Photo] {
  def id(in: Photo) = in.id

  def score(in: Photo): Double = dateToLong(in.createDate)

  def mkId(date: LocalDateTime, hash: String): String =
    date.toString("yyyyMMdd'-'HHmmssSSS").take(17) + "-" + hash

  override def indexes(in: Photo): List[String] = List(in.iso.map(x => "iso" + separator + x.toString)).flatten

  def findAllByMonth(year: Int, month: Int)(implicit r: RedisClient): Stream[Photo] = {
    val startDate = new LocalDateTime(year, month, 1, 0, 0)
    val endDate = startDate.plusMonths(1)
    r send cmd.zrangebyscore[String](indexKey(), min = dateToLong(startDate), max = dateToLong(endDate)) flatMap (ids =>
      r send cmd.mget[Photo](ids.map(key)) map (_.flatten)) getOrElse Stream.empty
  }

  override def afterSave(in: Photo)(implicit r: RedisClient) {
    val year = in.createDate.getYear
    val month = in.createDate.getMonthOfYear
    val day = in.createDate.getDayOfMonth
    r ! cmd.multiexec(List(cmd.zadd(indexKey(Some("years")), year, year),
                           cmd.zadd(indexKey(Some("months")), year * 100 + month, "%04d-%02d" format (year,month)),
                           cmd.zadd(indexKey(Some("days")), year * 10000 + month * 100 + day, "%04d-%02d-%02d" format (year, month, day))))
  }

  def timelineMonths(implicit r: RedisClient): Stream[(Int, Int)] = r send cmd.zrange[String](indexKey(Some("months")), 0, -1) getOrElse Stream.empty map (s => (s.take(4).toInt, s.drop(5).take(2).toInt))
}


case class Image(fileName: String,
                 fileSize: Int,
                 hash: String,
                 width: Int,
                 height: Int)

trait URI[A] {
  def apply(in: A): String
}

object URI {
  implicit object PhotoURI extends URI[Photo] {
    def apply(in: Photo) = "/photo/"+in.id
  }

  implicit object ImageURI extends URI[Image] {
    def apply(in: Image) = "http://photos.williams-family.ca/photos/"+in.fileName
  }
}
