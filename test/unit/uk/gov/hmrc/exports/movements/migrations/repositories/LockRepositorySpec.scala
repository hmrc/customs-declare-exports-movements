/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.exports.movements.migrations.repositories

import com.mongodb._
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.{DeleteResult, UpdateResult}
import com.mongodb.client.{FindIterable, MongoCollection, MongoDatabase}
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.{DateCodec, DocumentCodec, StringCodec}
import org.bson.conversions.Bson
import org.bson.{BsonDateTime, BsonDocument, Document}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, anyString, eq => meq}
import org.mockito.MockitoSugar.{mock, reset, verify, when}
import org.mongodb.scala.bson.BsonString
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import uk.gov.hmrc.exports.migrations.repositories.TestObjectsBuilder.buildMongoCursor
import uk.gov.hmrc.exports.movements.migrations.exceptions.LockPersistenceException
import uk.gov.hmrc.exports.movements.migrations.repositories.LockEntry._

import java.util.Date
import scala.jdk.javaapi.CollectionConverters.asJava

class LockRepositorySpec extends AnyWordSpec with BeforeAndAfterEach with Matchers {

  private val databaseName = "testDatabase"
  private val collectionName = "testCollection"
  private val mongoNamespace = new MongoNamespace(databaseName, collectionName)

  private val findIterable = mock[FindIterable[Document]]
  private val mongoCollection = mock[MongoCollection[Document]]
  private val mongoDatabase = mock[MongoDatabase]

  private val repo = {
    defineMocksBehaviourDefault()
    new LockRepository(collectionName, mongoDatabase)
  }

