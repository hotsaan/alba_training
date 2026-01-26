package kr.ac.uc.albago.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kr.ac.uc.albago.R;
import kr.ac.uc.albago.adapter.AppliedJobAdapter;
import kr.ac.uc.albago.api.ApiService;
import kr.ac.uc.albago.api.RetrofitClient;
import kr.ac.uc.albago.common.JobDetailActivity;
import kr.ac.uc.albago.model.AppliedJob;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ✔ 완료된 지원 내역(COMPLETED)만 보여주는 Fragment
 * - 조회만 가능
 * - 취소 / 삭제 불가
 */
public class CompletedFragment extends BaseFragment
        implements AppliedJobAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private AppliedJobAdapter adapter;
    private final List<AppliedJob> jobList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_completed, container, false);

        recyclerView = view.findViewById(R.id.recycler_completed);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AppliedJobAdapter(
                requireContext(),
                jobList,
                this,   // 클릭만 허용
                null,   // apply 없음
                null,   // ❌ remove 없음 (중요)
                null    // accept 없음
        );

        recyclerView.setAdapter(adapter);
        loadCompletedApplications();

        return view;
    }

    /* ================= 공통 유틸 ================= */

    private String getRole() {
        SharedPreferences prefs =
                requireActivity().getSharedPreferences("APP_PREFS", requireActivity().MODE_PRIVATE);
        return prefs.getString("user_role", "user");
    }

    private String getToken() {
        return requireActivity()
                .getSharedPreferences("APP_PREFS", requireActivity().MODE_PRIVATE)
                .getString("ACCESS_TOKEN", null);
    }

    /* ================= 데이터 로드 ================= */

    private void loadCompletedApplications() {
        if (!isAdded()) return;

        String token = getToken();
        if (token == null) {
            safeToast("로그인이 필요합니다.");
            return;
        }

        ApiService apiService = RetrofitClient
                .getInstance(getRole())
                .getApi();

        apiService.getApplications().enqueue(new Callback<List<AppliedJob>>() {
            @Override
            public void onResponse(
                    Call<List<AppliedJob>> call,
                    Response<List<AppliedJob>> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    jobList.clear();

                    for (AppliedJob job : response.body()) {
                        if ("COMPLETED".equals(job.getStatus())) {
                            jobList.add(job);
                        }
                    }

                    adapter.notifyDataSetChanged();
                } else {
                    safeToast("완료 내역 로드 실패");
                }
            }

            @Override
            public void onFailure(Call<List<AppliedJob>> call, Throwable t) {
                safeToast("네트워크 오류");
            }
        });
    }

    /* ================= 클릭 ================= */

    @Override
    public void onClick(int position) {
        AppliedJob job = jobList.get(position);

        Intent intent = new Intent(requireContext(), JobDetailActivity.class);
        intent.putExtra("jobId", job.getJobPostId());
        startActivity(intent);
    }
}
