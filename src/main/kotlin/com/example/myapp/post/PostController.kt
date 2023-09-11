package com.example.myapp.post

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
@RequestMapping("posts")
class PostController {

    // exposed selectAll -> List<ResultRow>
    // ResultRow는 transaction {} 구문 밖에서 접근 불가능함
    // transaction 구분 외부로 보낼 때는 별도의 객체로 변환해서 내보낸다.
    // 결과값

    @GetMapping
    fun fetch() = transaction {
        Posts.selectAll().map { r -> PostResponse(
                r[Posts.id], r[Posts.title], r[Posts.content],
                r[Posts.createdDate].toString())
        }
    }

    // PostCreateRequest
    // title: String, content: String -> 둘 다 null 이 불가능
    // null 체크를 할 필요 없음
    @PostMapping
    fun create(@RequestBody request: PostCreateRequest) :
            ResponseEntity<Map<String, Any?>> {
        println("${request.title}, ${request.content}")
        // 자바
        // Map<String, Object>
        // Object: nullable, int/long primitive 타입은 안 됨, Integer, Long

        // 코틀린
        // Map<String, Any?>
        // {"key" to null} -> Map<String, Student?>
        // {"key" to student} -> Map<String, Student>
        // {"key" to "str"} -> Map<String, String>
        // {"key" to 0L} -> Map<String, Long>
        // Java: Object, class들의 최상위 클래스

        if (!request.validate()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "title and content fields are required"))
        }

        if (request.title.isEmpty() || request.content.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("error" to "title and content fields are required"))
        }

        // Pair(first, second)
        // Student(name, age, grade) -> componentN() 메서드로 필드 순서를 지정했을때만
        // 코틀린의 구조분해는 객체의 필드 순서가 정의되어 있을 때만 사용가능
        val (result, response) = transaction {
            // insert 구문
            // List<ResultRow>?

            // ?: -> 앞의 식의 결과가 null이 아니면 앞의 식을 실행, null이면 뒤의 것을 실행
            val result = Posts.insert {
                // 매개변수 1개의 lambda함수의 매개변수를 it으로 단축표기
                // 함수식
                it[title] = request.title
                it[content] = request.content
                it[createdDate] = LocalDateTime.now()
            }.resultedValues
                ?:
                // ex) Pair(결과타입, 결과객체)
                // Pairs(first, second)
                return@transaction Pair(false, null)

            // List<ResultRow>
            val record = result.first()

            // ResultRow -> PostResponse
            return@transaction Pair(
                true, PostResponse(
                    record[Posts.id],
                    record[Posts.title],
                    record[Posts.content],
                    record[Posts.createdDate].toString(),
                )
            )
        }
        // 정확히 insert 됐을 때
        if (result) {
            return ResponseEntity
                .status(HttpStatus.CREATED).body(mapOf("data" to null))
        }
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(mapOf("data" to response, "error" to "conflict"))
    }
    @DeleteMapping("/{id}")
    fun remove(@PathVariable id : Long) : ResponseEntity<Any> {
        // 해당 id의 레코드 있는지 확인
        // 조회결과를 쓰지 않고 있는지 없는지만 확인

        // exposed
        // Posts.select { Posts.id.eq(id) } .firstOrNull()
        // Posts.select { where = Posts.id eq id } .firstOrNull()
        // 반환값: ResultRow?

        // SQL
        // select * from post where id = :id -> ResultRow?
        // [select * from] post where id = :id -> ResultRow?
        // post id = :id
        // Posts.slice(컬럼선택).select(조건문).limit(..).groupBy(..).orderBy(..)

        transaction { Posts.select(Posts.id eq id).firstOrNull() }
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        // delete
        transaction {
            Posts.deleteWhere { Posts.id eq id }
        }
        // 200 OK
            return ResponseEntity.ok().build()
    }

    @PutMapping("/{id}")
    fun modify(@PathVariable id : Long, @RequestBody request: PostModifyRequest): ResponseEntity<Any> {
        // 둘 다 null 이거나 빈값이면 400 : Bad request
        if(request.title.isNullOrEmpty() && request.content.isNullOrEmpty()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(mapOf("message" to "title or content are required"))
        }

        // id에 해당 레코드가 없으면 404
        transaction { Posts.select( Posts.id eq id ).firstOrNull() }
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        transaction {
            Posts.update(where = { Posts.id eq id }) {
                // title이 null 또는 "" 아니면
                // 값이 존재하면 수정
                if (!request.title.isNullOrEmpty()) {
                    it[title] = request.title
                }
                if (!request.content.isNullOrEmpty()) {
                    it[content] = request.content
                }
            }
        }
        return ResponseEntity.ok().build();
    }
}