package uk.gov.hmrc.exports.movements.migrations.changelogs.movementSubmissions

import com.mongodb.client.{MongoClients, MongoDatabase}
import org.bson.Document
import stubs.TestMongoDB
import stubs.TestMongoDB.mongoConfiguration
import uk.gov.hmrc.exports.movements.migrations.changelogs.ChangeLogsBaseISpec
import uk.gov.hmrc.exports.movements.migrations.changelogs.movementSubmissions.ConvertSubmissionTimestampToDateTypeISpec._

class ConvertSubmissionTimestampToDateTypeISpec extends ChangeLogsBaseISpec {

  private val MongoURI = mongoConfiguration.get[String]("mongodb.uri")
  private val DatabaseName = TestMongoDB.DatabaseName
  private val CollectionName = "movementSubmissions"

  private val mongoDatabase: MongoDatabase =
    MongoClients.create(MongoURI.replace("sslEnabled", "ssl")).getDatabase(DatabaseName)

  private val changeLog = new ConvertSubmissionTimestampToDateType

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

  "ConvertSubmissionTimestampToDateType" should {

    "correctly migrate data" when {

      "migrating a movements submission" in {
        runTest(TestDataBeforeChange.submission, TestDataAfterChange.submission)(changeLog.migrationFunction)
      }
    }

    "not change data already migrated" when {

      "migrating a movements submission" in {
        runTest(TestDataAfterChange.submission, TestDataAfterChange.submission)(changeLog.migrationFunction)
      }
    }
  }
}

object ConvertSubmissionTimestampToDateTypeISpec {

  object TestDataBeforeChange {

    val submission: String =
      """{
         |"_id": {    "$oid": "62de8df4a49db479b7076537"  },
         |"uuid": "fef9c8ca-14ba-4e23-8a01-2c1226fb1d42",
         |"eori": "GB7172755013782",
         |"conversationId": "9661b2a2-1e1b-47ba-bd57-683310813a2f",
         |"ucrBlocks": [    {      "ucr": "9GB123999746000-DUCR12345",      "ucrType": "D"    }  ],
         |"actionType": "Arrival",
         |"requestTimestamp": "2020-12-04T13:02:43.694Z"}
      """.stripMargin
  }

  object TestDataAfterChange {

    val submission: String =
      """{
        |"_id": {    "$oid": "62de8df4a49db479b7076537"  },
        |"uuid": "fef9c8ca-14ba-4e23-8a01-2c1226fb1d42",
        |"eori": "GB7172755013782",
        |"conversationId": "9661b2a2-1e1b-47ba-bd57-683310813a2f",
        |"ucrBlocks": [    {      "ucr": "9GB123999746000-DUCR12345",      "ucrType": "D"    }  ],
        |"actionType": "Arrival",
        |"requestTimestamp": {"$date":"2020-12-04T13:02:43.694Z"}}
      """.stripMargin
  }
}
