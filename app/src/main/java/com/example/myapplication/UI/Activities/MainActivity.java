package com.example.myapplication.UI.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.JetPack.LiveData.GetNumberLiveData;
import com.example.myapplication.JetPack.Paging.Adapter.RecyclerViewAdapter;
import com.example.myapplication.JetPack.Paging.Model.StackApiResponse;
import com.example.myapplication.JetPack.Paging.ViewModel.ItemViewModel;
import com.example.myapplication.JetPack.Room.Student;
import com.example.myapplication.JetPack.Room.StudentInitializer;
import com.example.myapplication.JetPack.ViewModel.GetNumberViewModel;
import com.example.myapplication.LazyLoad.Adapter.RVAdapter;
import com.example.myapplication.LazyLoad.Inteface.ILoadMore;
import com.example.myapplication.LazyLoad.Model.Item;
import com.example.myapplication.Models.ItemResult;
import com.example.myapplication.R;
import com.example.myapplication.Retrofit.RetrofitHelper;
import com.example.myapplication.RxJava.RxJava2.RxSample2;
import com.example.myapplication.RxJava.RxJava3.RxSample3;
import com.example.myapplication.RxJava.RxJavaWithRetrofit.Model.Comment;
import com.example.myapplication.RxJava.RxJavaWithRetrofit.Model.Post;
import com.example.myapplication.RxJava.RxJavaWithRetrofit.Retrofit.RequestApi;
import com.example.myapplication.RxJava.RxJavaWithRetrofit.Retrofit.ServiceGenerator;
import com.example.myapplication.Utils.DownloadFile.DownloadFileAsyncTask.DownloadTask;
import com.example.myapplication.Utils.DownloadFile.DownloadFileService.DownloadFileService;
import com.example.myapplication.Utils.DownloadFile.DownloadFileService.DownloadReceiver;
import com.example.myapplication.Utils.Persmission.RequestPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {

    //for downloading
    private ProgressDialog progressDialog;
    private Button button;
    //for recyclerview
    private List<Item> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.btnNext);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //startActivity(new Intent(MainActivity.this, ListActivity.class));
                rxSample();
            }
        });

    }

    //******************** RXJava Sample and
    private void rxSample() {
        /*RxSample rxSample = new RxSample(this);
        rxSample.observable1("hello world");
        rxSample.subscriber1();*/

        /*RxSample2 rxSample2 = new RxSample2();
        rxSample2.getListRxWithoutFreezingUi();*/

        RxSample3 rxSample3 = new RxSample3(MainActivity.this);
        rxSample3.addItemWithRx(new String[5226321]);
    }

    //******************** EndLess RecyclerView (LazyLoad)
    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        RVAdapter adapter = new RVAdapter(recyclerView, this, createRandomData());
        recyclerView.setAdapter(adapter);

        adapter.setiLoadMore(new ILoadMore() {
            @Override
            public void onLoadMore() {
                if (list.size() <= 100) {
                    list.add(null);
                    adapter.notifyDataSetChanged();
                    new Handler().postDelayed(() -> {
                        list.remove(list.size() - 1);
                        adapter.notifyDataSetChanged();

                        //Random more data
                        int index = list.size();
                        int end = index + 10;
                        for (int i = index; i < end; i++) {
                            String name = UUID.randomUUID().toString();
                            Item item = new Item(name, name.length());
                            list.add(i, item);
                        }
                        adapter.notifyDataSetChanged();
                        adapter.setLoaded();

                    }, 1000);
                } else {
                    Toast.makeText(MainActivity.this, "Load Completed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private List<Item> createRandomData() {
        for (int i = 0; i < 10; i++) {
            String name = UUID.randomUUID().toString();
            Item item = new Item(name, name.length());
            list.add(i, item);
        }
        return list;
    }
    //******************** /EndLess RecyclerView (LazyLoad)

    //******************** Jet Pack
    private void setUpRecyclerViewPaging() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        ItemViewModel itemViewModel = ViewModelProviders.of(this).get(ItemViewModel.class);

        final RecyclerViewAdapter adapter = new RecyclerViewAdapter(this);

        itemViewModel.itemPagedList.observe(this, new Observer<PagedList<StackApiResponse.Item>>() {
            @Override
            public void onChanged(PagedList<StackApiResponse.Item> items) {
                adapter.submitList(items);
            }
        });

        recyclerView.setAdapter(adapter);

    }

    private void getNumberLiveData() {
        GetNumberLiveData getNumberLiveData = ViewModelProviders.of(this).get(GetNumberLiveData.class);
        LiveData<String> getNumber = getNumberLiveData.getNumber();

        getNumber.observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setNumberLiveData() {
        GetNumberLiveData getNumberLiveData = ViewModelProviders.of(this).get(GetNumberLiveData.class);
        getNumberLiveData.setNumber();
    }

    private void getNumber() {
        GetNumberViewModel getNumberViewModel = ViewModelProviders.of(this).get(GetNumberViewModel.class);
        Toast.makeText(this, getNumberViewModel.getNumber(), Toast.LENGTH_SHORT).show();
    }

    private void insertData(String name, String lastName, String studentId) {
        StudentInitializer studentInitializer = ViewModelProviders.of(this).get(StudentInitializer.class);
        studentInitializer.insertStudent(new Student(name, lastName, studentId));
    }

    private void getStudentList() {
        StudentInitializer studentInitializer = ViewModelProviders.of(this).get(StudentInitializer.class);
        List<Student> list = null;
        try {
            list = studentInitializer.getStudentList();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                Toast.makeText(this, list.get(i).getName(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getStudentListLiveData() {
        StudentInitializer studentInitializer = ViewModelProviders.of(MainActivity.this).get(StudentInitializer.class);
        studentInitializer.getStudentListLiveData().observe(this, new Observer<List<Student>>() {
            @Override
            public void onChanged(List<Student> students) {
                for (int i = 0; i < students.size(); i++) {
                    Toast.makeText(MainActivity.this, students.get(i).getId() + " | " + students.get(i).getId() + " | " + students.get(i).getName() + " | " + students.get(i).getStudentId(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getStudentById(int id) {
        StudentInitializer studentInitializer = ViewModelProviders.of(this).get(StudentInitializer.class);
        String name = null;
        try {
            name = studentInitializer.getStudentById(1).getName();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
    }

    private void getStudentByStudentId(String id) {
        StudentInitializer studentInitializer = ViewModelProviders.of(this).get(StudentInitializer.class);
        int pid = 0;
        String name = null;
        try {
            name = studentInitializer.getStudentByStudentId(id).getName();
            pid = studentInitializer.getStudentByStudentId(id).getId();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, pid + name, Toast.LENGTH_SHORT).show();
    }

    private void updateStudent(int id, String name) {
        StudentInitializer studentInitializer = ViewModelProviders.of(this).get(StudentInitializer.class);

        Student student = new Student(name, "amir", "256");
        student.setId(id);

        studentInitializer.updateStudentName(student);
    }

    private void deleteStudent(int id) {
        StudentInitializer studentInitializer = ViewModelProviders.of(this).get(StudentInitializer.class);
        try {
            studentInitializer.deleteStudent(studentInitializer.getStudentById(id));
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    //******************** /Jet Pack

    //******************** DownloadFile
    private void downloadFileWithService(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        Intent intent = new Intent(this, DownloadFileService.class);
        intent.putExtra("url", url);
        intent.putExtra("fileName", fileName);
        intent.putExtra("receiver", new DownloadReceiver(this, new Handler()));

        startService(intent);
    }

    private void downloadFileWithAsyncTask(String url) {
        String fileName = url.substring(url.lastIndexOf('/') + 1);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(fileName);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(true);

        DownloadTask downloadTask = new DownloadTask(MainActivity.this, fileName, progressDialog);
        downloadTask.execute(url);

        progressDialog.setOnCancelListener(dialogInterface -> {
            downloadTask.cancel(true);
        });

    }
    //******************** /DownloadFile

    //******************** Retrofit
    private void sendRequestToServer() {
        RetrofitHelper helper = new RetrofitHelper(this, MainActivity.this::onCallBackComplete);
        helper.getData("getPosts.php");
    }

    private void onCallBackComplete(Object response) {
        List<ItemResult> list = (List<ItemResult>) response;
    }
    //******************** /Retrofit

    //******************** RequestPermission
    private void checkPermission() {
        new RequestPermission(this).checkPermission(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO}, 1);
        //on Request Permission Result is in BaseActivity
    }
    //******************** /RequestPermission
}
