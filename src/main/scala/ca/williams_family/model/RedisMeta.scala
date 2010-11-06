package ca.williams_family.model

import net.fyrie.redis.RedisClient
import net.fyrie.redis.{Commands => cmd}
import net.fyrie.redis.serialization.Parse

import net.liftweb.util.Helpers.{snakify}
import net.liftweb.http.SessionVar

import se.scalablesolutions.akka.dispatch.Future

object GlobalRedisClient extends RedisClient
object SessionRedisClient extends SessionVar[RedisClient](new RedisClient) {
  registerCleanupFunc(session => this.disconnect)
}

abstract class RedisMeta[A: Parse: Manifest] {
  val namespace: String =
    snakify(manifest[A].toString.split("\\.").toList.last)

  def separator: String = "::"

  def id(in: A): String

  def key(id: String) = namespace + separator + id

  def indexKey(name: Option[String] = None) = namespace + "-index" + (name.map(separator + _).getOrElse(""))

  def count(implicit r: RedisClient): Int = r send cmd.zcard(indexKey())

  def save(in: A)(implicit r: RedisClient): Future[Boolean] = {
    beforeSave(in)
    val result = (r !!! cmd.multiexec(cmd.set(key(id(in)), in) :: cmd.zadd(indexKey(), score(in), id(in)) :: indexes(in).map{idx => cmd.sadd(indexKey(Some(idx)), id(in))}) map (_.isDefined))
    afterSave(in)
    result
  }

  def find(id: String)(implicit r: RedisClient): Option[A] =
    r send cmd.get[A](key(id))

  def delete(in: A)(implicit r: RedisClient) =
    r send cmd.multiexec(cmd.del(List(key(id(in)))) :: cmd.zrem(indexKey(), id(in)) :: indexes(in).map{idx => cmd.srem(indexKey(Some(idx)), id(in))}) isDefined

  def indexes(in: A): List[String] = Nil

  def score(in: A): Double

  def beforeSave(in: A)(implicit r: RedisClient) {}

  def afterSave(in: A)(implicit r: RedisClient) {}
}
