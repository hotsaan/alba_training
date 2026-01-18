package kr.ac.uc.albago.api;

import java.util.List;
import java.util.Map;

import kr.ac.uc.albago.model.AppliedJob;
import kr.ac.uc.albago.model.Company;
import kr.ac.uc.albago.model.CompanyResponse;
import kr.ac.uc.albago.model.JobPostRequest;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    /**
     * 공고에 지원 요청
     * @param token       JWT 인증 토큰 (Bearer 포함)
     * @param jobPostId   지원할 공고의 ID
     * @return            서버로부터의 응답 (성공 시 HTTP 200 or 201, 실패 시 오류 코드)
     */
    @POST("/api/applications/{jobPostId}")
    Call<Void> applyToJob(
            @Header("Authorization") String token,
            @Path("jobPostId") long jobPostId
    );

    //  회사 정보 관련
    @GET("/api/employer/company/{companyId}")
    Call<CompanyResponse> getCompanyById(
            @Header("Authorization") String token,
            @Path("companyId") String companyId
    );

    //  회사 ID 중복 체크
    @GET("/api/check-company-id-duplicate")
    Call<Map<String, Boolean>> checkCompanyIdDuplicate(@Query("companyId") String companyId);

    //  사업자 정보 조회
    @GET("/api/employer/info")
    Call<EmployerInfo> getEmployerInfo(@Header("Authorization") String token);

    //  회사 등록
    @POST("/api/employer/company")
    Call<Map<String, String>> registerCompany(@Body Company company);

    //  사업자 공고 목록 조회
    @GET("/api/employer/jobposts")
    Call<List<JobPostRequest>> getEmployerJobPosts();

    // 사업자 공고 등록
    @POST("/api/employer/jobposts")
    Call<Map<String, String>> createJobPost(
            @Body JobPostRequest jobPost
    );

    //  공고 (공용: 지도 / 상세보기 / 수정 / 삭제 등)
    @GET("/api/jobposts")
    Call<List<JobPostRequest>> getJobPosts(@Query("sort") String sort);

    //  단일 공고 상세 조회
    @GET("/api/jobposts/{id}")
    Call<JobPostRequest> getJobPost(@Path("id") long id);

    //  공고 수정
    @PUT("/api/jobposts/{id}")
    Call<Void> updateJobPost(
            @Path("id") long id,
            @Body JobPostRequest post
    );

    //  공고 삭제 (🔧 수정됨: 토큰 누락되어 있었음 → 추가함)
    @DELETE("/api/jobposts/{id}")
    Call<Void> deleteJobPost(
            @Path("id") long id
    );
    //  회원가입 (최종 완료)
    @POST("/api/register")
    Call<RegisterResponse> register(@Body RegisterRequest req);

    // 로그인
    @POST("/api/login")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    /**
     * Google ID 토큰을 사용하여 로그인 또는 회원가입
     * @param body "idToken"을 키로 포함하는 Map
     * @return 로그인 성공 시 토큰과 사용자 정보를 포함하는 LoginResponse
     */
    @POST("/api/google-login")
    Call<LoginResponse> googleLogin(@Body Map<String, String> body);

    //  토큰 리프레시
    @POST("/api/refresh")
    Call<RefreshResponse> refresh(@Body Map<String, String> body);

    //  아이디 중복 체크
    @POST("/api/check-id")
    Call<Map<String, Boolean>> checkIdDuplicate(@Body Map<String, String> request);

    //  사용자 기본 정보 조회
    @GET("/api/userinfo")
    Call<UserInfo> getUserInfo(@Header("Authorization") String token);

    //  사용자 정보 수정
    @PUT("/api/userinfo")
    Call<Void> updateUserProfile(
            @Header("Authorization") String token,
            @Body UserInfoUpdateRequest updateRequest
    );

    // 이미지 업로드
    @Multipart
    @POST("/api/user/profile-image")
    Call<ResponseBody> uploadProfileImage(
            @Header("Authorization") String token,
            @Part MultipartBody.Part image
    );

    //  구직자 지원 내역 조회
    @GET("/api/jobseeker/job-applications")
    Call<List<AppliedJob>> getApplications(@Header("Authorization") String token);


    @POST("/api/employer/applications/{id}/accept")
    Call<Void> acceptApplication(
            @Header("Authorization") String token,
            @Path("id") Long applicationId
    );

    //  구직자 지원 취소
    @DELETE("/api/jobseeker/job-applications/{id}")
    Call<Void> cancelApplication(
            @Header("Authorization") String token,
            @Path("id") long id
    );

    //  즐겨찾기 삭제
    @DELETE("/api/jobseeker/favorites/{id}")
    Call<Void> removeFavorite(
            @Header("Authorization") String token,
            @Path("id") long id
    );

    //  대타 공고 조회
    @GET("/api/jobseeker/substitutes")
    Call<List<AppliedJob>> getSubstituteJobs(@Header("Authorization") String token);

    //  대타 지원하기
    @POST("/api/jobseeker/substitutes/{id}/apply")
    Call<Void> applySubstitute(
            @Header("Authorization") String token,
            @Path("id") long id
    );

    //  내 요청 목록 조회
    @GET("/api/jobseeker/my-requests")
    Call<List<AppliedJob>> getMyRequests(@Header("Authorization") String token);

    // 내 요청 취소
    @DELETE("/api/jobseeker/my-requests/{id}")
    Call<Void> cancelMyRequest(
            @Header("Authorization") String token,
            @Path("id") long id
    );

    @GET("/api/employer/my-companies")
    Call<List<CompanyResponse>> getMyCompanies();


    @GET("/api/employer/company/{companyId}/posts")
    Call<List<JobPostRequest>> getJobPostsByCompany(@Path("companyId") String companyId);

    /**
     * 사장님이 받은 전체 지원자 목록 조회
     */
    @GET("/api/employer/applicants")
    Call<List<AppliedJob>> getApplicants(
            @Header("Authorization") String token
    );

    @POST("/api/employer/jobposts/{id}/refresh")
    Call<Void> refreshJobPost(@Path("id") long jobId);
}
