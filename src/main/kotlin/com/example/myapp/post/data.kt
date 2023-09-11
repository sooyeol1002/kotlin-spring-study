package com.example.myapp.post

// query-model, view-model
// domain-model(JPA entity)

data class PostResponse(
        val id : Long,
        val title : String,
        val content : String,
        val createdDate: String
)
// Java
// String str = null;

// kotlin
// val:str

// 기존의 java, String이 nullable
// {"title": "", "content": ""}
// {"title": ""} -> content null

// 필드가 not-nullable
data class PostCreateRequest(val title : String, val content: String)

// 포스트 생성 요청 데이트를 검증하는 메서드
fun PostCreateRequest.validate() : Boolean {
    return !(this.title.isEmpty() || this.content.isEmpty())
}
data class PostModifyRequest(val title : String?, val content: String?)