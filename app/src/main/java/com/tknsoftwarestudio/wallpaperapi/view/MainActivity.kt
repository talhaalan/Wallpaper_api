package com.tknsoftwarestudio.wallpaperapi.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tknsoftwarestudio.wallpaperapi.R
import com.tknsoftwarestudio.wallpaperapi.adapter.ImageAdapter
import com.tknsoftwarestudio.wallpaperapi.api.ApiUtils
import com.tknsoftwarestudio.wallpaperapi.databinding.ActivityMainBinding
import com.tknsoftwarestudio.wallpaperapi.models.Photo
import com.tknsoftwarestudio.wallpaperapi.models.Search
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var recyclerView: RecyclerView
    lateinit var list : ArrayList<Photo>
    lateinit var manager : GridLayoutManager
    lateinit var adapter : ImageAdapter

    private var page = 1

    var job : Job? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        recyclerView = findViewById(R.id.recyclerView)
        list = ArrayList()
        adapter = ImageAdapter(this,list)
        manager = GridLayoutManager(this,2)
        recyclerView.layoutManager = manager
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        getData()



    }

    private fun getData() {

            ApiUtils.ApiUtils.getApiInterface().getImages(page,30)
                .enqueue(object : retrofit2.Callback<List<Photo>>{
                    override fun onResponse(
                        call: Call<List<Photo>>,
                        response: Response<List<Photo>>) {

                        job = CoroutineScope(Dispatchers.IO).launch {
                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    list.addAll(response.body()!!)
                                    binding.progressBar.visibility = View.INVISIBLE
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }

                    }

                    override fun onFailure(call: Call<List<Photo>>, t: Throwable) {
                        Toast.makeText(this@MainActivity,"Error: "+t.message,Toast.LENGTH_LONG).show()
                    }

                })
        }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.menu,menu)

        var search : MenuItem = menu!!.findItem(R.id.search)
        var searchView : androidx.appcompat.widget.SearchView = search.actionView as androidx.appcompat.widget.SearchView
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchData(newText)
                return true
            }

        })


        return true
    }

    private fun searchData(query: String?) {
        ApiUtils.ApiUtils.getApiInterface().searchImage(query!!).enqueue(object : retrofit2.Callback<Search>{
            override fun onResponse(call: Call<Search>, response: Response<Search>) {

                job = CoroutineScope(Dispatchers.IO).launch {
                    if (query.isEmpty()) {
                        getData()
                    }
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            list.clear()
                            list.addAll(response.body()?.results!!)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }

            }

            override fun onFailure(call: Call<Search>, t: Throwable) {
                Toast.makeText(this@MainActivity,t.message.toString(),Toast.LENGTH_LONG).show()
            }

        })
    }


}