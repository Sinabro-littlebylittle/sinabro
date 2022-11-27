package com.project.sinabro;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsActivity extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private ImageButton back_ibtn;                      // 뒤로가기 버튼
    private ImageButton Notifications_imgbtn;           // Notification 버튼
    private ImageButton Userpolicy_imgbtn;              // use policy 버튼
    private ImageButton Language_imgbtn;                // Language 버튼
    private ImageButton Delete_histoty_imgbtn;          // Delete histoty 버튼
    private ImageButton TextSize_imgbtn;                // TextSize 버튼


    public SettingsActivity() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingsActivity.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsActivity newInstance(String param1, String param2) {
        SettingsActivity fragment = new SettingsActivity();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings_activity, container, false);

        // 뒤로가기 버튼 기능
        back_ibtn = (ImageButton) view.findViewById(R.id.back_ibtn);
        back_ibtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        // Notification 페이지로 넘어가기
        Notifications_imgbtn =(ImageButton) view.findViewById(R.id.Notifications_imgbtn);
        Notifications_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Notification", "onClick: 테스트 중입니다.");
                Intent intent = new Intent(getActivity(),NotificationActivity.class);
                startActivity(intent);

            }
        });
        // use policy 페이지로 넘어가기
        Userpolicy_imgbtn = (ImageButton) view.findViewById(R.id.Userpolicy_imgbtn);
        Userpolicy_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("usepolicy", "onClick: 테스트 중입니다.");
                Intent intent = new Intent(getActivity(),UsePolicyActivity.class);
                startActivity(intent);

            }
        });

        // 텍스트 크기 설정
        /**
         * 추후 기능 논의후 변경
         */
        TextSize_imgbtn = (ImageButton) view.findViewById(R.id.TextSize_imgbtn);
        TextSize_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("텍스트 크기변경", "onClick: 테스트 중입니다.");
            }
        });
        //언어 설정
        Language_imgbtn =(ImageButton) view.findViewById(R.id.Language_imgbtn);
        Language_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("언어설정", "onClick: 테스트 중입니다.");
                // 설정창 효면에 띄우기
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View v = LayoutInflater.from(getActivity()).inflate(R.layout.settings_language_change,null,false);
                builder.setView(v);

                final Button button_Cancel = (Button) v.findViewById(R.id.button_Cancel);
                button_Cancel.setText("취소");

                // 알림창에서 버튼이 눌려졌을 때 -> 추후에 설정 변경까지 구현
                final AlertDialog dialog = builder.create();
                button_Cancel.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {                                   // 변경
                        Toast.makeText(getContext(),"Clicked Cancel",Toast.LENGTH_LONG).show();
                        dialog.cancel();

                    }
                });
                dialog.show();
            }

        });

        // 사용기록 삭제
        /**
         * 아직 기능 미 구현
         */
        Delete_histoty_imgbtn= (ImageButton) view.findViewById(R.id.Delete_histoty_imgbtn);
        Delete_histoty_imgbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("사용기록 삭제", "onClick: 테스트 중입니다.");
            }
        });





        return view;
    }
}