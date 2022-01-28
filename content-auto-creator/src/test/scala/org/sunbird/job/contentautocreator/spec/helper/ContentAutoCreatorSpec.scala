package org.sunbird.job.contentautocreator.spec.helper

import com.typesafe.config.{Config, ConfigFactory}
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.FileCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.mockito.ArgumentMatchers.{any, anyMap, anyString, contains, endsWith}
import org.mockito.Mockito.when
import org.mockito.{ArgumentMatchers, Mockito}
import org.neo4j.driver.v1.StatementResult
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}
import org.scalatestplus.mockito.MockitoSugar
import org.sunbird.job.contentautocreator.domain.Event
import org.sunbird.job.contentautocreator.helpers.ContentAutoCreator
import org.sunbird.job.domain.`object`.{DefinitionCache, ObjectDefinition}
import org.sunbird.job.task.ContentAutoCreatorConfig
import org.sunbird.job.util._

import java.io.File

class ContentAutoCreatorSpec extends FlatSpec with Matchers with MockitoSugar {

	implicit val mockNeo4JUtil: Neo4JUtil = mock[Neo4JUtil](Mockito.withSettings().serializable())
	val config: Config = ConfigFactory.load("test.conf").withFallback(ConfigFactory.systemEnvironment())
	val jobConfig: ContentAutoCreatorConfig = new ContentAutoCreatorConfig(config)
	val defCache = new DefinitionCache()
	implicit val cloudUtil : CloudStorageUtil = new CloudStorageUtil(jobConfig)
	var mockHttpUtil: HttpUtil = mock[HttpUtil]

	def delay(time: Long): Unit = {
		try {
			Thread.sleep(time)
		} catch {
			case ex: Exception => print("")
		}
	}

