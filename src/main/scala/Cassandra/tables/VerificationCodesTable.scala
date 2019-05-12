package Cassandra.tables

import com.outworkers.phantom.dsl._
import domain.models.User
import org.mindrot.jbcrypt.BCrypt

import scala.concurrent.Future

abstract class VerificationCodesTable extends Table[VerificationCodesTable, (String, String)] {

  override def tableName: String = "codes"

  object phoneNumber extends StringColumn with PartitionKey
  object code extends StringColumn

  def storeSmsCode(phoneNumber: String, code: String): Future[String] = {
    insert
      .value(_.phoneNumber, phoneNumber)
      .value(_.code, code)
      .future()
      .map { _ => code }
  }

  def getSmsCode(phoneNumber: String): Future[Option[(String, String)]] = {
    select
      .where(_.phoneNumber eqs phoneNumber)
      .one()
  }
}