  private val lockKey = "lockKeyValue"
  private val status = LockStatus.LockHeld.name
  private val owner = "ownerValue"
  private val date = new Date()
  private val lockEntry = LockEntry(lockKey, status, owner, date)

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(findIterable, mongoCollection, mongoDatabase)
    defineMocksBehaviourDefault()
  }

  private def defineMocksBehaviourDefault(): Unit = {
    when(findIterable.iterator()).thenReturn(buildMongoCursor(Seq.empty))
    when(mongoCollection.getNamespace).thenReturn(mongoNamespace)
    when(mongoCollection.find(any[Bson])).thenReturn(findIterable)
    when(mongoCollection.deleteMany(any[Bson])).thenReturn(DeleteResult.unacknowledged())
    when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions])).thenReturn(UpdateResult.unacknowledged())
    when(mongoDatabase.getCollection(anyString())).thenReturn(mongoCollection)
  }

  override def afterEach(): Unit = {
    reset(findIterable, mongoCollection, mongoDatabase)
    super.afterEach()
  }

  private def getDeleteManyFilterQuery: Bson = {
    val captor: ArgumentCaptor[Bson] = ArgumentCaptor.forClass(classOf[Bson])
    verify(mongoCollection).deleteMany(captor.capture())
    captor.getValue
  }

  private def getUpdateManyFilterQuery: Bson = {
    val captor: ArgumentCaptor[Bson] = ArgumentCaptor.forClass(classOf[Bson])
    verify(mongoCollection).updateMany(captor.capture(), any[Bson], any())
    captor.getValue
  }

  private def getUpdateManyUpdateQuery: Bson = {
    val captor: ArgumentCaptor[Bson] = ArgumentCaptor.forClass(classOf[Bson])
    verify(mongoCollection).updateMany(any[Bson], captor.capture(), any())
    captor.getValue
  }

  private def getUpdateManyUpdateOptions: UpdateOptions = {
    val captor: ArgumentCaptor[UpdateOptions] = ArgumentCaptor.forClass(classOf[UpdateOptions])
    verify(mongoCollection).updateMany(any[Bson], any[Bson], captor.capture())
    captor.getValue
  }

  private def bsonToDocument(bson: Bson) =
    bson.toBsonDocument(classOf[Document], CodecRegistries.fromCodecs(new DocumentCodec(), new StringCodec(), new DateCodec()))

  "LockRepository on findByKey" should {

    "call MongoCollection" in {
      repo.findByKey("testKey")
      verify(mongoCollection).find(any[Bson])
    }

    "provide MongoCollection with correct query filter" in {
      repo.findByKey(lockKey)

      val expectedFilter = new Document(KeyField, lockKey)
      verify(mongoCollection).find(meq(expectedFilter))
    }

    "return None" when {
      "MongoCollection returned empty Iterable" in {
        val result = repo.findByKey("testKey")
        result mustBe None
      }
    }

    "return LockEntry built from Document returned by MongoCollection" in {
      val elementInDb =
        new Document(asJava(Map(KeyField -> lockKey, StatusField -> "statusValue", OwnerField -> "ownerValue", ExpiresAtField -> date)))
      when(findIterable.iterator()).thenReturn(buildMongoCursor(Seq(elementInDb)))

      val result = repo.findByKey(lockKey)

      result mustBe defined
      val expectedLockEntry = LockEntry(lockKey, "statusValue", "ownerValue", date)
      result.get mustBe expectedLockEntry
    }
  }

  "LockRepository on removeByKeyAndOwner" should {

    "call MongoCollection" in {
      repo.removeByKeyAndOwner("lockKey", "owner")

      verify(mongoCollection).deleteMany(any[Bson])
    }

    "provide MongoCollection with correct query filter" in {
      repo.removeByKeyAndOwner(lockKey, owner)

      val andArray = bsonToDocument(getDeleteManyFilterQuery).getArray("$and")
      andArray.get(0).asDocument.getString(KeyField).getValue mustBe lockKey
      andArray.get(1).asDocument.getString(OwnerField).getValue mustBe owner
    }
  }

  "LockRepository on insertUpdate" should {

    "call MongoCollection" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.insertUpdate(lockEntry)

      verify(mongoCollection).updateMany(any[Bson], any[Bson], any[UpdateOptions])
    }

    "provide MongoCollection with correct filter query" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.insertUpdate(lockEntry)

      val andArray = bsonToDocument(getUpdateManyFilterQuery).getArray("$and")
      andArray.get(0).asDocument.getString(KeyField).getValue mustBe lockEntry.key
      andArray.get(1).asDocument.getString(StatusField).getValue mustBe lockEntry.status

      val orArray = andArray.get(2).asDocument.getArray("$or")
      orArray.get(0).asDocument().getDocument(ExpiresAtField).getDateTime("$lt") mustBe a[BsonDateTime]
      orArray.get(1).asDocument().getString(OwnerField).getValue mustBe lockEntry.owner
    }

    "provide MongoCollection with correct update query" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.insertUpdate(lockEntry)

      val actualLockEntry = bsonToDocument(getUpdateManyUpdateQuery).getDocument("$set")
      actualLockEntry.getString(KeyField).getValue mustBe lockEntry.key
      actualLockEntry.getString(StatusField).getValue mustBe lockEntry.status
      actualLockEntry.getString(OwnerField).getValue mustBe lockEntry.owner
      actualLockEntry.getDateTime(ExpiresAtField) mustBe a[BsonDateTime]
    }

    "provide MongoCollection with correct UpdateOptions" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.insertUpdate(lockEntry)

      val actualUpdateOptions = getUpdateManyUpdateOptions
      actualUpdateOptions.isUpsert mustBe true
    }

    "throw LockPersistenceException" when {

      "MongoCollection throws DuplicateKeyException" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenThrow(new DuplicateKeyException(new BsonDocument(), new ServerAddress(), WriteConcernResult.unacknowledged()))

        intercept[LockPersistenceException](repo.insertUpdate(lockEntry)).getMessage mustBe "Lock is held"
      }

      "MongoCollection throws MongoWriteException with error category Duplicate Key" in {
        val duplicateKeyErrorCode = 11000
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenThrow(new MongoWriteException(new WriteError(duplicateKeyErrorCode, "", new BsonDocument()), new ServerAddress()))

        intercept[LockPersistenceException](repo.insertUpdate(lockEntry)).getMessage mustBe "Lock is held"
      }

      "MongoCollection returns UpdateResult with ModifiedCount == 0 and UpsertedId == null" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenReturn(UpdateResult.acknowledged(1, 0L, null))

        intercept[LockPersistenceException](repo.insertUpdate(lockEntry)).getMessage mustBe "Lock is held"
      }
    }

    "throw MongoWriteException" when {
      "MongoCollection throws MongoWriteException with error category other than Duplicate Key" in {
        val UncategorizedErrorCode = 12345
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenThrow(new MongoWriteException(new WriteError(UncategorizedErrorCode, "", new BsonDocument()), new ServerAddress()))

        intercept[MongoWriteException](repo.insertUpdate(lockEntry))
      }
    }

    "not throw LockPersistenceException" when {

      "MongoCollection returns UpdateResult with ModifiedCount == 0 and defined UpsertedId" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenReturn(UpdateResult.acknowledged(1, 0L, BsonString("UpsertedId")))

        noException mustBe thrownBy(repo.insertUpdate(lockEntry))
      }

      "MongoCollection returns UpdateResult with ModifiedCount other than 0 and UpsertedId == null" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenReturn(UpdateResult.acknowledged(1, 1L, null))

        noException mustBe thrownBy(repo.insertUpdate(lockEntry))
      }
    }
  }

  "LockRepository on updateIfSameOwner" should {

    "call MongoCollection" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.updateIfSameOwner(lockEntry)

      verify(mongoCollection).updateMany(any[Bson], any[Bson], any[UpdateOptions])
    }

    "provide MongoCollection with correct filter query" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.updateIfSameOwner(lockEntry)

      val andArray = bsonToDocument(getUpdateManyFilterQuery).getArray("$and")
      andArray.get(0).asDocument.getString(KeyField).getValue mustBe lockEntry.key
      andArray.get(1).asDocument.getString(StatusField).getValue mustBe lockEntry.status
      andArray.get(2).asDocument.getArray("$or").get(0).asDocument.getString(OwnerField).getValue mustBe lockEntry.owner
    }

    "provide MongoCollection with correct update query" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.updateIfSameOwner(lockEntry)

      val actualLockEntry = bsonToDocument(getUpdateManyUpdateQuery).getDocument("$set")
      actualLockEntry.getString(KeyField).getValue mustBe lockEntry.key
      actualLockEntry.getString(StatusField).getValue mustBe lockEntry.status
      actualLockEntry.getString(OwnerField).getValue mustBe lockEntry.owner
      actualLockEntry.getDateTime(ExpiresAtField) mustBe a[BsonDateTime]
    }

    "provide MongoCollection with correct UpdateOptions" in {
      when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
        .thenReturn(UpdateResult.acknowledged(1, 1L, BsonString("UpsertedId")))

      repo.updateIfSameOwner(lockEntry)

      val actualUpdateOptions = getUpdateManyUpdateOptions
      actualUpdateOptions.isUpsert mustBe false
    }

    "throw LockPersistenceException" when {

      "MongoCollection throws DuplicateKeyException" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenThrow(new DuplicateKeyException(new BsonDocument(), new ServerAddress(), WriteConcernResult.unacknowledged()))

        intercept[LockPersistenceException](repo.updateIfSameOwner(lockEntry)).getMessage mustBe "Lock is held"
      }

      "MongoCollection throws MongoWriteException with error category Duplicate Key" in {
        val duplicateKeyErrorCode = 11000
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenThrow(new MongoWriteException(new WriteError(duplicateKeyErrorCode, "", new BsonDocument()), new ServerAddress()))

        intercept[LockPersistenceException](repo.updateIfSameOwner(lockEntry)).getMessage mustBe "Lock is held"
      }

      "MongoCollection returns UpdateResult with ModifiedCount == 0 and UpsertedId == null" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenReturn(UpdateResult.acknowledged(1, 0L, null))

        intercept[LockPersistenceException](repo.updateIfSameOwner(lockEntry)).getMessage mustBe "Lock is held"
      }
    }

    "throw MongoWriteException" when {
      "MongoCollection throws MongoWriteException with error category other than Duplicate Key" in {
        val UncategorizedErrorCode = 12345
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenThrow(new MongoWriteException(new WriteError(UncategorizedErrorCode, "", new BsonDocument()), new ServerAddress()))

        intercept[MongoWriteException](repo.updateIfSameOwner(lockEntry))
      }
    }

    "not throw LockPersistenceException" when {

      "MongoCollection returns UpdateResult with ModifiedCount == 0 and defined UpsertedId" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenReturn(UpdateResult.acknowledged(1, 0L, BsonString("UpsertedId")))

        noException mustBe thrownBy(repo.updateIfSameOwner(lockEntry))
      }

      "MongoCollection returns UpdateResult with ModifiedCount other than 0 and UpsertedId == null" in {
        when(mongoCollection.updateMany(any[Bson], any[Bson], any[UpdateOptions]))
          .thenReturn(UpdateResult.acknowledged(1, 1L, null))

        noException mustBe thrownBy(repo.updateIfSameOwner(lockEntry))
      }
    }
  }
}
