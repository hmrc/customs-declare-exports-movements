package uk.gov.hmrc.exports.movements.migrations.changelogs.ileQuerySubmissions

import com.mongodb.client.{MongoClients, MongoDatabase}
import org.bson.Document
import stubs.TestMongoDB
import stubs.TestMongoDB.mongoConfiguration
import uk.gov.hmrc.exports.movements.migrations.changelogs.ChangeLogsBaseISpec
import uk.gov.hmrc.exports.movements.migrations.changelogs.ileQuerySubmissions.ConvertIleQuerySubmissionTimestampToDateTypeISpec._

class ConvertIleQuerySubmissionTimestampToDateTypeISpec extends ChangeLogsBaseISpec {

  private val MongoURI = mongoConfiguration.get[String]("mongodb.uri")
  private val DatabaseName = TestMongoDB.DatabaseName
  private val CollectionName = "ileQuerySubmissions"

  private val mongoDatabase: MongoDatabase =
    MongoClients.create(MongoURI.replace("sslEnabled", "ssl")).getDatabase(DatabaseName)

  private val changeLog = new ConvertIleQuerySubmissionTimestampToDateType

  override def beforeEach(): Unit = {
    super.beforeEach()
    mongoDatabase.getCollection(CollectionName).drop()
  }

  override def afterEach(): Unit = {
    mongoDatabase.getCollection(CollectionName).drop()
    super.afterEach()
  }

  def runTest(inputDataJson: String, expectedDataJson: String)(test: MongoDatabase => Unit): Unit = {
    mongoDatabase.getCollection(CollectionName).insertOne(Document.parse(inputDataJson))

    test(mongoDatabase)

    val result: Document = mongoDatabase.getCollection(CollectionName).find.first
    val expectedResult: String = expectedDataJson

    compareJson(result.toJson, expectedResult)
  }

  "ConvertIleQuerySubmissionTimestampToDateType" should {

    "correctly migrate data" when {

      "migrating an ileQuerySubmission" in {
        runTest(TestDataBeforeChange.submission, TestDataAfterChange.submission)(changeLog.migrationFunction)
      }
    }

    "not change data already migrated" when {

      "migrating an ileQuerySubmission" in {
        runTest(TestDataAfterChange.submission, TestDataAfterChange.submission)(changeLog.migrationFunction)
      }
    }
  }
}

object ConvertIleQuerySubmissionTimestampToDateTypeISpec {

  object TestDataBeforeChange {

    val submission: String =
      """{
        |"_id": {    "$oid": "62df9a4afb1895532bdf7ac0"  },
        |"uuid": "957489cd-cb3d-4a77-a43d-0a2c669b6fde",
        |"eori": "GB7172755098487",
        |"conversationId": "06d35215-77cf-40f0-b306-3680f5842373",
        |"ucrBlock": {    "ucr": "9GB123999746000-DUCR12345",    "ucrType": "D"  },
        |"requestTimestamp": "2020-12-04T13:02:43.694Z"}
        |""".stripMargin
  }

  object TestDataAfterChange {

    val submission: String =
      """{
        |"_id": {    "$oid": "62df9a4afb1895532bdf7ac0"  },
        |"uuid": "957489cd-cb3d-4a77-a43d-0a2c669b6fde",
        |"eori": "GB7172755098487",
        |"conversationId": "06d35215-77cf-40f0-b306-3680f5842373",
        |"ucrBlock": {    "ucr": "9GB123999746000-DUCR12345",    "ucrType": "D"  },
        |"requestTimestamp": {"$date":"2020-12-04T13:02:43.694Z"}}
        |""".stripMargin
  }
}