	"process" should "not throw exception for dockEvent" in {
		val contentResponse = """{"responseCode":"OK","result":{"content":{"identifier":"do_21344892893869670417014","name":"Aparna","description":"about water","source":"https://drive.google.com/uc?export=download&id=1ZAW528VDqHNV6R3lTXfDOxhu9hyAXVl1","artifactUrl":"https://drive.google.com/uc?export=download&id=1ZAW528VDqHNV6R3lTXfDOxhu9hyAXVl1","appIcon":"https://drive.google.com/uc?export=download&id=1-tWar0Kl6DsuUpZ36f-3yF1_fDBhxzNj","creator":"Aparna","author":"Aparna", "versionKey":"1587624624051","audience":["Student"],"code":"1ee4c91a-61a4-2b5e-e3bd-a19c567242fc","mimeType":"application/pdf","primaryCategory":"eTextbook","lastPublishedBy":"9cb68c8f-7c23-476e-a5bf-11978f07e28b","createdBy":"9cb68c8f-7c23-476e-a5bf-11978f07e28b","programId":"a4597550-7119-11ec-902a-3b5d30502ba5","copyright":"2013","attributions":["Nadiya Anusha"],"keywords":["Drop"],"contentPolicyCheck":true,"channel":"01329314824202649627","framework":"ekstep_ncert_k-12","board":"CBSE","medium":["English"],"gradeLevel":["Class 1"],"subject":["English"],"boardIds":["ekstep_ncert_k-12_board_cbse"],"mediumIds":["ekstep_ncert_k-12_medium_english"],"gradeLevelIds":["ekstep_ncert_k-12_gradelevel_class1"],"subjectIds":["ekstep_ncert_k-12_subject_english"],"targetFWIds":["ekstep_ncert_k-12"],"license":"CC BY 4.0","processId":"761fd6b4-1478-4e0e-9c00-fe5aba11173c","objectType":"Content","status":"Draft"}}}"""
		val createResponse = """{"responseCode":"OK","result":{"identifier":"do_21344892893869670417014", "versionKey":"1587624624051"}}"""
		val uploadResponse = """{"responseCode":"OK","result":{"identifier":"do_21344892893869670417014", "artifactUrl": "https://sunbirddev.blob.core.windows.net/sunbird-content-dev/content/do_21344892893869670417014/artifact/cbse-copy-143.pdf"}}}"""
		val reviewResponse = """{"responseCode":"OK","result":{"node_id":"do_21344892893869670417014"}}"""
		val publishResponse = """{"responseCode":"OK","result":{"publishStatus":"Success"}}"""

		val dockEvent = "{\"eid\":\"BE_JOB_REQUEST\",\"ets\":1641714958337,\"mid\":\"LP.1641714958337.30e5f007-b878-4406-91d1-2a77a72911cb\",\"actor\":{\"id\":\"Auto Creator\",\"type\":\"System\"},\"context\":{\"pdata\":{\"id\":\"org.sunbird.platform\",\"ver\":\"1.0\",\"env\":\"staging\"},\"channel\":\"01329314824202649627\"},\"object\":{\"id\":\"do_21344892893869670417014\",\"ver\":null},\"edata\":{\"action\":\"auto-create\",\"originData\":{},\"iteration\":1,\"metadata\":{\"name\":\"Aparna\",\"description\":\"about water\",\"source\":\"https://drive.google.com/uc?export=download&id=1ZAW528VDqHNV6R3lTXfDOxhu9hyAXVl1\",\"artifactUrl\":\"https://drive.google.com/uc?export=download&id=1ZAW528VDqHNV6R3lTXfDOxhu9hyAXVl1\",\"appIcon\":\"https://drive.google.com/uc?export=download&id=1-tWar0Kl6DsuUpZ36f-3yF1_fDBhxzNj\",\"creator\":\"Aparna\",\"author\":\"Aparna\",\"audience\":[\"Student\"],\"code\":\"1ee4c91a-61a4-2b5e-e3bd-a19c567242fc\",\"mimeType\":\"application/pdf\",\"primaryCategory\":\"eTextbook\",\"lastPublishedBy\":\"9cb68c8f-7c23-476e-a5bf-11978f07e28b\",\"createdBy\":\"9cb68c8f-7c23-476e-a5bf-11978f07e28b\",\"programId\":\"a4597550-7119-11ec-902a-3b5d30502ba5\",\"copyright\":\"2013\",\"attributions\":[\"Nadiya Anusha\"],\"keywords\":[\"Drop\"],\"contentPolicyCheck\":true,\"channel\":\"01329314824202649627\",\"framework\":\"ekstep_ncert_k-12\",\"board\":\"CBSE\",\"medium\":[\"English\"],\"gradeLevel\":[\"Class 1\"],\"subject\":[\"English\"],\"boardIds\":[\"ekstep_ncert_k-12_board_cbse\"],\"mediumIds\":[\"ekstep_ncert_k-12_medium_english\"],\"gradeLevelIds\":[\"ekstep_ncert_k-12_gradelevel_class1\"],\"subjectIds\":[\"ekstep_ncert_k-12_subject_english\"],\"targetFWIds\":[\"ekstep_ncert_k-12\"],\"collectionId\":\"do_21344890175651020816870\",\"unitIdentifiers\":[\"do_21344890175781273616871\"],\"license\":\"CC BY 4.0\",\"processId\":\"761fd6b4-1478-4e0e-9c00-fe5aba11173c\",\"objectType\":\"Content\"},\"identifier\":\"do_21344892893869670417014\",\"collection\":[{\"identifier\":\"do_21344890175651020816870\",\"unitId\":\"do_21344890175781273616871\"}],\"objectType\":\"Content\",\"stage\":\"publish\"}}"
		val event = new Event(JSONUtil.deserialize[java.util.Map[String, Any]](dockEvent),0,1)
		when(mockHttpUtil.post(contains("/v3/search"), anyString, any())).thenReturn(HTTPResponse(200, ScalaJsonUtil.serialize(Map.empty[String, AnyRef])))
		when(mockHttpUtil.post(contains("/content/v4/create"), anyString, any())).thenReturn(HTTPResponse(200, createResponse))
		when(mockHttpUtil.get(anyString(), any())).thenReturn(HTTPResponse(200, contentResponse))
		when(mockHttpUtil.patch(contains("/content/v4/update"), anyString, any())).thenReturn(HTTPResponse(200, createResponse))
		when(mockHttpUtil.postFilePath(contains("/content/v4/upload"), anyString, anyString, any())).thenReturn(HTTPResponse(200, uploadResponse))
		when(mockHttpUtil.post(contains("/content/v4/review"), anyString, any())).thenReturn(HTTPResponse(200, reviewResponse))
		when(mockHttpUtil.post(contains("/content/v3/publish"), anyString, any())).thenReturn(HTTPResponse(200, publishResponse))
		new TestContentAutoCreator().process(jobConfig, event, mockHttpUtil, mockNeo4JUtil, cloudUtil)
	}

