package kr.ac.uc.albago.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import kr.ac.uc.albago.fragment.AppliedFragment;
import kr.ac.uc.albago.fragment.SubstituteFragment;
import kr.ac.uc.albago.fragment.CompletedFragment;
import kr.ac.uc.albago.fragment.FavoriteFragment;

public class MyActivityAdapter extends FragmentStateAdapter {
    public MyActivityAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return switch (position) {
            case 0 -> new AppliedFragment();      // 신청
            case 1 -> new SubstituteFragment();   // 대체
            case 2 -> new CompletedFragment();    // 완료
            case 3 -> new FavoriteFragment();     // 관심
            default -> new AppliedFragment();
        };
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}