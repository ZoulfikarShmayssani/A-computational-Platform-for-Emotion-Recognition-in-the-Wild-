package com.example.halac.keyloggers_notify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CheckListActivity extends AppCompatActivity implements Button.OnClickListener {
    private ListView listView;
    private Button SubmitData, Cancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_main);

        findViewsById();

        String[] elements = getResources().getStringArray(R.array.elements);
        CustomAdapter adapter = new CustomAdapter(this, elements);

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setAdapter(adapter);

        SubmitData.setOnClickListener(this);
        Cancel.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.SubmitData:
                List<String> selected = getSelectedItems();
                String logString = "Selected items: " + TextUtils.join(", ", selected);
                Toast.makeText(this, logString, Toast.LENGTH_SHORT).show();
                break;

            case R.id.cancel:
                finish();
                break;
        }
    }

    private void findViewsById() {
        listView = (ListView) findViewById(R.id.list);
        SubmitData = (Button) findViewById(R.id.SubmitData);
        Cancel=(Button)findViewById(R.id.cancel);

    }

    private List<String> getSelectedItems() {
        List<String> result = new ArrayList<>();
        SparseBooleanArray checkedItems = listView.getCheckedItemPositions();

        for (int i = 0; i < listView.getCount(); ++i) {
            if (checkedItems.valueAt(i)) {
                result.add((String) listView.getItemAtPosition(checkedItems.keyAt(i)));
            }
        }

        return result;
    }
}