	"process" should "not throw exception for sunbirdEvent" in {
		val contentResponse = """{"responseCode":"OK","result":{"content":{"identifier":"do_2134462034258575361402","ownershipType":["createdBy"],"unitIdentifiers":["do_2134460300692275201128"],"copyright":"Test axis,2076","organisationId":"da0d83d6-0692-4d94-95ae-7499d5e0a5bd","keywords":["Nadiya"],"subject":["Hindi"],"targetMediumIds":["ekstep_ncert_k-12_medium_english"],"channel":"01329314824202649627","language":["English"],"source":"https://dockstaging.sunbirded.org/api/content/v1/read/do_2134462034258575361402","mimeType":"video/mp4","targetGradeLevelIds":["ekstep_ncert_k-12_gradelevel_class2"],"objectType":"Content","appIcon":"https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/rhinocerous.thumb.jpg","primaryCategory":"Teacher Resource","appId":"staging.dock.portal","contentEncoding":"identity","artifactUrl":"https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/mp4_219.mp4","contentType":"MarkingSchemeRubric","trackable":{"enabled":"No","autoBatch":"No"},"identifier":"do_2134462034258575361402","audience":["Student"],"subjectIds":["ekstep_ncert_k-12_subject_hindi"],"visibility":"Default","author":"classmate5","mediaType":"content","osId":"org.ekstep.quiz.app","languageCode":["en"],"lastPublishedBy":"1cf88ea3-083d-4fdf-84be-3628e63ce7f0","version":2,"se_subjects":["Hindi"],"license":"CC BY 4.0","prevState":"Review","size":13992641,"lastPublishedOn":"2022-01-05T12:05:03.920+0000","name":"content_262","topic":["मेरे बचपन के दिन"],"attributions":["kayal"],"targetBoardIds":["ekstep_ncert_k-12_board_cbse"],"status":"Live","topicsIds":["ekstep_ncert_k-12_topic_8696da1edbd3ce327d2a0822f75bb44c7e4fecf8"],"code":"837d2b38-dd5b-10ff-f210-c93bd19adcb3","interceptionPoints":{},"credentials":{"enabled":"No"},"prevStatus":"Draft","description":"MP4","posterImage":"https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/rhinocerous.jpg","idealScreenSize":"normal","createdOn":"2022-01-05T12:04:57.124+0000","targetSubjectIds":["ekstep_ncert_k-12_subject_hindi"],"processId":"be4d7bf6-364c-42e7-bc0d-adea66117458","contentDisposition":"inline","lastUpdatedOn":"2022-01-05T12:05:05.269+0000","collectionId":"do_2134460300682690561125","dialcodeRequired":"No","lastStatusChangedOn":"2022-01-05T12:05:05.269+0000","creator":"cbsestaging26","os":["All"],"se_FWIds":["ekstep_ncert_k-12"],"targetFWIds":["ekstep_ncert_k-12"],"pkgVersion":1,"versionKey":"1641384302775","idealScreenDensity":"hdpi","framework":"ekstep_ncert_k-12","lastSubmittedOn":"2022-01-05T12:05:02.768+0000","createdBy":"530b19ea-dc8d-4cc7-a4b5-0c0214c8113a","se_topics":["मेरे बचपन के दिन"],"compatibilityLevel":1,"programId":"8f514bc0-6de9-11ec-9e9f-9f0c75510617","createdFor":["01329314824202649627"],"status":"Draft"}}}"""
		val createResponse = """{"responseCode":"OK","result":{"identifier":"do_2134462034258575361402", "versionKey":"1587624624051"}}"""
		val uploadResponse = """{"responseCode":"OK","result":{"identifier":"do_2134462034258575361402", "artifactUrl": "https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/mp4_219.mp4"}}}"""
		val reviewResponse = """{"responseCode":"OK","result":{"node_id":"do_2134462034258575361402"}}"""
		val publishResponse = """{"responseCode":"OK","result":{"publishStatus":"Success"}}"""

		val httpUtil = new HttpUtil
		val downloadPath = "/tmp/content" + File.separator + "_temp_" + System.currentTimeMillis
		val appIconUrl = "https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/rhinocerous.thumb.jpg"
		val artifactUrl = "https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/mp4_219.mp4"

		val sunbirdEvent = "{\"eid\":\"BE_JOB_REQUEST\",\"ets\":1641391738147,\"mid\":\"LP.1641391738147.eb3b7a96-259f-4b46-b386-d0bec1873a57\",\"actor\":{\"id\":\"Auto Creator\",\"type\":\"System\"},\"context\":{\"pdata\":{\"id\":\"org.sunbird.platform\",\"ver\":\"1.0\",\"env\":\"staging\"},\"channel\":\"01329314824202649627\"},\"object\":{\"id\":\"do_2134462034258575361402\",\"ver\":\"1641384302775\"},\"edata\":{\"action\":\"auto-create\",\"originData\":{},\"iteration\":1,\"metadata\":{\"ownershipType\":[\"createdBy\"],\"unitIdentifiers\":[\"do_2134460300692275201128\"],\"copyright\":\"Test axis,2076\",\"organisationId\":\"da0d83d6-0692-4d94-95ae-7499d5e0a5bd\",\"keywords\":[\"Nadiya\"],\"subject\":[\"Hindi\"],\"targetMediumIds\":[\"ekstep_ncert_k-12_medium_english\"],\"channel\":\"01329314824202649627\",\"language\":[\"English\"],\"source\":\"https://dockstaging.sunbirded.org/api/content/v1/read/do_2134462034258575361402\",\"mimeType\":\"video/mp4\",\"targetGradeLevelIds\":[\"ekstep_ncert_k-12_gradelevel_class2\"],\"objectType\":\"Content\",\"appIcon\":\"https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/rhinocerous.thumb.jpg\",\"primaryCategory\":\"Teacher Resource\",\"appId\":\"staging.dock.portal\",\"contentEncoding\":\"identity\",\"artifactUrl\":\"https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/mp4_219.mp4\",\"contentType\":\"MarkingSchemeRubric\",\"trackable\":{\"enabled\":\"No\",\"autoBatch\":\"No\"},\"identifier\":\"do_2134462034258575361402\",\"audience\":[\"Student\"],\"subjectIds\":[\"ekstep_ncert_k-12_subject_hindi\"],\"visibility\":\"Default\",\"author\":\"classmate5\",\"mediaType\":\"content\",\"osId\":\"org.ekstep.quiz.app\",\"languageCode\":[\"en\"],\"lastPublishedBy\":\"1cf88ea3-083d-4fdf-84be-3628e63ce7f0\",\"version\":2,\"se_subjects\":[\"Hindi\"],\"license\":\"CC BY 4.0\",\"prevState\":\"Review\",\"size\":13992641,\"lastPublishedOn\":\"2022-01-05T12:05:03.920+0000\",\"name\":\"content_262\",\"topic\":[\"मेरे बचपन के दिन\"],\"attributions\":[\"kayal\"],\"targetBoardIds\":[\"ekstep_ncert_k-12_board_cbse\"],\"status\":\"Live\",\"topicsIds\":[\"ekstep_ncert_k-12_topic_8696da1edbd3ce327d2a0822f75bb44c7e4fecf8\"],\"code\":\"837d2b38-dd5b-10ff-f210-c93bd19adcb3\",\"interceptionPoints\":{},\"credentials\":{\"enabled\":\"No\"},\"prevStatus\":\"Draft\",\"description\":\"MP4\",\"posterImage\":\"https://stagingdock.blob.core.windows.net/sunbird-content-dock/content/do_2134462034258575361402/artifact/rhinocerous.jpg\",\"idealScreenSize\":\"normal\",\"createdOn\":\"2022-01-05T12:04:57.124+0000\",\"targetSubjectIds\":[\"ekstep_ncert_k-12_subject_hindi\"],\"processId\":\"be4d7bf6-364c-42e7-bc0d-adea66117458\",\"contentDisposition\":\"inline\",\"lastUpdatedOn\":\"2022-01-05T12:05:05.269+0000\",\"collectionId\":\"do_2134460300682690561125\",\"dialcodeRequired\":\"No\",\"lastStatusChangedOn\":\"2022-01-05T12:05:05.269+0000\",\"creator\":\"cbsestaging26\",\"os\":[\"All\"],\"se_FWIds\":[\"ekstep_ncert_k-12\"],\"targetFWIds\":[\"ekstep_ncert_k-12\"],\"pkgVersion\":1,\"versionKey\":\"1641384302775\",\"idealScreenDensity\":\"hdpi\",\"framework\":\"ekstep_ncert_k-12\",\"lastSubmittedOn\":\"2022-01-05T12:05:02.768+0000\",\"createdBy\":\"530b19ea-dc8d-4cc7-a4b5-0c0214c8113a\",\"se_topics\":[\"मेरे बचपन के दिन\"],\"compatibilityLevel\":1,\"programId\":\"8f514bc0-6de9-11ec-9e9f-9f0c75510617\",\"createdFor\":[\"01329314824202649627\"]},\"repository\":\"https://dockstaging.sunbirded.org/api/content/v1/read/do_2134462034258575361402\",\"collection\":[{\"identifier\":\"do_21344602761084928012178\",\"unitId\":\"do_21344602911420416012179\"}],\"objectType\":\"Content\",\"stage\":\"\"}}"
		val event = new Event(JSONUtil.deserialize[java.util.Map[String, Any]](sunbirdEvent),0,1)
		when(mockHttpUtil.post(contains("/v3/search"), anyString, any())).thenReturn(HTTPResponse(200, ScalaJsonUtil.serialize(Map.empty[String, AnyRef])))
		when(mockHttpUtil.post(contains("/content/v4/create"), anyString, any())).thenReturn(HTTPResponse(200, createResponse))
		when(mockHttpUtil.get(anyString(), any())).thenReturn(HTTPResponse(200, contentResponse))
		when(mockHttpUtil.patch(contains("/content/v4/update"), anyString, any())).thenReturn(HTTPResponse(200, createResponse))
		when(mockHttpUtil.postFilePath(contains("/content/v4/upload"), anyString, anyString, any())).thenReturn(HTTPResponse(200, uploadResponse))
		when(mockHttpUtil.post(contains("/content/v4/review"), anyString, any())).thenReturn(HTTPResponse(200, reviewResponse))
		when(mockHttpUtil.post(contains("/content/v3/publish"), anyString, any())).thenReturn(HTTPResponse(200, publishResponse))
		when(mockHttpUtil.downloadFile(contains(".jpg"),anyString())).thenReturn(httpUtil.downloadFile(appIconUrl, downloadPath))
		when(mockHttpUtil.downloadFile(endsWith("mp4"),anyString())).thenReturn(httpUtil.downloadFile(artifactUrl, downloadPath))
		new TestContentAutoCreator().process(jobConfig, event, mockHttpUtil, mockNeo4JUtil, cloudUtil)
	}



}

class TestContentAutoCreator extends ContentAutoCreator {}
